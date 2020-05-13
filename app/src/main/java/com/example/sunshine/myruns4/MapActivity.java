package com.example.sunshine.myruns4;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.sunshine.myruns4.constants.MyConstants;
import com.example.sunshine.myruns4.database.ExerciseDataSource;
import com.example.sunshine.myruns4.database.ExerciseInsertTask;
import com.example.sunshine.myruns4.fragments.HistoryFragment;
import com.example.sunshine.myruns4.fragments.StartFragment;
import com.example.sunshine.myruns4.models.ExerciseEntry;
import com.example.sunshine.myruns4.services.ActivityIntentService;
import com.example.sunshine.myruns4.services.LocationIntentService;
import com.example.sunshine.myruns4.services.TrackingService;
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

import java.util.ArrayList;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String SOURCE = "Source";
    private static final String TAG = MapActivity.class.getName();
    private GoogleMap mMap;
    private int PERMISSION_REQUEST_CODE = 1;
    private Marker mMaker;
    private Intent serviceIntent;
    private ExerciseDataSource mDataSource;
    private Marker mFirstMarker, mLastMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // set up action bar
        setUpActionBar();

        // set up map fragment view and obtain the SupportMapFragment
        // get notified when the map is ready to be used.
        setUpMapFragment();

        // initialise reference to database
        mDataSource = new ExerciseDataSource(this);
        mDataSource.open();

        // Initialise DeleteTask
        // mDeleteTask = new DeleteExerciseTask();

        // Register Broadcast Receivers for Location Tracking and Activity Recognition
        registerBroadcastReceivers();
    }


    private void registerBroadcastReceivers() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationBroadcastReceiver,
                new IntentFilter(LocationIntentService.BROADCAST_LOCATION));

        LocalBroadcastManager.getInstance(this).registerReceiver(mActivityDetectionBroadcastReceiver,
                new IntentFilter(LocationIntentService.BROADCAST_ACTIVITY));
    }

    private void setUpActionBar() {
        // Set up action bar with back button
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Map Activity");
    }

    private void setUpMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /*
     * Check Permissions and if granted begin tracking service
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); // use one Map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);


        if (!checkPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        } else {
            startTrackingService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startTrackingService();
        } else {
            finish();
        }
    }

    /*
     * Begins Tracking Service which handles different threads for Activity Recognition
     * and Location Tracking. We pass in as the intent the activity type and the input type
     */
    private void startTrackingService() {
        String activityType = getIntent().getStringExtra(MyConstants.ACTIVITY_TYPE);
        String inputType = getIntent().getStringExtra(MyConstants.INPUT_TYPE);

        serviceIntent = new Intent(this, TrackingService.class);
        serviceIntent.putExtra(MyConstants.INPUT_TYPE, inputType);
        serviceIntent.putExtra(MyConstants.ACTIVITY_TYPE, activityType);

        startForegroundService(serviceIntent);
    }

    /*
     * Creates a BroadCast Receiver for an ActivityDetection BroadCast
     */
    BroadcastReceiver mLocationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(LocationIntentService.BROADCAST_LOCATION)) {
                Log.d(TrackingService.TAG, "MapActivity: onReceive(): Thread ID is:" + Thread.currentThread().getId());
                Location location = intent.getParcelableExtra(MyConstants.LAST_LOCATION);
                ExerciseEntry exerciseEntry = intent.getParcelableExtra(MyConstants.CURRENT_EXERCISE);
                updateMapDisplay(exerciseEntry);
                Toast.makeText(MapActivity.this, "Location Received", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /*
     * Draws the LatLongs in the exercise Entry on the Map and updates the text view
     */
    private void updateMapDisplay(ExerciseEntry exerciseEntry) {
        PolylineOptions polylineOptions = new PolylineOptions();
        for (LatLng latLng : exerciseEntry.getLocationList()) {
            polylineOptions.add(latLng);
        }

        // add Polyline to Map and style it
        Polyline polyline = mMap.addPolyline(polylineOptions);
        stylePolyline(polyline);

        // set start and end Markers
        setStartEndMarkers(exerciseEntry);

        // Focus on the last location commented for debugging
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(exerciseEntry.getLocationList()
                .get(exerciseEntry.getLocationList().size() - 1), 17));
    }

    /*
     * Helper to render start and end Markers on PolyLine
     */
    private void setStartEndMarkers(ExerciseEntry exerciseEntry) {
        if (exerciseEntry != null){
            ArrayList<LatLng> locationList = exerciseEntry.getLocationList();
            if (locationList != null) {
                LatLng firstPosition = locationList.get(0);
                LatLng lastPosition = locationList.get(locationList.size() - 1);

                if (mFirstMarker == null) {
                    mFirstMarker = mMap.addMarker(new MarkerOptions().position(firstPosition).icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_RED)).title("Start Position"));
                }

                // Remove old Last location if new Last location is obtained
                if (mLastMarker != null){
                    mLastMarker.setPosition(lastPosition);
                }else{
                    mLastMarker = mMap.addMarker(new MarkerOptions().position(lastPosition).icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_GREEN)).title("Recent Position"));
                }


            }


        }

    }

    /*
     * Set Custom styling for polyline of Map
     */
    private void stylePolyline(Polyline polyline) {
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
            if (intent.getAction().equals(LocationIntentService.BROADCAST_ACTIVITY)) {
                Log.d(TrackingService.TAG, "MapActivity: onReceive(): Thread ID is:" + Thread.currentThread().getId());
            }
        }
    };

    //******** Check run time permission for locationManager. This is for v23+  ********
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
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
            String sourceName = entryPoint.getStringExtra(SOURCE);
            if (sourceName != null && sourceName.equals(StartFragment.FRAGMENT_NAME)) {
                getMenuInflater().inflate(R.menu.save_activity, menu);
            } else if (sourceName != null && entryPoint.getStringExtra(SOURCE).equals(HistoryFragment.FRAGMENT_NAME)) {
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
                Log.d(TAG, "home button");
                startActivity(new Intent(MapActivity.this, MainActivity.class));
                closeAllServices();
                finish();
                return true;
            case R.id.save_activity_entry:
                // calls the save method on the database helper
//                ExerciseEntry newEntry = captureNewEntry();
//                ExerciseInsertTask task = new ExerciseInsertTask(this, mDataSource);
//                task.execute(newEntry);
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                closeAllServices();
                startActivity(new Intent(MapActivity.this, MainActivity.class));
                finish();
                return true;
            case R.id.delete_activity_entry:
                // calls the delete method on the database helper
//                Intent intent = getIntent();
////                if (intent != null) {
////                    Long id = intent.getLongExtra(EXERCISE_ENTRY_ID, -1);
////                    if (id > -1) {
////                        mDeleteTask.execute(id);
////                        startActivity(new Intent(MapActivity.this, MainActivity.class));
////                    } else {
////                        Log.d(TAG, "Invalid Exercise ID - Non-null index");
////                        Toast.makeText(this, "Invalid Exercise ID", Toast.LENGTH_SHORT).show();
////                    }
////                } else {
////                    Log.d(TAG, "Invalid Exercise ID");
////                }
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                closeAllServices();
                startActivity(new Intent(MapActivity.this, MainActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
     * Closes database when activity is being destroyed
     * Unregisters the Location callback listener
     */
    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationBroadcastReceiver);
        mDataSource.close();
        super.onDestroy();
    }
}

