package uk.ac.tees.nhsdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.navigation.NavigationBarView;

import uk.ac.tees.nhsdemo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //replaceActivity(new UserActivity());

        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.app_user){
                    startActivity(new Intent(MainActivity.this, UserActivity.class));
                }else if (itemId == R.id.survey_icon){
                    startActivity(new Intent(MainActivity.this, SurveyActivity.class));
                }else if (itemId == R.id.response_icon){
                    startActivity(new Intent(MainActivity.this, ResponseActivity.class));
                }
                return true;
            }
        });


    }

    private void replaceActivity(Activity activity) {
    }
}