package uk.ac.tees.nhsdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ResponseCardViewHolder extends RecyclerView.ViewHolder {

    public TextView day_TextView, month_TextView, date_TextView,time_TextView,createdBy_TextView,response_viewbtn;
    public View responseCardView;

    public ResponseCardViewHolder(@NonNull View itemView) {
        super(itemView);
        day_TextView = itemView.findViewById(R.id.dayTextView);
        month_TextView = itemView.findViewById(R.id.monthTextView);
        date_TextView= itemView.findViewById(R.id.dateTextView);
        time_TextView = itemView.findViewById(R.id.responseTime_txtview);
        createdBy_TextView = itemView.findViewById(R.id.responseUserName_txtView);
        response_viewbtn = itemView.findViewById(R.id.response_viewbtn);
        responseCardView = itemView;

    }
}