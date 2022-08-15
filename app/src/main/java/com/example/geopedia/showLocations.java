package com.example.geopedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.geopedia.Info.LocationInfo;
import com.example.geopedia.extras.CustomLocation;
import com.example.geopedia.extras.Question;
import com.example.geopedia.usermenu.Uquestions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class showLocations extends AppCompatActivity {

    private RecyclerView recycler_showlocations_user;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_locations);

        SwipeRefreshLayout pullToRefreshshowLocations = findViewById(R.id.pullToRefreshshowLocations);
        recycler_showlocations_user = findViewById(R.id.recycler_showlocations_user);
        recycler_showlocations_user.setHasFixedSize(true);
        recycler_showlocations_user.setLayoutManager(new LinearLayoutManager(this));
        recycler_showlocations_user.setAdapter(adapter);
        showallLocations("All");

        pullToRefreshshowLocations.setOnRefreshListener(() -> {
            showallLocations("All");
            pullToRefreshshowLocations.setRefreshing(false);
        });
    }

    private void showallLocations(String filter)
    {
        Query query;
        //Get the current latitude and longitude
        LocationManager locationManager = (LocationManager) showLocations.this.getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        if(filter.equals("All"))
        {
            query = db.collection("Locations").whereEqualTo("isApproved","1");
        }
        else
        {
            query = db.collection("Locations").whereEqualTo("type", filter).orderBy("name", Query.Direction.ASCENDING);
        }

        FirestoreRecyclerOptions<CustomLocation> options = new FirestoreRecyclerOptions.Builder<CustomLocation>()
                .setQuery(query, CustomLocation.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<CustomLocation, FiltersViewHolder>(options) {
            @NotNull
            @Override
            public FiltersViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_locations_user, parent, false);
                return new FiltersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FiltersViewHolder viewHolder, int position, @NonNull CustomLocation model) {

                /*
                if(model.getLocationLatitude() >= currentLatitude - 0.01 && model.getLocationLatitude() <= currentLatitude + 0.01 && model.getLocationLongitude() >= currentLongitude - 0.01 && model.getLocationLongitude() <= currentLongitude + 0.01)
                    viewHolder.locationCard.setVisibility(View.VISIBLE);
                else
                {
                    viewHolder.locationCard.setVisibility(View.INVISIBLE);
                    viewHolder.locationCard.getLayoutParams().height = 0;
                    viewHolder.locationCard.requestLayout();
                }

                 */

                viewHolder.locationName.setText(model.getLocationTitle());
                viewHolder.locationCategory.setText(model.getLocationCategory());

                double dis = calculateDistance(currentLatitude,currentLongitude,Double.valueOf(model.getLocationLatitude()), Double.valueOf(model.getLocationLongitude()));
                //convert double value into 0.00 format
                String distance = String.format("%.2f", dis);
                viewHolder.distanceFromYou.setText(distance + " km(s) away");

                if(model.isApproved.equals("0") && model.isDeclined.equals("0") && model.isDeleted.equals("0"))
                {
                    //set animation to pending and locationstatus to pending
                    viewHolder.lottieAnimationLocation.setAnimation("pending_location_animation.json");
                    viewHolder.lottieAnimationLocation.playAnimation();
                    viewHolder.locationStatus.setText("Pending");
                }
                else if(model.isApproved.equals("1") && model.isDeclined.equals("0") && model.isDeleted.equals("0"))
                {
                    //set animation to approved and locationstatus to approved
                    viewHolder.lottieAnimationLocation.setAnimation("approved_location_animation.json");
                    viewHolder.lottieAnimationLocation.playAnimation();
                    viewHolder.locationStatus.setText("Approved");
                }
                else if(model.isApproved.equals("0") && model.isDeclined.equals("1") && model.isDeleted.equals("0"))
                {
                    //set animation to declined and locationstatus to declined
                    viewHolder.lottieAnimationLocation.setAnimation("declined_location_animation.json");
                    viewHolder.lottieAnimationLocation.playAnimation();
                    viewHolder.locationStatus.setText("Declined");
                }
                else if(model.isApproved.equals("0") && model.isDeclined.equals("0") && model.isDeleted.equals("1"))
                {
                    //set animation to deleted and locationstatus to deleted
                    viewHolder.lottieAnimationLocation.setAnimation("pending_location_animation.json");
                    viewHolder.lottieAnimationLocation.playAnimation();
                    viewHolder.locationStatus.setText("Deleted");
                }

                viewHolder.locationCard.setOnClickListener(view -> {
                    //Intent to info activity.
                    Intent intent = new Intent(showLocations.this, LocationInfo.class);
                    intent.putExtra("location_id", model.getLocationId());
                    intent.putExtra("latitude", model.getLocationLatitude());
                    intent.putExtra("longitude", model.getLocationLongitude());
                    intent.putExtra("distance", distance);
                    startActivity(intent);

                });
            }
        };
        recycler_showlocations_user.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FiltersViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView locationName,locationCategory,locationStatus,distanceFromYou;
        LottieAnimationView lottieAnimationLocation;
        LinearLayout locationCard;

        FiltersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            locationName = mView.findViewById(R.id.locationName);
            locationCategory = mView.findViewById(R.id.locationCategory);
            locationStatus = mView.findViewById(R.id.locationStatus);
            distanceFromYou = mView.findViewById(R.id.distanceFromYou);
            lottieAnimationLocation = mView.findViewById(R.id.lottieAnimationLocation);
            locationCard = mView.findViewById(R.id.locationCard);
        }
    }

    private double calculateDistance(double currentLatitude, double currentLongitude, double latitude, double longitude) {
        double theta = currentLongitude - longitude;
        double dist = Math.sin(deg2rad(currentLatitude)) * Math.sin(deg2rad(latitude)) + Math.cos(deg2rad(currentLatitude)) * Math.cos(deg2rad(latitude)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}