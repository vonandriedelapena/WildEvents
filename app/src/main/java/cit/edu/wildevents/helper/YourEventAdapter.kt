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

        holder.eventTitle.text = event.eventName
        holder.eventDateTime.text = (formatter.format(event.startTime)).trim()
        holder.eventLocation.text = event.location

        if (!event.imageUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(event.imageUrl)
                .into(holder.eventImage)
        } else {
            holder.eventImage.setImageResource(R.drawable.placeholder_image)
        }

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
                    textSize = 16f
                    setTypeface(null, Typeface.BOLD)
                }
                attendeesLayout.addView(moreView)
            }
        }
    }
}
