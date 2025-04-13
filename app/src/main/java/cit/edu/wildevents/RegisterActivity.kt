package cit.edu.wildevents

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.csit284.myapplication.utils.toast

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_register)

        var userType: String = ""

        val firstName = findViewById<EditText>(R.id.firstName)
        val lastName = findViewById<EditText>(R.id.lastName)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.passwordRegister)
        val confirmPassword = findViewById<EditText>(R.id.confirmPassword)

        val goToLogin = findViewById<TextView>(R.id.goToLogin)
        val btnSignup = findViewById<Button>(R.id.btnSignup)
        val btnHost = findViewById<Button>(R.id.btnHost)
        val btnStudent = findViewById<Button>(R.id.btnStudent)

        val sharedPreferences: SharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        btnHost.setOnClickListener {
            selectRole(btnHost, btnStudent)
            userType = "host"
        }

        btnStudent.setOnClickListener {
            selectRole(btnStudent, btnHost)
            userType = "student"
        }

        // Function to handle "Enter" key navigation
        fun setEnterKeyListener(editText: EditText, nextEditText: EditText?) {
            editText.setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_NEXT ||
                    (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                    nextEditText?.requestFocus()
                    return@setOnEditorActionListener true
                }
                false
            }
        }

        // Setup "Enter" key navigation
        setEnterKeyListener(firstName, lastName)
        setEnterKeyListener(lastName, email)
        setEnterKeyListener(email, password)
        setEnterKeyListener(password, confirmPassword)
        setEnterKeyListener(confirmPassword, null) // Last field, so no next focus

        btnSignup.setOnClickListener {
            val passwordText = password.text.toString()
            val confirmPasswordText = confirmPassword.text.toString()

            if (firstName.text.isNullOrEmpty() || lastName.text.isNullOrEmpty() ||
                email.text.isNullOrEmpty() || passwordText.isEmpty() || confirmPasswordText.isEmpty()) {

                Toast.makeText(this, "Ensure all fields are filled", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email.text).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Validate password complexity
            if (!passwordText.matches(Regex(".*\\d.*")) || !passwordText.matches(Regex(".*[!@#\$%^&*()_+\\-=\\[\\]{};':\",.<>?/].*"))) {
                Toast.makeText(this, "Password must contain at least one number and one special character", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Validate password match
            if (passwordText != confirmPasswordText) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (userType.isEmpty()) {
                Toast.makeText(this, "Please select either Host or Student", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Save user info in SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putString("firstName", firstName.text.toString())
            editor.putString("lastName", lastName.text.toString())
            editor.putString("email", email.text.toString())
            editor.putString("phoneNumber", "Not provided")
            editor.putString("address", "Not provided")
            editor.putString("emergencyContact", "Not provided")
            editor.putBoolean("isVerified", false)
            editor.putString("userType", userType)
            editor.apply()

            // Redirect to login
            val loginScreen = Intent(this, LoginActivity::class.java)
            startActivity(loginScreen.apply {
                putExtra("email", email.text.toString())
                putExtra("password", passwordText)
            })
            finish() // Close this activity
        }

        goToLogin.setOnClickListener {
            val loginScreen = Intent(this, LoginActivity::class.java)
            startActivity(loginScreen)
            finish()
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finish() // Closes all activities and exits the app
    }

    private fun selectRole(selectedButton: Button, unselectedButton: Button) {
        selectedButton.isSelected = true
        unselectedButton.isSelected = false
    }
}
