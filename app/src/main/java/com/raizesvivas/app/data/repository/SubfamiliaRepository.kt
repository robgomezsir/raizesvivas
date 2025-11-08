package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.local.dao.SubfamiliaDao
import com.raizesvivas.app.data.local.dao.MembroFamiliaDao
import com.raizesvivas.app.data.local.dao.SugestaoSubfamiliaDao
import com.raizesvivas.app.data.local.entities.*
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.Subfamilia
import com.raizesvivas.app.domain.model.MembroFamilia
import com.raizesvivas.app.domain.model.SugestaoSubfamilia
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gerenciar subfamílias
 * 
 * Este repository implementa o padrão Repository, coordenando:
 * - Dados locais (Room) para cache e modo offline
 * - Dados remotos (Firestore) como fonte da verdade
 */
@Singleton
class SubfamiliaRepository @Inject constructor(
    private val subfamiliaDao: SubfamiliaDao,
    private val membroFamiliaDao: MembroFamiliaDao,
    private val sugestaoSubfamiliaDao: SugestaoSubfamiliaDao,
    private val firestoreService: FirestoreService
) {
    
    // ============================================
    // SUBFAMÍLIAS
    // ============================================
    
    /**
     * Observa todas as subfamílias (do cache local)
     */
    fun observarTodasSubfamilias(): Flow<List<Subfamilia>> {
        return subfamiliaDao.observarTodasSubfamilias()
            .map { entities -> 
                Timber.d("🌳 Observando subfamílias: ${entities.size} no cache local")
                entities.map { it.toDomain() }
            }
    }
    
    /**
     * Busca subfamília por ID (cache local primeiro)
     */
    suspend fun buscarPorId(subfamiliaId: String): Subfamilia? {
        if (subfamiliaId.isBlank()) {
            Timber.w("⚠️ Tentativa de buscar subfamília com ID vazio")
            return null
        }
        
        val local = subfamiliaDao.buscarPorId(subfamiliaId)?.toDomain()
        
        if (local == null) {
            val remoto = firestoreService.buscarSubfamilia(subfamiliaId).getOrNull()
            remoto?.let {
                subfamiliaDao.inserir(it.toEntity())
            }
            return remoto
        }
        
        return local
    }
    
    /**
     * Salva subfamília (local e remoto)
     */
    suspend fun salvar(subfamilia: Subfamilia): Result<Unit> {
        return try {
            // Salvar no Firestore primeiro
            val resultado = firestoreService.salvarSubfamilia(subfamilia)
            
            resultado.onSuccess {
                // Se sucesso no Firestore, salvar no cache local
                subfamiliaDao.inserir(subfamilia.toEntity())
                Timber.d("✅ Subfamília salva: ${subfamilia.id}")
            }.onFailure { erro ->
                Timber.e(erro, "❌ Erro ao salvar subfamília no Firestore")
                // Ainda assim, salvar localmente marcando para sincronizar depois
                val entity = subfamilia.toEntity().copy(precisaSincronizar = true)
                subfamiliaDao.inserir(entity)
            }
            
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao salvar subfamília")
            Result.failure(e)
        }
    }
    
    /**
     * Deleta subfamília
     */
    suspend fun deletar(subfamiliaId: String): Result<Unit> {
        return try {
            val resultado = firestoreService.deletarSubfamilia(subfamiliaId)
            
            resultado.onSuccess {
                subfamiliaDao.deletarPorId(subfamiliaId)
                // Também remover todos os membros dessa família
                membroFamiliaDao.removerTodosMembros(subfamiliaId)
                Timber.d("✅ Subfamília deletada: $subfamiliaId")
            }
            
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao deletar subfamília")
            Result.failure(e)
        }
    }
    
    /**
     * Sincroniza subfamílias do Firestore para o cache local
     */
    suspend fun sincronizarDoFirestore(): Result<Unit> {
        return try {
            Timber.d("🔄 Sincronizando subfamílias do Firestore...")
            
            val resultado = firestoreService.buscarTodasSubfamilias()
            
            resultado.onSuccess { subfamilias ->
                Timber.d("✅ Recebidas ${subfamilias.size} subfamílias do Firestore")
                
                val entities = subfamilias.map { it.toEntity() }
                subfamiliaDao.inserirTodas(entities)
                
                Timber.d("✅ ${entities.size} subfamílias salvas no cache local")
            }
            
            resultado.map { Unit }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao sincronizar subfamílias")
            Result.failure(e)
        }
    }
    
    // ============================================
    // MEMBROS DE FAMÍLIAS
    // ============================================
    
    /**
     * Observa membros de uma família
     */
    fun observarMembrosPorFamilia(familiaId: String): Flow<List<MembroFamilia>> {
        return membroFamiliaDao.observarMembrosPorFamilia(familiaId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    /**
     * Adiciona membro a uma família
     */
    suspend fun adicionarMembroAFamilia(membroFamilia: MembroFamilia): Result<Unit> {
        return try {
            val resultado = firestoreService.salvarMembroFamilia(membroFamilia)
            
            resultado.onSuccess {
                membroFamiliaDao.inserir(membroFamilia.toEntity())
                Timber.d("✅ Membro adicionado à família: ${membroFamilia.membroId} -> ${membroFamilia.familiaId}")
            }
            
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao adicionar membro à família")
            Result.failure(e)
        }
    }
    
    /**
     * Remove membro de uma família
     */
    suspend fun removerMembroDeFamilia(membroId: String, familiaId: String): Result<Unit> {
        return try {
            val resultado = firestoreService.deletarMembroFamilia(membroId, familiaId)
            
            resultado.onSuccess {
                membroFamiliaDao.removerMembroDeFamilia(membroId, familiaId)
                Timber.d("✅ Membro removido da família: $membroId -> $familiaId")
            }
            
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao remover membro da família")
            Result.failure(e)
        }
    }
    
    // ============================================
    // SUGESTÕES DE SUBFAMÍLIAS
    // ============================================
    
    /**
     * Observa sugestões pendentes
     */
    fun observarSugestoesPendentes(): Flow<List<SugestaoSubfamilia>> {
        return sugestaoSubfamiliaDao.observarSugestoesPendentes()
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    /**
     * Salva sugestão (local e remoto)
     */
    suspend fun salvarSugestao(sugestao: SugestaoSubfamilia): Result<Unit> {
        return try {
            val resultado = firestoreService.salvarSugestaoSubfamilia(sugestao)
            
            resultado.onSuccess {
                sugestaoSubfamiliaDao.inserir(sugestao.toEntity())
                Timber.d("✅ Sugestão salva: ${sugestao.id}")
            }
            
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao salvar sugestão")
            Result.failure(e)
        }
    }
    
    /**
     * Atualiza status de uma sugestão
     */
    suspend fun atualizarStatusSugestao(
        sugestaoId: String,
        status: com.raizesvivas.app.domain.model.StatusSugestao
    ): Result<Unit> {
        return try {
            val resultado = firestoreService.atualizarStatusSugestao(sugestaoId, status)
            
            resultado.onSuccess {
                sugestaoSubfamiliaDao.atualizarStatus(
                    sugestaoId,
                    status,
                    System.currentTimeMillis()
                )
                Timber.d("✅ Status da sugestão atualizado: $sugestaoId -> $status")
            }
            
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao atualizar status da sugestão")
            Result.failure(e)
        }
    }
}
