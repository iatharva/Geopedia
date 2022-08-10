package com.example.geopedia.usermenu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.geopedia.AddEvents;
import com.example.geopedia.AddQuestions;
import com.example.geopedia.CommentFeed;
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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Uevents extends Fragment implements OnMapReadyCallback, PermissionsListener {

    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
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

        final SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefreshEvents);
        RecyclerView recycler_events_user = view.findViewById(R.id.recycler_events_user);
        firebaseFirestore = FirebaseFirestore.getInstance();
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync( this);

        showEvents();
        recycler_events_user.setHasFixedSize(true);
        recycler_events_user.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_events_user.setAdapter(adapter);

        addEventFab.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), AddEvents.class);
            startActivity(intent);
        });

        pullToRefresh.setOnRefreshListener(() -> {
            showEvents();
            pullToRefresh.setRefreshing(false);
        });
        return view;
    }

    private void showEvents() {
        Query query = firebaseFirestore.collection("Events");

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
                viewHolder.eventName.setText(model.getEventTitle());
                viewHolder.eventDescription.setText(model.getEventDesc());
            }
        };
    }

    private static class EventsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        RelativeLayout cardLayout;
        TextView eventName,eventDescription;
        EventsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            cardLayout=mView.findViewById(R.id.cardLayout);
            eventName= mView.findViewById(R.id.eventName);
            eventDescription=mView.findViewById(R.id.eventDescription);
        }
    }

    //All required methods for map
    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        Uevents.this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> enableLocationComponent(style));
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {

        if (PermissionsManager.areLocationPermissionsGranted(getActivity())) {

            LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(getActivity())
                    .build();
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(getActivity(), loadedMapStyle)
                            .locationComponentOptions(customLocationComponentOptions)
                            .build());
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.zoomWhileTracking(200,0);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.NORMAL);

            //get the location of the clicked Event from model object.
            Location lastKnownLocation = locationComponent.getLastKnownLocation();
            if (lastKnownLocation != null)
            {
                mapboxMap.addMarker(new MarkerOptions().position(new LatLng(18.508486, 73.817409)).setIcon(
                        IconFactory.getInstance(getActivity()).fromResource(R.drawable.place_marker_green60)));
                mapboxMap.easeCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(18.508486, 73.817409), 15), 7000);
            }

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(getActivity(), "We need user permission in order to function the app properly", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(this::enableLocationComponent);
        } else {
            Toast.makeText(getActivity(), "User permission is not given", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
        adapter.startListening();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        adapter.notifyDataSetChanged();
        //getUserLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        adapter.stopListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        adapter.stopListening();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
