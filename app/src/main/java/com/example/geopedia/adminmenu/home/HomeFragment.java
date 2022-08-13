package com.example.geopedia.adminmenu.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.geopedia.HomeUser;
import com.example.geopedia.R;
import com.example.geopedia.databinding.FragmentHomeBinding;
import com.example.geopedia.extras.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TextView totalUsers,inTheArea;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        inTheArea = root.findViewById(R.id.inTheArea);
        totalUsers = root.findViewById(R.id.totalUsers);

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
}