package com.example.yinqinghao.childprotect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.dd.processbutton.FlatButton;
import com.example.yinqinghao.childprotect.asyncTask.GetLocationTask;
import com.example.yinqinghao.childprotect.entity.SharedData;
import com.example.yinqinghao.childprotect.entity.Zone;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wooplr.spotlight.utils.SpotlightSequence;

import java.util.Date;

public class AddZoneActivity extends AppCompatActivity implements OnMapReadyCallback,
        GetLocationTask.LocationResponse, GoogleMap.OnMarkerDragListener {
    private GoogleMap mMap;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 123;
    private static final double RADIUS_OF_EARTH_METERS = 6371009;
    private static final String TAG = "AddZoneActivity";
    private static boolean isEdit;

    private FlatButton mConfirm;
    private EditText mDesEditText;
    private RadioGroup mZoneRadioGroup;
    private View mCenterTutor;
    private View mRadiusTutor;

    private FirebaseDatabase mDb;
    private GetLocationTask locationTask;
    private Location mLocation;
    private MapView mMapView;
    private Marker mCenterMarker;
    private Marker mRadiusMarker;
    private Circle mCircle;
    private double mRadiusMeters;
    private LatLng mCenter;
    private int mStrokeColor = R.color.safeStrokeColor;
    private int mFillColor = R.color.safeFillColor;
    private View.OnClickListener mConfirmListener;
    private RadioGroup.OnCheckedChangeListener mCheckedChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_zone);
        setConfirmListener();
        setOnCheckedChangeListener();
        SharedData.pushContext(this);

        MapsInitializer.initialize(this);
        mMapView = (MapView) findViewById(R.id.map_add_zone);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);
        mDb = FirebaseDatabase.getInstance();


        mZoneRadioGroup = (RadioGroup) findViewById(R.id.radioGroup_zone);
        mDesEditText = (EditText) findViewById(R.id.txt_zone_des);
        mConfirm = (FlatButton) findViewById(R.id.btn_confirm);
        mCenterTutor = findViewById(R.id.center_marker_tour);
        mRadiusTutor = findViewById(R.id.radius_marker_tour);

        mConfirm.setOnClickListener(mConfirmListener);
        mZoneRadioGroup.setOnCheckedChangeListener(mCheckedChangeListener);
        mDesEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
    }

    private void setConfirmListener() {
        mConfirmListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getSharedPreferences("ID", Context.MODE_PRIVATE);
                final String familyId = sp.getString("currentGid",null);
                String des = mDesEditText.getText().toString().trim();
                String status = mZoneRadioGroup.getCheckedRadioButtonId() == R.id.rb_safe_zone ? "safe" : "danger";
                if (des.length() == 0) {
                    Toast.makeText(AddZoneActivity.this, "Please enter Zone Description.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                DatabaseReference refZone = mDb.getReference("zone")
                        .child(familyId);
                Intent intent = getIntent();
                Zone zone = intent.getParcelableExtra("zone");
                isEdit = zone != null;
                String zoneId;
                final Zone zone1;
                if (!isEdit) {
                    zoneId = refZone.push().getKey();
                } else {
                    zoneId = zone.getId();
                }
                zone1 = new Zone(new Date(),des,mCenter.latitude,mCenter.longitude, (long) mRadiusMeters,status,zoneId,familyId);
                refZone.child(zoneId).setValue(zone1).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("zone",zone1);
                            setResult(RESULT_OK, returnIntent);
                            finish();
                        } else {
                            Toast.makeText(AddZoneActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };
    }

    private void setOnCheckedChangeListener() {
        mCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId == R.id.rb_safe_zone) {
                    mStrokeColor = R.color.safeStrokeColor;
                    mFillColor = R.color.safeFillColor;
                } else if (checkedId == R.id.rb_danger_zone) {
                    mStrokeColor = R.color.dangerStrokeColor;
                    mFillColor = R.color.dangerFillColor;
                }
                mCircle.setStrokeColor(ContextCompat.getColor(AddZoneActivity.this,mStrokeColor));
                mCircle.setFillColor(ContextCompat.getColor(AddZoneActivity.this,mFillColor));
            }
        };
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
        mLocation = location;
        if (location == null) {
            mLocation = new Location("");
            mLocation.setLatitude(-37.8668);
            mLocation.setLongitude(145.016);
        }
        if (!isEdit) {
            showTutorial(mCenterTutor, mRadiusTutor);
        }
        mCenter = new LatLng(mLocation.getLatitude(),mLocation.getLongitude());
        mRadiusMeters = 500;
        drawCircle(mCenter, mRadiusMeters,
                ContextCompat.getColor(this,mStrokeColor),
                ContextCompat.getColor(this,mFillColor));
    }

    private void showTutorial(View view, View view2) {
        if (SharedData.isShowTutorial2()) {
            SpotlightSequence.getInstance(this,null)
                    .addSpotlight(view,
                            "Center of The Area", "Long Click to drag the center of the area.", SharedData.getRandomStr())
                    .addSpotlight(view2,
                            "Radius of The Area", "Long Click to adjust the radius of the area", SharedData.getRandomStr())
                    .startSequence();
        }
    }

    private void drawCircle(LatLng latLng, double radius, int strokeColor, int fillColor) {
        mCenterMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("center")
                .snippet("long click to drag")
                .anchor(0.0f, 1.0f)
                .draggable(true));

        mRadiusMarker = mMap.addMarker(new MarkerOptions()
                .position(toRadiusLatLng(latLng, radius))
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.drag)));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(latLng.latitude, latLng.longitude), 15));

        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(radius)
                .strokeColor(strokeColor)
                .fillColor(fillColor)
                .strokeWidth(3)
                .clickable(true);

        mCircle = mMap.addCircle(circleOptions);
        mCenterMarker.showInfoWindow();
    }

    public boolean onMarkerMoved(Marker marker) {
        if (marker.equals(mCenterMarker)) {
            mCircle.setCenter(marker.getPosition());
            mRadiusMarker.setPosition(toRadiusLatLng(marker.getPosition(), mRadiusMeters));
            return true;
        }
        if (marker.equals(mRadiusMarker)) {
            mRadiusMeters =
                    toRadiusMeters(mCenterMarker.getPosition(), mRadiusMarker.getPosition());
            mCircle.setRadius(mRadiusMeters);
            return true;
        }
        return false;
    }

    private static LatLng toRadiusLatLng(LatLng center, double radiusMeters) {
        double radiusAngle = Math.toDegrees(radiusMeters / RADIUS_OF_EARTH_METERS) /
                Math.cos(Math.toRadians(center.latitude));
        return new LatLng(center.latitude, center.longitude + radiusAngle);
    }

    private static double toRadiusMeters(LatLng center, LatLng radius) {
        float[] result = new float[1];
        Location.distanceBetween(center.latitude, center.longitude,
                radius.latitude, radius.longitude, result);
        return result[0];
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMarkerDragListener(this);
        Intent intent = getIntent();
        Zone zone = intent.getParcelableExtra("zone");
        if (zone == null) {
            locationTask = new GetLocationTask(this,this, 0);
            checkPermission();
            try {
                locationTask.execute();
            } catch (IllegalStateException ex) {

            }
        } else {
            boolean isSafe = zone.getStatus().equals("safe");
            mStrokeColor = isSafe ? R.color.safeStrokeColor : R.color.dangerStrokeColor;
            mFillColor = isSafe ? R.color.safeFillColor : R.color.dangerFillColor;
            mCenter = new LatLng(zone.getLat(),zone.getLng());
            mRadiusMeters = zone.getRadius();
            drawCircle(mCenter, mRadiusMeters,
                    ContextCompat.getColor(this,mStrokeColor),
                    ContextCompat.getColor(this,mFillColor));
            mDesEditText.setText(zone.getDes());
            mZoneRadioGroup.check(isSafe ? R.id.rb_safe_zone : R.id.rb_danger_zone);
        }

    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Check the location service permission and request it if there is no permission
     */
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23 ) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            } else
                try {
                    if (locationTask == null) {
                        locationTask = new GetLocationTask(this,this, 0);
                    }
                    locationTask.execute();
                } catch (IllegalStateException ex) {

                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        if (locationTask == null) {
                            locationTask = new GetLocationTask(this,this, 0);
                        }
                        locationTask.execute();
                    } catch (IllegalStateException ex) {

                    }
                } else
                    Toast.makeText(this, "Can't get the location, please grant the permission",
                            Toast.LENGTH_SHORT);
                return;
            }
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        onMarkerMoved(marker);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        onMarkerMoved(marker);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        onMarkerMoved(marker);
        validateRadius();
    }

    private void validateRadius() {
        mCenter = mCenterMarker.getPosition();
        mRadiusMeters = mCircle.getRadius();
        if (mRadiusMeters < 500) {
            Toast.makeText(this, "The radius have to be greater than 500m", Toast.LENGTH_SHORT).show();
            mRadiusMeters = 500;
            mCircle.setRadius(mRadiusMeters);
            mRadiusMarker.setPosition(toRadiusLatLng(mCenter, mRadiusMeters));

        }
    }

    @Override
    protected void onDestroy() {
        SharedData.popContext();
        super.onDestroy();
    }
}
