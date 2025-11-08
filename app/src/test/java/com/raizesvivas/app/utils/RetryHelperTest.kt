package com.raizesvivas.app.utils

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Testes para RetryHelper
 */
class RetryHelperTest {
    
    @Test
    fun `retry - deve retentar em caso de falha`() = runTest {
        var attempts = 0
        
        val result = RetryHelper.withRetry(
            maxRetries = 3,
            initialDelayMs = 10 // Delay curto para testes
        ) {
            attempts++
            if (attempts < 3) {
                Result.failure<Unit>(Exception("Falha $attempts"))
            } else {
                Result.success(Unit)
            }
        }
        
        assertTrue(result.isSuccess)
        assertEquals(3, attempts)
    }
    
    @Test
    fun `retry - deve falhar após todas tentativas`() = runTest {
        var attempts = 0
        
        val result = RetryHelper.withRetry(
            maxRetries = 3,
            initialDelayMs = 10
        ) {
            attempts++
            Result.failure<Unit>(Exception("Falha permanente"))
        }
        
        assertFalse(result.isSuccess)
        assertEquals(3, attempts)
    }
    
    @Test
    fun `retry - deve suceder na primeira tentativa`() = runTest {
        var attempts = 0
        
        val result = RetryHelper.withRetry(
            maxRetries = 3,
            initialDelayMs = 10
        ) {
            attempts++
            Result.success(Unit)
        }
        
        assertTrue(result.isSuccess)
        assertEquals(1, attempts)
    }
}

