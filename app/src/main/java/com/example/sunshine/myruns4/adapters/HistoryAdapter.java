package com.example.sunshine.myruns4.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sunshine.myruns4.R;
import com.example.sunshine.myruns4.models.ExerciseEntry;

import java.util.ArrayList;

public class HistoryAdapter extends  RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private ArrayList<ExerciseEntry> mItems;
    private onExerciseClickListener mOnExerciseClickListener;

    public HistoryAdapter(ArrayList<ExerciseEntry> items, onExerciseClickListener listener){
        super();
        mItems = items;
        mOnExerciseClickListener = listener;
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{
        // each data item is just a string in this case
        private TextView inputActivityType;
        private TextView timeStamp;
        private TextView distanceDuration;
        private onExerciseClickListener onExerciseClickListener;


        HistoryViewHolder(CardView view, onExerciseClickListener listener) {
            super(view);
            inputActivityType = view.findViewById(R.id.activity_input_type);
            timeStamp = view.findViewById(R.id.time_stamp);
            distanceDuration = view.findViewById(R.id.distance_duration);
            onExerciseClickListener = listener;

            view.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            onExerciseClickListener.onExerciseItemClick(getAdapterPosition());
        }
    }
    @NonNull
    @Override
    public HistoryAdapter.HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.exercise_history_view, parent, false);

        return new HistoryViewHolder(v, mOnExerciseClickListener);
    }

    /*
     * - get element from your dataset at this position
     *  - replace the contents of the view with that element
     */
    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.HistoryViewHolder holder, int position) {
        ExerciseEntry exercise = mItems.get(position);
        String inputActivityType = exercise.getInputType() + " " + exercise.getActivityType();
        String distanceDuration = exercise.getDistance() + ", " + exercise.getDuration();
        String dateTime = exercise.getDateTime();

        holder.inputActivityType.setText(inputActivityType);
        holder.distanceDuration.setText(distanceDuration);
        holder.timeStamp.setText(dateTime);
    }

    /*
     * Returns the total number of items in the data set held by the adapter.
     */
    @Override
    public int getItemCount() {
        if (mItems != null){
            return mItems.size();
        }
        return 0;
    }

    /*
     * Interface for handling Recycler item click events
     */
    public interface onExerciseClickListener{
        void onExerciseItemClick(int pos);
    }

}


