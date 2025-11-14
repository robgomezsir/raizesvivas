package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.local.ChatPreferences
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.MensagemChat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestoreService: FirestoreService,
    private val chatPreferences: ChatPreferences,
    private val authService: AuthService
) {

    companion object {
        private const val INITIAL_PAGE_SIZE = 50
        private const val PAGE_SIZE = 30
    }

    data class ChatConversationState(
        val mensagens: List<MensagemChat> = emptyList(),
        val isLoadingInicial: Boolean = true,
        val isCarregandoMais: Boolean = false,
        val possuiMaisAntigas: Boolean = true
    )

    private data class ConversationEntry(
        val state: MutableStateFlow<ChatConversationState>,
        val mutex: Mutex = Mutex(),
        var job: kotlinx.coroutines.Job? = null,
        val memoria: MutableList<MensagemChat> = mutableListOf(),
        var initialized: Boolean = false,
        var hasMoreOlder: Boolean = true,
        var oldestTimestamp: Date? = null
    )

    private val conversas = ConcurrentHashMap<String, ConversationEntry>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun observarConversa(
        remetenteId: String,
        destinatarioId: String
    ): StateFlow<ChatConversationState> {
        val conversaId = gerarConversaId(remetenteId, destinatarioId)
        val entry = conversas.computeIfAbsent(conversaId) {
            ConversationEntry(MutableStateFlow(ChatConversationState()))
        }

        scope.launch {
            try {
                inicializarConversa(entry, conversaId, remetenteId, destinatarioId)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (erro: Exception) {
                Timber.e(erro, "❌ Erro ao inicializar conversa $conversaId")
                entry.state.updateState(
                    mensagens = entry.state.value.mensagens,
                    isLoadingInicial = false,
                    isCarregandoMais = false,
                    possuiMaisAntigas = entry.state.value.possuiMaisAntigas
                )
            }
        }

        return entry.state.asStateFlow()
    }

    suspend fun carregarMensagensAntigas(
        remetenteId: String,
        destinatarioId: String
    ): Result<Unit> {
        val conversaId = gerarConversaId(remetenteId, destinatarioId)
        val entry = conversas[conversaId] ?: return Result.success(Unit)

        var referencia: Date?
        entry.mutex.withLock {
            if (entry.state.value.isCarregandoMais || !entry.hasMoreOlder) {
                return Result.success(Unit)
            }
            entry.state.updateState(
                mensagens = entry.state.value.mensagens,
                isLoadingInicial = entry.state.value.isLoadingInicial,
                isCarregandoMais = true,
                possuiMaisAntigas = entry.state.value.possuiMaisAntigas
            )
            referencia = entry.oldestTimestamp
        }

        val resultado = firestoreService.buscarMensagensAntigas(conversaId, PAGE_SIZE, referencia)

        resultado.onSuccess { antigas ->
            val vinteQuatroHorasAtras = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
            
            // Filtrar mensagens expiradas das mensagens antigas recebidas
            val antigasValidas = antigas.filter { it.enviadoEm.time >= vinteQuatroHorasAtras }
            
            var novasParaPersistir: List<MensagemChat> = emptyList()
            entry.mutex.withLock {
                if (antigasValidas.isEmpty()) {
                    entry.hasMoreOlder = false
                    entry.state.updateState(
                        mensagens = entry.memoria.toList(),
                        isLoadingInicial = false,
                        isCarregandoMais = false,
                        possuiMaisAntigas = false
                    )
                    return@withLock
                }

                val existentes = entry.memoria.mapTo(mutableSetOf()) { it.id }
                val mapa = LinkedHashMap<String, MensagemChat>()
                antigasValidas.forEach { mapa[it.id] = it }
                entry.memoria.forEach { mapa[it.id] = it }

                val ordenadas = mapa.values.sortedBy { it.enviadoEm }
                entry.memoria.clear()
                entry.memoria.addAll(ordenadas)
                entry.oldestTimestamp = ordenadas.firstOrNull()?.enviadoEm
                entry.hasMoreOlder = antigasValidas.size >= PAGE_SIZE
                entry.state.updateState(
                    mensagens = ordenadas,
                    isLoadingInicial = false,
                    isCarregandoMais = false,
                    possuiMaisAntigas = entry.hasMoreOlder
                )

                novasParaPersistir = antigasValidas.filter { it.id !in existentes }
            }

            if (novasParaPersistir.isNotEmpty()) {
                scope.launch {
                    novasParaPersistir.forEach { mensagem ->
                        try {
                            chatPreferences.salvarMensagem(mensagem)
                        } catch (e: Exception) {
                            Timber.w(e, "⚠️ Erro ao salvar mensagem antiga no cache local (conversaId=$conversaId)")
                        }
                    }
                }
            }
        }.onFailure { erro ->
            Timber.e(erro, "❌ Erro ao carregar mensagens antigas (conversaId=$conversaId)")
            entry.mutex.withLock {
                entry.state.updateState(
                    mensagens = entry.state.value.mensagens,
                    isLoadingInicial = entry.state.value.isLoadingInicial,
                    isCarregandoMais = false,
                    possuiMaisAntigas = entry.state.value.possuiMaisAntigas
                )
            }
        }

        return resultado.map { }
    }

    suspend fun reiniciarConversa(
        remetenteId: String,
        destinatarioId: String
    ) {
        val conversaId = gerarConversaId(remetenteId, destinatarioId)
        val entry = conversas.computeIfAbsent(conversaId) {
            ConversationEntry(MutableStateFlow(ChatConversationState()))
        }

        entry.mutex.withLock {
            entry.job?.cancel()
            entry.job = null
            entry.memoria.clear()
            entry.initialized = false
            entry.hasMoreOlder = true
            entry.oldestTimestamp = null
            entry.state.value = ChatConversationState()
        }

        scope.launch {
            inicializarConversa(entry, conversaId, remetenteId, destinatarioId)
        }
    }

    fun pararObservacao(
        remetenteId: String,
        destinatarioId: String
    ) {
        val conversaId = gerarConversaId(remetenteId, destinatarioId)
        conversas.remove(conversaId)?.let { entry ->
            entry.job?.cancel()
        }
    }

    fun observarMensagensNaoLidas(): Flow<Map<String, Int>> {
        val currentUser = authService.currentUser ?: return flowOf(emptyMap())

        return firestoreService.observarMensagensNaoLidas(currentUser.uid)
            .map { mensagens ->
                mensagens.groupBy { it.remetenteId }
                    .mapValues { (_, lista) -> lista.size }
            }
            .distinctUntilChanged()
            .onEach { counts ->
                Timber.d("🔔 Mensagens não lidas por contato: $counts")
            }
            .catch { error ->
                Timber.e(error, "❌ Erro ao observar mensagens não lidas")
                emit(emptyMap())
            }
    }

    suspend fun enviarMensagem(mensagem: MensagemChat): Result<Unit> {
        return try {
            try {
                chatPreferences.salvarMensagem(mensagem)
                Timber.d("✅ Mensagem salva no cache local: ${mensagem.id}")
            } catch (e: Exception) {
                Timber.e(e, "⚠️ Erro ao salvar mensagem no cache local (não crítico)")
            }

            val resultado = firestoreService.salvarMensagemChat(mensagem)
            resultado.onFailure { erro ->
                Timber.e(erro, "❌ Erro ao salvar mensagem no Firestore")
            }
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao enviar mensagem")
            Result.failure(e)
        }
    }

    suspend fun marcarMensagensComoLidas(
        remetenteId: String,
        destinatarioId: String
    ): Result<Unit> {
        return try {
            val resultado = firestoreService.marcarMensagensComoLidas(remetenteId, destinatarioId)
            try {
                chatPreferences.marcarMensagensComoLidas(remetenteId, destinatarioId)
            } catch (e: Exception) {
                Timber.e(e, "⚠️ Erro ao marcar mensagens como lidas no cache local")
            }
            atualizarMensagensComoLidasLocal(remetenteId, destinatarioId)
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao marcar mensagens como lidas")
            Result.failure(e)
        }
    }

    suspend fun limparMensagensConversa(
        remetenteId: String,
        destinatarioId: String
    ): Result<Unit> {
        return try {
            try {
                chatPreferences.limparMensagensConversa(remetenteId, destinatarioId)
            } catch (e: Exception) {
                Timber.e(e, "⚠️ Erro ao limpar mensagens do cache local")
            }

            val resultado = firestoreService.deletarMensagensConversa(remetenteId, destinatarioId)
            resultado.onSuccess {
                removerMensagensEnviadasLocal(remetenteId, destinatarioId)
            }
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao limpar mensagens da conversa")
            Result.failure(e)
        }
    }

    suspend fun limparMensagens(): Result<Unit> {
        return try {
            chatPreferences.limparMensagens()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao limpar mensagens locais")
            Result.failure(e)
        }
    }

    /**
     * Deleta uma mensagem específica (permite deletar mensagens recebidas)
     * Remove do cache local e memória imediatamente para melhor UX,
     * mesmo se a deleção no Firestore falhar
     *
     * @param mensagemId ID da mensagem a ser deletada
     * @return Result indicando sucesso ou falha
     */
    suspend fun deletarMensagem(mensagemId: String): Result<Unit> {
        return try {
            // Remover imediatamente do cache local e memória para melhor UX
            // Mesmo se o Firestore falhar, a mensagem desaparece da interface
            try {
                chatPreferences.deletarMensagem(mensagemId)
                Timber.d("✅ Mensagem $mensagemId deletada do cache local")
            } catch (e: Exception) {
                Timber.e(e, "⚠️ Erro ao deletar mensagem do cache local (não crítico)")
            }
            
            // Remover da memória da conversa
            removerMensagemLocal(mensagemId)
            
            // Tentar deletar do Firestore (em background, não bloqueia a UI)
            val resultadoFirestore = firestoreService.deletarMensagem(mensagemId)
            
            resultadoFirestore.onSuccess {
                Timber.d("✅ Mensagem $mensagemId deletada do Firestore")
            }.onFailure { error ->
                Timber.w(error, "⚠️ Erro ao deletar mensagem do Firestore (já removida localmente)")
                // Não retornamos erro aqui porque já removemos localmente
                // A mensagem pode ser re-sincronizada se necessário
            }
            
            // Sempre retorna sucesso se a remoção local funcionou
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao deletar mensagem")
            Result.failure(e)
        }
    }
    
    /**
     * Remove uma mensagem da memória local da conversa
     */
    private fun removerMensagemLocal(mensagemId: String) {
        conversas.values.forEach { entry ->
            scope.launch {
                entry.mutex.withLock {
                    val filtradas = entry.memoria.filterNot { it.id == mensagemId }
                    if (filtradas.size != entry.memoria.size) {
                        entry.memoria.clear()
                        entry.memoria.addAll(filtradas)
                        entry.oldestTimestamp = entry.memoria.firstOrNull()?.enviadoEm
                        entry.state.updateState(
                            mensagens = entry.memoria.toList(),
                            isLoadingInicial = entry.state.value.isLoadingInicial,
                            isCarregandoMais = entry.state.value.isCarregandoMais,
                            possuiMaisAntigas = entry.state.value.possuiMaisAntigas
                        )
                        Timber.d("🗑️ Mensagem $mensagemId removida da memória da conversa")
                    }
                }
            }
        }
    }

    private suspend fun inicializarConversa(
        entry: ConversationEntry,
        conversaId: String,
        remetenteId: String,
        destinatarioId: String
    ) {
        entry.mutex.withLock {
            if (entry.initialized) return
            entry.initialized = true
            entry.state.value = ChatConversationState()
        }

        val vinteQuatroHorasAtras = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
        
        val mensagensLocais = try {
            chatPreferences.buscarMensagens(remetenteId, destinatarioId)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar mensagens locais (conversaId=$conversaId)")
            emptyList()
        }.sortedBy { it.enviadoEm }

        // Filtrar mensagens expiradas do cache local
        val mensagensValidas = mensagensLocais.filter { it.enviadoEm.time >= vinteQuatroHorasAtras }
        val mensagensExpiradas = mensagensLocais.filter { it.enviadoEm.time < vinteQuatroHorasAtras }
        
        // Remover mensagens expiradas do cache local
        if (mensagensExpiradas.isNotEmpty()) {
            scope.launch {
                mensagensExpiradas.forEach { mensagem ->
                    try {
                        chatPreferences.deletarMensagem(mensagem.id)
                        Timber.d("🗑️ Mensagem expirada removida do cache local na inicialização: ${mensagem.id}")
                    } catch (e: Exception) {
                        Timber.w(e, "⚠️ Erro ao remover mensagem expirada do cache local (conversaId=$conversaId)")
                    }
                }
            }
        }

        entry.mutex.withLock {
            if (mensagensValidas.isNotEmpty()) {
                entry.memoria.clear()
                entry.memoria.addAll(mensagensValidas)
                entry.oldestTimestamp = mensagensValidas.first().enviadoEm
                entry.hasMoreOlder = mensagensValidas.size >= INITIAL_PAGE_SIZE
                entry.state.updateState(
                    mensagens = entry.memoria.toList(),
                    isLoadingInicial = true,
                    isCarregandoMais = false,
                    possuiMaisAntigas = entry.hasMoreOlder
                )
            } else {
                entry.oldestTimestamp = null
                entry.hasMoreOlder = true
            }
        }

        entry.job?.cancel()
        entry.job = scope.launch {
            firestoreService.observarMensagensChat(remetenteId, destinatarioId, INITIAL_PAGE_SIZE)
                .catch { erro ->
                    Timber.e(erro, "❌ Erro ao observar mensagens (conversaId=$conversaId)")
                    entry.mutex.withLock {
                        entry.state.updateState(
                            mensagens = entry.state.value.mensagens,
                            isLoadingInicial = false,
                            isCarregandoMais = false,
                            possuiMaisAntigas = entry.state.value.possuiMaisAntigas
                        )
                    }
                }
                .collect { mensagens ->
                    atualizarComMensagensRecentes(entry, conversaId, mensagens)
                }
        }
    }

    private suspend fun atualizarComMensagensRecentes(
        entry: ConversationEntry,
        conversaId: String,
        mensagens: List<MensagemChat>
    ) {
        val vinteQuatroHorasAtras = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
        
        var novasParaPersistir: List<MensagemChat> = emptyList()
        var mensagensExpiradasParaRemover: List<MensagemChat> = emptyList()

        entry.mutex.withLock {
            if (mensagens.isEmpty() && entry.memoria.isEmpty()) {
                entry.hasMoreOlder = false
                entry.state.value = ChatConversationState(
                    mensagens = emptyList(),
                    isLoadingInicial = false,
                    isCarregandoMais = false,
                    possuiMaisAntigas = false
                )
                return
            }

            // Filtrar mensagens expiradas das novas mensagens recebidas
            val mensagensValidas = mensagens.filter { it.enviadoEm.time >= vinteQuatroHorasAtras }
            val mensagensExpiradasNovas = mensagens.filter { it.enviadoEm.time < vinteQuatroHorasAtras }

            val mapa = LinkedHashMap<String, MensagemChat>()
            entry.memoria.forEach { mapa[it.id] = it }

            // Remover mensagens expiradas da memória
            val mensagensExpiradasMemoria = entry.memoria.filter { it.enviadoEm.time < vinteQuatroHorasAtras }
            mensagensExpiradasMemoria.forEach { mapa.remove(it.id) }

            val novos = mutableListOf<MensagemChat>()
            mensagensValidas.forEach { mensagem ->
                val anterior = mapa.put(mensagem.id, mensagem)
                if (anterior == null || anterior != mensagem) {
                    novos.add(mensagem)
                }
            }

            val ordenadas = mapa.values.sortedBy { it.enviadoEm }
            entry.memoria.clear()
            entry.memoria.addAll(ordenadas)
            entry.oldestTimestamp = ordenadas.firstOrNull()?.enviadoEm
            entry.hasMoreOlder = if (mensagensValidas.size >= INITIAL_PAGE_SIZE) {
                true
            } else {
                entry.hasMoreOlder && entry.memoria.size > mensagensValidas.size
            }
            entry.state.updateState(
                mensagens = ordenadas,
                isLoadingInicial = false,
                isCarregandoMais = false,
                possuiMaisAntigas = entry.hasMoreOlder
            )
            novasParaPersistir = novos
            mensagensExpiradasParaRemover = mensagensExpiradasNovas + mensagensExpiradasMemoria
        }

        // Remover mensagens expiradas do cache local
        if (mensagensExpiradasParaRemover.isNotEmpty()) {
            scope.launch {
                mensagensExpiradasParaRemover.forEach { mensagem ->
                    try {
                        chatPreferences.deletarMensagem(mensagem.id)
                        Timber.d("🗑️ Mensagem expirada removida do cache local: ${mensagem.id}")
                    } catch (e: Exception) {
                        Timber.w(e, "⚠️ Erro ao remover mensagem expirada do cache local (conversaId=$conversaId)")
                    }
                }
            }
        }

        if (novasParaPersistir.isNotEmpty()) {
            scope.launch {
                novasParaPersistir.forEach { mensagem ->
                    try {
                        chatPreferences.salvarMensagem(mensagem)
                    } catch (e: Exception) {
                        Timber.w(e, "⚠️ Erro ao salvar mensagem no cache local (conversaId=$conversaId)")
                    }
                }
            }
        }
    }

    private fun atualizarMensagensComoLidasLocal(
        remetenteId: String,
        destinatarioId: String
    ) {
        val conversaId = gerarConversaId(remetenteId, destinatarioId)
        conversas[conversaId]?.let { entry ->
            scope.launch {
                entry.mutex.withLock {
                    var houveAlteracao = false
                    val atualizadas = entry.memoria.map { mensagem ->
                        if (mensagem.remetenteId == remetenteId &&
                            mensagem.destinatarioId == destinatarioId &&
                            !mensagem.lida
                        ) {
                            houveAlteracao = true
                            mensagem.copy(lida = true)
                        } else {
                            mensagem
                        }
                    }
                    if (houveAlteracao) {
                        entry.memoria.clear()
                        entry.memoria.addAll(atualizadas)
                        entry.state.updateState(
                            mensagens = entry.memoria.toList(),
                            isLoadingInicial = entry.state.value.isLoadingInicial,
                            isCarregandoMais = entry.state.value.isCarregandoMais,
                            possuiMaisAntigas = entry.state.value.possuiMaisAntigas
                        )
                    }
                }
            }
        }
    }

    private fun removerMensagensEnviadasLocal(
        remetenteId: String,
        destinatarioId: String
    ) {
        val conversaId = gerarConversaId(remetenteId, destinatarioId)
        conversas[conversaId]?.let { entry ->
            scope.launch {
                entry.mutex.withLock {
                    val filtradas = entry.memoria.filterNot {
                        it.remetenteId == remetenteId && it.destinatarioId == destinatarioId
                    }
                    if (filtradas.size != entry.memoria.size) {
                        entry.memoria.clear()
                        entry.memoria.addAll(filtradas)
                        entry.oldestTimestamp = entry.memoria.firstOrNull()?.enviadoEm
                        entry.hasMoreOlder = true
                        entry.state.updateState(
                            mensagens = entry.memoria.toList(),
                            isLoadingInicial = entry.state.value.isLoadingInicial,
                            isCarregandoMais = entry.state.value.isCarregandoMais,
                            possuiMaisAntigas = entry.hasMoreOlder
                        )
                    }
                }
            }
        }
    }

    private fun gerarConversaId(id1: String, id2: String): String {
        return if (id1 <= id2) {
            "${id1}_${id2}"
        } else {
            "${id2}_${id1}"
        }
    }

    private fun MutableStateFlow<ChatConversationState>.updateState(
        mensagens: List<MensagemChat>,
        isLoadingInicial: Boolean,
        isCarregandoMais: Boolean,
        possuiMaisAntigas: Boolean
    ) {
        value = ChatConversationState(
            mensagens = mensagens,
            isLoadingInicial = isLoadingInicial,
            isCarregandoMais = isCarregandoMais,
            possuiMaisAntigas = possuiMaisAntigas
        )
    }
}
