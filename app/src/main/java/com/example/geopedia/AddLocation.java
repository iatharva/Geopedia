package com.example.geopedia;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

public class AddLocation extends AppCompatActivity {

    EditText locationTitleFld,locationDescFld;
    TextView categoryLocation;
    Button submitLocationBtn;
    CheckBox customLocationCheckbox;
    String[] categoryNames = {"Restaurants", "Takeaway", "Bars", "Delivery", "Cafes", "Parks", "Live music", "Gyms", "Films", "Art", "Museums", "Attractions", "Libraries", "Nightlife", "Groceries", "Shopping", "Beauty supplies", "Electronics", "Car dealers", "Sporting goods", "Home & Garden", "Convinience shop", "Clothing", "Food shelters", "Dry cleaning", "Night shelters", "Electronic vehicle charging", "Hotels", "Petrol", "ATMs", "Hospital & clinics", "Beauty salons","Post", "Car hire", "Parking", "Car repair", "Chemists", "Car wash"};
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        locationTitleFld = findViewById(R.id.locationTitleFld);
        locationDescFld = findViewById(R.id.locationDescFld);
        categoryLocation = findViewById(R.id.categoryLocation);
        submitLocationBtn = findViewById(R.id.submitLocationBtn);
        customLocationCheckbox = findViewById(R.id.customLocationCheckbox);

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

    }

    private void validationsforFieldsAndAdd() {

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

        //Create a new location object
        HashMap<String, Object> location = new HashMap<>();
        location.put("locationTitle", locationTitle);
        location.put("locationDescription", locationDescription);
        location.put("locationCategory", locationCategory);
        location.put("locationLatitude", 0);
        location.put("locationLongitude", 0);
        location.put("locationRating", 0);
        location.put("locationRatingCount", 0);
        location.put("isDeleted", "0");
        location.put("isApproved", "0");
        location.put("updatedBy", "");
        location.put("isDeclined", "0");
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