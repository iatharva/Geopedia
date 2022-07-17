package com.example.geopedia.adminmenu.Others.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.geopedia.R;
import com.example.geopedia.databinding.FragmentAboutBinding;

public class AboutFragment extends Fragment {

    private FragmentAboutBinding binding;
    public ImageView socialBtn,socialBtn1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        socialBtn = root.findViewById(R.id.socialBtn);
        socialBtn1 = root.findViewById(R.id.socialBtn1);

        socialBtn.setOnClickListener(view -> {
            //Open Github page in browser
            String url = "https://github.com/iatharva/Geopedia";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        socialBtn1.setOnClickListener(view -> {
            //Open Github release page in browser
            String url = "https://github.com/iatharva/Geopedia/releases/tag/beta";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}