package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.local.dao.AmigoDao
import com.raizesvivas.app.data.local.entities.AmigoEntity
import com.raizesvivas.app.data.local.entities.toDomain
import com.raizesvivas.app.data.local.entities.toEntity
import com.raizesvivas.app.domain.model.Amigo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gerenciar amigos da família
 * 
 * IMPORTANTE: Todos os usuários têm acesso total às operações de amigos.
 * Não há restrições de administrador - qualquer usuário pode:
 * - Ver todos os amigos
 * - Adicionar novos amigos
 * - Editar amigos existentes
 * - Excluir amigos
 */
@Singleton
class AmigoRepository @Inject constructor(
    private val amigoDao: AmigoDao
) {
    
    /**
     * Observa todos os amigos (do cache local)
     * Atualiza automaticamente quando o cache muda
     */
    fun observarTodosAmigos(): Flow<List<Amigo>> {
        return amigoDao.observarTodosAmigos()
            .map { entities -> 
                Timber.d("📋 Observando amigos: ${entities.size} entidades no cache local")
                entities.map { it.toDomain() } 
            }
    }
    
    /**
     * Busca todos os amigos (uma vez)
     */
    suspend fun buscarTodosAmigos(): List<Amigo> {
        return amigoDao.buscarTodosAmigos().map { it.toDomain() }
    }
    
    /**
     * Busca amigo por ID
     */
    suspend fun buscarPorId(amigoId: String): Amigo? {
        if (amigoId.isBlank()) {
            Timber.w("⚠️ Tentativa de buscar amigo com ID vazio")
            return null
        }
        
        return amigoDao.buscarPorId(amigoId)?.toDomain()
    }
    
    /**
     * Salva ou atualiza um amigo
     */
    suspend fun salvar(amigo: Amigo): Result<Unit> {
        return try {
            amigoDao.inserirOuAtualizar(amigo.toEntity())
            Timber.d("✅ Amigo salvo: ${amigo.nome}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao salvar amigo: ${amigo.nome}")
            Result.failure(e)
        }
    }
    
    /**
     * Deleta um amigo
     */
    suspend fun deletar(amigoId: String): Result<Unit> {
        return try {
            amigoDao.deletarPorId(amigoId)
            Timber.d("✅ Amigo deletado: $amigoId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao deletar amigo: $amigoId")
            Result.failure(e)
        }
    }
}

