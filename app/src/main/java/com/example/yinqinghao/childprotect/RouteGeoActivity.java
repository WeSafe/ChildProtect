package com.example.yinqinghao.childprotect;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.yinqinghao.childprotect.asyncTask.GetLocationTask;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.List;

public class RouteGeoActivity extends AppCompatActivity implements OnMapReadyCallback,
        GetLocationTask.LocationResponse {
    private GoogleMap mMap;

    private MapView mMapView;
    private ImageView mBtnClose;

    private Polyline mRouteLine;
    private Marker mMe;

    private GetLocationTask locationTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_geo);

        MapsInitializer.initialize(this);
        mMapView = (MapView) findViewById(R.id.map_routeGeo);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        mBtnClose = (ImageView) findViewById(R.id.btn_close);
        mBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap == null)
            return;
        mMap = googleMap;
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
        Intent intent = getIntent();
        String points = intent.getStringExtra("points");

        List<LatLng> latLngs = PolyUtil.decode(points);
        LatLng startPoint = latLngs.get(0);
        mRouteLine = mMap.addPolyline(new PolylineOptions().addAll(latLngs));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 13));

//        locationTask = new GetLocationTask(this,this,5);
//        locationTask.execute();
    }

    @Override
    public void locationProcessFinish(Location location) {
//        locationTask = null;

    }
}
