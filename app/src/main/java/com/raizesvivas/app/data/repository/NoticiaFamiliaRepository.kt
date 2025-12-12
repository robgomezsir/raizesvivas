package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.NoticiaFamilia
import com.raizesvivas.app.domain.model.TipoNoticiaFamilia
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gerenciar notícias/atividades da família
 */
@Singleton
class NoticiaFamiliaRepository @Inject constructor(
    private val firestoreService: FirestoreService
) {
    
    /**
     * Observa todas as notícias em tempo real
     */
    fun observarTodasNoticias(): Flow<List<NoticiaFamilia>> {
        return firestoreService.observarNoticias()
            .map { noticias ->
                Timber.d("📰 Observando notícias: ${noticias.size} notícias recebidas")
                noticias
            }
            .catch { error ->
                Timber.e(error, "❌ Erro ao observar notícias")
                emit(emptyList())
            }
    }
    
    /**
     * Observa notícias recentes (últimas 24h)
     */
    fun observarNoticiasRecentes(): Flow<List<NoticiaFamilia>> {
        return observarTodasNoticias()
            .map { noticias ->
                noticias.filter { it.ehRecente }
                    .sortedByDescending { it.criadoEm }
                    .take(10) // Limitar a 10 notícias mais recentes
            }
    }
    
    /**
     * Cria uma nova notícia
     */
    suspend fun criarNoticia(noticia: NoticiaFamilia): Result<Unit> {
        return try {
            firestoreService.salvarNoticia(noticia)
            Timber.d("✅ Notícia criada: ${noticia.titulo}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao criar notícia")
            Result.failure(e)
        }
    }
    
    /**
     * Marca notícia como lida
     */
    suspend fun marcarComoLida(noticiaId: String): Result<Unit> {
        return try {
            firestoreService.marcarNoticiaLida(noticiaId)
            Timber.d("✅ Notícia marcada como lida: $noticiaId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao marcar notícia como lida")
            Result.failure(e)
        }
    }
    
    /**
     * Busca notícias por tipo
     */
    fun observarNoticiasPorTipo(tipo: TipoNoticiaFamilia): Flow<List<NoticiaFamilia>> {
        return observarTodasNoticias()
            .map { noticias ->
                noticias.filter { it.tipo == tipo }
                    .sortedByDescending { it.criadoEm }
            }
    }
    
    /**
     * Deleta notícia
     */
    suspend fun deletar(noticiaId: String): Result<Unit> {
        return try {
            firestoreService.deletarNoticia(noticiaId)
            Timber.d("✅ Notícia deletada: $noticiaId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao deletar notícia")
            Result.failure(e)
        }
    }
}
