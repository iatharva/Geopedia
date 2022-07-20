package com.example.geopedia.usermenu;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.geopedia.AddQuestions;
import com.example.geopedia.HomeUser;
import com.example.geopedia.MainActivity;
import com.example.geopedia.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Uquestions extends Fragment {

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_uquestions, container, false);
        FloatingActionButton addQuestionFab = view.findViewById(R.id.addQuestionFab);

        addQuestionFab.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), AddQuestions.class);
            startActivity(intent);
        });
        return view;
    }
}
