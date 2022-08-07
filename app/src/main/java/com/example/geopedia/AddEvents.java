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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddEvents extends AppCompatActivity {

    EditText eventTitleFld, eventDescFld;
    CheckBox customLocationCheckbox;
    Button submitEventBtn;
    private static final int REQUEST_CODE = 56789;
    private static final int PLACE_SELECTION_REQUEST_CODE = 56789;
    String eventLatitude,eventLongitude;
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
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
            if(eventTitleFld.getText().toString().isEmpty() || eventDescFld.getText().toString().isEmpty()){
                Toast.makeText(getApplicationContext(),"Please enter the Question to submit",Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String eventTitle = eventTitleFld.getText().toString();
            String eventDesc = eventDescFld.getText().toString();
            if(customLocationCheckbox.isChecked()){
                eventLatitude = eventLatitude;
                eventLongitude = eventLongitude;
            }
            else{
                eventLatitude = String.valueOf(currentLatitude);
                eventLongitude = String.valueOf(currentLongitude);
            }

            saveTheEvent(userId,eventTitle,eventDesc,eventLatitude,eventLongitude);
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
            CarmenFeature carmenFeature = PlacePicker.getPlace(data);
            if (carmenFeature != null) {
                //get the json response
                String json = carmenFeature.toJson();
                //get the latitude and longitude from the json response
                //demo response "center":[73.782179,18.512556]
                String[] latLong = json.split("center");
                //get other location info
                //demo response "context":[{"id":"neighborhood.6629186566756840","text":"Shindenagar"},{"id":"locality.17205840760903160","text":"Bavdhan"},{"id":"place.10600147661377950","text":"Mulshi","wikidata":"Q17075340"},{"id":"district.8996689917444500","text":"Pune","wikidata":"Q1797336"},{"id":"region.11712446254386080","text":"Maharashtra","short_code":"IN-MH","wikidata":"Q1191"},{"id":"country.14770391688208260","text":"India","short_code":"in","wikidata":"Q668"}]
                //get the locality name from th econtext array
                String[] context = json.split("context");
                String[] locality = context[1].split("text");
                
                //get current data in eventDescFld
                String eventDesc = eventDescFld.getText().toString();
                if(eventDesc.isEmpty()){
                    eventDescFld.setText("Event is in " + locality[1]);
                }
                else{
                    //append the location info to the eventDescFld
                    eventDescFld.setText(eventDesc + "\nEvent is in " + locality[1]);
                }
            }
        }
    }

    /**
     * Save the event to the database
     */
    private void saveTheEvent(String userId, String eventTitle, String eventDesc, String eventLatitude, String eventLongitude) {
        String randomString = "";
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 28; i++) {
            randomString += characters.charAt((int) Math.floor(Math.random() * characters.length()));
        }
        //get current date and time
        java.util.Date date = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(date);
        java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("hh:mm a");
        String currentTime = sdf2.format(date);

        //Create a new event object
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", randomString);
        event.put("eventTitle", eventTitle);
        event.put("eventDesc", eventDesc);
        event.put("eventLatitude", eventLatitude);
        event.put("eventLongitude", eventLongitude);
        event.put("userId", userId);
        event.put("date", currentDate);
        event.put("time", currentTime);
        event.put("eventStatus", "pending");
        event.put("isActive", 1);
        event.put("isDeleted", 0);
        //need to add fields organizer, eventType, oneTime, eventDate, eventTime, allStepsCompleted.

        //Add question to database
        db.collection("Events").document(randomString).set(event).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Toast.makeText(AddEvents.this, "Request submitted successfully", Toast.LENGTH_SHORT).show();
                //Go back to home screen
                Intent intent = new Intent(AddEvents.this, HomeUser.class);
                startActivity(intent);
                finish();
            }
            else 
            {
                //Exception Case
                Toast.makeText(AddEvents.this, "Error! " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}