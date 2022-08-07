package com.example.geopedia.usermenu.options;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.example.geopedia.R;

public class PrivacyPolicy extends AppCompatActivity {

    TextView p1,p2,p3,p4,para18;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        p1 = findViewById(R.id.p1);
        p2 = findViewById(R.id.p2);
        p3 = findViewById(R.id.p3);
        p4 = findViewById(R.id.p4);
        para18 = findViewById(R.id.para18);

        p1.setMovementMethod(LinkMovementMethod.getInstance());
        p2.setMovementMethod(LinkMovementMethod.getInstance());
        p3.setMovementMethod(LinkMovementMethod.getInstance());
        p4.setMovementMethod(LinkMovementMethod.getInstance());
        para18.setMovementMethod(LinkMovementMethod.getInstance());
    }
}