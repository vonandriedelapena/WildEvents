package cit.edu.wildevents

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class DiscoverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_discover)

        val profileScreen = Intent(this, ProfileActivity::class.java)

    }
}