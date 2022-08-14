package com.example.geopedia.adminmenu.Information.locations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.example.geopedia.R;
import com.example.geopedia.databinding.FragmentLocationsBinding;
import com.example.geopedia.extras.CustomLocation;
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
            }
        };
        adapter.startListening();
        recyler_locations_admin.setAdapter(adapter);
    }

    public static class FiltersViewHolder extends RecyclerView.ViewHolder {
        View mView;
        LottieAnimationView lottieAnimationLocation;
        TextView locationName,locationCategory,locationStatus,submittedBy;

        FiltersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            locationName = mView.findViewById(R.id.locationName);
            locationCategory = mView.findViewById(R.id.locationCategory);
            locationStatus = mView.findViewById(R.id.locationStatus);
            submittedBy = mView.findViewById(R.id.submittedBy);
            lottieAnimationLocation = mView.findViewById(R.id.lottieAnimationLocation);

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}