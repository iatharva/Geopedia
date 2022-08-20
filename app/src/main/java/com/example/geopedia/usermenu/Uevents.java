package com.example.geopedia.usermenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.geopedia.AddEvents;
import com.example.geopedia.AddQuestions;
import com.example.geopedia.CommentFeed;
import com.example.geopedia.Info.EventInfo;
import com.example.geopedia.R;
import com.example.geopedia.extras.Events;
import com.example.geopedia.extras.Question;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Uevents extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private ToggleButton toggleEvent;
    private double currentSelectedLatitude=0,currentSelectedLongitude=0;
    private  double currentLatitude,currentLongitude;
    private EditText SearchEventFld;
    private ImageButton searchEButton;
    private RecyclerView recycler_events_user;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_uevents, container, false);
        FloatingActionButton addEventFab = view.findViewById(R.id.addEventFab);
        SearchEventFld = view.findViewById(R.id.SearchEventFld);
        searchEButton = view.findViewById(R.id.searchEButton);
        //toggleEvent = view.findViewById(R.id.toggleEvent);

        //set default toggle button text to "Show Present Events"
        //toggleEvent.setText("Show Present Events");
        //set default toggle button to true
        //toggleEvent.setChecked(true);

        /*
        if(toggleEvent.isChecked()){
            toggleEvent.setText("Show Present Events");
        }
        else{
            toggleEvent.setText("Show Past Events");
        }

        toggleEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toggleEvent.isChecked()){
                    toggleEvent.setText("Show Present Events");
                    showEvents(toggleEvent.isChecked());
                }
                else{
                    toggleEvent.setText("Show Past Events");
                    showEvents(toggleEvent.isChecked());
                }
            }
        });
         */

        final SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefreshEvents);
        recycler_events_user = view.findViewById(R.id.recycler_events_user);
        firebaseFirestore = FirebaseFirestore.getInstance();

        showEvents(true,"");
        recycler_events_user.setHasFixedSize(true);
        recycler_events_user.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_events_user.setAdapter(adapter);

        addEventFab.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), AddEvents.class);
            startActivity(intent);
        });

        pullToRefresh.setOnRefreshListener(() -> {
            showEvents(true, "");
            pullToRefresh.setRefreshing(false);
        });

        searchEButton.setOnClickListener(view -> {
            if(TextUtils.isEmpty(SearchEventFld.getText().toString()))
            {
                Toast.makeText(getActivity(),"Please enter something to search",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getActivity(), "Searching through our logs", Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(),"Searching -"+SearchEventFld.getText().toString(),Toast.LENGTH_SHORT).show();
                showEvents(false, SearchEventFld.getText().toString());
            }
        });


        return view;
    }

    private void showEvents(boolean isPresent, String searchEvent) {
        Query query;
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        if(isPresent)
        {
            query = firebaseFirestore.collection("Events").whereEqualTo("eventStatus", "Pending").whereEqualTo("isDeleted","0");
        }
        else
        {
            query = firebaseFirestore.collection("Events").whereEqualTo("isDeleted","0").whereEqualTo("eventTitle", searchEvent);;
        }

        FirestoreRecyclerOptions<Events> options = new FirestoreRecyclerOptions.Builder<Events>()
                .setQuery(query, Events.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Events, EventsViewHolder>(options) {
            @NotNull
            @Override
            public EventsViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_event_user, parent, false);
                return new EventsViewHolder(view);
            }

            @SuppressLint("DefaultLocale")
            @Override
            protected void onBindViewHolder(@NotNull EventsViewHolder viewHolder, int position, @NotNull final Events model) {
                final String event_id= model.getEventId();
                /*
                if(model.getEventLatitude() >= currentLatitude - 0.01 && model.getEventLatitude() <= currentLatitude + 0.01 && model.getEventLongitude() >= currentLongitude - 0.01 && model.getEventLongitude() <= currentLongitude + 0.01)
                    viewHolder.cardLayout.setVisibility(View.VISIBLE);
                else
                {
                    viewHolder.cardLayout.setVisibility(View.INVISIBLE);
                    viewHolder.cardLayout.getLayoutParams().height = 0;
                    viewHolder.cardLayout.requestLayout();
                }
                */
                viewHolder.eventName.setText(model.getEventTitle());
                viewHolder.eventDescription.setText(model.getEventDesc());

                viewHolder.cardLayout.setOnClickListener(view -> {
                    currentSelectedLatitude = model.getEventLatitude();
                    currentSelectedLongitude = model.getEventLongitude();
                    Intent intent = new Intent(getActivity(), EventInfo.class);
                    intent.putExtra("event_id", event_id);
                    intent.putExtra("latitude", currentSelectedLatitude);
                    intent.putExtra("longitude", currentSelectedLongitude);
                    startActivity(intent);
                });

                if(model.getEventType().equals("Once time")){
                    viewHolder.eventType.setText(String.format("Event on %s at %s", model.getEventDate(), model.getEventTime()));
                    viewHolder.eventStatus.setText(compareDateToPresent(model.getEventDate()));

                    if(compareDateToPresent(model.getEventDate()).equals("Completed")){
                        firebaseFirestore.collection("Events").document(model.getEventId()).update("eventStatus", "Completed");
                    }
                }else {
                    viewHolder.eventType.setText(String.format("This event happens %s", model.getEventRecurringOption()));
                    viewHolder.eventStatus.setText("Active");
                }
            }
        };
        adapter.startListening();
        recycler_events_user.setAdapter(adapter);
    }

    private static class EventsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        RelativeLayout cardLayout;
        TextView eventName,eventDescription,eventType,eventStatus;
        EventsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            cardLayout=mView.findViewById(R.id.cardLayout);
            eventName= mView.findViewById(R.id.eventName);
            eventDescription=mView.findViewById(R.id.eventDescription);
            eventStatus=mView.findViewById(R.id.eventStatus);
            eventType = mView.findViewById(R.id.eventType);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        //getUserLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.stopListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    public String compareDateToPresent(String eventDate)
    {
        //date in eventDate will be like 2022-8-15 i.e. yyyy-mm-dd where m and d can be single digit
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String[] currentDateArray = currentDate.split("-");
        String[] eventDateArray = eventDate.split("-");
        if(Integer.parseInt(currentDateArray[0])>Integer.parseInt(eventDateArray[0]))
        {
            return "Completed";
        }
        else if(Integer.parseInt(currentDateArray[0])==Integer.parseInt(eventDateArray[0]))
        {
            if(Integer.parseInt(currentDateArray[1])>Integer.parseInt(eventDateArray[1]))
            {
                return "Completed";
            }
            else if(Integer.parseInt(currentDateArray[1])==Integer.parseInt(eventDateArray[1]))
            {
                if(Integer.parseInt(currentDateArray[2])>Integer.parseInt(eventDateArray[2]))
                {
                    return "Completed";
                }
                else if(Integer.parseInt(currentDateArray[2])==Integer.parseInt(eventDateArray[2]))
                {
                    return "Active Today";
                }
                else
                {
                    return "Active in Future";
                }
            }
            else
            {
                return "Active in Future";
            }
        }
        else
        {
            return "Active in Future";
        }
    }
}
