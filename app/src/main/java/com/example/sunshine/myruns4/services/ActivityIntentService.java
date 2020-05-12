package com.example.sunshine.myruns4.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.example.sunshine.myruns4.models.ExerciseEntry;


public class ActivityIntentService extends IntentService {

    private static final String TAG = ActivityIntentService.class.getName();
    private static final String ACTIVITY_RECOGNITION = "Activity Recognition";

    public ActivityIntentService() {
        super("ActivityIntentService");
    }


    /*
     * Starts this service to perform Activity Recognition with the given parameters
     * TODO: Customize helper method to start IntentService
     */
    public static void startActivityRecognition(Context context, ExerciseEntry currentExercise) {
        Intent intent = new Intent(context, ActivityIntentService.class);
        intent.setAction(ACTIVITY_RECOGNITION);
        context.startService(intent);
    }

    /*
     * Handles the call to start Activity Recognition Intent Service
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTIVITY_RECOGNITION.equals(action)) {
                handleActivityRecognition();
            }
        }
    }

    /*
     * Handles action ActivityRecognition in the provided background thread
     * TODO: Handle Activity Recognition
     */
    private void handleActivityRecognition() {
      return;
    }


}
