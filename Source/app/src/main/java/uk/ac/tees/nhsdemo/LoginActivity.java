package uk.ac.tees.nhsdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    // iniializing all variables locally so they could be used in other methods
    private EditText email;
    private EditText password;
    private Button loginBtn;
    private TextView register, forgotPassword, survey;
    private ProgressBar loginProgressBar;
    private FirebaseAuth auth;
    private static final String TAG = "LoginActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        // Show and hide password using eye icon
        ImageView imageViewShowHidePwd = findViewById(R.id.show_hide_pwd);
        imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
        imageViewShowHidePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    // If password is visible then hide
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    // Change icon
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
                }else{
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });

        email = findViewById(R.id.email_text);
        password = findViewById(R.id.password_text);
        loginBtn = findViewById(R.id.login_button);
        register = findViewById(R.id.register_text);
        forgotPassword = findViewById(R.id.forget_password);
        loginProgressBar = findViewById(R.id.loginProgressBar);
        survey = findViewById(R.id.survey_demo);

        // set onClickListener for the forgot password text
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "You can reset your password now!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, ForgotPassword.class));
            }
        });

        // This is just a demo to check survey ui
        survey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SurveyActivity.class));
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String emailTxt = email.getText().toString().trim();
                final String passwordTxt = password.getText().toString().trim();


                if (emailTxt.isEmpty() || passwordTxt.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Empty Credentials", Toast.LENGTH_SHORT).show();
                    email.setError("Email is required");
                    email.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches()) {
                    Toast.makeText(LoginActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                    email.setError("Valid email is required");
                    email.requestFocus();
                } else{
                    loginProgressBar.setVisibility(View.VISIBLE);
                    loginUser(emailTxt, passwordTxt);
            }
            }
        });

        // This button sends us to the register activity
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

    }

    private void loginUser(String emailTxt, String passwordTxt) {
        auth.signInWithEmailAndPassword(emailTxt, passwordTxt).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    //ByPassing this activity to Survey Activity
                    startActivity(new Intent(LoginActivity.this, SurveyActivity.class));
                    //startActivity(new Intent(LoginActivity.this, UserActivity.class));
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e){
                        email.setError("User no longer valid. Please register again.");
                        email.requestFocus();
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        email.setError("Invalid credentials. Kindly check and re-enter");
                        email.requestFocus();
                    } catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                loginProgressBar.setVisibility(View.GONE);
            }
        });
    }
}