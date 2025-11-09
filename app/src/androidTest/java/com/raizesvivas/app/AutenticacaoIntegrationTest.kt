package com.raizesvivas.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.raizesvivas.app.data.remote.firebase.AuthService
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber

/**
 * Testes de integração para fluxo de autenticação
 * 
 * Testa login, cadastro e recuperação de senha
 */
@RunWith(AndroidJUnit4::class)
class AutenticacaoIntegrationTest {
    
    private lateinit var authService: AuthService
    private lateinit var firebaseAuth: FirebaseAuth
    private val testEmail = "teste@raizesvivas.com"
    private val testPassword = "Teste123456"
    private val testNomeCompleto = "Usuário Teste"
    
    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Inicializar Firebase
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        
        firebaseAuth = FirebaseAuth.getInstance()
        authService = AuthService(firebaseAuth)
        
        Timber.d("✅ Setup completo para testes de autenticação")
    }
    
    @After
    fun tearDown() = runTest {
        // Limpar usuário de teste se existir
        try {
            val user = firebaseAuth.currentUser
            if (user != null && user.email == testEmail) {
                user.delete().await()
                Timber.d("✅ Usuário de teste deletado")
            }
        } catch (e: Exception) {
            Timber.w(e, "Erro ao deletar usuário de teste")
        }
    }
    
    @Test
    fun testCadastroUsuario() = runTest {
        Timber.d("🧪 Testando cadastro de usuário...")
        
        // Deletar usuário se já existir
        try {
            val existingUser = firebaseAuth.currentUser
            existingUser?.delete()?.await()
        } catch (_: Exception) {
            // Ignorar se não existir
        }
        
        // Cadastrar novo usuário
        val resultado = authService.cadastrar(testEmail, testPassword, testNomeCompleto)
        
        assert(resultado.isSuccess) {
            "Cadastro deve ter sucesso. Erro: ${resultado.exceptionOrNull()?.message}"
        }
        
        val usuario = authService.currentUser
        assert(usuario != null) {
            "Usuário deve estar autenticado após cadastro"
        }
        
        assert(usuario?.email == testEmail) {
            "Email do usuário deve ser $testEmail"
        }
        
        Timber.d("✅ Teste de cadastro passou")
    }
    
    @Test
    fun testLoginUsuario() = runTest {
        Timber.d("🧪 Testando login de usuário...")
        
        // Garantir que usuário existe
        try {
            val user = firebaseAuth.currentUser
            if (user == null || user.email != testEmail) {
                authService.cadastrar(testEmail, testPassword, testNomeCompleto)
            }
        } catch (_: Exception) {
            authService.cadastrar(testEmail, testPassword, testNomeCompleto)
        }
        
        // Fazer logout
        authService.logout()
        
        // Tentar login
        val resultado = authService.login(testEmail, testPassword)
        
        assert(resultado.isSuccess) {
            "Login deve ter sucesso. Erro: ${resultado.exceptionOrNull()?.message}"
        }
        
        val usuario = authService.currentUser
        assert(usuario != null) {
            "Usuário deve estar autenticado após login"
        }
        
        assert(usuario?.email == testEmail) {
            "Email do usuário deve ser $testEmail"
        }
        
        Timber.d("✅ Teste de login passou")
    }
    
    @Test
    fun testLogout() = runTest {
        Timber.d("🧪 Testando logout...")
        
        // Garantir que usuário está logado
        try {
            val user = firebaseAuth.currentUser
            if (user == null || user.email != testEmail) {
                authService.cadastrar(testEmail, testPassword, testNomeCompleto)
            }
        } catch (_: Exception) {
            authService.cadastrar(testEmail, testPassword, testNomeCompleto)
        }
        
        assert(authService.currentUser != null) {
            "Usuário deve estar logado antes do logout"
        }
        
        // Fazer logout
        authService.logout()
        
        assert(authService.currentUser == null) {
            "Usuário não deve estar autenticado após logout"
        }
        
        Timber.d("✅ Teste de logout passou")
    }
    
    @Test
    fun testLoginComCredenciaisInvalidas() = runTest {
        Timber.d("🧪 Testando login com credenciais inválidas...")
        
        // Garantir que estamos deslogados
        authService.logout()
        
        // Tentar login com credenciais inválidas
        val resultado = authService.login("email@inexistente.com", "senha123")
        
        assert(resultado.isFailure) {
            "Login deve falhar com credenciais inválidas"
        }
        
        assert(authService.currentUser == null) {
            "Usuário não deve estar autenticado após login falhado"
        }
        
        Timber.d("✅ Teste de login inválido passou")
    }
}

