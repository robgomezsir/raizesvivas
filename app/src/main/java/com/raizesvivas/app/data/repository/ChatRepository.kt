package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.local.ChatPreferences
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.MensagemChat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gerenciar mensagens de chat
 * 
 * Coordena sincronização entre Firestore (backend) e cache local (DataStore)
 * - Salva mensagens no Firestore para sincronização entre dispositivos
 * - Mantém cache local para funcionamento offline
 * - Observa mudanças em tempo real do Firestore
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ChatRepository @Inject constructor(
    private val firestoreService: FirestoreService,
    private val chatPreferences: ChatPreferences,
    private val authService: AuthService
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Observa mensagens de uma conversa em tempo real
     * Combina dados do Firestore (tempo real) com cache local (offline)
     */
    fun observarMensagens(
        remetenteId: String,
        destinatarioId: String
    ): Flow<List<MensagemChat>> {
        return combine(
            // Fluxo do Firestore (tempo real)
            firestoreService.observarMensagensChat(remetenteId, destinatarioId)
                .catch { error ->
                    Timber.e(error, "❌ Erro ao observar mensagens do Firestore")
                    emit(emptyList())
                },
            // Fluxo do cache local
            chatPreferences.observarMensagens(remetenteId, destinatarioId)
                .catch { error ->
                    Timber.e(error, "❌ Erro ao observar mensagens do cache local")
                    emit(emptyList())
                }
        ) { mensagensFirestore, mensagensLocal ->
            // Combinar e remover duplicatas, priorizando Firestore
            val todasMensagens = (mensagensFirestore + mensagensLocal)
                .distinctBy { it.id }
                .sortedBy { it.enviadoEm }
            
            // Sincronizar cache local com Firestore em background
            scope.launch {
                try {
                    mensagensFirestore.forEach { mensagem ->
                        chatPreferences.salvarMensagem(mensagem)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "❌ Erro ao sincronizar cache local")
                }
            }
            
            todasMensagens
        }
    }
    
    /**
     * Envia uma mensagem
     * Salva no Firestore (para sincronização) e no cache local (para offline)
     */
    suspend fun enviarMensagem(mensagem: MensagemChat): Result<Unit> {
        return try {
            // 1. Salvar no Firestore primeiro (para sincronização instantânea)
            val resultadoFirestore = firestoreService.salvarMensagemChat(mensagem)
            
            resultadoFirestore.onSuccess {
                Timber.d("✅ Mensagem salva no Firestore: ${mensagem.id}")
                
                // 2. Salvar no cache local também (para funcionamento offline)
                try {
                    chatPreferences.salvarMensagem(mensagem)
                    Timber.d("✅ Mensagem salva no cache local: ${mensagem.id}")
                } catch (e: Exception) {
                    Timber.e(e, "⚠️ Erro ao salvar mensagem no cache local (não crítico)")
                }
            }.onFailure { error ->
                Timber.e(error, "❌ Erro ao salvar mensagem no Firestore")
                
                // Se falhar no Firestore, salvar apenas localmente (modo offline)
                try {
                    chatPreferences.salvarMensagem(mensagem)
                    Timber.d("💾 Mensagem salva apenas localmente (modo offline): ${mensagem.id}")
                } catch (e: Exception) {
                    Timber.e(e, "❌ Erro ao salvar mensagem localmente")
                }
            }
            
            resultadoFirestore
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao enviar mensagem")
            Result.failure(e)
        }
    }
    
    /**
     * Marca mensagens como lidas
     */
    suspend fun marcarMensagensComoLidas(
        remetenteId: String,
        destinatarioId: String
    ): Result<Unit> {
        return try {
            // Marcar como lidas no Firestore
            val resultadoFirestore = firestoreService.marcarMensagensComoLidas(remetenteId, destinatarioId)
            
            // Marcar como lidas no cache local também
            try {
                chatPreferences.marcarMensagensComoLidas(remetenteId, destinatarioId)
            } catch (e: Exception) {
                Timber.e(e, "⚠️ Erro ao marcar mensagens como lidas no cache local")
            }
            
            resultadoFirestore
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao marcar mensagens como lidas")
            Result.failure(e)
        }
    }
    
    /**
     * Limpa todas as mensagens de uma conversa
     */
    suspend fun limparMensagensConversa(
        remetenteId: String,
        destinatarioId: String
    ): Result<Unit> {
        return try {
            // Deletar do Firestore
            val resultadoFirestore = firestoreService.deletarMensagensConversa(remetenteId, destinatarioId)
            
            // Deletar do cache local também
            try {
                chatPreferences.limparMensagensConversa(remetenteId, destinatarioId)
            } catch (e: Exception) {
                Timber.e(e, "⚠️ Erro ao limpar mensagens do cache local")
            }
            
            resultadoFirestore
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao limpar mensagens da conversa")
            Result.failure(e)
        }
    }
}

