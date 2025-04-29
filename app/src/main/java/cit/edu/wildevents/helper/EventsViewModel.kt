package cit.edu.wildevents.helper

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cit.edu.wildevents.data.Event
import cit.edu.wildevents.data.TimeFilterMode
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class EventsViewModel : ViewModel() {

    private val _allEvents = MutableLiveData<List<Event>>(emptyList())
    private val _filteredEvents = MutableLiveData<List<Event>>(emptyList())
    val events: LiveData<List<Event>> get() = _filteredEvents

    private var listenerRegistration: ListenerRegistration? = null
    private var attendeeListener: ListenerRegistration? = null

    private var timeFilterMode: TimeFilterMode = TimeFilterMode.UPCOMING
    private var currentQuery: String = ""
    private var currentCategory: String? = null

    private val eventAttendees = mutableMapOf<String, MutableList<String>>()

    init {
        startListeningToEvents()
        startListeningToAttendees()
    }

    fun setTimeFilterMode(mode: TimeFilterMode) {
        timeFilterMode = mode
        filterEvents(currentQuery, currentCategory)
    }

    private fun startListeningToEvents() {
        listenerRegistration = FirebaseFirestore.getInstance()
            .collection("events")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val loadedEvents = snapshot.mapNotNull { doc ->
                        try {
                            val startTime = (doc.get("startTime") as? com.google.firebase.Timestamp)
                                ?.toDate()?.time ?: 0L
                            val endTime = (doc.get("endTime") as? com.google.firebase.Timestamp)
                                ?.toDate()?.time ?: 0L

                            Event(
                                eventId = doc.getString("id") ?: "",
                                eventName = doc.getString("name") ?: "",
                                startTime = startTime,
                                endTime = endTime,
                                location = doc.getString("location") ?: "",
                                description = doc.getString("description") ?: "",
                                imageUrl = doc.getString("coverImageUrl"),
                                hostId = doc.getString("hostId") ?: "",
                                tags = doc.get("tags") as? List<String> ?: emptyList(),
                                capacity = doc.getLong("capacity")?.toInt()
                            )
                        } catch (e: Exception) {
                            Log.e("EventsViewModel", "Error parsing event: ${e.localizedMessage}")
                            null
                        }
                    }

                    _allEvents.value = loadedEvents
                    filterEvents(currentQuery, currentCategory)
                }
            }
    }

    private fun startListeningToAttendees() {
        attendeeListener = FirebaseFirestore.getInstance()
            .collection("attendee")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val updatedMap = mutableMapOf<String, MutableList<String>>()
                    for (doc in snapshot.documents) {
                        val eventId = doc.getString("eventId") ?: continue
                        val userId = doc.getString("userId") ?: continue
                        updatedMap.getOrPut(eventId) { mutableListOf() }.add(userId)
                    }
                    synchronized(eventAttendees) {
                        eventAttendees.clear()
                        eventAttendees.putAll(updatedMap)
                    }
                }
            }
    }

    fun getAttendeesForEvent(eventId: String): List<String> {
        return synchronized(eventAttendees) {
            eventAttendees[eventId]?.toList() ?: emptyList()
        }
    }

    fun getEventsJoinedByUser(userId: String): List<String> {
        return synchronized(eventAttendees) {
            eventAttendees.filterValues { it.contains(userId) }.keys.toList()
        }
    }

    fun filterEvents(query: String, category: String? = null) {
        val currentTime = System.currentTimeMillis()
        val currentList = _allEvents.value ?: return

        currentQuery = query
        currentCategory = category

        val filtered = currentList.filter { event ->
            val matchesQuery = query.isBlank() ||
                    event.eventName.contains(query, ignoreCase = true) ||
                    event.description.contains(query, ignoreCase = true)

            val matchesCategory = category.isNullOrBlank() ||
                    event.tags.any { it.equals(category, ignoreCase = true) }

            val isUpcoming = event.endTime > currentTime
            val isPast = event.endTime <= currentTime

            val matchesTime = when (timeFilterMode) {
                TimeFilterMode.UPCOMING -> isUpcoming
                TimeFilterMode.PAST -> isPast
                TimeFilterMode.ALL -> true
            }

            matchesQuery && matchesCategory && matchesTime
        }

        _filteredEvents.postValue(filtered)
    }

    fun addEvent(event: Event) {
        val updatedList = (_allEvents.value ?: emptyList()) + event
        _allEvents.value = updatedList
        filterEvents(currentQuery, currentCategory)
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
        attendeeListener?.remove()
    }
}
