package cit.edu.wildevents

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast


class RegisterActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.screen_register)

        val firstName = findViewById<EditText>(R.id.firstName)
        val lastName = findViewById<EditText>(R.id.lastName)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.passwordRegister)
        val confirmPassword = findViewById<EditText>(R.id.confirmPassword)

        val goToLogin = findViewById<TextView>(R.id.goToLogin)
        val loginScreen = Intent(this, LoginActivity::class.java)

        val btnSignup = findViewById<Button>(R.id.btnSignup)

        btnSignup.setOnClickListener {
            if (firstName.text.isNullOrEmpty() || lastName.text.isNullOrEmpty() || email.text.isNullOrEmpty()
                || password.text.isNullOrEmpty() || confirmPassword.text.isNullOrEmpty()) {

                Toast.makeText(this, "Ensure all fields are filled", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            startActivity(loginScreen)
        }

        goToLogin.setOnClickListener {
            startActivity(loginScreen)
        }
    }
}