package cit.edu.wildevents

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildevents.app.MyApplication
import cit.edu.wildevents.data.NotificationData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MessagesFragment : Fragment() {
    private lateinit var notificationsAdapter: NotificationsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_messages, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.messages_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        notificationsAdapter = NotificationsAdapter { notification ->
            deleteNotification(notification)
        }
        recyclerView.adapter = notificationsAdapter

        loadNotifications()
        return view
    }


    private fun loadNotifications() {
        val userId = (requireActivity().application as MyApplication).currentUser?.id ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val notifications = documents.map { doc ->
                    doc.toObject(NotificationData::class.java)
                }
                notificationsAdapter.submitList(notifications)
            }
            .addOnFailureListener { e ->
                Log.e("MessagesFragment", "Failed to fetch notifications", e)
            }
    }

    private fun deleteNotification(notification: NotificationData) {
        val userId = (requireActivity().application as MyApplication).currentUser?.id ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).collection("notifications")
            .whereEqualTo("timestamp", notification.timestamp)
            .whereEqualTo("message", notification.message)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    snapshot.documents.first().reference.delete()
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Notification deleted", Toast.LENGTH_SHORT).show()
                            loadNotifications()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to delete", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

}

