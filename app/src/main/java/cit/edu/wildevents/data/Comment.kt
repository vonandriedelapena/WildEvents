package cit.edu.wildevents.data

data class Comment(
    val id: String,
    val userId: String,
    val eventId: String,
    val userName: String,
    val userAvatarUrl: String?,
    val content: String,
    val timestamp: Long
)

