package com.example.geopedia;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.geopedia.Info.EventInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class EventInfoAdmin extends AppCompatActivity {

    private LottieAnimationView eventStatusLottie;
    private TextView eventTitle,eventDescription,eventPostedOn,eventPostedBy,eventType,eventHappens,eventStatus,eventAddress;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    private Double latitude,longitude;
    private String event_id;
    private Button launchMaps,doubt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info_admin);

        launchMaps = findViewById(R.id.launchMaps);
        eventTitle = findViewById(R.id.eventTitle);
        eventDescription = findViewById(R.id.eventDescription);
        eventPostedOn = findViewById(R.id.eventPostedOn);
        eventPostedBy = findViewById(R.id.eventPostedBy);
        eventType = findViewById(R.id.eventType);
        eventHappens = findViewById(R.id.eventHappens);
        eventStatus = findViewById(R.id.eventStatus);
        eventAddress = findViewById(R.id.eventAddress);
        eventStatusLottie = findViewById(R.id.eventStatusLottie);
        doubt = findViewById(R.id.doubt);

        LocationManager locationManager = (LocationManager) EventInfoAdmin.this.getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        event_id = getIntent().getStringExtra("event_id");

        launchMaps.setOnClickListener(view -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q="+latitude+","+longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        doubt.setOnClickListener(view -> {
            Intent intent = new Intent(EventInfoAdmin.this, Feedback.class);
            startActivity(intent);
        });

        //Get the event info
        db.collection("Events").document(event_id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    eventTitle.setText(task.getResult().getString("eventTitle"));
                    eventDescription.setText(task.getResult().getString("eventDesc"));
                    eventPostedOn.setText(task.getResult().getString("date")+" "+task.getResult().getString("time"));
                    eventPostedBy.setText(task.getResult().getString("posted_by"));
                    eventType.setText(task.getResult().getString("eventType"));
                    eventAddress.setText(task.getResult().getString("eventAddr"));
                    if(task.getResult().getString("eventType").equals("Once time")){
                        eventHappens.setText(String.format("Event on %s at %s", task.getResult().getString("eventDate"), task.getResult().getString("eventTime")));
                        eventStatus.setText(compareDateToPresent(task.getResult().getString("eventDate")));
                    }else {
                        eventHappens.setText(String.format("This event happens %s", task.getResult().getString("eventRecurringOption")));
                        eventStatus.setText("Active");
                    }

                    showPostedBy(task.getResult().getString("userId"));
                }
            }
        });
    }

    public String compareDateToPresent(String eventDate)
    {
        //date in eventDate will be like 2022-8-15 i.e. yyyy-mm-dd where m and d can be single digit
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String[] currentDateArray = currentDate.split("-");
        String[] eventDateArray = eventDate.split("-");
        if(Integer.parseInt(currentDateArray[0])>Integer.parseInt(eventDateArray[0]))
        {
            return "Completed";
        }
        else if(Integer.parseInt(currentDateArray[0])==Integer.parseInt(eventDateArray[0]))
        {
            if(Integer.parseInt(currentDateArray[1])>Integer.parseInt(eventDateArray[1]))
            {
                return "Completed";
            }
            else if(Integer.parseInt(currentDateArray[1])==Integer.parseInt(eventDateArray[1]))
            {
                if(Integer.parseInt(currentDateArray[2])>Integer.parseInt(eventDateArray[2]))
                {
                    return "Completed";
                }
                else if(Integer.parseInt(currentDateArray[2])==Integer.parseInt(eventDateArray[2]))
                {
                    return "Active Today";
                }
                else
                {
                    return "Active in Future";
                }
            }
            else
            {
                return "Active in Future";
            }
        }
        else
        {
            return "Active in Future";
        }
    }

    private void showPostedBy(String userId)
    {
        //get the userId of the user who posted the event
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
    }
}