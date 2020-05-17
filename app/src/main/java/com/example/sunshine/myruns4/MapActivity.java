package com.example.sunshine.myruns4;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.example.sunshine.myruns4.constants.MyConstants;
import com.example.sunshine.myruns4.database.DeleteExerciseTask;
import com.example.sunshine.myruns4.database.ExerciseDataSource;
import com.example.sunshine.myruns4.database.ExerciseInsertTask;
import com.example.sunshine.myruns4.database.ExerciseListLoader;
import com.example.sunshine.myruns4.fragments.HistoryFragment;
import com.example.sunshine.myruns4.fragments.StartFragment;
import com.example.sunshine.myruns4.models.ExerciseEntry;
import com.example.sunshine.myruns4.services.ActivityIntentService;
import com.example.sunshine.myruns4.services.LocationIntentService;
import com.example.sunshine.myruns4.services.TrackingService;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<ArrayList<ExerciseEntry>> {

    private static final String TAG = MapActivity.class.getName();
    private GoogleMap mMap;
    private int PERMISSION_REQUEST_CODE = 1;
    private Intent serviceIntent;
    private ExerciseDataSource mDataSource;
    private Marker mFirstMarker, mLastMarker;
    private DeleteExerciseTask mDeleteTask;
    private ExerciseEntry mExerciseEntry;
    private String mEntryPoint;

    private TextView mActivityView;
    private TextView mAvgSpeedView;
    private TextView mClimbView;
    private TextView mCalorieView;
    private TextView mDistanceView;
    private String mActivityType;
    private String mInputType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mActivityView = findViewById(R.id.activity);
        mAvgSpeedView = findViewById(R.id.avg_speed);
        mClimbView = findViewById(R.id.climb);
        mCalorieView = findViewById(R.id.calorie);
        mDistanceView = findViewById(R.id.distance);



        // set up action bar
        setUpActionBar();

        // set up map fragment view and obtain the SupportMapFragment
        // get notified when the map is ready to be used.
        setUpMapFragment();

        // initialise reference to database
        mDataSource = new ExerciseDataSource(this);
        mDataSource.open();

        // Initialise DeleteTask
        mDeleteTask = new DeleteExerciseTask(this);

        // Figure out calling Activity
        Intent entry = getIntent();
        if (entry != null) {
            mEntryPoint = entry.getStringExtra(MyConstants.SOURCE);
            mActivityType = entry.getStringExtra(MyConstants.ACTIVITY_TYPE);
            mInputType = entry.getStringExtra(MyConstants.INPUT_TYPE);

            if (mEntryPoint != null) {
                if (savedInstanceState != null){
                    mExerciseEntry = savedInstanceState.getParcelable(MyConstants.CURRENT_EXERCISE);
                }else{
                    initExerciseEntry(mActivityType, mInputType);
                }
                switch (mEntryPoint) {
                    case HistoryFragment.FRAGMENT_NAME:
                        // start AsyncTaskLoader to fetch the data from the DB
                        LoaderManager mLoader = LoaderManager.getInstance(this);
                        mLoader.initLoader(MyConstants.FETCH_SINGLE_EXERCISE_ID, null, this).forceLoad();
                        break;
                    case StartFragment.FRAGMENT_NAME:
                        // Register Broadcast Receivers for Location Tracking and Activity Recognition
                        registerBroadcastReceivers(entry);
                        startTrackingService();
                        break;
                }
            }
        }

        Log.d(TAG, "onCreate()");
    }

    /* handle save instance state */

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(MyConstants.CURRENT_EXERCISE, mExerciseEntry);
        super.onSaveInstanceState(outState);
    }

    /*
     * Register BroadCastReceivers for Location and Activity Recognition Requests
     */
    private void registerBroadcastReceivers(Intent entry) {
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationBroadcastReceiver,
                new IntentFilter(LocationIntentService.BROADCAST_LOCATION));

        // register an activity detection listener only if we're in Automatic Mode
        String inputMode = entry.getStringExtra(MyConstants.INPUT_TYPE);
        if (inputMode.equals(MyConstants.INPUT_AUTOMATIC)) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mActivityDetectionBroadcastReceiver,
                    new IntentFilter(ActivityIntentService.getActivityRecognition()));
            Log.d(TAG, "registerBroadcastReceivers()");
        }
    }

    /*
     * Sets up Action Bar with back button and title
     */
    private void setUpActionBar() {
        // Set up action bar with back button
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Map Activity");
    }

    /*
     * Set up MapFragment through Async call to get Maps from Google
     */
    private void setUpMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    /*
     * Sets the map Instance and Map type
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        Log.d(TAG, "onMapReady()");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (mEntryPoint.equals(StartFragment.FRAGMENT_NAME)) {
                startTrackingService();
            }
        } else {
            finish();
        }
    }

    /*
     * Check Permissions and if granted begin tracking service else request permission
     * Begins Tracking Service which handles different threads for Activity Recognition
     * and Location Tracking. We pass in as the intent the activity type and the input type
     */
    private void startTrackingService() {
        if (checkPermission()) {

            String inputType = getIntent().getStringExtra(MyConstants.INPUT_TYPE);
            String activityType = getIntent().getStringExtra(MyConstants.ACTIVITY_TYPE);

            // if input Type is automatic, let's detect the Activity Type
            if (inputType.equals(MyConstants.INPUT_AUTOMATIC)) {
               activityType = "";
            }

            serviceIntent = new Intent(this, TrackingService.class);
            serviceIntent.putExtra(MyConstants.INPUT_TYPE, inputType);
            serviceIntent.putExtra(MyConstants.ACTIVITY_TYPE, activityType);

            startForegroundService(serviceIntent);
        } else {
            // Request Permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }
    }

    /*
     * Creates a BroadCast Receiver for an ActivityDetection BroadCast
     */
    BroadcastReceiver mLocationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(LocationIntentService.BROADCAST_LOCATION)) {
                Log.d(TrackingService.TAG, "MapActivity: onReceive(): MapActivity: Location Recognition Thread ID is:" + Thread.currentThread().getId());
                // mExerciseEntry = intent.getParcelableExtra(MyConstants.CURRENT_EXERCISE); // ABUJA
                addNewLocationToExercise(intent);
                addMetricsToExercise(intent);
                Log.d(TAG, "MapActivity: onReceive(): MapActivity: Location Recognition Exercise is: " + mExerciseEntry.toString());
                updateMapDisplay(mExerciseEntry);
                Toast.makeText(MapActivity.this, "Location Received", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /*
     * Draws the LatLongs in the exercise Entry on the Map and updates the text views
     */
    private void updateMapDisplay(ExerciseEntry exerciseEntry) {
        if (exerciseEntry != null && !exerciseEntry.getLocationList().isEmpty()) {
            PolylineOptions polylineOptions = new PolylineOptions();
            for (LatLng latLng : exerciseEntry.getLocationList()) {
                if (latLng != null){
                    polylineOptions.add(latLng); // sometimes throws error may be earlier test cases
                }
            }

            // add Polyline to Map and style it
            if (mMap != null ){
                if (polylineOptions != null){
                    Polyline polyline = mMap.addPolyline(polylineOptions);
                    stylePolyline(polyline);
                }

                // set start and end Markers
                setStartEndMarkers(exerciseEntry);

                // Focus on the last location commented for debugging
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(exerciseEntry.getLocationList()
                        .get(exerciseEntry.getLocationList().size() - 1), 17)); // exceptional null case with latlngs

                Log.d(TAG, "updateMapDisplay(): Empty LocationList ");
            }


            // set TextViews depending on the user's unit preferences
            applyUnitPreferences(exerciseEntry);
            }

    }

    /*
     * Called within updateMapDisplay to set Text Views with appropriate units
     */
    private void applyUnitPreferences(ExerciseEntry mExerciseEntry) {
        if (mExerciseEntry == null) {
            return;
        }
        String mDistanceUnitPrefs = PreferenceManager
                .getDefaultSharedPreferences(this).getString("unit_preference", "");

        Boolean inMiles = mDistanceUnitPrefs.equals(MyConstants.IMPERIAL_MILES);

        String currentDistance = mExerciseEntry.getDistance();
        String currentClimb = mExerciseEntry.getClimb();
        String currentCalorie = mExerciseEntry.getCalorie();
        String currentActivity = mExerciseEntry.getActivityType();
        String currentAvgSpeed = mExerciseEntry.getAvgSpeed();


        if (inMiles){
            // distance conversion
            if (currentDistance != null && currentDistance.contains("kms")) {

                currentDistance = currentDistance.replace(" kms", "");
                DecimalFormat df = new DecimalFormat("####0.00");
                currentDistance = df.format(Double.parseDouble(currentDistance) / MyConstants.MILE_CONVERSION_RATE);

                // change calorie calculation
                currentCalorie = df.format(MyConstants.CALORIE_CONSTANT * Double.parseDouble(currentDistance)) + " cals";

                // set distance
                currentDistance = currentDistance + " miles";
            }

            // speed conversion
            if (currentAvgSpeed != null && currentAvgSpeed.contains("km/s")){
                currentAvgSpeed = currentAvgSpeed.replace(" km/s", "");
                DecimalFormat df = new DecimalFormat("####0.00");
                currentAvgSpeed= df.format(Double.parseDouble(currentAvgSpeed) * (1 / MyConstants.MILE_CONVERSION_RATE) );
                currentAvgSpeed = currentAvgSpeed + " mi/s";
            }

            // climb conversion
            if (currentClimb != null && currentClimb.contains("kms")){
                currentClimb = currentClimb.replace(" kms", "");
                DecimalFormat df = new DecimalFormat("####0.00");
                currentClimb = df.format(Double.parseDouble(currentClimb) / MyConstants.MILE_CONVERSION_RATE);

                currentClimb = currentClimb +" mi";
            }

        }
        mDistanceView.setText(MyConstants.Distance + currentDistance);
        mCalorieView.setText(MyConstants.Calorie + currentCalorie);
        mClimbView.setText(MyConstants.Climb + currentClimb);
        mAvgSpeedView.setText(MyConstants.Avg_Speed + currentAvgSpeed);
        mActivityView.setText(MyConstants.Activity + currentActivity);

    }

    /*
     * Helper method to render start and end Markers on PolyLine
     */
    private void setStartEndMarkers(ExerciseEntry exerciseEntry) {
        if (exerciseEntry != null) {
            ArrayList<LatLng> locationList = exerciseEntry.getLocationList();
            if (locationList != null && !locationList.isEmpty()) {
                LatLng firstPosition = locationList.get(0);
                LatLng lastPosition = locationList.get(locationList.size() - 1);

                if (firstPosition == null || lastPosition == null){
                    return;
                }
                if (mFirstMarker == null) {
                    mFirstMarker = mMap.addMarker(new MarkerOptions()
                            .position(firstPosition)
                            .icon(BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_RED)).title("Start Position"));
                }

                // Remove old Last location if new Last location is obtained
                if (mLastMarker != null) {
                    mLastMarker.setPosition(lastPosition);
                } else {
                    mLastMarker = mMap.addMarker(new MarkerOptions()
                            .position(lastPosition)
                            .icon(BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_GREEN)).title("Recent Position"));
                }
            }
        }
    }

    /*
     * Set Custom styling for polyline of Map
     */
    private void stylePolyline(Polyline polyline) {
        if (polyline == null) return;
        polyline.setStartCap(new RoundCap());
        polyline.setEndCap(new RoundCap());
        polyline.setWidth(MyConstants.POLYLINE_STROKE_WIDTH_PX);
        polyline.setColor(MyConstants.POLYLINE_COLOR_PURPLE_ARGB);
        polyline.setJointType(JointType.ROUND);
    }

    /*
     * Creates a BroadCast Receiver for an ActivityDetection BroadCast
     */
    BroadcastReceiver mActivityDetectionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ActivityIntentService.getActivityRecognition())) {
                String detectedActivity =  intent.getStringExtra(MyConstants.DETECTED_ACTIVITY);
                // mExerciseEntry = intent.getParcelableExtra(MyConstants.CURRENT_EXERCISE); // ABUJA

                if (mExerciseEntry != null) {
                    mExerciseEntry.setActivityType(detectedActivity);
                    updateMapDisplay(mExerciseEntry);
                    Log.d(TAG, "onReceive(): MapActivity: Activity Recognition Thread ID is:" + Thread.currentThread().getId() + " " + mExerciseEntry.getActivityType());
                    Log.d(TAG, "MapActivity: onReceive(): MapActivity: Activity Recognition Exercise is: " + mExerciseEntry.toString());
                    Toast.makeText(MapActivity.this, "Activity Type Received", Toast.LENGTH_SHORT).show();
                }
            }

        }
    };

    //******** Check run time permission for locationManager. This is for v23+  ********
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED;
    }


    /*
     * Set up actionBar depending on the source of the intent
     * Sets Save option if source is StartFragment
     * Sets Delete option if source is HistoryFragment
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Intent entryPoint = getIntent();

        if (entryPoint != null) {
            String sourceName = entryPoint.getStringExtra(MyConstants.SOURCE);
            if (sourceName != null && sourceName.equals(StartFragment.FRAGMENT_NAME)) {
                getMenuInflater().inflate(R.menu.save_activity, menu);
            } else if (sourceName != null && entryPoint.getStringExtra(MyConstants.SOURCE)
                    .equals(HistoryFragment.FRAGMENT_NAME)) {
                getMenuInflater().inflate(R.menu.delete_activity, menu);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    /*
     * Handles the case when back button is clicked
     * Handles the case when save option is selected
     * Handles the case when delete option is selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(MapActivity.this, MainActivity.class));
                closeAllServices();
                finish();
                return true;
            case R.id.save_activity_entry:
                // Eventually calls the save method on the database helper
                handleSave();
                return true;
            case R.id.delete_activity_entry:
                // calls the delete method on the database helper
                handleDelete();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Saves an exercise Entry to the DB, closes any active services,
     * and starts MainActivity
     */
    private void handleSave() {
        captureDuration();
        ExerciseInsertTask task = new ExerciseInsertTask(this, mDataSource);
        task.execute(mExerciseEntry);
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        closeAllServices();
        startActivity(new Intent(MapActivity.this, MainActivity.class));
        finish();
    }

    /*
     * Handles Delete action of an exerciseEntry
     * We fire off the Delete AsyncTask, toast Deleted
     * close all active services and start the MainActivity
     */
    private void handleDelete() {
        Intent intent = getIntent();
        if (intent != null) {
            Long id = intent.getLongExtra(MyConstants.EXERCISE_ENTRY_ID, -1);
            if (id > -1) {
                mDeleteTask.execute(id);
                startActivity(new Intent(MapActivity.this, MainActivity.class));
            } else {
                Log.d(TAG, "Invalid Exercise ID - Non-null index");
                Toast.makeText(this, "Invalid Exercise ID", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "Invalid Exercise ID");
        }
        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
        closeAllServices();
        startActivity(new Intent(MapActivity.this, MainActivity.class));
        finish();
    }

    /*
     * Captures Duration of exercise entry. We subtract the exercise's
     * time stamp from the current time and setDuration() on the entry
     */
    private void captureDuration() {
        if (mExerciseEntry != null) {

            LocalTime startTime = LocalTime.parse(mExerciseEntry.getTime()); //TODO
            LocalTime now = LocalTime.now();

            long secs = now.getSecond() - startTime.getSecond();
            long hours = now.getHour() - startTime.getHour();
            long mins = now.getMinute() - startTime.getMinute();

            // convert everything else to mins
            mins = mins + hours * 60 + (secs * (1 / 60));

            String duration = mins + " mins";
            Log.d(TAG, "captureDuration() " + duration);
            mExerciseEntry.setDuration(duration);
        }
    }

    /*
     * Helper method to stop all services
     */
    private void closeAllServices() {
        stopService(new Intent(this, TrackingService.class));
        stopService(new Intent(this, LocationIntentService.class));
        stopService(new Intent(this, ActivityIntentService.class));
    }

    /*
     * Called when user hits back button
     */
    @Override
    public void onBackPressed() {
        startActivity(new Intent(MapActivity.this, MainActivity.class));
        closeAllServices();
        finish();
        super.onBackPressed();
    }

    /*
     * Closes database when activity is being destroyed
     * Unregisters the Location callback listener
     */
    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mActivityDetectionBroadcastReceiver);
        mDataSource.close();
        super.onDestroy();
    }


    /*
     * If Intent source is History Fragment we create an AsyncTaskLoader
     * used for Fetching single a Exercise Entry
     */
    @NonNull
    @Override
    public Loader<ArrayList<ExerciseEntry>> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == MyConstants.FETCH_SINGLE_EXERCISE_ID) {
            Intent intent = getIntent();
            if (intent != null) {
                Long exerciseID = intent.getLongExtra(MyConstants.EXERCISE_ENTRY_ID, -1);
                if (exerciseID > -1) {
                    return new ExerciseListLoader(MapActivity.this, exerciseID);
                } else {
                    Log.d(TAG, "Invalid Exercise ID - Non-null intent");
                }
            }
            Log.d(TAG, "Null Intent");
        }
        return null;
    }

    /*
     * Called when AsyncTaskLoader for Fetching Single Entry is finished
     * We update the adapter with contents of the fetched Entry
     */
    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<ExerciseEntry>> loader, ArrayList<ExerciseEntry> data) {
        Log.d(TAG, "onLoadFinished(): Thread ID " + Thread.currentThread().getId());
        if (loader.getId() == MyConstants.FETCH_SINGLE_EXERCISE_ID) {
            if (data.size() > 0) {
                mExerciseEntry = data.get(0);
                updateMapDisplay(mExerciseEntry);
            }
        }
    }

    /*
     * required method. No implementation needed
     */
    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<ExerciseEntry>> loader) {
    }


    /* *********************** ADDED CODE *********************/
    /*
     * Initialises an exercise entry with Activity and InputType
     * Also date and Time since these are required fields in the DB schema
     */
    private void initExerciseEntry(String activityType, String inputType) {
        mExerciseEntry = new ExerciseEntry();
        mExerciseEntry.setActivityType(activityType);
        mExerciseEntry.setInputType(inputType);
        mExerciseEntry.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))); // Record time to fine seconds
        mExerciseEntry.setDate(java.time.LocalDate.now().toString());

        String mDistanceUnitPrefs =  PreferenceManager
                .getDefaultSharedPreferences(this).getString("unit_preference", "");


        mExerciseEntry.setDistance(mDistanceUnitPrefs.equals(MyConstants.IMPERIAL_MILES) ? "0 miles" : "0 kms");
        mExerciseEntry.setCalorie("0 cal");
        mExerciseEntry.setClimb(mDistanceUnitPrefs.equals(MyConstants.IMPERIAL_MILES) ? "0 mi" : "0 kms");
        mExerciseEntry.setAvgSpeed(mDistanceUnitPrefs.equals(MyConstants.IMPERIAL_MILES) ? "0 mi/s" : "0 kms/s");
        mExerciseEntry.setDuration("0 mins"); // start from 0
    }

    private void addMetricsToExercise(Intent intent) {
        if (mExerciseEntry == null || intent == null) return;
        LocationResult locationResult = intent.getParcelableExtra(MyConstants.LOCATION_LIST);
        DecimalFormat df = new DecimalFormat("####0.00");

        double duration = Float.parseFloat(mExerciseEntry.getDuration().substring(0, mExerciseEntry.getDuration().indexOf(" ")));
        double avgSpeed = locationResult.getLastLocation().getSpeed() / (duration == 0 ? 1 : duration);
        double climb = (locationResult.getLastLocation().getAltitude() - mExerciseEntry.getStartAltitude()) / 1000;
        // we need to convert to km/s since getSpeed returns speed in m/s
        avgSpeed = avgSpeed / 1000;

        double distance = avgSpeed * duration;
        String calorie = df.format(MyConstants.CALORIE_CONSTANT * distance) + " cals";

        String sDistance  = df.format(distance) + " kms";
        String sAvgSpeed = df.format(avgSpeed) + " km/s";
        String sClimb = df.format(climb) + " kms";

        mExerciseEntry.setDistance(sDistance);
        mExerciseEntry.setAvgSpeed(sAvgSpeed);
        mExerciseEntry.setCalorie(calorie);
        mExerciseEntry.setClimb(sClimb);
    }

    /*
     * Called in onLocationResult each time a new location is read
     * We add the new location to the current Exercise's location list
     */
    private void addNewLocationToExercise(Intent intent) {
        if (mExerciseEntry == null || intent  == null){
            return;
        }

        LocationResult locationResult = intent.getParcelableExtra(MyConstants.LOCATION_LIST);

        ArrayList<LatLng> oldLocationList = mExerciseEntry.getLocationList();
        double latitude = locationResult.getLastLocation().getLatitude();
        double longitude = locationResult.getLastLocation().getLongitude();
        LatLng newLatLng = new LatLng(latitude, longitude);

        ArrayList<LatLng> newLocationList;
        if (oldLocationList == null) {
            // very first location received
            newLocationList = new ArrayList<>();
            newLocationList.add(newLatLng);
            mExerciseEntry.setLocationList(newLocationList);
            mExerciseEntry.setStartAltitude(locationResult.getLastLocation().getAltitude() / 1000);
        } else {
            // multiple locations obtained add on to the end
            newLocationList = oldLocationList;
            newLocationList.add(newLatLng);
            mExerciseEntry.setLocationList(newLocationList);
        }
    }

}

