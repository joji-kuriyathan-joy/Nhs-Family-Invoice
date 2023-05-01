package uk.ac.tees.nhsdemo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import uk.ac.tees.nhsdemo.Adapters.ResponseSummaryAdapter;
import uk.ac.tees.nhsdemo.Models.ResponseSummaryCardData;
import uk.ac.tees.nhsdemo.Utils.ResponseClickListener;
import uk.ac.tees.nhsdemo.databinding.FragmentResponseSummaryBinding;

public class ResponseSummaryFragment extends Fragment {
    FragmentResponseSummaryBinding binding;
    private FirebaseAuth authProfile;
    private FirebaseDatabase database;
    RecyclerView recyclerView;
    ResponseSummaryAdapter responseSummaryAdapter;
    ResponseClickListener responseClickListener;
    private DatabaseReference mDatabaseUserResponseRef,mDatabaseSurveyQuestionsRef;
    private String firebaseUserEmail, userID;
    private HashMap<String, HashMap<String, Object>> responsesHashMapFromDb = new HashMap<>();
    private final HashMap<Integer, String> questionsMap = new HashMap<>();
    private List<ResponseSummaryCardData> responseSummaryCardDataList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentResponseSummaryBinding.inflate(getLayoutInflater());

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResponseSummaryFragment.this
                        .getParentFragmentManager()
                        .popBackStackImmediate();
            }
        });

        //retrieving data using bundle
        Bundle bundle=getArguments();
        String selectedResponseDateTime = String.valueOf(bundle.getString("selectedResponseDateTimeKey"));
        Log.d("ResponseSummaryFragment","===>>> "+selectedResponseDateTime);
        //get the responses for the current logged in user from the database
        //get the current logged in user
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(ResponseSummaryFragment.this.getContext(),
                    "Something went wrong, user profile not available", Toast.LENGTH_SHORT).show();
        } else {
            firebaseUserEmail = firebaseUser.getEmail();
            userID = firebaseUser.getUid();
            Log.d("ResponseSummaryFragment", "Logged in User Email:" + firebaseUserEmail + " User Id:" + userID);
        }

        //-------get the questions from the db
        mDatabaseSurveyQuestionsRef = FirebaseDatabase.getInstance().getReference("Survey_Questions");
        mDatabaseSurveyQuestionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    long count = snapshot.getChildrenCount();


                    for (DataSnapshot snap : snapshot.getChildren()) {
                        questionsMap.put(Integer.valueOf(snap.getKey()), snap.getValue(String.class));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ResponseSummaryFragment.this.getContext(), "Something went wrong! Firebase Data not accessible", Toast.LENGTH_LONG).show();
            }
        });
        //------- end

        //-------get the User_Responses from the db
        mDatabaseUserResponseRef = FirebaseDatabase.getInstance().getReference("User_Responses");
        mDatabaseUserResponseRef.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null){
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Log.d("ResponseSummaryFragment", "Response Created DateTime-> " + snap.getKey() + "\n Details :\n " + snap.getValue());
                        if(snap.getKey().equalsIgnoreCase(selectedResponseDateTime)) {
                            HashMap<String, Object> eachResponseDetails = (HashMap<String, Object>) snap.getValue();
                            responsesHashMapFromDb.put(snap.getKey(), eachResponseDetails);
                        }
                    }

                    responseSummaryCardDataList = getUserResponseDataFromDB();
                    Log.d("ResponseSummaryFragment","Items in the list "+responseSummaryCardDataList.size());
                    recyclerView = getActivity().findViewById(R.id.responseSummaryRecyclerView);
//                    responseClickListener = new ResponseClickListener();
                    responseSummaryAdapter = new ResponseSummaryAdapter(responseSummaryCardDataList,getActivity().getApplication());
                    recyclerView.setAdapter(responseSummaryAdapter);
                    recyclerView.setLayoutManager(
                            new LinearLayoutManager(ResponseSummaryFragment.this.getContext()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ResponseSummaryFragment.this.getContext(), "Something went wrong! Firebase Data not accessible", Toast.LENGTH_LONG).show();

            }
        });
       return binding.getRoot();
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_response_summary, container, false);
    }

//    public void onBackPressed(){
//        FragmentManager fragmentManager = getParentFragmentManager();
//        if(fragmentManager.getBackStackEntryCount() > 0){
//            fragmentManager.popBackStackImmediate();
//        } else {
//            super.onB
//        }
//    }
private List<ResponseSummaryCardData> getUserResponseDataFromDB(){
    Log.d("ResponseSummaryFragment", "getUserResponseDataFromDB()");
    List<ResponseSummaryCardData> list = new ArrayList<>();
    for (Map.Entry<String, HashMap<String, Object>> entry : responsesHashMapFromDb.entrySet()) {
        HashMap<String, Object> responseDetailsHashMap = (HashMap<String, Object>) entry.getValue();
        //all details for the survey
        for (Map.Entry<String, Object> stringObjectEntry : responseDetailsHashMap.entrySet()) {
            //section to get only the survey questions and answers
            if (stringObjectEntry.getKey().equalsIgnoreCase("user_response_to_questions")) {
                HashMap<String,String> responseSummaryMap = (HashMap<String, String>) stringObjectEntry.getValue();

                //Sort the questions according to ascending order
                String question="",response="",questionNumber="";
                for (int questionCounter = 1;questionCounter<= responseSummaryMap.size(); questionCounter++){
                    for(Map.Entry<String, String> eachResponseSummaryMap : responseSummaryMap.entrySet()) {
                         question = eachResponseSummaryMap.getKey();
                         response = eachResponseSummaryMap.getValue();
                         questionNumber = getQuestionNumberFromQuestion(question);
                         if(Integer.valueOf(questionNumber).equals(questionCounter)){
                             break;
                         }
                    }
                    list.add(new ResponseSummaryCardData("Q." + question, "Response:" + response, questionNumber));

                }
            }
        }
    }

    return list;
}

    private String getQuestionNumberFromQuestion(String currentQuestion){
        Log.d("ResponseSummaryFragment", "getQuestionNumberFromQuestion()");

        String questionNumber ="";
        for(Map.Entry<Integer, String> eachQuestion : questionsMap.entrySet()){
            if(eachQuestion.getValue().equalsIgnoreCase(currentQuestion)){
                questionNumber = String.valueOf(eachQuestion.getKey());
                break;
            }
        }
        return  questionNumber;
    }
}