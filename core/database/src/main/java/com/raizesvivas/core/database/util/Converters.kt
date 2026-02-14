package com.raizesvivas.core.database.util

import androidx.room.TypeConverter
import com.raizesvivas.core.database.model.SyncStatus

/**
 * Conversores para persistir tipos complexos no SQLite através do Room.
 */
class Converters {
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String = status.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
}
