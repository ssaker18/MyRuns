package com.example.sunshine.myruns4.database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.example.sunshine.myruns4.models.ExerciseEntry;

import java.util.ArrayList;

public class ExerciseListLoader extends AsyncTaskLoader {

    private static final String TAG = ExerciseListLoader.class.getName();
    private final ExerciseDataSource dataSource;
    private Long mQueryIndex;

    /*
     * Single Constructor For Async Loader for Exercises
     * If queryIndex is null; the loader fetches all entries
     * Else: the loader fetches the exercise with the specific queryIndex
     */
    public ExerciseListLoader(@NonNull Context context, Long queryIndex) {
        super(context);
        mQueryIndex = queryIndex;
        dataSource = new ExerciseDataSource(context);
        dataSource.open();
        Log.d(TAG, "ExerciseDataSource(): Thread ID " + Thread.currentThread().getId());
    }

    /*
     * Single Background Loader: Returns fetched exercises
     * Could be single entry array/ all entries fetched
     */
    @Nullable
    @Override
    public ArrayList<ExerciseEntry> loadInBackground() {
        Log.d(TAG, "loadInBackground(): Thread ID " + Thread.currentThread().getId());
        if (mQueryIndex == null){
            return dataSource.fetchEntries();
        }else{
            ArrayList<ExerciseEntry> exerciseEntry = new ArrayList<>();
            exerciseEntry.add(dataSource.fetchEntryByIndex(mQueryIndex));
            return exerciseEntry;
        }

    }
}
