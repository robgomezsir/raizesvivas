package com.raizesvivas.core.domain.model

/**
 * Modelo de usuário do domínio
 * 
 * Representa um usuário autenticado no sistema Raízes Vivas.
 * Este modelo é usado em toda a aplicação para representar
 * informações do usuário logado.
 */
data class User(
    val id: String,
    val email: String,
    val name: String,
    val createdAt: String,
    val lastSignInAt: String
)
