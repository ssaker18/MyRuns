package com.example.sunshine.myruns4.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.sunshine.myruns4.constants.MyConstants;
import com.example.sunshine.myruns4.models.ExerciseEntry;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.Task;

import java.util.List;


public class ActivityIntentService extends IntentService {

    private static final String TAG = ActivityIntentService.class.getName();
    private static final String ACTIVITY_RECOGNITION = "Activity Recognition";
    private ExerciseEntry mCurrExercise;
    private ActivityRecognitionClient mActivityRecognitionClient;
    private PendingIntent mPendingIntent;


    public ActivityIntentService() {
        super("ActivityIntentService");
    }

//    @Override
//    public void onCreate() {
//        super.onCreate();
//        mActivityRecognitionClient = new ActivityRecognitionClient(this);
//        Intent mIntentService = new Intent(this, ActivityIntentService.class);
//        mPendingIntent = PendingIntent.getService(this,
//                1, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT);
//        //use flagUpdateCurrent in order to update the pendingIntent if it already exists
//        requestActivityUpdates();
//    }
//
//    private void requestActivityUpdates() {
//        if (mActivityRecognitionClient != null) {
//            Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
//                    DETECTION_INTERVAL_IN_MILLISECONDS,
//                    mPendingIntent);
//        }
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (mActivityRecognitionClient != null) {
//            Task<Void> task = mActivityRecognitionClient.removeActivityUpdates(mPendingIntent);
//        }
//    }

    /*
     * Starts this service to perform Activity Recognition with the given parameters
     * TODO: Customize helper method to start IntentService
     *  QUESTION: DOES onCreate or StartActivityRecognition execute first?
     */
    public static void startActivityRecognition(Context context, ExerciseEntry currentExercise) {
        Intent intent = new Intent(context, ActivityIntentService.class);
        intent.setAction(ACTIVITY_RECOGNITION);
        intent.putExtra(MyConstants.CURRENT_EXERCISE, currentExercise);
        context.startService(intent);
        Log.d(TAG, "On startActivityRecognition()");
    }

    /*
     * Handles the call to start Activity Recognition Intent Service
     */
    @Override
    protected void onHandleIntent(Intent intent) {//retrieve currentExercise
        if (intent != null) {
            Bundle bundle = intent.getBundleExtra(MyConstants.CURRENT_EXERCISE);
            mCurrExercise = bundle.getParcelable(MyConstants.CURRENT_EXERCISE);
            final String action = intent.getAction();
            if (ACTIVITY_RECOGNITION.equals(action)) {
                handleActivityRecognition(intent);
            }
        }//OnCreate is only called once--use logic from onStartCommand in onCreate in order to achieve intervals
    }

    /*
     * Handles action ActivityRecognition in the provided background thread
     */
    private void handleActivityRecognition(Intent intent) {
        ActivityRecognitionResult extractedResults = ActivityRecognitionResult.extractResult(intent);

        if (extractedResults == null) {
            return;
        }

        List<DetectedActivity> detectedActivities = extractedResults.getProbableActivities();

        if (detectedActivities.isEmpty()) {
            return;
        }

        //find the activity type with the highest confidence value and broadcast to MapActivity
        int max = detectedActivities.get(0).getConfidence();
        int maxIndex = 0;
        //iterate through the list of detectedActivities
        //then fire off intents in order to broadcast the information to MapActivity
        //FOR NOW: ONLY BROADCAST THE ACTIVITY TYPE WITH HIGHEST CONFIDENCE!
        //take mExerciseEntry
        //broadcast the exerciseEntry itself
        //will be on onRecieve in MapActivity
        for (int i = 0; i < detectedActivities.size(); i++) {
            if (max < detectedActivities.get(i).getConfidence()) {
                max = detectedActivities.get(i).getConfidence();
                maxIndex = i;
            }
        }
        //set exerciseActivity's activity type to the activity type with the highest confidence value
        mCurrExercise.setActivityType(convertDetectedActivityToString(detectedActivities.get(maxIndex).getType()));


        Intent toBroadcast = new Intent(ACTIVITY_RECOGNITION);
        //send updated ExerciseEntry
        toBroadcast.putExtra(MyConstants.CURRENT_EXERCISE, mCurrExercise);
        toBroadcast.putExtra(MyConstants.DETECTED_ACTIVITY, convertDetectedActivityToString(detectedActivities.get(maxIndex).getType()));
        //might need to be updated_exercises for the constant tag
        LocalBroadcastManager.getInstance(this).sendBroadcast(toBroadcast);

    }

    private String convertDetectedActivityToString(int type) {
        String toReturn = "Unrecognized";
        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                toReturn = "In_Vehicle";
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                toReturn = "On_Bicycle";
                break;
            }
            case DetectedActivity.ON_FOOT: {
                toReturn = "On_Foot";
                break;
            }
            case DetectedActivity.RUNNING: {
                toReturn = "Running";
                break;
            }
            case DetectedActivity.STILL: {
                toReturn = "Still";
                break;
            }
            case DetectedActivity.TILTING: {
                toReturn = "Tilting";
                break;
            }
            case DetectedActivity.WALKING: {
                toReturn = "Walking";
                break;
            }
            case DetectedActivity.UNKNOWN: {
                break;
            }
        }
        return toReturn;
    }

    public static String getActivityRecognition() {
        return ACTIVITY_RECOGNITION;
    }

}
