package cit.edu.wildevents

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SettingsPersonalInformation : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_personal_information)

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val items = listOf(
            PersonalInfoItem("Legal name", "Reynald Anthony Doble", true),
            PersonalInfoItem("Preferred first name", "Reynald", true),
            PersonalInfoItem("Phone number", "+63 *** *** 8954", true),
            PersonalInfoItem("Email", "d***d@gmail.com", true),
            PersonalInfoItem("Address", "Not provided", true),
            PersonalInfoItem("Emergency contact", "Not provided", true),
            PersonalInfoItem("Identity verification", "Verified", false)
        )

        recyclerView.adapter = PersonalInfoAdapter(items)

        val mainScreen = Intent(this, MainActivity::class.java)
        val goToMainActivity = findViewById<ImageView>(R.id.back_button)
        goToMainActivity.setOnClickListener {
            startActivity(mainScreen)
        }
    }
}
