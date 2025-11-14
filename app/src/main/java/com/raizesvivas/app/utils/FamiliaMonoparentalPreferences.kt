package com.raizesvivas.app.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.familiaMonoparentalDataStore by preferencesDataStore(name = "familia_monoparental_prefs")

object FamiliaMonoparentalPreferences {
    private val KEY_REJEITADOS = stringSetPreferencesKey("pais_rejeitados")

    /**
     * Salva o ID de um pai que teve a criação de família monoparental rejeitada
     */
    suspend fun adicionarRejeitado(context: Context, paiId: String) {
        context.familiaMonoparentalDataStore.edit { prefs ->
            val rejeitadosAtuais = prefs[KEY_REJEITADOS] ?: emptySet()
            prefs[KEY_REJEITADOS] = rejeitadosAtuais + paiId
        }
    }

    /**
     * Remove o ID de um pai da lista de rejeitados (permite que seja sugerido novamente)
     */
    suspend fun removerRejeitado(context: Context, paiId: String) {
        context.familiaMonoparentalDataStore.edit { prefs ->
            val rejeitadosAtuais = prefs[KEY_REJEITADOS] ?: emptySet()
            prefs[KEY_REJEITADOS] = rejeitadosAtuais - paiId
        }
    }

    /**
     * Retorna o conjunto de IDs de pais que foram rejeitados
     */
    suspend fun obterRejeitados(context: Context): Set<String> {
        return context.familiaMonoparentalDataStore.data.first()[KEY_REJEITADOS] ?: emptySet()
    }

    /**
     * Retorna um Flow com o conjunto de IDs de pais que foram rejeitados
     */
    fun observarRejeitados(context: Context) = context.familiaMonoparentalDataStore.data.map { prefs ->
        prefs[KEY_REJEITADOS] ?: emptySet()
    }

    /**
     * Limpa todos os rejeitados (útil para testes ou reset)
     */
    suspend fun limparTodos(context: Context) {
        context.familiaMonoparentalDataStore.edit { prefs ->
            prefs.remove(KEY_REJEITADOS)
        }
    }
}

