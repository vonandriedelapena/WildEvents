package cit.edu.wildevents

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast


class LoginActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val username = findViewById<EditText>(R.id.edittext_username)
        val password = findViewById<EditText>(R.id.edittext_password)

        val registerScreen = Intent(this, RegisterActivity::class.java)

        val buttonLogin = findViewById<Button>(R.id.button_login)
        buttonLogin.setOnClickListener {
            if(username.text.isNullOrEmpty() || password.text.isNullOrEmpty()) {
                Toast.makeText(this, "Username and Password must not be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if(username.text.contentEquals("csit", true) &&
               password.text.contentEquals("1234", false)) {
                Toast.makeText(this, "Successful login", Toast.LENGTH_LONG).show()
                Log.e("Mob Dev", "Button login is clicked")
                Toast.makeText(this, "Button login is clicked", Toast.LENGTH_LONG).show()
                startActivity(registerScreen)
            }
            else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_LONG).show()
            }
        }

        val goToSignup = findViewById<TextView>(R.id.goToSignup)
        goToSignup.setOnClickListener {
            startActivity(registerScreen)
        }
    }
}