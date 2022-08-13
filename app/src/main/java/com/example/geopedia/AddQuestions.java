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
        question.put("userId", userId);
        question.put("latitude", currentLatitude);
        question.put("longitude", currentLongitude);
        question.put("questionId", randomString);
        question.put("questionTitle", questionTitle);
        question.put("questionDesc", questionDesc);
        question.put("email", Email);
        question.put("fname", FName);
        question.put("lname", LName);
        question.put("date", currentDate);
        question.put("time", currentTime);
        
        question.put("isDeleted",0);

        //Add question to database
        String finalRandomString = randomString;
        db.collection("Questions").document(randomString).set(question).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                //Go back to home screen
                initiateRequiredStructure(finalRandomString,userId,currentDate,currentTime);
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

    /*
     * This function is used to initiate the required structure for the user to be able to answer/upvote the question
     */
    private void initiateRequiredStructure(String questionId,String userId,String date,String time) {
        
        //Initiate Upvote structure
        Map<String, Object> upvote = new HashMap<>();
        upvote.put(userId, 1);

        //Get randomstring for comment
        //Generate a random 28 character string
        String randomString = "";
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 28; i++) {
            randomString += characters.charAt((int) Math.floor(Math.random() * characters.length()));
        }

        String finalRandomString = randomString;
        db.collection("Upvotes").document(questionId).set(upvote).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {

                //Create the required object for comment
                Map<String, Object> comments = new HashMap<>();
                comments.put("userId", "aWuD6jzhCRgTwNzOcOBDywZBvR42");
                comments.put("latitude", 18.512291251681745);
                comments.put("longitude", 73.782062754035);
                comments.put("questionId", questionId);
                comments.put("commentId", finalRandomString);
                comments.put("fname", "Atharva");
                comments.put("lname", "(Dev)");
                comments.put("date", date);
                comments.put("time", time);
                comments.put("isDeleted", "0");
                comments.put("comment", "Hello I am the creator of the App, Just wanted to give heads up to keep comments meaningful and respectful. Thanks");

                db.collection("Comments").document(finalRandomString).set(comments).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(AddQuestions.this, "Question submitted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        //Exception Case
                        Toast.makeText(AddQuestions.this, "Error! " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else 
            {
                //Exception Case
                Toast.makeText(AddQuestions.this, "Error! " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}