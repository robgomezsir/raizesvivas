package com.raizesvivas.app.domain.model

import java.util.Date

/**
 * Modelo de domínio representando um usuário do app
 * 
 * Um usuário é alguém que tem login no app e pode estar
 * vinculado a uma pessoa específica na árvore genealógica.
 */
data class Usuario(
    val id: String = "",                  // Firebase Auth UID
    val nome: String = "",
    val email: String = "",
    val fotoUrl: String? = null,
    val posicaoRanking: Int? = null,
    
    // Vínculo com a árvore genealógica
    val pessoaVinculada: String? = null,  // ID da pessoa que representa este usuário
    
    // Permissões
    val ehAdministrador: Boolean = false,
    val ehAdministradorSenior: Boolean = false,  // Admin com poderes absolutos
    
    // Referência rápida à Família Zero
    val familiaZeroPai: String? = null,   // ID do patriarca
    val familiaZeroMae: String? = null,   // ID da matriarca
    
    // Flags de controle
    val primeiroAcesso: Boolean = true,   // True se ainda não completou onboarding
    
    // Metadados
    val criadoEm: Date = Date()
) {
    /**
     * Verifica se o usuário está vinculado a alguém na árvore
     */
    val estaVinculado: Boolean
        get() = pessoaVinculada != null
    
    /**
     * Verifica se a Família Zero já foi definida
     */
    val familiaZeroDefinida: Boolean
        get() = familiaZeroPai != null && familiaZeroMae != null
    
    /**
     * Retorna o nível de permissão do usuário
     */
    val nivelPermissao: NivelPermissao
        get() = when {
            ehAdministradorSenior -> NivelPermissao.FAMILIAR_ADMIN_SR
            ehAdministrador -> NivelPermissao.FAMILIAR_ADMIN
            else -> NivelPermissao.FAMILIAR
        }
    
    /**
     * Verifica se é apenas familiar (sem privilégios de admin)
     */
    val ehFamiliar: Boolean
        get() = !ehAdministrador && !ehAdministradorSenior
    
    /**
     * Verifica se é admin (admin comum ou sênior)
     */
    val ehAdmin: Boolean
        get() = ehAdministrador || ehAdministradorSenior
    
    /**
     * Verifica se é admin sênior (poderes absolutos)
     */
    val ehAdminSenior: Boolean
        get() = ehAdministradorSenior
    
    /**
     * Verifica se pode editar absolutamente (apenas admin sênior)
     */
    val podeEditarAbsolutamente: Boolean
        get() = ehAdministradorSenior
}

