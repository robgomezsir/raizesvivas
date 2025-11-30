package com.raizesvivas.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = RaizesPrimary,
    onPrimary = RaizesOnPrimary,
    primaryContainer = RaizesPrimaryContainer,
    onPrimaryContainer = RaizesOnPrimaryContainer,

    secondary = RaizesSecondary,
    onSecondary = RaizesOnSecondary,
    secondaryContainer = RaizesSecondaryContainer,
    onSecondaryContainer = RaizesOnSecondaryContainer,

    tertiary = RaizesTertiary,
    onTertiary = RaizesOnTertiary,
    tertiaryContainer = RaizesTertiaryContainer,
    onTertiaryContainer = RaizesOnTertiaryContainer,

    background = RaizesBackground,
    onBackground = RaizesOnBackground,

    surface = RaizesSurface,
    onSurface = RaizesOnSurface,
    surfaceVariant = RaizesSurfaceVariant,
    onSurfaceVariant = RaizesOnSurfaceVariant,

    error = RaizesError,
    onError = RaizesOnError,
    errorContainer = RaizesErrorContainer,
    onErrorContainer = RaizesOnErrorContainer,

    outline = RaizesOutline,
    outlineVariant = RaizesOutlineVariant,
    inverseSurface = RaizesInverseSurface,
    inverseOnSurface = RaizesInverseOnSurface,
    inversePrimary = RaizesInversePrimary,
    surfaceTint = RaizesPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = RaizesPrimaryDark,
    onPrimary = RaizesOnPrimaryDark,
    primaryContainer = RaizesPrimaryContainerDark,
    onPrimaryContainer = RaizesOnPrimaryContainerDark,

    secondary = RaizesSecondaryDark,
    onSecondary = RaizesOnSecondaryDark,
    secondaryContainer = RaizesSecondaryContainerDark,
    onSecondaryContainer = RaizesOnSecondaryContainerDark,

    tertiary = RaizesTertiaryDark,
    onTertiary = RaizesOnTertiaryDark,
    tertiaryContainer = RaizesTertiaryContainerDark,
    onTertiaryContainer = RaizesOnTertiaryContainerDark,

    background = RaizesBackgroundDark,
    onBackground = RaizesOnBackgroundDark,

    surface = RaizesSurfaceDark,
    onSurface = RaizesOnSurfaceDark,
    surfaceVariant = RaizesSurfaceVariantDark,
    onSurfaceVariant = RaizesOnSurfaceVariantDark,

    error = RaizesErrorDark,
    onError = RaizesOnErrorDark,
    errorContainer = RaizesErrorContainerDark,
    onErrorContainer = RaizesOnErrorContainerDark,

    outline = RaizesOutlineDark,
    outlineVariant = RaizesOutlineVariantDark,
    inverseSurface = RaizesInverseSurfaceDark,
    inverseOnSurface = RaizesInverseOnSurfaceDark,
    inversePrimary = RaizesPrimary,
    surfaceTint = RaizesPrimaryDark
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

