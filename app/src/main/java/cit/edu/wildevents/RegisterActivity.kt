package cit.edu.wildevents

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_register)

        val firstName = findViewById<EditText>(R.id.firstName)
        val lastName = findViewById<EditText>(R.id.lastName)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.passwordRegister)
        val confirmPassword = findViewById<EditText>(R.id.confirmPassword)

        val goToLogin = findViewById<TextView>(R.id.goToLogin)
        val btnSignup = findViewById<Button>(R.id.btnSignup)

        val sharedPreferences: SharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        btnSignup.setOnClickListener {
            if (firstName.text.isNullOrEmpty() || lastName.text.isNullOrEmpty() || email.text.isNullOrEmpty()
                || password.text.isNullOrEmpty() || confirmPassword.text.isNullOrEmpty()) {

                Toast.makeText(this, "Ensure all fields are filled", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Save user info in SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putString("firstName", firstName.text.toString())
            editor.putString("lastName", lastName.text.toString())
            editor.putString("email", email.text.toString())
            editor.putString("phoneNumber", "Not provided") // Default value
            editor.putString("address", "Not provided") // Default value
            editor.putString("emergencyContact", "Not provided") // Default value
            editor.putBoolean("isVerified", false) // Default false
            editor.apply()

            // Redirect to login
            val loginScreen = Intent(this, LoginActivity::class.java)
            startActivity(loginScreen.apply {
                putExtra("email", email.text.toString())
                putExtra("password", password.text.toString())
            })
            finish() // Close this activity
        }

        goToLogin.setOnClickListener {
            val loginScreen = Intent(this, LoginActivity::class.java)
            startActivity(loginScreen)
        }
    }
}
