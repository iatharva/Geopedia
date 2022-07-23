package com.example.geopedia;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geopedia.extras.Question;
import com.example.geopedia.usermenu.Uquestions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;

public class CommentFeed extends AppCompatActivity {

    EditText comment_field;
    ImageButton comment_btn;
    RecyclerView comment_recycler_view;
    ListView comment_listview;
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_feed);
        firebaseFirestore = FirebaseFirestore.getInstance();
        comment_field=findViewById(R.id.comment_field);
        comment_btn=findViewById(R.id.comment_btn);
        comment_listview = findViewById(R.id.comment_listview);
        //comment_recycler_view=findViewById(R.id.comment_recycler_view);
        Intent intent=getIntent();
        String questionId=intent.getStringExtra("questionid");
        showComments(questionId);
        comment_btn.setOnClickListener(v -> {
            String com=comment_field.getText().toString();
            if(TextUtils.isEmpty(com)){
                Toast.makeText(getApplicationContext(),"Cannot post empty comment >_<",Toast.LENGTH_SHORT).show();
                return;
            }else {
                addComment(questionId);
            }
            showComments(questionId);
        });
    }

    //Add the comment in questions
    private void addComment(String questionId){
        Map<String, Object> comments = new HashMap<>();
        comments.put(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), comment_field.getText().toString());
        db.collection("Comments").document(questionId).set(comments).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                Toast.makeText(CommentFeed.this, "Comment added", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showComments(String questionId) {
        db.collection("Comments").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> userIdList = new ArrayList<>();
                List<String> commentList = new ArrayList<>();
                //get the document from task.getResult()
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    if(document.getId().equals(questionId)){
                        //store the key values in a list
                        Map<String, Object> map = document.getData();
                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                            userIdList.add(entry.getKey());
                            commentList.add(entry.getValue().toString());
                        }
                        //display comments
                        displayComments(userIdList,commentList);
                    }
                }
            } else {
                Timber.tag(TAG).d(task.getException(), "Error getting documents: ");
            }
        });
    }

    public void displayComments(List<String> userIdList, List<String> commentList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                commentList);
        comment_listview.setAdapter(adapter);
        
    }
}