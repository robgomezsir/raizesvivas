package com.raizesvivas.app.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver

enum class ThemeMode {
    SISTEMA,
    CLARO,
    ESCURO
}

data class ThemeController(
    val modo: ThemeMode,
    val selecionarModo: (ThemeMode) -> Unit
)

val LocalThemeController = staticCompositionLocalOf<ThemeController> {
    error("ThemeController não foi provido")
}

val ThemeModeStateSaver: Saver<MutableState<ThemeMode>, String> = Saver(
    save = { it.value.name },
    restore = { saved -> mutableStateOf(ThemeMode.valueOf(saved)) }
)



