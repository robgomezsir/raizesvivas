package com.raizesvivas.app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.raizesvivas.app.data.repository.PessoaRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Worker para executar sincronização periódica de relações familiares
 * Executa validação e correção automática de inconsistências
 */
@HiltWorker
class SincronizacaoRelacoesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val pessoaRepository: PessoaRepository
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            Timber.d("🔄 Iniciando sincronização periódica de relações familiares...")
            
            val resultado = pessoaRepository.sincronizarRelacoesFamiliares()
            
            resultado.onSuccess { relatorio ->
                Timber.d("✅ Sincronização periódica concluída: ${relatorio.pessoasCorrigidas} pessoas corrigidas, ${relatorio.inconsistenciasEncontradas} inconsistências encontradas")
                
                // Se houver muitas inconsistências, pode ser necessário notificar admin
                if (relatorio.inconsistenciasEncontradas > 10) {
                    Timber.w("⚠️ Muitas inconsistências encontradas (${relatorio.inconsistenciasEncontradas}). Considere revisar os dados.")
                }
                
                Result.success()
            }.onFailure { exception ->
                Timber.e(exception, "❌ Erro na sincronização periódica de relações")
                // Retornar retry para tentar novamente mais tarde
                Result.retry()
            }
            
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro inesperado na sincronização periódica")
            Result.failure()
        }
    }
}

