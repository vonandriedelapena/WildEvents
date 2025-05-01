package cit.edu.wildevents

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import cit.edu.wildevents.app.MyApplication
import com.bumptech.glide.Glide

class ProfileFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.username).text =
            (requireContext().applicationContext as MyApplication).currentUser?.firstName


        val personalInfo = view.findViewById<View>(R.id.personal_info)
        personalInfo?.setOnClickListener {
            Log.d("ProfileFragment", "Personal Info Clicked")
            val intent = Intent(requireActivity(), SettingsPersonalInformation::class.java)
            startActivity(intent)
        }

        val profileView = view.findViewById<View>(R.id.profile_view)
        profileView?.setOnClickListener {
            Log.d("ProfileFragment", "Profile View Clicked")
            val intent = Intent(requireActivity(), SettingsProfileView::class.java)
            startActivity(intent)
        }

        val aboutDevelopersView = view.findViewById<View>(R.id.about_developers)
        aboutDevelopersView?.setOnClickListener {
            Log.d("ProfileFragment", "About Developers View Clicked")
            val intent = Intent(requireActivity(), SettingsAboutDevelopers::class.java)
            startActivity(intent)
        }

        val logoutButton = view.findViewById<View>(R.id.logout_button)
        logoutButton?.setOnClickListener {
            showLogoutConfirmation()
        }

        val promoBtn = view.findViewById<LinearLayout>(R.id.promo_button)
        promoBtn?.setOnClickListener {
            Log.d("ProfileFragment", "Promo Button Clicked")

            if((requireContext().applicationContext as MyApplication).currentUser?.isHost == true) {
                val intent = Intent(requireActivity(), CreateEventActivity::class.java)
                startActivity(intent)
            } else {
                // Show a message or handle the case when the user is not an event creator
                AlertDialog.Builder(requireContext())
                    .setTitle("Event Creator Required")
                    .setMessage("You need to be an event creator to access this feature.")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }

        val profileImage = view.findViewById<ImageView>(R.id.profile_image)
        val imageUrl = (requireContext().applicationContext as MyApplication).currentUser?.profilePic

        if (!imageUrl.isNullOrEmpty() && imageUrl != "default.png") {
            Glide.with(this) // or `Glide.with(requireContext())`
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_add) // optional error fallback
                .circleCrop()
                .into(profileImage)
        } else {
            profileImage.setImageResource(R.drawable.ic_placeholder)
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout Confirmation")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Continue") { _, _ ->
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}

