package com.example.geopedia.adminmenu.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.geopedia.R;
import com.example.geopedia.databinding.FragmentHomeBinding;
import com.google.firebase.firestore.FirebaseFirestore;

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