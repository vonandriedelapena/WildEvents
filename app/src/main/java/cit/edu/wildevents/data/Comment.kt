package cit.edu.wildevents.data

data class Comment(
    val id: String,
    val userName: String,
    val userAvatarUrl: String?,
    val content: String,
    val timestamp: Long
)