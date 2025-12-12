package com.raizesvivas.app.utils

import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import org.junit.Assert.*
import org.junit.Test
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Testes unitários para ErrorHandler
 */
class ErrorHandlerTest {
    
    @Test
    fun `handle network error - UnknownHostException`() {
        val exception = UnknownHostException("Host não encontrado")
        val appError = ErrorHandler.handle(exception)
        
        assertTrue(appError is AppError.NetworkError)
        assertTrue(appError.message.contains("conexão"))
    }
    
    @Test
    fun `handle network error - SocketTimeoutException`() {
        val exception = SocketTimeoutException("Timeout")
        val appError = ErrorHandler.handle(exception)
        
        assertTrue(appError is AppError.NetworkError)
        assertTrue(appError.message.contains("conexão"))
    }
    
    @Test
    fun `handle network error - ConnectException`() {
        val exception = ConnectException("Conexão recusada")
        val appError = ErrorHandler.handle(exception)
        
        assertTrue(appError is AppError.NetworkError)
        assertTrue(appError.message.contains("conexão"))
    }
    
    @Test
    fun `handle validation error - IllegalArgumentException`() {
        val exception = IllegalArgumentException("Dados inválidos")
        val appError = ErrorHandler.handle(exception)
        
        assertTrue(appError is AppError.ValidationError)
        assertEquals("Dados inválidos", appError.message)
    }
    
    @Test
    fun `handle unknown error - generic Exception`() {
        val exception = Exception("Erro genérico")
        val appError = ErrorHandler.handle(exception)
        
        assertTrue(appError is AppError.UnknownError)
        assertEquals("Erro genérico", appError.message)
    }
    
    @Test
    fun `isRecoverable - NetworkError should be recoverable`() {
        val error = AppError.NetworkError("Erro de rede")
        assertTrue(ErrorHandler.isRecoverable(error))
    }
    
    @Test
    fun `isRecoverable - AuthError should not be recoverable`() {
        val error = AppError.AuthError("Erro de autenticação")
        assertFalse(ErrorHandler.isRecoverable(error))
    }
    
    @Test
    fun `requiresUserAction - AuthError should require user action`() {
        val error = AppError.AuthError("Erro de autenticação")
        assertTrue(ErrorHandler.requiresUserAction(error))
    }
    
    @Test
    fun `requiresUserAction - NetworkError should not require user action`() {
        val error = AppError.NetworkError("Erro de rede")
        assertFalse(ErrorHandler.requiresUserAction(error))
    }
}

