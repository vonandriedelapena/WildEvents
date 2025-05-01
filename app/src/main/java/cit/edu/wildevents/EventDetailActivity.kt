package cit.edu.wildevents

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.Typeface.BOLD
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import cit.edu.wildevents.app.MyApplication
import com.google.firebase.firestore.FieldPath
import cit.edu.wildevents.data.Comment
import cit.edu.wildevents.data.User
import cit.edu.wildevents.services.EventReminderWorker
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.util.concurrent.TimeUnit

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
    private lateinit var participantsContainer: LinearLayout
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter

    private lateinit var attendeesLayout: LinearLayout
    private var attendeeDocId: String? = null
    private var eventId: String? = null
    private var currentUser: User? = null

    private lateinit var editEventLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.event_details)

        initViews()

        eventId = intent.getStringExtra("eventId")
        currentUser = (application as MyApplication).currentUser

        setupEditEventLauncher()

        loadEventDetails()
        setupCommentsSection()
    }

    private fun initViews() {
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
        commentsRecyclerView = findViewById(R.id.comments_recycler_view)
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupEditEventLauncher() {
        editEventLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val updatedEventId = result.data?.getStringExtra("eventId") ?: eventId
                val updatedEventName = result.data?.getStringExtra("eventName")
                val updatedDescription = result.data?.getStringExtra("description")
                val updatedStartTime = result.data?.getLongExtra("startTime", -1L) ?: -1L
                val updatedEndTime = result.data?.getLongExtra("endTime", -1L) ?: -1L
                val updatedLocation = result.data?.getStringExtra("location")
                val updatedImageUrl = result.data?.getStringExtra("imageUrl")

                // Now update the UI with the new details
                if (updatedEventId != null) {
                    eventId = updatedEventId
                }
                loadUpdatedEventDetails(updatedEventName, updatedDescription, updatedStartTime, updatedEndTime, updatedLocation, updatedImageUrl)
            }
        }
    }

    private fun loadUpdatedEventDetails(eventName: String?, description: String?, startTime: Long, endTime: Long, location: String?, imageUrl: String?) {
        // Schedule reminders for the updated event
        scheduleEventReminders(
            context = this,
            eventTitle = eventName ?: "Event",
            eventTimeInMillis = startTime
        )

        titleTextView.text = eventName
        descriptionTextView.text = description
        locationTextView.text = location
        formatDateTime(startTime, endTime)
        loadEventImage(imageUrl)

        // Optionally, you could also re-fetch the host info and participants after an event update
        // Re-fetch and show Host Info if needed
        if (!eventId.isNullOrEmpty()) {
            loadHostInfo(eventId!!)
        }

        // Load Comments
        loadCommentsForEvent(eventId)
    }


    private fun loadEventDetails() {
        if (eventId == null || currentUser == null) {
            Toast.makeText(this, "Missing event or user info.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Get Intent Extras
        val extras = intent.extras
        val eventName = extras?.getString("eventName")
        val description = extras?.getString("description")
        val startTime = extras?.getLong("startTime") ?: -1
        val endTime = extras?.getLong("endTime") ?: -1
        val location = extras?.getString("location")
        val hostId = extras?.getString("hostId")
        val imageUrl = extras?.getString("imageUrl")

        // Populate Basic Info
        titleTextView.text = eventName
        descriptionTextView.text = description
        locationTextView.text = location
        formatDateTime(startTime, endTime)
        loadEventImage(imageUrl)

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
                        //displayAttendees(profilePics, totalCount = userIds.size)
                    }
            }

        // Fetch and show Host Info
        if (!hostId.isNullOrEmpty()) {
            loadHostInfo(hostId)
        }

        // Setup Button
        setupJoinOrEditButton(hostId)

        // Load Comments
        loadCommentsForEvent(eventId)
    }

    private fun loadHostInfo(hostId: String) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(hostId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val profilePic = document.getString("profilePic") ?: "default.png"
                    hostTextView.text = "$firstName $lastName"

                    loadImageIntoView(profilePic, hostImageView, R.drawable.ic_user)
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

    private fun loadImageIntoView(url: String, imageView: ImageView, defaultResId: Int) {
        if (url.startsWith("http")) {
            Glide.with(this).load(url).placeholder(defaultResId).into(imageView)
        } else {
            val resId = resources.getIdentifier(url.substringBeforeLast('.'), "drawable", packageName)
            imageView.setImageResource(if (resId != 0) resId else defaultResId)
        }
    }

    private fun formatDateTime(start: Long, end: Long) {
        if (start > 0 && end > 0) {
            val dateFormatter = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
            val timeFormatter = SimpleDateFormat("h:mma", Locale.getDefault())
            dateTextView.text = dateFormatter.format(Date(start))
            timeTextView.text = "${timeFormatter.format(Date(start))} - ${timeFormatter.format(Date(end))}"
        }
    }

    private fun loadEventImage(imageUrl: String?) {
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.placeholder_image)
        }
    }

    private fun setupJoinOrEditButton(hostId: String?) {
        if (currentUser!!.isHost && currentUser!!.id == hostId) {
            setButtonToEdit()
        } else {
            FirebaseFirestore.getInstance()
                .collection("attendee")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", currentUser!!.id)
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


        // Button action
        joinButton.setOnClickListener {
            if (currentUser!!.isHost && currentUser!!.id == hostId) {
                val intent = Intent(this, EditEventActivity::class.java)
                intent.putExtra("eventId", eventId)
                editEventLauncher.launch(intent)
            } else {
                handleJoinOrLeaveEvent()
            }
        }
    }

    private fun handleJoinOrLeaveEvent() {
        val db = FirebaseFirestore.getInstance()

        if (attendeeDocId == null) {
            val attendeeData = mapOf(
                "eventId" to eventId,
                "userId" to currentUser!!.id
            )

            db.collection("attendee")
                .add(attendeeData)
                .addOnSuccessListener { docRef ->
                    attendeeDocId = docRef.id
                    Toast.makeText(this, "You joined the event!", Toast.LENGTH_SHORT).show()
                    setButtonToGoing()

                    // Schedule reminders for the event
                    scheduleEventReminders(
                        context = this,
                        eventTitle = titleTextView.text.toString(),
                        eventTimeInMillis = intent.getLongExtra("startTime", -1L)
                    )
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to join event.", Toast.LENGTH_SHORT).show()
                }
        } else {
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

                            WorkManager.getInstance(this).cancelAllWorkByTag(eventId!!)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to leave event.", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun setupCommentsSection() {
        commentAdapter = CommentAdapter(mutableListOf())
        commentsRecyclerView.adapter = commentAdapter
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.post_comment_button).setOnClickListener {
            val commentInput = findViewById<EditText>(R.id.comment_input)
            val content = commentInput.text.toString().trim()
            if (content.isNotEmpty()) {
                postComment(content)
            }
        }
    }

    private fun postComment(content: String) {
        if (currentUser != null && eventId != null) {
            val commentData = mapOf(
                "eventId" to eventId,
                "userId" to currentUser!!.id,
                "userName" to currentUser!!.firstName,
                "userAvatarUrl" to currentUser!!.profilePic,
                "content" to content,
                "timestamp" to FieldValue.serverTimestamp()
            )

            FirebaseFirestore.getInstance()
                .collection("comments")
                .add(commentData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Comment posted!", Toast.LENGTH_SHORT).show()
                    loadCommentsForEvent(eventId)
                    val commentInput = findViewById<EditText>(R.id.comment_input)
                    commentInput.text.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to post comment: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadCommentsForEvent(eventId: String?) {
        FirebaseFirestore.getInstance()
            .collection("comments")
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
                    } else null
                }.sortedBy { it.timestamp }

                commentAdapter.updateComments(comments)
                commentsRecyclerView.scrollToPosition(comments.size - 1)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading comments: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setButtonToGoing() {
        joinButton.text = "Going"
        joinButton.setBackgroundColor(Color.parseColor("#388E3C"))
    }

    private fun setButtonToJoin() {
        joinButton.text = "Join Event"
        joinButton.setBackgroundColor(Color.parseColor("#333333"))
    }

    private fun setButtonToEdit() {
        joinButton.text = "Edit Event"
        joinButton.setBackgroundColor(Color.parseColor("#D4AF37"))
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

    private fun scheduleEventReminders(
        context: Context,
        eventTitle: String,
        eventTimeInMillis: Long
    ) {
        val workManager = WorkManager.getInstance(context)

        // --- Helper Function ---
        fun scheduleReminder(delayMillis: Long, message: String) {
            val data = Data.Builder()
                .putString("eventTitle", eventTitle)
                .putString("message", message)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<EventReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(eventId!!)
                .build()

            workManager.enqueue(workRequest)
        }

        val now = System.currentTimeMillis()

        val timeUntilEvent = eventTimeInMillis - now

        if (timeUntilEvent > 0) {
            if (timeUntilEvent > 7 * 24 * 60 * 60 * 1000) { // More than a week left
                scheduleReminder(timeUntilEvent - 7 * 24 * 60 * 60 * 1000, "Your event '$eventTitle' is 1 week away!")
            }
            if (timeUntilEvent > 24 * 60 * 60 * 1000) { // More than a day left
                scheduleReminder(timeUntilEvent - 24 * 60 * 60 * 1000, "Reminder: '$eventTitle' is tomorrow!")
            }
            if (timeUntilEvent > 60 * 60 * 1000) { // More than an hour left
                scheduleReminder(timeUntilEvent - 60 * 60 * 1000, "Get ready! '$eventTitle' is starting in 1 hour.")
                Log.d("Reminders", "Reminder set for 1 hour before the event.")
            }
        }
    }
}
