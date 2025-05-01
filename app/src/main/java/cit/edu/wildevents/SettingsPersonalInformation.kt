package cit.edu.wildevents

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildevents.app.MyApplication
import cit.edu.wildevents.data.PersonalInfoItem
import cit.edu.wildevents.data.User
import com.google.firebase.firestore.FirebaseFirestore

class SettingsPersonalInformation : AppCompatActivity() {

    private lateinit var adapter: PersonalInfoAdapter
    private var items = mutableListOf<PersonalInfoItem>()
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_personal_information)

        currentUser = (application as MyApplication).currentUser ?: return
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)

        // Initialize the list with user data
        items = mutableListOf(
            PersonalInfoItem("Legal name", "${currentUser.firstName} ${currentUser.lastName}", true),
            PersonalInfoItem("Preferred first name", currentUser.firstName, true),
            PersonalInfoItem("Phone number", currentUser.phoneNumber, true),
            PersonalInfoItem("Email", currentUser.email, false),
            PersonalInfoItem("Address", currentUser.address, true),
            PersonalInfoItem("Emergency contact", currentUser.emergencyContact, true)
        )

        // Set up RecyclerView
        adapter = PersonalInfoAdapter(items) { item ->
            val dialog = EditPersonalInfoDialogFragment(item.title, item.value) { newValue ->
                updateUserField(item.title, newValue)
            }
            dialog.show(supportFragmentManager, "EditDialog")
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(20)

        val goToMainActivity = findViewById<ImageView>(R.id.back_button)
        goToMainActivity.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun updateUserField(title: String, newValue: String) {
        val user = (application as MyApplication).currentUser ?: return
        val userRef = FirebaseFirestore.getInstance().collection("users").document(user.id)

        when (title) {
            "Legal name" -> {
                val (first, last) = newValue.split(" ", limit = 2) + listOf("")
                userRef.update(mapOf("firstName" to first, "lastName" to last))
                user.firstName = first
                user.lastName = last
            }
            "Preferred first name" -> {
                userRef.update("firstName", newValue)
                user.firstName = newValue
            }
            "Phone number" -> {
                userRef.update("phoneNumber", newValue)
                user.phoneNumber = newValue
            }
            "Email" -> {
                userRef.update("email", newValue)
                user.email = newValue
            }
            "Address" -> {
                userRef.update("address", newValue)
                user.address = newValue
            }
            "Emergency contact" -> {
                userRef.update("emergencyContact", newValue)
                user.emergencyContact = newValue
            }
        }

        // Update the RecyclerView item
        items.find { it.title == title }?.let {
            it.value = newValue
            adapter.notifyDataSetChanged()
        }
    }
}
