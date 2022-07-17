package com.example.geopedia.adminmenu.declinedl;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.geopedia.adminmenu.approvedl.GalleryFragment;
import com.example.geopedia.databinding.FragmentSlideshowBinding;
import com.google.android.material.snackbar.Snackbar;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        //Show option to go to ALl locations
        Toast.makeText(getActivity(),"You can view all at Locations option",Toast.LENGTH_SHORT).show();
        /*
        Snackbar snackbar = Snackbar
                .make(root,"View all locations",Snackbar.LENGTH_LONG)
                .setAction(
                        "SHOW",
                        view -> {
                            Intent i = new Intent(getActivity(), GalleryFragment.class);
                            startActivity(i);
                        });
        snackbar.show();
         */
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}