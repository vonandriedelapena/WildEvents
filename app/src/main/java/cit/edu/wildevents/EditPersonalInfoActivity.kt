package cit.edu.wildevents

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class EditPersonalInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_personal_info)

        val title = intent.getStringExtra("title") ?: return
        val value = intent.getStringExtra("value") ?: ""

        val editText = findViewById<EditText>(R.id.edit_text)
        editText.setText(value)

        val saveButton = findViewById<Button>(R.id.save_button)
        saveButton.setOnClickListener {
            val newValue = editText.text.toString()

            val resultIntent = Intent().apply {
                putExtra("title", title)
                putExtra("value", newValue)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
