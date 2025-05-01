package cit.edu.wildevents.data

import com.google.firebase.Timestamp

data class NotificationData(
    val title: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val eventId: String = "",
    val seen: Boolean = false
)
