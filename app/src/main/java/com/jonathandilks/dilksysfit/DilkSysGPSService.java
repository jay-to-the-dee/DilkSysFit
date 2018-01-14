package com.jonathandilks.dilksysfit;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


/**
 * Created by jonathan on 04/01/18.
 */

public class DilkSysGPSService extends Service {
    private static final int FOREGROUND_NOTIFICATION_ID = 1;

    private final IBinder binder = new DilkSysGPSServiceBinder();
    private LocationManager locationManager;
    private MyLocationListener locationListener;

    private int mRunID;
    private int mCumPointsTracked;
    private float mCumDistanceTravelled;
    private Location mLastLocation;
    private Date mStartTime;


    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent startIntent = new Intent(DilkSysGPSServiceTask.SERVICE_STARTED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(startIntent);

        return START_STICKY;
    }

    private int getLastRunId() {
        int lastRunId;

        Cursor c = getContentResolver().query(RunDBContract.POINT_DATA_URI_LAST_ENTRY, null, null, null, null);
        if (c.moveToFirst()) {
            lastRunId = c.getInt(c.getColumnIndex(RunDBContract.POINT_DATA_RUNID));
        } else {
            lastRunId = 0; //We'll assume it's the first run ever
            Log.i("DilkSysFit", "Last run not found - is this our first time running?");
        }
        c.close();

        return lastRunId;
    }

    @Override
    public void onCreate() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        Location lastKnownLocation;

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification =
                new Notification.Builder(this)
                        .setContentTitle(getText(R.string.notification_title))
                        .setSmallIcon(R.drawable.ic_directions_run_white_48dp)
                        .setContentIntent(pendingIntent)
                        .build();
        startForeground(FOREGROUND_NOTIFICATION_ID, notification);

        mRunID = getLastRunId() + 1;
        mCumPointsTracked = 0;
        mCumDistanceTravelled = 0;
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5,
                    5,
                    locationListener);
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationListener.onLocationChanged(lastKnownLocation); //Trigger first point on record start
        } catch (SecurityException | NullPointerException e) {
            Log.d(getResources().getString(R.string.app_name), e.toString());
        }
        mStartTime = new Date();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        locationManager.removeUpdates(locationListener); //Stop adding new entries to the database
        try {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationListener.onLocationChanged(lastKnownLocation); //Trigger last point on record stop
        } catch (SecurityException | NullPointerException e) {
            Log.d(getResources().getString(R.string.app_name), e.toString());
        }

        if (mCumPointsTracked > 2) { //Don't add empty runs - require at least 3 points (start + movement + end)
            generateSummaryEntry();
        }

        Intent stopIntent = new Intent(DilkSysGPSServiceTask.SERVICE_STOPPED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(stopIntent);
    }

    private void generateSummaryEntry() {
        String locationName = null;
        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(this, Locale.UK);
            try {
                List<Address> finishLocationAddress = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                locationName = finishLocationAddress.get(0).getLocality();
            } catch (IOException e) {
            }
        }

        ContentValues values = new ContentValues();

        values.put(RunDBContract.RUN_SUMMARIES_ID, mRunID);
        values.put(RunDBContract.RUN_SUMMARIES_ID_TOTAL_DISTANCE, mCumDistanceTravelled);

        Date finishTime = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(finishTime);
        long diffMs = finishTime.getTime() - mStartTime.getTime();
        long diffSeconds = TimeUnit.MILLISECONDS.toSeconds(diffMs);
        values.put(RunDBContract.RUN_SUMMARIES_TOTAL_TIME, diffSeconds);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE", Locale.UK);
        String dayString = simpleDateFormat.format(calendar.getTime());

        // Get all the data we need for default name calculations
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        float avgMetresPerSecond = mCumDistanceTravelled / diffSeconds;

        String defaultSummaryString = dayString + " " + getTODString(hour);
        if (locationName != null) {
            defaultSummaryString += " " + getPaceVerb(avgMetresPerSecond) + " in " + locationName;
            values.put(RunDBContract.RUN_SUMMARIES_FINISH_LOCATION_NAME, locationName);
        }
        values.put(RunDBContract.RUN_SUMMARIES_NAME, defaultSummaryString);


        getContentResolver().insert(RunDBContract.RUN_SUMMARIES_URI, values);
    }

    public static String getTODString(int hour) {
        if (hour >= 0 && hour <= 4) {
            return "Midnight";
        } else if (hour <= 11) {
            return "Morning";
        } else if (hour <= 15) {
            return "Afternoon";
        } else if (hour <= 20) {
            return "Evening";
        } else {
            return "Night";
        }
    }

    public static String getPaceVerb(float avgMetresPerSecond) {
        if (avgMetresPerSecond < 1.6) {
            return "walk";//<~5.8 km/h
        } else if (avgMetresPerSecond < 2.3) {
            return "jog"; //<~8.5 km/h
        } else if (avgMetresPerSecond < 4.2) {
            return "run"; //<~15 km/h
        } else {
            return "sprint";
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private class DilkSysGPSServiceBinder extends Binder {
        //Bindings
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            //Log entry to DB here
            ContentValues values = new ContentValues();
            values.put(RunDBContract.POINT_DATA_RUNID, mRunID);
            values.put(RunDBContract.POINT_DATA_LATITUDE, location.getLatitude());
            values.put(RunDBContract.POINT_DATA_LONGITUDE, location.getLongitude());
            if (location.hasAltitude()) { //Don't write it if we don't have it
                values.put(RunDBContract.POINT_DATA_ALTITUDE, location.getAltitude());
            }
            if (location.hasSpeed()) {
                values.put(RunDBContract.POINT_DATA_SPEED, location.getSpeed()); //Even if we don't use this now - we may in future versions
            }
            getContentResolver().insert(RunDBContract.POINT_DATA_URI, values);

            if (mLastLocation != null) {
                mCumDistanceTravelled += location.distanceTo(mLastLocation);
            }
            mLastLocation = location;
            mCumPointsTracked++;
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            Context context = getApplicationContext();
            Toast.makeText(context, context.getString(R.string.please_enable_location_services), Toast.LENGTH_LONG).show();
        }
    }
}
