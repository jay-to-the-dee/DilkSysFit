package com.jonathandilks.dilksysfit;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    private static final int VIEW_RUN_REQUEST_CODE = 2;
    private static final String READY_TO_RECORD = "readyToRecord";

    private TextView mSummaryText;
    private LinearLayout mMapLayout;
    private CurrentRunMapFragment mCurrentRunMapFragment;
    private ListView mRunList;
    private BottomNavigationView mNavigation;
    private MenuItem mGPSRecordStartButton;
    private MenuItem mGPSRecordStopButton;
    private SimpleCursorAdapter mAdapter;

    private Intent dilkSysGPSServiceIntent;
    private UIUpdateReceiver uiUpdateReceiver;
    private ContentObserver runUpdateObserver;
    private Handler h = new Handler();
    private boolean readyToRecord = true;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            mSummaryText.setVisibility(View.GONE);
            mMapLayout.setVisibility(View.GONE);
            mRunList.setVisibility(View.GONE);

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mSummaryText.setVisibility(View.VISIBLE);
                    mSummaryText.setText(R.string.title_home);
                    return true;
                case R.id.navigation_current_run:
                    mMapLayout.setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_run_history:
                    mRunList.setVisibility(View.VISIBLE);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigation = findViewById(R.id.navigation);
        mSummaryText = findViewById(R.id.summary_text);
        mMapLayout = findViewById(R.id.map_layout);
        mRunList = findViewById(R.id.run_list);
        mCurrentRunMapFragment = (CurrentRunMapFragment) getFragmentManager().findFragmentById(R.id.current_run_fragment);

        String displayCols[] = new String[]
                {
                        RunDBContract.RUN_SUMMARIES_NAME,
                        RunDBContract.RUN_SUMMARIES_TOTAL_TIME,
                        RunDBContract.RUN_SUMMARIES_ID_TOTAL_DISTANCE,
                        RunDBContract.RUN_SUMMARIES_FINISH_LOCATION_NAME
                };
        int[] colResolutionIds = new int[]
                {
                        R.id.runNameEntry,
                        R.id.timeEntry,
                        R.id.distanceEntry,
                        R.id.approximateLocationEntry
                };

        Cursor runSummariesCursor = getRunSummariesCursor();
        mAdapter = new SimpleCursorAdapter(this,
                R.layout.run_history_list_layout,
                runSummariesCursor,
                displayCols,
                colResolutionIds,
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() { //This is needed to make the formatting nice in the ListView
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                TextView textView = (TextView) view;

                switch (columnIndex) {
                    case 4: //Time
                        int timeInSeconds = cursor.getInt(columnIndex);
                        int timeInMinutes = timeInSeconds / 60;
                        int timeInHours = timeInMinutes / 60;

                        timeInSeconds = timeInSeconds - (timeInMinutes * 60);
                        timeInMinutes = timeInMinutes - (timeInHours * 60);

                        if (timeInMinutes == 0 && timeInHours == 0) {
                            textView.setText(timeInSeconds + "s");
                        } else if (timeInHours == 0) {

                            textView.setText(timeInMinutes + "m " + timeInSeconds + "s");
                        } else {
                            textView.setText(timeInHours + "h " + timeInMinutes + "m " + timeInSeconds + "s");
                        }
                        return true;

                    case 5: //Distance
                        int distanceInMetres = (int) cursor.getFloat(columnIndex);
                        int distanceInKm = distanceInMetres / 1000;

                        if (distanceInKm == 0) {
                            textView.setText(distanceInMetres + "m");
                        } else {
                            textView.setText(distanceInKm + "km");
                        }
                        return true;
                }

                return false;
            }
        });

        mRunList.setAdapter(mAdapter);
        mRunList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putLong("id", id);

                Intent intent = new Intent(MainActivity.this, ViewPreviousRunActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, VIEW_RUN_REQUEST_CODE);
            }
        });

        //Variable setting
        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        dilkSysGPSServiceIntent = new Intent(this, DilkSysGPSService.class);
    }

    private void checkPermissionAndStartGPSservice() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MainActivity.MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        } else {
            startService(dilkSysGPSServiceIntent);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Sweet! We are good to go! Alles ist gut!

                    startService(dilkSysGPSServiceIntent);
                } else {
                    Toast.makeText(this, R.string.error_no_gps_permission, Toast.LENGTH_LONG).show();
                    finish(); //Close the app as it won't work. User will see error message.
                }
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gps_recording_menu, menu);
        mGPSRecordStartButton = menu.findItem(R.id.gps_record_start);
        mGPSRecordStopButton = menu.findItem(R.id.gps_record_stop);
        updateGpsRecordUI(readyToRecord);
        return true;
    }


    public void recordStartClicked(MenuItem item) {
        item.setEnabled(false);
        item.getIcon().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN); //Disable until next update

        mCurrentRunMapFragment.clearAllPoints(); //Clear previous route
        checkPermissionAndStartGPSservice();
    }

    public void recordStopClicked(MenuItem item) {
        item.setEnabled(false);
        item.getIcon().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);  //Disable until next update

        stopService(dilkSysGPSServiceIntent);
    }

    protected void onResume() {
        super.onResume();
        if (uiUpdateReceiver == null)
            uiUpdateReceiver = new UIUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter(DilkSysGPSServiceTask.SERVICE_STARTED);
        intentFilter.addAction(DilkSysGPSServiceTask.SERVICE_STOPPED);
        intentFilter.addAction(DilkSysGPSServiceTask.LOCATION_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(uiUpdateReceiver, intentFilter);

        if (runUpdateObserver == null)
            runUpdateObserver = new RunUpdateObserver(new Handler());

        getContentResolver().registerContentObserver(RunDBContract.ALL_URI, true, runUpdateObserver);
    }

    protected void onPause() {
        super.onPause();
        if (uiUpdateReceiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(uiUpdateReceiver);

        if (runUpdateObserver != null)
            getContentResolver().unregisterContentObserver(runUpdateObserver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(READY_TO_RECORD, readyToRecord);
        super.onSaveInstanceState(outState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        readyToRecord = savedInstanceState.getBoolean(READY_TO_RECORD);
        mNavigation.setSelectedItemId(mNavigation.getSelectedItemId());
    }

    private void updateGpsRecordUI(boolean readyToRecord) {
        this.readyToRecord = readyToRecord;

        mGPSRecordStartButton.setVisible(readyToRecord);
        mGPSRecordStopButton.setVisible(!readyToRecord);

        mGPSRecordStartButton.setEnabled(readyToRecord);
        mGPSRecordStopButton.setEnabled(!readyToRecord);

        mGPSRecordStartButton.getIcon().clearColorFilter(); //Clear any pre-existing colour filters
        mGPSRecordStopButton.getIcon().clearColorFilter();
    }

    public Cursor getRunSummariesCursor() {
        return getContentResolver().query(RunDBContract.RUN_SUMMARIES_URI, RunDBContract.allColsRunSummary, null, null, null);
    }

    private class UIUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            switch (intent.getAction()) {
                case DilkSysGPSServiceTask.SERVICE_STARTED:
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            updateGpsRecordUI(false);
                        }
                    });
                    break;
                case DilkSysGPSServiceTask.SERVICE_STOPPED:
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            updateGpsRecordUI(true);
                        }
                    });
                    break;
                case DilkSysGPSServiceTask.LOCATION_CHANGED:
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            Bundle extras = intent.getExtras();
                            double lat = extras.getDouble("lat");
                            double lng = extras.getDouble("lng");

                            mCurrentRunMapFragment.addPoint(new LatLng(lat, lng));
                        }
                    });
                    break;
            }
        }
    }

    private class RunUpdateObserver extends ContentObserver {
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public RunUpdateObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);

            switch (uri.getPathSegments().get(0)) {
                case "run_summaries":
                    mAdapter.changeCursor(getRunSummariesCursor());
                    break;
            }

        }
    }
}
