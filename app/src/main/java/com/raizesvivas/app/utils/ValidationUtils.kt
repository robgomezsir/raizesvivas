package com.raizesvivas.app.utils

/**
 * Utilitários para validação de dados
 */
object ValidationUtils {
    
    /**
     * Valida formato de email
     */
    fun validarEmail(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(false, "Email é obrigatório")
        }
        
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        
        return if (email.matches(emailRegex)) {
            ValidationResult(true)
        } else {
            ValidationResult(false, "Email inválido")
        }
    }
    
    /**
     * Valida senha (mínimo 8 caracteres)
     */
    fun validarSenha(senha: String): ValidationResult {
        return when {
            senha.isBlank() -> ValidationResult(false, "Senha é obrigatória")
            senha.length < 8 -> ValidationResult(false, "Senha deve ter no mínimo 8 caracteres")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Valida nome (não vazio)
     */
    fun validarNome(nome: String): ValidationResult {
        return when {
            nome.isBlank() -> ValidationResult(false, "Nome é obrigatório")
            nome.length < 3 -> ValidationResult(false, "Nome deve ter no mínimo 3 caracteres")
            else -> ValidationResult(true)
        }
    }
}

/**
 * Resultado de uma validação
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

