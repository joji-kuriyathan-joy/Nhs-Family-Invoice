package uk.ac.tees.nhsdemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.errorprone.annotations.Var;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import uk.ac.tees.nhsdemo.databinding.FragmentUserBinding;

public class UserFragment extends Fragment {
    FragmentUserBinding binding;
    private TextView txt_welcome, fullNameTxt, emailTxt, mobileTxt, postcodeTxt, nhs_numberTxt, residentialTxt, txtDoB, txtGender, countrySelected;
    private String fullName,  email_address, mobile, postcode, nhs_number, residential_address, dob, gender, nationalityS;
    private ProgressBar userProgressBar;
    private FirebaseAuth authProfile;
    private ImageView selectImage;
    private SwipeRefreshLayout swipeContainer;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserBinding.inflate(getLayoutInflater());
//        binding = FragmentUserBinding.inflate(getLayoutInflater());
//        getActivity().setContentView(R.layout.fragment_user);

//        getSupportActionBar().setTitle("Profile");

        swipeToRefresh();
        
        txt_welcome = binding.showWelcomeMsg;
        fullNameTxt = binding.showFullName;
        emailTxt = binding.showEmail;
        mobileTxt = binding.showMobile;
        postcodeTxt = binding.showPostcode;
        nhs_numberTxt = binding.showNhsNumber;
        residentialTxt = binding.showResiAddress;
        txtDoB = binding.showDoB;
        txtGender = binding.showSex;
        countrySelected = binding.showNationality;
        selectImage = binding.userProfileImage;

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();


        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserFragment.this.getContext(), UpdateProfilePicture.class);
                startActivity(intent);
            }
        });

        if (firebaseUser == null){
            Toast.makeText(UserFragment.this.getContext(), "Something went wrong, user profile not available", Toast.LENGTH_SHORT).show();

        } else {
            // userProgressBar.setVisibility(View.GONE);
            showUserProfile(firebaseUser);
            // Here we checked if user has verified email
            checkIfEmailVerified(firebaseUser);
        }


        return binding.getRoot();
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_user, container, false);
    }

    private void swipeToRefresh() {
        // Look up for the swipe container
        swipeContainer = binding.userSwipeRefresh;

        //Setup refresh listener which triggers new data load
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Here we configure our swipeContainer and setup the color changes

            }
        });
    }


    // User in UserActivity after successful registration
    private void checkIfEmailVerified(FirebaseUser firebaseUser) {
        if (!firebaseUser.isEmailVerified()){
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        // Setup the Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(UserFragment.this.getContext());
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

                    Picasso.get().load(uri).into(selectImage);
//                    Glide.with(UserFragment.this.getContext()).load(uri).into(selectImage);
                }else{
                    Toast.makeText(UserFragment.this.getContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserFragment.this.getContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
            }
        });
    }

}