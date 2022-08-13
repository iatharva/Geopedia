package com.example.geopedia;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
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

import com.example.geopedia.extras.Comment;
import com.example.geopedia.extras.Question;
import com.example.geopedia.usermenu.Uquestions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
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
    RecyclerView recycler_comments_user;
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    String questionId;
    private FirestoreRecyclerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_feed);
        comment_field=findViewById(R.id.comment_field);
        comment_btn=findViewById(R.id.comment_btn);
        Objects.requireNonNull(getSupportActionBar()).hide();
        recycler_comments_user = findViewById(R.id.recycler_comments_user);
        Intent intent=getIntent();
        questionId=intent.getStringExtra("questionid");
        //get latitude and longitude
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        recycler_comments_user.setHasFixedSize(true);
        recycler_comments_user.setLayoutManager(new LinearLayoutManager(CommentFeed.this));
        recycler_comments_user.setAdapter(adapter);
        showComments(questionId);
        comment_btn.setOnClickListener(v -> {
            String com=comment_field.getText().toString();
            if(TextUtils.isEmpty(com)){
                Toast.makeText(getApplicationContext(),"Cannot post empty comment >_<",Toast.LENGTH_SHORT).show();
                return;
            }else {
                DocumentReference typeref = db.collection("Users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                typeref.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String Email=documentSnapshot.getString("Email");
                        String FName=documentSnapshot.getString("FName");
                        String LName =documentSnapshot.getString("LName");
                        //Make database call to Comments and add comment
                        addComment(questionId,currentLatitude,currentLongitude,FName,LName,com);
                    }
                });
            }
            showComments(questionId);
        });
    }

    //Add the comment in questions
    private void addComment(String questionId,Double latitude,Double longitude,String fName, String lName, String comment)
    {
        //Generate a random 28 character string
        String randomString = "";
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 28; i++) {
            randomString += characters.charAt((int) Math.floor(Math.random() * characters.length()));
        }

        //get current date and time
        java.util.Date date = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(date);
        java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("hh:mm a");
        String currentTime = sdf2.format(date);

        //Create the required object for comment
        Map<String, Object> comments = new HashMap<>();
        comments.put("userId", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        comments.put("latitude", latitude);
        comments.put("longitude", longitude);
        comments.put("questionId", questionId);
        comments.put("commentId", randomString);
        comments.put("fname", fName);
        comments.put("lname", lName);
        comments.put("date", currentDate);
        comments.put("time", currentTime);
        comments.put("isDeleted", "0");
        comments.put("comment", comment);

        db.collection("Comments").document(randomString).set(comments).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                Toast.makeText(CommentFeed.this, "Comment added", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showComments(String questionId) {
        Query query = db.collection("Comments").whereEqualTo("questionId",questionId);

        FirestoreRecyclerOptions<Comment> options = new FirestoreRecyclerOptions.Builder<Comment>()
                .setQuery(query, Comment.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Comment, FiltersViewHolder>(options) {
            @NotNull
            @Override
            public FiltersViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_comment_user, parent, false);

                return new FiltersViewHolder(view);
            }
            @Override
            protected void onBindViewHolder(@NotNull FiltersViewHolder viewHolder, int position, @NotNull Comment model) {

                viewHolder.com_user.setText(model.getFname()+" "+model.getLname());
                viewHolder.com_here.setText(model.getComment());
                viewHolder.com_time.setText(model.getDate()+" "+model.getTime());
            }
        };
        adapter.startListening();
        recycler_comments_user.setAdapter(adapter);
    }

    public static class FiltersViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView com_user,com_here,com_time;
        FiltersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            com_user = mView.findViewById(R.id.com_user);
            com_here = mView.findViewById(R.id.com_here);
            com_time = mView.findViewById(R.id.com_time);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        showComments(questionId);
    }

}