package cit.edu.wildevents

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import cit.edu.wildevents.app.MyApplication
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Event Reminder"
        val message = intent.getStringExtra("message") ?: "You have an upcoming event!"

        // Save the notification to Firestore
        val userId = (context.applicationContext as MyApplication).currentUser?.id ?: return
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            timestamp = System.currentTimeMillis()
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).collection("notifications")
            .add(notification)
            .addOnSuccessListener {
                // Notification saved successfully
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }

        // Show the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(context, "event_reminders")
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(0, notificationBuilder.build())
    }
}