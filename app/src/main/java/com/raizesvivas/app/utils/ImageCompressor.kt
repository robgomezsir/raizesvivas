package com.raizesvivas.app.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Compressor de imagens para reduzir tamanho de arquivo
 * 
 * Comprime imagens para fotos de perfil mantendo qualidade aceitável
 */
object ImageCompressor {
    
    // Tamanho máximo para fotos de perfil: 250KB (balance entre qualidade e tamanho)
    private const val MAX_SIZE_KB_PERFIL = 250L // 250KB máximo para fotos de perfil
    private const val MAX_SIZE_BYTES_PERFIL = MAX_SIZE_KB_PERFIL * 1024
    
    // Tamanho máximo para fotos do álbum: 500KB
    private const val MAX_SIZE_KB_ALBUM = 500L // 500KB máximo para fotos do álbum
    private const val MAX_SIZE_BYTES_ALBUM = MAX_SIZE_KB_ALBUM * 1024
    
    // Tamanho máximo para imagens pequenas (compatibilidade com código existente)
    private const val MAX_SIZE_KB = 10L // 10KB máximo (para uso legado)
    private const val MAX_SIZE_BYTES = MAX_SIZE_KB * 1024
    
    // Dimensões máximas para fotos de perfil
    private const val MAX_WIDTH_PERFIL = 1200 // Largura máxima para perfil
    private const val MAX_HEIGHT_PERFIL = 1200 // Altura máxima para perfil
    
    // Dimensões máximas para fotos do álbum (podem ser maiores que perfil)
    private const val MAX_WIDTH_ALBUM = 1600 // Largura máxima para álbum
    private const val MAX_HEIGHT_ALBUM = 1600 // Altura máxima para álbum
    
    // Dimensões máximas para imagens pequenas (compatibilidade)
    private const val MAX_WIDTH = 800 // Largura máxima
    private const val MAX_HEIGHT = 800 // Altura máxima
    
    private const val QUALIDADE_INICIAL = 85
    
    /**
     * Comprime uma imagem para foto de perfil (até 250KB)
     * 
     * @param imagePath Caminho do arquivo de imagem original
     * @return ByteArray da imagem comprimida ou null em caso de erro
     */
    suspend fun comprimirParaPerfil(imagePath: String): Result<ByteArray> {
        return withContext(Dispatchers.IO) {
            try {
                // Ler bitmap original
                val bitmap = BitmapFactory.decodeFile(imagePath)
                    ?: return@withContext Result.failure(Exception("Erro ao decodificar imagem"))
                
                // Ajustar orientação (EXIF)
                val bitmapOrientado = corrigirOrientacao(bitmap, imagePath)
                
                // Redimensionar se necessário (1200x1200 para perfil)
                val bitmapRedimensionado = redimensionar(bitmapOrientado, MAX_WIDTH_PERFIL, MAX_HEIGHT_PERFIL)
                
                // Comprimir até atingir 250KB
                val imagemComprimida = comprimir(bitmapRedimensionado, MAX_SIZE_BYTES_PERFIL)
                
                Timber.d("✅ Imagem de perfil comprimida: ${imagemComprimida.size} bytes (${imagemComprimida.size / 1024}KB)")
                
                Result.success(imagemComprimida)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao comprimir imagem de perfil")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Comprime uma imagem para foto do álbum (até 500KB)
     * 
     * @param imagePath Caminho do arquivo de imagem original
     * @return ByteArray da imagem comprimida ou null em caso de erro
     */
    suspend fun comprimirParaAlbum(imagePath: String): Result<ByteArray> {
        return withContext(Dispatchers.IO) {
            try {
                // Ler bitmap original
                val bitmap = BitmapFactory.decodeFile(imagePath)
                    ?: return@withContext Result.failure(Exception("Erro ao decodificar imagem"))
                
                // Ajustar orientação (EXIF)
                val bitmapOrientado = corrigirOrientacao(bitmap, imagePath)
                
                // Redimensionar se necessário (1600x1600 para álbum)
                val bitmapRedimensionado = redimensionar(bitmapOrientado, MAX_WIDTH_ALBUM, MAX_HEIGHT_ALBUM)
                
                // Comprimir até atingir 500KB
                val imagemComprimida = comprimir(bitmapRedimensionado, MAX_SIZE_BYTES_ALBUM)
                
                Timber.d("✅ Imagem do álbum comprimida: ${imagemComprimida.size} bytes (${imagemComprimida.size / 1024}KB)")
                
                Result.success(imagemComprimida)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao comprimir imagem do álbum")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Comprime uma imagem para até 10KB (método legado para compatibilidade)
     * 
     * @param imagePath Caminho do arquivo de imagem original
     * @return ByteArray da imagem comprimida ou null em caso de erro
     */
    suspend fun comprimirPara10KB(imagePath: String): Result<ByteArray> {
        return withContext(Dispatchers.IO) {
            try {
                // Ler bitmap original
                val bitmap = BitmapFactory.decodeFile(imagePath)
                    ?: return@withContext Result.failure(Exception("Erro ao decodificar imagem"))
                
                // Ajustar orientação (EXIF)
                val bitmapOrientado = corrigirOrientacao(bitmap, imagePath)
                
                // Redimensionar se necessário
                val bitmapRedimensionado = redimensionar(bitmapOrientado, MAX_WIDTH, MAX_HEIGHT)
                
                // Comprimir até atingir 10KB
                val imagemComprimida = comprimir(bitmapRedimensionado, MAX_SIZE_BYTES)
                
                Timber.d("✅ Imagem comprimida: ${imagemComprimida.size} bytes (${imagemComprimida.size / 1024}KB)")
                
                Result.success(imagemComprimida)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao comprimir imagem")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Comprime um Bitmap para ByteArray até atingir tamanho máximo
     * 
     * @param bitmap Bitmap a ser comprimido
     * @param tamanhoMaximoBytes Tamanho máximo em bytes (padrão: MAX_SIZE_BYTES)
     * @return ByteArray da imagem comprimida
     */
    private suspend fun comprimir(bitmap: Bitmap, tamanhoMaximoBytes: Long = MAX_SIZE_BYTES): ByteArray = withContext(Dispatchers.IO) {
        var qualidade = QUALIDADE_INICIAL
        var outputStream: ByteArrayOutputStream
        var bytes: ByteArray
        var bitmapAtual = bitmap
        var tentativasRedimensionamento = 0
        val maxTentativasRedimensionamento = 3
        
        do {
            outputStream = ByteArrayOutputStream()
            
            // Comprimir bitmap
            bitmapAtual.compress(Bitmap.CompressFormat.JPEG, qualidade, outputStream)
            bytes = outputStream.toByteArray()
            
            // Se ainda está muito grande, reduzir qualidade
            if (bytes.size > tamanhoMaximoBytes) {
                qualidade -= 10
                
                // Se qualidade chegou a 0 e ainda está grande, redimensionar mais
                if (qualidade <= 0 && tentativasRedimensionamento < maxTentativasRedimensionamento) {
                    val fator = Math.sqrt(tamanhoMaximoBytes.toDouble() / bytes.size)
                    val novaWidth = (bitmapAtual.width * fator).toInt().coerceAtLeast(100)
                    val novaHeight = (bitmapAtual.height * fator).toInt().coerceAtLeast(100)
                    
                    // Se já redimensionamos antes, reciclar o bitmap anterior
                    if (bitmapAtual != bitmap) {
                        bitmapAtual.recycle()
                    }
                    
                    bitmapAtual = Bitmap.createScaledBitmap(
                        bitmap,
                        novaWidth,
                        novaHeight,
                        true
                    )
                    
                    qualidade = QUALIDADE_INICIAL
                    tentativasRedimensionamento++
                    outputStream.close()
                    continue // Tentar novamente com o bitmap redimensionado
                } else if (qualidade <= 0) {
                    // Já tentamos redimensionar várias vezes, aceitar o melhor resultado
                    break
                }
            }
            
            outputStream.close()
            
        } while (bytes.size > tamanhoMaximoBytes && qualidade > 0)
        
        // Reciclar bitmap temporário se foi criado
        if (bitmapAtual != bitmap) {
            bitmapAtual.recycle()
        }
        
        bytes
    }
    
    /**
     * Redimensiona bitmap mantendo proporção
     */
    private fun redimensionar(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        // Se já está dentro dos limites, retornar original
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }
        
        // Calcular nova dimensão mantendo proporção
        val ratio = minOf(
            maxWidth.toFloat() / width,
            maxHeight.toFloat() / height
        )
        
        val novaWidth = (width * ratio).toInt()
        val novaHeight = (height * ratio).toInt()
        
        return Bitmap.createScaledBitmap(
            bitmap,
            novaWidth,
            novaHeight,
            true
        )
    }
    
    /**
     * Corrige orientação da imagem baseada em EXIF
     */
    private fun corrigirOrientacao(bitmap: Bitmap, imagePath: String): Bitmap {
        return try {
            val exif = ExifInterface(imagePath)
            val orientacao = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            
            val matrix = Matrix()
            
            when (orientacao) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.postRotate(90f)
                    matrix.postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.postRotate(270f)
                    matrix.postScale(-1f, 1f)
                }
            }
            
            if (orientacao != ExifInterface.ORIENTATION_NORMAL) {
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }
            
        } catch (e: Exception) {
            Timber.w(e, "Erro ao corrigir orientação EXIF")
            bitmap // Retornar bitmap original se houver erro
        }
    }
    
    /**
     * Salva ByteArray em arquivo temporário
     */
    suspend fun salvarTemporario(bytes: ByteArray, prefixo: String = "imagem_"): Result<File> {
        return withContext(Dispatchers.IO) {
            try {
                val tempDir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_PICTURES
                )
                
                val tempFile = File.createTempFile(prefixo, ".jpg", tempDir)
                FileOutputStream(tempFile).use { output ->
                    output.write(bytes)
                }
                
                Timber.d("✅ Arquivo temporário criado: ${tempFile.absolutePath}")
                Result.success(tempFile)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao salvar arquivo temporário")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Verifica se arquivo é uma imagem válida
     */
    fun isImagemValida(imagePath: String): Boolean {
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, options)
            
            options.outWidth > 0 && options.outHeight > 0
            
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Comprime imagem para arquivo temporário
     * 
     * @param imagePath Caminho da imagem original
     * @param targetSizeKB Tamanho alvo em KB (padrão 250KB para perfil)
     * @param paraPerfil Se true, usa compressão otimizada para fotos de perfil (250KB, 1200x1200)
     * @param paraAlbum Se true, usa compressão otimizada para fotos do álbum (500KB, 1600x1600)
     * @return File temporário com imagem comprimida ou null em caso de erro
     */
    suspend fun compressToFile(
        imagePath: String, 
        targetSizeKB: Int = 250, 
        paraPerfil: Boolean = true,
        paraAlbum: Boolean = false
    ): File? {
        return try {
            val resultado = when {
                paraAlbum -> {
                    // Usar compressão otimizada para álbum (500KB, 1600x1600)
                    comprimirParaAlbum(imagePath)
                }
                paraPerfil && targetSizeKB >= 100 -> {
                    // Usar compressão otimizada para perfil (250KB, 1200x1200)
                    comprimirParaPerfil(imagePath)
                }
                else -> {
                    // Usar compressão pequena (compatibilidade)
                    comprimirPara10KB(imagePath)
                }
            }
            
            val bytes = resultado.getOrNull() ?: return null
            
            // Salvar em arquivo temporário
            val tempFile = File.createTempFile("imagem_comprimida_", ".jpg")
            FileOutputStream(tempFile).use { output ->
                output.write(bytes)
            }
            
            Timber.d("✅ Arquivo temporário criado: ${tempFile.absolutePath} (${bytes.size / 1024}KB)")
            tempFile
        } catch (e: Exception) {
            Timber.e(e, "Erro ao comprimir imagem para arquivo")
            null
        }
    }
}

