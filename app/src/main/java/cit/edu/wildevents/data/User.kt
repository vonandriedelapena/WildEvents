package cit.edu.wildevents.data

import java.io.Serializable

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val isHost: Boolean,
    val isVerified: Boolean,
    val phoneNumber: String,
    val emergencyContact: String,
    val address: String,
    val profilePic: String = "default.png",
    val isFirstTime: Boolean = true
) : Serializable
