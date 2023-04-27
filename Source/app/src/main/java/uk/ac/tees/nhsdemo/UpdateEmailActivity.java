package uk.ac.tees.nhsdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class UpdateEmailActivity extends AppCompatActivity {
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private ProgressBar upEmailPgBar;
    private TextView textViewAuthenticate;
    private String userOldEmail, userNewEmail, userPwd;
    private Button updateEmailButton;
    private EditText newEmail, textPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        upEmailPgBar = findViewById(R.id.updateEmail_pgB);
        textViewAuthenticate = findViewById(R.id.authenticate_userBtn);
        newEmail = findViewById(R.id.enter_new_email);
        textPwd = findViewById(R.id.input_password);
        updateEmailButton = findViewById(R.id.update_emailBtn);

        updateEmailButton.setEnabled(false); // disable button in the beginning until user is authenticated
        newEmail.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        // Set old email id on Textview
        userOldEmail = firebaseUser.getEmail();
        TextView textViewOldEmail = findViewById(R.id.show_current_email);
        textViewOldEmail.setText(userOldEmail);

        if (firebaseUser.equals("")){
            Toast.makeText(UpdateEmailActivity.this, "Something went wrong! User's details not available", Toast.LENGTH_SHORT).show();
        } else {
            reAuthenticate(firebaseUser);
        }

    }

    private void reAuthenticate(FirebaseUser firebaseUser) {
        Button buttonVerifyUser = findViewById(R.id.authenticate_userBtn);
        buttonVerifyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtain password authentication
                userPwd = textPwd.getText().toString();

                if (TextUtils.isEmpty(userPwd)){
                    Toast.makeText(UpdateEmailActivity.this, "Password is required to proceed", Toast.LENGTH_SHORT).show();
                    textPwd.setError("Please enter your password for authentication");
                    textPwd.requestFocus();
                } else {
                    upEmailPgBar.setVisibility(View.VISIBLE);

                    AuthCredential credential = EmailAuthProvider.getCredential(userOldEmail, userPwd);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                upEmailPgBar.setVisibility(View.GONE);
                                Toast.makeText(UpdateEmailActivity.this, "Password verified, continue to update email", Toast.LENGTH_SHORT).show();
                                // Set TextView to show that user is authenticated
                                textViewAuthenticate.setText("Your account is authenticated. You can update email now");

                                // Disable EditText for password and enable EditText for new email and update email button
                                newEmail.setEnabled(true);
                                textPwd.setEnabled(false);
                                buttonVerifyUser.setEnabled(false);
                                updateEmailButton.setEnabled(true);

                                // Change color of update email button
                                updateEmailButton.setBackgroundTintList(ContextCompat.getColorStateList(UpdateEmailActivity.this,
                                        R.color.light_green));

                                updateEmailButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        userNewEmail = newEmail.getText().toString();
                                        if (TextUtils.isEmpty(userNewEmail)) {
                                            Toast.makeText(UpdateEmailActivity.this, "New email is required", Toast.LENGTH_SHORT).show();
                                            newEmail.setError("Please enter new email");
                                            newEmail.requestFocus();
                                        } else if (!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()){
                                            Toast.makeText(UpdateEmailActivity.this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                                            newEmail.setError("Please provide valid email");
                                            newEmail.requestFocus();
                                        } else if (userOldEmail.matches(userNewEmail)){
                                            Toast.makeText(UpdateEmailActivity.this, "Choose a different email", Toast.LENGTH_SHORT).show();
                                            newEmail.setError("Please enter new email");
                                            newEmail.requestFocus();
                                        } else {
                                            upEmailPgBar.setVisibility(View.VISIBLE);
                                            updateEmail(firebaseUser);
                                        }
                                    }
                                });
                            } else {
                                try {
                                    throw task.getException();
                                } catch (Exception e){
                                    Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                    });
                }
            }
        });
    }

    private void updateEmail(FirebaseUser firebaseUser) {
        firebaseUser.updateEmail(userNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete()){
                    // Verify new Email
                    firebaseUser.sendEmailVerification();
                    Toast.makeText(UpdateEmailActivity.this, "Email has been updated. Please verify your new email", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdateEmailActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e){
                        Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                upEmailPgBar.setVisibility(View.GONE);
            }
        });
    }
}