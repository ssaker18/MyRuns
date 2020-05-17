package com.example.sunshine.myruns4.database;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.sunshine.myruns4.R;
import com.example.sunshine.myruns4.models.ExerciseEntry;


/*
 * AsyncTask to insert into database
 */
public class ExerciseInsertTask extends AsyncTask<ExerciseEntry, Void, Void> {
    private static final String TAG = ExerciseInsertTask.class.getName() ;
    private Context context;
    private ExerciseDataSource dataSource;

    public ExerciseInsertTask(Context context, ExerciseDataSource db){
        super();
        this.context = context;
        this.dataSource = db;
        this.dataSource.open();
    }

    @Override
    protected Void doInBackground(ExerciseEntry... exerciseEntries) {

        for (ExerciseEntry exerciseEntry : exerciseEntries) {
            long insertId = dataSource.insertEntry(exerciseEntry);
            exerciseEntry.setId(insertId);
            Log.d(TAG, "inserting exercise id: " + exerciseEntry.getId() + "in db");
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... unused) {
    }

    @Override
    protected void onPostExecute(Void unused) {
            Toast.makeText(this.context, R.string.save_exercise_sucess, Toast.LENGTH_SHORT).show();
            ((Activity)this.context).finish();
    }
}
