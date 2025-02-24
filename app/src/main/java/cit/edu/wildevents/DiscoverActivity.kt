package cit.edu.wildevents

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView

class DiscoverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_discover)

        val profileScreen = Intent(this, ProfileActivity::class.java)

        val buttonProfile = findViewById<ImageView>(R.id.profile_button)

        buttonProfile.setOnClickListener {
            startActivity(profileScreen);
        }
    }
}