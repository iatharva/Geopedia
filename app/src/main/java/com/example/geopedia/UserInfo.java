package com.example.geopedia;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Objects;

public class UserInfo extends AppCompatActivity {

    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView userFullName,userEmail,userDob,userType,userJoinedOn,userAccountType;
    String Email,FName,LName,Dob,IsAdmin,IsPaid,Uid;
    String userid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        userFullName = findViewById(R.id.userFullName);
        userEmail = findViewById(R.id.userEmail);
        userDob = findViewById(R.id.userDob);
        userType = findViewById(R.id.userType);
        userJoinedOn = findViewById(R.id.userJoinedOn);
        userAccountType = findViewById(R.id.userAccountType);
        userid = getIntent().getStringExtra("userid");
        getUserData();
    }

    public void getUserData()
    {
        DocumentReference typeref = db.collection("Users").document(userid);
        Uid=userid;
        typeref.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Email=documentSnapshot.getString("Email");
                FName=documentSnapshot.getString("FName");
                LName =documentSnapshot.getString("LName");
                Dob=documentSnapshot.getString("Dob");
                IsAdmin =documentSnapshot.getString("IsAdmin");
                IsPaid = documentSnapshot.getString("IsPaid");

                userFullName.setText(String.format("%s %s", FName, LName));
                userEmail.setText(Email);
                userDob.setText(Dob);;
                if(IsAdmin.equals("1"))
                {
                    userType.setText("User and Admin");
                }
                else if (IsAdmin.equals("0"))
                {
                    userType.setText("User only");
                }

                if(IsPaid.equals("1"))
                {
                    userAccountType.setText("Premium");
                }
                else if (IsPaid.equals("0"))
                {
                    userAccountType.setText("FREE");
                }

                userJoinedOn.setText("Unknown");
            }
        });

        //Get user's CreationDate from firebase auth using uid
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //TODO: Add account creation in user data while creating account. and then update it here
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getUserData();
    }
}