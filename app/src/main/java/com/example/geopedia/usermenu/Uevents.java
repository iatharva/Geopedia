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

import com.example.geopedia.AddEvents;
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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Uevents extends Fragment {

    private RecyclerView recycler_events_user;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;
    private TextView empty_message;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_uevents, container, false);
        FloatingActionButton addEventFab = view.findViewById(R.id.addEventFab);

        final SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefreshQuestions);
        //recycler_events_user = view.findViewById(R.id.recycler_questions_user);
        empty_message = view.findViewById(R.id.empty_message);
        empty_message.setVisibility(View.VISIBLE);
        firebaseFirestore = FirebaseFirestore.getInstance();

        //recycler_events_user.setHasFixedSize(true);
        //recycler_events_user.setLayoutManager(new LinearLayoutManager(getActivity()));
        //recycler_events_user.setAdapter(adapter);
        //showEvents("All",current_user_id);

        addEventFab.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), AddEvents.class);
            startActivity(intent);
        });
        return view;
    }
    /*
    private void showEvents(String filter,String userid)
    {
        Query query = null;
        if(filter.equals("All"))
        {
            query = firebaseFirestore.collection("Events");
        }

        FirestoreRecyclerOptions<Question> options = new FirestoreRecyclerOptions.Builder<Question>()
                .setQuery(query, Question.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Question, Uquestions.FiltersViewHolder>(options) {
            @NotNull
            @Override
            public Uquestions.FiltersViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_question_user, parent, false);

                return new Uquestions.FiltersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NotNull Uquestions.FiltersViewHolder viewHolder, int position, @NotNull Question model) {

                viewHolder.tv_name.setText(model.getFname()+" "+model.getLname());
                viewHolder.tv_status.setText(model.getQuestionTitle());
                viewHolder.tv_description.setText(model.getQuestionDesc());
                viewHolder.tv_time.setText(model.getDate()+" "+model.getTime());
                if(model.getQuestionDesc().isEmpty())
                    viewHolder.tv_description.setText("No Description");
                AtomicBoolean isLiked= new AtomicBoolean(false);

                //get the count of upvotes
                db.collection("Upvotes").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if(document.getId().equals(model.getQuestionId()))
                            {
                                int count=0;
                                for(String key: document.getData().keySet())
                                {
                                    count++;
                                }
                                viewHolder.tv_like.setText(String.valueOf(count));
                            }
                        }
                    }
                });

                //get the count of comments
                db.collection("Comments").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if(document.getId().equals(model.getQuestionId()))
                            {
                                int count=0;
                                for(String key: document.getData().keySet())
                                {
                                    count++;
                                }
                                viewHolder.tv_comment.setText(String.valueOf(count));
                            }
                        }
                    }
                });


                firebaseFirestore.collection("Upvotes").orderBy(model.getQuestionId()).whereEqualTo("userid",current_user_id).get().addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {
                        for(int i = 0;i<task.getResult().size();i++)
                        {
                            if(task.getResult().getDocuments().get(i).get("value").equals(1))
                            {
                                isLiked.set(true);
                                if(isLiked.get())
                                    viewHolder.likee.setBackgroundResource(R.drawable.upvote_color_50);
                                else
                                    viewHolder.likee.setBackgroundResource(R.drawable.upvot_black_50);
                            }
                        }
                    }
                });

                viewHolder.likelayout.setOnClickListener(v -> {
                    if(isLiked.get())
                    {
                        isLiked.set(false);
                        viewHolder.likee.setBackgroundResource(R.drawable.upvot_black_50);
                        firebaseFirestore.collection("Upvotes").orderBy(model.getQuestionId()).whereEqualTo("userid",current_user_id).get().addOnCompleteListener(task -> {
                            if(task.isSuccessful())
                            {
                                for(int i = 0;i<task.getResult().size();i++)
                                {
                                    firebaseFirestore.collection("Upvotes").document(task.getResult().getDocuments().get(i).getId()).delete();
                                    Toast.makeText(getActivity(), "Upvote removed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else{
                        isLiked.set(true);
                        viewHolder.likee.setBackgroundResource(R.drawable.upvote_color_50);
                        //Add a field with key=current_user_id and value=1 to the document model.getQuestionId() in collection upvoteswashingtonRef
                        db.collection("Upvotes").document(model.getQuestionId())
                                .update(current_user_id, 1)
                                .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "Upvoted", Toast.LENGTH_SHORT).show());
                    }
                });

                viewHolder.commentfeedlayout.setOnClickListener(view -> {
                    //Open the comment feed
                    Intent intent = new Intent(getActivity(), CommentFeed.class);
                    intent.putExtra("questionid",model.getQuestionId());
                    startActivity(intent);
                });

                if(isLiked.get())
                    viewHolder.likee.setBackgroundResource(R.drawable.upvote_color_50);
                else
                    viewHolder.likee.setBackgroundResource(R.drawable.upvot_black_50);
            }
        };
        adapter.startListening();
        recycler_questions_user.setAdapter(adapter);
        if(adapter.getItemCount()==0)
            empty_message.setVisibility(View.VISIBLE);
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

     */
}
