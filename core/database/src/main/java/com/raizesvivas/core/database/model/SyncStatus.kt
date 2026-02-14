package com.raizesvivas.core.database.model

/**
 * Representa o estado de sincronização de uma entidade local com o backend (Firebase).
 */
enum class SyncStatus {
    /** Entidade sincronizada com sucesso. */
    SYNCHRONIZED,
    
    /** Alterações locais pendentes de envio. */
    PENDING,
    
    /** Erro ao tentar sincronizar. */
    ERROR
}
