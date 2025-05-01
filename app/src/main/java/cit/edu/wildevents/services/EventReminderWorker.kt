package cit.edu.wildevents.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import cit.edu.wildevents.app.MyApplication
import cit.edu.wildevents.data.NotificationData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EventReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val eventTitle = inputData.getString("eventTitle") ?: "Upcoming Event"
        val message = inputData.getString("message") ?: "Your event is starting soon!"

        showNotification(eventTitle, message)

        val userId = (applicationContext as MyApplication).currentUser?.id ?: return Result.failure()
        val db = FirebaseFirestore.getInstance()
        val data = NotificationData(
            title = eventTitle,
            message = message,
            eventId = db.collection("events").document().id
        )
        saveNotification(userId, data)

        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "wild_events_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Event Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // --- Check permission before notifying ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(applicationContext).notify(
                    System.currentTimeMillis().toInt(),
                    builder.build()
                )
            } else {
                // Optionally log: user denied notifications
                Log.d("Reminder", "Notification permission denied")
            }
        } else {
            // For Android 12 and below, no permission needed
            NotificationManagerCompat.from(applicationContext).notify(
                System.currentTimeMillis().toInt(),
                builder.build()
            )
        }
    }
    private fun saveNotification(userId: String, data: NotificationData) {
        Log.d("NotificationDebug", "Attempting to save notification for $userId")

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userId)
            .collection("notifications")
            .add(data)
            .addOnSuccessListener {
                Log.d("NotificationDebug", "Notification saved")
            }
            .addOnFailureListener {
                Log.e("NotificationDebug", "Error saving notification", it)
            }
    }
}
