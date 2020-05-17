package com.example.sunshine.myruns4.services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.sunshine.myruns4.constants.MyConstants;
import com.example.sunshine.myruns4.models.ExerciseEntry;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class LocationIntentService extends IntentService {
    private static final long UPDATE_INTERVAL = 10000;
    private static final long FAST_INTERVAL = 1000;

    private static final String LOCATION_TRACKING = "Location Tracking"; //TODO
    public static final String BROADCAST_LOCATION = "BroadCast Location"; //TODO

    private LocationCallback mLocationCallback;


    private static final String TAG = LocationIntentService.class.getName();
    private ExerciseEntry mCurrExercise;
    private BroadcastReceiver mActivityDetectionBroadcastReceiver;

    public LocationIntentService() {
        super("LocationIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        // We register a listener here because the exercise parcelabe needs to know activity detection
//       mActivityDetectionBroadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (intent.getAction().equals(ActivityIntentService.getActivityRecognition())) {
//                    ExerciseEntry entry = intent.getParcelableExtra(MyConstants.CURRENT_EXERCISE);
//                    mCurrExercise.setActivityType(entry.getActivityType());
//                    Log.d(TAG, "onReceive(): MapActivity: Activity Recognition Thread ID is:" + Thread.currentThread().getId() + " " + entry.getActivityType());
//
//                }
//
//            }
//        };
//        LocalBroadcastManager.getInstance(this).registerReceiver(mActivityDetectionBroadcastReceiver,
//                new IntentFilter(ActivityIntentService.getActivityRecognition())); // ABUJA

        initLocationCallback();
        startLocationUpdates();
    }

    /*
     * Provides static method for starting this Intent service
     * to perform action LocationTracking with the given parameters.
     * Caller must provide a valid currentExercise
     */
    public static void startLocationTracking(Context context) {
        Intent intent = new Intent(context, LocationIntentService.class);
        intent.setAction(LOCATION_TRACKING);
//        intent.putExtra(MyConstants.CURRENT_EXERCISE, currentExercise);
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
               // addNewLocationToExercise(locationResult); // ABUJA
                // addMetricsToExercise(locationResult, intent); // ABUJA
//                intent.putExtra(MyConstants.CURRENT_EXERCISE, mCurrExercise); // ABUJA
                intent.putExtra(MyConstants.LOCATION_LIST, locationResult);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                Log.d(TAG, "onLocationAvailability(): Thread ID is:" + Thread.currentThread().getId());
            }
        };
    }

//    /*
//     * Helper method to add Metrics like calories, Avg_speed, distance to the current Exercise
//     * Calories is just a rough estimate we multiply distance in meters by 0.06.
//     * To get climb, we subtract start altitude from curr altitude (location.getAltitude)
//     * Average speed is distance travelled divided by duration spent traveling.
//     * We do all the calculations in kilometers and change units when rendering
//     */
//    private void addMetricsToExercise(LocationResult locationResult, Intent intent) {
//        if ( mCurrExercise == null) return;
//
//        DecimalFormat df = new DecimalFormat("####0.00");
//
//        double duration = Float.parseFloat(mCurrExercise.getDuration().substring(0, mCurrExercise.getDuration().indexOf(" ")));
//        double avgSpeed = locationResult.getLastLocation().getSpeed() / (duration == 0 ? 1 : duration);
//        double climb = (locationResult.getLastLocation().getAltitude() - mCurrExercise.getStartAltitude()) / 1000;
//        // we need to convert to km/s since getSpeed returns speed in m/s
//        avgSpeed = avgSpeed / 1000;
//
//        double distance = avgSpeed * duration;
//        String calorie = df.format(MyConstants.CALORIE_CONSTANT * distance) + " cals";
//
//        String sDistance  = df.format(distance) + " kms";
//        String sAvgSpeed = df.format(avgSpeed) + " km/s";
//        String sClimb = df.format(climb) + " kms";
//
//        mCurrExercise.setDistance(sDistance); // ABUJA
//        mCurrExercise.setAvgSpeed(sAvgSpeed); // ABUJA
//        mCurrExercise.setCalorie(calorie); // ABUJA
//        mCurrExercise.setClimb(sClimb); // ABUJA
//
//        intent.putExtra(MyConstants.DISTANCE_DETECTED, sDistance);
//        intent.putExtra(MyConstants.AVG_SPEED_DETECTED, sAvgSpeed);
//        intent.putExtra(MyConstants.CALORIES_DETECTED, sClimb);
//        intent.putExtra(MyConstants.CLIMB_DETECTED, sClimb);
//
//    }
//
//    /*
//     * Called in onLocationResult each time a new location is read
//     * We add the new location to the current Exercise's location list
//     */
//    private void addNewLocationToExercise(LocationResult locationResult) {
//        if (mCurrExercise == null){
//            return;
//        }
//        ArrayList<LatLng> oldLocationList = mCurrExercise.getLocationList();
//        double latitude = locationResult.getLastLocation().getLatitude();
//        double longitude = locationResult.getLastLocation().getLongitude();
//        LatLng newLatLng = new LatLng(latitude, longitude);
//
//        ArrayList<LatLng> newLocationList;
//        if (oldLocationList == null) {
//            // very first location received
//            newLocationList = new ArrayList<>();
//            newLocationList.add(newLatLng);
//            mCurrExercise.setLocationList(newLocationList);
//            mCurrExercise.setStartAltitude(locationResult.getLastLocation().getAltitude() / 1000);
//        } else {
//            // multiple locations obtained add on to the end
//            newLocationList = oldLocationList;
//            newLocationList.add(newLatLng);
//            mCurrExercise.setLocationList(newLocationList);
//        }
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mActivityDetectionBroadcastReceiver );
//    }
//
//    /*
//     * Captures Duration of exercise entry. We subtract the exercise's
//     * time stamp from the current time and setDuration() on the entry
//     */
//    private void captureDuration() {
//        if (mCurrExercise != null) {
//
//            LocalTime startTime = LocalTime.parse(mCurrExercise.getTime()); //TODO
//            LocalTime now = LocalTime.now();
//
//            long secs = now.getSecond() - startTime.getSecond();
//            long hours = now.getHour() - startTime.getHour();
//            long mins = now.getMinute() - startTime.getMinute();
//
//            // convert everything else to mins
//            mins = mins + hours * 60 + (secs * (1 / 60));
//
//            String duration = mins + " mins";
//            Log.d(TAG, "captureDuration() " + duration);
//            mCurrExercise.setDuration(duration);
//        }
//    }
}