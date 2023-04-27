package uk.ac.tees.nhsdemo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ResponseSummaryCardViewHolder extends RecyclerView.ViewHolder {

    public View responseSummaryCardView;
    public TextView responseQuestionNum_TextView, responseQuestion_TextView, responseQuestionAnswer_TextView;

    public ResponseSummaryCardViewHolder(@NonNull View itemView) {
        super(itemView);
        responseQuestionNum_TextView = itemView.findViewById(R.id.responseQuestionNumber_txtview);
        responseQuestion_TextView = itemView.findViewById(R.id.responseSummaryQuestion_txtview);
        responseQuestionAnswer_TextView = itemView.findViewById(R.id.responseSummaryQuestionAnswer_txtView);
        responseSummaryCardView = itemView;
    }
}