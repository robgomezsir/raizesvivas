package com.raizesvivas.app.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

private val Context.minhaFamiliaDataStore by preferencesDataStore(name = "minha_familia_prefs")

object MinhaFamiliaPreferences {
    private val KEY_FAMILIA_ID = stringPreferencesKey("minha_familia_id")

    suspend fun salvarFamiliaId(context: Context, familiaId: String?) {
        context.minhaFamiliaDataStore.edit { prefs ->
            if (familiaId != null) {
                prefs[KEY_FAMILIA_ID] = familiaId
                Timber.d("💾 Minha família salva: $familiaId")
            } else {
                prefs.remove(KEY_FAMILIA_ID)
                Timber.d("💾 Minha família removida")
            }
        }
    }

    suspend fun obterFamiliaId(context: Context): String? {
        return context.minhaFamiliaDataStore.data.map { prefs ->
            prefs[KEY_FAMILIA_ID]
        }.first()
    }

    suspend fun limparFamiliaId(context: Context) {
        context.minhaFamiliaDataStore.edit { prefs ->
            prefs.remove(KEY_FAMILIA_ID)
        }
    }
}

