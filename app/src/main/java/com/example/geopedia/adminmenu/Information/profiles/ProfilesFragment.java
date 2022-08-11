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

import java.util.Objects;

public class ProfilesFragment extends Fragment {
    private RecyclerView recycler_profiles_admin;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    private FragmentProfilesBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfilesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final SwipeRefreshLayout pullToRefresh = root.findViewById(R.id.pullToRefreshProfileAdmin);
        recycler_profiles_admin = root.findViewById(R.id.recycler_profiles_admin);
        firebaseFirestore = FirebaseFirestore.getInstance();

        recycler_profiles_admin.setHasFixedSize(true);
        recycler_profiles_admin.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_profiles_admin.setAdapter(adapter);
        LoadList();
        //End of adapter code

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
                    viewHolder.row_usertype.setText("Admin and User");
                }
                else if (usertype.equals("0"))
                {
                    viewHolder.row_usertype.setText("User");
                }
            }
        };
        adapter.startListening();
        recycler_profiles_admin.setAdapter(adapter);
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
    public void onResume()
    {
        super.onResume();
        final String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        LoadList();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}