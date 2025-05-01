package cit.edu.wildevents.data

import java.io.Serializable

data class User(
    val id: String,
    var firstName: String,
    var lastName: String,
    var email: String,
    val password: String,
    val isHost: Boolean,
    val isVerified: Boolean,
    var phoneNumber: String,
    var emergencyContact: String,
    var address: String,
    val profilePic: String = "default.png",
    val isFirstTime: Boolean = true
) : Serializable
