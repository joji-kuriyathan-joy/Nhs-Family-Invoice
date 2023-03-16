package uk.ac.tees.nhsdemo.model;

import java.util.HashMap;

public class UserResponse {
    public HashMap<String, String> user_response_to_questions;
    public String loggedIn_user_email;

    public UserResponse() {
    }

    public UserResponse(HashMap<String, String> userResponseMap, String currentUserEmailID) {
        this.user_response_to_questions = userResponseMap;
        this.loggedIn_user_email = currentUserEmailID;
    }
}
