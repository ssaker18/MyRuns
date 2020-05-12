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

import com.example.sunshine.myruns4.database.ExerciseDataSource;
import com.example.sunshine.myruns4.database.ExerciseInsertTask;
import com.example.sunshine.myruns4.fragments.HistoryFragment;
import com.example.sunshine.myruns4.fragments.StartFragment;
import com.example.sunshine.myruns4.models.ExerciseEntry;
import com.example.sunshine.myruns4.services.TrackingService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String SOURCE = "Source";
    private static final String TAG = MapActivity.class.getName();
    private GoogleMap mMap;
    private int PERMISSION_REQUEST_CODE = 1;
    private Marker mMaker;
    private Intent serviceIntent;
    private ExerciseDataSource mDataSource;


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
                new IntentFilter(TrackingService.BROADCAST_LOCATION));

        LocalBroadcastManager.getInstance(this).registerReceiver(mActivityDetectionBroadcastReceiver,
                new IntentFilter(TrackingService.BROADCAST_ACTIVITY));
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
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        if (!checkPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }else {
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
     * and Location Tracking
     */
    private void startTrackingService() {
        serviceIntent = new Intent(this, TrackingService.class);
        startForegroundService(serviceIntent);
    }

    /*
     * Creates a BroadCast Receiver for an ActivityDetection BroadCast
     */
     BroadcastReceiver mLocationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TrackingService.BROADCAST_LOCATION)) {
                Log.d(TrackingService.TAG, "MapActivity: onReceive(): Thread ID is:" + Thread.currentThread().getId());
                Location location = intent.getParcelableExtra("location");
                LatLng iAmHere = new LatLng(location.getLatitude(), location.getLongitude());
                mMaker = mMap.addMarker(new MarkerOptions().position(iAmHere).title("I am home"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(iAmHere, 17));
            }
        }
    };

    /*
     * Creates a BroadCast Receiver for an ActivityDetection BroadCast
     */
    private BroadcastReceiver mActivityDetectionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TrackingService.BROADCAST_ACTIVITY)) {
                Log.d(TrackingService.TAG, "MapActivity: onReceive(): Thread ID is:" + Thread.currentThread().getId());
            }
        }
    };

    //******** Check run time permission for locationManager. This is for v23+  ********
    private boolean checkPermission(){
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
            switch (entryPoint.getStringExtra(SOURCE)) {
                case StartFragment.FRAGMENT_NAME:
                    getMenuInflater().inflate(R.menu.save_activity, menu);
                    break;
                case HistoryFragment.FRAGMENT_NAME:
                    getMenuInflater().inflate(R.menu.delete_activity, menu);
                    break;
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
                finish();
                return true;
            case R.id.save_activity_entry:
                // calls the save method on the database helper
//                ExerciseEntry newEntry = captureNewEntry();
//                ExerciseInsertTask task = new ExerciseInsertTask(this, mDataSource);
//                task.execute(newEntry);
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                finish();
                startActivity(new Intent(MapActivity.this, MainActivity.class));
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Closes database when activity is being destroyed
     */
    @Override
    protected void onDestroy() {
        mDataSource.close();
        super.onDestroy();
    }
}

