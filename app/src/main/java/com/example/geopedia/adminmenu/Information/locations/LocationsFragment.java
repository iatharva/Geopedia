package com.example.geopedia.adminmenu.Information.locations;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.example.geopedia.Info.EventInfo;
import com.example.geopedia.R;
import com.example.geopedia.databinding.FragmentLocationsBinding;
import com.example.geopedia.extras.CustomLocation;
import com.example.geopedia.showLocations;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LocationsFragment extends Fragment {

    private RecyclerView recyler_locations_admin;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    private FragmentLocationsBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLocationsBinding.inflate(inflater, container, false);

        View root = binding.getRoot();
        recyler_locations_admin = root.findViewById(R.id.recyler_locations_admin);
        SwipeRefreshLayout pullToRefreshLocationAdmin = root.findViewById(R.id.pullToRefreshLocationAdmin);
        recyler_locations_admin.setHasFixedSize(true);
        recyler_locations_admin.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyler_locations_admin.setAdapter(adapter);
        showalllocations("All");

        pullToRefreshLocationAdmin.setOnRefreshListener(() -> {
            showalllocations("All");
            pullToRefreshLocationAdmin.setRefreshing(false);
        });
        
        return root;
    }

    private void showalllocations(String filter)
    {
        Query query;
        //show all questions where isDeleted (number) is 0
        query = db.collection("Locations");

        FirestoreRecyclerOptions<CustomLocation> options = new FirestoreRecyclerOptions.Builder<CustomLocation>()
                .setQuery(query, CustomLocation.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<CustomLocation, FiltersViewHolder>(options) {
            @NotNull
            @Override
            public FiltersViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_locations_admin, parent, false);
                return new FiltersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NotNull FiltersViewHolder viewHolder, int position, @NotNull CustomLocation model) {

                viewHolder.locationName.setText(model.getLocationTitle());
                viewHolder.locationCategory.setText(model.getLocationCategory());

                //get FName and LName from userId
                db.collection("Users").whereEqualTo("Uid", model.getUpdatedBy()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            viewHolder.submittedBy.setText(String.format("%s %s", document.getString("FName"), document.getString("LName")));
                        }
                    }
                });

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

                //get the popup menu
                viewHolder.locationPopupMenu.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(requireActivity(), viewHolder.locationPopupMenu);
                    popup.getMenuInflater().inflate(R.menu.event_admin_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(item -> {
                        if(item.getTitle().equals("Delete"))
                        {
                            showDialogToDelete(model.getLocationId(),model.getIsDeleted());
                        }
                        else if(item.getTitle().equals("View details"))
                        {
                            showDetails(model.getLocationId(),model.getLocationLatitude(), model.getLocationLongitude());
                        }

                        return true;
                    });
                    popup.show();
                });
            }
        };
        adapter.startListening();
        recyler_locations_admin.setAdapter(adapter);
    }

    public static class FiltersViewHolder extends RecyclerView.ViewHolder {
        View mView;
        LottieAnimationView lottieAnimationLocation;
        TextView locationName,locationCategory,locationStatus,submittedBy;
        ImageButton locationPopupMenu;

        FiltersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            locationName = mView.findViewById(R.id.locationName);
            locationCategory = mView.findViewById(R.id.locationCategory);
            locationStatus = mView.findViewById(R.id.locationStatus);
            submittedBy = mView.findViewById(R.id.submittedBy);
            lottieAnimationLocation = mView.findViewById(R.id.lottieAnimationLocation);
            locationPopupMenu = mView.findViewById(R.id.locationPopupMenu);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showDialogToDelete(String locationId, String isDeleted) {
        //show dialog if isDeleted = 0
        if(isDeleted.equals("0"))
        {
            //Show dialog box with textfield of adding comments and yes and no option.
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle("Delete Event");
            builder.setMessage("Are you sure you want to delete this event?");
            //Add textfield for why you want to delete the question
            final EditText input = new EditText(requireActivity());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setHint("Enter reason");
            builder.setView(input);
            builder.setPositiveButton("Yes", (dialog, which) -> {
                markEventAsDeleted(locationId,input.getText().toString());
            });
            builder.setNegativeButton("No", (dialog, which) -> {
                dialog.cancel();
            });
            builder.show();
        }
        else
        {
            Toast.makeText(requireActivity(), "Event already deleted", Toast.LENGTH_SHORT).show();
        }
    }

    //Function required for action show details
    private void showDetails(String locationId, Double latitude,Double longitude)
    {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        Intent intent = new Intent(requireActivity(), EventInfo.class);
        intent.putExtra("location_id", locationId);
        double dis = calculateDistance(currentLatitude,currentLongitude,latitude,longitude);
        //convert double value into 0.00 format
        String distance = String.format("%.2f", dis);
        intent.putExtra("distance", distance);
        //Convert double to string
        intent.putExtra("latitude", String.valueOf(latitude));
        intent.putExtra("longitude", String.valueOf(longitude));
        startActivity(intent);
    }

    //Function which actually mark question as deleted
    private void markEventAsDeleted(String locationId, String reason)
    {
        if (TextUtils.isEmpty(reason)) {
            Toast.makeText(requireActivity(), "Please enter reason to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("Locations").document(locationId).update("isDeleted",1);
        db.collection("Locations").document(locationId).update("deletedReason",reason);
        Toast.makeText(requireActivity(), "Location deleted", Toast.LENGTH_SHORT).show();
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