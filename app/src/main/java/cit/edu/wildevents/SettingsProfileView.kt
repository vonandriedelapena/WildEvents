package cit.edu.wildevents

// At the top of your file
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cit.edu.wildevents.app.MyApplication
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class SettingsProfileView : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_profile_view)

        // Firebase init
        db = FirebaseFirestore.getInstance()

        val currentUser = (application as MyApplication).currentUser
        currentUser?.let {
            val userId = currentUser.id
            fetchUserProfile(userId)
            fetchJoinedEvents(userId)
        }

        val goToMainActivity = findViewById<ImageView>(R.id.back_button)
        goToMainActivity.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun fetchUserProfile(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val firstName = document.getString("firstName")
                    val phone = document.getString("phoneNumber")
                    val address = document.getString("address")
                    val profilePic = document.getString("profilePic")
                    val isHost = document.getBoolean("isHost")

                    // Example: set these to TextViews (you need to define them in your layout)
                    findViewById<TextView>(R.id.profile_name).text = "$firstName"
                    findViewById<TextView>(R.id.profile_role).text = if (isHost!!) "Host" else "Student"
                    findViewById<TextView>(R.id.profile_id).text = "Lives in $address"

                    // Set image via cloudinary
                    val profileImageView = findViewById<ImageView>(R.id.profile_image)
                    if (!profilePic.isNullOrEmpty() && profilePic != "default.png") {

                        Glide.with(this)
                            .load(profilePic)
                            .placeholder(R.drawable.ic_placeholder)
                            .error(R.drawable.ic_add)
                            .into(profileImageView)
                    } else {
                        profileImageView.setImageResource(R.drawable.ic_placeholder)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error fetching user profile", e)
            }
    }
    private fun fetchJoinedEvents(userId: String) {
        db.collection("attendees")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { attendeeDocs ->
                val eventIds = attendeeDocs.mapNotNull { it.getString("eventId") }

                if (eventIds.isNotEmpty()) {
                    db.collection("events")
                        .whereIn(FieldPath.documentId(), eventIds)
                        .get()
                        .addOnSuccessListener { eventDocs ->
                            val joinedEvents = eventDocs.map { doc ->
                                doc.getString("eventName") ?: "Unnamed Event"
                            }

                            // Example: set these to a TextView or RecyclerView (you need to define them in your layout)
                        }
                        .addOnFailureListener {
                            Log.e("Firebase", "Failed to get events", it)
                        }
                }
            }
            .addOnFailureListener {
                Log.e("Firebase", "Failed to get attendee records", it)
            }
    }
}