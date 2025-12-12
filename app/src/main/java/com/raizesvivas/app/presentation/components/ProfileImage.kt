package com.raizesvivas.app.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import coil.size.Size

/**
 * Componente reutilizável para exibir fotos de perfil
 * 
 * Lida corretamente com:
 * - URLs nulas (mostra ícone padrão)
 * - Cache do Coil (invalida quando URL muda)
 * - Placeholder e erro
 */
@Composable
fun ProfileImage(
    photoUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    contentDescription: String = "Foto de perfil",
    placeholder: Painter? = null,
    error: Painter? = null
) {
    val context = LocalContext.current
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl.isNullOrBlank()) {
            // Sem foto - mostrar ícone padrão
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = contentDescription,
                modifier = Modifier.size(size * 0.6f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            // Com foto - carregar com Coil
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                    .data(photoUrl)
                    .size(Size.ORIGINAL) // Carregar tamanho original
                    .crossfade(true)
                    // Adicionar cache key baseado na URL para forçar reload quando muda
                    .memoryCacheKey(photoUrl)
                    .diskCacheKey(photoUrl)
                    .build(),
                placeholder = placeholder,
                error = error ?: placeholder
            )
            
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.size(size),
                contentScale = ContentScale.Crop
            )
        }
    }
}
