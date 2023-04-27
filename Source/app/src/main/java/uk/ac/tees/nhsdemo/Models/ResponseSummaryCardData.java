package uk.ac.tees.nhsdemo.Models;

public class ResponseSummaryCardData {
    public String responseQuestion, responseAnswer, responseQuestionNumber;

    public ResponseSummaryCardData(String responseQuestion, String responseAnswer, String responseQuestionNumber) {
        this.responseQuestion = responseQuestion;
        this.responseAnswer = responseAnswer;
        this.responseQuestionNumber = responseQuestionNumber;
    }
}
