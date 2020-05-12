package com.example.sunshine.myruns4.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.sunshine.myruns4.MapActivity;
import com.example.sunshine.myruns4.R;
import com.example.sunshine.myruns4.models.ExerciseEntry;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class TrackingService extends Service {
    public static final String TAG = TrackingService.class.getName();
    public static final String BROADCAST_ACTIVITY = "BroadCast Activity";
    private static final long UPDATE_INTERVAL = 5000;
    private static final long FAST_INTERVAL = 1000;
    private static final int SERVICE_NOTIFICATION_ID = 1;
    public static final String BROADCAST_LOCATION = "BroadCast Location";
    private NotificationManager notificationManger;
    private ExerciseEntry mExerciseEntry;


    public TrackingService() {
        Log.d(TAG, "TrackingService: Thread ID is:" + Thread.currentThread().getId());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "TrackingService: onCreate(): Thread ID is:" + Thread.currentThread().getId());
        initExerciseEntry();
        startLocationUpdates(); // TODO: May have to initialise an Intent Service class
        startActivityUpdates();

    }

    /*
     * Initialises an exercise entry
     */
    private void initExerciseEntry() {
        mExerciseEntry = new ExerciseEntry();
    }


    private void startActivityUpdates() {
        // TODO: May have to initialise an Intent Service class
    }

    private void startLocationUpdates() {
        // set criteria
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FAST_INTERVAL);

        // check if the system can support the criteria
        getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
        Log.d(TAG, "onStartLocationUpdates(): Thread ID is:" + Thread.currentThread().getId());

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand(): Thread ID is:" + Thread.currentThread().getId());
        createNotification();
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
        notificationManger = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "tracking";
        String channelName = "MyRuns";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        notificationManger.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, channelId)
                .setContentTitle("MyRuns")
                .setContentText("Tracking your locations")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setOngoing(false)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(SERVICE_NOTIFICATION_ID, notification);
    }

    private LocationCallback mLocationCallback = new LocationCallback()  {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Log.d(TAG, " onLocationResult(): Thread ID is:" + Thread.currentThread().getId());
            Intent intent = new Intent(BROADCAST_LOCATION);
            intent.putExtra("location", locationResult.getLastLocation());
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
            Log.d(TAG, "onLocationAvailability(): Thread ID is:" + Thread.currentThread().getId());
        }
    };
    

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
