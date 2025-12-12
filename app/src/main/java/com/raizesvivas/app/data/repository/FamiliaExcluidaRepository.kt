package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.local.dao.FamiliaExcluidaDao
import com.raizesvivas.app.data.local.entities.toEntity
import com.raizesvivas.app.data.local.entities.toDomain
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.FamiliaExcluida
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gerenciar famílias excluídas (blacklist)
 * Coordena persistência local (Room) e remota (Firestore)
 */
@Singleton
class FamiliaExcluidaRepository @Inject constructor(
    private val familiaExcluidaDao: FamiliaExcluidaDao,
    private val firestoreService: FirestoreService
) {
    
    /**
     * Observa todas as famílias excluídas
     */
    fun observarTodas(): Flow<List<FamiliaExcluida>> =
        familiaExcluidaDao.observarTodas().map { entities ->
            entities.map { it.toDomain() }
        }
    
    /**
     * Busca uma família excluída por ID
     */
    suspend fun buscarPorId(familiaId: String): FamiliaExcluida? {
        if (familiaId.isBlank()) return null
        return familiaExcluidaDao.buscarPorId(familiaId)?.toDomain()
    }
    
    /**
     * Adiciona uma família à blacklist
     */
    suspend fun salvar(familiaExcluida: FamiliaExcluida): Result<Unit> {
        return try {
            // Salvar no Firestore primeiro
            val resultado = firestoreService.salvarFamiliaExcluida(familiaExcluida)
            
            resultado.onSuccess {
                // Salvar localmente
                familiaExcluidaDao.inserir(
                    familiaExcluida.toEntity(sincronizadoEm = Date(), precisaSincronizar = false)
                )
                Timber.d("✅ Família adicionada à blacklist: ${familiaExcluida.familiaId}")
            }.onFailure { erro ->
                Timber.e(erro, "❌ Falha ao sincronizar família excluída, armazenando offline")
                // Salvar localmente mesmo com falha, marcar para sincronizar depois
                familiaExcluidaDao.inserir(
                    familiaExcluida.toEntity(sincronizadoEm = null, precisaSincronizar = true)
                )
            }
            
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro inesperado ao salvar família excluída")
            // Tentar salvar localmente
            familiaExcluidaDao.inserir(
                familiaExcluida.toEntity(sincronizadoEm = null, precisaSincronizar = true)
            )
            Result.failure(e)
        }
    }
    
    /**
     * Remove uma família da blacklist (restaurar)
     */
    suspend fun deletar(familiaId: String): Result<Unit> {
        return try {
            if (familiaId.isBlank()) {
                return Result.failure(IllegalArgumentException("familiaId não pode ser vazio"))
            }
            
            // Remover do Firestore primeiro
            val resultado = firestoreService.removerFamiliaExcluida(familiaId)
            
            resultado.onSuccess {
                // Remover do banco local
                familiaExcluidaDao.deletarPorId(familiaId)
                Timber.d("✅ Família removida da blacklist: $familiaId")
            }.onFailure { erro ->
                Timber.e(erro, "❌ Falha ao remover família excluída do Firestore")
                // Mesmo assim, tentar remover do banco local
                try {
                    familiaExcluidaDao.deletarPorId(familiaId)
                } catch (e: Exception) {
                    Timber.e(e, "❌ Erro ao remover família excluída do banco local")
                }
            }
            
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro inesperado ao deletar família excluída")
            Result.failure(e)
        }
    }
    
    /**
     * Sincroniza famílias excluídas do Firestore
     */
    suspend fun sincronizar(): Result<Unit> {
        return try {
            // 0. Tentar enviar itens pendentes primeiro
            val pendentesParaEnvio = familiaExcluidaDao.buscarPendenteSincronizacao()
            if (pendentesParaEnvio.isNotEmpty()) {
                Timber.d("📤 Tentando enviar ${pendentesParaEnvio.size} itens pendentes para o Firestore")
                pendentesParaEnvio.forEach { entity ->
                    try {
                        val domain = entity.toDomain()
                        val resultadoEnvio = firestoreService.salvarFamiliaExcluida(domain)
                        if (resultadoEnvio.isSuccess) {
                            // Atualizar localmente como sincronizado
                            familiaExcluidaDao.inserir(
                                entity.copy(sincronizadoEm = Date().time, precisaSincronizar = false)
                            )
                            Timber.d("✅ Item pendente enviado com sucesso: ${entity.familiaId}")
                        } else {
                            Timber.w("⚠️ Falha ao enviar item pendente: ${entity.familiaId}")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Erro ao processar item pendente: ${entity.familiaId}")
                    }
                }
            }

            val resultado = firestoreService.buscarFamiliasExcluidas()
            
            resultado.onSuccess { familiasExcluidas ->
                // 1. Salvar itens pendentes em memória antes de limpar
                val pendentes = familiaExcluidaDao.buscarPendenteSincronizacao()
                
                // 2. Limpar banco local
                familiaExcluidaDao.deletarTodas()
                
                // 3. Preparar lista combinada
                val entities = familiasExcluidas.map {
                    it.toEntity(sincronizadoEm = Date(), precisaSincronizar = false)
                }.toMutableList()
                
                // 4. Re-inserir itens pendentes (prioridade sobre o servidor)
                // Remover duplicatas da lista do servidor se já estiverem nos pendentes
                val pendentesIds = pendentes.map { it.familiaId }.toSet()
                entities.removeAll { it.familiaId in pendentesIds }
                
                // Adicionar pendentes
                entities.addAll(pendentes)
                
                if (entities.isNotEmpty()) {
                    familiaExcluidaDao.inserirTodas(entities)
                }
                
                Timber.d("✅ Sincronizadas ${entities.size} famílias excluídas (incluindo ${pendentes.size} pendentes)")
            }.onFailure { erro ->
                Timber.e(erro, "❌ Erro ao sincronizar famílias excluídas")
            }
            
            resultado.map { }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro inesperado ao sincronizar famílias excluídas")
            Result.failure(e)
        }
    }
}
