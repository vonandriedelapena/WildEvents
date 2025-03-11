package cit.edu.wildevents

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SettingsPersonalInformation : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: PersonalInfoAdapter
    private var items = mutableListOf<PersonalInfoItem>()

    // Register an ActivityResultLauncher to handle the edited data
    private val editInfoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val updatedTitle = result.data?.getStringExtra("title") ?: return@registerForActivityResult
                val updatedValue = result.data?.getStringExtra("value") ?: return@registerForActivityResult

                // Update SharedPreferences
                with(sharedPreferences.edit()) {
                    when (updatedTitle) {
                        "Legal name" -> putString("firstName", updatedValue.split(" ")[0])
                        "Preferred first name" -> putString("firstName", updatedValue)
                        "Phone number" -> putString("phoneNumber", updatedValue)
                        "Email" -> putString("email", updatedValue)
                        "Address" -> putString("address", updatedValue)
                        "Emergency contact" -> putString("emergencyContact", updatedValue)
                    }
                    apply()
                }

                // Update the list and refresh RecyclerView
                items.find { it.title == updatedTitle }?.let { it.value = updatedValue }
                adapter.notifyDataSetChanged()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_personal_information)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Load stored user info
        val firstName = sharedPreferences.getString("firstName", "Not provided") ?: "Not provided"
        val lastName = sharedPreferences.getString("lastName", "Not provided") ?: "Not provided"
        val email = sharedPreferences.getString("email", "Not provided") ?: "Not provided"
        val phoneNumber = sharedPreferences.getString("phoneNumber", "Not provided") ?: "Not provided"
        val address = sharedPreferences.getString("address", "Not provided") ?: "Not provided"
        val emergencyContact = sharedPreferences.getString("emergencyContact", "Not provided") ?: "Not provided"
        val isVerified = sharedPreferences.getBoolean("isVerified", false)

        items = mutableListOf(
            PersonalInfoItem("Legal name", "$firstName $lastName", true),
            PersonalInfoItem("Preferred first name", firstName, true),
            PersonalInfoItem("Phone number", phoneNumber, true),
            PersonalInfoItem("Email", email, false), // Email might not be editable
            PersonalInfoItem("Address", address, true),
            PersonalInfoItem("Emergency contact", emergencyContact, true),
            PersonalInfoItem("Identity verification", if (isVerified) "Verified" else "Not Verified", false)
        )

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PersonalInfoAdapter(items) { item ->
            val intent = Intent(this, EditPersonalInfoActivity::class.java).apply {
                putExtra("title", item.title)
                putExtra("value", item.value)
            }
            editInfoLauncher.launch(intent)
        }
        recyclerView.adapter = adapter

        val goToMainActivity = findViewById<ImageView>(R.id.back_button)
        goToMainActivity.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
