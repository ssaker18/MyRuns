package com.example.sunshine.myruns4.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.sunshine.myruns4.constants.MyConstants;
import com.example.sunshine.myruns4.models.ExerciseEntry;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class LocationIntentService extends IntentService {
    private static final long UPDATE_INTERVAL = 10000;
    private static final long FAST_INTERVAL = 1000;

    private static final String LOCATION_TRACKING = "Location Tracking"; //TODO
    public static final String BROADCAST_LOCATION = "BroadCast Location"; //TODO
    public static final String BROADCAST_ACTIVITY = "BroadCast Activity";  //TODO

    private LocationCallback mLocationCallback;


    private static final String TAG = LocationIntentService.class.getName();
    private ExerciseEntry mCurrExercise;

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
     * Provides static method for starting this Intent service
     * to perform action LocationTracking with the given parameters.
     * Caller must provide a valid currentExercise
     */
    public static void startLocationTracking(Context context, ExerciseEntry currentExercise) {
        Intent intent = new Intent(context, LocationIntentService.class);
        intent.setAction(LOCATION_TRACKING);
        intent.putExtra(MyConstants.CURRENT_EXERCISE, currentExercise);
        context.startService(intent);
        Log.d(TAG, "startLocationTracking()");
    }

    /*
     * Entry Point for Intent Service. Caller calls startLocationTracking to begin intentService
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (LOCATION_TRACKING.equals(action)) {
                ExerciseEntry exerciseEntry = intent.getParcelableExtra(MyConstants.CURRENT_EXERCISE); // grab from intent
                handleLocationTracking(exerciseEntry);
                Log.d(TAG, "onHandleIntent(): starting Tracking");
            } else {
                Log.d(TAG, "onHandleIntent(): Unknown intent action");
            }
        }
    }

    /*
     * Handle action LocationTracking in the provided background thread with the provided
     * parameters.
     */
    private void handleLocationTracking(ExerciseEntry exerciseEntry) {
        Log.d(TAG, "onHandleIntent(): starting Tracking handling");
        mCurrExercise = exerciseEntry;
        startLocationUpdates();
    }

    /*
     * Responsible for setting up criteria and initialising Location Request Calls
     */
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

    /*
     * Sets up CallBack for location Requests. OnLocationResult we add the new location
     * to the exercise entry's location list and send broadcast
     */
    private void initLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.d(TAG, " onLocationResult(): Thread ID is:" + Thread.currentThread().getId());
                Log.d(TAG, " onLocationResult(): Location is:" + locationResult.toString());
                Intent intent = new Intent(BROADCAST_LOCATION);
                addNewLocationToExercise(locationResult);
                intent.putExtra(MyConstants.CURRENT_EXERCISE, mCurrExercise);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                Log.d(TAG, "onLocationAvailability(): Thread ID is:" + Thread.currentThread().getId());
            }
        };
    }

    /*
     * Called in onLocationResult each time a new location is read
     * We add the new location to the current Exercise's location list
     */
    private void addNewLocationToExercise(LocationResult locationResult) {
        ArrayList<LatLng> oldLocationList = mCurrExercise.getLocationList();
        double latitude = locationResult.getLastLocation().getLatitude();
        double longitude = locationResult.getLastLocation().getLongitude();
        LatLng newLatLng = new LatLng(latitude, longitude);

        ArrayList<LatLng> newLocationList;
        if (oldLocationList == null) {
            // very first location received
            newLocationList = new ArrayList<>();
            newLocationList.add(newLatLng);
            mCurrExercise.setLocationList(newLocationList);
        } else {
            // multiple locations obtained add on to the end
            newLocationList = oldLocationList;
            newLocationList.add(newLatLng);
            mCurrExercise.setLocationList(newLocationList);
        }
    }

}