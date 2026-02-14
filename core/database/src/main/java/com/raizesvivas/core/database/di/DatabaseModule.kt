package com.raizesvivas.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.raizesvivas.core.database.RaizesVivasDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideRaizesVivasDatabase(
        @ApplicationContext context: Context
    ): RaizesVivasDatabase {
        return Room.databaseBuilder(
            context,
            RaizesVivasDatabase::class.java,
            "raizes_vivas_db"
        )
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        .build()
    }
}
