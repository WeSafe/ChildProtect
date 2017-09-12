package com.example.yinqinghao.childprotect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.brouding.simpledialog.SimpleDialog;
import com.dd.processbutton.FlatButton;
import com.example.yinqinghao.childprotect.asyncTask.GetLocationTask;
import com.example.yinqinghao.childprotect.entity.Group;
import com.example.yinqinghao.childprotect.entity.LocationData;
import com.example.yinqinghao.childprotect.entity.Person;
import com.example.yinqinghao.childprotect.entity.Route;
import com.example.yinqinghao.childprotect.entity.SharedData;
import com.example.yinqinghao.childprotect.service.LocationService;
import com.example.yinqinghao.childprotect.service.RouteService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RouteGeoActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    private MapView mMapView;
    private ImageView mBtnClose;
    private TextView mTextTime;
    private FlatButton mBtnStart;
    private FlatButton mBtnStop;

    private FirebaseDatabase mDb;
    private FirebaseAuth mAuth;

    private Polyline mRouteLine;
//    private Marker mMe;
    private String mMyId;
    private Person mUser;
    private String mGid;
    private Route mRoute;
    private long mTodayTime;
    private LatLng mDestination;
    private boolean reminded = false;
    private boolean reminded2 = false;
    private long mDuration;
    private List<String> mParentTokens;
    private CountDownTimer timer;

    private View.OnClickListener mBtnStartOnclickListener;
    private View.OnClickListener mBtnStopOnclickListener;
    private ValueEventListener mLocationValueListener;
    private ValueEventListener mParentTokenListener;
    private ValueEventListener mChildListener;
    private ValueEventListener mReachedMessageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_geo);
        SharedData.pushContext(this);

        MapsInitializer.initialize(this);
        mMapView = (MapView) findViewById(R.id.map_routeGeo);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        mDb = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mTextTime = (TextView) findViewById(R.id.txt_time);
        mBtnStart = (FlatButton) findViewById(R.id.btn_route);
        mBtnStop = (FlatButton) findViewById(R.id.btn_route_stop);
        mBtnClose = (ImageView) findViewById(R.id.btn_close);

        mTodayTime = Person.getDatetime();
        SharedPreferences sp = getSharedPreferences("ID", Context.MODE_PRIVATE);
        mMyId = sp.getString("uid", null);
        mGid = sp.getString("currentGid", null);

        initListener();

        if (!SharedData.isStartedService()) {
            Intent intent = new Intent(RouteGeoActivity.this, LocationService.class);
            startService(intent);
        }
        DatabaseReference refMe = mDb.getReference("userInfo")
                .child(mAuth.getCurrentUser().getUid());
        com.google.firebase.database.Query queryLocation = refMe
                .child("locationDatas").child(mTodayTime + "").orderByKey()
                .limitToLast(1);
        queryLocation.addValueEventListener(mLocationValueListener);

        mBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
//                SharedData.setmContext(get);
            }
        });
        mBtnStart.setOnClickListener(mBtnStartOnclickListener);
        mBtnStop.setOnClickListener(mBtnStopOnclickListener);

    }

    private static double getDistance(LatLng p1, LatLng p2) {
        float[] result = new float[1];
        Location.distanceBetween(p1.latitude, p1.longitude,
                p2.latitude, p2.longitude, result);
        return result[0];
    }

    private void stop() {
        popupReachMessage();
        mBtnStart.setVisibility(View.VISIBLE);
        mBtnStop.setVisibility(View.GONE);
        Intent intent = new Intent(RouteGeoActivity.this, LocationService.class);
        startService(intent);
        Intent intent2 = new Intent(RouteGeoActivity.this, RouteService.class);
        stopService(intent2);
        if (timer != null ) {
            timer.cancel();
            mTextTime.setText("You have reached the destination.");
        }
    }

    private void start() {
        mBtnStart.setVisibility(View.GONE);
        mBtnStop.setVisibility(View.VISIBLE);
        Intent intent = new Intent(RouteGeoActivity.this, LocationService.class);
        stopService(intent);
        SharedData.setRoute(mRoute);
        Intent intent2 = new Intent(RouteGeoActivity.this, RouteService.class);
        startService(intent2);
        createCountDownTimer(mRoute.getDuration());
    }

    private void createCountDownTimer(long time) {
        long limit = -1;
        if (time > 10 * 60) {
            limit = 5 * 60;
        } else if (time > 5 * 60){
            limit = 2 *  60;
        } else if (time > 3 * 60) {
            limit = 1 *  60;
        }
        final long l = limit;
        timer = new CountDownTimer(time * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTextTime.setText(showTime((int)millisUntilFinished/1000));
                if (l * 1000 >= millisUntilFinished && !reminded2) {
                    popupTime(millisUntilFinished);
                    reminded2 = true;
                }
            }

            @Override
            public void onFinish() {
                mTextTime.setText("Time is over!");
                sendNotification();
            }
        }.start();
    }

    private void sendNotification() {
        DatabaseReference refMe = mDb.getReference("userInfo")
                .child(mMyId);
        refMe.addListenerForSingleValueEvent(mChildListener);

        DatabaseReference refParents = mDb.getReference("group")
                .child(mGid);
        refParents.addListenerForSingleValueEvent(mParentTokenListener);
    }

    private void popupTime(final long l) {
        new SimpleDialog.Builder(RouteGeoActivity.this)
                .setTitle("Do you want to add 10 more mins?", true)
                .onConfirm(new SimpleDialog.BtnCallback() {
                    @Override
                    public void onClick(@NonNull SimpleDialog dialog, @NonNull SimpleDialog.BtnAction which) {
                        timer.cancel();
                        createCountDownTimer((int)(l/1000 + 10 * 60));
                    }
                })
                .setBtnConfirmText("Yes")
                .setBtnConfirmTextColor("#e6b115")
                .setBtnCancelText("No")
                .show();
    }

    private void popupStart() {
        new SimpleDialog.Builder(RouteGeoActivity.this)
                .setTitle("Please make sure you are on the path.", true)
                .onConfirm(new SimpleDialog.BtnCallback() {
                    @Override
                    public void onClick(@NonNull SimpleDialog dialog, @NonNull SimpleDialog.BtnAction which) {
                        start();
                        SharedData.setOnPath(true);
                    }
                })
                .setBtnConfirmText("Yes")
                .setBtnConfirmTextColor("#e6b115")
                .setBtnCancelText("No")
                .show();
    }

    private void popupReach () {
        new SimpleDialog.Builder(RouteGeoActivity.this)
                .setTitle("Have you reached the destination?", true)
                .onConfirm(new SimpleDialog.BtnCallback() {
                    @Override
                    public void onClick(@NonNull SimpleDialog dialog, @NonNull SimpleDialog.BtnAction which) {
                        stop();
                    }
                })
                .setBtnConfirmText("Yes")
                .setBtnConfirmTextColor("#e6b115")
                .setBtnCancelText("No")
                .show();
        reminded = true;
    }

    private void popupReachMessage () {
        new SimpleDialog.Builder(RouteGeoActivity.this)
                .setTitle("Do you want to notify that you have reached the destination?", true)
                .onConfirm(new SimpleDialog.BtnCallback() {
                    @Override
                    public void onClick(@NonNull SimpleDialog dialog, @NonNull SimpleDialog.BtnAction which) {
                        sendReachedMessage();
                    }
                })
                .setBtnConfirmText("Yes")
                .setBtnConfirmTextColor("#e6b115")
                .setBtnCancelText("No")
                .show();
    }

    private void sendReachedMessage() {
        if (mUser == null) {
            DatabaseReference refMe = mDb.getReference("userInfo")
                    .child(mMyId);
            refMe.addListenerForSingleValueEvent(mChildListener);
        }
        DatabaseReference refParents = mDb.getReference("group")
                .child(mGid);
        refParents.addListenerForSingleValueEvent(mReachedMessageListener);
    }

    private void initListener() {
        mLocationValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    LocationData locationData = null;
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        locationData = ds.getValue(LocationData.class);
                    }
                    if (locationData == null) return;
//                    if (mMe != null)
//                        mMe.remove();
                    LatLng myLatlng = new LatLng(locationData.getLat(), locationData.getLng());
                    if (!reminded && getDistance(mDestination, myLatlng) <  10 ) {
                        popupReach();
                    }
//                    mMe = mMap.addMarker(new MarkerOptions()
//                            .position(myLatlng)
//                            .title("my location"));
//                    mMe.showInfoWindow();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mChildListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mUser = dataSnapshot.getValue(Person.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mParentTokenListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mParentTokens = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    Group g = dataSnapshot.getValue(Group.class);
                    Map<String, String> user = g.getUsers();
                    for (String key: user.keySet()) {
                        if (!key.equals(mMyId) && !mParentTokens.contains(user.get(key))) {
                            mParentTokens.add(user.get(key));
                            DatabaseReference refNotification = mDb.getReference("notification")
                                    .child(user.get(key));
                            String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                                    .format(new Date().getTime());
                            String msg = mUser.getFirstName() + " hasn't reached destination in estimated time " +
                                    "(route: " + mRoute.getDes() +") at " + date ;
                            refNotification.push().child(msg).setValue(true);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mReachedMessageListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mParentTokens = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    Group g = dataSnapshot.getValue(Group.class);
                    Map<String, String> user = g.getUsers();
                    for (String key: user.keySet()) {
                        if (!key.equals(mMyId) && !mParentTokens.contains(user.get(key))) {
                            mParentTokens.add(user.get(key));
                            DatabaseReference refNotification = mDb.getReference("notification")
                                    .child(user.get(key));
                            String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                                    .format(new Date().getTime());
                            String msg = mUser.getFirstName() + " has reached destination " +
                                    "(route: " + mRoute.getDes() +") at " + date ;
                            refNotification.push().child(msg).setValue(true);
                        }
                    }
                }
                Toast.makeText(RouteGeoActivity.this, "Message is sent", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mBtnStopOnclickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupReach();
            }
        };

        mBtnStartOnclickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupStart();
            }
        };
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
        mMap.setPadding(0,80,0,0);
        Intent intent = getIntent();
        mRoute = intent.getParcelableExtra("route");
        String points = mRoute.getPoints();
        mDuration = mRoute.getDuration();
        mTextTime.setText(showTime(mDuration));

        List<LatLng> latLngs = PolyUtil.decode(points);
        LatLng startPoint = latLngs.get(0);
        mDestination = latLngs.get(latLngs.size()-1);
        mRouteLine = mMap.addPolyline(new PolylineOptions().addAll(latLngs));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 13));
    }

    private String showTime(long duration) {
        long days = TimeUnit.SECONDS.toDays(duration);
        duration -= TimeUnit.DAYS.toSeconds(days);
        long hours = TimeUnit.SECONDS.toHours(duration);
        duration -= TimeUnit.HOURS.toSeconds(hours);
        long minutes = TimeUnit.SECONDS.toMinutes(duration);
        duration -= TimeUnit.MINUTES.toSeconds(minutes);
        long seconds = duration;
        String dur = "";
        if (days == 0 && hours == 0 && minutes == 0) {
            dur = seconds + "s";
        } else if (days == 0 && hours == 0) {
            dur = minutes + "mins, " + seconds + "s";
        } else  if (days == 0) {
            dur = hours + "hours, " + minutes + "mins, " + seconds + "s";
        } else {
            dur = days + "days, " + hours + "hours, " + minutes + "mins, " + seconds + "s";
        }

        String str = "Estimated time: " + dur + ".";
        return str;
    }

    @Override
    public void onResume() {
        if (mMap != null && !mMap.isMyLocationEnabled()) {
            try {
                mMap.setMyLocationEnabled(true);
            } catch (SecurityException ex) {

            }
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (mMap != null && mMap.isMyLocationEnabled()) {
            try {
                mMap.setMyLocationEnabled(false);
            } catch (SecurityException ex) {

            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(RouteGeoActivity.this, LocationService.class);
        if (SharedData.isStartedService()) {
            startService(intent);
        } else {
            stopService(intent);
        }
        Intent intent2 = new Intent(RouteGeoActivity.this, RouteService.class);
        stopService(intent2);
        if (timer != null) {
            timer.cancel();
        }
        SharedData.popContext();
        super.onDestroy();
    }
}
