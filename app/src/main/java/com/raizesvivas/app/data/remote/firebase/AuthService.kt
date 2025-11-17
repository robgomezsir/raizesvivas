package com.raizesvivas.app.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serviço para gerenciar autenticação Firebase
 * 
 * Responsabilidades:
 * - Login com email/senha
 * - Cadastro de novos usuários
 * - Recuperação de senha
 * - Logout
 * - Obter usuário atual
 * - Observar mudanças no estado de autenticação
 */
@Singleton
class AuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    
    /**
     * Usuário atualmente logado (null se não estiver logado)
     */
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser
    
    /**
     * Verifica se há usuário logado
     */
    val isLoggedIn: Boolean
        get() = currentUser != null
    
    /**
     * Observa mudanças no estado de autenticação
     * Emite o usuário atual quando o estado muda (login, logout, etc.)
     */
    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        
        firebaseAuth.addAuthStateListener(listener)
        
        // Emitir estado inicial
        trySend(firebaseAuth.currentUser)
        
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }
    
    /**
     * Realiza login com email e senha
     * 
     * @param email Email do usuário
     * @param senha Senha do usuário
     * @return Result com FirebaseUser ou erro
     */
    suspend fun login(email: String, senha: String): Result<FirebaseUser> {
        return try {
            Timber.d("🔐 Tentando login: $email")
            
            val result = firebaseAuth
                .signInWithEmailAndPassword(email, senha)
                .await()
            
            val user = result.user
            
            if (user != null) {
                Timber.d("✅ Login bem-sucedido: ${user.uid}")
                Result.success(user)
            } else {
                Timber.e("❌ Login falhou: usuário nulo")
                Result.failure(Exception("Erro ao fazer login"))
            }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro no login")
            Result.failure(e)
        }
    }
    
    /**
     * Cria nova conta com email e senha
     * 
     * @param email Email do novo usuário
     * @param senha Senha (mínimo 8 caracteres)
     * @param nomeCompleto Nome completo para o perfil
     * @return Result com FirebaseUser ou erro
     */
    suspend fun cadastrar(
        email: String,
        senha: String,
        nomeCompleto: String
    ): Result<FirebaseUser> {
        return try {
            Timber.d("📝 Criando conta: $email")
            
            // Criar conta
            val result = firebaseAuth
                .createUserWithEmailAndPassword(email, senha)
                .await()
            
            val user = result.user
            
            if (user != null) {
                // Atualizar nome do perfil
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(nomeCompleto)
                    .build()
                
                user.updateProfile(profileUpdates).await()
                
                Timber.d("✅ Conta criada: ${user.uid}")
                Result.success(user)
            } else {
                Timber.e("❌ Falha ao criar conta: usuário nulo")
                Result.failure(Exception("Erro ao criar conta"))
            }
            
        } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
            Timber.e(e, "❌ Email já está em uso: $email")
            Result.failure(Exception("Este email já está cadastrado. Faça login em vez de criar uma nova conta.", e))
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao criar conta")
            
            // Mensagem mais amigável para erros de configuração do Firebase
            val mensagemErro = when {
                e.message?.contains("email-already-in-use") == true ||
                e.message?.contains("EMAIL_EXISTS") == true ||
                e.message?.contains("The email address is already in use") == true -> {
                    "Este email já está cadastrado. Faça login em vez de criar uma nova conta."
                }
                e.message?.contains("CONFIGURATION_NOT_FOUND") == true -> {
                    "Erro de configuração do Firebase. Verifique se SHA-1 e SHA-256 estão configurados no Firebase Console. Consulte ORIENTAÇÕES/CORRIGIR_ERRO_FIREBASE_AUTH.md"
                }
                e.message?.contains("weak-password") == true -> {
                    "Senha muito fraca. Use pelo menos 6 caracteres."
                }
                e.message?.contains("invalid-email") == true -> {
                    "Email inválido. Verifique o formato do email."
                }
                else -> e.message ?: "Erro desconhecido ao criar conta"
            }
            
            Result.failure(Exception(mensagemErro, e))
        }
    }
    
    /**
     * Envia email de recuperação de senha
     * 
     * @param email Email cadastrado
     * @return Result indicando sucesso ou erro
     */
    suspend fun recuperarSenha(email: String): Result<Unit> {
        return try {
            Timber.d("📧 Enviando email de recuperação para: $email")
            
            firebaseAuth.sendPasswordResetEmail(email).await()
            
            Timber.d("✅ Email de recuperação enviado")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao enviar email de recuperação")
            Result.failure(e)
        }
    }
    
    /**
     * Faz logout do usuário atual
     * 
     * Nota: Limpar dados biométricos deve ser feito separadamente
     */
    fun logout() {
        Timber.d("👋 Fazendo logout")
        firebaseAuth.signOut()
    }
    
    /**
     * Reautentica o usuário (necessário para operações sensíveis)
     */
    suspend fun reautenticar(senha: String): Result<Unit> {
        return try {
            val user = currentUser ?: return Result.failure(
                Exception("Nenhum usuário logado")
            )
            
            val credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(user.email!!, senha)
            
            user.reauthenticate(credential).await()
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao reautenticar")
            Result.failure(e)
        }
    }
}

