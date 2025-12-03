package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.FotoAlbum
import com.raizesvivas.app.domain.model.ComentarioFoto
import com.raizesvivas.app.utils.ErrorHandler
import com.raizesvivas.app.utils.RateLimiter
import com.raizesvivas.app.utils.OperationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gerenciar fotos do álbum de família
 */
@Singleton
class FotoAlbumRepository @Inject constructor(
    private val firestoreService: FirestoreService,
    private val rateLimiter: RateLimiter
) {
    
    /**
     * Busca todas as fotos do álbum de uma família
     */
    suspend fun buscarFotosPorFamilia(familiaId: String): Result<List<FotoAlbum>> {
        return try {
            val resultado = firestoreService.buscarFotosAlbum(familiaId)
            Timber.d("✅ Fotos do álbum carregadas: ${resultado.getOrNull()?.size ?: 0}")
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar fotos do álbum")
            val appError = ErrorHandler.handle(e)
            Result.failure(Exception(appError.message, e))
        }
    }
    
    /**
     * Busca fotos de uma pessoa específica
     */
    suspend fun buscarFotosPorPessoa(pessoaId: String): Result<List<FotoAlbum>> {
        return try {
            val resultado = firestoreService.buscarFotosAlbumPorPessoa(pessoaId)
            Timber.d("✅ Fotos da pessoa carregadas: ${resultado.getOrNull()?.size ?: 0}")
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar fotos da pessoa")
            val appError = ErrorHandler.handle(e)
            Result.failure(Exception(appError.message, e))
        }
    }
    
    /**
     * Salva uma nova foto no álbum
     */
    suspend fun salvarFoto(foto: FotoAlbum, userId: String? = null): Result<String> {
        return try {
            // Verificar rate limiting
            if (!rateLimiter.canExecute(OperationType.UPLOAD_FOTO, userId)) {
                val mensagem = rateLimiter.getLimitExceededMessage(OperationType.UPLOAD_FOTO)
                return Result.failure(Exception(mensagem))
            }
            
            val resultado = firestoreService.salvarFotoAlbum(foto)
            
            // Registrar operação se bem-sucedida
            resultado.onSuccess {
                rateLimiter.recordOperation(OperationType.UPLOAD_FOTO, userId)
            }
            
            Timber.d("✅ Foto salva no álbum: ${foto.id}")
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao salvar foto no álbum")
            val appError = ErrorHandler.handle(e)
            Result.failure(Exception(appError.message, e))
        }
    }
    
    /**
     * Deleta uma foto do álbum
     */
    suspend fun deletarFoto(fotoId: String): Result<Unit> {
        return try {
            val resultado = firestoreService.deletarFotoAlbum(fotoId)
            Timber.d("✅ Foto deletada do álbum: $fotoId")
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao deletar foto do álbum")
            val appError = ErrorHandler.handle(e)
            Result.failure(Exception(appError.message, e))
        }
    }
    
    /**
     * Observa TODAS as fotos do álbum em tempo real (sem filtro por familiaId)
     * App colaborativo: todos os usuários autenticados podem ver todas as fotos
     */
    fun observarTodasFotos(): Flow<List<FotoAlbum>> = flow {
        try {
            firestoreService.observarTodasFotosAlbum().collect { fotos ->
                emit(fotos)
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao observar todas as fotos do álbum")
            emit(emptyList())
        }
    }
    
    /**
     * Observa fotos do álbum em tempo real (versão com filtro por familiaId - mantida para compatibilidade)
     */
    fun observarFotosPorFamilia(familiaId: String): Flow<List<FotoAlbum>> = flow {
        try {
            firestoreService.observarFotosAlbum(familiaId).collect { fotos ->
                emit(fotos)
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao observar fotos do álbum")
            emit(emptyList())
        }
    }
    
    /**
     * Adiciona ou atualiza um apoio em uma foto
     */
    suspend fun adicionarApoio(fotoId: String, usuarioId: String, tipoApoio: com.raizesvivas.app.domain.model.TipoApoioFoto): Result<Unit> {
        return try {
            val resultado = firestoreService.adicionarApoioFoto(fotoId, usuarioId, tipoApoio)
            Timber.d("✅ Apoio adicionado à foto: $fotoId")
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao adicionar apoio à foto")
            val appError = ErrorHandler.handle(e)
            Result.failure(Exception(appError.message, e))
        }
    }
    
    /**
     * Remove um apoio de uma foto
     */
    suspend fun removerApoio(fotoId: String, usuarioId: String): Result<Unit> {
        return try {
            val resultado = firestoreService.removerApoioFoto(fotoId, usuarioId)
            Timber.d("✅ Apoio removido da foto: $fotoId")
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao remover apoio da foto")
            val appError = ErrorHandler.handle(e)
            Result.failure(Exception(appError.message, e))
        }
    }
    
    /**
     * Busca comentários de uma foto
     */
    suspend fun buscarComentarios(fotoId: String): Result<List<ComentarioFoto>> {
        return try {
            val resultado = firestoreService.buscarComentariosFoto(fotoId)
            Timber.d("✅ Comentários carregados: ${resultado.getOrNull()?.size ?: 0}")
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar comentários")
            val appError = ErrorHandler.handle(e)
            Result.failure(Exception(appError.message, e))
        }
    }
    
    /**
     * Adiciona um comentário em uma foto
     */
    suspend fun adicionarComentario(comentario: ComentarioFoto): Result<String> {
        return try {
            val resultado = firestoreService.adicionarComentarioFoto(comentario)
            Timber.d("✅ Comentário adicionado à foto: ${comentario.fotoId}")
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao adicionar comentário")
            val appError = ErrorHandler.handle(e)
            Result.failure(Exception(appError.message, e))
        }
    }
    
    /**
     * Deleta um comentário
     */
    suspend fun deletarComentario(fotoId: String, comentarioId: String): Result<Unit> {
        return try {
            val resultado = firestoreService.deletarComentarioFoto(fotoId, comentarioId)
            Timber.d("✅ Comentário deletado: $comentarioId")
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao deletar comentário")
            val appError = ErrorHandler.handle(e)
            Result.failure(Exception(appError.message, e))
        }
    }
    
    /**
     * Observa comentários de uma foto em tempo real
     */
    fun observarComentarios(fotoId: String): Flow<List<ComentarioFoto>> = flow {
        try {
            firestoreService.observarComentariosFoto(fotoId).collect { comentarios ->
                emit(comentarios)
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao observar comentários")
            emit(emptyList())
        }
    }
}

