package com.jonathandilks.dilksysfit;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class ViewPreviousRunActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private PolylineOptions mRoutePolyOptions;
    private final static float DESIRED_ZOOM = 14.5f;
    private Polyline mPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_previous_run);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Bundle bundle = getIntent().getExtras();
        long id = bundle.getLong("id");
        LatLng lastLatLng = new LatLng(51.5074f, 0.1278f);

        String runUri = RunDBContract.RUN_SUMMARIES_URI + "/" + String.valueOf(id);
        Cursor c = getContentResolver().query(Uri.parse(runUri), null, null, null, null);
        if (c.moveToFirst()) {
            String runName = c.getString(c.getColumnIndexOrThrow(RunDBContract.RUN_SUMMARIES_NAME));
            setTitle(runName);
        }
        c.close();

        mRoutePolyOptions = new PolylineOptions();
        String pointDataUri = RunDBContract.POINT_DATA_URI + "/" + String.valueOf(id);
        Cursor c2 = getContentResolver().query(Uri.parse(pointDataUri), null, null, null, null);
        if (c2.moveToFirst()) {
            do {
                float lat = c2.getFloat(c2.getColumnIndexOrThrow(RunDBContract.POINT_DATA_LATITUDE));
                float lng = c2.getFloat(c2.getColumnIndexOrThrow(RunDBContract.POINT_DATA_LONGITUDE));
                lastLatLng = new LatLng(lat, lng);
                mRoutePolyOptions.add(lastLatLng);
            } while (c2.moveToNext());
        }
        c2.close();


        mMap.moveCamera(CameraUpdateFactory.newLatLng(lastLatLng));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(DESIRED_ZOOM));
        mPolyline = googleMap.addPolyline(mRoutePolyOptions);
    }
}
