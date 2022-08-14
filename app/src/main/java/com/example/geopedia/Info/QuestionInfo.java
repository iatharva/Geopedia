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

public class QuestionInfo extends AppCompatActivity {

    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView quesTitle,quesDescription,quesPostedOn,quesPostedBy,quesComments,quesUpvotes;
    String title,description,postedOn,postedBy,comments,upvotes;
    String questionId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_info);

        quesTitle = findViewById(R.id.quesTitle);
        quesDescription = findViewById(R.id.quesDescription);
        quesPostedOn = findViewById(R.id.quesPostedOn);
        quesPostedBy = findViewById(R.id.quesPostedBy);
        quesComments = findViewById(R.id.quesComments);
        quesUpvotes = findViewById(R.id.quesUpvotes);

        questionId = getIntent().getStringExtra("questionId");
        getQuestionData();
    }

    public void getQuestionData()
    {
        //get the question data
        DocumentReference typeref = db.collection("Questions").document(questionId);
        typeref.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                title=documentSnapshot.getString("questionTitle");
                description=documentSnapshot.getString("questionDesc");
                postedOn=documentSnapshot.getString("date")+" "+documentSnapshot.getString("time");
                postedBy=documentSnapshot.getString("fname")+" "+documentSnapshot.getString("lname");

                quesTitle.setText(title);
                quesDescription.setText(description);
                quesPostedBy.setText(postedBy);
                quesPostedOn.setText(postedOn);
            }
        });

        //get the count of upvotes
        //set the upvote count
        db.collection("Upvotes").whereEqualTo("questionId",questionId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if(!queryDocumentSnapshots.isEmpty())
            {
                int count=0;
                for(QueryDocumentSnapshot document: queryDocumentSnapshots)
                {
                    //increase count if isUpvoted = "1"
                    if(document.getString("isUpvoted").equals("1"))
                        count++;
                }
                quesUpvotes.setText(String.valueOf(count) + " upvotes");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(QuestionInfo.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        });

        //get the count of comments
        db.collection("Comments").whereEqualTo("questionId",questionId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if(!queryDocumentSnapshots.isEmpty())
            {
                int count=0;
                for(QueryDocumentSnapshot document: queryDocumentSnapshots)
                {
                    count++;
                }
                quesComments.setText(String.valueOf(count) + " comments");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(QuestionInfo.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public void onResume()
    {
        super.onResume();
        getQuestionData();
    }
}