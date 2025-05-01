package cit.edu.wildevents

import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildevents.app.MyApplication
import cit.edu.wildevents.data.EventItemProfile
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.util.Locale

private const val REQUEST_CODE_PICK_IMAGE = 101

class SettingsProfileView : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_profile_view)

        initConfig()

        db = FirebaseFirestore.getInstance()

        val currentUser = (application as MyApplication).currentUser
        currentUser?.let {
            val userId = currentUser.id
            fetchUserProfile(userId)
            //fetchJoinedEvents(userId)
        }

        val goToMainActivity = findViewById<ImageView>(R.id.back_button)
        goToMainActivity.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val editProfileButton = findViewById<ImageView>(R.id.edit_profile_button)
        editProfileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
        }

        val editProfile = findViewById<TextView>(R.id.edit_profile)
        editProfile.setOnClickListener {
            val intent = Intent(this, SettingsPersonalInformation::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data
            if (selectedImageUri != null) {
                findViewById<ImageView>(R.id.profile_image).setImageURI(selectedImageUri)
                uploadImageToCloudinary(selectedImageUri)
            }
        }
    }

    private fun initConfig() {
        try {
            MediaManager.get()
        } catch (e: IllegalStateException) {
            val config = mapOf(
                "cloud_name" to "dms4vmxqk",
                "api_key" to "664212468912773",
                "api_secret" to "J-eXM5x5YLXcHk7LBQk0DYuXaiM",
                "secure" to true
            )
            MediaManager.init(this, config)
        }
    }

    private fun uploadImageToCloudinary(imageUri: Uri) {
        val userId = (application as MyApplication).currentUser?.id ?: return
        val filePath = getPathFromUri(imageUri)
        if (filePath == null) {
            Log.e("Cloudinary", "Invalid file path.")
            return
        }

        val file = File(filePath)
        MediaManager.get().upload(file.path)
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Log.d("Cloudinary", "Upload started")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    if (url != null) {
                        updateUserProfilePic(userId, url)
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("Cloudinary", "Upload error: ${error?.description}")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }

    private fun updateUserProfilePic(userId: String, imageUrl: String) {
        db.collection("users").document(userId)
            .update("profilePic", imageUrl)
            .addOnSuccessListener {
                Log.d("Firestore", "Profile picture updated successfully")
            }
            .addOnFailureListener {
                Log.e("Firestore", "Failed to update profile picture", it)
            }
        (application as MyApplication).currentUser?.profilePic = imageUrl
    }

    private fun getPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        return cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            it.getString(columnIndex)
        }
    }

    private fun fetchUserProfile(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val fullName = "$firstName $lastName"

                    val email = document.getString("email") ?: "No email provided"
                    val emergencyContact = document.getString("emergencyContact") ?: "Not set"
                    val address = document.getString("address") ?: "No address provided"
                    val phone = document.getString("phoneNumber") ?: "No phone number"
                    val profilePic = document.getString("profilePic")
                    val isHost = document.getBoolean("isHost")

                    findViewById<TextView>(R.id.profile_name).text = firstName
                    findViewById<TextView>(R.id.profile_name_full).text = fullName
                    findViewById<TextView>(R.id.profile_role).text = if (isHost == true) "Host" else "Student"
                    findViewById<TextView>(R.id.profile_email).text = email
                    findViewById<TextView>(R.id.profile_emergency).text = emergencyContact
                    findViewById<TextView>(R.id.profile_address).text = "Lives in $address"
                    findViewById<TextView>(R.id.profile_phone).text = phone

                    val profileImageView = findViewById<ImageView>(R.id.profile_image)
                    if (!profilePic.isNullOrEmpty() && profilePic != "default.png") {
                        Glide.with(this)
                            .load(profilePic)
                            .placeholder(R.drawable.ic_placeholder)
                            .error(R.drawable.ic_add)
                            .circleCrop()
                            .into(profileImageView)
                    } else {
                        profileImageView.setImageResource(R.drawable.ic_placeholder)
                    }

                    val eventsJoinedCount = findViewById<TextView>(R.id.events_joined_count)
                    db.collection("attendee")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener { attendeeDocs ->
                            val joinedCount = attendeeDocs.size()
                            eventsJoinedCount.text = joinedCount.toString()
                            Log.e("Firebase", "Joined events count: $joinedCount")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Error counting joined events", e)
                            eventsJoinedCount.text = "0"
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error fetching user profile", e)
            }
    }


    private fun fetchJoinedEvents(userId: String) {
        db.collection("attendee")
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
                                val timestamp = doc.getTimestamp("startTime")
                                val formattedDate = timestamp?.toDate()?.let {
                                    SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(it)
                                } ?: "No date"

                                EventItemProfile(
                                    id = doc.id,
                                    eventName = doc.getString("name") ?: "Unnamed",
                                    eventDescription = doc.getString("description") ?: "",
                                    eventDate = formattedDate,
                                    eventImage = doc.getString("coverImageUrl") ?: ""
                                )
                            }


                            val recyclerView = findViewById<RecyclerView>(R.id.events_recycler_view)
                            recyclerView.layoutManager = LinearLayoutManager(this)
                            recyclerView.adapter = EventAdapter(joinedEvents)
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
