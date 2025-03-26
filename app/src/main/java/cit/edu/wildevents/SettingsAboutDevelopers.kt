package cit.edu.wildevents

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

/*
* This activity displays the developers of the app and their skills.
* the list of skills is displayed in a Simple ListView.
* */
class SettingsAboutDevelopers : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_developer)

        // This goes back to the previous fragment without recreating MainActivity
        val goToMainActivity = findViewById<ImageView>(R.id.back_button)
        goToMainActivity.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // List of skills for each developer
        val developer1Skills = listOf("Java", "Kotlin", "UI/UX Design")
        val developer2Skills = listOf("Java", "Kotlin", "Firebase")

        // ListView for each developer
        val lvSkills1 = findViewById<ListView>(R.id.lvSkills1)
        val lvSkills2 = findViewById<ListView>(R.id.lvSkills2)

        /*
        * Adapters for the Simple ListView
        * */
        val arrayAdapter1 = ArrayAdapter(this, android.R.layout.simple_list_item_1, developer1Skills)
        val arrayAdapter2 = ArrayAdapter(this, android.R.layout.simple_list_item_1, developer2Skills)

        // Set the adapter for the ListViews and calculate their heights
        lvSkills1.adapter = arrayAdapter1
        setListViewHeightBasedOnChildren(lvSkills1)

        lvSkills2.adapter = arrayAdapter2
        setListViewHeightBasedOnChildren(lvSkills2)
    }

    /*
    * Since the ListView is inside a ScrollView, the ListView will only show the first item.
    * Thus, this function calculates the height of the ListView based on the number of items in the list
    * and set it to be the height of the ListView.
    * */
    private fun setListViewHeightBasedOnChildren(listView: ListView) {
        // Get list adapter of the ListView
        val listAdapter = listView.adapter ?: return
        // Calculate total height of all items in ListView
        var totalHeight = 0
        for (i in 0 until listAdapter.count) {
            val listItem = listAdapter.getView(i, null, listView)
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight
        }
        // Set list height
        val params = listView.layoutParams
        params.height = totalHeight + (listView.dividerHeight * (listAdapter.count - 1))
        listView.layoutParams = params
        listView.requestLayout()
    }

}
