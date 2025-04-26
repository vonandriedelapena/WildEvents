package cit.edu.wildevents

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildevents.data.Comment
import com.bumptech.glide.Glide

class CommentAdapter(private var comments: MutableList<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userAvatar: ImageView = view.findViewById(R.id.user_avatar)
        val userName: TextView = view.findViewById(R.id.user_name)
        val commentContent: TextView = view.findViewById(R.id.comment_content)
        val commentTimestamp: TextView = view.findViewById(R.id.comment_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.userName.text = comment.userName
        holder.commentContent.text = comment.content
        holder.commentTimestamp.text = comment.timestamp?.let {
            android.text.format.DateFormat.format("dd MMM yyyy, hh:mm a", it).toString()
        } ?: "Just now"

        if (!comment.userAvatarUrl.isNullOrEmpty()) {
            Glide.with(holder.userAvatar.context)
                .load(comment.userAvatarUrl)
                .circleCrop()
                .into(holder.userAvatar)
        } else {
            holder.userAvatar.setImageResource(R.drawable.ic_user) // fallback avatar
        }
    }

    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }
}
