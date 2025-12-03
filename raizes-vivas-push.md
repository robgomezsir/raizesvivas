# Implementação Completa de Notificações Push - Raízes Vivas

## 📋 Índice

1. [Configuração do Firebase Console](#1-configuração-do-firebase-console)
2. [Dependências do Android](#2-dependências-do-android)
3. [Implementar o FCM Service](#3-implementar-o-fcm-service)
4. [Repository para Notificações](#4-repository-para-notificações)
5. [Cloud Functions para Envio](#5-cloud-functions-para-envio)
6. [Solicitar Permissão (Android 13+)](#6-solicitar-permissão-android-13)
7. [Atualizar Modelo de Dados](#7-atualizar-modelo-de-dados)
8. [Criar Ícone de Notificação](#8-criar-ícone-de-notificação)
9. [Testar Notificações](#9-testar-notificações)
10. [Recursos Adicionais](#10-recursos-adicionais)

---

## 1. Configuração do Firebase Console

### 1.1. Ativar Cloud Messaging

1. Acesse o [Firebase Console](https://console.firebase.google.com)
2. Selecione seu projeto "Raízes Vivas"
3. Vá em **Build > Cloud Messaging**
4. Certifique-se de que o Cloud Messaging API está habilitado

### 1.2. Configurar chaves do servidor

1. Vá em **Configurações do Projeto** (ícone de engrenagem)
2. Aba **Cloud Messaging**
3. Na seção **Cloud Messaging API (V1)**, habilite a API se ainda não estiver
4. Copie o **Server Key** (será usado nas Cloud Functions)

---

## 2. Dependências do Android

### 2.1. Adicionar dependências no `build.gradle` (app level)

```gradle
dependencies {
    // Suas dependências existentes...
    
    // Firebase Cloud Messaging
    implementation("com.google.firebase:firebase-messaging:23.4.0")
    
    // Para notificações com ícones e imagens
    implementation("androidx.core:core-ktx:1.12.0")
    
    // Coroutines (se ainda não tiver)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    
    // WorkManager (para processar notificações em background)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Coil para carregar imagens (opcional, para notificações com imagem)
    implementation("io.coil-kt:coil:2.5.0")
    implementation("io.coil-kt:coil-compose:2.5.0")
}
```

### 2.2. Atualizar `AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    
    <!-- Permissões -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    
    <application
        android:name=".RaizesVivasApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.RaizesVivas"
        tools:targetApi="31">
        
        <!-- Suas activities existentes -->
        
        <!-- Service para receber mensagens FCM -->
        <service
            android:name=".notification.FCMService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
        
        <!-- Metadados para ícone de notificação padrão -->
        <meta-data
            android:name="com.google.firebase.messaging.default_notification_icon"
            android:resource="@drawable/ic_notification" />
        
        <meta-data
            android:name="com.google.firebase.messaging.default_notification_color"
            android:resource="@color/primary" />
        
        <!-- Canal de notificação padrão -->
        <meta-data
            android:name="com.google.firebase.messaging.default_notification_channel_id"
            android:value="raizes_vivas_default" />
            
    </application>
</manifest>
```

---

## 3. Implementar o FCM Service

### 3.1. Criar `notification/FCMService.kt`

```kotlin
package com.raizesvivas.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.raizesvivas.MainActivity
import com.raizesvivas.R
import com.raizesvivas.data.repository.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "Mensagem recebida de: ${remoteMessage.from}")

        // Processar dados da notificação
        remoteMessage.data.let { data ->
            if (data.isNotEmpty()) {
                Log.d(TAG, "Dados da mensagem: $data")
                
                val type = data["type"] ?: "geral"
                val title = data["title"] ?: "Raízes Vivas"
                val body = data["body"] ?: ""
                val targetUserId = data["targetUserId"]
                val relatedId = data["relatedId"]
                val imageUrl = data["imageUrl"]

                // Salvar notificação no Firestore
                CoroutineScope(Dispatchers.IO).launch {
                    saveNotificationToFirestore(
                        type = type,
                        title = title,
                        body = body,
                        targetUserId = targetUserId,
                        relatedId = relatedId
                    )
                }

                // Exibir notificação local
                CoroutineScope(Dispatchers.Main).launch {
                    sendNotification(title, body, type, relatedId, imageUrl)
                }
            }
        }

        // Se houver notificação visual (quando app está em foreground)
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Notificação visual: ${notification.title}")
            
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
        Log.d(TAG, "Novo token FCM: $token")
        
        // Salvar novo token no Firestore
        CoroutineScope(Dispatchers.IO).launch {
            try {
                notificationRepository.updateFCMToken(token)
                Log.d(TAG, "Token FCM atualizado no Firestore")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao atualizar token FCM", e)
            }
        }
    }

    private suspend fun saveNotificationToFirestore(
        type: String,
        title: String,
        body: String,
        targetUserId: String?,
        relatedId: String?
    ) {
        try {
            notificationRepository.createNotification(
                type = type,
                title = title,
                message = body,
                relatedId = relatedId,
                targetUserId = targetUserId
            )
            Log.d(TAG, "Notificação salva no Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar notificação", e)
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
        
        Log.d(TAG, "Notificação exibida - ID: $notificationId, Tipo: $type")
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
            
            Log.d(TAG, "Canais de notificação criados: ${channels.size}")
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
                Log.e(TAG, "Erro ao carregar imagem da notificação", e)
                null
            }
        }
    }

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_DEFAULT = "raizes_vivas_default"
        private const val CHANNEL_MESSAGES = "raizes_vivas_messages"
        private const val CHANNEL_EDITS = "raizes_vivas_edits"
        private const val CHANNEL_ACHIEVEMENTS = "raizes_vivas_achievements"
        private const val CHANNEL_BIRTHDAYS = "raizes_vivas_birthdays"
        private const val CHANNEL_INVITES = "raizes_vivas_invites"
    }
}
```

---

## 4. Repository para Notificações

### 4.1. Criar `data/repository/NotificationRepository.kt`

```kotlin
package com.raizesvivas.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val fcm: FirebaseMessaging
) {
    
    /**
     * Obtém o token FCM atual do dispositivo
     */
    suspend fun getFCMToken(): String? {
        return try {
            val token = fcm.token.await()
            Log.d(TAG, "Token FCM obtido: $token")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter token FCM", e)
            null
        }
    }

    /**
     * Atualiza o token FCM do usuário no Firestore
     */
    suspend fun updateFCMToken(token: String) {
        val userId = auth.currentUser?.uid ?: run {
            Log.w(TAG, "Usuário não autenticado, não é possível atualizar token")
            return
        }
        
        try {
            firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "fcmToken" to token,
                        "fcmTokenUpdatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
            
            Log.d(TAG, "Token FCM atualizado no Firestore para usuário: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar token FCM no Firestore", e)
        }
    }

    /**
     * Remove o token FCM do usuário (útil no logout)
     */
    suspend fun removeFCMToken() {
        val userId = auth.currentUser?.uid ?: return
        
        try {
            firestore.collection("users")
                .document(userId)
                .update("fcmToken", null)
                .await()
            
            // Deletar token do FCM
            fcm.deleteToken().await()
            
            Log.d(TAG, "Token FCM removido")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao remover token FCM", e)
        }
    }

    /**
     * Cria uma notificação no Firestore
     */
    suspend fun createNotification(
        type: String,
        title: String,
        message: String,
        relatedId: String?,
        targetUserId: String?
    ) {
        val userId = targetUserId ?: auth.currentUser?.uid ?: run {
            Log.w(TAG, "Nenhum usuário alvo especificado")
            return
        }
        
        val notification = hashMapOf(
            "type" to type,
            "title" to title,
            "message" to message,
            "relatedId" to relatedId,
            "lida" to false,
            "criadaEm" to com.google.firebase.Timestamp.now()
        )

        try {
            firestore.collection("usuarios")
                .document(userId)
                .collection("notificacoes")
                .add(notification)
                .await()
            
            Log.d(TAG, "Notificação criada no Firestore - Tipo: $type, Usuário: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar notificação no Firestore", e)
        }
    }

    /**
     * Marca uma notificação como lida
     */
    suspend fun markNotificationAsRead(notificationId: String) {
        val userId = auth.currentUser?.uid ?: return
        
        try {
            firestore.collection("usuarios")
                .document(userId)
                .collection("notificacoes")
                .document(notificationId)
                .update("lida", true)
                .await()
            
            Log.d(TAG, "Notificação marcada como lida: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao marcar notificação como lida", e)
        }
    }

    /**
     * Obtém todas as notificações não lidas do usuário
     */
    suspend fun getUnreadNotifications(): List<Map<String, Any>> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        
        return try {
            val snapshot = firestore.collection("usuarios")
                .document(userId)
                .collection("notificacoes")
                .whereEqualTo("lida", false)
                .orderBy("criadaEm", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.data?.plus("id" to doc.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter notificações não lidas", e)
            emptyList()
        }
    }

    /**
     * Deleta todas as notificações lidas antigas (mais de 30 dias)
     */
    suspend fun deleteOldReadNotifications() {
        val userId = auth.currentUser?.uid ?: return
        
        try {
            val thirtyDaysAgo = com.google.firebase.Timestamp(
                System.currentTimeMillis() / 1000 - (30 * 24 * 60 * 60),
                0
            )
            
            val snapshot = firestore.collection("usuarios")
                .document(userId)
                .collection("notificacoes")
                .whereEqualTo("lida", true)
                .whereLessThan("criadaEm", thirtyDaysAgo)
                .get()
                .await()
            
            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            
            Log.d(TAG, "Notificações antigas deletadas: ${snapshot.size()}")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar notificações antigas", e)
        }
    }

    companion object {
        private const val TAG = "NotificationRepository"
    }
}
```

### 4.2. Atualizar módulo Hilt `AppModule.kt`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    // Suas outras injeções...
    
    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideNotificationRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        fcm: FirebaseMessaging
    ): NotificationRepository {
        return NotificationRepository(firestore, auth, fcm)
    }
}
```

---

## 5. Cloud Functions para Envio

### 5.1. Atualizar `functions/src/index.ts`

```typescript
import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

admin.initializeApp();

// ============================================================
// FUNÇÕES AUXILIARES
// ============================================================

/**
 * Envia notificação push para um usuário específico
 */
async function sendPushNotification(
  userId: string,
  title: string,
  body: string,
  type: string,
  relatedId?: string,
  imageUrl?: string
): Promise<void> {
  try {
    // Buscar token FCM do usuário
    const userDoc = await admin.firestore()
      .collection('users')
      .doc(userId)
      .get();

    const fcmToken = userDoc.data()?.fcmToken;

    if (!fcmToken) {
      console.log(`Usuário ${userId} não possui token FCM registrado`);
      return;
    }

    // Montar payload da notificação
    const message: admin.messaging.Message = {
      token: fcmToken,
      notification: {
        title: title,
        body: body,
        imageUrl: imageUrl
      },
      data: {
        type: type,
        targetUserId: userId,
        relatedId: relatedId || '',
        timestamp: Date.now().toString()
      },
      android: {
        priority: 'high',
        notification: {
          channelId: getChannelId(type),
          sound: 'default',
          priority: 'high',
          defaultVibrateTimings: true
        }
      },
      apns: {
        payload: {
          aps: {
            sound: 'default',
            badge: 1
          }
        }
      }
    };

    // Enviar notificação
    const response = await admin.messaging().send(message);
    console.log(`✅ Notificação enviada para ${userId}:`, response);

    // Registrar analytics
    await admin.firestore()
      .collection('analytics_notificacoes')
      .add({
        userId: userId,
        type: type,
        sentAt: admin.firestore.FieldValue.serverTimestamp(),
        success: true
      });

  } catch (error: any) {
    console.error(`❌ Erro ao enviar notificação para ${userId}:`, error);
    
    // Se o token for inválido, removê-lo do Firestore
    if (error.code === 'messaging/invalid-registration-token' ||
        error.code === 'messaging/registration-token-not-registered') {
      await admin.firestore()
        .collection('users')
        .doc(userId)
        .update({ fcmToken: admin.firestore.FieldValue.delete() });
      
      console.log(`Token FCM inválido removido para usuário ${userId}`);
    }
  }
}

/**
 * Retorna o ID do canal de notificação baseado no tipo
 */
function getChannelId(type: string): string {
  const channels: { [key: string]: string } = {
    'mensagem': 'raizes_vivas_messages',
    'edicao_aprovada': 'raizes_vivas_edits',
    'edicao_rejeitada': 'raizes_vivas_edits',
    'conquista': 'raizes_vivas_achievements',
    'aniversario': 'raizes_vivas_birthdays',
    'convite': 'raizes_vivas_invites',
    'recado': 'raizes_vivas_messages'
  };
  return channels[type] || 'raizes_vivas_default';
}

/**
 * Busca nome de um usuário
 */
async function getUserName(userId: string): Promise<string> {
  try {
    const userDoc = await admin.firestore()
      .collection('users')
      .doc(userId)
      .get();
    
    return userDoc.data()?.nome || 'Alguém';
  } catch (error) {
    console.error('Erro ao buscar nome do usuário:', error);
    return 'Alguém';
  }
}

/**
 * Busca nome de uma pessoa
 */
async function getPersonName(personId: string): Promise<string> {
  try {
    const personDoc = await admin.firestore()
      .collection('people')
      .doc(personId)
      .get();
    
    return personDoc.data()?.nome || 'Uma pessoa';
  } catch (error) {
    console.error('Erro ao buscar nome da pessoa:', error);
    return 'Uma pessoa';
  }
}

// ============================================================
// TRIGGERS DE NOTIFICAÇÃO
// ============================================================

/**
 * TRIGGER: Nova mensagem de chat
 */
export const onNewChatMessage = functions.firestore
  .document('mensagens_chat/{messageId}')
  .onCreate(async (snap, context) => {
    const message = snap.data();
    const recipientId = message.destinatarioId;
    const senderId = message.remetenteId;

    // Não enviar notificação se for o mesmo usuário
    if (recipientId === senderId) {
      return;
    }

    const senderName = await getUserName(senderId);

    await sendPushNotification(
      recipientId,
      `💬 Nova mensagem de ${senderName}`,
      message.mensagem,
      'mensagem',
      senderId
    );
  });

/**
 * TRIGGER: Edição aprovada ou rejeitada
 */
export const onEditStatusChanged = functions.firestore
  .document('pending_edits/{editId}')
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();
    const editId = context.params.editId;

    // Edição aprovada
    if (before.status === 'pendente' && after.status === 'aprovado') {
      const requesterId = after.solicitadoPor;
      const personName = await getPersonName(after.pessoaId);

      await sendPushNotification(
        requesterId,
        '✅ Edição aprovada!',
        `Sua sugestão de edição para ${personName} foi aprovada`,
        'edicao_aprovada',
        editId
      );
    }

    // Edição rejeitada
    if (before.status === 'pendente' && after.status === 'rejeitado') {
      const requesterId = after.solicitadoPor;
      const personName = await getPersonName(after.pessoaId);
      const reason = after.motivoRejeicao || 'Não foi aprovada pelos administradores';

      await sendPushNotification(
        requesterId,
        '❌ Edição não aprovada',
        `Sua sugestão para ${personName}: ${reason}`,
        'edicao_rejeitada',
        editId
      );
    }
  });

/**
 * TRIGGER: Novo recado direcionado
 */
export const onNewDirectMessage = functions.firestore
  .document('recados/{recadoId}')
  .onCreate(async (snap, context) => {
    const recado = snap.data();
    const recadoId = context.params.recadoId;

    if (recado.direcionadoParaId) {
      const authorName = await getUserName(recado.autorId);

      await sendPushNotification(
        recado.direcionadoParaId,
        `📌 Recado de ${authorName}`,
        recado.mensagem,
        'recado',
        recadoId
      );
    }
  });

/**
 * TRIGGER: Nova conquista desbloqueada
 */
export const onAchievementUnlocked = functions.firestore
  .document('usuarios/{userId}/conquistasProgresso/{conquistaId}')
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();
    const userId = context.params.userId;
    const conquistaId = context.params.conquistaId;

    if (!before.desbloqueada && after.desbloqueada) {
      const conquistaDoc = await admin.firestore()
        .collection('conquist