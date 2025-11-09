package com.raizesvivas.app.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.raizesvivas.app.presentation.theme.ThemeMode
import kotlinx.coroutines.flow.first

private val Context.themeDataStore by preferencesDataStore(name = "theme_preferences")

object ThemePreferenceManager {
    private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")

    suspend fun writeThemeMode(context: Context, mode: ThemeMode) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun readThemeMode(context: Context): ThemeMode? {
        val prefs = context.themeDataStore.data.first()
        val value = prefs[KEY_THEME_MODE] ?: return null
        return runCatching { ThemeMode.valueOf(value) }.getOrNull()
    }
}



