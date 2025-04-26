package cit.edu.wildevents.data

data class Comment(
    val id: String,
    val userName: String,
    val userAvatarUrl: String?, // nullable
    val content: String,
    val timestamp: Long
)
