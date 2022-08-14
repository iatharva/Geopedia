package com.example.geopedia.adminmenu.approvedl;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.geopedia.R;
import com.example.geopedia.accounts.ForgetPassword;
import com.example.geopedia.accounts.LogIn;
import com.example.geopedia.adminmenu.declinedl.SlideshowFragment;
import com.example.geopedia.databinding.FragmentGalleryBinding;
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

public class GalleryFragment extends Fragment {

    private RecyclerView recyler_locations_approved;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    private FragmentGalleryBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        recyler_locations_approved = root.findViewById(R.id.recyler_locations_approved);
        SwipeRefreshLayout pullToRefreshApprovedLocation = root.findViewById(R.id.pullToRefreshApprovedLocation);
        firebaseFirestore = FirebaseFirestore.getInstance();
        recyler_locations_approved.setHasFixedSize(true);
        recyler_locations_approved.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyler_locations_approved.setAdapter(adapter);
        showapprovedlocations("All");
        pullToRefreshApprovedLocation.setOnRefreshListener(() -> {
            showapprovedlocations("All");
            pullToRefreshApprovedLocation.setRefreshing(false);
        });
        //Show option to go to ALl locations
        Toast.makeText(getActivity(),"You can view all at Locations option",Toast.LENGTH_SHORT).show();
        return root;
    }

    private void showapprovedlocations(String filter)
    {
        Query query;
        //show all questions where isDeleted (number) is 0
        query = firebaseFirestore.collection("Locations")
                .whereEqualTo("isApproved","1")
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
                        .inflate(R.layout.row_approved_admin, parent, false);
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
                            viewHolder.updatedBy.setText(String.format("%s %s", document.getString("FName"), document.getString("LName")));
                        }
                    }
                });
            }
        };
        adapter.startListening();
        recyler_locations_approved.setAdapter(adapter);
    }

    public static class FiltersViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView locationName,locationCategory,updatedBy;

        FiltersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            locationName = mView.findViewById(R.id.locationName);
            locationCategory = mView.findViewById(R.id.locationCategory);
            updatedBy = mView.findViewById(R.id.updatedBy);

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}