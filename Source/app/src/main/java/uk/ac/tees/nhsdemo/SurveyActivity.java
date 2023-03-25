package uk.ac.tees.nhsdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.ac.tees.nhsdemo.model.SurveyData;

public class SurveyActivity extends AppCompatActivity {
    private FirebaseAuth authProfile;
    private FirebaseDatabase database;
    private DatabaseReference mDatabaseSurveyQuestionsRef, mDatabaseUserResponseRef;
    private String firebaseUserEmail, userID;

    private TextView questionNumberTextView, questionTextView,
            previousQuestionTextView, nextQuestionTextView;
    private Button allOfTheTimeBtn, mostOfTheTimeBtn, someOfTheTimeBtn,
            noneOfTheTimeBtn, dontKnowBtn, submitBtn;
    private ConstraintLayout prevConstraintLayout, nextConstraintLayout;
    private final HashMap<Integer, String> questionsMap = new HashMap<>();
    private final HashMap<String, String> respondsToQuestionMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        //declare all the widgets from the Survey screen necessary
        questionNumberTextView = findViewById(R.id.questionNumber);
        questionTextView = findViewById(R.id.question);
        previousQuestionTextView = findViewById(R.id.prev_text);
        nextQuestionTextView = findViewById(R.id.next_text);
        allOfTheTimeBtn = findViewById(R.id.answer1_btn);
        mostOfTheTimeBtn = findViewById(R.id.answer2_btn);
        someOfTheTimeBtn = findViewById(R.id.answer3_btn);
        noneOfTheTimeBtn = findViewById(R.id.answer4_btn);
        dontKnowBtn = findViewById(R.id.answer5_btn);
        submitBtn = findViewById(R.id.submit_btn);

        //Button Layout for Previous and next
        prevConstraintLayout = findViewById(R.id.prev_constraintLayout);
        nextConstraintLayout = findViewById(R.id.next_constraintLayout);

        //when the activity is created the question to show will be the first question
        //So hide the Previous Button and Submit Btn. Note the Submit btn needs to be visible only at the final question
        prevConstraintLayout.setVisibility(View.INVISIBLE);
        submitBtn.setVisibility(View.INVISIBLE);

        //get the current logged in user
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(SurveyActivity.this,
                    "Something went wrong, user profile not available", Toast.LENGTH_SHORT).show();
        } else {
            firebaseUserEmail = firebaseUser.getEmail();
            userID = firebaseUser.getUid();
            Log.d("SurveyActivity", "Logged in User Email:" + firebaseUserEmail + " User Id:" + userID);
        }
        //get current date and time
        //get the questions from the db
        mDatabaseSurveyQuestionsRef = FirebaseDatabase.getInstance().getReference("Survey_Questions");
        mDatabaseSurveyQuestionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    long count = snapshot.getChildrenCount();


                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Log.d("SurveyActivity", snap.getKey() + "--" + snap.getValue(String.class));
                        questionsMap.put(Integer.valueOf(snap.getKey()), snap.getValue(String.class));
                    }
                    //display the first question and question number
                    questionTextView.setText(questionsMap.get(1));
                    questionNumberTextView.setText("1");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SurveyActivity.this, "Something went wrong! Firebase Data not accessible", Toast.LENGTH_LONG).show();
            }
        });
        //------- Navigation button events -------
        //navigate to previous questions
        previousQuestionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check what is the current question number
                String currentQuestionNumber = (String) questionNumberTextView.getText();
                //hide the previous button if this is the first question
                if (Integer.valueOf(currentQuestionNumber) - 1 == 1) {
                    prevConstraintLayout.setVisibility(View.INVISIBLE);
                }
                //hide the submit button if the current question number is less than the count of questions
                //replace the question and number
                if (Integer.valueOf(currentQuestionNumber) - 1 < questionsMap.size()) {
                    nextConstraintLayout.setVisibility(View.VISIBLE);
                    submitBtn.setVisibility(View.INVISIBLE);
                    questionTextView.setText(questionsMap.get(Integer.valueOf(currentQuestionNumber) - 1));
                    questionNumberTextView.setText(String.valueOf(Integer.valueOf(currentQuestionNumber) - 1));
                } else {
                    questionTextView.setText(questionsMap.get(Integer.valueOf(currentQuestionNumber) - 1));
                    questionNumberTextView.setText(String.valueOf(Integer.valueOf(currentQuestionNumber) - 1));
                }
                setSelectedAnswerBackgroundChange(getResponseForPreviousQuestion((String) questionTextView.getText()),v);
            }
        });

        //navigate to next question
        nextQuestionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Make all Answer buttons background color default
                allOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
                mostOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
                someOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
                noneOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
                dontKnowBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));


                //check if user has responded to previous question
                boolean hasResponded = checkUserResponseToPreviousQuestion((String) questionTextView.getText());
                if (hasResponded) {
                    //check  what is the current question number
                    String currentQuestionNumber = (String) questionNumberTextView.getText();
                    //show the previous button if this is not the first question to navigate
                    if (Integer.valueOf(currentQuestionNumber) + 1 > 1) {
                        prevConstraintLayout.setVisibility(View.VISIBLE);
                    }
                    //show the submit button if this is the last question
                    //Replace the question and question number with the next.
                    if (Integer.valueOf(currentQuestionNumber) + 1 == questionsMap.size()) {
                        nextConstraintLayout.setVisibility(View.INVISIBLE);
                        submitBtn.setVisibility(View.VISIBLE);
                        questionTextView.setText(questionsMap.get(Integer.valueOf(currentQuestionNumber) + 1));
                        questionNumberTextView.setText(String.valueOf(Integer.valueOf(currentQuestionNumber) + 1));
                    } else {
                        questionTextView.setText(questionsMap.get(Integer.valueOf(currentQuestionNumber) + 1));
                        questionNumberTextView.setText(String.valueOf(Integer.valueOf(currentQuestionNumber) + 1));
                    }
                    setSelectedAnswerBackgroundChange(getResponseForPreviousQuestion((String) questionTextView.getText()),v);
                } else {
                    Toast.makeText(SurveyActivity.this,
                            "Please choose any one response for this question.", Toast.LENGTH_LONG).show();
                }

            }
        });

        //---------Responds Button events -------
        allOfTheTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String allOfTheTimeStr = getString(R.string.answer_1);
                Log.d("SurveyActivity", allOfTheTimeStr);
                setResponseToQuestions(allOfTheTimeStr, (String) questionTextView.getText());
                setSelectedAnswerBackgroundChange(allOfTheTimeStr,v);
            }
        });
        mostOfTheTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mostOfTheTimeStr = getString(R.string.answer_2);
                Log.d("SurveyActivity", mostOfTheTimeStr);
                setResponseToQuestions(mostOfTheTimeStr, (String) questionTextView.getText());
                setSelectedAnswerBackgroundChange(mostOfTheTimeStr,v);
            }
        });
        someOfTheTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String someOfTheTimeStr = getString(R.string.answer_3);
                Log.d("SurveyActivity", someOfTheTimeStr);
                setResponseToQuestions(someOfTheTimeStr, (String) questionTextView.getText());
                setSelectedAnswerBackgroundChange(someOfTheTimeStr,v);
            }
        });
        noneOfTheTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String noneOfTheTimeStr = getString(R.string.answer_4);
                Log.d("SurveyActivity", noneOfTheTimeStr);
                setResponseToQuestions(noneOfTheTimeStr, (String) questionTextView.getText());
                setSelectedAnswerBackgroundChange(noneOfTheTimeStr,v);
            }
        });
        dontKnowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dontKnowStr = getString(R.string.answer_5);
                Log.d("SurveyActivity", dontKnowStr);
                setResponseToQuestions(dontKnowStr, (String) questionTextView.getText());
                setSelectedAnswerBackgroundChange(dontKnowStr,v);

            }
        });

        //-------- Submit Button Event -------
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if user has responded to last question
                boolean hasResponded = checkUserResponseToPreviousQuestion((String) questionTextView.getText());
                if (hasResponded) {
                    //create a Hashmap for question as key and response as value
                    //include current date and time
                    //include the current user emailID
                    HashMap<String, String> userResponseToQuestionHashMap = new HashMap<>();
                    for (Map.Entry<Integer, String> entry : questionsMap.entrySet()) {
                        //each question
                        String question = entry.getValue();
                        //check the question in the respondsToQuestionMap and get the response.
                        //Finally put the question and response to the userResponseToQuestionHashMap
                        if (respondsToQuestionMap.containsKey(question)) {
                            userResponseToQuestionHashMap.put(question,
                                    (String) respondsToQuestionMap.get(question));
                        }
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    Date date = new Date();
                    String currentDateAndTimeStr = sdf.format(date);
                    String dayOfTheWeek = (String) DateFormat.format("EEEE", date); // Thursday
                    String day          = (String) DateFormat.format("dd",   date); // 20
                    String monthString  = (String) DateFormat.format("MMM",  date); // Jun
                    String year         = (String) DateFormat.format("yyyy", date); // 2013
                    Log.d("SurveyActivity", "Current Date Time : "+currentDateAndTimeStr);
                    SurveyData surveyData = new SurveyData(userResponseToQuestionHashMap,
                            firebaseUserEmail,dayOfTheWeek,monthString,year,day);
                    //Store the details in the Response db
                    // Get reference to Firebase Realtime Database [User_Responses] node
                    mDatabaseUserResponseRef = FirebaseDatabase.getInstance().getReference("User_Responses");

                    mDatabaseUserResponseRef.child(firebaseUser.getUid()).child(currentDateAndTimeStr)
                            .setValue(surveyData).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(SurveyActivity.this,
                                    "Survey successfully completed!", Toast.LENGTH_LONG).show();
                            // Open user profile after each successful registration
                            Intent intent = new Intent(SurveyActivity.this, ResponseActivity.class);

                            // To prevent user from returning to register activity on pressing back button after registration
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SurveyActivity.this,
                                    "Something went wrong while submitting the survey. Please try again.", Toast.LENGTH_LONG).show();

                        }
                    });

                } else {
                    Toast.makeText(SurveyActivity.this, "Please choose any one response for this question.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //**function to check if user has choose any response to current question before going next question
    private boolean checkUserResponseToPreviousQuestion(String currentQuestion) {
        boolean hasResponded = respondsToQuestionMap.containsKey(currentQuestion.trim());
        return hasResponded;
    }

    private String getResponseForPreviousQuestion(String prevQuestion){
        String prev_response = "";
        if(respondsToQuestionMap.containsKey(prevQuestion)){
            prev_response = respondsToQuestionMap.get(prevQuestion);
        }
        return prev_response;
    }

    //This is a generic method that attaches respond to the current question.
    private void setResponseToQuestions(String respondType, String currentQuestion) {
        //Add Or Update the respondsToQuestionMap Hashmap with the current selected choice
        //
        //check if the question is already added to the map then update current response
        if (respondsToQuestionMap.containsKey(currentQuestion.trim())) {
            respondsToQuestionMap.put(currentQuestion.trim(), respondType);
        } else {
            respondsToQuestionMap.put(currentQuestion.trim(), respondType);
        }
        //Just to display the values in the Map
        for (Map.Entry<String, String> entry : respondsToQuestionMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Log.d("SurveyActivity", "Q:" + key + "-- Ans:" + value);
        }
    }

    private void setSelectedAnswerBackgroundChange(String selectedAnsweStr,View v){
        if(selectedAnsweStr.equalsIgnoreCase(getString(R.string.answer_1))){
            allOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button_selected));
            mostOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
            someOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
            noneOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
            dontKnowBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));

        }
        else if(selectedAnsweStr.equalsIgnoreCase(getString(R.string.answer_2))){
            allOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
            mostOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button_selected));
            someOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
            noneOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
            dontKnowBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
        }
        else if(selectedAnsweStr.equalsIgnoreCase(getString(R.string.answer_3))){
            allOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
            mostOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
            someOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button_selected));
            noneOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
            dontKnowBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));

        }else if(selectedAnsweStr.equalsIgnoreCase(getString(R.string.answer_4))){
            allOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
            mostOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
            someOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
            noneOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button_selected));
            dontKnowBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
        }else if(selectedAnsweStr.equalsIgnoreCase(getString(R.string.answer_5))){
            allOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
            mostOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
            someOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
            noneOfTheTimeBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button));
            dontKnowBtn.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.answer_button_selected));
        }
    }
}