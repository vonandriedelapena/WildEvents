package cit.edu.wildevents

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PersonalInfoAdapter(private val items: List<PersonalInfoItem>) : RecyclerView.Adapter<PersonalInfoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val value: TextView = view.findViewById(R.id.value)
        val editButton: TextView = view.findViewById(R.id.edit_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.settings_personal_info_name, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.value.text = item.value
        holder.editButton.visibility = if (item.isEditable) View.VISIBLE else View.GONE

        holder.editButton.setOnClickListener {
            // Handle edit action here
        }
    }

    override fun getItemCount() = items.size
}
