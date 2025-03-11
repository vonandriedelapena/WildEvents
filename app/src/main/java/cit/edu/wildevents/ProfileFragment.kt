package cit.edu.wildevents

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog

class ProfileFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

