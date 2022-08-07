package com.example.geopedia.usermenu;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.geopedia.usermenu.options.AboutUs;
import com.example.geopedia.usermenu.options.PrivacyPolicy;
import com.example.geopedia.R;
import com.example.geopedia.usermenu.options.TermsAndConditions;
import com.example.geopedia.accounts.UpdateProfile;

public class Usettings extends Fragment {

    View view;
    TextView profileBtn,additionalBtn,tncBtn,privacyBtn,aboutUsBtn;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_usettings, container, false);

        profileBtn = view.findViewById(R.id.profileBtn);
        additionalBtn = view.findViewById(R.id.additionalBtn);
        tncBtn = view.findViewById(R.id.tncBtn);
        privacyBtn = view.findViewById(R.id.privacyBtn);
        aboutUsBtn = view.findViewById(R.id.aboutUsBtn);

        profileBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), UpdateProfile.class);
            startActivity(intent);
        });

        additionalBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), TermsAndConditions.class);
            startActivity(intent);
        });

        tncBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), TermsAndConditions.class);
            startActivity(intent);
        });

        privacyBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), PrivacyPolicy.class);
            startActivity(intent);
        });

        aboutUsBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), AboutUs.class);
            startActivity(intent);
        });

        return view;
    }
}
