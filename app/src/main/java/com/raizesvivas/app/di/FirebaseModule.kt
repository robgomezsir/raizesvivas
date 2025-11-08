package com.raizesvivas.app.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
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
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance().apply {
            // Configurações de performance
            // Nota: setPersistenceEnabled e setCacheSizeBytes são deprecated no Firestore
            // A persistência é habilitada por padrão e o tamanho do cache é gerenciado automaticamente
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .build()
        }
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
}

