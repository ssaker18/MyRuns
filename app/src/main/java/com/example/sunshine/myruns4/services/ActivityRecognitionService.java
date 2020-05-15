package com.example.sunshine.myruns4.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.Task;

public class ActivityRecognitionService extends Service {
    private static final String TAG = ActivityRecognitionService.class.getName();

    public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 20000; // every 20 seconds,
    // drains less battery and not as noisy

    private PendingIntent mPendingIntent;
    private ActivityRecognitionClient mActivityRecognitionClient;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //called from TrackingService's onStartCommand
    //in order to process GPS' activity type recognition feature
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        Intent mIntentService = new Intent(this, ActivityIntentService.class);
        mPendingIntent = PendingIntent.getService(this,
                1, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT);
        //use flagUpdateCurrent in order to update the pendingIntent if it already exists
        requestActivityUpdates();
        return START_STICKY;
    }
    //will request updates at an interval of 20 seconds
    private void requestActivityUpdates() {
        if(mActivityRecognitionClient!=null){
            Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                    DETECTION_INTERVAL_IN_MILLISECONDS,
                    mPendingIntent);
        }
    }


    // stop requesting activity updates from Google play
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mActivityRecognitionClient != null) {
            Task<Void> task = mActivityRecognitionClient.removeActivityUpdates(mPendingIntent);
        }
    }


}
