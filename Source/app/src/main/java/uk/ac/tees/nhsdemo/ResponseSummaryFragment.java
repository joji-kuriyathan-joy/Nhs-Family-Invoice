package uk.ac.tees.nhsdemo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.ac.tees.nhsdemo.databinding.FragmentResponseSummaryBinding;

public class ResponseSummaryFragment extends Fragment {
    FragmentResponseSummaryBinding binding;

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
}