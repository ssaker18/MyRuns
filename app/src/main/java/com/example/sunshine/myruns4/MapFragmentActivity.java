package com.example.sunshine.myruns4;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.example.sunshine.myruns4.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragmentActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private int PERMISSION_REQUEST_CODE = 1;
    private Marker mMaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in the Dartmouth Green and move the camera
        LatLng dartmouth = new LatLng(43.703354, -72.288566);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMaker = mMap.addMarker(new MarkerOptions().position(dartmouth).title("The Green sez hiya Androidistas!"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dartmouth, 17));

        if (!checkPermission())
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        else
            startTrackingService();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startTrackingService();
        } else {
            finish();
        }
    }

    private void startTrackingService() {
    }

    //******** Check run time permission for locationManager. This is for v23+  ********
    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }
    //****** Check run time permission ************
}
