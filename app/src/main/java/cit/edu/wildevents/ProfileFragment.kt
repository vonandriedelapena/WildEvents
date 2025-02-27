package cit.edu.wildevents

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

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
    }


}

