package com.raizesvivas.app.data.remote.firebase

import com.google.firebase.storage.FirebaseStorage
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
     * @return Result com URL da imagem ou erro
     */
    suspend fun uploadImagem(
        bytes: ByteArray,
        caminho: String
    ): Result<String> {
        return RetryHelper.withNetworkRetry {
            try {
                val imageRef = bucket.child(caminho)
                
                // Upload com metadata
                imageRef.putBytes(bytes)
                    .await()
                
                // Obter URL de download
                val downloadUrl = imageRef.downloadUrl.await()
                
                Timber.d("✅ Imagem enviada: $caminho (${bytes.size} bytes)")
                Timber.d("✅ URL: $downloadUrl")
                
                Result.success(downloadUrl.toString())
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao fazer upload de imagem")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Faz upload de arquivo de imagem
     * 
     * @param file Arquivo de imagem
     * @param caminho Caminho no Storage
     * @return Result com URL da imagem ou erro
     */
    suspend fun uploadImagem(
        file: File,
        caminho: String
    ): Result<String> {
        return RetryHelper.withNetworkRetry {
            try {
                val imageRef = bucket.child(caminho)
                
                imageRef.putFile(android.net.Uri.fromFile(file))
                    .await()
                
                val downloadUrl = imageRef.downloadUrl.await()
                
                Timber.d("✅ Imagem enviada: $caminho (${file.length()} bytes)")
                Timber.d("✅ URL: $downloadUrl")
                
                Result.success(downloadUrl.toString())
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao fazer upload de imagem")
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
        return uploadImagem(file, caminho)
    }
}

