package com.example.geopedia;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddQuestions extends AppCompatActivity {

EditText questionTitleFld,questionDescFld;
Button submitQuestionBtn;
final FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_questions);

        questionTitleFld = findViewById(R.id.questionTitleFld);
        questionDescFld = findViewById(R.id.questionDescFld);
        submitQuestionBtn = findViewById(R.id.submitQuestionBtn);

        submitQuestionBtn.setOnClickListener(v -> {
            String questionTitle = questionTitleFld.getText().toString();
            String questionDesc = questionDescFld.getText().toString();
            if(TextUtils.isEmpty(questionTitle)){
                Toast.makeText(getApplicationContext(),"Please enter the Question to submit",Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DocumentReference typeref = db.collection("Users").document(userId);
            typeref.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String Email=documentSnapshot.getString("Email");
                    String FName=documentSnapshot.getString("FName");
                    String LName =documentSnapshot.getString("LName");
    
                    //Make database call to Questions and add question
                    AddQuestion(questionTitle,questionDesc,userId,Email,FName,LName);
                }
            });
        });
    }
    private void AddQuestion(String questionTitle,String questionDesc,String userId,String Email,String FName,String LName){
        //get current date and time
        java.util.Date date = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(date);
        java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("hh:mm a");
        String currentTime = sdf2.format(date);

        //get latitude and longitude
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        //Generate a random 28 character string
        String randomString = "";
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 28; i++) {
            randomString += characters.charAt((int) Math.floor(Math.random() * characters.length()));
        }

        //Create a new question object
        Map<String, Object> question = new HashMap<>();
        question.put("questionTitle", questionTitle);
        question.put("questionDesc", questionDesc);
        question.put("userId", userId);
        question.put("Email", Email);
        question.put("FName", FName);
        question.put("LName", LName);
        question.put("date", currentDate);
        question.put("time", currentTime);
        question.put("latitude", currentLatitude);
        question.put("longitude", currentLongitude);
        question.put("questionId", randomString);
        question.put("isDeleted",0);

        //Add question to database
         db.collection("Questions").document(randomString).set(question).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Toast.makeText(AddQuestions.this, "Question added successfully", Toast.LENGTH_SHORT).show();
                //Go back to home screen
                Intent intent = new Intent(AddQuestions.this, HomeUser.class);
                startActivity(intent);
                finish();
            }
            else 
            {
                //Exception Case
                Toast.makeText(AddQuestions.this, "Error! " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });




    }
}