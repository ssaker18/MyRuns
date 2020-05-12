package com.example.sunshine.myruns4.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.sunshine.myruns4.models.ExerciseEntry;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class LocationIntentService extends IntentService {
    private static final long UPDATE_INTERVAL = 10000;
    private static final long FAST_INTERVAL = 1000;

    private static final String LOCATION_TRACKING = "Location Tracking";
    public static final String BROADCAST_LOCATION = "BroadCast Location";
    public static final String BROADCAST_ACTIVITY = "BroadCast Activity";

    private LocationCallback mLocationCallback;


    private static final String TAG = LocationIntentService.class.getName();

    public LocationIntentService() {
        super("LocationIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initLocationCallback();
        startLocationUpdates();
    }

    /*
     * Starts this service to perform action LocationTracking with the given parameters.
     * TODO: Customize helper method to start IntentService
     */
    public static void startLocationTracking(Context context, ExerciseEntry currentExercise) {
        Intent intent = new Intent(context, LocationIntentService.class);
        intent.setAction(LOCATION_TRACKING);
        context.startService(intent);
        Log.d(TAG, "startLocationTracking()");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (LOCATION_TRACKING.equals(action)) {
                ExerciseEntry exerciseEntry = null; // grab from intent
                handleLocationTracking(exerciseEntry);
                Log.d(TAG, "onHandleIntent(): starting Tracking");
            }else{
                Log.d(TAG, "onHandleIntent(): Unknown intent action");
            }
        }
    }

    /*
     * Handle action LocationTracking in the provided background thread with the provided
     * parameters.
     * TODO: Handle Location Tracking
     */
    private void handleLocationTracking(ExerciseEntry exerciseEntry) {
        Log.d(TAG, "onHandleIntent(): starting Tracking handling");
        startLocationUpdates();
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


    private void initLocationCallback(){
         mLocationCallback = new LocationCallback()  {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.d(TAG, " onLocationResult(): Thread ID is:" + Thread.currentThread().getId());
                Log.d(TAG, " onLocationResult(): Location is:" + locationResult.toString());
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
    }

}
