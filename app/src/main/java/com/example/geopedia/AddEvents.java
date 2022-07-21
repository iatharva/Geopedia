package com.example.geopedia;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions;

public class AddEvents extends AppCompatActivity {

    EditText eventTitleFld, eventDescFld;
    CheckBox customLocationCheckbox;
    Button submitEventBtn;
    private static final int REQUEST_CODE = 56789;
    private static final int PLACE_SELECTION_REQUEST_CODE = 56789;
    String eventLatitude,eventLongitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this,getString(R.string.mapbox_private_token));
        setContentView(R.layout.activity_add_events);
        eventTitleFld = findViewById(R.id.eventTitleFld);
        eventDescFld = findViewById(R.id.eventDescFld);
        customLocationCheckbox = findViewById(R.id.customLocationCheckbox);
        submitEventBtn = findViewById(R.id.submitEventBtn);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        //if custom location is checked, open maps app to select location
        customLocationCheckbox.setOnClickListener(v -> {
            if(customLocationCheckbox.isChecked()){
                //Launch Placepicker activity
                goToPickerActivity(currentLatitude,currentLongitude);
            }
        });

        //Save the event request and save to database.
        submitEventBtn.setOnClickListener(v -> {

        });
    }

    /**
     * Set up the PlacePickerOptions and startActivityForResult
     */
    private void goToPickerActivity(double currentLatitude, double currentLongitude) {
        Intent intent = new PlacePicker.IntentBuilder()
                .accessToken(getString(R.string.mapbox_public_token))
                .placeOptions(
                        PlacePickerOptions.builder()
                                .statingCameraPosition(
                                        new CameraPosition.Builder()
                                                .target(new LatLng(currentLatitude, currentLongitude))
                                                .zoom(16)
                                                .build())
                                .build())
                .build(this);
        startActivityForResult(intent, PLACE_SELECTION_REQUEST_CODE);
    }

    /**
    * This fires after a location is selected in the Places Plugin's PlacePickerActivity.
    * @param requestCode code that is a part of the return to this activity
    * @param resultCode code that is a part of the return to this activity
    * @param data the data that is a part of the return to this activity
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // Retrieve the information from the selected location's CarmenFeature
            CarmenFeature carmenFeature = PlacePicker.getPlace(data);

            // Set the TextView text to the entire CarmenFeature. The CarmenFeature
            // also be parsed through to grab and display certain information such as
            // its placeName, text, or coordinates.
            if (carmenFeature != null) {
                eventDescFld.setText(carmenFeature.toJson());
            }
        }
    }

}