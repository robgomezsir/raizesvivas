package com.raizesvivas.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.raizesvivas.app.data.local.RaizesVivasDatabase
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.domain.model.Pessoa
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber
import java.util.*

/**
 * Testes de integração para cadastro de pessoas
 * 
 * Testa criação, atualização, sincronização local↔remoto
 */
@RunWith(AndroidJUnit4::class)
class PessoaIntegrationTest {
    
    private lateinit var pessoaRepository: PessoaRepository
    private lateinit var firestoreService: FirestoreService
    private lateinit var authService: AuthService
    private lateinit var database: RaizesVivasDatabase
    private val testEmail = "teste_pessoa@raizesvivas.com"
    private val testPassword = "Teste123456"
    
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
            authService.cadastrar(testEmail, testPassword)
        } catch (e: Exception) {
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
        
        val usuarioRepository = UsuarioRepository(firestoreService, database.usuarioDao())
        pessoaRepository = PessoaRepository(
            firestoreService,
            database.pessoaDao(),
            usuarioRepository
        )
        
        Timber.d("✅ Setup completo para testes de pessoa")
    }
    
    @After
    fun tearDown() = runTest {
        // Limpar database
        database.close()
        
        // Limpar dados de teste do Firestore
        try {
            val currentUser = authService.currentUser
            if (currentUser != null) {
                // Buscar e deletar pessoas de teste
                val pessoas = pessoaRepository.buscarTodas()
                pessoas.forEach { pessoa ->
                    if (pessoa.nome.contains("TESTE")) {
                        pessoaRepository.deletar(pessoa.id)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "Erro ao limpar dados de teste")
        }
    }
    
    @Test
    fun testCriarPessoa() = runTest {
        Timber.d("🧪 Testando criação de pessoa...")
        
        val pessoa = Pessoa(
            id = UUID.randomUUID().toString(),
            nome = "TESTE - Pessoa Teste",
            dataNascimento = Date(),
            criadoPor = authService.currentUser?.uid ?: "",
            criadoEm = Date(),
            aprovado = true,
            versao = 1
        )
        
        val resultado = pessoaRepository.salvar(pessoa, ehAdmin = true)
        
        assert(resultado.isSuccess) {
            "Criação de pessoa deve ter sucesso. Erro: ${resultado.exceptionOrNull()?.message}"
        }
        
        // Verificar se pessoa foi salva localmente
        val pessoaLocal = pessoaRepository.buscarPorId(pessoa.id)
        assert(pessoaLocal != null) {
            "Pessoa deve estar salva localmente"
        }
        
        assert(pessoaLocal?.nome == pessoa.nome) {
            "Nome da pessoa deve corresponder"
        }
        
        Timber.d("✅ Teste de criação de pessoa passou")
    }
    
    @Test
    fun testAtualizarPessoa() = runTest {
        Timber.d("🧪 Testando atualização de pessoa...")
        
        // Criar pessoa
        val pessoa = Pessoa(
            id = UUID.randomUUID().toString(),
            nome = "TESTE - Pessoa Original",
            dataNascimento = Date(),
            criadoPor = authService.currentUser?.uid ?: "",
            criadoEm = Date(),
            aprovado = true,
            versao = 1
        )
        
        pessoaRepository.salvar(pessoa, ehAdmin = true)
        
        // Atualizar pessoa
        val pessoaAtualizada = pessoa.copy(
            nome = "TESTE - Pessoa Atualizada",
            versao = pessoa.versao + 1
        )
        
        val resultado = pessoaRepository.atualizar(pessoaAtualizada, ehAdmin = true)
        
        assert(resultado.isSuccess) {
            "Atualização de pessoa deve ter sucesso. Erro: ${resultado.exceptionOrNull()?.message}"
        }
        
        // Verificar atualização
        val pessoaRecuperada = pessoaRepository.buscarPorId(pessoa.id)
        assert(pessoaRecuperada != null) {
            "Pessoa deve existir após atualização"
        }
        
        assert(pessoaRecuperada?.nome == "TESTE - Pessoa Atualizada") {
            "Nome da pessoa deve estar atualizado"
        }
        
        assert(pessoaRecuperada?.versao == 2) {
            "Versão deve estar incrementada"
        }
        
        Timber.d("✅ Teste de atualização de pessoa passou")
    }
    
    @Test
    fun testDeletarPessoa() = runTest {
        Timber.d("🧪 Testando deleção de pessoa...")
        
        // Criar pessoa
        val pessoa = Pessoa(
            id = UUID.randomUUID().toString(),
            nome = "TESTE - Pessoa para Deletar",
            dataNascimento = Date(),
            criadoPor = authService.currentUser?.uid ?: "",
            criadoEm = Date(),
            aprovado = true,
            versao = 1
        )
        
        pessoaRepository.salvar(pessoa, ehAdmin = true)
        
        // Verificar que pessoa existe
        assert(pessoaRepository.buscarPorId(pessoa.id) != null) {
            "Pessoa deve existir antes de deletar"
        }
        
        // Deletar pessoa
        pessoaRepository.deletar(pessoa.id)
        
        // Verificar deleção
        val pessoaRecuperada = pessoaRepository.buscarPorId(pessoa.id)
        assert(pessoaRecuperada == null) {
            "Pessoa não deve existir após deleção"
        }
        
        Timber.d("✅ Teste de deleção de pessoa passou")
    }
    
    @Test
    fun testSincronizacaoLocalRemoto() = runTest {
        Timber.d("🧪 Testando sincronização local↔remoto...")
        
        // Criar pessoa no Firestore diretamente
        val pessoa = Pessoa(
            id = UUID.randomUUID().toString(),
            nome = "TESTE - Pessoa Remota",
            dataNascimento = Date(),
            criadoPor = authService.currentUser?.uid ?: "",
            criadoEm = Date(),
            aprovado = true,
            versao = 1
        )
        
        val resultado = firestoreService.salvarPessoa(pessoa)
        assert(resultado.isSuccess) {
            "Pessoa deve ser salva no Firestore"
        }
        
        // Buscar pessoa usando repository (deve buscar do Firestore e salvar localmente)
        val pessoaRecuperada = pessoaRepository.buscarPorId(pessoa.id)
        
        assert(pessoaRecuperada != null) {
            "Pessoa deve ser recuperada do Firestore"
        }
        
        assert(pessoaRecuperada?.nome == pessoa.nome) {
            "Nome da pessoa deve corresponder"
        }
        
        // Verificar se está salva localmente
        val pessoaLocal = database.pessoaDao().buscarPorId(pessoa.id)
        assert(pessoaLocal != null) {
            "Pessoa deve estar sincronizada localmente"
        }
        
        Timber.d("✅ Teste de sincronização passou")
    }
}

