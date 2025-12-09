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
    size: Int = 120,
    imageUrl: String? = null // URL da imagem remota (Firebase Storage)
) {
    val context = LocalContext.current
    var showRemoveDialog by remember { mutableStateOf(false) }
    
    // Launcher para galeria
    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            Timber.d("📷 Imagem selecionada da galeria: $uri")
            // Copiar arquivo para cache local
            val arquivoLocal = copiarParaCache(context, uri)
            if (arquivoLocal != null) {
                Timber.d("✅ Arquivo copiado para cache: ${arquivoLocal.absolutePath}")
                onImageSelected(arquivoLocal.absolutePath)
            } else {
                Timber.e("❌ Erro ao copiar arquivo para cache")
            }
        } else {
            Timber.d("⚠️ Nenhuma imagem selecionada")
        }
    }
    
    // Diálogo de confirmação de remoção
    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text("Remover foto?")
            },
            text = {
                Text("Tem certeza que deseja remover esta foto de perfil?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRemoveDialog = false
                        onImageSelected("")
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Remover")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRemoveDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    Box(
        modifier = modifier.size(size.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Mostrar imagem se houver caminho local ou URL remota
                val imageData = imagePath?.takeIf { File(it).exists() } ?: imageUrl
                if (imageData != null) {
                    // Mostrar imagem selecionada (local ou remota)
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(imageData)
                                .build()
                        ),
                        contentDescription = "Foto da pessoa",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                // Clicar na imagem abre a galeria para trocar
                                galeriaLauncher.launch("image/*")
                            },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Ícone placeholder - clicável para adicionar foto
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                galeriaLauncher.launch("image/*")
                            },
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
        
        // Botão de remover - FORA do Card, visível apenas quando há foto
        val imageData = imagePath?.takeIf { File(it).exists() } ?: imageUrl
        if (imageData != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(36.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error,
                shadowElevation = 4.dp
            ) {
                IconButton(
                    onClick = { showRemoveDialog = true },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remover foto",
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(20.dp)
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
