package cit.edu.wildevents.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import cit.edu.wildevents.MainActivity // Ensure this import matches your MainActivity's package
import cit.edu.wildevents.R // Ensure this import matches your app's package

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Extract notification data
        val title = remoteMessage.notification?.title ?: "Notification"
        val message = remoteMessage.notification?.body ?: "You have a new notification"

        // Show notification
        showNotification(title, message)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Log the new token (optional)
        println("New FCM Token: $token")

        // Send the token to your server or save it locally
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        // Example: Send the token to your backend server
        // You can use Retrofit, Volley, or any HTTP client
        println("Token sent to server: $token")
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "default_channel"
        val notificationId = System.currentTimeMillis().toInt()

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Default Channel", NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_bell) // Ensure this drawable exists
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}