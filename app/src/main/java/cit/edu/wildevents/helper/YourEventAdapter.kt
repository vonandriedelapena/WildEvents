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

class YourEventAdapter(private val context: Context) :
    RecyclerView.Adapter<YourEventAdapter.YourEventViewHolder>() {

    private var events: List<Event> = emptyList()

    class YourEventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val attendeesLayout: LinearLayout = itemView.findViewById(R.id.attendeesLayout)
        val eventImage: ImageView = itemView.findViewById(R.id.eventImage)
        val eventDateTime: TextView = itemView.findViewById(R.id.event_date)
        val eventTitle: TextView = itemView.findViewById(R.id.eventTitle)
        val eventLocation: TextView = itemView.findViewById(R.id.event_location)
        var joinButton: Button = itemView.findViewById(R.id.btnConfirm)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YourEventViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_event_list, parent, false)
        return YourEventViewHolder(view)
    }

    override fun onBindViewHolder(holder: YourEventViewHolder, position: Int) {
        val event = events[position]
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val db = FirebaseFirestore.getInstance()

        holder.attendeesLayout.removeAllViews()

        holder.eventTitle.text = event.eventName.trim()
        holder.eventDateTime.text = formatter.format(event.startTime)
        holder.eventLocation.text = event.location

        if (!event.imageUrl.isNullOrEmpty()) {
            Glide.with(context).load(event.imageUrl).into(holder.eventImage)
        } else {
            holder.eventImage.setImageResource(R.drawable.placeholder_image)
        }

        val currentUser = (context.applicationContext as cit.edu.wildevents.app.MyApplication).currentUser

        if (currentUser != null) {
            val isHostOfThisEvent = currentUser.isHost && currentUser.id == event.hostId

            if (isHostOfThisEvent) {
                // Host of this event
                setButtonToEdit(holder)

                holder.joinButton.setOnClickListener {
                    val intent = Intent(context, cit.edu.wildevents.EditEventActivity::class.java)
                    intent.putExtra("eventId", event.eventId)
                    context.startActivity(intent)
                }

                // 👉 DO NOT fetch or show attendees for their own event
            } else {
                // Normal user or host viewing other events

                // Check RSVP status
                db.collection("attendee")
                    .whereEqualTo("eventId", event.eventId)
                    .whereEqualTo("userId", currentUser.id)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val attendeeDocId = documents.documents[0].id
                            setButtonToGoing(holder)

                            holder.joinButton.setOnClickListener {
                                AlertDialog.Builder(context)
                                    .setTitle("Cancel RSVP")
                                    .setMessage("Should I cancel your RSVP?")
                                    .setPositiveButton("Yes") { _, _ ->
                                        db.collection("attendee")
                                            .document(attendeeDocId)
                                            .delete()
                                            .addOnSuccessListener {
                                                setButtonToJoin(holder)
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
                            setButtonToJoin(holder)

                            holder.joinButton.setOnClickListener {
                                val attendeeData = mapOf(
                                    "eventId" to event.eventId,
                                    "userId" to currentUser.id
                                )
                                db.collection("attendee")
                                    .add(attendeeData)
                                    .addOnSuccessListener {
                                        setButtonToGoing(holder)
                                        Toast.makeText(context, "You joined the event!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Failed to join event.", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }

                // 👉 Fetch attendees normally (only if not the host of this event)
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
            }
        }

        // Open Event Detail when clicking card
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



    private fun setButtonToGoing(holder: YourEventViewHolder) {
        holder.joinButton.text = "Going"
        holder.joinButton.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
    }

    private fun setButtonToJoin(holder: YourEventViewHolder) {
        holder.joinButton.text = "Join Event"
        holder.joinButton.setBackgroundColor(Color.parseColor("#333333"))
    }

    private fun setButtonToEdit(holder: YourEventViewHolder) {
        holder.joinButton.text = "Edit Event"
        holder.joinButton.setBackgroundColor(Color.parseColor("#FFA500"))
    }


    override fun getItemCount(): Int = events.size

    fun updateEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }

    private fun formatDateTime(timeInMillis: Long): String {
        val sdf = SimpleDateFormat("d MMM - EEE - h:mm a", Locale.getDefault())
        return sdf.format(Date(timeInMillis))
    }

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

        // Corrected Condition
        if (actualCount > maxVisiblePfps) {
            val remainingCount = totalCount - maxVisiblePfps
            if (remainingCount > 0) {
                val moreView = TextView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(imageSize, imageSize).apply {
                        marginStart = overlapMargin
                    }
                    text = "+$remainingCount"
                    gravity = Gravity.CENTER
                    background = ContextCompat.getDrawable(context, R.drawable.circle_black_with_white_border) // better visibility
                    setTextColor(Color.WHITE)
                    textSize = 14f
                    setTypeface(null, Typeface.BOLD)
                }
                attendeesLayout.addView(moreView)
            }
        }
    }
}
