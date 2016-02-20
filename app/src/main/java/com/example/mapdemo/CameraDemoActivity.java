/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mapdemo;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NONE;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN;

/**
 * This shows how to change the camera position for the map.
 */
public class CameraDemoActivity extends AppCompatActivity implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, OnItemSelectedListener {

    /**
     * The amount by which to scroll the camera. Note that this amount is in raw pixels, not dp
     * (density-independent pixels).
     */
    private static final int SCROLL_BY_PX = 100;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static final CameraPosition BONDI =
            new CameraPosition.Builder().target(new LatLng(-33.891614, 151.276417))
                    .zoom(15.5f)
                    .bearing(300)
                    .tilt(50)
                    .build();

    public static final CameraPosition SYDNEY =
            new CameraPosition.Builder().target(new LatLng(-33.87365, 151.20689))
                    .zoom(15.5f)
                    .bearing(0)
                    .tilt(25)
                    .build();
    public static final CameraPosition INDIA =
            new CameraPosition.Builder().target(new LatLng(23.9129143, 79.7561333))
                    .zoom(4.5f)
                    .bearing(0)
                    .tilt(25)
                    .build();
    public static final CameraPosition LAWRENCE =
            new CameraPosition.Builder().target(new LatLng(38.97355, -95.2859463))
                    .zoom(11.5f)
                    .bearing(0)
                    .tilt(25)
                    .build();
    public static final CameraPosition NEW_YORK =
            new CameraPosition.Builder().target(new LatLng(40.7058254, -73.91808254))
                    .zoom(9.5f)
                    .bearing(0)
                    .tilt(25)
                    .build();
    public static final CameraPosition VEGAS =
            new CameraPosition.Builder().target(new LatLng(36.1251958, -115.3150833))
                    .zoom(11.5f)
                    .bearing(0)
                    .tilt(25)
                    .build();
    public static final CameraPosition NoWhere =
            new CameraPosition.Builder().target(new LatLng(40.7317736, -73.9863048))
                    .zoom(-2.5f)
                    .bearing(0)
                    .tilt(25)
                    .build();
    public String Temp;
    private GoogleMap mMap;

    private CompoundButton mAnimateToggle;

    private CompoundButton mCustomDurationToggle;

    private SeekBar mCustomDurationBar;
    Spinner LocationToVisit;
    private Spinner mLayerSpinner;
    private CheckBox ShowMyLocation;
    private boolean mShowPermissionDeniedDialog = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_demo);

        //Code for adding dropdown "mySpinner"
        LocationToVisit =(Spinner) findViewById(R.id.mySpinner);
        //Initialize the Adapter for the array "visitLocation"
        ArrayAdapter<CharSequence> LocationToVisitAdapter = ArrayAdapter.createFromResource(this,R.array.visitLocation,android.R.layout.simple_spinner_item);

        //Specify the layout
        LocationToVisitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        //After the adapter is created... Applying the adapter to the spinner
        LocationToVisit.setAdapter(LocationToVisitAdapter);


        //Layer Spinner <code>

        mLayerSpinner = (Spinner) findViewById(R.id.layersChoice);
        ArrayAdapter<CharSequence> layer_adapter = ArrayAdapter.createFromResource(
                this, R.array.layers_array, android.R.layout.simple_spinner_item);
        layer_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mLayerSpinner.setAdapter(layer_adapter);
        mLayerSpinner.setOnItemSelectedListener(this);

        //End

        mAnimateToggle = (CompoundButton) findViewById(R.id.animate);
        mCustomDurationToggle = (CompoundButton) findViewById(R.id.duration_toggle);
        mCustomDurationBar = (SeekBar) findViewById(R.id.duration_bar);

        updateEnabledState();

        ShowMyLocation = (CheckBox) findViewById(R.id.my_location_display);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Temp=LocationToVisit.getSelectedItem().toString();

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateEnabledState();
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        updateMapType();
    }
    private void updateMapType() {
        // No toast because this can also be called by the Android framework in onResume() at which
        // point mMap may not be ready yet.
        if (mMap == null) {
            return;
        }
        String layerName = ((String) mLayerSpinner.getSelectedItem());
        if (layerName.equals(getString(R.string.normal))) {
            mMap.setMapType(MAP_TYPE_NORMAL);
        } else if (layerName.equals(getString(R.string.hybrid))) {
            mMap.setMapType(MAP_TYPE_HYBRID);
        } else if (layerName.equals(getString(R.string.satellite))) {
            mMap.setMapType(MAP_TYPE_SATELLITE);
        } else if (layerName.equals(getString(R.string.terrain))) {
            mMap.setMapType(MAP_TYPE_TERRAIN);
        } else if (layerName.equals(getString(R.string.none_map))) {
            mMap.setMapType(MAP_TYPE_NONE);
        } else {
            Log.i("LDA", "Error setting layer with name " + layerName);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing.
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // We will provide our own zoom controls.
        mMap.getUiSettings().setZoomControlsEnabled(false);

        // Show Sydney
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.87365, 151.20689), 10));
        updateMapType();
        updateMyLocation();
        Toast.makeText(this, R.string.OrientationChanged, Toast.LENGTH_SHORT).show();
    }

    public void onMyLocationToggled(View view) {
        updateMyLocation();
    }

    private void updateMyLocation() {
        if (!checkReady()) {
            return;
        }

        if (!ShowMyLocation.isChecked()) {
            mMap.setMyLocationEnabled(false);
            return;
        }

        // Enable the location layer. Request the location permission if needed.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Uncheck the box until the layer has been enabled and request missing permission.
            ShowMyLocation.setChecked(false);
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, false);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, results,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            mMap.setMyLocationEnabled(true);
            ShowMyLocation.setChecked(true);
        } else {
            mShowPermissionDeniedDialog = true;
        }
    }
    /**
     * When the map is not ready the CameraUpdateFactory cannot be used. This should be called on
     * all entry points that call methods on the Google Maps API.
     */
    private boolean checkReady() {
        if (mMap == null) {
            Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    public CameraPosition GetLocation(){
        Temp=LocationToVisit.getSelectedItem().toString();
        switch (Temp){
            case "Bondi" :
                return BONDI;
            case "India" :
                return INDIA;
            case "Lawrence" :
                return LAWRENCE;
            case "New York" :
                return NEW_YORK;
            case "Sydney" :
                return SYDNEY;
            case "Vegas" :
                return VEGAS;
            default:
                return NoWhere;
        }

    }

    /**
     * Called when the Go To selected location when the button is clicked.
     */
    public void onGoToSetLocation(View view) {
        if (!checkReady()) {
            return;
        }

        changeCamera(CameraUpdateFactory.newCameraPosition(GetLocation()));

            Toast.makeText(getBaseContext(), "Traveling to " + Temp, Toast.LENGTH_SHORT)
                    .show();
    }

    /**
     * Called when the Animate To Sydney button is clicked.
     */
    public void onGoToSydney(View view) {
        if (!checkReady()) {
            return;
        }

        changeCamera(CameraUpdateFactory.newCameraPosition(SYDNEY), new CancelableCallback() {
            @Override
            public void onFinish() {
                Toast.makeText(getBaseContext(), "Animation to Sydney complete", Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getBaseContext(), "Animation to Sydney canceled", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    /**
     * Called when the stop button is clicked.
     */
    public void onStopAnimation(View view) {
        if (!checkReady()) {
            return;
        }

        mMap.stopAnimation();
    }

    /**
     * Called when the zoom in button (the one with the +) is clicked.
     */
    public void onZoomIn(View view) {
        if (!checkReady()) {
            return;
        }

        changeCamera(CameraUpdateFactory.zoomIn());
    }

    /**
     * Called when the zoom out button (the one with the -) is clicked.
     */
    public void onZoomOut(View view) {
        if (!checkReady()) {
            return;
        }

        changeCamera(CameraUpdateFactory.zoomOut());
    }

    /**
     * Called when the tilt more button (the one with the /) is clicked.
     */
    public void onTiltMore(View view) {
        if (!checkReady()) {
            return;
        }

        CameraPosition currentCameraPosition = mMap.getCameraPosition();
        float currentTilt = currentCameraPosition.tilt;
        float newTilt = currentTilt + 10;

        newTilt = (newTilt > 90) ? 90 : newTilt;

        CameraPosition cameraPosition = new CameraPosition.Builder(currentCameraPosition)
                .tilt(newTilt).build();

        changeCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
     * Called when the tilt less button (the one with the \) is clicked.
     */
    public void onTiltLess(View view) {
        if (!checkReady()) {
            return;
        }

        CameraPosition currentCameraPosition = mMap.getCameraPosition();

        float currentTilt = currentCameraPosition.tilt;

        float newTilt = currentTilt - 10;
        newTilt = (newTilt > 0) ? newTilt : 0;

        CameraPosition cameraPosition = new CameraPosition.Builder(currentCameraPosition)
                .tilt(newTilt).build();

        changeCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
     * Called when the left arrow button is clicked. This causes the camera to move to the left
     */
    public void onScrollLeft(View view) {
        if (!checkReady()) {
            return;
        }

        changeCamera(CameraUpdateFactory.scrollBy(-SCROLL_BY_PX, 0));
    }

    /**
     * Called when the right arrow button is clicked. This causes the camera to move to the right.
     */
    public void onScrollRight(View view) {
        if (!checkReady()) {
            return;
        }

        changeCamera(CameraUpdateFactory.scrollBy(SCROLL_BY_PX, 0));
    }

    /**
     * Called when the up arrow button is clicked. The causes the camera to move up.
     */
    public void onScrollUp(View view) {
        if (!checkReady()) {
            return;
        }

        changeCamera(CameraUpdateFactory.scrollBy(0, -SCROLL_BY_PX));
    }

    /**
     * Called when the down arrow button is clicked. This causes the camera to move down.
     */
    public void onScrollDown(View view) {
        if (!checkReady()) {
            return;
        }

        changeCamera(CameraUpdateFactory.scrollBy(0, SCROLL_BY_PX));
    }

    /**
     * Called when the animate button is toggled
     */
    public void onToggleAnimate(View view) {
        updateEnabledState();
    }

    /**
     * Called when the custom duration checkbox is toggled
     */
    public void onToggleCustomDuration(View view) {
        updateEnabledState();
    }

    /**
     * Update the enabled state of the custom duration controls.
     */
    private void updateEnabledState() {
        mCustomDurationToggle.setEnabled(mAnimateToggle.isChecked());
        mCustomDurationBar
                .setEnabled(mAnimateToggle.isChecked() && mCustomDurationToggle.isChecked());
    }

    private void changeCamera(CameraUpdate update) {
        changeCamera(update, null);
    }

    /**
     * Change the camera position by moving or animating the camera depending on the state of the
     * animate toggle button.
     */
    private void changeCamera(CameraUpdate update, CancelableCallback callback) {
        if (mAnimateToggle.isChecked()) {
            if (mCustomDurationToggle.isChecked()) {
                int duration = mCustomDurationBar.getProgress();
                // The duration must be strictly positive so we make it at least 1.
                mMap.animateCamera(update, Math.max(duration, 1), callback);
            } else {
                mMap.animateCamera(update, callback);
            }
        } else {
            mMap.moveCamera(update);
        }
    }
}
