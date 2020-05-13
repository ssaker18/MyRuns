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
import com.example.sunshine.myruns4.models.ExerciseEntry;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class TrackingService extends Service {
    public static final String TAG = TrackingService.class.getName();
    private static final int SERVICE_NOTIFICATION_ID = 1;
    private NotificationManager notificationManger;
    private ExerciseEntry mExerciseEntry;


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
     * Initialises an exercise entry with Activity and InputType
     * Also date and Time since these are required fields in the DB schema
     */
    private void initExerciseEntry(String activityType, String inputType) {
        mExerciseEntry = new ExerciseEntry();
        mExerciseEntry.setActivityType(activityType);
        mExerciseEntry.setInputType(inputType);
        mExerciseEntry.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        mExerciseEntry.setDate(java.time.LocalDate.now().toString());
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

            // set up exercise Entry
            initExerciseEntry(activityType, inputType);

            if (activityType != null && inputType.equals(MyConstants.ACTIVITY_AUTOMATIC)) {
                LocationIntentService.startLocationTracking(TrackingService.this, mExerciseEntry);
            } else if (activityType != null && inputType.equals(MyConstants.ACTIVITY_GPS)) {
                LocationIntentService.startLocationTracking(TrackingService.this, mExerciseEntry);
                ActivityIntentService.startActivityRecognition(TrackingService.this, mExerciseEntry);
            }
        }
        return START_STICKY;
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
        Log.d(TAG, "onDestroy(): Thread ID is: " + Thread.currentThread().getId());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: Thread ID is:" + Thread.currentThread().getId());
        return null;
    }

}
