package com.example.geopedia.usermenu.options;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.geopedia.R;

public class AboutUs extends AppCompatActivity {

    public ImageView socialBtn,socialBtn1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        socialBtn = findViewById(R.id.socialBtn);
        socialBtn1 = findViewById(R.id.socialBtn1);

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
    }
}