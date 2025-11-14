package com.raizesvivas.app.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

private val Context.familiaOrdemDataStore by preferencesDataStore(name = "familia_ordem_prefs")

object FamiliaOrdemPreferences {
    private val KEY_ORDEM = stringPreferencesKey("ordem_familias")

    suspend fun salvarOrdem(context: Context, ordem: List<String>) {
        context.familiaOrdemDataStore.edit { prefs ->
            val ordemJson = ordem.joinToString(",")
            prefs[KEY_ORDEM] = ordemJson
            Timber.d("💾 Ordem salva: ${ordem.size} famílias")
        }
    }

    suspend fun obterOrdem(context: Context): List<String> {
        return context.familiaOrdemDataStore.data.map { prefs ->
            val ordemJson = prefs[KEY_ORDEM] ?: ""
            if (ordemJson.isBlank()) {
                emptyList()
            } else {
                ordemJson.split(",").filter { it.isNotBlank() }
            }
        }.first()
    }

    suspend fun limparOrdem(context: Context) {
        context.familiaOrdemDataStore.edit { prefs ->
            prefs.remove(KEY_ORDEM)
        }
    }
}

