package com.example.geopedia.adminmenu.Information.questions;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.geopedia.CommentFeed;
import com.example.geopedia.Info.QuestionInfo;
import com.example.geopedia.R;
import com.example.geopedia.adminmenu.Information.events.EventsFragment;
import com.example.geopedia.databinding.FragmentQuestionsBinding;
import com.example.geopedia.extras.Events;
import com.example.geopedia.extras.Question;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class QuestionsFragment extends Fragment {

    private RecyclerView recycler_questions_admin;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    private FragmentQuestionsBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQuestionsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final SwipeRefreshLayout pullToRefresh = root.findViewById(R.id.pullToRefreshQuestionsAdmin);
        recycler_questions_admin = root.findViewById(R.id.recycler_questions_admin);
        firebaseFirestore = FirebaseFirestore.getInstance();

        recycler_questions_admin.setHasFixedSize(true);
        recycler_questions_admin.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_questions_admin.setAdapter(adapter);
        showquestions("All");
        //End of adapter code

        pullToRefresh.setOnRefreshListener(() -> {
            showquestions("All");
            pullToRefresh.setRefreshing(false);
        });

        return root;
    }

    private void showquestions(String filter) {
        Query query;
        if(filter.equals("All"))
        {
            query = firebaseFirestore.collection("Questions");
        }else{
            //Update the query
            query = firebaseFirestore.collection("Orders").whereEqualTo("sellerid","userid")
                    .whereEqualTo("status",filter);
        }

        FirestoreRecyclerOptions<Question> options = new FirestoreRecyclerOptions.Builder<Question>()
                .setQuery(query, Question.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Question, FiltersViewHolder>(options) {
            @NotNull
            @Override
            public FiltersViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_question_admin, parent, false);

                return new FiltersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NotNull FiltersViewHolder viewHolder, int position, @NotNull Question model) {

                viewHolder.tv_status.setText(model.getQuestionTitle());
                if(model.getIsDeleted() == 0)
                {
                    viewHolder.tv_status.setTextColor(getResources().getColor(R.color.Green));
                }else{
                    viewHolder.tv_status.setTextColor(getResources().getColor(R.color.Red));
                }
                viewHolder.tv_description.setText(model.getQuestionDesc());
                viewHolder.tv_time.setText(String.format("%s %s", model.getDate(), model.getTime()));


                //get the name of user
                DocumentReference typeref = db.collection("Users").document(model.getUserId());
                typeref.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String FName=documentSnapshot.getString("FName");
                        String LName =documentSnapshot.getString("LName");
                        viewHolder.tv_name.setText(String.format("%s %s", FName, LName));
                    }
                });

                //get the count of upvotes
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
                        }
                        viewHolder.tv_like.setText(String.valueOf(count) + " upvotes");
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                });

                //get the count of comments
                db.collection("Comments").whereEqualTo("questionId",model.getQuestionId()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if(!queryDocumentSnapshots.isEmpty())
                    {
                        int count=0;
                        for(QueryDocumentSnapshot document: queryDocumentSnapshots)
                        {
                            if(document.getString("isDeleted").equals("0"))
                                count++;
                        }
                        viewHolder.tv_comment.setText(String.valueOf(count) + " comments");
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                });

                //get the popup menu
                viewHolder.questionPopupMenu.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(requireActivity(), viewHolder.questionPopupMenu);
                    popup.getMenuInflater().inflate(R.menu.question_admin_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(item -> {
                        if(item.getTitle().equals("Delete"))
                        {
                            showDialogToDeleteTheQuestion(model.getQuestionId(),model.getIsDeleted());
                        }
                        else if(item.getTitle().equals("View Comments"))
                        {
                            showComments(model.getQuestionId());
                        }
                        else if(item.getTitle().equals("View details"))
                        {
                            showDetails(model.getQuestionId());
                        }

                        return true;
                    });
                    popup.show();
                });
            }
        };
        adapter.startListening();
        recycler_questions_admin.setAdapter(adapter);
    }

    public static class FiltersViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageView imgView_propic;
        TextView tv_name,tv_time,tv_status,tv_description,tv_like,tv_comment;
        ImageButton questionPopupMenu;

        FiltersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            imgView_propic = mView.findViewById(R.id.imgView_propic);
            tv_name = mView.findViewById(R.id.tv_name);
            tv_time = mView.findViewById(R.id.tv_time);
            tv_status = mView.findViewById(R.id.tv_status);
            tv_description = mView.findViewById(R.id.tv_description);
            tv_like = mView.findViewById(R.id.tv_like);
            tv_comment = mView.findViewById(R.id.tv_comment);
            questionPopupMenu = mView.findViewById(R.id.questionPopupMenu);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        showquestions("All");
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //Function required for action Delete
    private void showDialogToDeleteTheQuestion(String questionId,int isDeleted)
    {
        //show dialog if isDeleted = 0
        if(isDeleted==0)
        {
            //Show dialog box with textfield of adding comments and yes and no option.
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle("Delete Question");
            builder.setMessage("Are you sure you want to delete this question?");
            //Add textfield for why you want to delete the question
            final EditText input = new EditText(requireActivity());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setHint("Enter reason");
            builder.setView(input);
            builder.setPositiveButton("Yes", (dialog, which) -> {
                markQuestionAsDeleted(questionId,input.getText().toString());
            });
            builder.setNegativeButton("No", (dialog, which) -> {
                dialog.cancel();
            });
            builder.show();
        }
        else
        {
            Toast.makeText(requireActivity(), "Question already deleted", Toast.LENGTH_SHORT).show();
        }
    }

    //Function required for action show comments
    private void showComments(String questionId)
    {
        Intent intent = new Intent(getActivity(), CommentFeed.class);
        intent.putExtra("questionid",questionId);
        startActivity(intent);
    }

    //Function required for action show details
    private void showDetails(String questionId)
    {
        Intent intent = new Intent(requireActivity(), QuestionInfo.class);
        intent.putExtra("questionId", questionId);
        startActivity(intent);
    }

    //Function which actually mark question as deleted
    private void markQuestionAsDeleted(String questionId, String reason)
    {
        if (TextUtils.isEmpty(reason)) {
            Toast.makeText(requireActivity(), "Please enter reason to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("Questions").document(questionId).update("isDeleted",1);
        db.collection("Questions").document(questionId).update("deletedReason",reason);
        Toast.makeText(requireActivity(), "Question deleted", Toast.LENGTH_SHORT).show();
    }
}