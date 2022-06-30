package com.example.geopedia;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ForgetPassword extends AppCompatActivity {

    public EditText EmailField;
    public Button ReceiveMailBtn;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        firebaseAuth=FirebaseAuth.getInstance();
        EmailField = findViewById(R.id.EmailField);
        ReceiveMailBtn = findViewById(R.id.ReceiveMailBtn);

        //Method to send the reset link on click
        ReceiveMailBtn.setOnClickListener(view -> {
            String remail= EmailField.getText().toString();
            if(!TextUtils.isEmpty(remail)){
                //Firebase method for sending Reset Link
                firebaseAuth.sendPasswordResetEmail(remail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //Success case (Email Sent)
                            Toast.makeText(ForgetPassword.this,"Check your email inbox", Toast.LENGTH_LONG).show();
                        }else{
                            //Failure case (Email not Sent)
                            String errorMessage= Objects.requireNonNull(task.getException()).getMessage();
                            Toast.makeText(ForgetPassword.this,"Error: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }else{
                //Show message
                Toast.makeText(ForgetPassword.this,"Enter the email", Toast.LENGTH_LONG).show();
            }
        });
    }
}