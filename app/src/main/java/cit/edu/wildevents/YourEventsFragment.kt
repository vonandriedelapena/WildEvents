package cit.edu.wildevents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildevents.helper.YourEventAdapter // ✅ updated import
import cit.edu.wildevents.data.TimeFilterMode
import cit.edu.wildevents.helper.EventsViewModel
import com.google.android.material.button.MaterialButtonToggleGroup

class YourEventsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: YourEventAdapter // ✅ changed type
    private lateinit var viewModel: EventsViewModel
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var upcomingBtn: Button
    private lateinit var pastBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_your_event, container, false)

        recyclerView = view.findViewById(R.id.eventsRecyclerView)
        toggleGroup = view.findViewById(R.id.eventToggleGroup)
        upcomingBtn = view.findViewById(R.id.upcomingBtn)
        pastBtn = view.findViewById(R.id.pastBtn)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = YourEventAdapter(requireContext())
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(requireActivity())[EventsViewModel::class.java]
        viewModel.setTimeFilterMode(TimeFilterMode.UPCOMING)

        viewModel.events.observe(viewLifecycleOwner) { filteredEvents ->
            adapter.updateEvents(filteredEvents)
        }

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.upcomingBtn -> viewModel.setTimeFilterMode(TimeFilterMode.UPCOMING)
                    R.id.pastBtn -> viewModel.setTimeFilterMode(TimeFilterMode.PAST)
                }
            }
        }

        return view
    }
}
