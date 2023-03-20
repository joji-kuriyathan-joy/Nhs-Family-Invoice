package uk.ac.tees.nhsdemo.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import uk.ac.tees.nhsdemo.R;
import uk.ac.tees.nhsdemo.ResponseCardViewHolder;
import uk.ac.tees.nhsdemo.model.ResponseCardData;
import uk.ac.tees.nhsdemo.utils.ResponseClickListener;

public class ResponseAdapter extends RecyclerView.Adapter<ResponseCardViewHolder> {
    List<ResponseCardData> responseCardDataList = Collections.emptyList();

    Context context;
    ResponseClickListener listiner;

    public ResponseAdapter(List<ResponseCardData> responseCardDataList, Context context, ResponseClickListener listiner) {
        this.responseCardDataList = responseCardDataList;
        this.context = context;
        this.listiner = listiner;
    }

    @NonNull
    @Override
    public ResponseCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.activity_response_card, parent, false);
        ResponseCardViewHolder responseCardViewHolder = new ResponseCardViewHolder(view);
        return responseCardViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ResponseCardViewHolder holder, int position) {
        final int index = holder.getAdapterPosition();
        holder.day_TextView.setText(responseCardDataList.get(position).dayOfTheWeek);
        holder.date_TextView.setText(responseCardDataList.get(position).day);
        holder.month_TextView.setText(responseCardDataList.get(position).month);
        holder.time_TextView.setText(responseCardDataList.get(position).time);
        holder.createdBy_TextView.setText(responseCardDataList.get(position).createdBy);

        //16-03-2023
        //Save the response date time which is the Key in Db under which the response is stored.
        //Using the .setTag() we can set the value. This also act as a Hidden Field to store data
        //In the ResponseActivity class when a response is clicked use the .getTag() to retrieve the data
        holder.time_TextView.setTag(responseCardDataList.get(position).createdDateTimeKey);
        holder.responseCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listiner.click(v, index);
            }
        });

    }

    @Override
    public int getItemCount() {
        Log.d("ResponseAdapter", "Count of items in adapter" + responseCardDataList.size());
        return responseCardDataList.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
