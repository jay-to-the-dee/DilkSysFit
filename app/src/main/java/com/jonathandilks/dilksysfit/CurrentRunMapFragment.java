package com.jonathandilks.dilksysfit;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.LinkedList;

public class CurrentRunMapFragment extends MapFragment implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private PolylineOptions mRoutePolyOptions;
    private Polyline mPolyline;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRoutePolyOptions = null;
        this.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setIndoorLevelPickerEnabled(false);

        try {
            googleMap.setMyLocationEnabled(true); //Add current location dot and button

            LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new MapZoomLocationListener(googleMap), null);
        } catch (SecurityException | NullPointerException e) {
            //Let's just zoom into London as a last resort
            float lat = 51.5074f;
            float lng = 0.1278f;
            new MapZoomLocationListener(googleMap).setGMapCallback(new LatLng(lat, lng));
        }
    }

    public void addPoint(LatLng latLng) {
        if (mRoutePolyOptions == null) {
            mRoutePolyOptions = new PolylineOptions();
        }

        mRoutePolyOptions.add(latLng);
        if (mPolyline != null) {
            mPolyline.remove();
        }
        mPolyline = googleMap.addPolyline(mRoutePolyOptions);
    }

    public void clearAllPoints() {
        if (mPolyline != null) {
            mPolyline.remove();
        }
        mPolyline = null;
        mRoutePolyOptions = null;
    }


    private class MapZoomLocationListener implements LocationListener {
        private final GoogleMap googleMap;

        public MapZoomLocationListener(GoogleMap googleMap) {
            this.googleMap = googleMap;
        }

        @Override
        public void onLocationChanged(Location location) {
            final LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            setGMapCallback(currentLatLng);
        }

        public void setGMapCallback(final LatLng latLng) {
            final float DESIRED_ZOOM = 14.5f;

            googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(latLng)
                                    .zoom(DESIRED_ZOOM)
                                    .build()));
                }
            });
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }
}
