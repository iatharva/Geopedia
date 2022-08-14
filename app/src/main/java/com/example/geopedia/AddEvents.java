package com.example.geopedia;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddEvents extends AppCompatActivity {

    EditText eventTitleFld, eventDescFld,eventAddFld;
    CheckBox customLocationCheckbox;
    Button submitEventBtn;
    TextView eventlatitude,eventlongitude,eventOnDate,eventOnTime,eventRecurringoptiontitle;
    Spinner eventTypeSpinner,eventRecurringOption;
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
        eventAddFld = findViewById(R.id.eventAddFld);
        customLocationCheckbox = findViewById(R.id.customLocationCheckbox);
        submitEventBtn = findViewById(R.id.submitEventBtn);
        eventlatitude = findViewById(R.id.eventlatitude);
        eventlongitude = findViewById(R.id.eventlongitude);
        eventTypeSpinner = findViewById(R.id.eventTypeSpinner);
        eventRecurringOption = findViewById(R.id.eventRecurringOption);
        eventRecurringoptiontitle = findViewById(R.id.eventRecurringoptiontitle);
        eventOnDate = findViewById(R.id.eventOnDate);
        eventOnTime = findViewById(R.id.eventOnTime);

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Once time");
        arrayList.add("Recurring");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(AddEvents.this,android.R.layout.simple_spinner_item, arrayList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventTypeSpinner.setAdapter(arrayAdapter);

        //if eventType is Once time then hide eventRecurringOption spinner & show eventDate and eventTime,
        //else if eventType is Recurring then hide eventDate and eventTime and show eventRecurringOption spinner
        eventTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i==0){
                    eventRecurringOption.setVisibility(View.GONE);
                    eventRecurringoptiontitle.setVisibility(View.GONE);
                    eventOnDate.setVisibility(View.VISIBLE);
                    eventOnTime.setVisibility(View.VISIBLE);
                }
                else if(i==1){
                    eventRecurringOption.setVisibility(View.VISIBLE);
                    eventRecurringoptiontitle.setVisibility(View.VISIBLE);
                    eventOnDate.setVisibility(View.GONE);
                    eventOnTime.setVisibility(View.GONE);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> adapterView) {}
        });
       

        ArrayList<String> arrayList1 = new ArrayList<>();
        arrayList1.add("Daily");
        arrayList1.add("Weekly");
        arrayList1.add("Monthly");
        arrayList1.add("Yearly");
        ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<String>(AddEvents.this,android.R.layout.simple_spinner_item, arrayList1);
        arrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventRecurringOption.setAdapter(arrayAdapter1);

        eventOnDate.setOnClickListener(view -> {
            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(AddEvents.this, (view1, year, month, dayOfMonth) -> {
                eventOnDate.setText(year + "-"+(month + 1)+"-"+dayOfMonth);
            }, 2022, 7, 15);
            datePickerDialog.show();

        });

        eventOnTime.setOnClickListener(view -> {
            android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(AddEvents.this, (view1, hourOfDay, minute) -> {
                eventOnTime.setText(hourOfDay + ":" + minute + " " + ((hourOfDay < 12) ? "AM" : "PM"));
            }, 0, 0, false);
            timePickerDialog.show();
        });

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
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String eventTitle = eventTitleFld.getText().toString();
            String eventDesc = eventDescFld.getText().toString();
            String eventAddr = eventAddFld.getText().toString();

            //Validation
            if (TextUtils.isEmpty(eventTitle)) {
                eventTitleFld.setError("Event title is required");
                eventTitleFld.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(eventDesc)) {
                eventDescFld.setError("Event description is required");
                eventDescFld.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(eventAddr)) {
                eventAddFld.setError("Event address is required");
                eventAddFld.requestFocus();
                return;
            }

            if(customLocationCheckbox.isChecked()){
                eventLatitude = eventlatitude.getText().toString().trim();
                eventLongitude = eventlongitude.getText().toString().trim();
            }
            else{
                eventLatitude = String.valueOf(currentLatitude);
                eventLongitude = String.valueOf(currentLongitude);
            }

            String eventType = eventTypeSpinner.getSelectedItem().toString();
            if(eventType.equals("Once time")){
                String eventDate = eventOnDate.getText().toString();
                String eventTime = eventOnTime.getText().toString();
                if(eventDate.equals("event on Date")){
                    eventOnDate.setError("Event date is required");
                    eventOnDate.requestFocus();
                    return;
                }
                if(eventTime.equals("event on Time")){
                    eventOnTime.setError("Event time is required");
                    eventOnTime.requestFocus();
                    return;
                }
            }
            else if(eventType.equals("Recurring")){
                String eventRecurringOptiondata = eventRecurringOption.getSelectedItem().toString();
                if(TextUtils.isEmpty(eventRecurringOptiondata)){
                    Toast.makeText(getApplicationContext(),"Please select the recurring type",Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            else{
                Toast.makeText(getApplicationContext(),"Please select event type",Toast.LENGTH_SHORT).show();
                return;
            }

            if(eventType.equals("Once time"))
            {
                saveTheEvent(userId,eventTitle,eventDesc,eventAddr,eventLatitude,eventLongitude,eventType,"",eventOnDate.getText().toString(),eventOnTime.getText().toString());
            }
            else {
                String eventRecurringOptiondata = eventRecurringOption.getSelectedItem().toString();
                saveTheEvent(userId,eventTitle,eventDesc,eventAddr,eventLatitude,eventLongitude,eventType,eventRecurringOptiondata,"","");
            }
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

                //Sample response {"type":"Feature","id":"poi.644245183508","geometry":{"coordinates":[73.006172,20.270833],"type":"Point"},"properties":{"foursquare":"5303a0e611d2eac30b655658","landmark":true,"address":"HDFC Bank ATM","category":"bank, finance, financial"},"text":"HDFC Bank ATM","place_name":"HDFC Bank ATM, HDFC Bank ATM, Silvassa, Dadra and Nagar Haveli, India","place_type":["poi"],"center":[73.006172,20.270833],"context":[{"id":"place.2985005810871660","text":"Silvassa","wikidata":"Q318404"},{"id":"district.3327407548291910","text":"Dadra and Nagar Haveli","wikidata":"Q33158489"},{"id":"region.8712006080422370","text":"Dadra and Nagar Haveli","short_code":"IN-DN","wikidata":"Q46107"},{"id":"country.14770391688208260","text":"India","short_code":"in","wikidata":"Q668"}],"relevance":1.0}
                //get the latitude and longitude from the json response
                Map<String, Object> jsonMap = new Gson().fromJson(json, HashMap.class);
                Map<String, Object> geometryMap = (Map<String, Object>) jsonMap.get("geometry");
                Map<String, Object> propertiesMap = (Map<String, Object>) jsonMap.get("properties");
                //get the coordinates arrayList from the geometryMap
                ArrayList<Object> coordinates = (ArrayList<Object>) geometryMap.get("coordinates");
                //get the latitude and longitude from the coordinates arrayList
                eventLatitude = coordinates.get(1).toString();
                eventLongitude = coordinates.get(0).toString();
                eventlatitude.setText(eventLatitude);
                eventlongitude.setText(eventLongitude);
            }
        }
    }

    /**
     * Save the event to the database
     */
    private void saveTheEvent(String userId, String eventTitle, String eventDesc,String eventAddr, String eventLatitude, String eventLongitude, String eventType, String eventRecurringOptiondata, String eventDate, String eventTime) {
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

        //Convert eventLatitude and eventLongitude to double
        double eventLatitudeDouble = Double.parseDouble(eventLatitude);
        double eventLongitudeDouble = Double.parseDouble(eventLongitude);

        //Create a new event object
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", randomString);
        event.put("eventTitle", eventTitle);
        event.put("eventDesc", eventDesc);
        event.put("eventAddr",eventAddr);
        event.put("eventLatitude", eventLatitudeDouble);
        event.put("eventLongitude", eventLongitudeDouble);
        event.put("userId", userId);
        event.put("date", currentDate);
        event.put("time", currentTime);
        event.put("eventStatus", "Pending");
        event.put("isActive", "1");
        event.put("isDeleted", "0");
        event.put("eventType", eventType);
        event.put("eventRecurringOption", eventRecurringOptiondata);
        event.put("eventDate", eventDate);
        event.put("eventTime", eventTime);

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