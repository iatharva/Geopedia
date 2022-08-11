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

import com.example.geopedia.R;
import com.example.geopedia.accounts.ForgetPassword;
import com.example.geopedia.accounts.LogIn;
import com.example.geopedia.adminmenu.declinedl.SlideshowFragment;
import com.example.geopedia.databinding.FragmentGalleryBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private LinearLayout approvedlayout;
    private Spinner locationsfilter;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        //Show option to go to ALl locations
        Toast.makeText(getActivity(),"You can view all at Locations option",Toast.LENGTH_SHORT).show();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}