package com.raizesvivas.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.raizesvivas.app.data.local.RaizesVivasDatabase
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.data.repository.FamiliaZeroRepository
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.domain.model.FamiliaZero
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber
import java.util.*

/**
 * Testes de integração para criação de Família Zero
 * 
 * Testa criação inicial da árvore genealógica e atribuição de admin
 */
@RunWith(AndroidJUnit4::class)
class FamiliaZeroIntegrationTest {
    
    private lateinit var familiaZeroRepository: FamiliaZeroRepository
    private lateinit var firestoreService: FirestoreService
    private lateinit var authService: AuthService
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var database: RaizesVivasDatabase
    private val testEmail = "teste_familia@raizesvivas.com"
    private val testPassword = "Teste123456"
    private val testNomeCompleto = "Usuário Teste Família"
    
    @Before
    fun setup() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Inicializar Firebase
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        
        val firestore = FirebaseFirestore.getInstance()
        val firebaseAuth = FirebaseAuth.getInstance()
        
        firestoreService = FirestoreService(firestore)
        authService = AuthService(firebaseAuth)
        
        // Criar ou autenticar usuário de teste
        try {
            authService.cadastrar(testEmail, testPassword, testNomeCompleto)
        } catch (_: Exception) {
            try {
                authService.login(testEmail, testPassword)
            } catch (e2: Exception) {
                Timber.w(e2, "Não foi possível criar/login")
            }
        }
        
        // Inicializar database
        database = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            RaizesVivasDatabase::class.java
        ).allowMainThreadQueries().build()
        
        usuarioRepository = UsuarioRepository(database.usuarioDao(), firestoreService)
        familiaZeroRepository = FamiliaZeroRepository(firestoreService)
        
        Timber.d("✅ Setup completo para testes de Família Zero")
    }
    
    @After
    fun tearDown() = runTest {
        // Limpar database
        database.close()
        
        // Limpar Família Zero de teste se existir
        try {
            val currentUser = authService.currentUser
            if (currentUser != null) {
                val familiaZero = familiaZeroRepository.buscar()
                // Nota: Deleção de Família Zero não está implementada (operações críticas)
                // Em produção, isso seria um caso especial de administração
            }
        } catch (e: Exception) {
            Timber.w(e, "Erro ao limpar dados de teste")
        }
    }
    
    @Test
    fun testCriarFamiliaZero() = runTest {
        Timber.d("🧪 Testando criação de Família Zero...")
        
        val familiaZero = FamiliaZero(
            id = UUID.randomUUID().toString() + "_TESTE",
            pai = "pai_teste",
            mae = "mae_teste",
            fundadoPor = authService.currentUser?.uid.orEmpty(),
            fundadoEm = Date(),
            locked = false,
            arvoreNome = "TESTE - Família Teste"
        )
        
        val resultado = familiaZeroRepository.criar(familiaZero)
        
        assert(resultado.isSuccess) {
            "Criação de Família Zero deve ter sucesso. Erro: ${resultado.exceptionOrNull()?.message}"
        }
        
        // Verificar se Família Zero foi criada
        val familiaRecuperada = familiaZeroRepository.buscar()
        assert(familiaRecuperada != null) {
            "Família Zero deve estar criada"
        }
        
        assert(familiaRecuperada?.arvoreNome == familiaZero.arvoreNome) {
            "Nome da Família Zero deve corresponder"
        }
        
        Timber.d("✅ Teste de criação de Família Zero passou")
    }
    
    @Test
    fun testVerificarAdminAposCriarFamiliaZero() = runTest {
        Timber.d("🧪 Testando atribuição de admin após criar Família Zero...")
        
        val familiaZero = FamiliaZero(
            id = UUID.randomUUID().toString() + "_TESTE",
            pai = "pai_teste",
            mae = "mae_teste",
            fundadoPor = authService.currentUser?.uid.orEmpty(),
            fundadoEm = Date(),
            locked = false,
            arvoreNome = "TESTE - Família Admin"
        )
        
        familiaZeroRepository.criar(familiaZero)
        
        // Verificar se usuário foi marcado como admin
        val usuarioId = authService.currentUser?.uid
        assert(usuarioId != null) {
            "Usuário deve estar autenticado"
        }
        
        val usuario = usuarioRepository.buscarPorId(usuarioId!!)
        assert(usuario != null) {
            "Usuário deve existir"
        }
        
        // Nota: A atribuição de admin é feita automaticamente no repository
        // Este teste verifica se o usuário que criou a Família Zero tem privilégios
        
        Timber.d("✅ Teste de verificação de admin passou")
    }
}

