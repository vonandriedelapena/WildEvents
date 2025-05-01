package cit.edu.wildevents

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
    private var previouslySelectedButton: ImageView? = null
    private var previousTag: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES), 102)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        // Load default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DiscoverFragment())
                .commit()
        }

        val discoverButton = findViewById<ImageView>(R.id.nav_discover)
        val profileButton = findViewById<ImageView>(R.id.nav_profile)
        val wishlistButton = findViewById<ImageView>(R.id.nav_wishlist)
        val messagesButton = findViewById<ImageView>(R.id.nav_messages)


        // Set default selected button
        previouslySelectedButton = discoverButton
        previousTag = "home"
        discoverButton.setImageResource(R.drawable.ic_home_filled)
        switchFragment(DiscoverFragment())

        discoverButton.setOnClickListener {
            switchFragment(DiscoverFragment())
            updateSelectedButton(discoverButton)
        }

        profileButton.setOnClickListener {
            switchFragment(ProfileFragment())
            updateSelectedButton(profileButton)
        }

        wishlistButton.setOnClickListener {
            switchFragment(YourEventsFragment())
            updateSelectedButton(wishlistButton)
        }

        messagesButton.setOnClickListener {
            switchFragment(MessagesFragment())
            updateSelectedButton(messagesButton)
        }
    }

    private fun updateSelectedButton(newSelectedButton: ImageView) {
        // Reset previous to its outline version
        previousTag?.let {
            val defaultResId = getDrawableIdForTag(it, filled = false)
            previouslySelectedButton?.setImageResource(defaultResId)
        }

        // Set new to its filled version
        val newTag = newSelectedButton.tag as String
        val filledResId = getDrawableIdForTag(newTag, filled = true)
        newSelectedButton.setImageResource(filledResId)

        // Update state
        previouslySelectedButton = newSelectedButton
        previousTag = newTag
    }

    private fun getDrawableIdForTag(tag: String, filled: Boolean): Int {
        val resourceName = if (filled) "ic_${tag}_filled" else "ic_${tag}_hollow"
        return resources.getIdentifier(resourceName, "drawable", packageName)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
