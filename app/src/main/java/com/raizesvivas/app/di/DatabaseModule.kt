package com.raizesvivas.app.di

import android.content.Context
import androidx.room.Room
import com.raizesvivas.app.data.local.BiometricCrypto
import com.raizesvivas.app.data.local.BiometricPreferences
import com.raizesvivas.app.data.local.BiometricService
import com.raizesvivas.app.data.local.RaizesVivasDatabase
import com.raizesvivas.app.data.local.dao.PessoaDao
import com.raizesvivas.app.data.local.dao.SubfamiliaDao
import com.raizesvivas.app.data.local.dao.MembroFamiliaDao
import com.raizesvivas.app.data.local.dao.SugestaoSubfamiliaDao
import com.raizesvivas.app.data.local.dao.NotificacaoDao
import com.raizesvivas.app.data.local.dao.ConquistaDao
import com.raizesvivas.app.data.local.dao.PerfilGamificacaoDao
import com.raizesvivas.app.data.local.dao.UsuarioDao
import com.raizesvivas.app.data.local.migration.RoomMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para prover instâncias do Room Database
 * 
 * Este módulo cria e configura o banco de dados local.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Provê instância do Room Database
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): RaizesVivasDatabase {
        return Room.databaseBuilder(
            context,
            RaizesVivasDatabase::class.java,
            RaizesVivasDatabase.DATABASE_NAME
        )
            .addMigrations(*RoomMigrations.getAllMigrations())
            .fallbackToDestructiveMigration() // Fallback: recria DB se versão mudar sem migration
            .build()
    }
    
    /**
     * Provê DAO de Pessoa
     */
    @Provides
    @Singleton
    fun providePessoaDao(
        database: RaizesVivasDatabase
    ): PessoaDao {
        return database.pessoaDao()
    }
    
    /**
     * Provê DAO de Usuário
     */
    @Provides
    @Singleton
    fun provideUsuarioDao(
        database: RaizesVivasDatabase
    ): UsuarioDao {
        return database.usuarioDao()
    }
    
    /**
     * Prov DAO de Subfamlia
     */
    @Provides
    @Singleton
    fun provideSubfamiliaDao(
        database: RaizesVivasDatabase
    ): SubfamiliaDao {
        return database.subfamiliaDao()
    }
    
    /**
     * Prov DAO de Membro de Famlia
     */
    @Provides
    @Singleton
    fun provideMembroFamiliaDao(
        database: RaizesVivasDatabase
    ): MembroFamiliaDao {
        return database.membroFamiliaDao()
    }
    
    /**
     * Prov DAO de Sugesto de Subfamlia
     */
    @Provides
    @Singleton
    fun provideSugestaoSubfamiliaDao(
        database: RaizesVivasDatabase
    ): SugestaoSubfamiliaDao {
        return database.sugestaoSubfamiliaDao()
    }

    /**
     * Provê DAO de Notificação
     */
    @Provides
    @Singleton
    fun provideNotificacaoDao(
        database: RaizesVivasDatabase
    ): NotificacaoDao {
        return database.notificacaoDao()
    }

    /**
     * Provê DAO de Conquista
     */
    @Provides
    @Singleton
    fun provideConquistaDao(
        database: RaizesVivasDatabase
    ): ConquistaDao {
        return database.conquistaDao()
    }

    /**
     * Provê DAO de Perfil de Gamificação
     */
    @Provides
    @Singleton
    fun providePerfilGamificacaoDao(
        database: RaizesVivasDatabase
    ): PerfilGamificacaoDao {
        return database.perfilGamificacaoDao()
    }
    
    /**
     * Provê instância do BiometricService
     */
    @Provides
    @Singleton
    fun provideBiometricService(
        @ApplicationContext context: Context
    ): BiometricService {
        return BiometricService(context)
    }
    
    /**
     * Provê instância do BiometricPreferences
     */
    @Provides
    @Singleton
    fun provideBiometricPreferences(
        @ApplicationContext context: Context
    ): BiometricPreferences {
        return BiometricPreferences(context)
    }
    
    /**
     * Provê instância do BiometricCrypto
     */
    @Provides
    @Singleton
    fun provideBiometricCrypto(
        @ApplicationContext context: Context
    ): BiometricCrypto {
        return BiometricCrypto(context)
    }
}

