package com.example.geopedia;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddLocation extends AppCompatActivity {

    EditText locationTitleFld,locationDescFld;
    TextView categoryLocation;
    Button submitLocationBtn;
    CheckBox customLocationCheckbox;
    String[] categoryNames = {"Restaurants", "Takeaway", "Bars", "Delivery", "Cafes", "Parks", "Live music", "Gyms", "Films", "Art", "Museums", "Attractions", "Libraries", "Nightlife", "Groceries", "Shopping", "Beauty supplies", "Electronics", "Car dealers", "Sporting goods", "Home & Garden", "Convinience shop", "Clothing", "Food shelters", "Dry cleaning", "Night shelters", "Electronic vehicle charging", "Hotels", "Petrol", "ATMs", "Hospital & clinics", "Beauty salons","Post", "Car hire", "Parking", "Car repair", "Chemists", "Car wash"};
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
          validationsforFields();
        });

    }

    private void validationsforFields() {
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
    }
}