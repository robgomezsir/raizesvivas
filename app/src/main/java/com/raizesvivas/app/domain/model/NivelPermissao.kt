package com.raizesvivas.app.domain.model

/**
 * Níveis de permissão dos usuários no sistema
 */
enum class NivelPermissao(val descricao: String) {
    FAMILIAR("Familiar"),
    FAMILIAR_ADMIN("Familiar Admin"),
    FAMILIAR_ADMIN_SR("Familiar Admin Sênior")
}

