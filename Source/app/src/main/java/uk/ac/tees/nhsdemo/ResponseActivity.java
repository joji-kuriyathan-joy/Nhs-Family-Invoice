package uk.ac.tees.nhsdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.tees.nhsdemo.adapters.ResponseAdapter;
import uk.ac.tees.nhsdemo.model.ResponseCardData;
import uk.ac.tees.nhsdemo.utils.ResponseClickListener;

public class ResponseActivity extends AppCompatActivity implements NavigationView
        .OnNavigationItemSelectedListener {

    ResponseAdapter responseAdapter;
    RecyclerView recyclerView;
    ResponseClickListener responseClickListener;
    TextView responseTime_TextView;
    String createdDateTimeKeyStr;
    private FirebaseAuth authProfile;
    private FirebaseDatabase database;
    private DatabaseReference mDatabaseUserResponseRef;
    private String firebaseUserEmail, userID;
    private HashMap<String, HashMap<String, Object>> responsesHashMapFromDb = new HashMap<>();
    ;
    private List<ResponseCardData> responseCardDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        //get the responses for the current logged in user from the database
        //get the current logged in user
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(ResponseActivity.this,
                    "Something went wrong, user profile not available", Toast.LENGTH_SHORT).show();
        } else {
            firebaseUserEmail = firebaseUser.getEmail();
            userID = firebaseUser.getUid();
            Log.d("ResponseActivity", "Logged in User Email:" + firebaseUserEmail + " User Id:" + userID);
        }


        mDatabaseUserResponseRef = FirebaseDatabase.getInstance().getReference("User_Responses");
        mDatabaseUserResponseRef.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    long count = snapshot.getChildrenCount();
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Log.d("ResponseActivity", "Fetching DB-> " + snap.getKey() + "\n Details :\n " + snap.getValue());
                        HashMap<String, Object> eachResponseDetails = (HashMap<String, Object>) snap.getValue();
                        responsesHashMapFromDb.put(snap.getKey(), eachResponseDetails);
                    }
                    responseCardDataList = getResponseDataFromDb();
                    recyclerView = findViewById(R.id.tasksRecyclerView);
                    responseClickListener = new ResponseClickListener() {
                        @Override
                        public void click(View v, int index) {
                            super.click(v, index);

                            responseTime_TextView = (TextView) v.findViewById(R.id.responseTime_txtview);
                            createdDateTimeKeyStr = (String) responseTime_TextView.getTag();
                            Toast.makeText(ResponseActivity.this, "clicked item index is "
                                    + index + "-- And Id :" + createdDateTimeKeyStr, Toast.LENGTH_LONG).show();
                        }

                        // -------- View Survey ActionButton START--------
//                        @Override
//                        public void view_survey_click(View v, int index) {
//                            super.view_survey_click(v, index);
//                            responseTime_TextView = (TextView) v.findViewById(R.id.responseTime_txtview);
//                            createdDateTimeKeyStr = (String) responseTime_TextView.getTag();
//                        }
                        // -------- View Survey ActionButton END--------
                    };
                    responseAdapter = new ResponseAdapter(responseCardDataList, getApplication(), responseClickListener);
                    recyclerView.setAdapter(responseAdapter);
                    recyclerView.setLayoutManager(
                            new LinearLayoutManager(ResponseActivity.this));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ResponseActivity.this, "Something went wrong! Firebase Data not accessible", Toast.LENGTH_LONG).show();
            }
        });


    }

    private List<ResponseCardData> getResponseDataFromDb() {
        List<ResponseCardData> list = new ArrayList<>();
        for (Map.Entry<String, HashMap<String, Object>> entry : responsesHashMapFromDb.entrySet()) {
            String createdDateTime = entry.getKey();
            String dayOfWeek = "";
            String day = "";
            String month = "";
            String year = "";
            String createdByMail = "";
            String whitespaceMetaChar = "\\s";
            String time = createdDateTime.split(whitespaceMetaChar)[1];

            HashMap<String, Object> responseDetailsHashMap = (HashMap<String, Object>) entry.getValue();
            for (Map.Entry<String, Object> stringObjectEntry : responseDetailsHashMap.entrySet()) {

                if (stringObjectEntry.getKey().equalsIgnoreCase("dayOfTheWeek")) {
                    dayOfWeek = (String) stringObjectEntry.getValue();
                } else if (stringObjectEntry.getKey().equalsIgnoreCase("day")) {
                    day = (String) stringObjectEntry.getValue();
                } else if (stringObjectEntry.getKey().equalsIgnoreCase("monthString")) {
                    month = (String) stringObjectEntry.getValue();
                } else if (stringObjectEntry.getKey().equalsIgnoreCase("year")) {
                    year = (String) stringObjectEntry.getValue();
                } else if (stringObjectEntry.getKey().equalsIgnoreCase("loggedIn_user_email")) {
                    createdByMail = (String) stringObjectEntry.getValue();
                }

            }

            list.add(new ResponseCardData(dayOfWeek, day, month, year, "Time: " + time,
                    "Created By: " + createdByMail, createdDateTime));
        }
        return list;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}