package uk.ac.tees.nhsdemo;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class HomeActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    BottomNavigationView bottomNavigationView;
    ContextCompat homeContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottomNavigationBar);

        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.user);
    }

    UserFragment userFragment = new UserFragment();
    ResponsesFragment completedFragment = new ResponsesFragment();
    SurveyFragment surveyFragment = new SurveyFragment();
    SettingsFragment settingsFragment = new SettingsFragment();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.user:
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, userFragment).commit();
                return true;

            case R.id.survey:
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, surveyFragment).commit();
                return true;

            case R.id.responses:
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, completedFragment).commit();
                return true;

            case R.id.settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, settingsFragment).commit();
                return true;

        }
        return false;
    }
}