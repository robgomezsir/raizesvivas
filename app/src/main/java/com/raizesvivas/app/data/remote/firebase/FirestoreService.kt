package com.raizesvivas.app.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.raizesvivas.app.domain.model.*
import com.raizesvivas.app.utils.RetryHelper
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serviço para operações no Cloud Firestore
 * 
 * Responsabilidades:
 * - CRUD de pessoas, usuários, convites
 * - Queries específicas
 * - Listeners em tempo real
 */
@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    // Referências de collections
    private val usersCollection = firestore.collection("users")
    private val peopleCollection = firestore.collection("people")
    private val familiaZeroCollection = firestore.collection("familia_zero")
    private val invitesCollection = firestore.collection("invites")
    private val pendingEditsCollection = firestore.collection("pending_edits")
    private val duplicatesCollection = firestore.collection("duplicates")
    private val recadosCollection = firestore.collection("recados")
    private val familiasPersonalizadasCollection = firestore.collection("familias_personalizadas")
    
    // Coleção de conquistas: usuarios/{userId}/conquistas/{conquistaId}
    private fun conquistasCollection(usuarioId: String) = 
        usersCollection.document(usuarioId).collection("conquistas")
    
    // ============================================
    // USUÁRIOS
    // ============================================
    
    /**
     * Cria ou atualiza usuário no Firestore com retry logic
     */
    suspend fun salvarUsuario(usuario: Usuario): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                // Validação: ID não pode estar vazio
                if (usuario.id.isBlank()) {
                    Timber.e("❌ Erro: ID do usuário está vazio")
                    return@withNetworkRetry Result.failure(
                        IllegalArgumentException("ID do usuário não pode estar vazio")
                    )
                }
                
                val data = hashMapOf(
                    "nome" to usuario.nome,
                    "email" to usuario.email,
                    "fotoUrl" to usuario.fotoUrl,
                    "pessoaVinculada" to usuario.pessoaVinculada,
                    "ehAdministrador" to usuario.ehAdministrador,
                    "familiaZeroPai" to usuario.familiaZeroPai,
                    "familiaZeroMae" to usuario.familiaZeroMae,
                    "primeiroAcesso" to usuario.primeiroAcesso,
                    "criadoEm" to usuario.criadoEm
                )
                
                usersCollection.document(usuario.id)
                    .set(data)
                    .await()
                
                Timber.d("✅ Usuário salvo: ${usuario.id}")
                Result.success(Unit)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao salvar usuário")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Busca usuário por ID
     */
    suspend fun buscarUsuario(userId: String): Result<Usuario?> {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            
            if (!snapshot.exists()) {
                return Result.success(null)
            }
            
            val usuario = snapshot.toObject(Usuario::class.java)
                ?.copy(id = snapshot.id) // Garantir que o ID do documento está definido
            
            if (usuario != null && usuario.id.isBlank()) {
                Timber.w("⚠️ Usuário retornado do Firestore sem ID, usando ID do documento: $userId")
                val usuarioComId = usuario.copy(id = userId)
                Result.success(usuarioComId)
            } else {
                Result.success(usuario)
            }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar usuário")
            Result.failure(e)
        }
    }
    
    /**
     * Observa usuário em tempo real
     */
    fun observarUsuario(userId: String): Flow<Usuario?> = callbackFlow {
        val registration = usersCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Erro ao observar usuário")
                    close(error)
                    return@addSnapshotListener
                }
                
                val usuario = snapshot?.toObject(Usuario::class.java)
                    ?.copy(id = snapshot.id) // Garantir que o ID do documento está definido
                trySend(usuario)
            }
        
        awaitClose { registration.remove() }
    }
    
    /**
     * Busca todos os administradores
     */
    suspend fun buscarAdministradores(): Result<List<Usuario>> {
        return try {
            val snapshot = usersCollection
                .whereEqualTo("ehAdministrador", true)
                .get()
                .await()
            
            val admins = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Usuario::class.java)?.copy(id = doc.id) // Garantir que o ID está definido
            }
            
            Result.success(admins)
            
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
            // Limite de 100 para economizar leituras e cumprir regras de segurança
            val snapshot = usersCollection
                .orderBy("nome", Query.Direction.ASCENDING)
                .limit(100)
                .get()
                .await()
            
            val usuarios = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Usuario::class.java)?.copy(id = doc.id) // Garantir que o ID está definido
            }
            
            Result.success(usuarios)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar todos os usuários")
            Result.failure(e)
        }
    }
    
    /**
     * Deleta um usuário do Firestore
     * 
     * ATENÇÃO: Isso não deleta o usuário do Firebase Auth, apenas do Firestore
     * Para deletar completamente, use Firebase Admin SDK ou Cloud Function
     */
    suspend fun deletarUsuario(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId)
                .delete()
                .await()
            
            Timber.d("✅ Usuário deletado do Firestore: $userId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao deletar usuário")
            Result.failure(e)
        }
    }
    
    // ============================================
    // FAMÍLIA ZERO
    // ============================================
    
    /**
     * Verifica se a Família Zero já foi criada
     */
    suspend fun familiaZeroExiste(): Boolean {
        return try {
            val snapshot = familiaZeroCollection.document("raiz").get().await()
            snapshot.exists()
        } catch (e: Exception) {
            Timber.e(e, "Erro ao verificar Família Zero")
            false
        }
    }
    
    /**
     * Cria a Família Zero (apenas no primeiro acesso)
     */
    suspend fun criarFamiliaZero(familiaZero: FamiliaZero): Result<Unit> {
        return try {
            // Se já existe, atualizar ao invés de falhar
            if (familiaZeroExiste()) {
                Timber.d("🔄 Família Zero já existe, atualizando...")
                return atualizarFamiliaZero(familiaZero)
            }
            
            val data = hashMapOf(
                "pai" to familiaZero.pai,
                "mae" to familiaZero.mae,
                "fundadoPor" to familiaZero.fundadoPor,
                "fundadoEm" to familiaZero.fundadoEm,
                "locked" to true,
                "arvoreNome" to familiaZero.arvoreNome
            )
            
            familiaZeroCollection.document("raiz")
                .set(data)
                .await()
            
            Timber.d("🌳 Família Zero criada!")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao criar Família Zero")
            
            // Mensagem mais amigável para erros de permissão
            val mensagemErro = when {
                e.message?.contains("PERMISSION_DENIED") == true -> {
                    "Erro de permissão do Firestore. Verifique se as regras de segurança estão configuradas corretamente. " +
                    "Consulte ORIENTAÇÕES/CORRIGIR_REGRA_FIRESTORE_FAMILIA_ZERO.md"
                }
                else -> e.message ?: "Erro desconhecido ao criar Família Zero"
            }
            
            Result.failure(Exception(mensagemErro, e))
        }
    }
    
    /**
     * Atualiza a Família Zero
     */
    suspend fun atualizarFamiliaZero(familiaZero: FamiliaZero): Result<Unit> {
        return try {
            val data = hashMapOf(
                "pai" to familiaZero.pai,
                "mae" to familiaZero.mae,
                "fundadoPor" to familiaZero.fundadoPor,
                "fundadoEm" to familiaZero.fundadoEm,
                "locked" to true,
                "arvoreNome" to familiaZero.arvoreNome
            )
            
            familiaZeroCollection.document("raiz")
                .update(data as Map<String, Any>)
                .await()
            
            Timber.d("🌳 Família Zero atualizada!")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao atualizar Família Zero")
            Result.failure(e)
        }
    }
    
    /**
     * Busca a Família Zero
     */
    suspend fun buscarFamiliaZero(): Result<FamiliaZero?> {
        return try {
            val snapshot = familiaZeroCollection.document("raiz").get().await()
            
            if (!snapshot.exists()) {
                return Result.success(null)
            }
            
            val familiaZero = snapshot.toObject(FamiliaZero::class.java)
            Result.success(familiaZero)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar Família Zero")
            Result.failure(e)
        }
    }
    
    /**
     * Observa a Família Zero em tempo real
     */
    fun observarFamiliaZero(): Flow<FamiliaZero?> = callbackFlow {
        val registration = familiaZeroCollection.document("raiz")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Erro ao observar Família Zero")
                    close(error)
                    return@addSnapshotListener
                }
                
                val familiaZero = snapshot?.toObject(FamiliaZero::class.java)
                trySend(familiaZero)
            }
        
        awaitClose { registration.remove() }
    }
    
    // ============================================
    // PESSOAS
    // ============================================
    
    /**
     * Salva pessoa no Firestore
     */
    suspend fun salvarPessoa(pessoa: Pessoa): Result<Unit> {
        return try {
            val data = hashMapOf(
                "nome" to pessoa.nome,
                "dataNascimento" to pessoa.dataNascimento,
                "dataFalecimento" to pessoa.dataFalecimento,
                "localNascimento" to pessoa.localNascimento,
                "localResidencia" to pessoa.localResidencia,
                "profissao" to pessoa.profissao,
                "biografia" to pessoa.biografia,
                "telefone" to pessoa.telefone,
                "estadoCivil" to (pessoa.estadoCivil?.name),
                "genero" to (pessoa.genero?.name),
                "pai" to pessoa.pai,
                "mae" to pessoa.mae,
                "conjugeAtual" to pessoa.conjugeAtual,
                    "exConjuges" to pessoa.exConjuges,
                    "filhos" to pessoa.filhos,
                    "familias" to pessoa.familias,
                "fotoUrl" to pessoa.fotoUrl,
                "criadoPor" to pessoa.criadoPor,
                "criadoEm" to pessoa.criadoEm,
                "modificadoPor" to pessoa.modificadoPor,
                "modificadoEm" to pessoa.modificadoEm,
                "aprovado" to pessoa.aprovado,
                "versao" to pessoa.versao,
                "ehFamiliaZero" to pessoa.ehFamiliaZero,
                "distanciaFamiliaZero" to pessoa.distanciaFamiliaZero
                // nomeNormalizado é uma propriedade calculada, não deve ser salvo
            )
            
            peopleCollection.document(pessoa.id)
                .set(data)
                .await()
            
            Timber.d("✅ Pessoa salva: ${pessoa.nome}")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao salvar pessoa")
            Result.failure(e)
        }
    }
    
    /**
     * Busca pessoa por ID
     */
    suspend fun buscarPessoa(pessoaId: String): Result<Pessoa?> {
        return try {
            val snapshot = peopleCollection.document(pessoaId).get().await()
            
            if (!snapshot.exists()) {
                return Result.success(null)
            }
            
            val pessoa = snapshot.toPessoa()
            Result.success(pessoa)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar pessoa")
            Result.failure(e)
        }
    }
    
    /**
     * Busca todas as pessoas
     */
    suspend fun buscarTodasPessoas(): Result<List<Pessoa>> {
        return try {
            Timber.d("🔍 Buscando todas as pessoas no Firestore...")
            val snapshot = peopleCollection
                .orderBy("nome", Query.Direction.ASCENDING)
                .limit(100)
                .get()
                .await()
            
            Timber.d("📦 Firestore retornou ${snapshot.documents.size} documentos")
            
            // Converter manualmente para ter melhor controle e logs
            val pessoas = mutableListOf<Pessoa>()
            snapshot.documents.forEachIndexed { index, doc ->
                try {
                    val pessoa = doc.toPessoa()
                    if (pessoa != null) {
                        // Garantir que campos obrigatórios não sejam nulos
                        val pessoaCompleta = pessoa.copy(
                            id = doc.id,
                            nome = pessoa.nome.takeIf { it.isNotBlank() } ?: "Sem nome",
                            criadoPor = pessoa.criadoPor.takeIf { it.isNotBlank() } ?: "unknown",
                            criadoEm = pessoa.criadoEm.takeIf { it.time > 0 } ?: java.util.Date(),
                            modificadoPor = pessoa.modificadoPor.takeIf { it.isNotBlank() } ?: pessoa.criadoPor,
                            modificadoEm = pessoa.modificadoEm.takeIf { it.time > 0 } ?: pessoa.criadoEm
                        )
                        pessoas.add(pessoaCompleta)
                        Timber.d("✅ Documento $index convertido: ${pessoaCompleta.nome} (ID: ${pessoaCompleta.id})")
                    } else {
                        Timber.w("⚠️ Documento $index não pôde ser convertido para Pessoa (ID: ${doc.id})")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "❌ Erro ao converter documento $index (ID: ${doc.id})")
                }
            }
            
            Timber.d("✅ ${pessoas.size} pessoas convertidas do Firestore (de ${snapshot.documents.size} documentos)")
            
            // Ordenar localmente por nome
            val pessoasOrdenadas = pessoas.sortedBy { it.nome }
            Result.success(pessoasOrdenadas)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar pessoas")
            Result.failure(e)
        }
    }
    
    /**
     * Observa todas as pessoas em tempo real
     */
    fun observarTodasPessoas(): Flow<List<Pessoa>> = callbackFlow {
        val registration = peopleCollection
            .orderBy("nome", Query.Direction.ASCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Erro ao observar pessoas")
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    Timber.d("📡 Snapshot recebido: ${snapshot.documents.size} documentos")
                    
                    // Converter manualmente para ter melhor controle e logs
                    val pessoas = mutableListOf<Pessoa>()
                    snapshot.documents.forEachIndexed { index, doc ->
                        try {
                            val pessoa = doc.toPessoa()
                            if (pessoa != null) {
                                // Garantir que campos obrigatórios não sejam nulos
                                val pessoaCompleta = pessoa.copy(
                                    id = doc.id,
                                    nome = pessoa.nome.takeIf { it.isNotBlank() } ?: "Sem nome",
                                    criadoPor = pessoa.criadoPor.takeIf { it.isNotBlank() } ?: "unknown",
                                    criadoEm = pessoa.criadoEm.takeIf { it.time > 0 } ?: java.util.Date(),
                                    modificadoPor = pessoa.modificadoPor.takeIf { it.isNotBlank() } ?: pessoa.criadoPor,
                                    modificadoEm = pessoa.modificadoEm.takeIf { it.time > 0 } ?: pessoa.criadoEm
                                )
                                pessoas.add(pessoaCompleta)
                                Timber.d("📡 Documento $index observado: ${pessoaCompleta.nome} (ID: ${pessoaCompleta.id})")
                            } else {
                                Timber.w("⚠️ Documento $index não pôde ser convertido para Pessoa (ID: ${doc.id})")
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "❌ Erro ao converter documento $index (ID: ${doc.id})")
                        }
                    }
                    
                    Timber.d("📡 ${pessoas.size} pessoas observadas (de ${snapshot.documents.size} documentos)")
                    
                    // Ordenar localmente por nome
                    val pessoasOrdenadas = pessoas.sortedBy { it.nome }
                    trySend(pessoasOrdenadas)
                } else {
                    Timber.d("📡 Snapshot nulo recebido")
                    trySend(emptyList())
                }
            }
        
        awaitClose { registration.remove() }
    }
    
    /**
     * Busca pessoas aprovadas
     */
    suspend fun buscarPessoasAprovadas(): Result<List<Pessoa>> {
        return try {
            val snapshot = peopleCollection
                .whereEqualTo("aprovado", true)
                .orderBy("nome", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val pessoas = snapshot.documents.mapNotNull { it.toPessoa() }
            Result.success(pessoas)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar pessoas aprovadas")
            Result.failure(e)
        }
    }
    
    /**
     * Busca o casal da Família Zero
     */
    suspend fun buscarCasalFamiliaZero(): Result<List<Pessoa>> {
        return try {
            val snapshot = peopleCollection
                .whereEqualTo("ehFamiliaZero", true)
                .get()
                .await()
            
            val casal = snapshot.documents.mapNotNull { it.toPessoa() }
            Result.success(casal)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar casal Família Zero")
            Result.failure(e)
        }
    }
    
    /**
     * Deleta pessoa (apenas se NÃO for Família Zero)
     */
    suspend fun deletarPessoa(pessoaId: String): Result<Unit> {
        return try {
            // Verificar se não é Família Zero
            val pessoa = buscarPessoa(pessoaId).getOrNull()
            
            if (pessoa?.ehFamiliaZero == true) {
                return Result.failure(Exception("Não é possível deletar a Família Zero!"))
            }
            
            peopleCollection.document(pessoaId).delete().await()
            Timber.d("✅ Pessoa deletada: $pessoaId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao deletar pessoa")
            Result.failure(e)
        }
    }
    
    // ============================================
    // BUSCA E QUERIES ESPECIAIS
    // ============================================
    
    /**
     * Busca pessoas por nome (pesquisa parcial)
     * Como nomeNormalizado não está mais salvo, busca por nome diretamente
     */
    suspend fun buscarPessoasPorNome(termo: String): Result<List<Pessoa>> {
        return try {
            val termoLower = termo.lowercase()
            
            // Buscar todas e filtrar localmente (nomeNormalizado é calculado)
            // Limite de 100 para economizar leituras e cumprir regras de segurança
            val snapshot = peopleCollection
                .orderBy("nome", Query.Direction.ASCENDING)
                .limit(100)
                .get()
                .await()
            val todasPessoas = snapshot.documents.mapNotNull { it.toPessoa() }
            
            // Filtrar por nome normalizado calculado
            val pessoasFiltradas = todasPessoas.filter { pessoa ->
                pessoa.nomeNormalizado.contains(termoLower)
            }
            
            Result.success(pessoasFiltradas)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar por nome")
            Result.failure(e)
        }
    }
    
    /**
     * Detecta possíveis duplicatas
     * (mesmo nome, data de nascimento e pais)
     */
    suspend fun detectarDuplicatas(
        nome: String,
        dataNascimento: Date?,
        pai: String?,
        mae: String?
    ): Result<List<Pessoa>> {
        return try {
            var query = peopleCollection
                .whereEqualTo("nome", nome)
                .orderBy("nome", Query.Direction.ASCENDING)
                .limit(100)
            
            if (dataNascimento != null) {
                query = query.whereEqualTo("dataNascimento", dataNascimento)
            }
            
            val snapshot = query.get().await()
            val possiveis = snapshot.documents.mapNotNull { it.toPessoa() }
            
            // Filtrar por pais
            val duplicatas = possiveis.filter { 
                it.pai == pai && it.mae == mae 
            }
            
            Result.success(duplicatas)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao detectar duplicatas")
            Result.failure(e)
        }
    }
    
    // ============================================
    // CONVITES
    // ============================================
    
    /**
     * Cria um novo convite
     * 
     * @param convite Convite a ser criado
     * @return Result com sucesso ou erro
     */
    suspend fun criarConvite(convite: Convite): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                val data = hashMapOf(
                    "emailConvidado" to convite.emailConvidado,
                    "convidadoPor" to convite.convidadoPor,
                    "pessoaVinculada" to convite.pessoaVinculada,
                    "status" to convite.status.name,
                    "criadoEm" to convite.criadoEm,
                    "expiraEm" to convite.expiraEm
                )
                
                invitesCollection.document(convite.id)
                    .set(data)
                    .await()
                
                Timber.d("✅ Convite criado: ${convite.id} para ${convite.emailConvidado}")
                Result.success(Unit)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao criar convite")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Busca convite por ID
     */
    suspend fun buscarConvite(conviteId: String): Result<Convite?> {
        return RetryHelper.withNetworkRetry {
            try {
                val snapshot = invitesCollection.document(conviteId).get().await()
                
                if (!snapshot.exists()) {
                    Result.success(null)
                } else {
                    // Converter mapa para Convite
                    val convite = snapshot.toConvite()
                    Result.success(convite)
                }
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao buscar convite")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Busca convites pendentes por email
     */
    suspend fun buscarConvitesPorEmail(email: String): Result<List<Convite>> {
        return RetryHelper.withNetworkRetry {
            try {
                val snapshot = invitesCollection
                    .whereEqualTo("emailConvidado", email)
                    .whereEqualTo("status", StatusConvite.PENDENTE.name)
                    .get()
                    .await()
                
                val convites = snapshot.documents.map { it.toConvite() }
                Result.success(convites)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao buscar convites por email")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Busca todos os convites (para admin)
     */
    suspend fun buscarTodosConvites(): Result<List<Convite>> {
        return RetryHelper.withNetworkRetry {
            try {
                // Limite de 100 para economizar leituras e cumprir regras de segurança
                val snapshot = invitesCollection
                    .orderBy("criadoEm", Query.Direction.DESCENDING)
                    .limit(100)
                    .get()
                    .await()
                val convites = snapshot.documents.map { it.toConvite() }
                Result.success(convites)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao buscar todos os convites")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Atualiza status do convite
     */
    suspend fun atualizarStatusConvite(
        conviteId: String,
        status: StatusConvite
    ): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                invitesCollection.document(conviteId)
                    .update("status", status.name)
                    .await()
                
                Timber.d("✅ Status do convite $conviteId atualizado para $status")
                Result.success(Unit)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao atualizar status do convite")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Aceita convite (atualiza status e vincula pessoa se necessário)
     */
    suspend fun aceitarConvite(
        conviteId: String,
        userId: String,
        pessoaId: String?
    ): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                val batch = firestore.batch()
                
                // Atualizar status do convite
                val conviteRef = invitesCollection.document(conviteId)
                batch.update(conviteRef, "status", StatusConvite.ACEITO.name)
                
                // Se tem pessoa vinculada, atualizar usuário
                pessoaId?.let {
                    val userRef = usersCollection.document(userId)
                    batch.update(userRef, "pessoaVinculada", pessoaId)
                }
                
                batch.commit().await()
                
                Timber.d("✅ Convite $conviteId aceito pelo usuário $userId")
                Result.success(Unit)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao aceitar convite")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Rejeita convite
     */
    suspend fun rejeitarConvite(conviteId: String): Result<Unit> {
        return atualizarStatusConvite(conviteId, StatusConvite.REJEITADO)
    }
    
    /**
     * Deleta convite
     */
    suspend fun deletarConvite(conviteId: String): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                invitesCollection.document(conviteId).delete().await()
                Timber.d("✅ Convite deletado: $conviteId")
                Result.success(Unit)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao deletar convite")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Observa convites pendentes em tempo real
     */
    fun observarConvitesPendentes(email: String): Flow<List<Convite>> = callbackFlow {
        val registration = invitesCollection
            .whereEqualTo("emailConvidado", email)
            .whereEqualTo("status", StatusConvite.PENDENTE.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Erro ao observar convites")
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val convites = snapshot.documents.map { it.toConvite() }
                    trySend(convites.filter { it.estaValido })
                }
            }
        
        awaitClose { registration.remove() }
    }
    
    /**
     * Helper para converter DocumentSnapshot para Convite
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toConvite(): Convite {
        val data = this.data ?: return Convite()
        
        return Convite(
            id = id,
            emailConvidado = data["emailConvidado"] as? String ?: "",
            convidadoPor = data["convidadoPor"] as? String ?: "",
            pessoaVinculada = data["pessoaVinculada"] as? String,
            status = try {
                StatusConvite.valueOf(data["status"] as? String ?: StatusConvite.PENDENTE.name)
            } catch (e: Exception) {
                StatusConvite.PENDENTE
            },
            criadoEm = (data["criadoEm"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
            expiraEm = (data["expiraEm"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
        )
    }
    
    // ============================================
    // EDIÇÕES PENDENTES
    // ============================================
    
    /**
     * Cria uma nova edição pendente
     */
    suspend fun criarEdicaoPendente(edicao: EdicaoPendente): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                val data = hashMapOf(
                    "pessoaId" to edicao.pessoaId,
                    "camposAlterados" to edicao.camposAlterados,
                    "editadoPor" to edicao.editadoPor,
                    "status" to edicao.status.name,
                    "criadoEm" to edicao.criadoEm,
                    "revisadoEm" to edicao.revisadoEm,
                    "revisadoPor" to edicao.revisadoPor
                )
                
                pendingEditsCollection.document(edicao.id)
                    .set(data)
                    .await()
                
                Timber.d("✅ Edição pendente criada: ${edicao.id} para pessoa ${edicao.pessoaId}")
                Result.success(Unit)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao criar edição pendente")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Busca todas as edições pendentes
     */
    suspend fun buscarTodasEdicoesPendentes(): Result<List<EdicaoPendente>> {
        return RetryHelper.withNetworkRetry {
            try {
                val snapshot = pendingEditsCollection
                    .whereEqualTo("status", StatusEdicao.PENDENTE.name)
                    .get()
                    .await()
                
                val edicoes = snapshot.documents.map { it.toEdicaoPendente() }
                Result.success(edicoes)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao buscar edições pendentes")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Busca edições pendentes por pessoa
     */
    suspend fun buscarEdicoesPorPessoa(pessoaId: String): Result<List<EdicaoPendente>> {
        return RetryHelper.withNetworkRetry {
            try {
                val snapshot = pendingEditsCollection
                    .whereEqualTo("pessoaId", pessoaId)
                    .whereEqualTo("status", StatusEdicao.PENDENTE.name)
                    .get()
                    .await()
                
                val edicoes = snapshot.documents.map { it.toEdicaoPendente() }
                Result.success(edicoes)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao buscar edições por pessoa")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Aprova uma edição pendente e aplica as mudanças
     */
    suspend fun aprovarEdicao(
        edicaoId: String,
        revisadoPor: String
    ): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                // Buscar edição pendente
                val edicaoSnapshot = pendingEditsCollection.document(edicaoId).get().await()
                
                if (!edicaoSnapshot.exists()) {
                    Result.failure(Exception("Edição pendente não encontrada"))
                } else {
                    val edicao = edicaoSnapshot.toEdicaoPendente()
                    
                    // Buscar pessoa original
                    val pessoaSnapshot = peopleCollection
                        .document(edicao.pessoaId)
                        .get()
                        .await()
                    
                    if (!pessoaSnapshot.exists()) {
                        Result.failure(Exception("Pessoa não encontrada"))
                    } else {
                        val pessoaOriginal = pessoaSnapshot.toPessoa()
                            ?: return@withNetworkRetry Result.failure(Exception("Erro ao converter pessoa"))
                        
                        // Aplicar mudanças
                        val pessoaAtualizada = aplicarMudancas(pessoaOriginal, edicao.camposAlterados)
                        
                        // Batch update: atualizar pessoa e marcar edição como aprovada
                        val batch = firestore.batch()
                        
                        // Salvar pessoa atualizada usando set() completo
                        val pessoaData = hashMapOf(
                            "nome" to pessoaAtualizada.nome,
                            "dataNascimento" to pessoaAtualizada.dataNascimento,
                            "dataFalecimento" to pessoaAtualizada.dataFalecimento,
                            "localNascimento" to pessoaAtualizada.localNascimento,
                            "localResidencia" to pessoaAtualizada.localResidencia,
                            "profissao" to pessoaAtualizada.profissao,
                            "biografia" to pessoaAtualizada.biografia,
                            "estadoCivil" to (pessoaAtualizada.estadoCivil?.name),
                            "genero" to (pessoaAtualizada.genero?.name),
                            "pai" to pessoaAtualizada.pai,
                            "mae" to pessoaAtualizada.mae,
                            "conjugeAtual" to pessoaAtualizada.conjugeAtual,
                            "exConjuges" to pessoaAtualizada.exConjuges,
                            "filhos" to pessoaAtualizada.filhos,
                            "familias" to pessoaAtualizada.familias,
                            "fotoUrl" to pessoaAtualizada.fotoUrl,
                            "criadoPor" to pessoaAtualizada.criadoPor,
                            "criadoEm" to pessoaAtualizada.criadoEm,
                            "modificadoPor" to pessoaAtualizada.modificadoPor,
                            "modificadoEm" to pessoaAtualizada.modificadoEm,
                            "aprovado" to pessoaAtualizada.aprovado,
                            "versao" to pessoaAtualizada.versao,
                            "ehFamiliaZero" to pessoaAtualizada.ehFamiliaZero,
                            "distanciaFamiliaZero" to pessoaAtualizada.distanciaFamiliaZero
                            // nomeNormalizado é uma propriedade calculada, não deve ser salvo
                        )
                        
                        val pessoaRef = peopleCollection.document(edicao.pessoaId)
                        batch.set(pessoaRef, pessoaData)
                        
                        val edicaoRef = pendingEditsCollection.document(edicaoId)
                        batch.update(edicaoRef, mapOf(
                            "status" to StatusEdicao.APROVADA.name,
                            "revisadoPor" to revisadoPor,
                            "revisadoEm" to Date()
                        ))
                        
                        batch.commit().await()
                        
                        Timber.d("✅ Edição $edicaoId aprovada e aplicada")
                        Result.success(Unit)
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao aprovar edição")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Rejeita uma edição pendente
     */
    suspend fun rejeitarEdicao(
        edicaoId: String,
        revisadoPor: String
    ): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                pendingEditsCollection.document(edicaoId)
                    .update(
                        mapOf(
                            "status" to StatusEdicao.REJEITADA.name,
                            "revisadoPor" to revisadoPor,
                            "revisadoEm" to Date()
                        )
                    )
                    .await()
                
                Timber.d("✅ Edição $edicaoId rejeitada")
                Result.success(Unit)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao rejeitar edição")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Observa edições pendentes em tempo real
     */
    fun observarEdicoesPendentes(): Flow<List<EdicaoPendente>> = callbackFlow {
        val registration = pendingEditsCollection
            .whereEqualTo("status", StatusEdicao.PENDENTE.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Erro ao observar edições pendentes")
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val edicoes = snapshot.documents.map { it.toEdicaoPendente() }
                    trySend(edicoes)
                }
            }
        
        awaitClose { registration.remove() }
    }
    
    /**
     * Helper para aplicar mudanças a uma pessoa
     */
    private fun aplicarMudancas(
        pessoaOriginal: Pessoa,
        camposAlterados: Map<String, Any>
    ): Pessoa {
        var pessoaAtualizada = pessoaOriginal
        
        camposAlterados.forEach { (campo, valor) ->
            pessoaAtualizada = when (campo) {
                "nome" -> pessoaAtualizada.copy(nome = valor as String)
                "dataNascimento" -> pessoaAtualizada.copy(dataNascimento = valor as? Date)
                "dataFalecimento" -> pessoaAtualizada.copy(dataFalecimento = valor as? Date)
                "localNascimento" -> pessoaAtualizada.copy(localNascimento = valor as? String)
                "localResidencia" -> pessoaAtualizada.copy(localResidencia = valor as? String)
                "profissao" -> pessoaAtualizada.copy(profissao = valor as? String)
                "biografia" -> pessoaAtualizada.copy(biografia = valor as? String)
                "estadoCivil" -> pessoaAtualizada.copy(
                    estadoCivil = (valor as? String)?.let {
                        try {
                            EstadoCivil.valueOf(it)
                        } catch (e: Exception) {
                            null
                        }
                    }
                )
                "genero" -> pessoaAtualizada.copy(
                    genero = (valor as? String)?.let {
                        try {
                            Genero.valueOf(it)
                        } catch (e: Exception) {
                            null
                        }
                    }
                )
                "pai" -> pessoaAtualizada.copy(pai = valor as? String)
                "mae" -> pessoaAtualizada.copy(mae = valor as? String)
                "conjugeAtual" -> pessoaAtualizada.copy(conjugeAtual = valor as? String)
                "filhos" -> pessoaAtualizada.copy(
                    filhos = (valor as? List<*>)
                        ?.filterIsInstance<String>()
                        ?: emptyList()
                )
                else -> pessoaAtualizada
            }
        }
        
        return pessoaAtualizada.copy(
            modificadoEm = Date(),
            versao = pessoaOriginal.versao + 1
        )
    }
    
    /**
     * Helper para converter DocumentSnapshot para EdicaoPendente
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toEdicaoPendente(): EdicaoPendente {
        val data = this.data ?: return EdicaoPendente()
        
        @Suppress("UNCHECKED_CAST")
        return EdicaoPendente(
            id = id,
            pessoaId = data["pessoaId"] as? String ?: "",
            camposAlterados = (data["camposAlterados"] as? Map<String, Any>) ?: emptyMap(),
            editadoPor = data["editadoPor"] as? String ?: "",
            status = try {
                StatusEdicao.valueOf(data["status"] as? String ?: StatusEdicao.PENDENTE.name)
            } catch (e: Exception) {
                StatusEdicao.PENDENTE
            },
            criadoEm = (data["criadoEm"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
            revisadoEm = (data["revisadoEm"] as? com.google.firebase.Timestamp)?.toDate(),
            revisadoPor = data["revisadoPor"] as? String
        )
    }
    
    // ============================================
    // SUBFAMÍLIAS
    // ============================================
    
    // Referências de collections
    private val subfamiliasCollection = firestore.collection("subfamilias")
    private val sugestoesSubfamiliasCollection = firestore.collection("sugestoes_subfamilias")
    private val membrosFamiliasCollection = firestore.collection("membros_familias")
    
    /**
     * Salva subfamília no Firestore
     */
    suspend fun salvarSubfamilia(subfamilia: Subfamilia): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                if (subfamilia.id.isBlank()) {
                    return@withNetworkRetry Result.failure(
                        IllegalArgumentException("ID da subfamília não pode estar vazio")
                    )
                }
                
                val data = hashMapOf(
                    "nome" to subfamilia.nome,
                    "tipo" to subfamilia.tipo.name,
                    "familiaPaiId" to subfamilia.familiaPaiId,
                    "membroOrigem1Id" to subfamilia.membroOrigem1Id,
                    "membroOrigem2Id" to subfamilia.membroOrigem2Id,
                    "nivelHierarquico" to subfamilia.nivelHierarquico,
                    "criadoEm" to subfamilia.criadoEm,
                    "criadoPor" to subfamilia.criadoPor,
                    "descricao" to (subfamilia.descricao ?: ""),
                    "ativa" to subfamilia.ativa
                )
                
                subfamiliasCollection.document(subfamilia.id)
                    .set(data)
                    .await()
                
                Timber.d("✅ Subfamília salva no Firestore: ${subfamilia.id}")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao salvar subfamília no Firestore")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Busca subfamília por ID
     */
    suspend fun buscarSubfamilia(subfamiliaId: String): Result<Subfamilia?> {
        return try {
            val snapshot = subfamiliasCollection.document(subfamiliaId).get().await()
            
            if (!snapshot.exists()) {
                return Result.success(null)
            }
            
            val subfamilia = snapshot.toSubfamilia()
            Result.success(subfamilia)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar subfamília")
            Result.failure(e)
        }
    }
    
    /**
     * Busca todas as subfamílias
     */
    suspend fun buscarTodasSubfamilias(): Result<List<Subfamilia>> {
        return try {
            // Limite de 100 para economizar leituras e cumprir regras de segurança
            val snapshot = subfamiliasCollection
                .whereEqualTo("ativa", true)
                .orderBy("nome", Query.Direction.ASCENDING)
                .limit(100)
                .get()
                .await()
            
            val subfamilias = snapshot.documents.mapNotNull { it.toSubfamilia() }
            Result.success(subfamilias)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar subfamílias")
            Result.failure(e)
        }
    }
    
    /**
     * Deleta subfamília
     */
    suspend fun deletarSubfamilia(subfamiliaId: String): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                subfamiliasCollection.document(subfamiliaId).delete().await()
                Timber.d("✅ Subfamília deletada: $subfamiliaId")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao deletar subfamília")
                Result.failure(e)
            }
        }
    }
    
    // ============================================
    // FAMÍLIAS PERSONALIZADAS
    // ============================================
    
    suspend fun salvarFamiliaPersonalizada(familia: FamiliaPersonalizada): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                if (familia.familiaId.isBlank()) {
                    return@withNetworkRetry Result.failure(IllegalArgumentException("familiaId não pode ser vazio"))
                }
                
                val data = hashMapOf(
                    "familiaId" to familia.familiaId,
                    "nome" to familia.nome,
                    "conjuguePrincipalId" to familia.conjuguePrincipalId,
                    "conjugueSecundarioId" to familia.conjugueSecundarioId,
                    "ehFamiliaZero" to familia.ehFamiliaZero,
                    "atualizadoPor" to familia.atualizadoPor,
                    "atualizadoEm" to familia.atualizadoEm
                )
                
                familiasPersonalizadasCollection
                    .document(familia.familiaId)
                    .set(data)
                    .await()
                
                Timber.d("✅ Família personalizada salva: ${familia.familiaId} -> ${familia.nome}")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao salvar família personalizada")
                Result.failure(e)
            }
        }
    }
    
    suspend fun buscarFamiliasPersonalizadas(): Result<List<FamiliaPersonalizada>> {
        return RetryHelper.withNetworkRetry {
            try {
                // Limite de 100 para economizar leituras e cumprir regras de segurança
                val snapshot = familiasPersonalizadasCollection
                    .orderBy("nome", Query.Direction.ASCENDING)
                    .limit(100)
                    .get()
                    .await()
                val familias = snapshot.documents.mapNotNull { it.toFamiliaPersonalizada() }
                Timber.d("📚 Encontradas ${familias.size} famílias personalizadas")
                Result.success(familias)
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao buscar famílias personalizadas")
                Result.failure(e)
            }
        }
    }
    
    fun observarFamiliasPersonalizadas(): Flow<List<FamiliaPersonalizada>> = callbackFlow {
        // Limite de 100 para economizar leituras e cumprir regras de segurança
        val listener = familiasPersonalizadasCollection
            .orderBy("nome", Query.Direction.ASCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "❌ Erro ao observar famílias personalizadas")
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            val familias = snapshot?.documents?.mapNotNull { it.toFamiliaPersonalizada() } ?: emptyList()
            trySend(familias).isSuccess
        }
        
        awaitClose { listener.remove() }
    }
    
    // ============================================
    // MEMBROS DE FAMÍLIAS
    // ============================================
    
    /**
     * Salva membro de família no Firestore
     */
    suspend fun salvarMembroFamilia(membroFamilia: MembroFamilia): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                val data = hashMapOf(
                    "membroId" to membroFamilia.membroId,
                    "familiaId" to membroFamilia.familiaId,
                    "papelNaFamilia" to membroFamilia.papelNaFamilia.name,
                    "elementoNestaFamilia" to membroFamilia.elementoNestaFamilia.name,
                    "geracaoNaFamilia" to membroFamilia.geracaoNaFamilia
                )
                
                membrosFamiliasCollection.document(membroFamilia.id)
                    .set(data)
                    .await()
                
                Timber.d("✅ Membro de família salvo: ${membroFamilia.id}")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao salvar membro de família")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Deleta membro de família
     */
    suspend fun deletarMembroFamilia(membroId: String, familiaId: String): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                val id = "${membroId}_${familiaId}"
                membrosFamiliasCollection.document(id).delete().await()
                Timber.d("✅ Membro de família deletado: $id")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao deletar membro de família")
                Result.failure(e)
            }
        }
    }
    
    // ============================================
    // SUGESTÕES DE SUBFAMÍLIAS
    // ============================================
    
    /**
     * Salva sugestão de subfamília no Firestore
     */
    suspend fun salvarSugestaoSubfamilia(sugestao: SugestaoSubfamilia): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                if (sugestao.id.isBlank()) {
                    return@withNetworkRetry Result.failure(
                        IllegalArgumentException("ID da sugestão não pode estar vazio")
                    )
                }
                
                val data = hashMapOf(
                    "membro1Id" to sugestao.membro1Id,
                    "membro2Id" to sugestao.membro2Id,
                    "nomeSugerido" to sugestao.nomeSugerido,
                    "membrosIncluidos" to sugestao.membrosIncluidos,
                    "status" to sugestao.status.name,
                    "criadoEm" to sugestao.criadoEm,
                    "processadoEm" to (sugestao.processadoEm ?: ""),
                    "usuarioId" to sugestao.usuarioId,
                    "familiaZeroId" to sugestao.familiaZeroId
                )
                
                sugestoesSubfamiliasCollection.document(sugestao.id)
                    .set(data)
                    .await()
                
                Timber.d("✅ Sugestão salva no Firestore: ${sugestao.id}")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao salvar sugestão no Firestore")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Atualiza status de sugestão
     */
    suspend fun atualizarStatusSugestao(
        sugestaoId: String,
        status: com.raizesvivas.app.domain.model.StatusSugestao
    ): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                val data = hashMapOf<String, Any>(
                    "status" to status.name,
                    "processadoEm" to Date()
                )
                
                sugestoesSubfamiliasCollection.document(sugestaoId)
                    .update(data as Map<String, Any>)
                    .await()
                
                Timber.d("✅ Status da sugestão atualizado: $sugestaoId -> $status")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao atualizar status da sugestão")
                Result.failure(e)
            }
        }
    }
    
    // ============================================
    // HELPERS DE CONVERSÃO
    // ============================================
    
    /**
     * Helper para converter DocumentSnapshot para Pessoa (com conversão manual de gênero)
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toPessoa(): Pessoa? {
        return try {
            val pessoa = this.toObject(Pessoa::class.java) ?: return null
            
            // Converter gênero manualmente se necessário (pode vir como String do Firestore)
            val genero = when {
                pessoa.genero != null -> pessoa.genero // Já está convertido
                else -> {
                    val generoString = this.data?.get("genero") as? String
                    generoString?.let {
                        try {
                            Genero.valueOf(it)
                        } catch (e: Exception) {
                            Timber.w("⚠️ Gênero inválido no Firestore: $it")
                            null
                        }
                    }
                }
            }
            
            pessoa.copy(genero = genero)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao converter DocumentSnapshot para Pessoa")
            null
        }
    }
    
    /**
     * Helper para converter DocumentSnapshot para Subfamilia
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toSubfamilia(): Subfamilia {
        val data = this.data ?: return Subfamilia()
        
        return Subfamilia(
            id = id,
            nome = data["nome"] as? String ?: "",
            tipo = try {
                TipoFamilia.valueOf(data["tipo"] as? String ?: TipoFamilia.SUBFAMILIA.name)
            } catch (e: Exception) {
                TipoFamilia.SUBFAMILIA
            },
            familiaPaiId = data["familiaPaiId"] as? String ?: "",
            membroOrigem1Id = data["membroOrigem1Id"] as? String ?: "",
            membroOrigem2Id = data["membroOrigem2Id"] as? String ?: "",
            nivelHierarquico = (data["nivelHierarquico"] as? Long)?.toInt() ?: 1,
            criadoEm = (data["criadoEm"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
            criadoPor = data["criadoPor"] as? String ?: "",
            descricao = data["descricao"] as? String,
            ativa = data["ativa"] as? Boolean ?: true
        )
    }
    
    private fun com.google.firebase.firestore.DocumentSnapshot.toFamiliaPersonalizada(): FamiliaPersonalizada? {
        val data = this.data ?: return null
        
        val atualizadoEm = when (val valor = data["atualizadoEm"]) {
            is com.google.firebase.Timestamp -> valor.toDate()
            is Date -> valor
            else -> Date()
        }
        
        val familiaId = (data["familiaId"] as? String)?.takeIf { it.isNotBlank() } ?: id
        val nome = data["nome"] as? String ?: return null
        
        return FamiliaPersonalizada(
            familiaId = familiaId,
            nome = nome,
            conjuguePrincipalId = data["conjuguePrincipalId"] as? String,
            conjugueSecundarioId = data["conjugueSecundarioId"] as? String,
            ehFamiliaZero = data["ehFamiliaZero"] as? Boolean ?: false,
            atualizadoPor = data["atualizadoPor"] as? String,
            atualizadoEm = atualizadoEm
        )
    }
    
    // ============================================
    // RECADOS
    // ============================================
    
    /**
     * Salva um recado no Firestore
     */
    suspend fun salvarRecado(recado: Recado): Result<Recado> {
        return RetryHelper.withNetworkRetry {
            try {
                // Validações básicas
                if (recado.autorId.isBlank()) {
                    Timber.e("❌ Erro: autorId do recado está vazio")
                    return@withNetworkRetry Result.failure(
                        IllegalArgumentException("ID do autor não pode estar vazio")
                    )
                }
                
                if (recado.titulo.isBlank() && recado.mensagem.isBlank()) {
                    Timber.e("❌ Erro: título e mensagem do recado estão vazios")
                    return@withNetworkRetry Result.failure(
                        IllegalArgumentException("Título ou mensagem deve ser preenchido")
                    )
                }
                
                val data = hashMapOf(
                    "autorId" to recado.autorId,
                    "autorNome" to recado.autorNome,
                    "destinatarioId" to recado.destinatarioId,
                    "destinatarioNome" to recado.destinatarioNome,
                    "titulo" to recado.titulo.trim(),
                    "mensagem" to recado.mensagem.trim(),
                    "cor" to recado.cor,
                    "criadoEm" to com.google.firebase.Timestamp(recado.criadoEm),
                    "atualizadoEm" to com.google.firebase.Timestamp(recado.atualizadoEm),
                    // NOTA: Campo "deletado" removido - agora fazemos hard delete (exclusão definitiva)
                    "fixado" to recado.fixado,
                    "fixadoAte" to (recado.fixadoAte?.let { com.google.firebase.Timestamp(it) }),
                    "fixadoPor" to recado.fixadoPor,
                    "apoiosFamiliares" to recado.apoiosFamiliares
                )
                
                val docRef = if (recado.id.isBlank()) {
                    recadosCollection.document()
                } else {
                    recadosCollection.document(recado.id)
                }
                
                Timber.d("💾 Salvando recado no documento: ${docRef.id}")
                docRef.set(data).await()
                
                val recadoSalvo = recado.copy(id = docRef.id)
                Timber.d("✅ Recado salvo com sucesso: ${recadoSalvo.id} | Título: ${recadoSalvo.titulo}")
                Result.success(recadoSalvo)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao salvar recado: ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Busca todos os recados (gerais e direcionados ao usuário/pessoa)
     * 
     * @param filtroId ID da pessoa vinculada ao usuário (ou userId como fallback)
     * @param autorId ID do usuário autenticado (para filtrar recados criados por ele)
     */
    suspend fun buscarRecados(filtroId: String, autorId: String): Result<List<Recado>> {
        return RetryHelper.withNetworkRetry {
            try {
                // NOTA: Não é mais necessário filtrar por "deletado" pois agora fazemos hard delete
                // Buscar recados gerais (destinatarioId == null) e direcionados
                // Como não podemos fazer query OR no Firestore, buscamos todos e filtramos
                // Limite de 100 para economizar leituras e cumprir regras de segurança
                val snapshot = recadosCollection
                    .orderBy("criadoEm", Query.Direction.DESCENDING)
                    .limit(100)
                    .get()
                    .await()
                
                val recados = snapshot.documents.mapNotNull { it.toRecado() }
                    .filter { recado ->
                        // Filtrar recados expirados (exceto fixados)
                        if (recado.estaExpirado() && !recado.estaFixadoEValido()) {
                            return@filter false
                        }
                        
                        // Filtrar: 
                        // - Recados gerais (destinatarioId == null)
                        // - Recados direcionados ao filtroId (pessoa vinculada)
                        // - Recados criados pelo próprio usuário (sempre visíveis)
                        recado.ehGeral || recado.destinatarioId == filtroId || recado.autorId == autorId
                    }
                
                Result.success(recados)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao buscar recados")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Observa recados em tempo real
     * 
     * @param filtroId ID da pessoa vinculada ao usuário (ou userId como fallback)
     * @param autorId ID do usuário autenticado (para filtrar recados criados por ele)
     */
    fun observarRecados(filtroId: String, autorId: String): Flow<List<Recado>> = callbackFlow {
        try {
            // NOTA: Não é mais necessário filtrar por "deletado" pois agora fazemos hard delete
            // Apenas ordenar por data de criação (descendente)
            // Limite de 100 para economizar leituras e cumprir regras de segurança
            val registration = recadosCollection
                .orderBy("criadoEm", Query.Direction.DESCENDING)
                .limit(100)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        val errorMessage = error.message ?: "Erro desconhecido"
                        Timber.e(error, "❌ Erro ao observar recados: $errorMessage")
                        
                        // Não fechar o channel, apenas logar o erro e emitir lista vazia
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        try {
                            val recados = snapshot.documents.mapNotNull { doc ->
                                try {
                                    doc.toRecado()
                                } catch (e: Exception) {
                                    Timber.e(e, "❌ Erro ao converter documento para Recado: ${doc.id}")
                                    null
                                }
                            }.filter { recado ->
                                // Filtrar recados expirados (exceto fixados)
                                if (recado.estaExpirado() && !recado.estaFixadoEValido()) {
                                    return@filter false
                                }
                                
                                // Filtrar: 
                                // - Recados gerais (destinatarioId == null)
                                // - Recados direcionados ao filtroId (pessoa vinculada)
                                // - Recados criados pelo próprio usuário (sempre visíveis)
                                recado.ehGeral || recado.destinatarioId == filtroId || recado.autorId == autorId
                            }
                            Timber.d("📨 Recados observados: ${recados.size} recados (filtroId: $filtroId)")
                            trySend(recados)
                        } catch (e: Exception) {
                            Timber.e(e, "❌ Erro ao processar recados")
                            trySend(emptyList())
                        }
                    } else {
                        Timber.w("⚠️ Snapshot de recados é null")
                        trySend(emptyList())
                    }
                }
            
            awaitClose { 
                Timber.d("🔌 Fechando observação de recados")
                registration.remove() 
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao configurar observação de recados")
            close(e)
        }
    }
    
    /**
     * Atualiza um recado
     */
    suspend fun atualizarRecado(recado: Recado): Result<Recado> {
        return RetryHelper.withNetworkRetry {
            try {
                if (recado.id.isBlank()) {
                    return@withNetworkRetry Result.failure(
                        IllegalArgumentException("ID do recado não pode estar vazio")
                    )
                }
                
                val data = hashMapOf(
                    "titulo" to recado.titulo,
                    "mensagem" to recado.mensagem,
                    "cor" to recado.cor,
                    "atualizadoEm" to com.google.firebase.Timestamp.now()
                )
                
                recadosCollection.document(recado.id)
                    .update(data as Map<String, Any>)
                    .await()
                
                Timber.d("✅ Recado atualizado: ${recado.id}")
                Result.success(recado.copy(atualizadoEm = java.util.Date()))
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao atualizar recado")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Deleta um recado definitivamente do Firestore
     * Admins podem deletar todos os recados
     * IMPORTANTE: Esta é uma exclusão permanente (hard delete), o documento será completamente removido
     */
    suspend fun deletarRecado(recadoId: String, userId: String, isAdmin: Boolean = false): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                // Se não é admin, verificar se o usuário é o autor
                if (!isAdmin) {
                    val recadoSnapshot = recadosCollection.document(recadoId).get().await()
                    val recado = recadoSnapshot.toRecado()
                    
                    if (recado == null || recado.autorId != userId) {
                        return@withNetworkRetry Result.failure(
                            Exception("Apenas o autor ou um administrador pode deletar o recado")
                        )
                    }
                }
                
                // Exclusão definitiva: remover o documento completamente do Firestore
                recadosCollection.document(recadoId)
                    .delete()
                    .await()
                
                Timber.d("✅ Recado deletado permanentemente do Firestore: $recadoId")
                Result.success(Unit)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao deletar recado permanentemente")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Helper para converter DocumentSnapshot para Recado
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toRecado(): Recado? {
        val data = this.data ?: return null
        
        // Converter apoiosFamiliares de List<Any> para List<String>
        val apoiosFamiliares = (data["apoiosFamiliares"] as? List<*>)?.mapNotNull { 
            it as? String 
        } ?: emptyList()
        
        return Recado(
            id = id,
            autorId = data["autorId"] as? String ?: "",
            autorNome = data["autorNome"] as? String ?: "",
            destinatarioId = data["destinatarioId"] as? String,
            destinatarioNome = data["destinatarioNome"] as? String,
            titulo = data["titulo"] as? String ?: "",
            mensagem = data["mensagem"] as? String ?: "",
            cor = data["cor"] as? String ?: "primary",
            criadoEm = (data["criadoEm"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
            atualizadoEm = (data["atualizadoEm"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
            deletado = data["deletado"] as? Boolean ?: false,
            fixado = data["fixado"] as? Boolean ?: false,
            fixadoAte = (data["fixadoAte"] as? com.google.firebase.Timestamp)?.toDate(),
            fixadoPor = data["fixadoPor"] as? String,
            apoiosFamiliares = apoiosFamiliares
        )
    }
    
    /**
     * Fixa ou desfixa um recado (apenas admin)
     * @param recadoId ID do recado
     * @param fixado Se deve fixar (true) ou desfixar (false)
     * @param fixadoAte Data até quando fixar (null = permanentemente)
     * @param adminId ID do admin que está fixando
     */
    suspend fun fixarRecado(
        recadoId: String,
        fixado: Boolean,
        fixadoAte: Date? = null,
        adminId: String
    ): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                val updateData = hashMapOf<String, Any>(
                    "fixado" to fixado,
                    "atualizadoEm" to com.google.firebase.Timestamp.now()
                )
                
                if (fixado) {
                    // Se fixadoAte for null, significa fixação permanente (não expira)
                    if (fixadoAte != null) {
                        updateData["fixadoAte"] = com.google.firebase.Timestamp(fixadoAte)
                    }
                    // Se fixadoAte for null, não adicionar o campo (fixação permanente)
                    updateData["fixadoPor"] = adminId
                } else {
                    // Desfixar: usar FieldValue.delete() para remover campos
                    updateData["fixadoAte"] = com.google.firebase.firestore.FieldValue.delete()
                    updateData["fixadoPor"] = com.google.firebase.firestore.FieldValue.delete()
                }
                
                recadosCollection.document(recadoId)
                    .update(updateData)
                    .await()
                
                Timber.d("✅ Recado ${if (fixado) "fixado" else "desfixado"}: $recadoId")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao ${if (fixado) "fixar" else "desfixar"} recado")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Adiciona ou remove apoio familiar (curtida) de um recado
     * @param recadoId ID do recado
     * @param userId ID do usuário que está curtindo/descurtindo
     * @param curtir true para curtir, false para descurtir
     */
    suspend fun curtirRecado(recadoId: String, userId: String, curtir: Boolean): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                val recadoRef = recadosCollection.document(recadoId)
                
                if (curtir) {
                    // Adicionar userId à lista de apoiosFamiliares usando arrayUnion
                    recadoRef.update(
                        mapOf(
                            "apoiosFamiliares" to com.google.firebase.firestore.FieldValue.arrayUnion(userId),
                            "atualizadoEm" to com.google.firebase.Timestamp.now()
                        )
                    ).await()
                    Timber.d("✅ Apoio familiar adicionado ao recado $recadoId pelo usuário $userId")
                } else {
                    // Remover userId da lista de apoiosFamiliares usando arrayRemove
                    recadoRef.update(
                        mapOf(
                            "apoiosFamiliares" to com.google.firebase.firestore.FieldValue.arrayRemove(userId),
                            "atualizadoEm" to com.google.firebase.Timestamp.now()
                        )
                    ).await()
                    Timber.d("✅ Apoio familiar removido do recado $recadoId pelo usuário $userId")
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao ${if (curtir) "curtir" else "descurtir"} recado")
                Result.failure(e)
            }
        }
    }
    
    // ============================================
    // CONQUISTAS
    // ============================================
    
    /**
     * Salva progresso de conquista do usuário no Firestore
     */
    suspend fun salvarConquista(
        usuarioId: String,
        conquistaId: String,
        desbloqueada: Boolean,
        desbloqueadaEm: Long?,
        progressoAtual: Int,
        progressoTotal: Int
    ): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                if (usuarioId.isBlank() || conquistaId.isBlank()) {
                    Timber.e("❌ Erro: usuarioId ou conquistaId está vazio")
                    return@withNetworkRetry Result.failure(
                        IllegalArgumentException("usuarioId e conquistaId não podem estar vazios")
                    )
                }
                
                val data = hashMapOf(
                    "conquistaId" to conquistaId,
                    "usuarioId" to usuarioId,
                    "desbloqueada" to desbloqueada,
                    "desbloqueadaEm" to (desbloqueadaEm?.let { 
                        com.google.firebase.Timestamp(Date(it))
                    }),
                    "progressoAtual" to progressoAtual,
                    "progressoTotal" to progressoTotal,
                    "atualizadoEm" to com.google.firebase.Timestamp.now()
                )
                
                conquistasCollection(usuarioId)
                    .document(conquistaId)
                    .set(data)
                    .await()
                
                Timber.d("✅ Conquista salva no Firestore: $conquistaId para usuário $usuarioId")
                Result.success(Unit)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao salvar conquista no Firestore")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Salva múltiplas conquistas do usuário no Firestore
     */
    suspend fun salvarTodasConquistas(
        usuarioId: String,
        conquistas: List<ProgressoConquista>
    ): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                if (usuarioId.isBlank()) {
                    Timber.e("❌ Erro: usuarioId está vazio")
                    return@withNetworkRetry Result.failure(
                        IllegalArgumentException("usuarioId não pode estar vazio")
                    )
                }
                
                val batch = firestore.batch()
                val collection = conquistasCollection(usuarioId)
                
                conquistas.forEach { progresso ->
                    val data = hashMapOf(
                        "conquistaId" to progresso.conquistaId,
                        "usuarioId" to usuarioId,
                        "desbloqueada" to progresso.desbloqueada,
                        "desbloqueadaEm" to (progresso.desbloqueadaEm?.let { 
                            com.google.firebase.Timestamp(it)
                        }),
                        "progressoAtual" to progresso.progressoAtual,
                        "progressoTotal" to progresso.progressoTotal,
                        "atualizadoEm" to com.google.firebase.Timestamp.now()
                    )
                    
                    val docRef = collection.document(progresso.conquistaId)
                    batch.set(docRef, data)
                }
                
                batch.commit().await()
                Timber.d("✅ ${conquistas.size} conquistas salvas no Firestore para usuário $usuarioId")
                Result.success(Unit)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao salvar conquistas no Firestore")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Busca todas as conquistas do usuário no Firestore
     */
    suspend fun buscarConquistasDoUsuario(usuarioId: String): Result<List<ProgressoConquista>> {
        return try {
            // VALIDAÇÃO CRÍTICA: usuarioId não pode estar vazio
            if (usuarioId.isBlank()) {
                Timber.e("❌ ERRO CRÍTICO: usuarioId está vazio ao buscar conquistas do Firestore!")
                return Result.failure(IllegalArgumentException("usuarioId não pode estar vazio"))
            }
            
            Timber.d("🔍 Buscando conquistas do Firestore para usuarioId: $usuarioId")
            
            // Usar subcollection específica do usuário: users/{userId}/conquistas/{conquistaId}
            // Isso garante que apenas conquistas deste usuarioId sejam retornadas
            val snapshot = conquistasCollection(usuarioId).get().await()
            
            val conquistas = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    
                    // VALIDAÇÃO: Verificar se o documento tem usuarioId e se corresponde
                    val docUsuarioId = data["usuarioId"] as? String
                    if (docUsuarioId != null && docUsuarioId != usuarioId) {
                        Timber.e("❌ ERRO CRÍTICO: Documento de conquista tem usuarioId incorreto! Doc: ${doc.id}, Esperado: $usuarioId, Encontrado: $docUsuarioId")
                        // Não incluir este documento
                        return@mapNotNull null
                    }
                    
                    ProgressoConquista(
                        conquistaId = data["conquistaId"] as? String ?: doc.id,
                        desbloqueada = data["desbloqueada"] as? Boolean ?: false,
                        desbloqueadaEm = (data["desbloqueadaEm"] as? com.google.firebase.Timestamp)?.toDate(),
                        progressoAtual = (data["progressoAtual"] as? Long)?.toInt() ?: 0,
                        progressoTotal = (data["progressoTotal"] as? Long)?.toInt() ?: 0
                    )
                } catch (e: Exception) {
                    Timber.e(e, "❌ Erro ao converter conquista: ${doc.id} para usuarioId: $usuarioId")
                    null
                }
            }
            
            Timber.d("✅ ${conquistas.size} conquistas carregadas do Firestore para usuarioId: $usuarioId")
            Result.success(conquistas)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar conquistas do Firestore para usuarioId: $usuarioId")
            Result.failure(e)
        }
    }
    
    /**
     * Observa conquistas do usuário em tempo real
     */
    fun observarConquistasDoUsuario(usuarioId: String): Flow<List<ProgressoConquista>> {
        return callbackFlow {
            if (usuarioId.isBlank()) {
                Timber.e("❌ Erro: usuarioId está vazio")
                close()
                return@callbackFlow
            }
            
            val listenerRegistration = conquistasCollection(usuarioId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Timber.e(error, "❌ Erro ao observar conquistas")
                        close(error)
                        return@addSnapshotListener
                    }
                    
                    if (snapshot == null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    
                    val conquistas = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            ProgressoConquista(
                                conquistaId = data["conquistaId"] as? String ?: doc.id,
                                desbloqueada = data["desbloqueada"] as? Boolean ?: false,
                                desbloqueadaEm = (data["desbloqueadaEm"] as? com.google.firebase.Timestamp)?.toDate(),
                                progressoAtual = (data["progressoAtual"] as? Long)?.toInt() ?: 0,
                                progressoTotal = (data["progressoTotal"] as? Long)?.toInt() ?: 0
                            )
                        } catch (e: Exception) {
                            Timber.e(e, "❌ Erro ao converter conquista: ${doc.id}")
                            null
                        }
                    }
                    
                    trySend(conquistas)
                }
            
            awaitClose {
                listenerRegistration.remove()
            }
        }
    }
    
    // ============================================
    // CHAT - MENSAGENS INSTANTÂNEAS
    // ============================================
    
    // Collection de mensagens do chat
    private val mensagensChatCollection = firestore.collection("mensagens_chat")
    
    /**
     * Salva uma mensagem de chat no Firestore
     */
    suspend fun salvarMensagemChat(mensagem: com.raizesvivas.app.domain.model.MensagemChat): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                if (mensagem.remetenteId.isBlank() || mensagem.destinatarioId.isBlank()) {
                    return@withNetworkRetry Result.failure(
                        IllegalArgumentException("remetenteId e destinatarioId não podem estar vazios")
                    )
                }
                
                val data = hashMapOf(
                    "remetenteId" to mensagem.remetenteId,
                    "remetenteNome" to mensagem.remetenteNome,
                    "destinatarioId" to mensagem.destinatarioId,
                    "destinatarioNome" to mensagem.destinatarioNome,
                    "texto" to mensagem.texto.trim(),
                    "enviadoEm" to com.google.firebase.Timestamp(mensagem.enviadoEm),
                    "lida" to mensagem.lida
                )
                
                val docRef = if (mensagem.id.isBlank()) {
                    mensagensChatCollection.document()
                } else {
                    mensagensChatCollection.document(mensagem.id)
                }
                
                docRef.set(data).await()
                
                Timber.d("💬 Mensagem de chat salva no Firestore: ${docRef.id}")
                Result.success(Unit)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao salvar mensagem de chat no Firestore")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Observa mensagens de uma conversa em tempo real
     * Retorna mensagens onde o usuário é remetente OU destinatário
     * Usa dois listeners separados e combina os resultados
     */
    fun observarMensagensChat(
        remetenteId: String,
        destinatarioId: String
    ): Flow<List<com.raizesvivas.app.domain.model.MensagemChat>> = callbackFlow {
        try {
            var mensagens1 = emptyList<com.raizesvivas.app.domain.model.MensagemChat>()
            var mensagens2 = emptyList<com.raizesvivas.app.domain.model.MensagemChat>()
            
            fun combinarEEnviar() {
                val todasMensagens = (mensagens1 + mensagens2)
                    .distinctBy { it.id }
                    .sortedBy { it.enviadoEm }
                
                Timber.d("📨 Mensagens observadas: ${todasMensagens.size} mensagens")
                trySend(todasMensagens)
            }
            
            // Listener 1: remetenteId -> destinatarioId
            // Limite de 100 para economizar leituras e cumprir regras de segurança
            val listener1 = mensagensChatCollection
                .whereEqualTo("remetenteId", remetenteId)
                .whereEqualTo("destinatarioId", destinatarioId)
                .orderBy("enviadoEm", Query.Direction.ASCENDING)
                .limit(100)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Timber.e(error, "❌ Erro ao observar mensagens (direção 1)")
                        mensagens1 = emptyList()
                        combinarEEnviar()
                        return@addSnapshotListener
                    }
                    
                    mensagens1 = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toMensagemChat()
                        } catch (e: Exception) {
                            Timber.e(e, "❌ Erro ao converter mensagem: ${doc.id}")
                            null
                        }
                    } ?: emptyList()
                    
                    combinarEEnviar()
                }
            
            // Listener 2: destinatarioId -> remetenteId (direção inversa)
            // Limite de 100 para economizar leituras e cumprir regras de segurança
            val listener2 = mensagensChatCollection
                .whereEqualTo("remetenteId", destinatarioId)
                .whereEqualTo("destinatarioId", remetenteId)
                .orderBy("enviadoEm", Query.Direction.ASCENDING)
                .limit(100)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Timber.e(error, "❌ Erro ao observar mensagens (direção 2)")
                        mensagens2 = emptyList()
                        combinarEEnviar()
                        return@addSnapshotListener
                    }
                    
                    mensagens2 = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toMensagemChat()
                        } catch (e: Exception) {
                            Timber.e(e, "❌ Erro ao converter mensagem: ${doc.id}")
                            null
                        }
                    } ?: emptyList()
                    
                    combinarEEnviar()
                }
            
            awaitClose {
                listener1.remove()
                listener2.remove()
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao configurar observação de mensagens")
            close(e)
        }
    }
    
    /**
     * Marca mensagens como lidas
     */
    suspend fun marcarMensagensComoLidas(
        remetenteId: String,
        destinatarioId: String
    ): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                // Buscar todas as mensagens não lidas onde o usuário atual é o destinatário
                val snapshot = mensagensChatCollection
                    .whereEqualTo("remetenteId", remetenteId)
                    .whereEqualTo("destinatarioId", destinatarioId)
                    .whereEqualTo("lida", false)
                    .get()
                    .await()
                
                if (snapshot.documents.isNotEmpty()) {
                    val batch = firestore.batch()
                    snapshot.documents.forEach { doc ->
                        batch.update(doc.reference, "lida", true)
                    }
                    batch.commit().await()
                    Timber.d("✅ ${snapshot.documents.size} mensagens marcadas como lidas")
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao marcar mensagens como lidas")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Deleta todas as mensagens de uma conversa
     */
    suspend fun deletarMensagensConversa(
        remetenteId: String,
        destinatarioId: String
    ): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                // Buscar todas as mensagens da conversa (ambas as direções)
                val snapshot1 = mensagensChatCollection
                    .whereEqualTo("remetenteId", remetenteId)
                    .whereEqualTo("destinatarioId", destinatarioId)
                    .get()
                    .await()
                
                val snapshot2 = mensagensChatCollection
                    .whereEqualTo("remetenteId", destinatarioId)
                    .whereEqualTo("destinatarioId", remetenteId)
                    .get()
                    .await()
                
                val batch = firestore.batch()
                snapshot1.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                snapshot2.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                
                if (snapshot1.documents.isNotEmpty() || snapshot2.documents.isNotEmpty()) {
                    batch.commit().await()
                    Timber.d("✅ Mensagens da conversa deletadas")
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao deletar mensagens da conversa")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Helper para converter DocumentSnapshot para MensagemChat
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toMensagemChat(): com.raizesvivas.app.domain.model.MensagemChat? {
        val data = this.data ?: return null
        
        return com.raizesvivas.app.domain.model.MensagemChat(
            id = id,
            remetenteId = data["remetenteId"] as? String ?: "",
            remetenteNome = data["remetenteNome"] as? String ?: "",
            destinatarioId = data["destinatarioId"] as? String ?: "",
            destinatarioNome = data["destinatarioNome"] as? String ?: "",
            texto = data["texto"] as? String ?: "",
            enviadoEm = (data["enviadoEm"] as? com.google.firebase.Timestamp)?.toDate() ?: java.util.Date(),
            lida = data["lida"] as? Boolean ?: false
        )
    }
}

