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
}

