package cit.edu.wildevents.data

/**
 * This data class represents an event in the app.
 * It contains the event's title, date, description, and image resource ID.
 */
data class Event(
    val eventId: String = "",
    val eventName: String = "",
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val location: String = "",
    val description: String = "",
    val imageUrl: String? = null, // Only one image
    val hostId: String = "",
    val tags: List<String> = emptyList(),
    val capacity: Int? = null,
)
