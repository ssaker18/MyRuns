package com.example.sunshine.myruns4.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.sunshine.myruns4.MapActivity;
import com.example.sunshine.myruns4.R;
import com.example.sunshine.myruns4.constants.MyConstants;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.Task;


public class TrackingService extends Service {
    public static final String TAG = TrackingService.class.getName();
    private static final int SERVICE_NOTIFICATION_ID = 1;
    private static final long DETECTION_INTERVAL_IN_MILLISECONDS = 5000;

    private NotificationManager notificationManger;
    private PendingIntent mPendingIntent;
    private ActivityRecognitionClient mActivityRecognitionClient;


    public TrackingService() {
        Log.d(TAG, "TrackingService constructor(): Thread ID is:" + Thread.currentThread().getId());
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "TrackingService: onCreate(): Thread ID is:" + Thread.currentThread().getId());
        createNotification();
    }

    /*
     * Called from the Map Activity when Tracking Service is requested
     * We first notify the user of location tracking
     * Next, depending on the Activity Type and Input Type we call the appropriate
     * IntentServices to handle heavy lifting processes
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand(): Thread ID is:" + Thread.currentThread().getId());

        if (intent != null) {
            String activityType = intent.getStringExtra(MyConstants.ACTIVITY_TYPE);
            String inputType = intent.getStringExtra(MyConstants.INPUT_TYPE);

            if (activityType != null) {
                LocationIntentService.startLocationTracking(TrackingService.this);

                //start up activity recognition if we're in automatic mode
                if (inputType != null && inputType.equals(MyConstants.INPUT_AUTOMATIC)){
                    mActivityRecognitionClient = new ActivityRecognitionClient(this);
                    Intent mIntentService = new Intent(this, ActivityIntentService.class);
                    mIntentService.setAction(ActivityIntentService.getActivityRecognition());
                    mPendingIntent = PendingIntent.getService(this,
                            1, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT);
                    requestActivityUpdates();
                }
            }
        }
        return START_STICKY;
    }

    private void requestActivityUpdates() {
        if (mActivityRecognitionClient != null) {
            Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                    DETECTION_INTERVAL_IN_MILLISECONDS,
                    mPendingIntent);
        }
    }

    /*
     * Notifies the user about location tracking and allows user to get to
     * MapActivity through notifications
     */
    private void createNotification() {
        Intent notificationIntent = new Intent(this, MapActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // Create notification and its channel
        notificationManger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "tracking";
        String channelName = "MyRuns";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        notificationManger.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, channelId)
                .setContentTitle(MyConstants.NOTIFICATION_TITLE)
                .setContentText(MyConstants.NOTIFICATION_CONTENT_TEXT)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setOngoing(false)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(SERVICE_NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mActivityRecognitionClient != null) {
            Task<Void> task = mActivityRecognitionClient.removeActivityUpdates(mPendingIntent);
        }
        Log.d(TAG, "onDestroy(): Thread ID is: " + Thread.currentThread().getId());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: Thread ID is:" + Thread.currentThread().getId());
        return null;
    }

}
