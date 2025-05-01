package cit.edu.wildevents

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildevents.helper.EventAdapter
import cit.edu.wildevents.app.MyApplication
import cit.edu.wildevents.data.TimeFilterMode
import cit.edu.wildevents.helper.EventsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DiscoverFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventAdapter
    private lateinit var viewModel: EventsViewModel
    private lateinit var searchBar: EditText
    private var selectedCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_discover, container, false)

        recyclerView = view.findViewById(R.id.events_recycler_view)
        searchBar = view.findViewById(R.id.search_bar)
        val fabAddEvent: FloatingActionButton = view.findViewById(R.id.fab_add_event)

        val currentUser = MyApplication.instance.currentUser
        fabAddEvent.visibility = if (currentUser?.isHost == true) View.VISIBLE else View.GONE

        fabAddEvent.setOnClickListener {
            startActivity(Intent(requireContext(), CreateEventActivity::class.java))
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel = ViewModelProvider(requireActivity())[EventsViewModel::class.java]
        viewModel.setTimeFilterMode(TimeFilterMode.UPCOMING)

        // Adapter does not need refresh callback anymore
        adapter = EventAdapter(requireContext())
        recyclerView.adapter = adapter

        viewModel.events.observe(viewLifecycleOwner) { updatedEvents ->
            adapter.updateEvents(updatedEvents)
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.filterEvents(s.toString(), selectedCategory)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val categoryIds = listOf(
            R.id.button_general,
            R.id.button_engr,
            R.id.button_arch,
            R.id.button_cnahs,
            R.id.button_ccs,
            R.id.button_crim,
            R.id.button_cmba,
        )

        val defaultColor = ContextCompat.getColor(requireContext(), R.color.maroon)
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.gold)

        val categoryButtons = mutableMapOf<String, Button>()
        categoryIds.forEach { id ->
            val button = view.findViewById<Button>(id)
            val category = button.tag?.toString() ?: ""
            categoryButtons[category] = button

            button.setOnClickListener {
                selectedCategory = category
                viewModel.filterEvents(searchBar.text.toString(), selectedCategory)

                categoryIds.forEach { resetId ->
                    val resetButton = view.findViewById<Button>(resetId)
                    resetButton.setBackgroundResource(R.drawable.category_background)
                    resetButton.backgroundTintList = ColorStateList.valueOf(defaultColor)
                    resetButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }

                button.setBackgroundResource(R.drawable.category_background)
                button.backgroundTintList = ColorStateList.valueOf(selectedColor)
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }

        val defaultCategory = "General"
        selectedCategory = defaultCategory
        categoryButtons[defaultCategory]?.performClick()

        return view
    }
}
