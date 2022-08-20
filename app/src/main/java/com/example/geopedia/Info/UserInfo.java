package com.example.geopedia.Info;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geopedia.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Objects;

public class UserInfo extends AppCompatActivity {

    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView userFullName,userEmail,userDob,userType,userJoinedOn,userAccountType,other_info;
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
        other_info = findViewById(R.id.other_info);
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
                String joined = documentSnapshot.getString("JoinedOn");

                userFullName.setText(String.format("%s %s", FName, LName));
                userEmail.setText(Email);
                userDob.setText(Dob);;
                if(IsAdmin.equals("1"))
                {
                    userType.setText("User and Administrator");
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

                userJoinedOn.setText(joined);
            }
        });

        db.collection("Comments").whereEqualTo("userId",userid).whereEqualTo("isDeleted","0").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if(!queryDocumentSnapshots.isEmpty())
            {
                int count=0;
                for(QueryDocumentSnapshot document: queryDocumentSnapshots)
                {
                    if(document.getString("commentId")!=null)
                        count++;
                }
                other_info.setText("Total contribution of\n\nTotal Comments: "+count+"");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(UserInfo.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        });

        db.collection("Questions").whereEqualTo("userId",userid).whereEqualTo("isDeleted",0).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if(!queryDocumentSnapshots.isEmpty())
            {
                int count=0;
                for(QueryDocumentSnapshot document: queryDocumentSnapshots)
                {
                    if(document.getString("questionId")!=null)
                        count++;
                }

                if(other_info.getText().toString().equals("Currently, No other information is available"))
                {
                    other_info.setText("Total contribution of\n\nTotal Questions: "+count+"");
                }
                else
                {
                    if(!other_info.getText().toString().contains("Total Questions: "))
                    {
                        other_info.setText(other_info.getText().toString()+"\n\nTotal Questions: "+count+"");
                    }

                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(UserInfo.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getUserData();
    }
}