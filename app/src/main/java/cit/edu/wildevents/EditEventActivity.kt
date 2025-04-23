package cit.edu.wildevents

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class EditEventActivity : AppCompatActivity() {

    private lateinit var eventNameInput: EditText
    private lateinit var eventDescriptionInput: EditText
    private lateinit var eventDateTextView: TextView
    private lateinit var eventTimeTextView: TextView
    private lateinit var eventLocationInput: EditText
    private lateinit var saveEventButton: Button

    private val calendar = Calendar.getInstance()
    private var eventId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_eventdesign)

        // Initialize UI elements
        eventNameInput = findViewById(R.id.eventNameInput)
        eventDescriptionInput = findViewById(R.id.eventDescriptionInput)
        eventDateTextView = findViewById(R.id.event_date)
        eventTimeTextView = findViewById(R.id.event_startendtime)
        eventLocationInput = findViewById(R.id.eventLocationInput)
        saveEventButton = findViewById(R.id.createEventBtn)

        // Update button text for editing
        saveEventButton.text = "Save Changes"

        // Get event ID from intent
        eventId = intent.getStringExtra("eventId")

        // Load existing event details if editing
        loadEventDetails()

        // Set up date picker
        eventDateTextView.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    updateDateText()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Set up time picker
        eventTimeTextView.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    updateTimeText()
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        // Save button click listener
        saveEventButton.setOnClickListener {
            saveEventDetails()
        }
    }

    private fun loadEventDetails() {
        if (eventId != null) {
            FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId!!)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        eventNameInput.setText(document.getString("title"))
                        eventDescriptionInput.setText(document.getString("description"))
                        eventLocationInput.setText(document.getString("location"))

                        val timestamp = document.getLong("startTime") ?: 0L
                        calendar.timeInMillis = timestamp
                        updateDateText()
                        updateTimeText()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load event details.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateDateText() {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        eventDateTextView.text = dateFormat.format(calendar.time)
    }

    private fun updateTimeText() {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        eventTimeTextView.text = timeFormat.format(calendar.time)
    }

    private fun saveEventDetails() {
        val title = eventNameInput.text.toString().trim()
        val description = eventDescriptionInput.text.toString().trim()
        val location = eventLocationInput.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
            return
        }

        val eventDetails = mapOf(
            "title" to title,
            "description" to description,
            "location" to location,
            "startTime" to calendar.timeInMillis
        )

        if (eventId != null) {
            FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId!!)
                .update(eventDetails)
                .addOnSuccessListener {
                    Toast.makeText(this, "Event updated successfully.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update event.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}