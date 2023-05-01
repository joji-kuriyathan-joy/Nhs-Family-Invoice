package uk.ac.tees.nhsdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class UserActivity extends AppCompatActivity {
    private TextView  txt_welcome, fullNameTxt, emailTxt, mobileTxt, postcodeTxt, nhs_numberTxt, residentialTxt, txtDoB, txtGender, countrySelected;
    private String fullName,  email_address, mobile, postcode, nhs_number, residential_address, dob, gender, nationalityS;
    private ProgressBar userProgressBar;
    private FirebaseAuth authProfile;
    private ImageView selectImage;

    BottomNavigationView navigationView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

//        getSupportActionBar().setTitle("Profile");

        txt_welcome = findViewById(R.id.show_welcome_msg);
        fullNameTxt = findViewById(R.id.show_full_name);
        emailTxt = findViewById(R.id.show_email);
        mobileTxt = findViewById(R.id.show_mobile);
        postcodeTxt = findViewById(R.id.show_postcode);
        nhs_numberTxt = findViewById(R.id.show_nhs_number);
        residentialTxt = findViewById(R.id.show_resi_address);
        txtDoB = findViewById(R.id.show_DoB);
        txtGender = findViewById(R.id.show_sex);
        countrySelected = findViewById(R.id.show_nationality);
        selectImage = findViewById(R.id.user_profile_image);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        navigationView = findViewById(R.id.bottomNavigationView);

        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.app_user:
                        startActivity(new Intent(UserActivity.this, UserActivity.class));
                        break;
                    case R.id.survey_icon:
                        startActivity(new Intent(UserActivity.this, SurveyActivity.class));
                        break;
                    case R.id.response_icon:
                        startActivity(new Intent(UserActivity.this, ResponseActivity.class));
                        break;

                    default:
                }

                return true;
            }
        });

        if (firebaseUser == null){
            Toast.makeText(UserActivity.this, "Something went wrong, user profile not available", Toast.LENGTH_SHORT).show();

        } else {
            // Here we checked if user has verified email
            checkIfEmailVerified(firebaseUser);
//            userProgressBar.setVisibility(View.GONE);
            showUserProfile(firebaseUser);
        }
    }

    // User in UserActivity after successful registration
    private void checkIfEmailVerified(FirebaseUser firebaseUser) {
        if (!firebaseUser.isEmailVerified()){
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        // Setup the Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email to continue.");

        // Open Email app if user clicks/taps continue button
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Start email app in new window
                startActivity(intent);
            }
        });
        // Create the AlertDialog
        AlertDialog alertDialog = builder.create();
        // Show the AlertDialog
        alertDialog.show();
    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();

        //Extracting User reference from Database for "Registered Users"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readWriteUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readWriteUserDetails != null){
                    fullName = readWriteUserDetails.fullName;
                    email_address = firebaseUser.getEmail();
                    mobile = readWriteUserDetails.mobile;
                    postcode = readWriteUserDetails.postcode;
                    nhs_number = readWriteUserDetails.nhs_number;
                    residential_address = readWriteUserDetails.resi_address;
                    dob = readWriteUserDetails.doB;
                    gender = readWriteUserDetails.gender;
                    nationalityS = readWriteUserDetails.countrySelected;


                    // Here we set the extracted data from the DataBase on the user ui
                    txt_welcome.setText("Welcome, " + fullName + "!");
                    fullNameTxt.setText(fullName);
                    emailTxt.setText(email_address);
                    mobileTxt.setText(mobile);
                    postcodeTxt.setText(postcode);
                    nhs_numberTxt.setText("NHS No.: " + nhs_number);
                    residentialTxt.setText(residential_address);
                    txtDoB.setText(dob);
                    txtGender.setText(gender);
                    countrySelected.setText(nationalityS);

                    // Set user DP after upload
                    Uri uri = firebaseUser.getPhotoUrl();

                    Glide.with(UserActivity.this).load(uri).into(selectImage);
                }else{
                    Toast.makeText(UserActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                userProgressBar.setVisibility(View.GONE);
            }
        });
    }

    // Creating ActionBar menu
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate menu items
//        getMenuInflater().inflate(R.menu.common_menu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    // When any menu item is selected
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == R. id.menu_profile){
//            Intent intent = new Intent(UserActivity.this, UserActivity.class);
//            startActivity(intent);
//        } else if(id == R.id.menu_update_profile){
//            Intent intent = new Intent(UserActivity.this, UpdateProfileActivity.class);
//            startActivity(intent);
//        } /*else if (id == R.id.menu_update_email){
//            Intent intent = new Intent(UserActivity.this, UpdateEmail.class);
//            startActivity(intent);
//        } else if (id == R.id.change_password){
//            Intent intent = new Intent(UserActivity.this, ChangePassword.class);
//            startActivity(intent);
//        } else if (id == R.id.delete_profile){
//            Intent intent = new Intent(UserActivity.this, DeleteProfile.class);
//            startActivity(intent);
//        } */else if (id == R.id.menu_logout){
//            authProfile.signOut();
//            Toast.makeText(UserActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(UserActivity.this, LoginActivity.class);
//
//            // Clear stack to prevent user from going back into UserActivity after logging out
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//            finish(); // Close UserActivity
//        } else {
//            Toast.makeText(UserActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
//        }
//        return super.onOptionsItemSelected(item);
//    }
}