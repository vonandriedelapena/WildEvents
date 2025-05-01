package cit.edu.wildevents

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildevents.data.NotificationData
import java.text.DateFormat

class NotificationsAdapter(
    private val onDelete: (NotificationData) -> Unit
) : ListAdapter<NotificationData, NotificationsAdapter.NotificationViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view, onDelete)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NotificationViewHolder(
        itemView: View,
        private val onDelete: (NotificationData) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val titleTextView: TextView = itemView.findViewById(R.id.notification_title)
        private val messageTextView: TextView = itemView.findViewById(R.id.notification_message)
        private val timeTextView: TextView = itemView.findViewById(R.id.notification_time)

        fun bind(notification: NotificationData) {
            titleTextView.text = notification.title
            messageTextView.text = notification.message
            timeTextView.text = DateFormat.getDateTimeInstance().format(notification.timestamp.toDate())

            itemView.setOnLongClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Delete Notification")
                    .setMessage("Do you want to delete this notification?")
                    .setPositiveButton("Delete") { _, _ -> onDelete(notification) }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<NotificationData>() {
        override fun areItemsTheSame(oldItem: NotificationData, newItem: NotificationData): Boolean {
            return oldItem.timestamp == newItem.timestamp && oldItem.message == newItem.message
        }

        override fun areContentsTheSame(oldItem: NotificationData, newItem: NotificationData): Boolean {
            return oldItem == newItem
        }
    }
}

