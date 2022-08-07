package com.example.geopedia.accounts;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geopedia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Objects;

public class UpdateProfile extends AppCompatActivity {

    EditText FNameField,LNameField,DobField;
    Button updateBtn;
    TextView accountTypeData,DobInfo;
    private FirebaseAuth fAuth;
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public String UID,Email,FName,LName,Dob;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        fAuth = FirebaseAuth.getInstance();
        FNameField = findViewById(R.id.FNameField);
        LNameField = findViewById(R.id.LNameField);
        DobField = findViewById(R.id.DobField);
        updateBtn = findViewById(R.id.updateBtn);
        accountTypeData = findViewById(R.id.accountTypeData);
        DobInfo = findViewById(R.id.DobInfo);

        accountTypeData.setOnClickListener(view -> Toast.makeText(UpdateProfile.this,"Currently logged in as User with Free access account", Toast.LENGTH_LONG).show());
        DobField.setOnClickListener(view -> Toast.makeText(UpdateProfile.this,"Sorry, but you can't change your date of birth", Toast.LENGTH_LONG).show());

        updateBtn.setOnClickListener(view -> UpdateName());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getUserData();
    }

    public void getUserData()
    {
        UID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
        DocumentReference typeref = db.collection("Users").document(UID);
        typeref.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Email=documentSnapshot.getString("Email");
                FName=documentSnapshot.getString("FName");
                LName =documentSnapshot.getString("LName");
                Dob=documentSnapshot.getString("Dob");

                //get first 3 characters from string Email
                //EmailSecured = Email.substring(0,3)+"****";
                //EmailField.setHint(EmailSecured);
                FNameField.setText(FName);
                LNameField.setText(LName);
                DobField.setText(Dob);
                String[] dob = Dob.split("-");
                int age = getAge(Integer.parseInt(dob[2]),Integer.parseInt(dob[1]),Integer.parseInt(dob[0]));
                DobInfo.setText(String.valueOf(age)+" years");
            }
        });
    }

    private void UpdateName() {
        String FNameEntered,LNameEntered;
        //Get all the data from UI
        FNameEntered = FNameField.getText().toString().trim();
        LNameEntered = LNameField.getText().toString().trim();
        String correctEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        //Validations
        if(TextUtils.isEmpty(FNameEntered)){
            Toast.makeText(getApplicationContext(),"Please enter your name",Toast.LENGTH_SHORT).show();
            return;
        }
        else if(TextUtils.isEmpty(LNameEntered)) {
            Toast.makeText(getApplicationContext(), "Please enter your last name", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(FNameEntered.equals(FName) && LNameEntered.equals(LName))
        {
            Toast.makeText(getApplicationContext(), "Please change name to update", Toast.LENGTH_SHORT).show();
            return;
        }
        {
            assert correctEmail != null;
            ShowDialogForAuthentication(FNameEntered,LNameEntered,correctEmail);
        }
    }

    private void ShowDialogForAuthentication(String fNameEntered, String lNameEntered, String correctEmail) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Confirm your email to update");
        builder.setMessage("Hint:"+"****"+correctEmail.substring(correctEmail.indexOf("@")));
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);
        builder.setPositiveButton("Submit", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if(email.equals(correctEmail))
            {
                updateUserData(fNameEntered,lNameEntered,correctEmail,email);
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Email does not match",Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateUserData(String FNameEntered, String LNameEntered, String correctEmail, String emailEntered)
    {
        if(!emailEntered.equals(correctEmail))
        {
            Toast.makeText(getApplicationContext(),"Please enter correct email to update",Toast.LENGTH_SHORT).show();
        }
        else
        {
            DocumentReference userref = db.collection("Users").document(UID);
            userref
                    .update("FName",FNameEntered)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this,"Name updated!",Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Log.w(TAG, "FName not updated. Error :", e));

            DocumentReference userref1 = db.collection("Users").document(UID);
            userref1
                    .update("LName",LNameEntered)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this,"Name updated!",Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Log.w(TAG, "LName not updated. Error :", e));
        }
    }
    /**
     * Calculate the Age from Data of Birth of user
     */
    private int getAge(int year, int month, int day) {
        int age = 0;
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH)+1;
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        if (currentMonth > month) {
            age = currentYear - year;
        } else if (currentMonth == month) {
            if (currentDay >= day) {
                age = currentYear - year;
            } else {
                age = currentYear - year - 1;
            }
        } else {
            age = currentYear - year - 1;
        }
        return age;
    }
}