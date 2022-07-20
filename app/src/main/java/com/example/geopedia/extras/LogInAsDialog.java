package com.example.geopedia.extras;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.geopedia.HomeAdmin;
import com.example.geopedia.HomeUser;

public class LogInAsDialog extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Login As");
        builder.setMessage("Choose your login type");
        builder.setPositiveButton("Admin", (dialog, which) -> {
            Intent intent = new Intent(getActivity(), HomeAdmin.class);
            startActivity(intent);
            Toast.makeText(getActivity(), "Logged in Successfully as Admin", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("User", (dialog, which) -> {
            Intent intent = new Intent(getActivity(), HomeUser.class);
            startActivity(intent);
            Toast.makeText(getActivity(), "Welcome User", Toast.LENGTH_SHORT).show();
        });
        return builder.create();
    }
}

