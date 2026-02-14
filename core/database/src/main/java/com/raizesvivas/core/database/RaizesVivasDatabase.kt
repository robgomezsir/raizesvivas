package com.raizesvivas.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.raizesvivas.core.database.util.Converters

// Nota: Adicionaremos as entidades conforme elas forem criadas.
// Por enquanto, criamos a estrutura base.

@Database(
    entities = [], // Adicionar entidades aqui
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class RaizesVivasDatabase : RoomDatabase() {
    // DAOs serão declarados aqui
}
