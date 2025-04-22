package cit.edu.wildevents.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildevents.EventDetailActivity
import cit.edu.wildevents.R
import cit.edu.wildevents.data.Event
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class EventAdapter(private val context: Context) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private var events: List<Event> = emptyList()

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventImage: ImageView = view.findViewById(R.id.event_image)
        val eventTitle: TextView = view.findViewById(R.id.event_title)
        val eventDate: TextView = view.findViewById(R.id.event_date)
        val eventDescription: TextView = view.findViewById(R.id.event_description)
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

        holder.eventTitle.text = event.eventName
        holder.eventDate.text = formatDateTime(event.startTime)
        holder.eventDescription.text = event.description

        if (!event.imageUrl.isNullOrEmpty()) {
            Glide.with(context).load(event.imageUrl).into(holder.eventImage)
        } else {
            holder.eventImage.setImageResource(R.drawable.placeholder_image)
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

    private fun formatDateTime(timeInMillis: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault())
        return sdf.format(Date(timeInMillis))
    }
}
