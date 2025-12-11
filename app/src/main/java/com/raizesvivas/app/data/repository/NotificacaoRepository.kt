package com.raizesvivas.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.raizesvivas.app.data.local.dao.NotificacaoDao
import com.raizesvivas.app.data.local.entities.NotificacaoEntity
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.Notificacao
import com.raizesvivas.app.domain.model.TipoNotificacao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gerenciar notificações
 */
@Singleton
class NotificacaoRepository @Inject constructor(
    private val notificacaoDao: NotificacaoDao,
    private val usuarioRepository: UsuarioRepository,
    private val firestoreService: FirestoreService,
    private val authService: AuthService,
    private val fcm: com.google.firebase.messaging.FirebaseMessaging,
    private val firestore: FirebaseFirestore
) {

    /**
     * Registra analytics para clique no botão de download de atualização
     */
    suspend fun registrarCliqueDownloadAtualizacao(notificacaoId: String, versao: String?, downloadUrl: String?) {
        try {
            val usuarioAtual = authService.currentUser ?: return
            val extras = buildMap<String, Any> {
                versao?.let { put("versao", it) }
                downloadUrl?.let { put("downloadUrl", it) }
            }
            firestoreService.registrarEventoNotificacao(
                usuarioId = usuarioAtual.uid,
                notificacaoId = notificacaoId,
                evento = "download_atualizacao_click",
                extras = extras
            )
        } catch (_: Exception) {
            // Silencioso: analytics não deve quebrar fluxo
        }
    }
    
    // ============================================================
    // MÉTODOS FCM (Firebase Cloud Messaging)
    // ============================================================
    
    /**
     * Obtém o token FCM atual do dispositivo
     */
    suspend fun getFCMToken(): String? {
        return try {
            val token = fcm.token.await()
            Timber.d("🔑 Token FCM obtido: $token")
            token
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao obter token FCM")
            null
        }
    }

    /**
     * Atualiza o token FCM do usuário no Firestore
     * Cria o documento se não existir
     */
    suspend fun updateFCMToken(token: String) {
        val userId = authService.currentUser?.uid ?: run {
            Timber.w("⚠️ Usuário não autenticado, não é possível atualizar token")
            return
        }
        
        try {
            // Usar set com merge para criar documento se não existir
            firestore.collection("usuarios")
                .document(userId)
                .set(
                    mapOf(
                        "fcmToken" to token,
                        "fcmTokenUpdatedAt" to com.google.firebase.Timestamp.now()
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                )
                .await()
            
            Timber.d("✅ Token FCM atualizado no Firestore para usuário: $userId")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao atualizar token FCM no Firestore")
        }
    }

    /**
     * Remove o token FCM do usuário (útil no logout)
     */
    suspend fun removeFCMToken() {
        val userId = authService.currentUser?.uid ?: return
        
        try {
            firestore.collection("usuarios")
                .document(userId)
                .update("fcmToken", null)
                .await()
            
            // Deletar token do FCM
            fcm.deleteToken().await()
            
            Timber.d("✅ Token FCM removido")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao remover token FCM")
        }
    }

    
    /**
     * Observa todas as notificações
     */
    fun observarTodasNotificacoes(): Flow<List<Notificacao>> {
        return notificacaoDao.observarTodasNotificacoes()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }
    
    /**
     * Observa apenas notificações não lidas
     */
    fun observarNotificacoesNaoLidas(): Flow<List<Notificacao>> {
        return notificacaoDao.observarNotificacoesNaoLidas()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }
    
    /**
     * Conta notificações não lidas
     */
    fun contarNaoLidas(): Flow<Int> {
        return notificacaoDao.contarNaoLidas()
    }
    
    /**
     * Cria uma nova notificação
     */
    suspend fun criarNotificacao(notificacao: Notificacao) {
        try {
            val entity = NotificacaoEntity.fromDomain(notificacao)
            notificacaoDao.inserirOuAtualizar(entity)
            Timber.d("✅ Notificação criada: ${notificacao.titulo}")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao criar notificação")
        }
    }
    
    /**
     * Marca notificação como lida (local e Firestore)
     */
    suspend fun marcarComoLida(id: String) {
        try {
            // Marcar como lida no Room local
            notificacaoDao.marcarComoLida(id)
            
            // Marcar como lida no Firestore (se houver usuário logado)
            val usuarioAtual = authService.currentUser
            if (usuarioAtual != null) {
                firestoreService.marcarNotificacaoComoLida(usuarioAtual.uid, id)
                    .onFailure { error ->
                        Timber.w(error, "⚠️ Erro ao marcar notificação como lida no Firestore (continuando localmente)")
                    }
            }
            
            Timber.d("✅ Notificação marcada como lida: $id")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao marcar notificação como lida")
        }
    }
    
    /**
     * Marca todas as notificações como lidas
     */
    suspend fun marcarTodasComoLidas() {
        try {
            notificacaoDao.marcarTodasComoLidas()
            Timber.d("✅ Todas as notificações marcadas como lidas")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao marcar todas como lidas")
        }
    }
    
    /**
     * Busca notificação de aniversário de hoje não lida
     */
    suspend fun buscarAniversarioHojeNaoLido(): Notificacao? {
        return try {
            // Calcular início do dia de hoje e amanhã
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val inicioHoje = calendar.timeInMillis
            
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
            val inicioAmanha = calendar.timeInMillis
            
            val entity = notificacaoDao.buscarAniversarioHojeNaoLido(inicioHoje, inicioAmanha)
            entity?.toDomain()
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar aniversário de hoje")
            null
        }
    }
    
    /**
     * Cria uma notificação ADMIN_MENSAGEM para todos os usuários do sistema
     * Salva no Firestore para cada usuário, para que seja sincronizada em todos os dispositivos
     * 
     * @param titulo Título da notificação
     * @param mensagem Mensagem da notificação
     * @return Número de notificações criadas
     */
    suspend fun criarNotificacaoParaTodosUsuarios(titulo: String, mensagem: String): Result<Int> {
        return try {
            // Buscar todos os usuários
            val resultadoUsuarios = usuarioRepository.buscarTodosUsuarios()
            val usuarios = resultadoUsuarios.getOrNull() ?: emptyList()
            
            if (usuarios.isEmpty()) {
                Timber.w("⚠️ Nenhum usuário encontrado para enviar notificação")
                return Result.success(0)
            }
            
            // Criar notificação para cada usuário
            var contador = 0
            var erros = 0
            
            usuarios.forEach { usuario ->
                try {
                    val notificacao = Notificacao(
                        id = UUID.randomUUID().toString(),
                        tipo = TipoNotificacao.ADMIN_MENSAGEM,
                        titulo = titulo,
                        mensagem = mensagem,
                        lida = false,
                        criadaEm = Date(),
                        relacionadoId = null,
                        dadosExtras = emptyMap()
                    )
                    
                    // Salvar no Firestore para o usuário específico
                    val resultadoFirestore = firestoreService.salvarNotificacao(usuario.id, notificacao)
                    
                    resultadoFirestore.onSuccess {
                        Timber.d("✅ Notificação salva no Firestore para usuário ${usuario.id}")
                        contador++
                    }
                    
                    resultadoFirestore.onFailure { error ->
                        Timber.e(error, "❌ Erro ao salvar notificação no Firestore para usuário ${usuario.id}")
                        erros++
                    }
                } catch (e: Exception) {
                    Timber.e(e, "❌ Erro ao processar notificação para usuário ${usuario.id}")
                    erros++
                }
            }
            
            if (contador > 0) {
                Timber.d("✅ Notificações criadas no Firestore para $contador usuário(s)${if (erros > 0) " ($erros erro(s))" else ""}")
                Result.success(contador)
            } else {
                Timber.e("❌ Nenhuma notificação foi criada com sucesso")
                Result.failure(Exception("Nenhuma notificação foi criada. $erros erro(s) ocorreram."))
            }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao criar notificações para todos os usuários")
            Result.failure(e)
        }
    }
    
    /**
     * Busca primeira notificação ADMIN_MENSAGEM não lida
     */
    suspend fun buscarAdminMensagemNaoLida(): Notificacao? {
        return try {
            val entity = notificacaoDao.buscarAdminMensagemNaoLida()
            entity?.toDomain()
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar ADMIN_MENSAGEM não lida")
            null
        }
    }
    
    /**
     * Sincroniza notificações do Firestore para o banco local (Room)
     * Busca todas as notificações do usuário atual no Firestore e salva no Room
     */
    suspend fun sincronizarNotificacoesDoFirestore(): Result<Int> {
        return try {
            val usuarioAtual = authService.currentUser
            if (usuarioAtual == null) {
                Timber.w("⚠️ Nenhum usuário logado para sincronizar notificações")
                return Result.success(0)
            }
            
            // Buscar notificações do Firestore
            val resultado = firestoreService.buscarNotificacoes(usuarioAtual.uid)
            
            val notificacoes = resultado.getOrNull()
            if (notificacoes != null) {
                // Salvar todas as notificações no Room local
                var contador = 0
                notificacoes.forEach { notificacao ->
                    try {
                        val entity = NotificacaoEntity.fromDomain(notificacao)
                        notificacaoDao.inserirOuAtualizar(entity)
                        contador++
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Erro ao salvar notificação no Room: ${notificacao.id}")
                    }
                }
                
                Timber.d("✅ ${contador} notificação(ões) sincronizada(s) do Firestore")
                Result.success(contador)
            } else {
                // Se getOrNull() retornou null, significa que houve erro
                val error = resultado.exceptionOrNull() ?: Exception("Erro desconhecido ao buscar notificações")
                Timber.e(error, "❌ Erro ao buscar notificações do Firestore")
                Result.failure(error)
            }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao sincronizar notificações")
            Result.failure(e)
        }
    }

    /**
     * Cria uma notificação ADMIN_MENSAGEM para todos com dadosExtras de atualização (versão e link)
     */
    suspend fun criarNotificacaoAtualizacaoParaTodosUsuarios(
        titulo: String,
        mensagem: String,
        versao: String,
        downloadUrl: String
    ): Result<Int> {
        return try {
            val resultadoUsuarios = usuarioRepository.buscarTodosUsuarios()
            val usuarios = resultadoUsuarios.getOrNull() ?: emptyList()

            if (usuarios.isEmpty()) {
                Timber.w("⚠️ Nenhum usuário encontrado para enviar atualização")
                return Result.success(0)
            }

            var contador = 0
            var erros = 0

            usuarios.forEach { usuario ->
                try {
                    val notificacao = Notificacao(
                        id = UUID.randomUUID().toString(),
                        tipo = TipoNotificacao.ADMIN_MENSAGEM,
                        titulo = titulo,
                        mensagem = mensagem,
                        lida = false,
                        criadaEm = Date(),
                        relacionadoId = null,
                        dadosExtras = mapOf(
                            "versao" to versao,
                            "downloadUrl" to downloadUrl
                        )
                    )

                    val resultadoFirestore = firestoreService.salvarNotificacao(usuario.id, notificacao)
                    resultadoFirestore.onSuccess {
                        Timber.d("✅ Notificação de atualização salva para usuário ${usuario.id}")
                        contador++
                    }
                    resultadoFirestore.onFailure { error ->
                        Timber.e(error, "❌ Erro ao salvar notificação de atualização para usuário ${usuario.id}")
                        erros++
                    }
                } catch (e: Exception) {
                    Timber.e(e, "❌ Erro ao processar notificação de atualização para usuário ${usuario.id}")
                    erros++
                }
            }

            if (contador > 0) {
                Timber.d("✅ Atualização criada no Firestore para $contador usuário(s)${if (erros > 0) " ($erros erro(s))" else ""}")
                Result.success(contador)
            } else {
                Timber.e("❌ Nenhuma notificação de atualização criada com sucesso")
                Result.failure(Exception("Nenhuma notificação foi criada. $erros erro(s) ocorreram."))
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao criar notificações de atualização")
            Result.failure(e)
        }
    }
}
