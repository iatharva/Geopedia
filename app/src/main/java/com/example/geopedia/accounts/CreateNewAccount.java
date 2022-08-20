package com.example.geopedia.accounts;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.geopedia.HomeAdmin;
import com.example.geopedia.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateNewAccount extends AppCompatActivity {
    public EditText EmailField,PasswordField,FNameField,LNameField,DateField;
    public String Email,Password,FName,LName,DateOfBirth,UID;
    public DatePickerDialog.OnDateSetListener mDateSetListener;
    public Button AddAccountBtn;
    private FirebaseAuth fAuth;
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_account);
        EmailField = findViewById(R.id.EmailField);
        PasswordField = findViewById(R.id.PasswordField);
        FNameField = findViewById(R.id.FNameField);
        LNameField = findViewById(R.id.LNameField);
        DateField = findViewById(R.id.DateField);
        AddAccountBtn = findViewById(R.id.AddAccountBtn);

        LocationManager locationManager = (LocationManager) CreateNewAccount.this.getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        //Get Date DialogBox
        DateField.setOnClickListener(v -> {
            Calendar cal= Calendar.getInstance();
            int year=cal.get(Calendar.YEAR);
            int month=cal.get(Calendar.MONTH);
            int day=cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog= new DatePickerDialog(
                    CreateNewAccount.this,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    mDateSetListener,
                    year,month,day);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });

        //Set Date in proper format
        mDateSetListener= (view, year, month, dayOfMonth) -> {
            month=month+1;
            String date= dayOfMonth + "-" + month+"-"+ year;
            DateField.setText(date);
        };

        //Add Account in Firebase
        AddAccountBtn.setOnClickListener(view -> {

            //Get all the data from UI
            Email = EmailField.getText().toString().trim();
            Password = PasswordField.getText().toString().trim();
            FName = FNameField.getText().toString().trim();
            LName = LNameField.getText().toString().trim();
            DateOfBirth = DateField.getText().toString().trim();

            //Validations
            if(TextUtils.isEmpty(Email)){
                Toast.makeText(getApplicationContext(),"Please enter email",Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(Password)){
                Toast.makeText(getApplicationContext(),"Please enter password",Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(FName)){
                Toast.makeText(getApplicationContext(),"Please enter your name",Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(LName)){
                Toast.makeText(getApplicationContext(),"Please enter your last name",Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(DateOfBirth)){
                Toast.makeText(getApplicationContext(),"Select your date of birth",Toast.LENGTH_SHORT).show();
                return;
            }

            //Firebase operations
            fAuth = FirebaseAuth.getInstance();
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            fAuth.createUserWithEmailAndPassword(Email,Password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    UID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
                    Map<String, Object> user = new HashMap<>();
                    //get current date and time
                    java.util.Date date = new java.util.Date();
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMMM yyyyy");
                    String currentDate = sdf.format(date);
                    java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("hh:mm a");
                    String currentTime = sdf2.format(date);

                    user.put("Uid", UID);
                    user.put("Email", Email);
                    user.put("FName", FName);
                    user.put("LName", LName);
                    user.put("Dob", DateOfBirth);
                    user.put("IsPaid", "0");
                    user.put("IsAdmin", "0");
                    user.put("LastLongitude",currentLongitude);
                    user.put("LastLatitude",currentLatitude);
                    user.put("JoinedOn",currentDate+" "+currentTime);
                    //Insert and check if user is data is inserted successfully and user is created
                    db.collection("Users").document(UID).set(user).addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()) {
                            //Success Case
                            AddAccountBtn.setText(R.string.create_account);
                            Toast.makeText(CreateNewAccount.this,"Account created succesfully!", Toast.LENGTH_LONG).show();
                            String userid = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
                            DocumentReference typeref = db.collection("Users").document(userid);
                            typeref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.exists()){
                                        String type= documentSnapshot.getString("IsAdmin");
                                        Toast.makeText(CreateNewAccount.this, "Logged in Successfully as "+type, Toast.LENGTH_SHORT).show();
                                        assert type != null;
                                        if(type.equals("1")){
                                            Intent intent = new Intent(CreateNewAccount.this, HomeAdmin.class);
                                            startActivity(intent);
                                            finish();
                                        }else if(type.equals("0")){
                                            Intent intent = new Intent(CreateNewAccount.this, HomeAdmin.class);
                                            intent.putExtra("user_id" ,userid);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                }
                            });
                        } else{
                            //Failure Case
                            AddAccountBtn.setText(R.string.create_account);
                            String errorMessage = Objects.requireNonNull(task1.getException()).getMessage();
                            Toast.makeText(CreateNewAccount.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                }else {
                    //Exception Case
                    Toast.makeText(CreateNewAccount.this, "Error! " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

    }
}