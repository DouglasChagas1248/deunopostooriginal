package br.com.dotchagas.deunoposteorig.deunoposteorig

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        private const val CHANNEL_ID = "default_channel"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Token atualizado: $token")
        FcmToken.enviarToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        var titulo = "Resultado"
        var corpo = ""

        remoteMessage.notification?.body?.let {
            corpo = it
        }

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Dados recebidos: ${remoteMessage.data}")

            remoteMessage.data["message"]?.let {
                corpo = it
            }
            remoteMessage.data["loteria"]?.let {
                titulo = it
            }
        }

        mostrarNotificacao(titulo, corpo)
    }

    private fun mostrarNotificacao(titulo: String, corpo: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                "Notificações Padrão",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(canal)
        }

        val intent = Intent(this, PrincipalActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(titulo)
            .setContentText(corpo)
            .setSmallIcon(R.drawable.ic_launcher)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            notificationManager.notify(1, builder.build())
        } else {
            Log.w(TAG, "Permissão POST_NOTIFICATIONS não concedida. Notificação não exibida.")
        }
    }
}