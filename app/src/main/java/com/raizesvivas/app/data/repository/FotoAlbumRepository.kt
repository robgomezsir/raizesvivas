package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.FotoAlbum
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
    private val firestoreService: FirestoreService
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
            Result.failure(e)
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
            Result.failure(e)
        }
    }
    
    /**
     * Salva uma nova foto no álbum
     */
    suspend fun salvarFoto(foto: FotoAlbum): Result<String> {
        return try {
            val resultado = firestoreService.salvarFotoAlbum(foto)
            Timber.d("✅ Foto salva no álbum: ${foto.id}")
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao salvar foto no álbum")
            Result.failure(e)
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
            Result.failure(e)
        }
    }
    
    /**
     * Observa fotos do álbum em tempo real
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
}

