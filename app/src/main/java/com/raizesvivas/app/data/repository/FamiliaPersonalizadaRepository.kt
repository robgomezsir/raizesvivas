package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.local.dao.FamiliaPersonalizadaDao
import com.raizesvivas.app.data.local.entities.FamiliaPersonalizadaEntity
import com.raizesvivas.app.data.local.entities.toDomain
import com.raizesvivas.app.data.local.entities.toEntity
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.FamiliaPersonalizada
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamiliaPersonalizadaRepository @Inject constructor(
    private val familiaPersonalizadaDao: FamiliaPersonalizadaDao,
    private val firestoreService: FirestoreService
) {

    fun observarTodas(): Flow<List<FamiliaPersonalizada>> =
        familiaPersonalizadaDao.observarTodas().map { entities ->
            entities.map(FamiliaPersonalizadaEntity::toDomain)
        }

    suspend fun buscarPorId(familiaId: String): FamiliaPersonalizada? {
        if (familiaId.isBlank()) return null
        val local = familiaPersonalizadaDao.buscarPorId(familiaId)?.toDomain()
        if (local != null) {
            return local
        }
        val remoto = firestoreService.buscarFamiliasPersonalizadas()
            .getOrNull()
            ?.firstOrNull { it.familiaId == familiaId }
        remoto?.let {
            familiaPersonalizadaDao.inserir(
                it.toEntity(sincronizadoEm = Date(), precisaSincronizar = false)
            )
        }
        return remoto
    }

    suspend fun salvar(familia: FamiliaPersonalizada): Result<Unit> {
        return try {
            val resultado = firestoreService.salvarFamiliaPersonalizada(familia)
            resultado.onSuccess {
                familiaPersonalizadaDao.inserir(
                    familia.toEntity(sincronizadoEm = Date(), precisaSincronizar = false)
                )
            }.onFailure { erro ->
                Timber.e(erro, "❌ Falha ao sincronizar família personalizada, armazenando offline")
                familiaPersonalizadaDao.inserir(
                    familia.toEntity(sincronizadoEm = null, precisaSincronizar = true)
                )
            }
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro inesperado ao salvar família personalizada")
            familiaPersonalizadaDao.inserir(
                familia.toEntity(sincronizadoEm = null, precisaSincronizar = true)
            )
            Result.failure(e)
        }
    }

    suspend fun sincronizar(): Result<Unit> {
        return try {
            val resultado = firestoreService.buscarFamiliasPersonalizadas()
            resultado.onSuccess { familias ->
                familiaPersonalizadaDao.deletarTodas()
                val entities = familias.map {
                    it.toEntity(sincronizadoEm = Date(), precisaSincronizar = false)
                }
                if (entities.isNotEmpty()) {
                    familiaPersonalizadaDao.inserirTodas(entities)
                }
                Timber.d("✅ Sincronizadas ${entities.size} famílias personalizadas")
            }.onFailure { erro ->
                Timber.e(erro, "❌ Erro ao sincronizar famílias personalizadas")
            }
            resultado.map { }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro inesperado ao sincronizar famílias personalizadas")
            Result.failure(e)
        }
    }
    
    suspend fun deletar(familiaId: String): Result<Unit> {
        return try {
            if (familiaId.isBlank()) {
                return Result.failure(IllegalArgumentException("familiaId não pode ser vazio"))
            }
            
            val resultado = firestoreService.deletarFamiliaPersonalizada(familiaId)
            resultado.onSuccess {
                // Remover do banco local também
                familiaPersonalizadaDao.deletarPorId(familiaId)
                Timber.d("✅ Família personalizada deletada localmente: $familiaId")
            }.onFailure { erro ->
                Timber.e(erro, "❌ Falha ao deletar família personalizada no Firestore")
                // Mesmo assim, tentar remover do banco local
                try {
                    familiaPersonalizadaDao.deletarPorId(familiaId)
                } catch (e: Exception) {
                    Timber.e(e, "❌ Erro ao deletar família personalizada do banco local")
                }
            }
            resultado
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro inesperado ao deletar família personalizada")
            Result.failure(e)
        }
    }
}

