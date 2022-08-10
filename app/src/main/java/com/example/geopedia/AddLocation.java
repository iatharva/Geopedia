package com.example.geopedia;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class AddLocation extends AppCompatActivity {

    EditText locationTitleFld,locationDescFld,categoryLocation;
    Button submitLocationBtn;
    CheckBox customLocationCheckbox;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        locationTitleFld = findViewById(R.id.locationTitleFld);
        locationDescFld = findViewById(R.id.locationDescFld);
        categoryLocation = findViewById(R.id.categoryLocation);
        submitLocationBtn = findViewById(R.id.submitLocationBtn);
        customLocationCheckbox = findViewById(R.id.customLocationCheckbox);
    }
}