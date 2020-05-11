package com.example.sunshine.myruns4.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sunshine.myruns4.ManualEntryActivity;
import com.example.sunshine.myruns4.R;
import com.example.sunshine.myruns4.adapters.HistoryAdapter;
import com.example.sunshine.myruns4.database.ExerciseDataSource;
import com.example.sunshine.myruns4.database.ExerciseListLoader;
import com.example.sunshine.myruns4.models.ExerciseEntry;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

import static com.example.sunshine.myruns4.ManualEntryActivity.IMPERIAL_MILES;
import static com.example.sunshine.myruns4.ManualEntryActivity.MILE_CONVERSION_RATE;


public class HistoryFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<ArrayList<ExerciseEntry>>,
        HistoryAdapter.onExerciseClickListener {

    public static final String FRAGMENT_NAME = "History Fragment";
    private static final int FETCH_ALL_EXERCISES_ID = 1;
    private static final String TAG = HistoryFragment.class.getName();
    private static final String EXERCISE_ENTRY_ID = "id";
    private static final String SOURCE = "Source";
    private RecyclerView recyclerView;
    private ArrayList<ExerciseEntry> mExerciseHistoryData;
    private ExerciseDataSource dataSource;
    private LoaderManager mLoader;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.my_recycler_view);

        // improves performance since content changes don't change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // start AsyncTaskLoader
        mLoader = LoaderManager.getInstance(this);
        mLoader.initLoader(FETCH_ALL_EXERCISES_ID, null, this).forceLoad();

        // Grab reference to DB
        dataSource = new ExerciseDataSource(getContext());

        Log.d(TAG, "OnCreateView");
        return view;
    }


    /*
     * Creates AsyncTaskLoader for Fetching all exercise Entries in DB
     */
    @NonNull
    @Override
    public Loader<ArrayList<ExerciseEntry>> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == FETCH_ALL_EXERCISES_ID) {
            ExerciseListLoader exerciseListLoader = new ExerciseListLoader(getContext(), null);
            return exerciseListLoader; // null -> all entries
        }
        return null;
    }

    /*
     * Called after fetching all Exercise entries.
     * We apply the user's unit changes and update the Recycler View Adapter
     */
    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<ExerciseEntry>> loader, ArrayList<ExerciseEntry> data) {
        Log.d(TAG, "onLoadFinished(): Thread ID " + Thread.currentThread().getId());
        if (loader.getId() == FETCH_ALL_EXERCISES_ID) {
            applyUnitPreferences(data);
            mExerciseHistoryData = data;
            // History Adapter
            RecyclerView.Adapter mAdapter = new HistoryAdapter(mExerciseHistoryData, this);
            recyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }
    }

    /*
     * Depending on the User's Unit preference, (Miles / kilometers)
     * we update the the distance field of the exercise data passed to the Recycler Adapter
     */
    private void applyUnitPreferences(ArrayList<ExerciseEntry> data) {
        if (data == null) {
            return;
        }
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        if (sharedPreferences.getString("unit_preference", "").equals(IMPERIAL_MILES)) {
            data.forEach(exerciseEntry -> {
                String distance = exerciseEntry.getDistance();
                if (distance.contains("kms")) {
                    distance = distance.replace(" kms", "");
                    DecimalFormat df = new DecimalFormat("####0.00");
                    distance = df.format(Double.parseDouble(distance) / MILE_CONVERSION_RATE);
                    distance = distance + " miles";
                    exerciseEntry.setDistance(distance);
                }else {
                    Log.d(TAG, "Already in miles");
                }
            });
        } else {
            data.forEach(exerciseEntry -> {
                String distance = exerciseEntry.getDistance();
                if (distance.contains(" miles")) {
                    distance = distance.replace(" miles", "");
                    DecimalFormat df = new DecimalFormat("####0.00");
                    distance = df.format((Double.parseDouble(distance) * MILE_CONVERSION_RATE));
                    distance = distance + " kms";
                    exerciseEntry.setDistance(distance);
                } else {
                    Log.d(TAG, "Already in kms");
                }

            });
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<ExerciseEntry>> loader) {
    }

    /*
     * Closes DB
     */
    @Override
    public void onPause() {
        Log.d(TAG, "OnPause");
        dataSource.close();
        super.onPause();
    }

    /*
     * Fetches all exercises, before  fragment is in active view
     * Handles refreshing RecyclerView across device rotations
     */
    @Override
    public void onResume() {
        Log.d(TAG, "OnResume");
        super.onResume();
        Objects.requireNonNull(mLoader.getLoader(FETCH_ALL_EXERCISES_ID)).onContentChanged();
    }

    /*
     * Closes DB
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "OnDestroy");
        super.onDestroy();
        dataSource.close();

    }

    /*
     * Called when an exercise is clicked
     * We start ManualActivity passing the id of the selected exercise
     */
    @Override
    public void onExerciseItemClick(int pos) {
        ExerciseEntry exerciseEntry = mExerciseHistoryData.get(pos);
        Long id = exerciseEntry.getId();
        Intent intent = new Intent(getContext(), ManualEntryActivity.class);
        intent.putExtra(EXERCISE_ENTRY_ID, id);
        intent.putExtra(SOURCE, HistoryFragment.FRAGMENT_NAME);
        startActivity(intent);
        Log.d(TAG, "Exercise with _ID " + id + " Clicked");
    }
}
