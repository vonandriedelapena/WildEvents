package cit.edu.wildevents

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildevents.app.MyApplication
import cit.edu.wildevents.data.Comment
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query

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
    private lateinit var participantsContainer: LinearLayout // You already have this
    private var attendeeDocId: String? = null
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentsRecyclerView: RecyclerView

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
        participantsContainer = findViewById(R.id.participants_container)

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

        // Button action
        joinButton.setOnClickListener {
            val isHost = currentUser?.isHost == true  && currentUser.id == hostId
            Log.d("EventDetailActivity", "isHost: $isHost")
            if (isHost) {
                setButtonToEdit()
                joinButton.setOnClickListener {
                    val intent = Intent(this, EditEventActivity::class.java)
                    intent.putExtra("eventId", eventId)
                    startActivity(intent)
                }
            } else if (eventId != null && currentUser != null) {
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

        commentsRecyclerView = findViewById(R.id.comments_recycler_view)
        val commentInput = findViewById<EditText>(R.id.comment_input)
        val postCommentButton = findViewById<Button>(R.id.post_comment_button)

        commentAdapter = CommentAdapter(mutableListOf())
        commentsRecyclerView.adapter = commentAdapter
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)

        loadCommentsForEvent(eventId)

        postCommentButton.setOnClickListener {
            val content = commentInput.text.toString().trim()
            if (content.isNotEmpty()) {
                postComment(content)
            }
        }
    }
    private fun postComment(content: String) {
        val currentUser = (application as MyApplication).currentUser
        val eventId = intent.getStringExtra("eventId")

        if (currentUser != null && eventId != null) {
            val commentData = mapOf(
                "eventId" to eventId,
                "userId" to currentUser.id,
                "userName" to currentUser.firstName,
                "userAvatarUrl" to currentUser.profilePic,
                "content" to content,
                "timestamp" to FieldValue.serverTimestamp()
            )

            FirebaseFirestore.getInstance()
                .collection("comments")
                .add(commentData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Comment posted successfully!", Toast.LENGTH_SHORT).show()
                    loadCommentsForEvent(eventId) // Refresh comments
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to post comment: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Unable to post comment. Missing user or event information.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun loadCommentsForEvent(eventId: String?) {
        val db = FirebaseFirestore.getInstance()
        db.collection("comments")
            .whereEqualTo("eventId", eventId)
            .get()
            .addOnSuccessListener { result ->
                val comments = result.mapNotNull { doc ->
                    val timestamp = doc.getTimestamp("timestamp")?.toDate()?.time
                    if (timestamp != null) {
                        Comment(
                            id = doc.id,
                            userName = doc.getString("userName") ?: "Unknown",
                            userAvatarUrl = doc.getString("userAvatarUrl"),
                            content = doc.getString("content") ?: "",
                            timestamp = timestamp
                        )
                    } else null // exclude comments without valid timestamps
                }.sortedBy { it.timestamp }

                commentAdapter.updateComments(comments)
                commentsRecyclerView.scrollToPosition(comments.size - 1)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading comments: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun setButtonToGoing() {
        joinButton.text = "Going"
        joinButton.setBackgroundColor(getColor(android.R.color.holo_green_dark))
    }

    fun setButtonToJoin() {
        joinButton.text = "Join Event"
        joinButton.setBackgroundColor(Color.parseColor("#333333")) // properly parsed hex color
    }

    fun setButtonToEdit() {
        joinButton.text = "Edit Event"
        joinButton.setBackgroundColor(Color.parseColor("#FFA500")) // Orange color for edit
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}
