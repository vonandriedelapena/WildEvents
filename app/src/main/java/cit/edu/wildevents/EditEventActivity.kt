package cit.edu.wildevents

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.Timestamp
class EditEventActivity : AppCompatActivity() {

    private lateinit var coverImage: ImageView
    private lateinit var pickImagesBtn: ImageButton
    private lateinit var eventNameInput: EditText
    private lateinit var eventLocationInput: EditText
    private lateinit var eventDateAndTimeLayout: LinearLayout
    private lateinit var eventDateText: TextView
    private lateinit var eventTimeText: TextView
    private lateinit var eventDescriptionInput: EditText
    private lateinit var tagFlexbox: FlexboxLayout
    private lateinit var eventCapacityText: TextView
    private lateinit var requireApprovalSwitch: Switch
    private lateinit var updateEventBtn: Button

    private var imageUri: Uri? = null
    private var existingCoverImageUrl: String? = null

    private var eventDateTime: Calendar? = null
    private var startTime: Calendar? = null
    private var endTime: Calendar? = null
    private var capacity: Int? = null

    private val predefinedTags = listOf("General", "Engr", "Arch", "Cnahs", "CCS", "Crim", "Cmba")
    private val selectedTags = mutableListOf<String>()

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private lateinit var eventId: String
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_eventdesign)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize views
        coverImage = findViewById(R.id.coverImage)
        pickImagesBtn = findViewById(R.id.pickImagesBtn)
        eventNameInput = findViewById(R.id.eventNameInput)
        eventLocationInput = findViewById(R.id.eventLocationInput)
        eventDateAndTimeLayout = findViewById(R.id.eventTimeAndDate)
        eventDateText = findViewById(R.id.event_date)
        eventTimeText = findViewById(R.id.event_startendtime)
        eventDescriptionInput = findViewById(R.id.eventDescriptionInput)
        tagFlexbox = findViewById(R.id.tagFlexbox)
        eventCapacityText = findViewById(R.id.eventCapacity)
        requireApprovalSwitch = findViewById(R.id.requireApprovalSwitch)
        updateEventBtn = findViewById(R.id.saveEventBtn)

        db = FirebaseFirestore.getInstance()

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val selectedImageUri = result.data?.data
                imageUri = selectedImageUri
                coverImage.setImageURI(selectedImageUri)
            }
        }

        // Get the eventId passed from previous screen
        eventId = intent.getStringExtra("eventId") ?: throw IllegalArgumentException("EVENT_ID is required")
        Log.d("EditEventActivity", "onCreate called")

        pickImagesBtn.setOnClickListener { openImagePicker() }
        eventDateAndTimeLayout.setOnClickListener { showDateTimePicker() }
        updateEventBtn.setOnClickListener { updateEvent() }

        setupTags()
        setupCapacityOption()

        initConfig()
        loadEventData()
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

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun loadEventData() {
        db.collection("events").document(eventId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    eventNameInput.setText(document.getString("name"))
                    eventLocationInput.setText(document.getString("location"))
                    eventDescriptionInput.setText(document.getString("description"))
                    existingCoverImageUrl = document.getString("coverImageUrl")

                    document.get("tags")?.let { tags ->
                        if (tags is List<*>) {
                            selectedTags.addAll(tags.filterIsInstance<String>())
                            updateTagSelections()
                        }
                    }

                    val capacityValue = document.getLong("capacity")?.toInt()
                    if (capacityValue != null) {
                        capacity = capacityValue
                        eventCapacityText.text = capacity.toString()
                    } else {
                        eventCapacityText.text = "Unlimited"
                    }

                    requireApprovalSwitch.isChecked = document.getBoolean("requiresApproval") == true

                    val startTimestamp = document.getTimestamp("startTime")
                    val endTimestamp = document.getTimestamp("endTime")
                    if (startTimestamp != null && endTimestamp != null) {
                        startTime = Calendar.getInstance().apply { time = startTimestamp.toDate() }
                        endTime = Calendar.getInstance().apply { time = endTimestamp.toDate() }

                        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

                        eventDateText.text = dateFormat.format(startTime!!.time)
                        eventTimeText.text = "${timeFormat.format(startTime!!.time)} - ${timeFormat.format(endTime!!.time)}"
                    }

                    // Load the existing image if available
                    if (!existingCoverImageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(existingCoverImageUrl).into(coverImage)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load event: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateTagSelections() {
        for (i in 0 until tagFlexbox.childCount) {
            val checkBox = tagFlexbox.getChildAt(i) as? CheckBox
            checkBox?.isChecked = selectedTags.contains(checkBox?.text.toString())
        }
    }

    @SuppressLint("InflateParams")
    private fun showDateTimePicker() {
        // (Same showDateTimePicker() code as in CreateEventActivity)
        val dialogView = layoutInflater.inflate(R.layout.dialog_date_time_picker, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create()

        val btnPickDate = dialogView.findViewById<Button>(R.id.btnPickDate)
        val btnPickStartTime = dialogView.findViewById<Button>(R.id.btnPickStartTime)
        val btnPickEndTime = dialogView.findViewById<Button>(R.id.btnPickEndTime)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)

        val selectedDate = Calendar.getInstance()

        btnPickDate.setOnClickListener {
            val now = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, day)
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnPickStartTime.setOnClickListener {
            val now = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                startTime = Calendar.getInstance().apply {
                    set(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH), hour, minute)
                }
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show()
        }

        btnPickEndTime.setOnClickListener {
            val now = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                endTime = Calendar.getInstance().apply {
                    set(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH), hour, minute)
                }
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            if (startTime != null && endTime != null) {
                val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

                eventDateText.text = dateFormat.format(startTime!!.time)
                eventTimeText.text = "${timeFormat.format(startTime!!.time)} - ${timeFormat.format(endTime!!.time)}"
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please select both start and end time", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun setupTags() {
        tagFlexbox.removeAllViews()
        predefinedTags.forEach { tag ->
            val checkBox = CheckBox(this).apply {
                text = tag
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedTags.add(tag)
                    else selectedTags.remove(tag)
                }
            }
            tagFlexbox.addView(checkBox)
        }
    }

    private fun setupCapacityOption() {
        findViewById<LinearLayout>(R.id.layoutCapacity).setOnClickListener {
            val input = EditText(this).apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                hint = "Enter max capacity"
            }

            val dialog = AlertDialog.Builder(this)
                .setTitle("Set Capacity")
                .setView(input)
                .setPositiveButton("Set") { _, _ ->
                    val text = input.text.toString()
                    if (text.isNotEmpty()) {
                        capacity = text.toInt()
                        eventCapacityText.text = text
                    }
                }
                .setNegativeButton("Remove Limit") { _, _ ->
                    capacity = null
                    eventCapacityText.text = "Unlimited"
                }
                .create()

            dialog.show()
        }
    }

    private fun updateEvent() {
        val eventName = eventNameInput.text.toString().trim()
        val location = eventLocationInput.text.toString().trim()
        val description = eventDescriptionInput.text.toString().trim()

        if (eventName.isEmpty() || location.isEmpty() || startTime == null || endTime == null) {
            Toast.makeText(this, "Please complete all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val eventMap = hashMapOf<String, Any?>(
            "name" to eventName,
            "location" to location,
            "description" to description,
            "tags" to selectedTags,
            "startTime" to Timestamp(startTime!!.time),
            "endTime" to Timestamp(endTime!!.time),
            "capacity" to capacity,
            "requiresApproval" to requireApprovalSwitch.isChecked
        )

        if (imageUri == null && existingCoverImageUrl != null) {
            saveUpdatedEvent(eventMap)
        } else if (imageUri != null) {
            MediaManager.get().upload(imageUri)
                .option("folder", "event_covers/")
                .option("resource_type", "image")
                .callback(object : com.cloudinary.android.callback.UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                        val imageUrl = resultData?.get("secure_url") as? String
                        eventMap["coverImageUrl"] = imageUrl
                        saveUpdatedEvent(eventMap)
                    }
                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Toast.makeText(this@EditEventActivity, "Image upload failed", Toast.LENGTH_SHORT).show()
                    }
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                })
                .dispatch()
        } else {
            saveUpdatedEvent(eventMap)
        }
    }

    private fun saveUpdatedEvent(eventMap: HashMap<String, Any?>) {
        db.collection("events").document(eventId).update(eventMap as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show()
                // After updating the event in Firestore
                val updatedIntent = Intent()
                updatedIntent.putExtra("eventId", eventId) // Pass the eventId
                updatedIntent.putExtra("eventName", eventNameInput.text.toString())
                updatedIntent.putExtra("description", eventDescriptionInput.text.toString())
                updatedIntent.putExtra("startTime", startTime?.time)
                updatedIntent.putExtra("endTime", endTime?.time)
                updatedIntent.putExtra("location", eventLocationInput.text.toString())
                updatedIntent.putExtra("imageUrl", existingCoverImageUrl.toString())

                setResult(Activity.RESULT_OK, updatedIntent)
                finish()

            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update event", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
