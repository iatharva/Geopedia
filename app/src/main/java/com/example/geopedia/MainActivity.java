package com.example.geopedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();
        //Checked signed
        fAuth = FirebaseAuth.getInstance();
        mAuthStateListener = firebaseAuth -> {
            FirebaseUser mFirebaseUser = firebaseAuth.getCurrentUser();
            if (mFirebaseUser != null) {

                //User of customer start
                userid = mFirebaseUser.getUid();
                DocumentReference typeref = db.collection("Users").document(userid);
                typeref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String type = documentSnapshot.getString("isAdmin");
                            assert type != null;
                            if (type.equals("1")) {
                                Intent intent = new Intent(MainActivity.this, HomeAdmin.class);
                                startActivity(intent);
                                finish();
                            } else if (type.equals("0")) {
                                Intent intent = new Intent(MainActivity.this, HomeAdmin.class);
                                intent.putExtra("user_id" ,userid);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                });
                //END
            } else {
                Intent i = new Intent(MainActivity.this, LogIn.class);
                startActivity(i);
                finish();
            }
        };
    }
    @Override
    public void onStart() {
        super.onStart();
        int SPLASH_TIME_OUT = 1700;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                logincheck();
            }
        }, SPLASH_TIME_OUT);
    }

    public void logincheck(){
        fAuth.addAuthStateListener(mAuthStateListener);
    }

}