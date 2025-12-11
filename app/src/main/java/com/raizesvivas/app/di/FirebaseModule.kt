package com.raizesvivas.app.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.raizesvivas.app.data.remote.firebase.StorageService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para prover instâncias do Firebase
 * 
 * Este módulo centraliza a configuração de todas as
 * instâncias do Firebase usadas no app.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    
    /**
     * Provê instância do Firebase Authentication
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance().apply {
            // Configurar idioma para PT-BR
            setLanguageCode("pt-BR")
        }
    }
    
    /**
     * Provê instância do Cloud Firestore
     * 
     * Nota: A persistência offline é habilitada por padrão nas versões recentes do Firestore.
     * Os métodos setPersistenceEnabled() e setCacheSizeBytes() foram deprecated porque
     * a persistência offline agora é automática e não precisa ser configurada explicitamente.
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    
    /**
     * Provê instância do Firebase Storage
     */
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance().apply {
            maxUploadRetryTimeMillis = 60000 // 60 segundos
        }
    }
    
    /**
     * Provê instância do StorageService
     */
    @Provides
    @Singleton
    fun provideStorageService(storage: FirebaseStorage): StorageService {
        return StorageService(storage)
    }
    
    /**
     * Provê instância do Firebase Cloud Messaging
     */
    @Provides
    @Singleton
    fun provideFirebaseMessaging(): com.google.firebase.messaging.FirebaseMessaging {
        return com.google.firebase.messaging.FirebaseMessaging.getInstance()
    }
}

