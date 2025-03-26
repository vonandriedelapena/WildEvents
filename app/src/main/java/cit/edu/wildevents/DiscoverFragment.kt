import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import cit.edu.wildevents.R
import cit.edu.wildevents.data.Event
import cit.edu.wildevents.helper.EventAdapter

class DiscoverFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_discover, container, false)

        val eventListView: ListView = view.findViewById(R.id.events_list_view)

        val events = listOf(
            Event("INTRAMSURALS 2025: CLASH OF CELESTIAL REALMS", "12-15 March, 25",
                "The celestial realms awaken, and the stars have set the stage for the ultimate clash.", R.drawable.img_event1),
            Event("LIKHA: THE GRANDEST FINALE", "15 March, 25",
                "Are you ready? The grandest night of Intramurals 2025 is here!", R.drawable.img_event2),
            Event("CCS: ACQUAINTANCE PARTY", "15 March, 25",
                "Get ready to rewind time as we teleport you to an era of glitter, disco balls, and neon dreams!", R.drawable.img_event3)
        )

        val adapter = EventAdapter(requireContext(), events)
        eventListView.adapter = adapter

        return view
    }
}
