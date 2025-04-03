package com.csit284.myapplication.utils

import android.app.Activity
import android.widget.EditText
import android.widget.Toast

fun EditText.txt(): String {
    return this.text.toString()
}

fun EditText.isInvalidEntry(): Boolean {
    val username = this.text.toString()
    val usernameRegex = "^[a-zA-Z0-9_]{4,20}$".toRegex()  // Only letters, numbers, and underscores, 4-20 chars

    return username.isEmpty() || !username.matches(usernameRegex)
}


fun Activity.toast(msg:String) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}