package com.raizesvivas.core.database.model

/**
 * Interface que define os metadados necessários para o motor de sincronização.
 */
interface SyncableEntity {
    val syncStatus: SyncStatus
    val lastModified: Long
}
