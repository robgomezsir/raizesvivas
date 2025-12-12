package com.raizesvivas.app.notification

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Componente para obter e logar o token FCM
 * Útil para testes e debugging
 */
@Composable
fun FCMTokenLogger(
    autoSave: Boolean = true,
    onTokenObtained: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        try {
            // Obter token FCM
            val token = FirebaseMessaging.getInstance().token.await()
            
            // Logar no console
            Timber.d("════════════════════════════════════════")
            Timber.d("🔔 FCM TOKEN PARA TESTES")
            Timber.d("Token: $token")
            Timber.d("════════════════════════════════════════")
            
            // Também imprimir no System.out para aparecer no Logcat
            println("════════════════════════════════════════")
            println("🔔 FCM TOKEN: $token")
            println("════════════════════════════════════════")
            
            // Copiar para clipboard automaticamente
            if (autoSave) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clip = ClipData.newPlainText("FCM Token", token)
                clipboard?.setPrimaryClip(clip)
                
                Toast.makeText(
                    context,
                    "✅ Token FCM copiado! Verifique o Logcat",
                    Toast.LENGTH_LONG
                ).show()
            }
            
            // Callback opcional
            onTokenObtained?.invoke(token)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao obter token FCM")
            Toast.makeText(
                context,
                "❌ Erro ao obter token FCM",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

/**
 * Versão simplificada que apenas loga sem UI
 */
suspend fun logFCMToken() {
    Timber.d("🔍 Iniciando obtenção de token FCM...")
    println("🔍 Iniciando obtenção de token FCM...")
    
    try {
        Timber.d("📡 Chamando FirebaseMessaging.getInstance().token...")
        val token = FirebaseMessaging.getInstance().token.await()
        
        Timber.d("════════════════════════════════════════")
        Timber.d("🔔 FCM TOKEN")
        Timber.d("$token")
        Timber.d("════════════════════════════════════════")
        
        println("════════════════════════════════════════")
        println("🔔 FCM TOKEN: $token")
        println("════════════════════════════════════════")
        
        // Log adicional para garantir visibilidade
        android.util.Log.d("FCM_TOKEN", "════════════════════════════════════════")
        android.util.Log.d("FCM_TOKEN", "Token: $token")
        android.util.Log.d("FCM_TOKEN", "════════════════════════════════════════")
        
    } catch (e: Exception) {
        Timber.e(e, "❌ Erro ao obter token FCM")
        println("❌ ERRO AO OBTER TOKEN FCM: ${e.message}")
        android.util.Log.e("FCM_TOKEN", "❌ Erro ao obter token FCM", e)
        e.printStackTrace()
    }
}
