package cit.edu.wildevents

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.csit284.myapplication.utils.isValidEntry
import com.csit284.myapplication.utils.txt


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_login)

        val thisEmail = findViewById<EditText>(R.id.edittext_email)
        val thisPassword = findViewById<EditText>(R.id.edittext_password)

        intent?.let {
            it.getStringExtra("email")?.let {email ->
                thisEmail.setText(email)
            }

            it.getStringExtra("password")?.let {password ->
                thisPassword.setText(password)
            }
        }

        val registerScreen = Intent(this, RegisterActivity::class.java)
        val mainScreen = Intent(this, MainActivity::class.java)

        val buttonLogin = findViewById<Button>(R.id.button_login)
        buttonLogin.setOnClickListener {
            if(thisEmail.isValidEntry()|| thisPassword.isValidEntry()) {
                Toast.makeText(this, "Username and Password must not be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            startActivity(mainScreen)
        }

        val goToSignup = findViewById<TextView>(R.id.goToSignup)
        goToSignup.setOnClickListener {
            startActivity(registerScreen)
        }
    }
}