package com.raizesvivas.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.raizesvivas.app.domain.model.MensagemChat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerenciador de preferências para armazenar mensagens do chat localmente
 * Usa DataStore para persistência segura
 */
@Singleton
class ChatPreferences @Inject constructor(
    private val context: Context
) {
    private val Context.chatDataStore: DataStore<Preferences> by preferencesDataStore(name = "chat_prefs")
    private val gson = Gson()

    companion object {
        private const val MENSAGENS_KEY_PREFIX = "mensagens_"
    }

    /**
     * Salva uma mensagem no chat entre dois usuários
     */
    suspend fun salvarMensagem(mensagem: MensagemChat) {
        try {
            val conversaId = gerarConversaId(mensagem.remetenteId, mensagem.destinatarioId)
            val key = stringPreferencesKey("${MENSAGENS_KEY_PREFIX}$conversaId")

            val mensagensExistentes = buscarMensagensPorConversaId(conversaId)
            val mensagensAtualizadas = mensagensExistentes + mensagem

            context.chatDataStore.edit { preferences ->
                val mensagensJson = gson.toJson(mensagensAtualizadas.map { it.toSerializable() })
                preferences[key] = mensagensJson
            }

            Timber.d("💬 Mensagem salva: ${mensagem.id} na conversa $conversaId")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao salvar mensagem")
        }
    }

    /**
     * Observa todas as mensagens de uma conversa
     */
    fun observarMensagens(remetenteId: String, destinatarioId: String): Flow<List<MensagemChat>> {
        val conversaId = gerarConversaId(remetenteId, destinatarioId)
        val key = stringPreferencesKey("${MENSAGENS_KEY_PREFIX}$conversaId")

        return context.chatDataStore.data.map { preferences ->
            val mensagensJson = preferences[key] ?: return@map emptyList<MensagemChat>()
            try {
                val type = object : TypeToken<List<MensagemChatSerializable>>() {}.type
                val mensagensSerializadas: List<MensagemChatSerializable> = gson.fromJson(mensagensJson, type)
                mensagensSerializadas.map { it.toDomain() }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao decodificar mensagens")
                emptyList<MensagemChat>()
            }
        }
    }

    /**
     * Busca todas as mensagens de uma conversa (suspend)
     */
    suspend fun buscarMensagens(remetenteId: String, destinatarioId: String): List<MensagemChat> {
        val conversaId = gerarConversaId(remetenteId, destinatarioId)
        return buscarMensagensPorConversaId(conversaId)
    }

    /**
     * Busca todas as mensagens de uma conversa por ID interno
     */
    private suspend fun buscarMensagensPorConversaId(conversaId: String): List<MensagemChat> {
        val key = stringPreferencesKey("${MENSAGENS_KEY_PREFIX}$conversaId")
        val preferences = context.chatDataStore.data.first()
        val mensagensJson = preferences[key] ?: return emptyList<MensagemChat>()

        return try {
            val type = object : TypeToken<List<MensagemChatSerializable>>() {}.type
            val mensagensSerializadas: List<MensagemChatSerializable> = gson.fromJson(mensagensJson, type)
            mensagensSerializadas.map { it.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao decodificar mensagens")
            emptyList<MensagemChat>()
        }
    }

    /**
     * Marca mensagens como lidas
     */
    suspend fun marcarMensagensComoLidas(remetenteId: String, destinatarioId: String) {
        try {
            val conversaId = gerarConversaId(remetenteId, destinatarioId)
            val mensagens = buscarMensagensPorConversaId(conversaId)

            val mensagensAtualizadas = mensagens.map { mensagem ->
                if (mensagem.remetenteId == remetenteId && !mensagem.lida) {
                    mensagem.copy(lida = true)
                } else {
                    mensagem
                }
            }

            val key = stringPreferencesKey("${MENSAGENS_KEY_PREFIX}$conversaId")
            context.chatDataStore.edit { preferences ->
                val mensagensJson = gson.toJson(mensagensAtualizadas.map { it.toSerializable() })
                preferences[key] = mensagensJson
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao marcar mensagens como lidas")
        }
    }

    /**
     * Limpa todas as mensagens (útil para logout)
     */
    suspend fun limparMensagens() {
        try {
            context.chatDataStore.edit { preferences ->
                preferences.clear()
            }
            Timber.d("🗑️ Mensagens do chat limpas")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao limpar mensagens")
        }
    }
    
    /**
     * Deleta uma mensagem específica da conversa
     */
    suspend fun deletarMensagem(mensagemId: String, remetenteId: String, destinatarioId: String) {
        try {
            val conversaId = gerarConversaId(remetenteId, destinatarioId)
            val mensagens = buscarMensagensPorConversaId(conversaId)
            
            val mensagensAtualizadas = mensagens.filter { it.id != mensagemId }
            
            val key = stringPreferencesKey("${MENSAGENS_KEY_PREFIX}$conversaId")
            context.chatDataStore.edit { preferences ->
                if (mensagensAtualizadas.isEmpty()) {
                    preferences.remove(key)
                } else {
                    val mensagensJson = gson.toJson(mensagensAtualizadas.map { it.toSerializable() })
                    preferences[key] = mensagensJson
                }
            }
            
            Timber.d("🗑️ Mensagem $mensagemId deletada da conversa $conversaId")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao deletar mensagem")
        }
    }
    
    /**
     * Deleta uma mensagem específica (sobrecarga que busca a conversa automaticamente)
     */
    suspend fun deletarMensagem(mensagemId: String) {
        // Buscar em todas as conversas para encontrar a mensagem
        try {
            val todasChaves = context.chatDataStore.data.first().asMap().keys
                .filter { it.name.startsWith(MENSAGENS_KEY_PREFIX) }
            
            for (chave in todasChaves) {
                val conversaId = chave.name.removePrefix(MENSAGENS_KEY_PREFIX)
                val mensagens = buscarMensagensPorConversaId(conversaId)
                
                if (mensagens.any { it.id == mensagemId }) {
                    // Encontrou a mensagem, deletar
                    val mensagensAtualizadas = mensagens.filter { it.id != mensagemId }
                    val key = stringPreferencesKey(chave.name)
                    
                    context.chatDataStore.edit { preferences ->
                        if (mensagensAtualizadas.isEmpty()) {
                            preferences.remove(key)
                        } else {
                            val mensagensJson = gson.toJson(mensagensAtualizadas.map { it.toSerializable() })
                            preferences[key] = mensagensJson
                        }
                    }
                    
                    Timber.d("🗑️ Mensagem $mensagemId deletada da conversa $conversaId")
                    return
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao deletar mensagem")
        }
    }
    
    /**
     * Limpa todas as mensagens ENVIADAS pelo usuário para um destinatário específico
     * IMPORTANTE: Remove apenas mensagens onde remetenteId == remetenteId passado
     * Não remove mensagens recebidas do destinatário
     */
    suspend fun limparMensagensConversa(remetenteId: String, destinatarioId: String) {
        try {
            val conversaId = gerarConversaId(remetenteId, destinatarioId)
            val key = stringPreferencesKey("${MENSAGENS_KEY_PREFIX}$conversaId")
            
            // Buscar todas as mensagens da conversa
            val mensagensAtuais = context.chatDataStore.data.first()[key]?.let { json ->
                try {
                    val type = object : TypeToken<List<MensagemChatSerializable>>() {}.type
                    gson.fromJson<List<MensagemChatSerializable>>(json, type) ?: emptyList()
                } catch (e: Exception) {
                    Timber.e(e, "❌ Erro ao parsear mensagens")
                    emptyList()
                }
            } ?: emptyList()
            
            // Filtrar apenas mensagens ENVIADAS pelo remetente (não remover mensagens recebidas)
            val mensagensParaManter = mensagensAtuais.filter { it.remetenteId != remetenteId }
            
            Timber.d("🗑️ Limpando mensagens: ${mensagensAtuais.size} total, ${mensagensAtuais.size - mensagensParaManter.size} enviadas serão removidas, ${mensagensParaManter.size} recebidas serão mantidas")
            
            // Salvar apenas as mensagens que devem ser mantidas
            context.chatDataStore.edit { preferences ->
                if (mensagensParaManter.isEmpty()) {
                    // Se não há mensagens para manter, remover a chave completamente
                    preferences.remove(key)
                } else {
                    // Salvar apenas as mensagens recebidas
                    val json = gson.toJson(mensagensParaManter)
                    preferences[key] = json
                }
            }
            
            Timber.d("✅ Mensagens ENVIADAS da conversa $conversaId limpas (${mensagensAtuais.size - mensagensParaManter.size} removidas)")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao limpar mensagens da conversa")
            throw e
        }
    }

    /**
     * Gera um ID único para a conversa entre dois usuários
     * Sempre ordena os IDs para garantir consistência
     */
    private fun gerarConversaId(id1: String, id2: String): String {
        return if (id1 < id2) {
            "${id1}_${id2}"
        } else {
            "${id2}_${id1}"
        }
    }
}

/**
 * Versão serializável da mensagem para armazenamento no DataStore
 */
private data class MensagemChatSerializable(
    val id: String,
    val remetenteId: String,
    val remetenteNome: String,
    val destinatarioId: String,
    val destinatarioNome: String,
    val texto: String,
    val enviadoEm: Long,
    val lida: Boolean
)

/**
 * Converte MensagemChat para formato serializável
 */
private fun MensagemChat.toSerializable(): MensagemChatSerializable {
    return MensagemChatSerializable(
        id = id,
        remetenteId = remetenteId,
        remetenteNome = remetenteNome,
        destinatarioId = destinatarioId,
        destinatarioNome = destinatarioNome,
        texto = texto,
        enviadoEm = enviadoEm.time,
        lida = lida
    )
}

/**
 * Converte MensagemChatSerializable para formato de domínio
 */
private fun MensagemChatSerializable.toDomain(): MensagemChat {
    return MensagemChat(
        id = id,
        remetenteId = remetenteId,
        remetenteNome = remetenteNome,
        destinatarioId = destinatarioId,
        destinatarioNome = destinatarioNome,
        texto = texto,
        enviadoEm = Date(enviadoEm),
        lida = lida
    )
}
