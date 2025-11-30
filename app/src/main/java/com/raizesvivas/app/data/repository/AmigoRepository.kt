package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.local.dao.AmigoDao
import com.raizesvivas.app.data.local.entities.AmigoEntity
import com.raizesvivas.app.data.local.entities.toDomain
import com.raizesvivas.app.data.local.entities.toEntity
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.Amigo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
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
    private val amigoDao: AmigoDao,
    private val firestoreService: FirestoreService
) {
    
    /**
     * Observa todos os amigos diretamente do Firestore em tempo real
     * Sincroniza automaticamente com o cache local quando há mudanças
     * Garante que todos os usuários vejam todos os amigos cadastrados
     */
    fun observarTodosAmigos(): Flow<List<Amigo>> {
        return firestoreService.observarTodosAmigos()
            .onEach { amigos ->
                // Sincronizar com cache local em background
                try {
                    if (amigos.isNotEmpty()) {
                        val entities = amigos.map { it.toEntity() }
                        amigoDao.inserirOuAtualizarTodos(entities)
                        Timber.d("✅ ${amigos.size} amigos sincronizados no cache local")
                    } else {
                        Timber.d("📋 Nenhum amigo para sincronizar")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "❌ Erro ao sincronizar amigos no cache local")
                }
            }
            .catch { error ->
                Timber.e(error, "❌ Erro ao observar amigos do Firestore")
                // Em caso de erro, emitir lista vazia
                // O usuário pode tentar recarregar ou o erro será resolvido na próxima atualização
                emit(emptyList())
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
            // Salvar no Firestore primeiro
            val resultado = firestoreService.salvarAmigo(amigo)
            
            resultado.onSuccess {
                // Salvar no cache local
                amigoDao.inserirOuAtualizar(amigo.toEntity())
                Timber.d("✅ Amigo salvo: ${amigo.nome}")
            }
            
            resultado
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
            // Deletar do Firestore primeiro
            val resultado = firestoreService.deletarAmigo(amigoId)
            
            resultado.onSuccess {
                // Deletar do cache local
                amigoDao.deletarPorId(amigoId)
                Timber.d("✅ Amigo deletado: $amigoId")
            }
            
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao deletar amigo: $amigoId")
            Result.failure(e)
        }
    }
    
    /**
     * Sincroniza amigos do Firestore para o cache local
     */
    suspend fun sincronizar(): Result<Unit> {
        return try {
            Timber.d("🔄 Sincronizando amigos do Firestore...")
            
            val resultado = firestoreService.buscarTodosAmigos()
            
            resultado.onSuccess { amigos ->
                if (amigos.isNotEmpty()) {
                    // Converter para entities
                    val entities = amigos.map { it.toEntity() }
                    
                    // Atualizar cache local
                    // Nota: Não deletamos tudo antes para evitar perder dados não sincronizados
                    // Mas idealmente deveríamos ter uma estratégia de merge
                    amigoDao.inserirOuAtualizarTodos(entities)
                    
                    Timber.d("✅ ${amigos.size} amigos sincronizados do Firestore")
                } else {
                    Timber.d("✅ Nenhum amigo encontrado no Firestore")
                }
            }
            
            resultado.map { }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao sincronizar amigos")
            Result.failure(e)
        }
    }
}

