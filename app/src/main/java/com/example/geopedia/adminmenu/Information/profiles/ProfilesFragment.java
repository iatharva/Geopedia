package com.example.geopedia.adminmenu.Information.profiles;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.geopedia.R;
import com.example.geopedia.databinding.FragmentProfilesBinding;
import com.example.geopedia.extras.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ProfilesFragment extends Fragment {
    private RecyclerView recyclerView;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;
    private FragmentProfilesBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfilesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        firebaseFirestore = FirebaseFirestore.getInstance();
        final SwipeRefreshLayout pullToRefresh = root.findViewById(R.id.pullToRefresh);
        recyclerView=root.findViewById(R.id.admin_profiles_list);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        LoadList();
        //Refresh and reload the data
        pullToRefresh.setOnRefreshListener(() -> {
            LoadList();
            pullToRefresh.setRefreshing(false);
        });
        return root;
    }

    private void LoadList() {

        Query query=firebaseFirestore.collection("Users");
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<User,UsersViewHolder>(options) {
            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_user_list, parent, false);
                return new UsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder viewHolder, int position, final User model) {
                viewHolder.row_username.setText(String.format("%s %s", model.getFName(), model.getLName()));
                String Email = model.getEmail();
                String EmailSecured = Email.substring(0,3)+"****";
                viewHolder.row_email.setText(EmailSecured);
                String usertype=model.getIsAdmin();
                if(usertype.equals("1"))
                {
                    viewHolder.row_usertype.setText("User");
                }
                else if (usertype.equals("0"))
                {
                    viewHolder.row_username.setText("Admin and User");
                }
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private static class UsersViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView row_username,row_usertype,row_email;
        UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            row_username=mView.findViewById(R.id.row_username);
            row_usertype=mView.findViewById(R.id.row_usertype);
            row_email=mView.findViewById(R.id.row_email);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LoadList();
    }
}