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
 * Comprime imagens para até 10KB mantendo qualidade aceitável
 */
object ImageCompressor {
    
    private const val MAX_SIZE_KB = 10L // 10KB máximo
    private const val MAX_SIZE_BYTES = MAX_SIZE_KB * 1024
    private const val MAX_WIDTH = 800 // Largura máxima
    private const val MAX_HEIGHT = 800 // Altura máxima
    private const val QUALIDADE_INICIAL = 85
    
    /**
     * Comprime uma imagem para até 10KB
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
                val imagemComprimida = comprimir(bitmapRedimensionado)
                
                Timber.d("✅ Imagem comprimida: ${imagemComprimida.size} bytes (${imagemComprimida.size / 1024}KB)")
                
                Result.success(imagemComprimida)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao comprimir imagem")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Comprime um Bitmap para ByteArray
     */
    private suspend fun comprimir(bitmap: Bitmap): ByteArray = withContext(Dispatchers.IO) {
        var qualidade = QUALIDADE_INICIAL
        var outputStream: ByteArrayOutputStream
        var bytes: ByteArray
        
        do {
            outputStream = ByteArrayOutputStream()
            
            // Comprimir bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, qualidade, outputStream)
            bytes = outputStream.toByteArray()
            
            // Se ainda está muito grande, reduzir qualidade
            if (bytes.size > MAX_SIZE_BYTES) {
                qualidade -= 10
                
                // Se qualidade chegou a 0 e ainda está grande, redimensionar mais
                if (qualidade <= 0) {
                    val fator = Math.sqrt(MAX_SIZE_BYTES.toDouble() / bytes.size)
                    val novaWidth = (bitmap.width * fator).toInt().coerceAtLeast(100)
                    val novaHeight = (bitmap.height * fator).toInt().coerceAtLeast(100)
                    
                    val bitmapMenor = Bitmap.createScaledBitmap(
                        bitmap,
                        novaWidth,
                        novaHeight,
                        true
                    )
                    
                    qualidade = QUALIDADE_INICIAL
                    bitmap.compress(Bitmap.CompressFormat.JPEG, qualidade, outputStream)
                    bytes = outputStream.toByteArray()
                    
                    bitmapMenor.recycle()
                    
                    // Se ainda não funcionou, retornar o melhor resultado
                    if (bytes.size > MAX_SIZE_BYTES && qualidade == 0) {
                        break
                    }
                }
            }
            
            outputStream.close()
            
        } while (bytes.size > MAX_SIZE_BYTES && qualidade > 0)
        
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
     * @param targetSizeKB Tamanho alvo em KB (padrão 10KB)
     * @return File temporário com imagem comprimida ou null em caso de erro
     */
    @Suppress("UNUSED_PARAMETER")
    suspend fun compressToFile(imagePath: String, targetSizeKB: Int = 10): File? {
        return try {
            val resultado = comprimirPara10KB(imagePath)
            val bytes = resultado.getOrNull() ?: return null
            
            // Salvar em arquivo temporário
            val tempFile = File.createTempFile("imagem_comprimida_", ".jpg")
            FileOutputStream(tempFile).use { output ->
                output.write(bytes)
            }
            
            tempFile
        } catch (e: Exception) {
            Timber.e(e, "Erro ao comprimir imagem para arquivo")
            null
        }
    }
}

