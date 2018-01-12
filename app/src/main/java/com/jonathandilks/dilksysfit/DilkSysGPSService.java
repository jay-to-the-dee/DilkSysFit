package com.jonathandilks.dilksysfit;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;


/**
 * Created by jonathan on 04/01/18.
 */

public class DilkSysGPSService extends Service {
    private static final int FOREGROUND_NOTIFICATION_ID = 1;
    private final IBinder binder = new DilkSysGPSServiceBinder();
    private MyLocationListener locationListener;

    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent startIntent = new Intent(DilkSysGPSServiceTask.SERVICE_STARTED);
        sendBroadcast(startIntent); //TODO: Temporary workaround before we move to onCreate

        return START_STICKY;
    }

    @Override
    public void onCreate() {

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

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification =
                new Notification.Builder(this)
                        .setContentTitle(getText(R.string.notification_title))
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentIntent(pendingIntent)
                        .build();
        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Intent stopIntent = new Intent(DilkSysGPSServiceTask.SERVICE_STOPPED);
        sendBroadcast(stopIntent);
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
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
            values.put(RunDBContract.RUN_LATITUDE, location.getLatitude());
            values.put(RunDBContract.RUN_LONGITUDE, location.getLongitude());
            values.put(RunDBContract.RUN_ALTITUDE, location.getAltitude());

            getContentResolver().insert(RunDBContract.URI, values);

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Context context = getApplicationContext();
            Toast.makeText(context, context.getString(R.string.please_enable_location_services), Toast.LENGTH_LONG).show();
        }
    }
}
