package com.raizesvivas.app.utils

/**
 * Sealed class representando erros da aplicação
 * Usado para padronizar tratamento de erros em toda a aplicação
 */
sealed class AppError(
    val message: String,
    val cause: Throwable? = null
) {
    /**
     * Erro de rede (conexão, timeout, etc.)
     */
    data class NetworkError(
        val errorMessage: String,
        val errorCause: Throwable? = null
    ) : AppError(errorMessage, errorCause)
    
    /**
     * Erro de autenticação (login, permissões, etc.)
     */
    data class AuthError(
        val errorMessage: String,
        val errorCause: Throwable? = null
    ) : AppError(errorMessage, errorCause)
    
    /**
     * Erro de validação (dados inválidos)
     */
    data class ValidationError(
        val errorMessage: String,
        val errorCause: Throwable? = null
    ) : AppError(errorMessage, errorCause)
    
    /**
     * Erro de permissão (usuário não tem permissão para ação)
     */
    data class PermissionError(
        val errorMessage: String,
        val errorCause: Throwable? = null
    ) : AppError(errorMessage, errorCause)
    
    /**
     * Erro desconhecido ou não categorizado
     */
    data class UnknownError(
        val errorMessage: String,
        val errorCause: Throwable? = null
    ) : AppError(errorMessage, errorCause)
}

/**
 * Extensão para converter Exception em AppError
 */
fun Exception.toAppError(): AppError {
    return when (this) {
        is java.net.UnknownHostException,
        is java.net.SocketTimeoutException,
        is java.net.ConnectException -> {
            AppError.NetworkError(
                errorMessage = "Erro de conexão. Verifique sua internet e tente novamente.",
                errorCause = this
            )
        }
        is com.google.firebase.auth.FirebaseAuthException -> {
            AppError.AuthError(
                errorMessage = when {
                    message?.contains("wrong-password") == true -> "Senha incorreta"
                    message?.contains("user-not-found") == true -> "Usuário não encontrado"
                    message?.contains("email-already-in-use") == true -> "Este email já está cadastrado"
                    message?.contains("invalid-email") == true -> "Email inválido"
                    message?.contains("weak-password") == true -> "Senha muito fraca. Use pelo menos 6 caracteres"
                    message?.contains("network-request-failed") == true -> "Erro de conexão. Verifique sua internet"
                    else -> message ?: "Erro de autenticação"
                },
                errorCause = this
            )
        }
        is com.google.firebase.firestore.FirebaseFirestoreException -> {
            when (code) {
                com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    AppError.PermissionError(
                        errorMessage = "Você não tem permissão para realizar esta ação.",
                        errorCause = this
                    )
                }
                com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE -> {
                    AppError.NetworkError(
                        errorMessage = "Serviço temporariamente indisponível. Tente novamente.",
                        errorCause = this
                    )
                }
                com.google.firebase.firestore.FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> {
                    AppError.NetworkError(
                        errorMessage = "Tempo de espera esgotado. Verifique sua conexão e tente novamente.",
                        errorCause = this
                    )
                }
                else -> {
                    AppError.UnknownError(
                        errorMessage = message ?: "Erro ao acessar dados",
                        errorCause = this
                    )
                }
            }
        }
        is IllegalArgumentException -> {
            AppError.ValidationError(
                errorMessage = message ?: "Dados inválidos",
                errorCause = this
            )
        }
        else -> {
            AppError.UnknownError(
                errorMessage = message ?: "Erro desconhecido",
                errorCause = this
            )
        }
    }
}

