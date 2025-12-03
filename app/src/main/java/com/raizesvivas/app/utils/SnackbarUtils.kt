package com.raizesvivas.app.utils

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Utilitários para exibir Snackbars de forma consistente
 */
object SnackbarUtils {
    
    /**
     * Exibe uma mensagem de erro
     */
    suspend fun showError(
        snackbarHostState: SnackbarHostState,
        message: String,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ): SnackbarResult {
        Timber.e("❌ Erro exibido ao usuário: $message")
        return snackbarHostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = androidx.compose.material3.SnackbarDuration.Long
        ).also {
            if (it == SnackbarResult.ActionPerformed && onAction != null) {
                onAction()
            }
        }
    }
    
    /**
     * Exibe uma mensagem de sucesso
     */
    suspend fun showSuccess(
        snackbarHostState: SnackbarHostState,
        message: String
    ) {
        Timber.d("✅ Sucesso exibido ao usuário: $message")
        snackbarHostState.showSnackbar(
            message = message,
            duration = androidx.compose.material3.SnackbarDuration.Short
        )
    }
    
    /**
     * Exibe uma mensagem informativa
     */
    suspend fun showInfo(
        snackbarHostState: SnackbarHostState,
        message: String
    ) {
        Timber.d("ℹ️ Info exibido ao usuário: $message")
        snackbarHostState.showSnackbar(
            message = message,
            duration = androidx.compose.material3.SnackbarDuration.Short
        )
    }
    
    /**
     * Exibe mensagem de erro em um CoroutineScope
     */
    fun showErrorAsync(
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        message: String
    ) {
        scope.launch {
            showError(snackbarHostState, message)
        }
    }
    
    /**
     * Exibe mensagem de sucesso em um CoroutineScope
     */
    fun showSuccessAsync(
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        message: String
    ) {
        scope.launch {
            showSuccess(snackbarHostState, message)
        }
    }
    
    /**
     * Exibe um AppError como snackbar
     * 
     * @param snackbarHostState Estado do SnackbarHost
     * @param error AppError a ser exibido
     * @param onRetry Callback opcional para ação "Tentar novamente" (se erro for recuperável)
     */
    suspend fun showError(
        snackbarHostState: SnackbarHostState,
        error: AppError,
        onRetry: (() -> Unit)? = null
    ): SnackbarResult {
        val isRecoverable = ErrorHandler.isRecoverable(error)
        val actionLabel = if (isRecoverable && onRetry != null) "Tentar novamente" else null
        
        return showError(
            snackbarHostState = snackbarHostState,
            message = error.message,
            actionLabel = actionLabel,
            onAction = if (isRecoverable) onRetry else null
        )
    }
    
    /**
     * Exibe um AppError em um CoroutineScope
     */
    fun showErrorAsync(
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        error: AppError,
        onRetry: (() -> Unit)? = null
    ) {
        scope.launch {
            showError(snackbarHostState, error, onRetry)
        }
    }
    
    /**
     * Exibe uma Exception como snackbar (converte para AppError automaticamente)
     */
    suspend fun showError(
        snackbarHostState: SnackbarHostState,
        exception: Exception,
        onRetry: (() -> Unit)? = null
    ): SnackbarResult {
        val error = ErrorHandler.handle(exception)
        return showError(snackbarHostState, error, onRetry)
    }
    
    /**
     * Exibe uma Exception em um CoroutineScope
     */
    fun showErrorAsync(
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        exception: Exception,
        onRetry: (() -> Unit)? = null
    ) {
        scope.launch {
            showError(snackbarHostState, exception, onRetry)
        }
    }
}

