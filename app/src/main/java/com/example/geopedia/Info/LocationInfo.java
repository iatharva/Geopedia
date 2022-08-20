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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geopedia.Feedback;
import com.example.geopedia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

import java.util.List;
import java.util.Objects;

public class LocationInfo extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private TextView locationTitle,locationDescription,locationCategory,eventStatus,eventPostedOn,eventPostedBy,approvedBy,rating;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    private Double latitude,longitude;
    private String location_id,distance;
    private Button launchMaps,doubt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_info);
        mapView = findViewById(R.id.mapView);
        locationTitle = findViewById(R.id.locationTitle);
        locationDescription = findViewById(R.id.locationDescription);
        locationCategory = findViewById(R.id.locationCategory);
        eventStatus = findViewById(R.id.eventStatus);
        eventPostedOn = findViewById(R.id.eventPostedOn);
        eventPostedBy = findViewById(R.id.eventPostedBy);
        approvedBy = findViewById(R.id.approvedBy);
        rating = findViewById(R.id.rating);
        launchMaps = findViewById(R.id.launchMaps);
        doubt = findViewById(R.id.doubt);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync( this);
        LocationManager locationManager = (LocationManager) LocationInfo.this.getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        latitude = getIntent().getDoubleExtra("latitude",location.getLatitude());
        longitude = getIntent().getDoubleExtra("longitude",location.getLongitude());
        location_id = getIntent().getStringExtra("location_id");
        distance = getIntent().getStringExtra("distance");
        rating.setText(distance+" kms away");

        launchMaps.setOnClickListener(view -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q="+latitude+","+longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        launchMaps.setOnClickListener(view -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q="+latitude+","+longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        doubt.setOnClickListener(view -> {
            Intent intent = new Intent(LocationInfo.this, Feedback.class);
            startActivity(intent);
        });

        //Get the event info
        db.collection("Locations").document(location_id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    locationTitle.setText(task.getResult().getString("locationTitle"));
                    locationDescription.setText(task.getResult().getString("locationDescription"));
                    locationCategory.setText(task.getResult().getString("locationCategory"));
                    eventStatus.setText("Approved");
                    setNames(task.getResult().getString("updatedBy"),task.getResult().getString("userId"));
                }
            }
        });
    }

    private void setNames(String updatedBy, String userId) {
        DocumentReference userRef = db.collection("Users").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful())
            {
                DocumentSnapshot user = task.getResult();
                if(user.exists())
                {
                    String userName = user.getString("FName")+" "+user.getString("LName");
                    eventPostedBy.setText(String.format("%s", userName));
                }
            }
        });

        DocumentReference userRef1 = db.collection("Users").document(updatedBy);
        userRef1.get().addOnCompleteListener(task -> {
            if(task.isSuccessful())
            {
                DocumentSnapshot user = task.getResult();
                if(user.exists())
                {
                    String userName = user.getString("FName")+" "+user.getString("LName");
                    approvedBy.setText(String.format("%s", userName));
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

        if (PermissionsManager.areLocationPermissionsGranted(LocationInfo.this)) {

            LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(LocationInfo.this)
                    .build();
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(LocationInfo.this, loadedMapStyle)
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
                        IconFactory.getInstance(LocationInfo.this).fromResource(R.drawable.place_marker_green60)));
                mapboxMap.easeCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(latitude, longitude), 15), 7000);
            }

        } else {
            permissionsManager = new PermissionsManager((PermissionsListener) this);
            permissionsManager.requestLocationPermissions(LocationInfo.this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(LocationInfo.this, "We need user permission in order to function the app properly", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(this::enableLocationComponent);
        } else {
            Toast.makeText(LocationInfo.this, "User permission is not given", Toast.LENGTH_LONG).show();
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
}