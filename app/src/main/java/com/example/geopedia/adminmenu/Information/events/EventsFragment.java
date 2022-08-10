package com.example.geopedia.adminmenu.Information.events;

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

import com.example.geopedia.CommentFeed;
import com.example.geopedia.R;
import com.example.geopedia.databinding.FragmentEventsBinding;
import com.example.geopedia.extras.Events;
import com.example.geopedia.extras.Question;
import com.example.geopedia.usermenu.Uquestions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventsFragment extends Fragment {

    private RecyclerView recycler_events_admin;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    private FragmentEventsBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEventsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final SwipeRefreshLayout pullToRefresh = root.findViewById(R.id.pullToRefreshEvents);
        recycler_events_admin = root.findViewById(R.id.recycler_events_admin);
        firebaseFirestore = FirebaseFirestore.getInstance();

        recycler_events_admin.setHasFixedSize(true);
        recycler_events_admin.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_events_admin.setAdapter(adapter);
        showevents("All");
        //End of adapter code

        pullToRefresh.setOnRefreshListener(() -> {
            showevents("All");
            pullToRefresh.setRefreshing(false);
        });
        return root;
    }

    private void showevents(String filter) {
        Query query;
        if(filter.equals("All"))
        {
            query = firebaseFirestore.collection("Events");
        }else{
            //Update the query
            query = firebaseFirestore.collection("Orders").whereEqualTo("sellerid","userid")
                    .whereEqualTo("status",filter);
        }

        FirestoreRecyclerOptions<Events> options = new FirestoreRecyclerOptions.Builder<Events>()
                .setQuery(query, Events.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Events, FiltersViewHolder>(options) {
            @NotNull
            @Override
            public FiltersViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_event_admin, parent, false);

                return new FiltersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NotNull FiltersViewHolder viewHolder, int position, @NotNull Events model) {

                viewHolder.eventName.setText(model.getEventTitle());
                viewHolder.eventDescription.setText(model.getEventDesc());
                viewHolder.tv_time.setText(model.getDate()+" "+model.getTime());
            }
        };
        adapter.startListening();
        recycler_events_admin.setAdapter(adapter);
    }

    public static class FiltersViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView eventName,tv_time,eventDescription;
        RelativeLayout cardLayout;

        FiltersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            eventName = mView.findViewById(R.id.eventName);
            eventDescription = mView.findViewById(R.id.eventDescription);
            tv_time = mView.findViewById(R.id.tv_time);
            cardLayout = mView.findViewById(R.id.cardLayout);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        showevents("All");
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
