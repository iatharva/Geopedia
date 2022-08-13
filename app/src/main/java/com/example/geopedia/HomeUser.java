package com.example.geopedia;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.geopedia.extras.LogoutDialog;
import com.example.geopedia.usermenu.Uevents;
import com.example.geopedia.usermenu.Uhome;
import com.example.geopedia.usermenu.Uquestions;
import com.example.geopedia.usermenu.Usettings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class HomeUser extends AppCompatActivity {

    Button HomeBtn,QuestionsBtn,EventsBtn;
    ImageButton SettingsBtn;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user);
        HomeBtn = findViewById(R.id.HomeBtn);
        QuestionsBtn = findViewById(R.id.QuestionsBtn);
        EventsBtn = findViewById(R.id.EventsBtn);
        SettingsBtn = findViewById(R.id.SettingsBtn);

        //Set by default Home fragment
        replaceFragment(new Uhome());
        //Setting respective fragment on selection
        HomeBtn.setOnClickListener(v -> replaceFragment(new Uhome()));
        QuestionsBtn.setOnClickListener(v -> replaceFragment(new Uquestions()));
        EventsBtn.setOnClickListener(view -> replaceFragment(new Uevents()));
        SettingsBtn.setOnClickListener(view -> replaceFragment(new Usettings()));
    }

    /**
     * Replace the fragment with the selected option
     */
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout,fragment);
        fragmentTransaction.commit();
    }

    /**
     * Create and show options in action bar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_admin, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logoutButtonHeader) {
            openDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void openDialog(){
        LogoutDialog logoutdialog=new LogoutDialog();
        logoutdialog.show(getSupportFragmentManager(),"Log out Dialog");
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getUserLocation();
    }
    public void getUserLocation()
    {
        LocationManager locationManager = (LocationManager) HomeUser.this.getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        //Update the Users collection with currentLatitude and currentLongitude
        db.collection("Users").document(current_user_id).update("LastLatitude",currentLatitude);
        db.collection("Users").document(current_user_id).update("LastLongitude",currentLongitude);
        
    }
}