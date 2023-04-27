package uk.ac.tees.nhsdemo;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import uk.ac.tees.nhsdemo.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {
    FragmentSettingsBinding binding;

    private TextView profile_text, update_profile_text, update_email_text, change_password_text, delete_profile_text, logout_text;
    private FirebaseAuth authProfile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(getLayoutInflater());

        authProfile = FirebaseAuth.getInstance();
//        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        binding.updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsFragment.this.getContext(), UpdateProfileActivity.class);
                startActivity(intent);
            }
        });

        binding.updateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsFragment.this.getContext(), UpdateEmailActivity.class);
                startActivity(intent);
            }
        });

        binding.changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsFragment.this.getContext(), UpdatePasswordActivity.class);
                startActivity(intent);
            }
        });

        binding.deleteProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsFragment.this.getContext(), DeleteProfileActivity.class);
                startActivity(intent);
            }
        });

        binding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authProfile.signOut();
                Toast.makeText(SettingsFragment.this.getContext(), "Logged out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SettingsFragment.this.getContext(), LoginActivity.class);

                // Clear stack to prevent user from going back into UserActivity after logging out
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish(); // Close UserActivity
            }
        });

        return binding.getRoot();
    }
}