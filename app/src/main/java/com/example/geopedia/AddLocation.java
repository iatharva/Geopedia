package com.example.geopedia;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddLocation extends AppCompatActivity {

    EditText locationTitleFld,locationDescFld;
    TextView categoryLocation;
    TextView eventlatitude,eventlongitude;
    Button submitLocationBtn;
    CheckBox customLocationCheckbox;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    String[] categoryNames = {"Restaurants", "Takeaway", "Bars", "Delivery", "Cafes", "Parks", "Live music", "Gyms", "Films", "Art", "Museums", "Attractions", "Libraries", "Nightlife", "Groceries", "Shopping", "Beauty supplies", "Electronics", "Car dealers", "Sporting goods", "Home & Garden", "Convinience shop", "Clothing", "Food shelters", "Dry cleaning", "Night shelters", "Electronic vehicle charging", "Hotels", "Petrol", "ATMs", "Hospital & clinics", "Beauty salons","Post", "Car hire", "Parking", "Car repair", "Chemists", "Car wash"};
    private static final int REQUEST_CODE = 56789;
    private static final int PLACE_SELECTION_REQUEST_CODE = 56789;
    String eventLatitude,eventLongitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        locationTitleFld = findViewById(R.id.locationTitleFld);
        locationDescFld = findViewById(R.id.locationDescFld);
        categoryLocation = findViewById(R.id.categoryLocation);
        submitLocationBtn = findViewById(R.id.submitLocationBtn);
        customLocationCheckbox = findViewById(R.id.customLocationCheckbox);
        eventlatitude = findViewById(R.id.eventlatitude);
        eventlongitude = findViewById(R.id.eventlongitude);

        //Select the category dialog box
        categoryLocation.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(AddLocation.this);
            builder.setTitle("Select Category");
            Toast.makeText(this,"Select the category of the location",Toast.LENGTH_SHORT).show();
            //show the list in categoryNames array in the dialog box with checkbox single choice item
            builder.setSingleChoiceItems(categoryNames, -1, (dialogInterface, i) -> {
                categoryLocation.setText(categoryNames[i]);
                dialogInterface.dismiss();
            });
            builder.show();
        } );

        submitLocationBtn.setOnClickListener(view -> {
            validationsforFieldsAndAdd();
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

    private void validationsforFieldsAndAdd() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission")
        Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double currentLatitude = location1.getLatitude();
        double currentLongitude = location1.getLongitude();

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

        String locationTitle = locationTitleFld.getText().toString().trim();
        String locationDescription = locationDescFld.getText().toString().trim();
        String locationCategory = categoryLocation.getText().toString().trim();

        //Validation
        if (TextUtils.isEmpty(locationTitle)) {
            locationTitleFld.setError("Location Name is required");
            return;
        }
        if (TextUtils.isEmpty(locationDescription)) {
            locationDescFld.setError("Description is required");
            return;
        }
        if (TextUtils.isEmpty(locationCategory) || locationCategory.equals("Category")) {
            Toast.makeText(this, "Location Category needs to be selected", Toast.LENGTH_SHORT).show();
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

        double LatitudeDouble = Double.parseDouble(eventLatitude);
        double LongitudeDouble = Double.parseDouble(eventLongitude);
        //Create a new location object
        HashMap<String, Object> location = new HashMap<>();
        location.put("locationTitle", locationTitle);
        location.put("locationDescription", locationDescription);
        location.put("locationCategory", locationCategory);
        location.put("locationLatitude", LatitudeDouble);
        location.put("locationLongitude", LongitudeDouble);
        location.put("isDeleted", "0");
        location.put("isApproved", "0");
        location.put("updatedBy", "");
        location.put("isDeclined", "0");
        location.put("userId",current_user_id);
        location.put("locationId", randomString);


        db.collection("Locations").document(randomString).set(location).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Toast.makeText(AddLocation.this, "Request submitted, Now you just need to wait!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AddLocation.this, HomeUser.class);
                startActivity(intent);
                finish();
            }
            else 
            {
                //Exception Case
                Toast.makeText(AddLocation.this, "Error! " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}