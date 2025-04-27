package cit.edu.wildevents

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.Typeface.BOLD
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import cit.edu.wildevents.app.MyApplication
import com.google.firebase.firestore.FieldPath

class EventDetailActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var hostTextView: TextView
    private lateinit var hostImageView: ImageView
    private lateinit var joinButton: Button
    private lateinit var attendeesLayout: LinearLayout
    private var attendeeDocId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.event_details)

        // Set toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // View references
        imageView = findViewById(R.id.event_detail_image)
        titleTextView = findViewById(R.id.event_detail_title)
        descriptionTextView = findViewById(R.id.event_detail_description)
        dateTextView = findViewById(R.id.event_date)
        timeTextView = findViewById(R.id.event_time)
        locationTextView = findViewById(R.id.event_location)
        hostTextView = findViewById(R.id.event_hostName)
        hostImageView = findViewById(R.id.event_host_pic)
        joinButton = findViewById(R.id.joinBtn)
        attendeesLayout = findViewById(R.id.attendeesLayout)

        // Get data from Intent
        val eventName = intent.getStringExtra("eventName")
        val description = intent.getStringExtra("description")
        val startTime = intent.getLongExtra("startTime", -1)
        val endTime = intent.getLongExtra("endTime", -1)
        val location = intent.getStringExtra("location")
        val hostId = intent.getStringExtra("hostId")
        val imageUrl = intent.getStringExtra("imageUrl")
        val eventId = intent.getStringExtra("eventId")
        val currentUser = (application as MyApplication).currentUser

        // Bind data
        titleTextView.text = eventName
        descriptionTextView.text = description
        locationTextView.text = location

        // Fetch and display host details
        if (!hostId.isNullOrEmpty()) {
            FirebaseFirestore.getInstance()
                .collection("users") // Assuming your users collection is called "users"
                .document(hostId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firstName = document.getString("firstName") ?: ""
                        val lastName = document.getString("lastName") ?: ""
                        val profilePic = document.getString("profilePic") ?: "default.png"

                        hostTextView.text = "$firstName $lastName"

                        // Load profile pic
                        if (profilePic.startsWith("http")) {
                            Glide.with(this)
                                .load(profilePic)
                                .placeholder(R.drawable.ic_user) // your fallback
                                .circleCrop()
                                .into(hostImageView)
                        } else {
                            // Load local/default image
                            val resId = resources.getIdentifier(
                                profilePic.substringBeforeLast('.'),
                                "drawable",
                                packageName
                            )
                            if (resId != 0) {
                                hostImageView.setImageResource(resId)
                            } else {
                                hostImageView.setImageResource(R.drawable.ic_user)
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load host info", Toast.LENGTH_SHORT).show()
                }
        }

        if (eventId != null && currentUser != null) {
            FirebaseFirestore.getInstance()
                .collection("attendee")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", currentUser.id)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        attendeeDocId = documents.documents[0].id
                        setButtonToGoing()
                    } else {
                        setButtonToJoin()
                    }
                }
        }

        // Format and display date/time
        if (startTime > 0 && endTime > 0) {
            val dateFormatter = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
            val timeFormatter = SimpleDateFormat("h:mma", Locale.getDefault())
            dateTextView.text = dateFormatter.format(Date(startTime))
            timeTextView.text = "${timeFormatter.format(Date(startTime))} - ${timeFormatter.format(Date(endTime))}"
        }

        // Load image
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.placeholder_image)
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("attendee")
            .whereEqualTo("eventId", eventId)
            .get()
            .addOnSuccessListener { attendeeDocuments ->
                val userIds = attendeeDocuments.mapNotNull { it.getString("userId") }

                if (userIds.isEmpty()) return@addOnSuccessListener

                // Now fetch users' profile pics
                db.collection("users")
                    .whereIn(FieldPath.documentId(), userIds.take(10)) // Firestore 'whereIn' limit is 10
                    .get()
                    .addOnSuccessListener { userDocuments ->
                        val profilePics = userDocuments.mapNotNull { it.getString("profilePic") }
                        displayAttendees(profilePics, totalCount = userIds.size)
                    }
            }


        // Button action
        joinButton.setOnClickListener {
            if (eventId != null && currentUser != null) {
                val db = FirebaseFirestore.getInstance()

                if (attendeeDocId == null) {
                    // Not attending → Join
                    val attendeeData = mapOf(
                        "eventId" to eventId,
                        "userId" to currentUser.id,
                    )

                    db.collection("attendee")
                        .add(attendeeData)
                        .addOnSuccessListener { docRef ->
                            attendeeDocId = docRef.id
                            Toast.makeText(this, "You joined the event!", Toast.LENGTH_SHORT).show()
                            setButtonToGoing()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to join event.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Already attending → Confirm cancel
                    AlertDialog.Builder(this)
                        .setTitle("Leave Event?")
                        .setMessage("Are you sure you want to cancel your RSVP?")
                        .setPositiveButton("Yes") { _, _ ->
                            db.collection("attendee")
                                .document(attendeeDocId!!)
                                .delete()
                                .addOnSuccessListener {
                                    attendeeDocId = null
                                    Toast.makeText(this, "You left the event.", Toast.LENGTH_SHORT).show()
                                    setButtonToJoin()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Failed to leave event.", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .setNegativeButton("No", null)
                        .show()
                }
            } else {
                Toast.makeText(this, "Missing user or event info.", Toast.LENGTH_SHORT).show()
            }
        }


    }

    fun setButtonToGoing() {
        joinButton.text = "Going"
        joinButton.setBackgroundColor(getColor(android.R.color.holo_green_dark))
    }

    fun setButtonToJoin() {
        joinButton.text = "Join Now"
        joinButton.setBackgroundColor(Color.parseColor("#333333")) // properly parsed hex color
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun displayAttendees(profilePics: List<String>, totalCount: Int) {
        attendeesLayout.removeAllViews()

        val maxVisiblePfps = 3
        val imageSize = 80
        val overlapMargin = -30 // adjust this for more or less overlap

        val actualCount = profilePics.size

        for ((index, picUrl) in profilePics.withIndex()) {
            if (index < maxVisiblePfps) {
                val imageView = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(imageSize, imageSize).apply {
                        if (index != 0) {
                            marginStart = overlapMargin
                        }
                        setPadding(4)
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    background = ContextCompat.getDrawable(this@EventDetailActivity, R.drawable.background_circle)
                    clipToOutline = true
                }

                Glide.with(this)
                    .load(picUrl)
                    .placeholder(R.drawable.ic_user)
                    .circleCrop()
                    .into(imageView)

                attendeesLayout.addView(imageView)
            }
        }

        // 👉 Only show the "+N" if there are more attendees than max visible
        if (totalCount > maxVisiblePfps && actualCount > maxVisiblePfps) {
            val extraCount = totalCount - maxVisiblePfps
            if (extraCount > 0) {
                val textView = TextView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(imageSize, imageSize).apply {
                        marginStart = overlapMargin
                    }
                    text = "+$extraCount"
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    gravity = Gravity.CENTER
                    background = ContextCompat.getDrawable(this@EventDetailActivity, R.drawable.circle_black_with_white_border)
                    setTextColor(Color.WHITE)
                    setTypeface(null, Typeface.BOLD)
                }
                attendeesLayout.addView(textView)
            }
        }
    }


}
