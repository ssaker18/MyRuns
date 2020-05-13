package com.example.sunshine.myruns4.database;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/*
 * AsyncTask for deleting exercise from DataBase
 * Calls deleteEntryByIndex() method in background
 */
public class DeleteExerciseTask extends AsyncTask<Long, Void, Void> {
    private static final String TAG = "DeleteExerciseTask";
    private final ExerciseDataSource mDataSource;
    private Context mContext;

    public DeleteExerciseTask(Context context) {
        mContext = context;
        mDataSource = new ExerciseDataSource(context);
        mDataSource.open();
    }
    @Override
    protected Void doInBackground(Long... id) {
        if (isCancelled()) {
            return null;
        }
        mDataSource.deleteEntryByIndex(id);

        Log.d(TAG, "doInBackground: still deleting");
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... unused) {
        if (isCancelled()) {
            Log.d(TAG, "onProgressUpdate: deleting in progress");
        }
    }

    @Override
    protected void onPostExecute(Void unused) {
        // Return to Main Activity on the history fragment
        ((Activity) mContext).finish();
        Log.d(TAG, "onPostExecute: deleted exercise");
    }
}
