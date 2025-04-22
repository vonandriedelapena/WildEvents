package cit.edu.wildevents

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildevents.adapter.EventAdapter
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
        adapter = EventAdapter(requireContext())
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(requireActivity())[EventsViewModel::class.java]
        viewModel.setTimeFilterMode(TimeFilterMode.UPCOMING)

        // Observe event list
        viewModel.events.observe(viewLifecycleOwner) { updatedEvents ->
            adapter.updateEvents(updatedEvents)

            // ✅ Force filter after first observation
            if (updatedEvents.isEmpty()) {
                viewModel.filterEvents(searchBar.text.toString(), selectedCategory)
            }
        }


        // Search bar listener
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.filterEvents(s.toString(), selectedCategory)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Category button listeners
        val categoryIds = listOf(
            R.id.button_general,
            R.id.button_engr,
            R.id.button_arch,
            R.id.button_cnahs,
            R.id.button_ccs,
            R.id.button_crim,
            R.id.button_cmba,
        )

        categoryIds.forEach { id ->
            view.findViewById<Button>(id).setOnClickListener {
                selectedCategory = it.tag?.toString()
                viewModel.filterEvents(searchBar.text.toString(), selectedCategory)
            }
        }

        return view
    }
}
