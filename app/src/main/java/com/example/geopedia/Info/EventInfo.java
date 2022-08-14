package com.example.geopedia.Info;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.geopedia.R;
import com.example.geopedia.usermenu.Uevents;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class EventInfo extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LottieAnimationView eventStatusLottie;
    private TextView eventTitle,eventDescription,eventPostedOn,eventPostedBy,eventType,eventHappens,eventStatus,eventAddress;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    private Double latitude,longitude;
    private String event_id;
    private Button launchMaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);

        mapView = findViewById(R.id.mapView);
        launchMaps = findViewById(R.id.launchMaps);
        eventTitle = findViewById(R.id.eventTitle);
        eventDescription = findViewById(R.id.eventDescription);
        eventPostedOn = findViewById(R.id.eventPostedOn);
        eventPostedBy = findViewById(R.id.eventPostedBy);
        eventType = findViewById(R.id.eventType);
        eventHappens = findViewById(R.id.eventHappens);
        eventStatus = findViewById(R.id.eventStatus);
        eventAddress = findViewById(R.id.eventAddress);
        eventStatusLottie = findViewById(R.id.eventStatusLottie);
        
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync( this);

        LocationManager locationManager = (LocationManager) EventInfo.this.getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        latitude = getIntent().getDoubleExtra("latitude",location.getLatitude());
        longitude = getIntent().getDoubleExtra("longitude",location.getLongitude());
        event_id = getIntent().getStringExtra("event_id");

        launchMaps.setOnClickListener(view -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q="+latitude+","+longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        //Get the event info
        db.collection("Events").document(event_id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    eventTitle.setText(task.getResult().getString("eventTitle"));
                    eventDescription.setText(task.getResult().getString("eventDesc"));
                    eventPostedOn.setText(task.getResult().getString("date")+" "+task.getResult().getString("time"));
                    eventPostedBy.setText(task.getResult().getString("posted_by"));
                    eventType.setText(task.getResult().getString("eventType"));
                    eventAddress.setText(task.getResult().getString("eventAddr"));
                    if(task.getResult().getString("eventType").equals("Once time")){
                        eventHappens.setText(String.format("Event on %s at %s", task.getResult().getString("eventDate"), task.getResult().getString("eventTime")));
                        eventStatus.setText(compareDateToPresent(task.getResult().getString("eventDate")));
                    }else {
                        eventHappens.setText(String.format("This event happens %s", task.getResult().getString("eventRecurringOption")));
                        eventStatus.setText("Active");
                    }
                }
            }
        });
    }

    //All required methods for map
    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> enableLocationComponent(style));
    }

    private void enableLocationComponent(@NonNull Style loadedMapStyle) {

        if (PermissionsManager.areLocationPermissionsGranted(EventInfo.this)) {

            LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(EventInfo.this)
                    .build();
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(EventInfo.this, loadedMapStyle)
                            .locationComponentOptions(customLocationComponentOptions)
                            .build());
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.zoomWhileTracking(200,0);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.NORMAL);

            //get the location of the clicked Event from model object.
            Location lastKnownLocation = locationComponent.getLastKnownLocation();
            if (lastKnownLocation != null)
            {
                mapboxMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).setIcon(
                        IconFactory.getInstance(EventInfo.this).fromResource(R.drawable.place_marker_green60)));
                mapboxMap.easeCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(latitude, longitude), 15), 7000);
            }

        } else {
            permissionsManager = new PermissionsManager((PermissionsListener) this);
            permissionsManager.requestLocationPermissions(EventInfo.this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(EventInfo.this, "We need user permission in order to function the app properly", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(this::enableLocationComponent);
        } else {
            Toast.makeText(EventInfo.this, "User permission is not given", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
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