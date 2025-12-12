package com.raizesvivas.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.raizesvivas.app.MainActivity
import com.raizesvivas.app.R
import com.raizesvivas.app.data.repository.NotificacaoRepository
import com.raizesvivas.app.domain.model.TipoNotificacao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Service para receber e processar notificações push do Firebase Cloud Messaging
 */
@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var notificacaoRepository: NotificacaoRepository

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Timber.d("📩 Mensagem FCM recebida de: ${remoteMessage.from}")

        // Processar dados da notificação
        remoteMessage.data.let { data ->
            if (data.isNotEmpty()) {
                Timber.d("📦 Dados da mensagem: $data")
                
                val type = data["type"] ?: "geral"
                val title = data["title"] ?: "Raízes Vivas"
                val body = data["body"] ?: ""
                val targetUserId = data["targetUserId"]
                val relatedId = data["relatedId"]
                val imageUrl = data["imageUrl"]

                // Exibir notificação local
                CoroutineScope(Dispatchers.Main).launch {
                    sendNotification(title, body, type, relatedId, imageUrl)
                }
            }
        }

        // Se houver notificação visual (quando app está em foreground)
        remoteMessage.notification?.let { notification ->
            Timber.d("🔔 Notificação visual: ${notification.title}")
            
            CoroutineScope(Dispatchers.Main).launch {
                sendNotification(
                    title = notification.title ?: "Raízes Vivas",
                    body = notification.body ?: "",
                    type = "geral",
                    relatedId = null,
                    imageUrl = notification.imageUrl?.toString()
                )
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("🔑 Novo token FCM: $token")
        
        // Salvar novo token no Firestore
        CoroutineScope(Dispatchers.IO).launch {
            try {
                notificacaoRepository.updateFCMToken(token)
                Timber.d("✅ Token FCM atualizado no Firestore")
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao atualizar token FCM")
            }
        }
    }

    private suspend fun sendNotification(
        title: String,
        body: String,
        type: String,
        relatedId: String?,
        imageUrl: String?
    ) {
        val intent = createNotificationIntent(type, relatedId)
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = getChannelIdForType(type)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        // Adicionar imagem se houver
        imageUrl?.let { url ->
            val bitmap = loadBitmap(url)
            bitmap?.let {
                notificationBuilder
                    .setLargeIcon(it)
                    .setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(it)
                            .bigLargeIcon(null as Bitmap?)
                    )
            }
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Criar canal de notificação (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels(notificationManager)
        }

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
        
        Timber.d("✅ Notificação exibida - ID: $notificationId, Tipo: $type")
    }

    private fun createNotificationIntent(type: String, relatedId: String?): Intent {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("notification_type", type)
            relatedId?.let { putExtra("related_id", it) }
        }
        
        return intent
    }

    private fun getChannelIdForType(type: String): String {
        return when (type) {
            "mensagem" -> CHANNEL_MESSAGES
            "edicao_aprovada", "edicao_rejeitada" -> CHANNEL_EDITS
            "conquista" -> CHANNEL_ACHIEVEMENTS
            "aniversario" -> CHANNEL_BIRTHDAYS
            "convite" -> CHANNEL_INVITES
            "recado" -> CHANNEL_MESSAGES
            else -> CHANNEL_DEFAULT
        }
    }

    private fun createNotificationChannels(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_DEFAULT,
                    "Notificações Gerais",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notificações gerais do aplicativo"
                    enableVibration(true)
                    enableLights(true)
                },
                NotificationChannel(
                    CHANNEL_MESSAGES,
                    "Mensagens",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificações de mensagens e recados"
                    enableVibration(true)
                    enableLights(true)
                },
                NotificationChannel(
                    CHANNEL_EDITS,
                    "Edições",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notificações sobre edições aprovadas ou rejeitadas"
                },
                NotificationChannel(
                    CHANNEL_ACHIEVEMENTS,
                    "Conquistas",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Notificações de conquistas desbloqueadas"
                },
                NotificationChannel(
                    CHANNEL_BIRTHDAYS,
                    "Aniversários",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Lembretes de aniversários da família"
                    enableVibration(true)
                    enableLights(true)
                },
                NotificationChannel(
                    CHANNEL_INVITES,
                    "Convites",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificações sobre convites"
                    enableVibration(true)
                }
            )

            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
            
            Timber.d("📢 Canais de notificação criados: ${channels.size}")
        }
    }

    private suspend fun loadBitmap(url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val loader = ImageLoader(this@FCMService)
                val request = ImageRequest.Builder(this@FCMService)
                    .data(url)
                    .allowHardware(false)
                    .build()
                
                val result = loader.execute(request)
                (result as? SuccessResult)?.drawable?.toBitmap()
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao carregar imagem da notificação")
                null
            }
        }
    }

    companion object {
        private const val CHANNEL_DEFAULT = "raizes_vivas_default"
        private const val CHANNEL_MESSAGES = "raizes_vivas_messages"
        private const val CHANNEL_EDITS = "raizes_vivas_edits"
        private const val CHANNEL_ACHIEVEMENTS = "raizes_vivas_achievements"
        private const val CHANNEL_BIRTHDAYS = "raizes_vivas_birthdays"
        private const val CHANNEL_INVITES = "raizes_vivas_invites"
    }
}
