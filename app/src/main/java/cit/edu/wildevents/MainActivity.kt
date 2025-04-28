package cit.edu.wildevents

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
    private var previouslySelectedButton: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        discoverButton.setImageResource(R.drawable.icon_selector)

        // Handle Bottom Navigation Clicks
        discoverButton.setOnClickListener {
            switchFragment(DiscoverFragment())
            updateSelectedButton(discoverButton, R.drawable.ic_magnifying_glass)
        }

        profileButton.setOnClickListener {
            switchFragment(ProfileFragment())
            updateSelectedButton(profileButton, R.drawable.ic_user)
        }

        wishlistButton.setOnClickListener {
            switchFragment(YourEventsFragment())
            updateSelectedButton(wishlistButton, R.drawable.ic_heart)
        }

        messagesButton.setOnClickListener {
            switchFragment(MessagesFragment())
            updateSelectedButton(messagesButton, R.drawable.ic_message)
        }
    }

    private fun updateSelectedButton(newSelectedButton: ImageView, defaultDrawable: Int) {
        // Reset the previously selected button to its default drawable
        previouslySelectedButton?.setImageResource(defaultDrawable)

        // Apply the icon_selector to the new selected button
        newSelectedButton.setImageResource(R.drawable.icon_selector)

        // Update the reference to the currently selected button
        previouslySelectedButton = newSelectedButton
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // Add this line to retain fragment state
            .commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish() // Closes all activities and exits the app
    }
}