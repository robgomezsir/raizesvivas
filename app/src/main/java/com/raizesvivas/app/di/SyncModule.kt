package com.raizesvivas.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Módulo Hilt para dependências de sincronização
 * 
 * SyncManager e SyncRepository são automaticamente injetados pelo Hilt
 * através de @Inject nos construtores. Este módulo está reservado
 * para futuras configurações se necessário.
 */
@Module
@InstallIn(SingletonComponent::class)
object SyncModule {
    // SyncManager e SyncRepository são injetados automaticamente
    // Se precisar de configuração adicional, adicione aqui
}

