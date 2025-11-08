package com.raizesvivas.app.di

import android.content.Context
import com.raizesvivas.app.utils.NetworkUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para dependências de rede
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideNetworkUtils(
        @ApplicationContext context: Context
    ): NetworkUtils {
        return NetworkUtils(context)
    }
}

