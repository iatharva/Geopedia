package com.example.geopedia.usermenu;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.geopedia.AddQuestions;
import com.example.geopedia.CommentFeed;
import com.example.geopedia.R;
import com.example.geopedia.extras.Question;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Uquestions extends Fragment {
    private RecyclerView recycler_questions_user;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_uquestions, container, false);
        FloatingActionButton addQuestionFab = view.findViewById(R.id.addQuestionFab);
        final SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefreshQuestions);
        recycler_questions_user = view.findViewById(R.id.recycler_questions_user);
        firebaseFirestore = FirebaseFirestore.getInstance();

        recycler_questions_user.setHasFixedSize(true);
        recycler_questions_user.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_questions_user.setAdapter(adapter);
        showquestions("All",current_user_id);

        pullToRefresh.setOnRefreshListener(() -> {
            showquestions("All",current_user_id);
            pullToRefresh.setRefreshing(false);
        });

        addQuestionFab.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), AddQuestions.class);
            startActivity(intent);
        });
        return view;
    }

    private void showquestions(String filter,String userid)
    {
        Query query;
        //show all questions where isDeleted (number) is 0
        query = firebaseFirestore.collection("Questions").whereEqualTo("isDeleted",0);

        FirestoreRecyclerOptions<Question> options = new FirestoreRecyclerOptions.Builder<Question>()
                .setQuery(query, Question.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Question, FiltersViewHolder>(options) {
            @NotNull
            @Override
            public FiltersViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_question_user, parent, false);

                return new FiltersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NotNull FiltersViewHolder viewHolder, int position, @NotNull Question model) {
                String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
                //get current date and time
                java.util.Date date = new java.util.Date();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                String currentDate = sdf.format(date);
                java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("hh:mm a");
                String currentTime = sdf2.format(date);
                AtomicBoolean isLiked = new AtomicBoolean(false);
                viewHolder.tv_name.setText(model.getFname()+" "+model.getLname());
                viewHolder.tv_status.setText(model.getQuestionTitle());
                viewHolder.tv_description.setText(model.getQuestionDesc());
                viewHolder.tv_time.setText(model.getDate()+" "+model.getTime());
                if(model.getQuestionDesc().isEmpty())
                    viewHolder.tv_description.setText("No Description");

                //get the count of comments from the collection Comments where questionId is equal to the questionId
                db.collection("Comments").whereEqualTo("questionId",model.getQuestionId()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if(!queryDocumentSnapshots.isEmpty())
                    {
                        int count=0;
                        for(QueryDocumentSnapshot document: queryDocumentSnapshots)
                        {
                            count++;
                        }
                        viewHolder.tv_comment.setText(String.valueOf(count) + " comments");
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                });

                //Open the comment feed
                viewHolder.commentfeedlayout.setOnClickListener(view -> {
                    Intent intent = new Intent(getActivity(), CommentFeed.class);
                    intent.putExtra("questionid",model.getQuestionId());
                    startActivity(intent);
                });

                viewHolder.likee.setBackgroundResource(R.drawable.upvot_black_50);
                isLiked.set(false);

                //set the upvote count
                db.collection("Upvotes").whereEqualTo("questionId",model.getQuestionId()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if(!queryDocumentSnapshots.isEmpty())
                    {
                        int count=0;
                        for(QueryDocumentSnapshot document: queryDocumentSnapshots)
                        {
                            //increase count if isUpvoted = "1"
                            if(document.getString("isUpvoted").equals("1"))
                                count++;
                            //check if the current user has upvoted the question
                            if(document.getString("userId").equals(current_user_id) && document.getString("isUpvoted").equals("1"))
                            {
                                viewHolder.likee.setBackgroundResource(R.drawable.upvote_color_50);
                                isLiked.set(true);
                            }
                        }
                        viewHolder.tv_like.setText(String.valueOf(count) + " upvotes");
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                });

                viewHolder.likelayout.setOnClickListener(view -> {
                    adapter.notifyItemChanged(position);
                    //get the Upvotes collection where questionId is equal to the questionId and userId is equal to the current userId
                    db.collection("Upvotes").whereEqualTo("questionId",model.getQuestionId()).whereEqualTo("userId",current_user_id).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        if(!queryDocumentSnapshots.isEmpty())
                        {
                            for(QueryDocumentSnapshot document: queryDocumentSnapshots)
                            {
                                //if the user has upvoted the question, then update the isUpvoted field to 0
                                if(document.getString("isUpvoted").equals("1"))
                                {
                                    db.collection("Upvotes").document(document.getId()).update("isUpvoted","0");
                                    viewHolder.likee.setBackgroundResource(R.drawable.upvot_black_50);
                                    isLiked.set(false);
                                    Toast.makeText(getActivity(), "Downvoted", Toast.LENGTH_SHORT).show();
                                }else{
                                    //if the user has not upvoted the question, then update the isUpvoted field to 1
                                    db.collection("Upvotes").document(document.getId()).update("isUpvoted","1");
                                    viewHolder.likee.setBackgroundResource(R.drawable.upvote_color_50);
                                    isLiked.set(true);
                                    Toast.makeText(getActivity(), "Upvoted", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else{
                            //if the user has not upvoted the question, then add a new document to the Upvotes collection
                            Map<String, Object> Upvotes = new HashMap<>();
                            String randomString1 = "";
                            for (int i = 0; i < 28; i++) {
                                randomString1 += characters.charAt((int) Math.floor(Math.random() * characters.length()));
                            }
                            Upvotes.put("upvoteId", randomString1);
                            Upvotes.put("questionId", model.getQuestionId());
                            Upvotes.put("userId", current_user_id);
                            Upvotes.put("date", currentDate);
                            Upvotes.put("isUpvoted", "1");
                            Upvotes.put("time", currentTime);
                            db.collection("Upvotes").add(Upvotes).addOnSuccessListener(documentReference -> {
                                viewHolder.likee.setBackgroundResource(R.drawable.upvote_color_50);
                                isLiked.set(true);
                                Toast.makeText(getActivity(), "Upvoted", Toast.LENGTH_SHORT).show();
                            }).addOnFailureListener(e -> {
                                Toast.makeText(getActivity(), "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

                    //Refresh after due to the change in the upvote count
                    adapter.notifyItemChanged(position);
                });
            }
        };
        adapter.startListening();
        recycler_questions_user.setAdapter(adapter);
    }

    public static class FiltersViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView tv_name,tv_status,tv_description,tv_like,tv_comment,tv_time;
        RelativeLayout likelayout,commentfeedlayout,sharelayout;
        ImageView imgView_propic,likee;

        FiltersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            tv_name = mView.findViewById(R.id.tv_name);
            tv_status = mView.findViewById(R.id.tv_status);
            tv_description = mView.findViewById(R.id.tv_description);
            tv_like = mView.findViewById(R.id.tv_like);
            tv_comment = mView.findViewById(R.id.tv_comment);
            likelayout = mView.findViewById(R.id.likelayout);
            commentfeedlayout = mView.findViewById(R.id.commentfeedlayout);
            sharelayout = mView.findViewById(R.id.sharelayout);
            imgView_propic = mView.findViewById(R.id.imgView_propic);
            likee = mView.findViewById(R.id.likee);
            tv_time = mView.findViewById(R.id.tv_time);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        showquestions("All",current_user_id);
    }
}