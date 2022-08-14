package com.example.geopedia.adminmenu.declinedl;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.geopedia.R;
import com.example.geopedia.adminmenu.approvedl.GalleryFragment;
import com.example.geopedia.adminmenu.home.HomeFragment;
import com.example.geopedia.databinding.FragmentSlideshowBinding;
import com.example.geopedia.extras.CustomLocation;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SlideshowFragment extends Fragment {

    private RecyclerView recyler_locations_declined;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    private FragmentSlideshowBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        recyler_locations_declined = root.findViewById(R.id.recyler_locations_declined);
        SwipeRefreshLayout pullToRefreshDeclinedLocation = root.findViewById(R.id.pullToRefreshDeclinedLocation);
        firebaseFirestore = FirebaseFirestore.getInstance();
        recyler_locations_declined.setHasFixedSize(true);
        recyler_locations_declined.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyler_locations_declined.setAdapter(adapter);
        showdeclinedLocations("All");

        pullToRefreshDeclinedLocation.setOnRefreshListener(() -> {
            showdeclinedLocations("All");
            pullToRefreshDeclinedLocation.setRefreshing(false);
        });

        //Show option to go to ALl locations
        Toast.makeText(getActivity(),"You can view all at Locations option",Toast.LENGTH_SHORT).show();
        return root;
    }

    private void showdeclinedLocations(String filter) {
        Query query;
        //show all questions where isDeleted (number) is 0
        query = firebaseFirestore.collection("Locations")
                .whereEqualTo("isApproved","0")
                .whereEqualTo("isDeleted","0")
                .whereEqualTo("isDeclined","1");

        FirestoreRecyclerOptions<CustomLocation> options = new FirestoreRecyclerOptions.Builder<CustomLocation>()
                .setQuery(query, CustomLocation.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<CustomLocation, FiltersViewHolder>(options) {
            @NotNull
            @Override
            public FiltersViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_declined_admin, parent, false);
                return new FiltersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NotNull FiltersViewHolder viewHolder, int position, @NotNull CustomLocation model) {

                viewHolder.locationName.setText(model.getLocationTitle());
                viewHolder.reason.setText(model.getReason());
                //get FName and LName from userId
                db.collection("Users").whereEqualTo("Uid", model.getUpdatedBy()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            viewHolder.updatedBy.setText(String.format("%s %s", document.getString("FName"), document.getString("LName")));
                        }
                    }
                });
            }
        };
        adapter.startListening();
        recyler_locations_declined.setAdapter(adapter);
    }

    public static class FiltersViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView locationName,reason,updatedBy;

        FiltersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            locationName = mView.findViewById(R.id.locationName);
            reason = mView.findViewById(R.id.reason);
            updatedBy = mView.findViewById(R.id.updatedBy);

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}