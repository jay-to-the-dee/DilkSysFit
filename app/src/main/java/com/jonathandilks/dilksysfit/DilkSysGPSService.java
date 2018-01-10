package com.jonathandilks.dilksysfit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.widget.Toast;


/**
 * Created by jonathan on 04/01/18.
 */

public class DilkSysGPSService extends Service {
    private final IBinder binder = new DilkSysGPSServiceBinder();
    private MyLocationListener locationListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5,
                    5,
                    (LocationListener) locationListener);
        } catch (SecurityException e) {
            Log.d(getResources().getString(R.string.app_name), e.toString());
        }

        return START_STICKY;
    }

    private void handleCommand(Intent intent) {
    }

    private class DilkSysGPSServiceBinder extends Binder {
        //Bindings
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
//            Log.d("g53mdp", location.getLatitude() + " " + location.getLongitude());
            //Log entry to DB here (and send broadcast?!?!)
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        // information about the signal, i.e. number of satellites
            Log.d("g53mdp", "onStatusChanged: " + provider + " " + status);
        }

        @Override
        public void onProviderEnabled(String provider) {
        // the user enabled (for example) the GPS
            Log.d("g53mdp", "onProviderEnabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
        // the user disabled (for example) the GPS
            Log.d("g53mdp", "onProviderDisabled: " + provider);
        }
    }
}
