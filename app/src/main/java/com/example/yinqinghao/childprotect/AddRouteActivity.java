package com.example.yinqinghao.childprotect;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.processbutton.FlatButton;
import com.example.yinqinghao.childprotect.asyncTask.GetLocationTask;
import com.example.yinqinghao.childprotect.service.LocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AddRouteActivity extends AppCompatActivity implements OnMapReadyCallback,
        GetLocationTask.LocationResponse, GoogleMap.OnMarkerDragListener {
    private GoogleMap mMap;
    private FirebaseDatabase mDb;

    private MapView mMapView;
    private EditText mDesEditText;
    private FlatButton mConfirm;
    private FlatButton mAddWayPoint;
    private FlatButton mGetRoute;
    private GetLocationTask locationTask;

    private Marker mStartMarker;
    private Marker mEndMarker;
    private List<Marker> mWayPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_route);

        MapsInitializer.initialize(this);
        mMapView = (MapView) findViewById(R.id.map_add_route);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);
        mDb = FirebaseDatabase.getInstance();

        mConfirm = (FlatButton) findViewById(R.id.btn_confirm_route);
        mAddWayPoint = (FlatButton) findViewById(R.id.btn_add_wp);
        mGetRoute = (FlatButton) findViewById(R.id.btn_get_route);
        mDesEditText = (EditText) findViewById(R.id.txt_route_des);
        mDesEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap == null)
            return;
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-37.8668, 145.016), 13));
        locationTask = new GetLocationTask(this, this, 5);
        locationTask.execute();

    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * press the back button
     * @param item  back button
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent parentIntent = NavUtils.getParentActivityIntent(this);
                if(parentIntent != null) {
                    Intent returnIntent = new Intent();
                    setResult(RESULT_CANCELED, returnIntent);
                    finish();
                    return true;
                } else {
                    parentIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
                            | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(parentIntent);
                    finish();
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void locationProcessFinish(Location location) {
        locationTask = null;
        if (location == null) {
            Toast.makeText(this, "can't get current location.", Toast.LENGTH_SHORT).show();
            return;
        }
        setMarkers(location);
    }

    private void setMarkers(Location location) {
        double offset = 0.005;
        LatLng startLatLag = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng endLatLag = new LatLng(location.getLatitude()+offset, location.getLongitude()+offset);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                startLatLag, 14));
        mStartMarker = mMap.addMarker(new MarkerOptions()
                .position(startLatLag)
                .anchor(0.0f, 1.0f)
                .draggable(true));

        mEndMarker = mMap.addMarker(new MarkerOptions()
                .position(endLatLag)
                .anchor(0.0f, 1.0f)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .draggable(true));
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }
}
