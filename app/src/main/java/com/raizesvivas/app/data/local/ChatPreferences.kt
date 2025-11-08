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
            
            // Buscar mensagens existentes
            val mensagensExistentes = buscarMensagens(conversaId)
            val mensagensAtualizadas = mensagensExistentes + mensagem
            
            // Salvar todas as mensagens
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
     * Busca todas as mensagens de uma conversa
     */
    fun observarMensagens(remetenteId: String, destinatarioId: String): Flow<List<MensagemChat>> {
        val conversaId = gerarConversaId(remetenteId, destinatarioId)
        val key = stringPreferencesKey("${MENSAGENS_KEY_PREFIX}$conversaId")
        
        return context.chatDataStore.data.map { preferences ->
            val mensagensJson = preferences[key] ?: return@map emptyList()
            try {
                val type = object : TypeToken<List<MensagemChatSerializable>>() {}.type
                val mensagensSerializadas: List<MensagemChatSerializable> = gson.fromJson(mensagensJson, type)
                mensagensSerializadas.map { it.toDomain() }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao decodificar mensagens")
                emptyList()
            }
        }
    }
    
    /**
     * Busca todas as mensagens de uma conversa (suspend)
     */
    suspend fun buscarMensagens(remetenteId: String, destinatarioId: String): List<MensagemChat> {
        val conversaId = gerarConversaId(remetenteId, destinatarioId)
        return buscarMensagens(conversaId)
    }
    
    /**
     * Busca todas as mensagens de uma conversa por ID
     */
    private suspend fun buscarMensagens(conversaId: String): List<MensagemChat> {
        val key = stringPreferencesKey("${MENSAGENS_KEY_PREFIX}$conversaId")
        val preferences = context.chatDataStore.data.first()
        val mensagensJson = preferences[key] ?: return emptyList()
        
        return try {
            val type = object : TypeToken<List<MensagemChatSerializable>>() {}.type
            val mensagensSerializadas: List<MensagemChatSerializable> = gson.fromJson(mensagensJson, type)
            mensagensSerializadas.map { it.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao decodificar mensagens")
            emptyList()
        }
    }
    
    /**
     * Busca todas as conversas (lista de IDs de usuários com quem há mensagens)
     */
    suspend fun buscarConversas(usuarioId: String): List<String> {
        val preferences = context.chatDataStore.data.first()
        val conversas = mutableSetOf<String>()
        
        // Iterar sobre todas as chaves conhecidas
        // Como não podemos listar todas as chaves diretamente, vamos tentar buscar por padrão
        // ou manter uma lista separada de conversas ativas
        // Por enquanto, retornamos lista vazia - pode ser melhorado com uma chave separada
        return conversas.toList()
    }
    
    /**
     * Limpa todas as mensagens (útil para logout)
     * Como não podemos listar todas as chaves diretamente no DataStore,
     * esta função limpa apenas as chaves conhecidas ou pode ser melhorada
     * mantendo uma lista separada de conversas ativas
     */
    suspend fun limparMensagens() {
        try {
            // Limpar todas as preferências do chat
            context.chatDataStore.edit { preferences ->
                preferences.clear()
            }
            Timber.d("🗑️ Mensagens do chat limpas")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao limpar mensagens")
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
    
    /**
     * Marca mensagens como lidas
     */
    suspend fun marcarMensagensComoLidas(remetenteId: String, destinatarioId: String) {
        try {
            val conversaId = gerarConversaId(remetenteId, destinatarioId)
            val mensagens = buscarMensagens(conversaId)
            
            // Marcar apenas mensagens do remetente que ainda não foram lidas
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
}

/**
 * Versão serializável da mensagem para armazenamento
 */
private data class MensagemChatSerializable(
    val id: String,
    val remetenteId: String,
    val remetenteNome: String,
    val destinatarioId: String,
    val destinatarioNome: String,
    val texto: String,
    val enviadoEm: Long, // Timestamp em milissegundos
    val lida: Boolean
)

/**
 * Extensões para conversão entre domínio e serializável
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

