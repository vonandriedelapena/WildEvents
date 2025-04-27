package cit.edu.wildevents.helper

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildevents.EventDetailActivity
import cit.edu.wildevents.R
import cit.edu.wildevents.data.Event
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class EventAdapter(private val context: Context) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private var events: List<Event> = emptyList()

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val attendeesLayout: LinearLayout = view.findViewById(R.id.attendeesLayout)
        val eventImage: ImageView = view.findViewById(R.id.event_image)
        val eventTitle: TextView = view.findViewById(R.id.event_title)
        val eventDate: TextView = view.findViewById(R.id.event_date)
        val eventDescription: TextView = view.findViewById(R.id.event_description)
        var joinButton: Button = view.findViewById(R.id.btnConfirm)
    }

    fun updateEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.event_list_item, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val db = FirebaseFirestore.getInstance()

        holder.eventTitle.text = event.eventName.trim()
        holder.eventDate.text = formatter.format(event.startTime)
        holder.eventDescription.text = event.location

        if (!event.imageUrl.isNullOrEmpty()) {
            Glide.with(context).load(event.imageUrl).into(holder.eventImage)
        } else {
            holder.eventImage.setImageResource(R.drawable.placeholder_image)
        }

        // Display attendees
        db.collection("attendee")
            .whereEqualTo("eventId", event.eventId)
            .get()
            .addOnSuccessListener { attendeeDocuments ->
                val userIds = attendeeDocuments.mapNotNull { it.getString("userId") }
                if (userIds.isEmpty()) return@addOnSuccessListener

                db.collection("users")
                    .whereIn(FieldPath.documentId(), userIds.take(10))
                    .get()
                    .addOnSuccessListener { userDocuments ->
                        val profilePics = userDocuments.mapNotNull { it.getString("profilePic") }
                        displayAttendees(holder.attendeesLayout, profilePics, totalCount = userIds.size)
                    }
            }

        val currentUser = (context.applicationContext as cit.edu.wildevents.app.MyApplication).currentUser

        if (currentUser != null) {
            db.collection("attendee")
                .whereEqualTo("eventId", event.eventId)
                .whereEqualTo("userId", currentUser.id)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // Already joined
                        holder.joinButton.text = "Going"
                        holder.joinButton.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
                        val attendeeDocId = documents.documents[0].id

                        holder.joinButton.setOnClickListener {
                            // Show confirmation dialog before leaving
                            AlertDialog.Builder(context)
                                .setTitle("Cancel RSVP")
                                .setMessage("Should I cancel your RSVP?")
                                .setPositiveButton("Yes") { _, _ ->
                                    db.collection("attendee")
                                        .document(attendeeDocId)
                                        .delete()
                                        .addOnSuccessListener {
                                            holder.joinButton.text = "Join Now"
                                            holder.joinButton.setBackgroundColor(Color.parseColor("#333333"))
                                            Toast.makeText(context, "You left the event.", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Failed to leave event.", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .setNegativeButton("No", null)
                                .show()
                        }
                    } else {
                        // Not yet joined
                        holder.joinButton.text = "Join Now"
                        holder.joinButton.setBackgroundColor(Color.parseColor("#333333"))

                        holder.joinButton.setOnClickListener {
                            // Join event immediately
                            val attendeeData = mapOf(
                                "eventId" to event.eventId,
                                "userId" to currentUser.id
                            )

                            db.collection("attendee")
                                .add(attendeeData)
                                .addOnSuccessListener {
                                    holder.joinButton.text = "Going"
                                    holder.joinButton.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
                                    Toast.makeText(context, "You joined the event!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Failed to join event.", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }
        }


        // Open Event Details when clicking the whole card
        holder.itemView.setOnClickListener {
            val intent = Intent(context, EventDetailActivity::class.java).apply {
                putExtra("eventId", event.eventId)
                putExtra("eventName", event.eventName)
                putExtra("startTime", event.startTime)
                putExtra("endTime", event.endTime)
                putExtra("location", event.location)
                putExtra("description", event.description)
                putExtra("imageUrl", event.imageUrl)
                putExtra("hostId", event.hostId)
                putExtra("tags", event.tags.toTypedArray())
                putExtra("capacity", event.capacity ?: -1)
            }
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int = events.size

    private fun displayAttendees(
        attendeesLayout: LinearLayout,
        profilePics: List<String>,
        totalCount: Int
    ) {
        attendeesLayout.removeAllViews()

        val maxVisiblePfps = 3
        val imageSize = 80
        val overlapMargin = -30

        val actualCount = profilePics.size

        for ((index, picUrl) in profilePics.withIndex()) {
            if (index < maxVisiblePfps) {
                val imageView = ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(imageSize, imageSize).apply {
                        if (index != 0) {
                            marginStart = overlapMargin
                        }
                        setPadding(4)
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    background = ContextCompat.getDrawable(context, R.drawable.background_circle)
                    clipToOutline = true
                }

                Glide.with(context)
                    .load(picUrl)
                    .placeholder(R.drawable.ic_user)
                    .circleCrop()
                    .into(imageView)

                attendeesLayout.addView(imageView)
            }
        }

        // 👉 Only add "+N" if there are **more** attendees than max visible
        if (actualCount > maxVisiblePfps) {
            val remainingCount = totalCount - maxVisiblePfps
            if (remainingCount > 0) {
                val moreView = TextView(context).apply {
                    text = "+$remainingCount"
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(imageSize, imageSize).apply {
                        marginStart = overlapMargin
                    }
                    background = ContextCompat.getDrawable(context, R.drawable.background_circle)
                    setTextColor(Color.BLACK)
                    textSize = 16f
                }

                attendeesLayout.addView(moreView)
            }
        }
    }

}
