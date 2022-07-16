package com.example.geopedia;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.geopedia.extras.LogInAsDialog;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LogIn extends AppCompatActivity {
    public EditText EmailField,PasswordField;
    public Button ForgetPasswordBtn,CreateAccountBtn,LogInBtn;
    private FirebaseAuth fAuth;
    private String UID;
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        fAuth = FirebaseAuth.getInstance();
        ForgetPasswordBtn = findViewById(R.id.ForgetPasswordBtn);
        CreateAccountBtn = findViewById(R.id.CreateAccountBtn);
        LogInBtn = findViewById(R.id.LogInBtn);
        EmailField = findViewById(R.id.EmailField);
        PasswordField = findViewById(R.id.PasswordField);

        //To open Forget Password Activity
        ForgetPasswordBtn.setOnClickListener(view -> {
            Intent i = new Intent(LogIn.this,ForgetPassword.class);
            startActivity(i);
        });

        //To open Create account Activity
        CreateAccountBtn.setOnClickListener(view -> {
            Intent i = new Intent(LogIn.this,CreateNewAccount.class);
            startActivity(i);
        });


        //To log in
        LogInBtn.setOnClickListener(view -> {
            //Getting data from UI
            String email = EmailField.getText().toString().trim();
            String password = PasswordField.getText().toString().trim();

            //Validation
            if (TextUtils.isEmpty(email)) {
                EmailField.setError("Email ID is Required.");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                PasswordField.setError("Password is Required.");
                return;
            }
            if (password.length() < 6) {
                PasswordField.setError("Password Must be more than 6 Characters");
                return;
            }

            // Authenticating the user
            LogInBtn.setText(R.string.signingin);
            fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful())
                {
                    //Success case (go to Home Screen) after checking for new user
                    String userid = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
                    DocumentReference typeref = db.collection("Users").document(userid);
                    typeref.get().addOnSuccessListener(documentSnapshot -> {
                        if(documentSnapshot.exists()){
                            String type= documentSnapshot.getString("IsAdmin");
                            assert type != null;
                            if(type.equals("1")){
                                //Show a dialog with radio buttons asking if user wanted to log in as admin or user
                                openLogInAsDialog();
                            }else if(type.equals("0")){
                                openLogInAsDialog();
                                //Intent intent = new Intent(LogIn.this, HomeAdmin.class);
                                //intent.putExtra("user_id" ,userid);
                                //startActivity(intent);
                                //Toast.makeText(LogIn.this, "Welcome User", Toast.LENGTH_SHORT).show();
                                //finish();
                            }
                        }
                    });
                }
                else
                {
                    //Failure case (Show error)
                    String errorMessage = Objects.requireNonNull(task.getException()).getMessage();
                    Toast.makeText(LogIn.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                }
                LogInBtn.setText(R.string.log_in);
            });
        });
    }

    //To login as admin or user
    public void loginAs() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LogIn.this);
        builder.setTitle("Login As");
        builder.setMessage("Choose your login type");
        builder.setPositiveButton("Admin", (dialog, which) -> {
            Intent intent = new Intent(LogIn.this, HomeAdmin.class);
            intent.putExtra("user_id" ,UID);
            startActivity(intent);
            Toast.makeText(LogIn.this, "Logged in Successfully as Admin", Toast.LENGTH_SHORT).show();
            finish();
        });
        builder.setNegativeButton("User", (dialog, which) -> {
            Intent intent = new Intent(LogIn.this, HomeAdmin.class);
            intent.putExtra("user_id" ,UID);
            startActivity(intent);
            Toast.makeText(LogIn.this, "Welcome User", Toast.LENGTH_SHORT).show();
            finish();
        });
        builder.show();
    }

    public void openLogInAsDialog(){
        LogInAsDialog logInAsDialog=new LogInAsDialog();
        logInAsDialog.show(getSupportFragmentManager(),"Log in as");
    }
}