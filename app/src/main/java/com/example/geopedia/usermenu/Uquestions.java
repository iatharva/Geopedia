package com.example.geopedia.usermenu;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Uquestions extends Fragment {
    private RecyclerView recycler_questions_user;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;
    private EditText SearchQuestionFld;
    private ImageButton search_question_btn;
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
        search_question_btn = view.findViewById(R.id.search_question_btn);
        SearchQuestionFld = view.findViewById(R.id.SearchQuestionFld);
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

        search_question_btn.setOnClickListener(view -> {
            if(TextUtils.isEmpty(SearchQuestionFld.getText().toString()))
            {
                Toast.makeText(getActivity(),"Please enter something to search",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getActivity(), "Searching through our logs", Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(),"Searching -"+SearchQuestionFld.getText().toString(),Toast.LENGTH_SHORT).show();
                showquestions(SearchQuestionFld.getText().toString(),current_user_id);
            }
        });

        return view;
    }

    private void showquestions(String filter,String userid)
    {
        Query query;
        //show all questions where isDeleted (number) is 0
        if(filter.equals("All"))
        {
            query = firebaseFirestore.collection("Questions").whereEqualTo("isDeleted",0);
        }
        else
        {
            query = firebaseFirestore.collection("Questions").whereEqualTo("isDeleted",0).whereEqualTo("questionTitle", filter);
        }


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

                viewHolder.sharelayout.setOnClickListener(view -> {

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/*");
                    intent.putExtra(Intent.EXTRA_TEXT, "Check out the question "+ model.getQuestionTitle()+" in Geopedia\n Join Geopedia to view: :\n https://github.com/iatharva/Geopedia");
                    startActivity(Intent.createChooser(intent, "Share Question"));
                    /*
                    Dexter.withActivity(getActivity()).withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .withListener(new MultiplePermissionsListener() {
                                @Override
                                public void onPermissionsChecked(MultiplePermissionsReport report) {
                                    if (report.areAllPermissionsGranted()) {

                                        File file = saveBitMap(getActivity(), viewHolder.QuestionCard);
                                        if (file != null) {
                                            Log.i("TAG", "Drawing saved to the gallery!");

                                            Intent intent = new Intent(Intent.ACTION_SEND);
                                            intent.setType("image/*");
                                            intent.putExtra(Intent.EXTRA_TEXT, "Check out this question in Geopedia\n Join Geopedia to view: :\n https://github.com/iatharva/Geopedia");
                                            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                                            getActivity().startActivity(Intent.createChooser(intent, "Share Image"));

                                        } else {
                                            Log.i("TAG", "Oops! Image could not be saved.");
                                            Toast.makeText(getActivity(), "Failed sharing!", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(getActivity(), "Permissions are not granted!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(List<com.karumi.dexter.listener.PermissionRequest> list, PermissionToken permissionToken) {
                                    permissionToken.continuePermissionRequest();
                                }
                            }).check();
                     */
                });

            }
        };
        adapter.startListening();
        recycler_questions_user.setAdapter(adapter);
    }

    public static class FiltersViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView tv_name,tv_status,tv_description,tv_like,tv_comment,tv_time;
        RelativeLayout likelayout,commentfeedlayout,sharelayout,QuestionCard;
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
            QuestionCard = mView.findViewById(R.id.QuestionCard);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        showquestions("All",current_user_id);
    }

    private File saveBitMap(Context context, View drawView) {
        File pictureFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Handcare"); // enter folder name to save image
        if (!pictureFileDir.exists()) {
            boolean isDirectoryCreated = pictureFileDir.mkdirs();
            if (!isDirectoryCreated)
                Log.i("ATG", "Can't create directory to save the image");
            return null;
        }
        String filename = pictureFileDir.getPath() + File.separator + System.currentTimeMillis() + ".jpg";
        File pictureFile = new File(filename);
        Bitmap bitmap = getBitmapFromView(drawView);
        try {
            pictureFile.createNewFile();
            FileOutputStream oStream = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, oStream);
            oStream.flush();
            oStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("TAG", "There was an issue saving the image.");
        }
        scanGallery(context, pictureFile.getAbsolutePath());
        return pictureFile;
    }

    //create bitmap from view and returns it
    private Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    private void scanGallery(Context cntx, String path) {
        try {
            MediaScannerConnection.scanFile(cntx, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}