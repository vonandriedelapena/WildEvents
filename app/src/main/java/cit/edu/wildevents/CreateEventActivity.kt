package cit.edu.wildevents

import android.annotation.SuppressLint
import android.app.AlarmManager
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import cit.edu.wildevents.app.MyApplication
import cit.edu.wildevents.data.Event
import cit.edu.wildevents.helper.EventsViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.work.OneTimeWorkRequestBuilder
import cit.edu.wildevents.services.EventReminderWorker


class CreateEventActivity : AppCompatActivity() {

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
    private lateinit var createEventBtn: Button

    private var imageUri: Uri? = null
    private var eventDateTime: Calendar? = null
    private var startTime: Calendar? = null
    private var endTime: Calendar? = null
    private var capacity: Int? = null

    private val predefinedTags = listOf("General", "Engr", "Arch", "Cnahs", "CCS", "Crim","Cmba")
    private val selectedTags = mutableListOf<String>()

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_eventdesign)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
        createEventBtn = findViewById(R.id.createEventBtn)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val selectedImageUri = result.data?.data
                imageUri = selectedImageUri
                coverImage.setImageURI(selectedImageUri)
                Log.d("CreateEventActivity", "Image selected: $selectedImageUri")
            }
        }

        pickImagesBtn.setOnClickListener { openImagePicker() }
        eventDateAndTimeLayout.setOnClickListener { showDateTimePicker() }
        createEventBtn.setOnClickListener { createEvent() }

        setupTags()
        setupCapacityOption()
        initConfig();
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

    @SuppressLint("InflateParams")
    private fun showDateTimePicker() {
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
                eventDateTime = startTime
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

    private fun createEvent() {
        val eventName = eventNameInput.text.toString().trim()
        val location = eventLocationInput.text.toString().trim()
        val description = eventDescriptionInput.text.toString().trim()

        if (eventName.isEmpty() || location.isEmpty() || eventDateTime == null || startTime == null || endTime == null) {
            Toast.makeText(this, "Please complete all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val user = (application as MyApplication).currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        val eventId = db.collection("events").document().id

        val eventMap = hashMapOf(
            "id" to eventId,
            "name" to eventName,
            "location" to location,
            "description" to description,
            "tags" to selectedTags,
            "hostId" to user.id,
            "startTime" to Timestamp(startTime!!.time),
            "endTime" to Timestamp(endTime!!.time),
            "capacity" to capacity,
            "requiresApproval" to requireApprovalSwitch.isChecked
        )

        val imageUri = this.imageUri
        if (imageUri == null) {
            Log.d("CreateEventActivity", "No image selected. Saving event without image.")
            saveEventToFirestore(db, eventId, eventMap)
        } else {
            MediaManager.get().upload(imageUri)
                .option("folder", "event_covers/")
                .option("resource_type", "image")
                .callback(object : com.cloudinary.android.callback.UploadCallback {
                    override fun onStart(requestId: String?) {
                        Log.d("CreateEventActivity", "Cloudinary upload started: $requestId")
                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                        Log.d("CreateEventActivity", "Upload progress: $bytes / $totalBytes")
                    }

                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                        val imageUrl = resultData?.get("secure_url") as? String
                        Log.d("CreateEventActivity", "Cloudinary upload successful: $imageUrl")
                        eventMap["coverImageUrl"] = imageUrl
                        saveEventToFirestore(db, eventId, eventMap)
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Log.e("CreateEventActivity", "Cloudinary upload failed: ${error?.description}")
                        Toast.makeText(this@CreateEventActivity, "Upload failed: ${error?.description}", Toast.LENGTH_LONG).show()
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        Log.w("CreateEventActivity", "Cloudinary upload rescheduled: ${error?.description}")
                        Toast.makeText(this@CreateEventActivity, "Upload rescheduled: ${error?.description}", Toast.LENGTH_SHORT).show()
                    }

                })
                .dispatch()
        }

        // Check and request exact alarm permission
        checkAndRequestExactAlarmPermission()

        // Schedule a reminder 1 hour before the event
        scheduleNotification(startTime!!, eventName)
    }


    private fun saveEventToFirestore(db: FirebaseFirestore, eventId: String, eventMap: HashMap<String, Any?>) {
        db.collection("events").document(eventId).set(eventMap)
            .addOnSuccessListener {
                val event = Event(
                    eventId = eventId,
                    eventName = eventMap["name"] as String,
                    startTime = (eventMap["startTime"] as Timestamp).toDate().time,
                    endTime = (eventMap["endTime"] as Timestamp).toDate().time,
                    location = eventMap["location"] as String,
                    description = eventMap["description"] as String,
                    imageUrl = eventMap["coverImageUrl"] as? String,
                    hostId = eventMap["hostId"] as String,
                    tags = eventMap["tags"] as List<String>,
                    capacity = eventMap["capacity"] as? Int
                )

                // ✅ Automatically RSVP the host (creator)
                val user = (application as MyApplication).currentUser
                if (user != null) {
                    val attendeeData = mapOf(
                        "eventId" to eventId,
                        "userId" to user.id,
                        "timestamp" to Timestamp.now()
                    )
                    db.collection("attendee").add(attendeeData)
                        .addOnSuccessListener {
                            Log.d("CreateEventActivity", "Host RSVP saved in attendee")
                        }
                        .addOnFailureListener {
                            Log.w("CreateEventActivity", "Failed to RSVP host: ${it.message}")
                        }
                }

                // Schedule reminders for the event
                scheduleEventReminders(
                    context = this,
                    eventTitle = eventNameInput.text.toString(),
                    eventTimeInMillis =  startTime!!.timeInMillis// Assuming eventDate is a Date object
                )

                // Add to local ViewModel
                val viewModel = ViewModelProvider(this@CreateEventActivity)[EventsViewModel::class.java]
                viewModel.addEvent(event)

                Toast.makeText(this, "Event created successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create event: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun scheduleNotification(startTime: Calendar, eventName: String) {
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("title", "Upcoming Event")
            putExtra("message", "$eventName is starting soon!")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            startTime.timeInMillis.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            startTime.timeInMillis - 60 * 60 * 1000, // 10 minutes before event
            pendingIntent
        )
    }


    private fun checkAndRequestExactAlarmPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Guide the user to the app settings to enable the permission
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
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
