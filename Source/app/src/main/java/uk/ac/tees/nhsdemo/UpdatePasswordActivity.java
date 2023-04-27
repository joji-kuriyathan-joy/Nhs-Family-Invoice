package uk.ac.tees.nhsdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

public class UpdatePasswordActivity extends AppCompatActivity {
    private FirebaseAuth authProfile;
    private EditText editTextCurrPwd, editTextNewPwd, editTextConfirmNew;
    private TextView textViewAuthenticate;
    private Button changePwdBtn, reAuthenticateBtn;
    private ProgressBar updatePwdPgBar;
    private String userCurrPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        editTextNewPwd = findViewById(R.id.enter_new_pwd);
        editTextCurrPwd = findViewById(R.id.input_password);
        editTextConfirmNew = findViewById(R.id.enter_confirm_pwd);
        textViewAuthenticate = findViewById(R.id.update_pwd_head2);
        updatePwdPgBar = findViewById(R.id.updatePwd_pgB);
        reAuthenticateBtn = findViewById(R.id.authenticate_pwd_userBtn);
        changePwdBtn = findViewById(R.id.update_passwordBtn);

        // Disable editText for new password, confirm new password and make change button unresponsive until user is authenticated
        editTextNewPwd.setEnabled(false);
        editTextConfirmNew.setEnabled(false);
        changePwdBtn.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser.equals("")){
            Toast.makeText(UpdatePasswordActivity.this, "Something went wrong! User details not available",
                    Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UpdatePasswordActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            reAuthenticateUser(firebaseUser);
        }
    }

    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        reAuthenticateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userCurrPwd = editTextCurrPwd.getText().toString();

                if (TextUtils.isEmpty(userCurrPwd)){
                    Toast.makeText(UpdatePasswordActivity.this, "Password is required", Toast.LENGTH_SHORT).show();
                    editTextCurrPwd.setError("Please enter your current password to authenticate");
                    editTextCurrPwd.requestFocus();
                } else {
                    updatePwdPgBar.setVisibility(View.VISIBLE);

                    // Re-authenticate user now
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userCurrPwd);
                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                updatePwdPgBar.setVisibility(View.GONE);

                                // Disable editText for current password. Enable for new password and confirm password
                                editTextCurrPwd.setEnabled(false);
                                editTextNewPwd.setEnabled(true);
                                editTextConfirmNew.setEnabled(true);

                                // Enable change password button and disable authenticate button
                                reAuthenticateBtn.setEnabled(false);
                                changePwdBtn.setEnabled(true);

                                // Set TextView to show user is authenticated/verified
                                textViewAuthenticate.setText("User verified. you can change password now!");
                                Toast.makeText(UpdatePasswordActivity.this, "Password has been verified." + "Change password now", Toast.LENGTH_SHORT).show();

                                // Update color of change password button
                                changePwdBtn.setBackgroundTintList(ContextCompat.getColorStateList(
                                        UpdatePasswordActivity.this, R.color.light_green));
                                changePwdBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        changePwd(firebaseUser);
                                    }
                                });
                            } else {
                                try {
                                    throw task.getException();
                                } catch (Exception e){
                                    Toast.makeText(UpdatePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            updatePwdPgBar.setVisibility(View.GONE);
                        }
                    });

                }
            }
        });

    }

    private void changePwd(FirebaseUser firebaseUser) {
        String userNewPwd = editTextNewPwd.getText().toString();
        String userConfirmNewPwd = editTextConfirmNew.getText().toString();

        if (TextUtils.isEmpty(userNewPwd)){
            Toast.makeText(UpdatePasswordActivity.this, "New password is needed", Toast.LENGTH_SHORT).show();
            editTextNewPwd.setError("Please enter your new password");
            editTextNewPwd.requestFocus();
        } else if (TextUtils.isEmpty(userConfirmNewPwd)){
            Toast.makeText(UpdatePasswordActivity.this, "Please confirm new password", Toast.LENGTH_SHORT).show();
            editTextConfirmNew.setError("Please confirm new password");
            editTextConfirmNew.requestFocus();
        } else if (!userNewPwd.matches(userConfirmNewPwd)){
            Toast.makeText(UpdatePasswordActivity.this, "Password does not match", Toast.LENGTH_SHORT).show();
            editTextConfirmNew.setError("Please re-enter same password");
            editTextConfirmNew.requestFocus();
        } else if (userCurrPwd.matches(userNewPwd)){
            Toast.makeText(UpdatePasswordActivity.this, "New password cannot be the same as old", Toast.LENGTH_SHORT).show();
            editTextNewPwd.setError("Please enter a new password");
            editTextNewPwd.requestFocus();
        } else {
            updatePwdPgBar.setVisibility(View.VISIBLE);

            firebaseUser.updatePassword(userNewPwd).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(UpdatePasswordActivity.this, "Password has been changed", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UpdatePasswordActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        try{
                            throw task.getException();
                        } catch (Exception e){
                            Toast.makeText(UpdatePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    updatePwdPgBar.setVisibility(View.GONE);
                }
            });
        }
    }
}