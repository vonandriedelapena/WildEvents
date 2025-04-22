package cit.edu.wildevents

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import cit.edu.wildevents.app.MyApplication
import cit.edu.wildevents.data.User
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class LoginActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_login)

        db = FirebaseFirestore.getInstance()

        val thisEmail = findViewById<EditText>(R.id.edittext_email)
        val thisPassword = findViewById<EditText>(R.id.edittext_password)
        val checkbox = findViewById<CheckBox>(R.id.checkbox_remember)

        val rememberedUser = loadUserSession()
        rememberedUser?.let {
            thisEmail.setText(it.email)
            thisPassword.setText(it.password)
            checkbox.isChecked = true
        }

        intent?.getStringExtra("email")?.let { thisEmail.setText(it) }
        intent?.getStringExtra("password")?.let { thisPassword.setText(it) }

        val registerScreen = Intent(this, RegisterActivity::class.java)
        val mainScreen = Intent(this, MainActivity::class.java)
        val landingScreen = Intent(this, LandingPageActivity::class.java)

        findViewById<Button>(R.id.button_login).setOnClickListener {
            val email = thisEmail.text.toString().trim()
            val password = thisPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password must not be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            db.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener { result ->
                    Log.d("LoginCheck", "Result: ${result.documents}")

                    if (result.isEmpty) {
                        Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val userDoc = result.documents[0]
                    val user = User(
                        id = userDoc.id,
                        firstName = userDoc.getString("firstName") ?: "",
                        lastName = userDoc.getString("lastName") ?: "",
                        email = userDoc.getString("email") ?: "",
                        password = userDoc.getString("password") ?: "",
                        isHost = userDoc.getBoolean("isHost") ?: false,
                        isVerified = userDoc.getBoolean("isVerified") ?: false,
                        phoneNumber = userDoc.getString("phoneNumber") ?: "Not provided",
                        emergencyContact = userDoc.getString("emergencyContact") ?: "Not provided",
                        address = userDoc.getString("address") ?: "Not provided",
                        profilePic = userDoc.getString("profilePic") ?: "default.png",
                        isFirstTime = userDoc.getBoolean("isFirstTime") ?: true
                    )

                    MyApplication.instance.currentUser = user

                    if (checkbox.isChecked) {
                        saveUserSession(user)
                    } else {
                        deleteUserSession()
                    }

                    Toast.makeText(this, "Welcome ${user.firstName}!", Toast.LENGTH_SHORT).show()

                    if (user.isFirstTime) {
                        // 🔁 Update Firestore isFirstTime to false
                        db.collection("users").document(user.id)
                            .update("isFirstTime", false)
                            .addOnSuccessListener {
                                Log.d("FirstTimeLogin", "isFirstTime set to false in Firestore")
                            }
                            .addOnFailureListener { e ->
                                Log.e("FirestoreError", "Failed to update isFirstTime: ${e.message}")
                            }

                        startActivity(landingScreen)
                    } else {
                        startActivity(mainScreen)
                    }

                    finish()

                }
                .addOnFailureListener { e ->
                    Log.e("LoginError", "Login failed: ${e.message}")
                    Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        findViewById<TextView>(R.id.goToSignup).setOnClickListener {
                startActivity(registerScreen)
        }
    }

    private fun saveUserSession(user: User) {
        try {
            val file = File(filesDir, "user_session.ser")
            ObjectOutputStream(FileOutputStream(file)).use { it.writeObject(user) }
            Log.d("Session", "User session saved successfully.")
        } catch (e: Exception) {
            Log.e("SessionError", "Failed to save session: ${e.message}")
        }
    }

    private fun loadUserSession(): User? {
        return try {
            val file = File(filesDir, "user_session.ser")
            if (!file.exists()) return null
            ObjectInputStream(FileInputStream(file)).use { it.readObject() as? User }
        } catch (e: Exception) {
            Log.e("SessionError", "Failed to load session: ${e.message}")
            null
        }
    }

    private fun deleteUserSession() {
        val file = File(filesDir, "user_session.ser")
        if (file.exists()) {
            file.delete()
            Log.d("Session", "User session file deleted.")
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
