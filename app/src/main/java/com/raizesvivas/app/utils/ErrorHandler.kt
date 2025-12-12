package com.raizesvivas.app.utils

import timber.log.Timber

/**
 * Handler centralizado para tratamento de erros
 * 
 * Fornece métodos estáticos para mapear exceções em mensagens amigáveis
 * e AppError padronizado
 */
object ErrorHandler {
    
    /**
     * Converte uma Exception em AppError
     * 
     * @param exception Exceção a ser convertida
     * @return AppError correspondente
     */
    fun handle(exception: Exception): AppError {
        Timber.e(exception, "Erro capturado: ${exception.message}")
        return exception.toAppError()
    }
    
    /**
     * Obtém mensagem amigável de uma Exception
     * 
     * @param exception Exceção
     * @return Mensagem amigável para o usuário
     */
    fun getMessage(exception: Exception): String {
        return handle(exception).message
    }
    
    /**
     * Obtém mensagem amigável de um AppError
     * 
     * @param error AppError
     * @return Mensagem amigável para o usuário
     */
    fun getMessage(error: AppError): String {
        return error.message
    }
    
    /**
     * Verifica se o erro é recuperável (pode tentar novamente)
     * 
     * @param error AppError
     * @return true se o erro é recuperável
     */
    fun isRecoverable(error: AppError): Boolean {
        return when (error) {
            is AppError.NetworkError -> true
            is AppError.AuthError -> false
            is AppError.ValidationError -> false
            is AppError.PermissionError -> false
            is AppError.UnknownError -> true
        }
    }
    
    /**
     * Verifica se o erro requer ação do usuário
     * 
     * @param error AppError
     * @return true se o erro requer ação do usuário
     */
    fun requiresUserAction(error: AppError): Boolean {
        return when (error) {
            is AppError.AuthError -> true
            is AppError.PermissionError -> true
            is AppError.ValidationError -> true
            is AppError.NetworkError -> false
            is AppError.UnknownError -> false
        }
    }
}

