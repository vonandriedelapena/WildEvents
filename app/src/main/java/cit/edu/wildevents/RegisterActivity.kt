package cit.edu.wildevents

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.csit284.myapplication.utils.toast

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var isHost: Boolean? = null
    private var isVerified: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_register)

        db = FirebaseFirestore.getInstance()

        val firstName = findViewById<EditText>(R.id.firstName)
        val lastName = findViewById<EditText>(R.id.lastName)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.passwordRegister)
        val confirmPassword = findViewById<EditText>(R.id.confirmPassword)

        val btnSignup = findViewById<Button>(R.id.btnSignup)
        val btnHost = findViewById<Button>(R.id.btnHost)
        val btnStudent = findViewById<Button>(R.id.btnStudent)
        val goToLogin = findViewById<TextView>(R.id.goToLogin)

        // Role selection
        btnHost.setOnClickListener {
            if (isVerified) {
                selectRole(btnHost, btnStudent)
                isHost = true
                return@setOnClickListener
            }

            val input = EditText(this)
            input.hint = "Enter Host Code"
            input.inputType = android.text.InputType.TYPE_CLASS_TEXT

            val errorText = TextView(this).apply {
                setTextColor(Color.parseColor("#800000"))
                textSize = 14f
                text = ""
            }

            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(50, 20, 50, 0)
                addView(input)
                addView(errorText)
            }

            val dialog = AlertDialog.Builder(this)
                .setTitle("Host Verification")
                .setMessage("Please enter the verification code to register as a host.")
                .setView(layout)
                .setPositiveButton("Verify", null) // override later
                .setNegativeButton("Cancel") { d, _ ->
                    isHost = null
                    isVerified = false
                    d.cancel()
                }
                .create()

            dialog.setOnShowListener {
                val verifyButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                verifyButton.setOnClickListener {
                    val enteredCode = input.text.toString().trim()
                    val correctCode = "WILDHOST2024"

                    if (enteredCode == correctCode) {
                        selectRole(btnHost, btnStudent)
                        isHost = true
                        isVerified = true
                        toast("Host verified")
                        dialog.dismiss()
                    } else {
                        errorText.text = "Incorrect verification code. Try again."
                    }
                }
            }

            dialog.show()
        }


        btnStudent.setOnClickListener {
            selectRole(btnStudent, btnHost)
            isHost = false
        }

        // Enter key navigation
        setEnterKeyListener(firstName, lastName)
        setEnterKeyListener(lastName, email)
        setEnterKeyListener(email, password)
        setEnterKeyListener(password, confirmPassword)

        btnSignup.setOnClickListener {
            val fName = firstName.text.toString().trim()
            val lName = lastName.text.toString().trim()
            val userEmail = email.text.toString().trim()
            val pass = password.text.toString()
            val confirmPass = confirmPassword.text.toString()

            if (fName.isEmpty() || lName.isEmpty() || userEmail.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                toast("Please fill in all fields")
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                toast("Invalid email format")
                return@setOnClickListener
            }

            if (!pass.matches(Regex(".*\\d.*")) || !pass.matches(Regex(".*[!@#\$%^&*()_+\\-=\\[\\]{};':\",.<>?/].*"))) {
                toast("Password must contain at least one number and one special character")
                return@setOnClickListener
            }

            if (pass != confirmPass) {
                toast("Passwords do not match")
                return@setOnClickListener
            }

            if (isHost == null) {
                toast("Please select either Host or Student")
                return@setOnClickListener
            }

            if(isHost == false){
                isVerified = false;
            }

            db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        toast("Email already exists")
                        return@addOnSuccessListener
                    }

                    val userData = hashMapOf(
                        "firstName" to fName,
                        "lastName" to lName,
                        "email" to userEmail,
                        "password" to pass,
                        "isHost" to isHost!!,
                        "address" to "Not provided",
                        "phoneNumber" to "Not provided",
                        "emergencyContact" to "Not provided",
                        "isVerified" to isVerified,
                        "profilePic" to "default.png",
                        "isFirstTime" to true,
                    )


                    db.collection("users").add(userData)
                        .addOnSuccessListener {
                            toast("Registered successfully!")
                            val loginIntent = Intent(this, LoginActivity::class.java)
                            loginIntent.putExtra("email", userEmail)
                            loginIntent.putExtra("password", pass)
                            startActivity(loginIntent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            toast("Registration failed: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    toast("Error checking existing email: ${e.message}")
                }
        }

        goToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun selectRole(selectedButton: Button, unselectedButton: Button) {
        selectedButton.isSelected = true
        unselectedButton.isSelected = false
    }

    private fun setEnterKeyListener(current: EditText, next: EditText?) {
        current.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                next?.requestFocus()
                return@setOnEditorActionListener true
            }
            false
        }
    }
}
