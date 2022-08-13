package com.example.geopedia.Info;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

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
        db.collection("Upvotes").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if(document.getId().equals(questionId))
                    {
                        int count=0;
                        for(String key: document.getData().keySet())
                        {
                            count++;
                        }
                        quesUpvotes.setText(String.format("%s Upvotes", String.valueOf(count)));
                    }
                }
            }
        });

        //get the count of comments
        db.collection("Comments").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if(document.getId().equals(questionId))
                    {
                        int count=0;
                        for(String key: document.getData().keySet())
                        {
                            count++;
                        }
                        quesComments.setText(String.format("%s Comments", String.valueOf(count)));
                    }
                }
            }
        });

    }

    @Override
    public void onResume()
    {
        super.onResume();
        getQuestionData();
    }
}