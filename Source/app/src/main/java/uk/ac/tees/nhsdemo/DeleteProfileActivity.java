package uk.ac.tees.nhsdemo;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DeleteProfileActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private EditText txtUserPwd;
    private TextView txtDeleteHead2;
    private ProgressBar deleteUserPgBar;
    private String userPwd;
    private Button reAuthenticateBtn, deleteUserBtn;
    private static final String TAG = "DeleteProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

        deleteUserPgBar = findViewById(R.id.delete_pgB);
        txtUserPwd = findViewById(R.id.input_password);
        deleteUserBtn = findViewById(R.id.delete_profileBtn);
        reAuthenticateBtn = findViewById(R.id.authenticate_delete_userBtn);
        txtDeleteHead2 = findViewById(R.id.delete_head2);

        // Disable delete user button until user is authenticated
        deleteUserBtn.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser.equals("")){
            Toast.makeText(DeleteProfileActivity.this, "Something went wong!" +
                    "User details not available at the moment.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DeleteProfileActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            reAuthenticateUser(firebaseUser);
        }
    }
    // Re-authenticate user before deleting user
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        reAuthenticateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPwd = txtUserPwd.getText().toString();

                if (TextUtils.isEmpty(userPwd)){
                    Toast.makeText(DeleteProfileActivity.this, "Password is required", Toast.LENGTH_SHORT).show();
                    txtUserPwd.setError("Please enter your current password to authenticate");
                    txtUserPwd.requestFocus();
                } else {
                    deleteUserPgBar.setVisibility(View.VISIBLE);

                    // Re-authenticate user now
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPwd);
                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                deleteUserPgBar.setVisibility(View.GONE);

                                // Disable editText for current password. Enable for new password and confirm password
                                txtUserPwd.setEnabled(false);

                                // Enable delete button and disable authenticate button
                                reAuthenticateBtn.setEnabled(false);
                                deleteUserBtn.setEnabled(true);

                                // Set TextView to show user is authenticated/verified
                                txtDeleteHead2.setText("User verified. you can delete your profile and related data now!");
                                Toast.makeText(DeleteProfileActivity.this, "Password has been verified." + "You can delete account now", Toast.LENGTH_SHORT).show();

                                // Update color of change password button
                                deleteUserBtn.setBackgroundTintList(ContextCompat.getColorStateList(
                                        DeleteProfileActivity.this, R.color.light_green));
                                deleteUserBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        showAlertDialog();
                                    }
                                });
                            } else {
                                try {
                                    throw task.getException();
                                } catch (Exception e){
                                    Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            deleteUserPgBar.setVisibility(View.GONE);
                        }
                    });

                }
            }
        });

    }

    private void showAlertDialog() {
        // Setup the Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteProfileActivity.this);
        builder.setTitle("Delete user and related data");
        builder.setMessage("Are you sure you want to delete your profile and related data? This action is irreversible!");

        // Open Email app if user clicks/taps continue button
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUserData(firebaseUser);
            }
        });

        // Return to user profile activity if user click the cancel button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(DeleteProfileActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        // Create the AlertDialog
        AlertDialog alertDialog = builder.create();

        // Change "Continue" button color
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
            }
        });

        // Show the AlertDialog
        alertDialog.show();
    }

    private void deleteUser(){
        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    authProfile.signOut();
                    Toast.makeText(DeleteProfileActivity.this, "User has been deleted!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DeleteProfileActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    try {
                        throw task.getException();
                    } catch (Exception e){
                        Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                deleteUserPgBar.setVisibility(View.GONE);
            }
        });
    }

    // Delete all user data
    private void deleteUserData(FirebaseUser firebaseUser) {
        // Delete Display Pic. Also check if the user has uploaded any pic before deleting
        if (firebaseUser.getPhotoUrl() != null){
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(firebaseUser.getPhotoUrl().toString());
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d(TAG, "OnSuccess: Photo Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, e.getMessage());
                    Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


        // Delete data from Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");
        databaseReference.child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "OnSuccess: User Data Deleted");

                // Finally delete user after deleting related data
                deleteUser();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.getMessage());
                Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }
}