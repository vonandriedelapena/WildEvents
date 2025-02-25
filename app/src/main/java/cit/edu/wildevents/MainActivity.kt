package cit.edu.wildevents

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .commit()
        }

        // Handle Bottom Navigation Clicks
        findViewById<ImageView>(R.id.nav_profile).setOnClickListener {
            switchFragment(ProfileFragment())
        }

        findViewById<ImageView>(R.id.nav_discover).setOnClickListener {
            switchFragment(DiscoverFragment())
        }

//        findViewById<ImageView>(R.id.nav_settings).setOnClickListener {
//            switchFragment(SettingsFragment())
//        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
