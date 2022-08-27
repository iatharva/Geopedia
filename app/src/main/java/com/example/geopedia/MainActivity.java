package com.example.geopedia;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Toast;

import com.example.geopedia.accounts.LogIn;
import com.example.geopedia.extras.LogInAsDialog;
import com.fivesoft.library.PermissionRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userid,type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        //Check permissions and location and network on
        if(PermissionRequest.isPermissionGranted(Manifest.permission.INTERNET, this)){
            //You have permission Now check for Location
            if(PermissionRequest.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION, this)){
                //You have permission Now check for exact
                if(PermissionRequest.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION, this)){
                    //You have permission
                } else {
                    //Permission not granted
                    PermissionRequest.from(this)
                            .setPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                            .setListener(isGranted -> {
                                if(!isGranted)
                                {
                                    Toast.makeText(MainActivity.this, "Sorry to see you, but we need that permission in order for smooth user experience", Toast.LENGTH_SHORT).show();
                                    System.exit(1);
                                }
                            }).request();
                }
            } else {
                //Permission not granted
                PermissionRequest.from(this)
                        .setPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        .setListener(isGranted -> {
                            if(!isGranted)
                            {
                                Toast.makeText(MainActivity.this, "Sorry to see you, but we need that permission in order for smooth user experience", Toast.LENGTH_SHORT).show();
                                System.exit(1);
                            }
                        }).request();
            }
        } else {
            //Permission not granted
            PermissionRequest.from(this)
                    .setPermission(Manifest.permission.INTERNET)
                    .setListener(isGranted -> {
                        if(!isGranted)
                        {
                            Toast.makeText(MainActivity.this, "Sorry to see you, but we need that permission in order for smooth user experience", Toast.LENGTH_SHORT).show();
                            System.exit(1);
                        }
                    }).request();
        }

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            MainActivity.this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            Toast.makeText(MainActivity.this, "Please enable location and relaunch app", Toast.LENGTH_SHORT).show();
            System.exit(1);
        }
        else
        {
            //Check signed in or not
            CheckAndRedirect();
        }
        //Finished permissions
    }

    private void CheckAndRedirect() {
        mAuthStateListener = firebaseAuth -> {
            FirebaseUser mFirebaseUser = firebaseAuth.getCurrentUser();
            if (mFirebaseUser != null) {
                //User of customer start
                userid = mFirebaseUser.getUid();
                DocumentReference typeref = db.collection("Users").document(userid);
                typeref.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        type = documentSnapshot.getString("IsAdmin");
                        assert type != null;
                        if (type.equals("1")) {
                            openLogInAsDialog();
                        } else if (type.equals("0")) {
                            Intent intent = new Intent(MainActivity.this, HomeUser.class);
                            //intent.putExtra("user_id" ,userid);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
                //END
            } else {
                Intent i = new Intent(MainActivity.this, LogIn.class);
                startActivity(i);
                finish();
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        int SPLASH_TIME_OUT = 1700;
        new Handler().postDelayed(() -> logincheck(), SPLASH_TIME_OUT);
    }

    public void logincheck(){
        fAuth.addAuthStateListener(mAuthStateListener);
    }

    public void openLogInAsDialog(){

        LogInAsDialog logInAsDialog=new LogInAsDialog();
        logInAsDialog.show(getSupportFragmentManager(),"Log in as");
    }
}