package cit.edu.wildevents.data

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
