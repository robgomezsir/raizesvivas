package com.raizesvivas.app.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Inicializa e gerencia jobs periódicos usando WorkManager
 */
@Singleton
class WorkManagerInitializer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val SINCRONIZACAO_RELACOES_WORK_NAME = "sincronizacao_relacoes_periodica"
        private const val VERIFICAR_ANIVERSARIOS_WORK_NAME = "verificar_aniversarios_periodico"
    }
    
    /**
     * Agenda a sincronização periódica de relações familiares
     * Executa diariamente, preferencialmente quando há conexão com internet
     */
    fun agendarSincronizacaoRelacoes() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<SincronizacaoRelacoesWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = 1,
            flexTimeIntervalUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag("sincronizacao")
            .addTag("relacoes_familiares")
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SINCRONIZACAO_RELACOES_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        
        Timber.d("✅ Sincronização periódica de relações agendada (executa a cada 24 horas)")
    }
    
    /**
     * Cancela a sincronização periódica
     */
    fun cancelarSincronizacaoRelacoes() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(SINCRONIZACAO_RELACOES_WORK_NAME)
        Timber.d("❌ Sincronização periódica de relações cancelada")
    }
    
    /**
     * Executa sincronização imediatamente (one-time work)
     */
    fun executarSincronizacaoImediata() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val workRequest = androidx.work.OneTimeWorkRequestBuilder<SincronizacaoRelacoesWorker>()
            .setConstraints(constraints)
            .addTag("sincronizacao")
            .addTag("relacoes_familiares")
            .addTag("imediata")
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
        Timber.d("✅ Sincronização imediata de relações agendada")
    }
    
    /**
     * Agenda a verificação periódica de aniversários
     * Executa diariamente às 8h da manhã
     */
    fun agendarVerificacaoAniversarios() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        
        // Executar diariamente, com flexibilidade de 1 hora
        val workRequest = PeriodicWorkRequestBuilder<VerificarAniversariosWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = 1,
            flexTimeIntervalUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag("aniversarios")
            .addTag("notificacoes")
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            VERIFICAR_ANIVERSARIOS_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        
        Timber.d("✅ Verificação periódica de aniversários agendada (executa diariamente)")
    }
    
    /**
     * Cancela a verificação periódica de aniversários
     */
    fun cancelarVerificacaoAniversarios() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(VERIFICAR_ANIVERSARIOS_WORK_NAME)
        Timber.d("❌ Verificação periódica de aniversários cancelada")
    }
    
    /**
     * Executa verificação de aniversários imediatamente (one-time work)
     */
    fun executarVerificacaoAniversariosImediata() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        
        val workRequest = androidx.work.OneTimeWorkRequestBuilder<VerificarAniversariosWorker>()
            .setConstraints(constraints)
            .addTag("aniversarios")
            .addTag("notificacoes")
            .addTag("imediata")
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
        Timber.d("✅ Verificação imediata de aniversários agendada")
    }
}

