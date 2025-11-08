package com.raizesvivas.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PastelPrimary,
    onPrimary = PastelOnPrimary,
    primaryContainer = PastelPrimaryContainer,
    onPrimaryContainer = PastelOnPrimaryContainer,

    secondary = PastelSecondary,
    onSecondary = PastelOnSecondary,
    secondaryContainer = PastelSecondaryContainer,
    onSecondaryContainer = PastelOnSecondaryContainer,

    tertiary = PastelTertiary,
    onTertiary = PastelOnTertiary,
    tertiaryContainer = PastelTertiaryContainer,
    onTertiaryContainer = PastelOnTertiaryContainer,

    background = PastelBackground,
    onBackground = PastelOnBackground,

    surface = PastelSurface,
    onSurface = PastelOnSurface,
    surfaceVariant = PastelSurfaceVariant,
    onSurfaceVariant = PastelOnSurfaceVariant,

    error = PastelError,
    onError = PastelOnError,
    errorContainer = PastelErrorContainer,
    onErrorContainer = PastelOnErrorContainer,

    outline = PastelOutline,
    outlineVariant = PastelOutlineVariant,
    inverseSurface = PastelInverseSurface,
    inverseOnSurface = PastelInverseOnSurface,
    inversePrimary = PastelInversePrimary,
    surfaceTint = PastelPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = PastelPrimaryDark,
    onPrimary = PastelOnPrimaryDark,
    primaryContainer = PastelPrimaryContainerDark,
    onPrimaryContainer = PastelOnPrimaryContainerDark,

    secondary = PastelSecondaryDark,
    onSecondary = PastelOnSecondaryDark,
    secondaryContainer = PastelSecondaryContainerDark,
    onSecondaryContainer = PastelOnSecondaryContainerDark,

    tertiary = PastelTertiaryDark,
    onTertiary = PastelOnTertiaryDark,
    tertiaryContainer = PastelTertiaryContainerDark,
    onTertiaryContainer = PastelOnTertiaryContainerDark,

    background = PastelBackgroundDark,
    onBackground = PastelOnBackgroundDark,

    surface = PastelSurfaceDark,
    onSurface = PastelOnSurfaceDark,
    surfaceVariant = PastelSurfaceVariantDark,
    onSurfaceVariant = PastelOnSurfaceVariantDark,

    error = PastelErrorDark,
    onError = PastelOnErrorDark,
    errorContainer = PastelErrorContainerDark,
    onErrorContainer = PastelOnErrorContainerDark,

    outline = PastelOutlineDark,
    outlineVariant = PastelOutlineVariantDark,
    inverseSurface = PastelInverseSurfaceDark,
    inverseOnSurface = PastelInverseOnSurfaceDark,
    inversePrimary = PastelPrimary,
    surfaceTint = PastelPrimaryDark
)

@Composable
@Suppress("UNUSED_PARAMETER")
fun RaizesVivasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

