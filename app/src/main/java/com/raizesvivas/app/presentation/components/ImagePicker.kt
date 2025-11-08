package com.raizesvivas.app.presentation.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/**
 * Componente para seleção de imagem da galeria ou câmera
 */
@Composable
fun ImagePicker(
    imagePath: String?,
    onImageSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 120
) {
    val context = LocalContext.current
    
    // Launcher para galeria
    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Copiar arquivo para cache local
            val arquivoLocal = copiarParaCache(context, it)
            arquivoLocal?.let { file ->
                onImageSelected(file.absolutePath)
            }
        }
    }
    
    // Launcher para câmera
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri?.let { uri ->
                val arquivoLocal = copiarParaCache(context, uri)
                arquivoLocal?.let { file ->
                    onImageSelected(file.absolutePath)
                }
            }
        }
    }
    
    Card(
        modifier = modifier.size(size.dp),
        shape = CircleShape
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    // Por enquanto, apenas galeria
                    galeriaLauncher.launch("image/*")
                },
            contentAlignment = Alignment.Center
        ) {
            if (imagePath != null && File(imagePath).exists()) {
                // Mostrar imagem selecionada
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(imagePath)
                            .build()
                    ),
                    contentDescription = "Foto da pessoa",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Overlay com botão de remover
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error
                ) {
                    IconButton(
                        onClick = { onImageSelected("") },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remover foto",
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                // Ícone placeholder
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = "Adicionar foto",
                        modifier = Modifier.size((size * 0.4f).dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Foto",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Copia URI para arquivo local em cache
 */
private fun copiarParaCache(context: android.content.Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        
        val cacheDir = context.cacheDir
        val arquivoCache = File(cacheDir, "temp_imagem_${System.currentTimeMillis()}.jpg")
        
        FileOutputStream(arquivoCache).use { output ->
            inputStream.copyTo(output)
        }
        
        arquivoCache
        
    } catch (e: Exception) {
        Timber.e(e, "Erro ao copiar imagem para cache")
        null
    }
}

