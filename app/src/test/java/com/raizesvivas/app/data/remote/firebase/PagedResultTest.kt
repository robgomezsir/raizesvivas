package com.raizesvivas.app.data.remote.firebase

import com.google.firebase.firestore.DocumentSnapshot
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock

/**
 * Testes unitários para PagedResult
 */
class PagedResultTest {
    
    @Mock
    private lateinit var mockDocument: DocumentSnapshot
    
    @Test
    fun `PagedResult empty should return empty result`() {
        val result = PagedResult.empty<String>()
        
        assertTrue(result.data.isEmpty())
        assertFalse(result.hasMore)
        assertNull(result.lastDocument)
    }
    
    @Test
    fun `PagedResult with data should contain correct values`() {
        val data = listOf("item1", "item2", "item3")
        val result = PagedResult(
            data = data,
            hasMore = true,
            lastDocument = mockDocument
        )
        
        assertEquals(3, result.data.size)
        assertTrue(result.hasMore)
        assertNotNull(result.lastDocument)
    }
    
    @Test
    fun `PagedResult with hasMore false should indicate no more pages`() {
        val data = listOf("item1")
        val result = PagedResult(
            data = data,
            hasMore = false,
            lastDocument = null
        )
        
        assertEquals(1, result.data.size)
        assertFalse(result.hasMore)
        assertNull(result.lastDocument)
    }
}

