package com.raizesvivas.app.data.remote.firebase

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import com.raizesvivas.app.utils.RetryHelper
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serviço para gerenciar upload/download de arquivos no Firebase Storage
 * 
 * Gerencia imagens de pessoas (fotos de perfil)
 */
@Singleton
class StorageService @Inject constructor(
    private val storage: FirebaseStorage
) {
    
    private val bucket = storage.reference
    
    /**
     * Faz upload de imagem comprimida
     * 
     * @param bytes ByteArray da imagem comprimida
     * @param caminho Caminho no Storage (ex: "pessoas/pessoaId/foto.jpg")
     * @param contentType Tipo MIME da imagem (padrão: "image/jpeg")
     * @return Result com URL da imagem ou erro
     */
    suspend fun uploadImagem(
        bytes: ByteArray,
        caminho: String,
        contentType: String = "image/jpeg"
    ): Result<String> {
        return RetryHelper.withNetworkRetry {
            try {
                // Validar tipo de arquivo
                if (!validarTipoImagem(contentType)) {
                    return@withNetworkRetry Result.failure(
                        IllegalArgumentException("Tipo de arquivo inválido. Apenas imagens são permitidas.")
                    )
                }
                
                // Validar tamanho (máximo 300KB conforme storage.rules)
                val tamanhoMaximo = 300 * 1024L // 300KB
                if (bytes.size > tamanhoMaximo) {
                    return@withNetworkRetry Result.failure(
                        IllegalArgumentException("Imagem muito grande. Tamanho máximo: 300KB")
                    )
                }
                
                val imageRef = bucket.child(caminho)
                
                // Criar metadata com contentType
                val metadata = StorageMetadata.Builder()
                    .setContentType(contentType)
                    .setCacheControl("public, max-age=31536000") // Cache por 1 ano
                    .build()
                
                // Upload com metadata
                imageRef.putBytes(bytes, metadata)
                    .await()
                
                // Obter URL de download
                val downloadUrl = imageRef.downloadUrl.await()
                
                Timber.d("✅ Imagem enviada: $caminho (${bytes.size} bytes, ${bytes.size / 1024}KB)")
                Timber.d("✅ Content-Type: $contentType")
                Timber.d("✅ URL: $downloadUrl")
                
                Result.success(downloadUrl.toString())
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao fazer upload de imagem")
                Timber.e("   Tipo de erro: ${e.javaClass.simpleName}")
                Timber.e("   Mensagem: ${e.message}")
                Timber.e("   Caminho: $caminho")
                Timber.e("   Tamanho: ${bytes.size / 1024}KB")
                Timber.e("   ContentType: $contentType")
                if (e.message?.contains("permission", ignoreCase = true) == true || 
                    e.message?.contains("denied", ignoreCase = true) == true) {
                    Timber.e("   ⚠️ ERRO DE PERMISSÃO DETECTADO - Verifique as regras do Storage")
                }
                Result.failure(e)
            }
        }
    }
    
    /**
     * Faz upload de arquivo de imagem
     * 
     * @param file Arquivo de imagem
     * @param caminho Caminho no Storage
     * @param contentType Tipo MIME da imagem (padrão: detectado automaticamente)
     * @return Result com URL da imagem ou erro
     */
    suspend fun uploadImagem(
        file: File,
        caminho: String,
        contentType: String? = null
    ): Result<String> {
        return RetryHelper.withNetworkRetry {
            // Detectar contentType se não fornecido (fora do try para estar acessível no catch)
            val mimeType = contentType ?: detectarContentType(file)
            
            try {
                // Validar que o arquivo existe
                if (!file.exists() || !file.isFile) {
                    return@withNetworkRetry Result.failure(
                        IllegalArgumentException("Arquivo não encontrado: ${file.absolutePath}")
                    )
                }
                
                // Validar tamanho (máximo 300KB conforme storage.rules)
                val tamanhoMaximo = 300 * 1024L // 300KB
                val tamanhoArquivo = file.length()
                if (tamanhoArquivo > tamanhoMaximo) {
                    return@withNetworkRetry Result.failure(
                        IllegalArgumentException("Imagem muito grande (${tamanhoArquivo / 1024}KB). Tamanho máximo: 300KB")
                    )
                }
                
                // Validar tipo de arquivo
                if (!validarTipoImagem(mimeType)) {
                    return@withNetworkRetry Result.failure(
                        IllegalArgumentException("Tipo de arquivo inválido: $mimeType. Apenas imagens são permitidas.")
                    )
                }
                
                // Ler arquivo como bytes para garantir que o contentType seja aplicado corretamente
                val bytes = file.readBytes()
                
                // Validar tamanho dos bytes (reutilizar tamanhoMaximo já declarado)
                if (bytes.size > tamanhoMaximo) {
                    return@withNetworkRetry Result.failure(
                        IllegalArgumentException("Imagem muito grande. Tamanho máximo: 300KB")
                    )
                }
                
                val imageRef = bucket.child(caminho)
                
                // Criar metadata com contentType
                val metadata = StorageMetadata.Builder()
                    .setContentType(mimeType)
                    .setCacheControl("public, max-age=31536000") // Cache por 1 ano
                    .build()
                
                // Upload com metadata
                imageRef.putBytes(bytes, metadata)
                    .await()
                
                // Obter URL de download
                val downloadUrl = imageRef.downloadUrl.await()
                
                Timber.d("✅ Imagem enviada: $caminho (${bytes.size} bytes, ${bytes.size / 1024}KB)")
                Timber.d("✅ Content-Type: $mimeType")
                Timber.d("✅ URL: $downloadUrl")
                
                Result.success(downloadUrl.toString())
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao fazer upload de imagem")
                Timber.e("   Tipo de erro: ${e.javaClass.simpleName}")
                Timber.e("   Mensagem: ${e.message}")
                Timber.e("   Arquivo: ${file.absolutePath}")
                Timber.e("   Tamanho: ${file.length() / 1024}KB")
                Timber.e("   Caminho Storage: $caminho")
                Timber.e("   ContentType: $mimeType")
                if (e.message?.contains("permission", ignoreCase = true) == true || 
                    e.message?.contains("denied", ignoreCase = true) == true) {
                    Timber.e("   ⚠️ ERRO DE PERMISSÃO DETECTADO - Verifique as regras do Storage")
                }
                Result.failure(e)
            }
        }
    }
    
    /**
     * Faz download de imagem
     * 
     * @param url URL da imagem no Firebase Storage
     * @return Result com ByteArray da imagem ou erro
     */
    suspend fun downloadImagem(url: String): Result<ByteArray> {
        return RetryHelper.withNetworkRetry {
            try {
                // Se é URL do Firebase Storage, extrair caminho
                val caminho = extrairCaminho(url)
                val imageRef = bucket.child(caminho)
                
                val tamanhoMaximo = 10 * 1024 * 1024L // 10MB máximo
                val bytes = imageRef.getBytes(tamanhoMaximo).await()
                
                Timber.d("✅ Imagem baixada: $caminho (${bytes.size} bytes)")
                
                Result.success(bytes)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao fazer download de imagem")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Deleta imagem do Storage
     * 
     * @param caminho Caminho da imagem no Storage
     * @return Result com sucesso ou erro
     */
    suspend fun deletarImagem(caminho: String): Result<Unit> {
        return RetryHelper.withNetworkRetry {
            try {
                val imageRef = bucket.child(caminho)
                imageRef.delete().await()
                
                Timber.d("✅ Imagem deletada: $caminho")
                Result.success(Unit)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao deletar imagem")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Deleta imagem por URL
     */
    suspend fun deletarImagemPorUrl(url: String): Result<Unit> {
        val caminho = extrairCaminho(url)
        return deletarImagem(caminho)
    }
    
    /**
     * Extrai caminho do Storage de uma URL
     */
    private fun extrairCaminho(url: String): String {
        // URL do Firebase Storage tem formato:
        // https://firebasestorage.googleapis.com/v0/b/projeto.appspot.com/o/caminho%2Farquivo.jpg?alt=media&token=...
        
        return try {
            val uri = android.net.Uri.parse(url)
            
            // Tentar extrair do path
            val path = uri.path ?: return url
            
            // Remover prefixo /o/ e decodificar
            if (path.contains("/o/")) {
                val caminhoEncoded = path.substringAfter("/o/")
                    .substringBefore("?")
                
                java.net.URLDecoder.decode(caminhoEncoded, "UTF-8")
            } else {
                url
            }
            
        } catch (e: Exception) {
            Timber.w(e, "Erro ao extrair caminho da URL")
            url
        }
    }
    
    /**
     * Gera caminho para foto de pessoa
     */
    fun gerarCaminhoFotoPessoa(pessoaId: String): String {
        return "pessoas/$pessoaId/foto.jpg"
    }
    
    /**
     * Faz upload de foto de pessoa
     * 
     * @param file Arquivo de imagem comprimida
     * @param pessoaId ID da pessoa
     * @return Result com URL da imagem ou erro
     */
    suspend fun uploadPessoaPhoto(file: File, pessoaId: String): Result<String> {
        val caminho = gerarCaminhoFotoPessoa(pessoaId)
        return uploadImagem(file, caminho, "image/jpeg")
    }
    
    /**
     * Gera caminho para foto do álbum
     */
    fun gerarCaminhoFotoAlbum(pessoaId: String, fotoId: String): String {
        return "album_familia/$pessoaId/$fotoId.jpg"
    }
    
    /**
     * Faz upload de foto do álbum de família
     * 
     * @param file Arquivo de imagem comprimida
     * @param pessoaId ID da pessoa
     * @param fotoId ID único da foto (geralmente timestamp ou UUID)
     * @return Result com URL da imagem ou erro
     */
    suspend fun uploadFotoAlbum(file: File, pessoaId: String, fotoId: String): Result<String> {
        val caminho = gerarCaminhoFotoAlbum(pessoaId, fotoId)
        
        // Validar tamanho específico para fotos do álbum (máximo 300KB conforme storage.rules)
        val tamanhoMaximoAlbum = 300 * 1024L // 300KB
        val tamanhoArquivo = file.length()
        
        if (tamanhoArquivo > tamanhoMaximoAlbum) {
            val tamanhoKB = tamanhoArquivo / 1024
            return Result.failure(
                IllegalArgumentException(
                    "Imagem muito grande para o álbum (${tamanhoKB}KB). " +
                    "Tamanho máximo permitido: 300KB. " +
                    "A imagem foi comprimida, mas ainda está acima do limite. " +
                    "Tente usar uma imagem menor ou com menos detalhes."
                )
            )
        }
        
        return uploadImagem(file, caminho, "image/jpeg")
    }
    
    /**
     * Valida se o tipo MIME é uma imagem válida
     */
    private fun validarTipoImagem(contentType: String): Boolean {
        return contentType.startsWith("image/") && when (contentType.lowercase()) {
            "image/jpeg", "image/jpg", "image/png", "image/webp" -> true
            else -> false
        }
    }
    
    /**
     * Detecta o tipo MIME de um arquivo baseado na extensão
     */
    private fun detectarContentType(file: File): String {
        val extensao = file.extension.lowercase()
        return when (extensao) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "gif" -> "image/gif"
            else -> "image/jpeg" // Padrão
        }
    }
}

