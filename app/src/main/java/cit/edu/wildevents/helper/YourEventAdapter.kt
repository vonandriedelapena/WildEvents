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

class YourEventAdapter(private val context: Context) :
    RecyclerView.Adapter<YourEventAdapter.YourEventViewHolder>() {

    private var events: List<Event> = emptyList()

    class YourEventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventImage: ImageView = itemView.findViewById(R.id.eventImage)
        val eventDateTime: TextView = itemView.findViewById(R.id.eventDateTime)
        val eventTitle: TextView = itemView.findViewById(R.id.eventTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YourEventViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_event_list, parent, false)
        return YourEventViewHolder(view)
    }

    override fun onBindViewHolder(holder: YourEventViewHolder, position: Int) {
        val event = events[position]

        holder.eventTitle.text = event.eventName
        holder.eventDateTime.text = formatDateTime(event.startTime)

        if (!event.imageUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(event.imageUrl)
                .into(holder.eventImage)
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

    fun updateEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }

    private fun formatDateTime(timeInMillis: Long): String {
        val sdf = SimpleDateFormat("d MMM - EEE - h:mm a", Locale.getDefault())
        return sdf.format(Date(timeInMillis))
    }
}
