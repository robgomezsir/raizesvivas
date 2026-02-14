package com.raizesvivas.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class FamilyMood {
    ANCESTRAL, SERENE, LEGACY
}

@Composable
fun RaizesVivasTheme(
    mood: FamilyMood = FamilyMood.ANCESTRAL,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> {
            darkColorScheme(
                primary = when(mood) {
                    FamilyMood.ANCESTRAL -> FamilyMoods.AncestralPrimary
                    FamilyMood.SERENE -> FamilyMoods.SerenePrimary
                    FamilyMood.LEGACY -> FamilyMoods.LegacyPrimary
                },
                surface = DarkSurface,
                onSurface = DarkOnSurface
            )
        }
        else -> {
            lightColorScheme(
                primary = when(mood) {
                    FamilyMood.ANCESTRAL -> FamilyMoods.AncestralPrimary
                    FamilyMood.SERENE -> FamilyMoods.SerenePrimary
                    FamilyMood.LEGACY -> FamilyMoods.LegacyPrimary
                }
            )
        }
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
