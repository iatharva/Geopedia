package com.example.geopedia.adminmenu.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.example.geopedia.CommentFeed;
import com.example.geopedia.HomeUser;
import com.example.geopedia.R;
import com.example.geopedia.databinding.FragmentHomeBinding;
import com.example.geopedia.extras.CustomLocation;
import com.example.geopedia.extras.Question;
import com.example.geopedia.extras.User;
import com.example.geopedia.usermenu.Uquestions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TextView totalUsers,inTheArea;
    private RecyclerView recycler_pending_location_admin;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;
    private SwipeRefreshLayout pullToRefreshPendingLocationAdmin;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        inTheArea = root.findViewById(R.id.inTheArea);
        totalUsers = root.findViewById(R.id.totalUsers);
        recycler_pending_location_admin = root.findViewById(R.id.recycler_pending_location_admin);
        pullToRefreshPendingLocationAdmin = root.findViewById(R.id.pullToRefreshPendingLocationAdmin);
        firebaseFirestore = FirebaseFirestore.getInstance();
        recycler_pending_location_admin.setHasFixedSize(true);
        recycler_pending_location_admin.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_pending_location_admin.setAdapter(adapter);
        showpendingLocations("All",current_user_id);

        pullToRefreshPendingLocationAdmin.setOnRefreshListener(() -> {
            showpendingLocations("All",current_user_id);
            pullToRefreshPendingLocationAdmin.setRefreshing(false);
        });

        return root;
    }

    public void getData() {
        //Count the number of documents present in the collection Users
        db.collection("Users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            totalUsers.setText("0"+String.valueOf(queryDocumentSnapshots.size()));
        });

        //Get the current latitude and longitude
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        //write a query to get the Users where LastLongitude and latitude is in 10 km diameter from currentLatitude and currentLongitude
        db.collection("Users").whereGreaterThanOrEqualTo("LastLongitude", currentLongitude - 0.01)
        .whereLessThanOrEqualTo("LastLongitude", currentLongitude + 0.01)
        .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int count = 0;
                for (QueryDocumentSnapshot document : task.getResult()) {
                        double lastLatitude = document.getDouble("LastLatitude");
                        double lastLongitude = document.getDouble("LastLongitude");
                        if (lastLatitude >= currentLatitude - 0.01 && lastLatitude <= currentLatitude + 0.01 && lastLongitude >= currentLongitude - 0.01 && lastLongitude <= currentLongitude + 0.01) {
                            count++;
                        }
                    inTheArea.setText("0" + String.valueOf(count));
                }
            }
        });
    }

    private void showpendingLocations(String all, String current_user_id) {
        Query query;
        //show all questions where isDeleted (number) is 0
        query = firebaseFirestore.collection("Locations")
        .whereEqualTo("isApproved","0")
        .whereEqualTo("isDeleted","0")
        .whereEqualTo("isDeclined","0");

        FirestoreRecyclerOptions<CustomLocation> options = new FirestoreRecyclerOptions.Builder<CustomLocation>()
                .setQuery(query, CustomLocation.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<CustomLocation, FiltersViewHolder>(options) {
            @NotNull
            @Override
            public FiltersViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_pending_admin, parent, false);

                return new FiltersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NotNull FiltersViewHolder viewHolder, int position, @NotNull CustomLocation model) {

                viewHolder.locationName.setText(model.getLocationTitle());

                //get FName and LName from userId
                db.collection("Users").whereEqualTo("Uid", model.getUserId()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            viewHolder.submittedBy.setText(String.format("%s %s", document.getString("FName"), document.getString("LName")));
                        }
                    }
                });

                viewHolder.acceptButton.setOnClickListener(view -> {
                    //update isApproved to 1 and updatedBy to current userId in CustomLocation having locationId equal to model.getLocationId
                    db.collection("Locations").document(model.getLocationId()).update("isApproved", "1", "updatedBy", current_user_id);
                });

                viewHolder.declineButton.setOnClickListener(view -> {
                    //update isDeclined to 1 and updatedBy to current userId in CustomLocation having locationId equal to model.getLocationId
                    db.collection("Locations").document(model.getLocationId()).update("isDeclined", "1", "updatedBy", current_user_id);
                });

                viewHolder.cardadminLocation.setOnClickListener(view -> {
                    //Show dialog for info
                    showLocationInfo(model.getLocationTitle(), model.getLocationDescription(), model.getLocationCategory());
                });
                viewHolder.animationLocation.setOnClickListener(view -> {
                    //Show dialog for info
                    showLocationInfo(model.getLocationTitle(), model.getLocationDescription(), model.getLocationCategory());
                });

            }
        };
        adapter.startListening();
        recycler_pending_location_admin.setAdapter(adapter);
    }

    public static class FiltersViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView submittedBy,locationName;
        ImageButton declineButton,acceptButton;
        RelativeLayout cardadminLocation;
        LottieAnimationView animationLocation;

        FiltersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            submittedBy = mView.findViewById(R.id.submittedBy);
            locationName = mView.findViewById(R.id.locationName);
            declineButton = mView.findViewById(R.id.declineButton);
            acceptButton = mView.findViewById(R.id.acceptButton);
            cardadminLocation = mView.findViewById(R.id.cardadminLocation);
            animationLocation = mView.findViewById(R.id.animationLocation);
            
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getData();
    }

    public void showLocationInfo(String locationTitle,String locationDescription,String locationCategory)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(locationTitle);
        builder.setMessage(locationDescription);
        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }
}