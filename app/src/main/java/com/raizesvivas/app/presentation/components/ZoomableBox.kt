package com.raizesvivas.app.presentation.components

import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

/**
 * Estado de zoom e pan para a árvore
 */
data class ZoomState(
    var scale: Float = 1f,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f
)

/**
 * Componente que adiciona funcionalidade de zoom e pan usando transformable
 */
@Composable
fun ZoomableBox(
    modifier: Modifier = Modifier,
    zoomState: ZoomState,
    onZoomStateChange: (ZoomState) -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    var transformState by remember { 
        mutableStateOf(
            androidx.compose.ui.geometry.Offset(zoomState.offsetX, zoomState.offsetY)
        )
    }
    var scale by remember { mutableStateOf(zoomState.scale) }
    
    val minScale = 0.5f
    val maxScale = 3f
    
    // Sincronizar com zoomState externo
    LaunchedEffect(zoomState) {
        scale = zoomState.scale
        transformState = androidx.compose.ui.geometry.Offset(zoomState.offsetX, zoomState.offsetY)
    }
    
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(minScale, maxScale)
        transformState += panChange
        
        val newState = ZoomState(
            scale = scale,
            offsetX = transformState.x,
            offsetY = transformState.y
        )
        onZoomStateChange(newState)
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .transformable(state = transformableState)
    ) {
        // Este Box aplica as transformações de zoom e pan
        // O conteúdo dentro dele (Canvas e Cards) estão na mesma camada transformada
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = zoomState.offsetX.roundToInt(),
                        y = zoomState.offsetY.roundToInt()
                    )
                }
                .graphicsLayer {
                    scaleX = zoomState.scale
                    scaleY = zoomState.scale
                    // Transformar a origem para que o zoom aconteça no centro
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                }
        ) {
            content(Modifier.fillMaxSize())
        }
    }
}

