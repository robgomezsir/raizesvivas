package com.raizesvivas.app.presentation.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// PALETA RAÍZES VIVAS - Material Design 3 (Tema Azul/Pastel)
// ============================================================================

// ----------------------------------------------------------------------------
// TEMA CLARO - Cores Primárias (Azul Vibrante & Pastéis)
// ----------------------------------------------------------------------------

// Primary: Azul Profundo (Color1)
val RaizesPrimary = Color(0xFF381BE3)
val RaizesOnPrimary = Color(0xFFFFFFFF)
val RaizesPrimaryContainer = Color(0xFFE0E0FF) // Pastel muito claro
val RaizesOnPrimaryContainer = Color(0xFF06006B)

// Secondary: Azul Médio (Color3)
val RaizesSecondary = Color(0xFF6940F1)
val RaizesOnSecondary = Color(0xFFFFFFFF)
val RaizesSecondaryContainer = Color(0xFFEADDFF) // Pastel lavanda
val RaizesOnSecondaryContainer = Color(0xFF24005B)

// Tertiary: Azul Claro/Roxo (Color5)
val RaizesTertiary = Color(0xFF9A65FF)
val RaizesOnTertiary = Color(0xFFFFFFFF)
val RaizesTertiaryContainer = Color(0xFFF2E7FF) // Pastel roxo muito claro
val RaizesOnTertiaryContainer = Color(0xFF2D006D)

// Background e Surface
val RaizesBackground = Color(0xFFFDFBFF) // Branco com leve toque azul
val RaizesOnBackground = Color(0xFF1B1B1F)
val RaizesSurface = Color(0xFFFDFBFF)
val RaizesOnSurface = Color(0xFF1B1B1F)
val RaizesSurfaceVariant = Color(0xFFE3E1EC) // Cinza azulado claro
val RaizesOnSurfaceVariant = Color(0xFF46464F)

// Outline
val RaizesOutline = Color(0xFF767680)
val RaizesOutlineVariant = Color(0xFFC7C5D0)
val RaizesInverseSurface = Color(0xFF303034)
val RaizesInverseOnSurface = Color(0xFFF3F0F4)
val RaizesInversePrimary = Color(0xFFBFC2FF)

// ----------------------------------------------------------------------------
// TEMA ESCURO - Cores Suavizadas
// ----------------------------------------------------------------------------

// Primary: Azul Suave
val RaizesPrimaryDark = Color(0xFFBFC2FF)
val RaizesOnPrimaryDark = Color(0xFF1F00A5)
val RaizesPrimaryContainerDark = Color(0xFF3E3D98) // Azul médio escuro
val RaizesOnPrimaryContainerDark = Color(0xFFE0E0FF)

// Secondary: Lavanda Suave
val RaizesSecondaryDark = Color(0xFFD3BBFF)
val RaizesOnSecondaryDark = Color(0xFF3A0093)
val RaizesSecondaryContainerDark = Color(0xFF5128D6)
val RaizesOnSecondaryContainerDark = Color(0xFFEADDFF)

// Tertiary: Roxo Suave
val RaizesTertiaryDark = Color(0xFFE5B8FF)
val RaizesOnTertiaryDark = Color(0xFF5300A9)
val RaizesTertiaryContainerDark = Color(0xFF7D4AE3)
val RaizesOnTertiaryContainerDark = Color(0xFFF2E7FF)

// Background e Surface
val RaizesBackgroundDark = Color(0xFF1B1B1F)
val RaizesOnBackgroundDark = Color(0xFFE4E1E6)
val RaizesSurfaceDark = Color(0xFF1B1B1F)
val RaizesOnSurfaceDark = Color(0xFFE4E1E6)
val RaizesSurfaceVariantDark = Color(0xFF46464F)
val RaizesOnSurfaceVariantDark = Color(0xFFC7C5D0)

// Outline
val RaizesOutlineDark = Color(0xFF90909A)
val RaizesOutlineVariantDark = Color(0xFF46464F)
val RaizesInverseSurfaceDark = Color(0xFFE4E1E6)
val RaizesInverseOnSurfaceDark = Color(0xFF1B1B1F)
val RaizesInversePrimaryDark = Color(0xFF381BE3)

// ----------------------------------------------------------------------------
// CORES DE ESTADO (Light & Dark)
// ----------------------------------------------------------------------------

// Error (Light)
val RaizesError = Color(0xFFBA1A1A)
val RaizesOnError = Color(0xFFFFFFFF)
val RaizesErrorContainer = Color(0xFFFFDAD6)
val RaizesOnErrorContainer = Color(0xFF410002)

// Error (Dark)
val RaizesErrorDark = Color(0xFFFFB4AB)
val RaizesOnErrorDark = Color(0xFF690005)
val RaizesErrorContainerDark = Color(0xFF93000A)
val RaizesOnErrorContainerDark = Color(0xFFFFDAD6)

// ----------------------------------------------------------------------------
// CORES SEMÂNTICAS - Uso específico no app
// ----------------------------------------------------------------------------

// Heritage: Mantendo tons terrosos mas harmonizados
val Heritage = Color(0xFF8D6E63)
val HeritageLight = Color(0xFFD7CCC8)
val HeritageDark = Color(0xFF5D4037)

// Growth: Verde harmonizado
val Growth = Color(0xFF66BB6A)
val GrowthLight = Color(0xFFC8E6C9)
val GrowthDark = Color(0xFF388E3C)

// Legacy: Dourado
val Legacy = Color(0xFFFFB300)
val LegacyLight = Color(0xFFFFECB3)
val LegacyDark = Color(0xFFFF8F00)

// Connection: Azul (agora alinhado com o tema principal)
val Connection = RaizesPrimary
val ConnectionLight = RaizesPrimaryContainer
val ConnectionDark = RaizesPrimaryDark

// ----------------------------------------------------------------------------
// CORES UTILITÁRIAS
// ----------------------------------------------------------------------------

val RaizesSuccess = Growth
val RaizesWarning = Legacy
val RaizesInfo = Connection

