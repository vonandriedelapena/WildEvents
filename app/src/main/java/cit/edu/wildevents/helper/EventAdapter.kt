package cit.edu.wildevents.helper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import cit.edu.wildevents.R
import cit.edu.wildevents.data.Event

class EventAdapter(private val context: Context, private val events: List<Event>) : BaseAdapter() {

    override fun getCount(): Int = events.size
    override fun getItem(position: Int): Any = events[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.event_list_item, parent, false)

        val event = events[position]
        view.findViewById<ImageView>(R.id.event_image).setImageResource(event.imageResId)
        view.findViewById<TextView>(R.id.event_title).text = event.title
        view.findViewById<TextView>(R.id.event_date).text = event.date
        view.findViewById<TextView>(R.id.event_description).text = event.description

        view.setOnClickListener {
            Toast.makeText(
                context,
                "Title: ${event.title}\nDate: ${event.date}\nDescription: ${event.description}",
                Toast.LENGTH_LONG
            ).show()
        }

        return view
    }

}
