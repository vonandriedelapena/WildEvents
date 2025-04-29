package cit.edu.wildevents

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildevents.app.MyApplication
import cit.edu.wildevents.data.Event
import cit.edu.wildevents.data.TimeFilterMode
import cit.edu.wildevents.helper.YourEventAdapter
import cit.edu.wildevents.helper.EventsViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class YourEventsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: YourEventAdapter
    private lateinit var viewModel: EventsViewModel

    private lateinit var calendarBtn: ImageView
    private lateinit var calendarView: CalendarView
    private lateinit var calendarLayout: LinearLayout

    private lateinit var eventFilterLayout: LinearLayout
    private lateinit var eventFilterText: TextView
    private lateinit var eventFilterIcon: ImageView

    private lateinit var ownershipFilterLayout: LinearLayout
    private lateinit var ownershipFilterText: TextView
    private lateinit var ownershipFilterIcon: ImageView

    private var fullEventList: List<Event> = emptyList()
    private var allFilteredEvents: List<Event> = emptyList()

    private var selectedOwnership = "Joined"
    private var isUserHost = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_your_event, container, false)

        recyclerView = view.findViewById(R.id.eventsRecyclerView)
        calendarBtn = view.findViewById(R.id.btnCalendar)
        calendarView = view.findViewById(R.id.calendarView)
        calendarLayout = view.findViewById(R.id.showCalendar)

        eventFilterLayout = view.findViewById(R.id.eventFilterDropdown)
        eventFilterText = view.findViewById(R.id.eventFilterText)
        eventFilterIcon = view.findViewById(R.id.eventFilterIcon)

        ownershipFilterLayout = view.findViewById(R.id.ownershipFilterDropdown)
        ownershipFilterText = view.findViewById(R.id.ownershipFilterText)
        ownershipFilterIcon = view.findViewById(R.id.ownershipFilterIcon)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = YourEventAdapter(requireContext())
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(requireActivity())[EventsViewModel::class.java]
        viewModel.setTimeFilterMode(TimeFilterMode.UPCOMING)

        calendarBtn.setOnClickListener {
            calendarLayout.visibility =
                if (calendarLayout.visibility == View.VISIBLE) View.GONE else View.VISIBLE

            if (calendarLayout.visibility == View.GONE) {
                applyCombinedFilter()
            }
        }

        eventFilterLayout.setOnClickListener {
            val popup = PopupMenu(requireContext(), eventFilterLayout)
            popup.menuInflater.inflate(R.menu.event_filter_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_all -> {
                        viewModel.setTimeFilterMode(TimeFilterMode.ALL)
                        eventFilterText.text = "All"
                        true
                    }
                    R.id.menu_upcoming -> {
                        viewModel.setTimeFilterMode(TimeFilterMode.UPCOMING)
                        eventFilterText.text = "Upcoming"
                        true
                    }
                    R.id.menu_past -> {
                        viewModel.setTimeFilterMode(TimeFilterMode.PAST)
                        eventFilterText.text = "Past"
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        ownershipFilterLayout.setOnClickListener {
            val popup = PopupMenu(requireContext(), ownershipFilterLayout)
            popup.menuInflater.inflate(R.menu.ownership_filter_menu, popup.menu)

            if (!isUserHost) {
                popup.menu.findItem(R.id.menu_hosted)?.isVisible = false
            }

            popup.setOnMenuItemClickListener { item ->
                selectedOwnership = when (item.itemId) {
                    R.id.menu_all -> "All"
                    R.id.menu_joined -> "Joined"
                    R.id.menu_hosted -> "Hosted"
                    R.id.menu_not_attended -> "Not Attended"
                    else -> "All"
                }

                ownershipFilterText.text = selectedOwnership
                applyCombinedFilter()
                true
            }

            popup.show()
        }


        viewModel.events.observe(viewLifecycleOwner) { filteredEvents ->
            val currentUser = (requireContext().applicationContext as MyApplication).currentUser
            if (currentUser != null) {
                val goingEventIds = viewModel.getEventsJoinedByUser(currentUser.id)
                isUserHost = filteredEvents.any { it.hostId == currentUser.id }

                fullEventList = filteredEvents
                allFilteredEvents = filteredEvents // <-- Include all events here
                applyCombinedFilter()
            } else {
                adapter.updateEvents(emptyList())
            }
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDateStr = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val filteredByDate = allFilteredEvents.filter { event ->
                val eventDateStr = sdf.format(Date(event.startTime))
                eventDateStr == selectedDateStr
            }

            adapter.updateEvents(filteredByDate)
        }

        return view
    }

    private fun applyCombinedFilter() {
        val currentUser = (requireContext().applicationContext as MyApplication).currentUser
        val filtered = allFilteredEvents.filter { event ->
            when (selectedOwnership) {
                "Joined" -> event.hostId != currentUser?.id &&
                        viewModel.getAttendeesForEvent(event.eventId).contains(currentUser?.id)
                "Hosted" -> event.hostId == currentUser?.id
                "Not Attended" -> event.hostId != currentUser?.id &&
                        !viewModel.getAttendeesForEvent(event.eventId).contains(currentUser?.id)
                else -> true
            }
        }
        adapter.updateEvents(filtered)
    }
}