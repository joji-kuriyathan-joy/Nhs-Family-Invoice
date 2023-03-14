package uk.ac.tees.nhsdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPassword extends AppCompatActivity {
    private AppCompatButton resetPasswordBtn;
    private EditText editTextPwdResetEmail;
    private ProgressBar pwdResetProgressBar;
    private FirebaseAuth authProfile;
    private final static String TAG = "ForgotPwdActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        resetPasswordBtn = findViewById(R.id.reset_password_button);
        editTextPwdResetEmail = findViewById(R.id.editText_email);
        pwdResetProgressBar = findViewById(R.id.reset_password_progressBar);


        resetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextPwdResetEmail.getText().toString().trim();

                if (TextUtils.isEmpty(email)){
                    Toast.makeText(ForgotPassword.this, "Please enter your registered email", Toast.LENGTH_SHORT).show();
                    editTextPwdResetEmail.setError("Email is required");
                    editTextPwdResetEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toast.makeText(ForgotPassword.this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                    editTextPwdResetEmail.setError("Valid email is required");
                    editTextPwdResetEmail.requestFocus();
                } else {
                    pwdResetProgressBar.setVisibility(View.VISIBLE);
                    resetPassword(email);
                }
            }
        });
    }

    private void resetPassword(String email) {
        authProfile = FirebaseAuth.getInstance();
        authProfile.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(ForgotPassword.this, "Please check email for password reset link", Toast.LENGTH_SHORT).show();

                    // Redirect user to login activity after password reset
                    Intent intent = new Intent(ForgotPassword.this, LoginActivity.class);

                    // Clear stack to prevent user from going back into ForgotPasswordActivity after resetting password
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // Close ForgotPasswordActivity
                } else {
                    // Handling exceptions
                    try{
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e){
                        editTextPwdResetEmail.setError("User does not exist or no longer valid. Please register again");
                    } catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(ForgotPassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(ForgotPassword.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                pwdResetProgressBar.setVisibility(View.GONE);
            }
        });
    }
}