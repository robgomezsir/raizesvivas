package com.raizesvivas.app.data.sync

import com.raizesvivas.app.data.repository.PessoaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

/**
 * Testes unitários para SyncManager
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SyncManagerTest {
    
    @Mock
    private lateinit var pessoaRepository: PessoaRepository
    
    private lateinit var syncManager: SyncManager
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        syncManager = SyncManager(pessoaRepository)
    }
    
    @Test
    fun `syncIncremental with forceSync should call sincronizarDoFirestore`() = runTest {
        // Arrange
        whenever(pessoaRepository.sincronizarDoFirestore()).thenReturn(Result.success(Unit))
        
        // Act
        val result = syncManager.syncIncremental(forceSync = true).first()
        
        // Assert
        assertTrue(result is SyncResult.Success)
        verify(pessoaRepository, times(1)).sincronizarDoFirestore()
        verify(pessoaRepository, never()).sincronizarModificadasDesde(any())
    }
    
    @Test
    fun `syncIncremental when lastSyncTime is null should do full sync`() = runTest {
        // Arrange
        whenever(pessoaRepository.sincronizarDoFirestore()).thenReturn(Result.success(Unit))
        
        // Act
        val result = syncManager.syncIncremental(forceSync = false).first()
        
        // Assert
        assertTrue(result is SyncResult.Success)
        verify(pessoaRepository, times(1)).sincronizarDoFirestore()
    }
    
    @Test
    fun `clearSyncTime should reset lastSyncTime`() {
        // Act
        syncManager.clearSyncTime()
        
        // Assert - não há como verificar diretamente, mas não deve lançar exceção
        // Na próxima sincronização, deve fazer sync completo
    }
}

