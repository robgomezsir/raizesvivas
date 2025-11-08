package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.local.dao.UsuarioDao
import com.raizesvivas.app.data.local.entities.toDomain
import com.raizesvivas.app.data.local.entities.toEntity
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gerenciar usuários
 * 
 * Este repository coordena dados locais (Room) e remotos (Firestore)
 * para gerenciar informações de usuários do app.
 */
@Singleton
class UsuarioRepository @Inject constructor(
    private val usuarioDao: UsuarioDao,
    private val firestoreService: FirestoreService
) {
    
    /**
     * Observa usuário por ID (do cache local)
     */
    fun observarPorId(userId: String): Flow<Usuario?> {
        return usuarioDao.observarPorId(userId)
            .map { it?.toDomain() }
    }
    
    /**
     * Busca usuário por ID (cache local primeiro)
     */
    suspend fun buscarPorId(userId: String): Usuario? {
        // Buscar no cache local
        val local = usuarioDao.buscarPorId(userId)?.toDomain()
        
        // Se não estiver no cache, buscar no Firestore
        if (local == null) {
            val remoto = firestoreService.buscarUsuario(userId).getOrNull()
            
            // Salvar no cache se encontrou
            remoto?.let {
                usuarioDao.inserir(it.toEntity())
            }
            
            return remoto
        }
        
        return local
    }
    
    /**
     * Salva usuário (local + remoto)
     */
    suspend fun salvar(usuario: Usuario): Result<Unit> {
        return try {
            // Salvar no Firestore
            val resultado = firestoreService.salvarUsuario(usuario)
            
            resultado.onSuccess {
                // Salvar no cache local
                usuarioDao.inserir(usuario.toEntity())
                Timber.d("✅ Usuário salvo: ${usuario.nome}")
            }
            
            resultado
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao salvar usuário")
            Result.failure(e)
        }
    }
    
    /**
     * Atualiza usuário existente
     */
    suspend fun atualizar(usuario: Usuario): Result<Unit> {
        return try {
            // Verificar se o ID está definido antes de atualizar
            if (usuario.id.isBlank()) {
                Timber.e("❌ Tentativa de atualizar usuário sem ID")
                return Result.failure(IllegalArgumentException("ID do usuário não pode estar vazio ao atualizar"))
            }
            
            Timber.d("📝 Atualizando usuário: ${usuario.id}")
            
            // Atualizar no Firestore
            val resultado = firestoreService.salvarUsuario(usuario)
            
            resultado.onSuccess {
                // Atualizar cache local
                usuarioDao.atualizar(usuario.toEntity())
            }
            
            resultado
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao atualizar usuário")
            Result.failure(e)
        }
    }
    
    /**
     * Atualiza flag de primeiro acesso
     */
    suspend fun atualizarPrimeiroAcesso(userId: String, valor: Boolean): Result<Unit> {
        return try {
            val usuario = buscarPorId(userId)
            
            if (usuario != null) {
                val atualizado = usuario.copy(primeiroAcesso = valor)
                atualizar(atualizado)
            } else {
                Result.failure(Exception("Usuário não encontrado"))
            }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao atualizar primeiro acesso")
            Result.failure(e)
        }
    }
    
    /**
     * Vincula pessoa ao usuário
     */
    suspend fun vincularPessoa(userId: String, pessoaId: String): Result<Unit> {
        return try {
            // Validações
            if (userId.isBlank()) {
                return Result.failure(Exception("ID do usuário não pode estar vazio"))
            }
            if (pessoaId.isBlank()) {
                return Result.failure(Exception("ID da pessoa não pode estar vazio"))
            }
            
            var usuario = buscarPorId(userId)
            
            // Se o usuário não existe, criar um básico
            if (usuario == null) {
                Timber.d("📝 Usuário não encontrado, criando novo usuário: $userId")
                val novoUsuario = Usuario(
                    id = userId,
                    nome = "",
                    email = "",
                    pessoaVinculada = pessoaId
                )
                val resultadoCriacao = salvar(novoUsuario)
                if (resultadoCriacao.isFailure) {
                    return resultadoCriacao
                }
                // Usuário criado e salvo com sucesso, não precisa atribuir a variável
            } else {
                // Verificar se o ID está definido corretamente
                if (usuario.id.isBlank()) {
                    Timber.w("⚠️ Usuário encontrado mas sem ID, corrigindo: $userId")
                    usuario = usuario.copy(id = userId)
                }
                
                // Atualizar apenas o campo pessoaVinculada
                val atualizado = usuario.copy(pessoaVinculada = pessoaId)
                val resultado = atualizar(atualizado)
                
                if (resultado.isFailure) {
                    return resultado
                }
            }
            
            Timber.d("✅ Pessoa vinculada com sucesso: usuário $userId -> pessoa $pessoaId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao vincular pessoa")
            Result.failure(e)
        }
    }
    
    /**
     * Atualiza referência à Família Zero
     */
    suspend fun atualizarFamiliaZero(userId: String, paiId: String, maeId: String): Result<Unit> {
        return try {
            val usuario = buscarPorId(userId)
            
            if (usuario != null) {
                val atualizado = usuario.copy(
                    familiaZeroPai = paiId,
                    familiaZeroMae = maeId
                )
                atualizar(atualizado)
            } else {
                Result.failure(Exception("Usuário não encontrado"))
            }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao atualizar Família Zero")
            Result.failure(e)
        }
    }
    
    /**
     * Busca todos os administradores
     */
    suspend fun buscarAdministradores(): Result<List<Usuario>> {
        return try {
            val resultado = firestoreService.buscarAdministradores()
            
            resultado.onSuccess { admins ->
                // Salvar no cache local
                admins.forEach { admin ->
                    usuarioDao.inserir(admin.toEntity())
                }
            }
            
            resultado
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar administradores")
            Result.failure(e)
        }
    }
    
    /**
     * Busca todos os usuários
     */
    suspend fun buscarTodosUsuarios(): Result<List<Usuario>> {
        return try {
            val resultado = firestoreService.buscarTodosUsuarios()
            
            resultado.onSuccess { usuarios ->
                // Salvar no cache local
                usuarios.forEach { usuario ->
                    usuarioDao.inserir(usuario.toEntity())
                }
            }
            
            resultado
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar todos os usuários")
            Result.failure(e)
        }
    }
    
    /**
     * Promove ou rebaixa um usuário a administrador
     */
    suspend fun promoverAdmin(userId: String, ehAdmin: Boolean): Result<Unit> {
        return try {
            val usuario = buscarPorId(userId)
            
            if (usuario != null) {
                val atualizado = usuario.copy(ehAdministrador = ehAdmin)
                atualizar(atualizado)
            } else {
                Result.failure(Exception("Usuário não encontrado"))
            }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao ${if (ehAdmin) "promover" else "rebaixar"} admin")
            Result.failure(e)
        }
    }
    
    /**
     * Limpa dados do usuário (logout)
     */
    suspend fun limparDados() {
        usuarioDao.deletarTodos()
    }
    
    /**
     * Verifica se é o primeiro usuário do sistema (nenhum admin existe ainda)
     */
    suspend fun ehPrimeiroUsuario(): Boolean {
        return try {
            val resultado = firestoreService.buscarAdministradores()
            
            resultado.onSuccess { admins ->
                // Se não houver nenhum admin, este é o primeiro usuário
                val isPrimeiro = admins.isEmpty()
                Timber.d("🔍 Verificando primeiro usuário: ${admins.size} admin(s) encontrado(s). É primeiro: $isPrimeiro")
                return isPrimeiro
            }
            
            // Em caso de erro, assumir que não é o primeiro (segurança)
            resultado.onFailure { error ->
                Timber.e(error, "❌ Erro ao verificar se é primeiro usuário")
            }
            
            false // Por padrão, não é o primeiro em caso de erro
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao verificar primeiro usuário")
            false
        }
    }
}

