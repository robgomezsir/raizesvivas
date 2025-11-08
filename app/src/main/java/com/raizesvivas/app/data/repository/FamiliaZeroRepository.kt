package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.FamiliaZero
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gerenciar a Família Zero
 */
@Singleton
class FamiliaZeroRepository @Inject constructor(
    private val firestoreService: FirestoreService
) {
    
    /**
     * Verifica se a Família Zero já foi criada
     */
    suspend fun existe(): Boolean {
        return firestoreService.familiaZeroExiste()
    }
    
    /**
     * Cria a Família Zero (apenas no primeiro acesso)
     */
    suspend fun criar(familiaZero: FamiliaZero): Result<Unit> {
        Timber.d("🌳 Criando Família Zero...")
        return firestoreService.criarFamiliaZero(familiaZero)
    }
    
    /**
     * Salva a Família Zero (cria ou atualiza)
     */
    suspend fun salvar(familiaZero: FamiliaZero): Result<Unit> {
        Timber.d("🌳 Salvando Família Zero...")
        // Se já existe, usar criarFamiliaZero que atualiza
        // Caso contrário, criar
        return firestoreService.criarFamiliaZero(familiaZero)
    }
    
    /**
     * Busca a Família Zero
     */
    suspend fun buscar(): FamiliaZero? {
        return firestoreService.buscarFamiliaZero().getOrNull()
    }
    
    /**
     * Observa a Família Zero em tempo real
     */
    fun observar(): Flow<FamiliaZero?> {
        return firestoreService.observarFamiliaZero()
    }
}

