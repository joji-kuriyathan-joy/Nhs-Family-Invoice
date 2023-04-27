 package uk.ac.tees.nhsdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

 public class UpdateProfileActivity extends AppCompatActivity {
     private TextView fullNameTxt, mobileTxt, postcodeTxt, nhs_numberTxt, residentialTxt, txtDoB, countrySelected;
     private String fullName, mobile, postcode, nhs_number, residential_address, dob, gender, nationalityS;
     private RadioGroup registerGender;
     private RadioButton selectedGenderBtn;
     private FirebaseAuth auth;
     private Spinner nationalityUpdate;
     private ProgressBar updateProgressBar;
     //private ArrayAdapter<CharSequence> nationalityAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        fullNameTxt = findViewById(R.id.edit_fullName);
        mobileTxt = findViewById(R.id.edit_mobile);
        postcodeTxt = findViewById(R.id.edit_postcode);
        nhs_numberTxt = findViewById(R.id.edit_nhs_number);
        txtDoB = findViewById(R.id.edit_doB);
        registerGender = findViewById(R.id.radio_gp_update_gender);
        residentialTxt = findViewById(R.id.edit_resi_address);
        updateProgressBar = findViewById(R.id.update_profile_progressbar);
        countrySelected = findViewById(R.id.update_country);
        EditText updateDob = findViewById(R.id.edit_doB);

        nationalityUpdate = findViewById(R.id.nationality);
//        nationalityAdapter = ArrayAdapter.createFromResource(this, R.array.array_countries, R.layout.spinner_layout);
//
//        // Specify the layout to use when the list of choices appear
//        nationalityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        // Set the adapter to the spinner to populate the spinner
//        nationalityS.setAdapter(nationalityAdapter);


        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();

        // Show Profile Data
        showProfile(firebaseUser);

        //upload profile picture
        Button updatePicBtn = findViewById(R.id.update_picture);
        updatePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfileActivity.this, UpdateProfilePicture.class);
                startActivity(intent);
                finish();
            }
        });

        /*Button updateEmailBtn = findViewById(R.id.update_email);
        updateEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfileActivity.this, UpdateEmailActivity.class);
                startActivity(intent);
                finish();
            }
        });*/

        updateDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Extracting saved dd, m, yyyy into different variables by creating an array delimited by "/"
                String textSADoB[] = dob.split("/");

                int day = Integer.parseInt(textSADoB[0]);
                int month = Integer.parseInt(textSADoB[1]) - 1; // This takes care of month index starting from 0
                int year = Integer.parseInt(textSADoB[2]);

                DatePickerDialog picker;
                // Date picker dialog
                picker = new DatePickerDialog(UpdateProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        txtDoB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        // Update profile
        Button updateProfileBtn = findViewById(R.id.update_profile);
        updateProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(firebaseUser);
            }
        });
    }

    // Update profile
     private void updateProfile(FirebaseUser firebaseUser) {
        int selectedGenderID = registerGender.getCheckedRadioButtonId();
        selectedGenderBtn = findViewById(selectedGenderID);
        updateProgressBar.setVisibility(View.VISIBLE);

//         String mobileRegex = "^(\\+44\\s?\\d{10}|0044\\s?\\d{10}|0\\s?\\d{10})?$";
//         Matcher mobileMatcher;
//         Pattern mobilePattern = Pattern.compile(mobileRegex);
//         mobileMatcher = mobilePattern.matcher(mobile);


         // checking if the user have filled all the required fields and in the correct manner
         if (TextUtils.isEmpty(fullName)) {
             Toast.makeText(UpdateProfileActivity.this, "Please enter full name", Toast.LENGTH_SHORT).show();
             fullNameTxt.setError("Full name is required");
             fullNameTxt.requestFocus();
         }  else if (TextUtils.isEmpty(mobile)) {
             Toast.makeText(UpdateProfileActivity.this, "Please enter mobile number", Toast.LENGTH_SHORT).show();
             mobileTxt.setError("Mobile number is required");
             mobileTxt.requestFocus();
         } else if (mobileTxt.length() != 10) {
             Toast.makeText(UpdateProfileActivity.this, "Please enter a valid mobile number", Toast.LENGTH_SHORT).show();
             mobileTxt.setError("Mobile number should be 10 digits");
             mobileTxt.requestFocus();
         }/*else if (!mobileMatcher.find()){
             Toast.makeText(UpdateProfileActivity.this, "Please enter a valid mobile number", Toast.LENGTH_SHORT).show();
             mobileTxt.setError("Mobile number is not valid");
             mobileTxt.requestFocus();
         }*/ else if (TextUtils.isEmpty(postcode)) {
             Toast.makeText(UpdateProfileActivity.this, "Please enter postcode", Toast.LENGTH_SHORT).show();
             postcodeTxt.setError("Postcode is required");
             postcodeTxt.requestFocus();
         } else if (TextUtils.isEmpty(nhs_number)) {
             Toast.makeText(UpdateProfileActivity.this, "Please enter NHS number", Toast.LENGTH_SHORT).show();
             nhs_numberTxt.setError("NHS number is required");
             nhs_numberTxt.requestFocus();
         } else if (TextUtils.isEmpty(residential_address)) {
             Toast.makeText(UpdateProfileActivity.this, "Please enter residential address", Toast.LENGTH_SHORT).show();
             residentialTxt.setError("Residential address is required");
             residentialTxt.requestFocus();
         } else if (TextUtils.isEmpty(dob)) {
             Toast.makeText(UpdateProfileActivity.this, "Please enter date of birth", Toast.LENGTH_SHORT).show();
             txtDoB.setError("Date of birth is required");
             txtDoB.requestFocus();
         } else if (TextUtils.isEmpty(selectedGenderBtn.getText())) {
             Toast.makeText(UpdateProfileActivity.this, "Please select your gender", Toast.LENGTH_SHORT).show();
             selectedGenderBtn.setError("Gender is required");
             selectedGenderBtn.requestFocus();
         } else {
             // Obtain the data entered by user
             gender = selectedGenderBtn.getText().toString();
             fullName = fullNameTxt.getText().toString();
             mobile = mobileTxt.getText().toString();
             postcode = postcodeTxt.getText().toString();
             nhs_number = nhs_numberTxt.getText().toString();
             residential_address = residentialTxt.getText().toString();
             dob = txtDoB.getText().toString();

             // Enter User data into the Firebase Realtime Database. Set up dependencies
             ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(gender, mobile, postcode, nhs_number, residential_address, dob, countrySelected);

             // Extract User reference from Database for "Registered Users"
             DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

             String userID = firebaseUser.getUid();

             referenceProfile.child(userID).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                 @Override
                 public void onComplete(@NonNull Task<Void> task) {
                     if (task.isSuccessful()){
                         // Setting new display name
                         UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().
                                 setDisplayName(fullName).build();
                         firebaseUser.updateProfile(profileUpdates);

                         Toast.makeText(UpdateProfileActivity.this, "Update Successful", Toast.LENGTH_SHORT).show();

                         // Stop user from returning to UpdateProfileActivity on pressing back button and close current activity
                         Intent intent = new Intent(UpdateProfileActivity.this, UserActivity.class);
                         intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                 Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                         startActivity(intent);
                         finish();
                     } else {
                         try{
                             throw task.getException();
                         }catch(Exception e){
                             Toast.makeText(UpdateProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                         }
                     }
                     updateProgressBar.setVisibility(View.GONE);
                 }
             });


         }
     }

     // Fetch data from Firebase and display
     private void showProfile(FirebaseUser firebaseUser) {
        String userIDofRegistered = firebaseUser.getUid();

        // Extracting user reference from the database for "Registered Users"
         DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
         updateProgressBar.setVisibility(View.VISIBLE);

         referenceProfile.child(userIDofRegistered).addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
            if (readUserDetails != null){
//                fullName = firebaseUser.getDisplayName();
                fullName = readUserDetails.fullName;
                mobile = readUserDetails.mobile;
                postcode = readUserDetails.postcode;
                nhs_number = readUserDetails.nhs_number;
                residential_address = readUserDetails.resi_address;
                dob = readUserDetails.doB;
                gender = readUserDetails.gender;
                nationalityS = readUserDetails.countrySelected;

                fullNameTxt.setText(fullName);
                mobileTxt.setText(mobile);
                postcodeTxt.setText(postcode);
                nhs_numberTxt.setText(nhs_number);
                residentialTxt.setText(residential_address);
                txtDoB.setText(dob);
                nationalityUpdate.getSelectedItem();

                // show gender through radio button
                if (gender.equals("Male")){
                    selectedGenderBtn = findViewById(R.id.radio_male);
                } else {
                    selectedGenderBtn = findViewById(R.id.radio_female);
                }
                selectedGenderBtn.setChecked(true);
            } else {
                Toast.makeText(UpdateProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
            updateProgressBar.setVisibility(View.GONE);
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                updateProgressBar.setVisibility(View.GONE);
             }
         });
     }
 }