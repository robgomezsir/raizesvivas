package com.raizesvivas.app.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Utilitário para feedback tátil (vibração)
 * Fornece feedback háptico em ações importantes
 */
object HapticFeedback {
    
    /**
     * Tipos de feedback háptico
     */
    enum class FeedbackType {
        LIGHT,      // Vibração leve (ex: tocar em botão)
        MEDIUM,     // Vibração média (ex: selecionar item)
        HEAVY,      // Vibração forte (ex: ação importante)
        SUCCESS,    // Vibração de sucesso
        ERROR       // Vibração de erro
    }
    
    /**
     * Executa feedback háptico
     * @param context Contexto da aplicação
     * @param type Tipo de feedback
     */
    fun perform(context: Context, type: FeedbackType) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = when (type) {
                        FeedbackType.LIGHT -> VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
                        FeedbackType.MEDIUM -> VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                        FeedbackType.HEAVY -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                        FeedbackType.SUCCESS -> VibrationEffect.createWaveform(
                            longArrayOf(0, 10, 50, 10),
                            intArrayOf(0, 50, 0, 100),
                            -1
                        )
                        FeedbackType.ERROR -> VibrationEffect.createWaveform(
                            longArrayOf(0, 30, 30, 30),
                            intArrayOf(0, 100, 0, 100),
                            -1
                        )
                    }
                    it.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    when (type) {
                        FeedbackType.LIGHT -> it.vibrate(10)
                        FeedbackType.MEDIUM -> it.vibrate(20)
                        FeedbackType.HEAVY -> it.vibrate(50)
                        FeedbackType.SUCCESS -> it.vibrate(longArrayOf(0, 10, 50, 10), -1)
                        FeedbackType.ERROR -> it.vibrate(longArrayOf(0, 30, 30, 30), -1)
                    }
                }
            }
        } catch (e: SecurityException) {
            // Permissão de vibração não disponível - ignorar silenciosamente
        } catch (e: Exception) {
            // Outros erros de vibração - ignorar silenciosamente
        }
    }
}

/**
 * Composable para obter uma função de feedback háptico
 */
@Composable
fun rememberHapticFeedback(): (HapticFeedback.FeedbackType) -> Unit {
    val context = LocalContext.current
    return remember {
        { type -> HapticFeedback.perform(context, type) }
    }
}
