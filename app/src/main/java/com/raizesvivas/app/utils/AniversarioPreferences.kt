package com.raizesvivas.app.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

private val Context.aniversarioDataStore by preferencesDataStore(name = "aniversario_prefs")

object AniversarioPreferences {
    private val KEY_NOTIFICACOES_HABILITADAS = booleanPreferencesKey("notificacoes_aniversario_habilitadas")
    private val KEY_NOTIFICAR_ANIVERSARIANTE = booleanPreferencesKey("notificar_aniversariante")
    private val KEY_NOTIFICAR_FAMILIARES = booleanPreferencesKey("notificar_familiares")

    /**
     * Verifica se as notificações de aniversário estão habilitadas
     */
    suspend fun notificacoesHabilitadas(context: Context): Boolean {
        return context.aniversarioDataStore.data.map { prefs ->
            prefs[KEY_NOTIFICACOES_HABILITADAS] ?: true // Por padrão, habilitado
        }.first()
    }

    /**
     * Habilita ou desabilita notificações de aniversário
     */
    suspend fun definirNotificacoesHabilitadas(context: Context, habilitadas: Boolean) {
        context.aniversarioDataStore.edit { prefs ->
            prefs[KEY_NOTIFICACOES_HABILITADAS] = habilitadas
            Timber.d("💾 Notificações de aniversário ${if (habilitadas) "habilitadas" else "desabilitadas"}")
        }
    }

    /**
     * Verifica se deve notificar o aniversariante
     */
    suspend fun notificarAniversariante(context: Context): Boolean {
        return context.aniversarioDataStore.data.map { prefs ->
            prefs[KEY_NOTIFICAR_ANIVERSARIANTE] ?: true // Por padrão, habilitado
        }.first()
    }

    /**
     * Define se deve notificar o aniversariante
     */
    suspend fun definirNotificarAniversariante(context: Context, notificar: Boolean) {
        context.aniversarioDataStore.edit { prefs ->
            prefs[KEY_NOTIFICAR_ANIVERSARIANTE] = notificar
        }
    }

    /**
     * Verifica se deve notificar familiares sobre aniversários
     */
    suspend fun notificarFamiliares(context: Context): Boolean {
        return context.aniversarioDataStore.data.map { prefs ->
            prefs[KEY_NOTIFICAR_FAMILIARES] ?: true // Por padrão, habilitado
        }.first()
    }

    /**
     * Define se deve notificar familiares sobre aniversários
     */
    suspend fun definirNotificarFamiliares(context: Context, notificar: Boolean) {
        context.aniversarioDataStore.edit { prefs ->
            prefs[KEY_NOTIFICAR_FAMILIARES] = notificar
        }
    }
}

