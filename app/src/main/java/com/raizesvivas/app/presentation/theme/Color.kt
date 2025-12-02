package com.raizesvivas.app.presentation.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// PALETA RAÍZES VIVAS - Material Design 3 (Nova Paleta Vibrante)
// ============================================================================

// ----------------------------------------------------------------------------
// TEMA CLARO - Cores Primárias (Nova Paleta)
// ----------------------------------------------------------------------------

// Primary: Verde Vibrante (#00BF7D) - Contraste 8.75:1 com texto preto
val RaizesPrimary = Color(0xFF00BF7D)
val RaizesOnPrimary = Color(0xFF000000) // Preto para contraste
val RaizesPrimaryContainer = Color(0xFFB3F0DD) // Versão pastel do verde
val RaizesOnPrimaryContainer = Color(0xFF003D2A) // Verde muito escuro

// Secondary: Teal/Azul-esverdeado (#00B4C5) - Contraste 8.33:1 com texto preto
val RaizesSecondary = Color(0xFF00B4C5)
val RaizesOnSecondary = Color(0xFF000000) // Preto para contraste
val RaizesSecondaryContainer = Color(0xFFB3E8F0) // Versão pastel do teal
val RaizesOnSecondaryContainer = Color(0xFF003A3F) // Teal muito escuro

// Tertiary: Azul (#0073E6) - Contraste 4.57:1 com texto branco
val RaizesTertiary = Color(0xFF0073E6)
val RaizesOnTertiary = Color(0xFFFFFFFF) // Branco para contraste
val RaizesTertiaryContainer = Color(0xFFB3D9FF) // Versão pastel do azul
val RaizesOnTertiaryContainer = Color(0xFF001A3D) // Azul muito escuro

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
// TEMA ESCURO - Cores Suavizadas (Nova Paleta)
// ----------------------------------------------------------------------------

// Primary: Verde mais claro para tema escuro (#00BF7D com ajuste)
val RaizesPrimaryDark = Color(0xFF00E699) // Versão mais clara para tema escuro
val RaizesOnPrimaryDark = Color(0xFF003D2A) // Verde escuro
val RaizesPrimaryContainerDark = Color(0xFF006B4D) // Verde médio escuro
val RaizesOnPrimaryContainerDark = Color(0xFFB3F0DD)

// Secondary: Teal mais claro para tema escuro (#00B4C5 com ajuste)
val RaizesSecondaryDark = Color(0xFF00D9F0) // Versão mais clara para tema escuro
val RaizesOnSecondaryDark = Color(0xFF003A3F) // Teal escuro
val RaizesSecondaryContainerDark = Color(0xFF006B75) // Teal médio escuro
val RaizesOnSecondaryContainerDark = Color(0xFFB3E8F0)

// Tertiary: Azul mais claro para tema escuro (#0073E6 com ajuste)
val RaizesTertiaryDark = Color(0xFF4DA3FF) // Versão mais clara para tema escuro
val RaizesOnTertiaryDark = Color(0xFF001A3D) // Azul escuro
val RaizesTertiaryContainerDark = Color(0xFF004C99) // Azul médio escuro
val RaizesOnTertiaryContainerDark = Color(0xFFB3D9FF)

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

// Growth: Verde da nova paleta (#00BF7D)
val Growth = RaizesPrimary // #00BF7D
val GrowthLight = RaizesPrimaryContainer // Verde pastel claro
val GrowthDark = Color(0xFF006B4D) // Versão escura

// Legacy: Azul escuro da nova paleta (#2546F0) - Contraste 6.54:1 com texto branco
val Legacy = Color(0xFF2546F0)
val LegacyLight = Color(0xFFB3C0FF) // Versão pastel
val LegacyDark = Color(0xFF1A33B3) // Versão escura

// Connection: Roxo da nova paleta (#5928ED) - Contraste 7.12:1 com texto branco
val Connection = Color(0xFF5928ED)
val ConnectionLight = Color(0xFFD4C6FF) // Versão pastel
val ConnectionDark = Color(0xFF3D1CA3) // Versão escura

// ----------------------------------------------------------------------------
// CORES UTILITÁRIAS
// ----------------------------------------------------------------------------

val RaizesSuccess = Growth
val RaizesWarning = Legacy
val RaizesInfo = Connection

