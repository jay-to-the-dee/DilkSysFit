package com.jonathandilks.dilksysfit;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;


/**
 * Created by jonathan on 04/01/18.
 */

public class DilkSysGPSService extends Service {
    private static final int FOREGROUND_NOTIFICATION_ID = 1;
    private final IBinder binder = new DilkSysGPSServiceBinder();
    private LocationManager locationManager;
    private MyLocationListener locationListener;
    private int runID;

    public int onStartCommand(Intent intent, int flags, int startId) {
        runID = getLastRunId() + 1;

        Intent startIntent = new Intent(DilkSysGPSServiceTask.SERVICE_STARTED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(startIntent);

        return START_STICKY;
    }

    private int getLastRunId() {
        int lastRunId;

        Cursor c = getContentResolver().query(RunDBContract.LASTRUNID_URI, null, null, null, null);
        if (c.moveToFirst()) {
            lastRunId = c.getInt(c.getColumnIndex(RunDBContract.RUN_RUNID));
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
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5,
                    5,
                    (LocationListener) locationListener);
        } catch (SecurityException e) {
            Log.d(getResources().getString(R.string.app_name), e.toString());
        }

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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        locationManager.removeUpdates(locationListener); //Stop adding new entries to the database

        Intent stopIntent = new Intent(DilkSysGPSServiceTask.SERVICE_STOPPED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(stopIntent);
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
            //Log entry to DB here (and send broadcast?!?!)

            ContentValues values = new ContentValues();
            values.put(RunDBContract.RUN_RUNID, runID);
            values.put(RunDBContract.RUN_LATITUDE, location.getLatitude());
            values.put(RunDBContract.RUN_LONGITUDE, location.getLongitude());
            values.put(RunDBContract.RUN_ALTITUDE, location.getAltitude());

            getContentResolver().insert(RunDBContract.URI, values);
//            getContentResolver().notifyChange(insertUri, null);
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
