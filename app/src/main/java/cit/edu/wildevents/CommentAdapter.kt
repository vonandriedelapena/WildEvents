package cit.edu.wildevents

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildevents.data.Comment
import com.bumptech.glide.Glide

class CommentAdapter(
    private var comments: MutableList<Comment>,
    private val currentUserId: String,
    private val currentEventId: String,
    private val isHost: Boolean,
    private val onDeleteComment: (Comment) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userAvatar: ImageView = view.findViewById(R.id.user_avatar)
        val userName: TextView = view.findViewById(R.id.user_name)
        val commentContent: TextView = view.findViewById(R.id.comment_content)
        val commentTimestamp: TextView = view.findViewById(R.id.comment_timestamp)

        fun bind(comment: Comment) {
            userName.text = comment.userName
            commentContent.text = comment.content
            commentTimestamp.text = android.text.format.DateFormat.format("dd MMM yyyy, hh:mm a", comment.timestamp).toString()

            if (!comment.userAvatarUrl.isNullOrEmpty()) {
                Glide.with(userAvatar.context)
                    .load(comment.userAvatarUrl)
                    .circleCrop()
                    .into(userAvatar)
            } else {
                userAvatar.setImageResource(R.drawable.ic_user)
            }

            // Handle long press for deletion
            itemView.setOnLongClickListener {
                val canDelete = (comment.userId == currentUserId || isHost) && comment.eventId == currentEventId
                if (canDelete) {
                    AlertDialog.Builder(itemView.context)
                        .setTitle("Delete Comment")
                        .setMessage("Are you sure you want to delete this comment?")
                        .setPositiveButton("Delete") { _, _ ->
                            onDeleteComment(comment)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }
}

