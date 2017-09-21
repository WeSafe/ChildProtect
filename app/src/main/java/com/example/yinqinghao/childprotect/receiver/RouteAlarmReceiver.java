package com.example.yinqinghao.childprotect.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.widget.Toast;

import com.example.yinqinghao.childprotect.LoginActivity;
import com.example.yinqinghao.childprotect.RouteGeoActivity;
import com.example.yinqinghao.childprotect.asyncTask.GetLocationTask;
import com.example.yinqinghao.childprotect.entity.Group;
import com.example.yinqinghao.childprotect.entity.LocationData;
import com.example.yinqinghao.childprotect.entity.Person;
import com.example.yinqinghao.childprotect.entity.Route;
import com.example.yinqinghao.childprotect.entity.SharedData;
import com.example.yinqinghao.childprotect.entity.Zone;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by yinqinghao on 29/8/17.
 */

public class RouteAlarmReceiver  extends BroadcastReceiver
        implements GetLocationTask.LocationResponse {

    private GetLocationTask locationTask;
    private Location mLocation;
    private String mMyId;
    private String mGid;
    private int mBatteryLevel;
    private FirebaseDatabase mDb;
    private PowerManager.WakeLock mWl;
    private Context mContext;

    private Route mRoute;
    private List<String> mParentTokens;
    private Person mMe;

    private ValueEventListener mParentTokenListener;
    private ValueEventListener mChildListener;

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            mBatteryLevel = level;
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mDb = FirebaseDatabase.getInstance();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        mWl.acquire();
        mMyId = intent.getStringExtra("uid");
        mGid = intent.getStringExtra("currentGid");
        if (mMyId == null) {
//            Intent intent1 = new Intent(context, LoginActivity.class);
//            context.startActivity(intent1);
            Toast.makeText(mContext, "Can't get User id", Toast.LENGTH_SHORT).show();
            return;
        }

        if (SharedData.getRoute() == null) {
            return;
        }
        mRoute = SharedData.getRoute();

        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mContext.getApplicationContext().registerReceiver(mBatInfoReceiver, batteryLevelFilter);
        initLinstener();
        getData();
    }

    @Override
    public void locationProcessFinish(Location location) {
        locationTask = null;
        mLocation = location;
        if (location == null) {
            Toast.makeText(mContext,
                    "Can't get the current location now, please check the location service and network",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String today = Person.getDatetime() + "";
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        double speed = SharedData.getSpeed(latLng);
        LocationData locationData = new LocationData(new Date(),
                location.getLatitude(),location.getLongitude(), mBatteryLevel, mMyId, speed);
        DatabaseReference refLocation = mDb.getReference("userInfo")
                .child(mMyId)
                .child("locationDatas")
                .child(today)
                .child(locationData.getDatetime().getTime()+"");
        refLocation.setValue(locationData);
        checkIfInRoute(location);
        mWl.release();
    }

    private void checkIfInRoute(Location location) {
//        SharedData.addToken(mGid, mParentTokens);
        List<LatLng> points = PolyUtil.decode(mRoute.getPoints());
        LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        double tolerance = 20;
        boolean isOnPath = PolyUtil.isLocationOnPath(myLatLng,points,true,tolerance);
        if (isOnPath) {
            SharedData.setOnPath(isOnPath);
        } else if (SharedData.isOnPath()) {
            sendNotification(mRoute);
            SharedData.setOnPath(false);
        }
    }

    private void sendNotification(Route route) {
        for (String token : mParentTokens) {
            DatabaseReference refNotification = mDb.getReference("notification")
                    .child(token);
            String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                    .format(new Date().getTime());
            String msg = mMe.getFirstName() + " is not on the route (" + route.getDes() +") at " + date ;
            refNotification.push().child(msg).setValue(true);
        }
        Toast.makeText(mContext, "You are out of path.", Toast.LENGTH_SHORT).show();
    }

    public void setAlarm(Context context, String childId, String familyId) {
        AlarmManager am =(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, RouteAlarmReceiver.class);
        i.putExtra("uid", childId);
        i.putExtra("currentGid", familyId);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (2 * 1000), 1000 * 5, pi);
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, RouteAlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    private void sendLocation(Context context) {
        locationTask = new GetLocationTask(this,context, 5);
        locationTask.execute();

    }

    private void initLinstener() {
        mChildListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mMe = dataSnapshot.getValue(Person.class);
                    sendLocation(mContext);
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
                        if (!key.equals(mMyId)
                                && !mParentTokens.contains(user.get(key))
                                && !user.get(key).equals("Offline")) {
                            mParentTokens.add(user.get(key));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void getData() {
        DatabaseReference refParents = mDb.getReference("group")
                .child(mGid);
        refParents.addListenerForSingleValueEvent(mParentTokenListener);
//        if (SharedData.getTokens().containsKey(mGid)) {
//            DatabaseReference refParents = mDb.getReference("group")
//                    .child(mGid);
//            refParents.addListenerForSingleValueEvent(mParentTokenListener);
//        } else {
//            mParentTokens = SharedData.getTokens().get(mGid);
//        }

        DatabaseReference refMe = mDb.getReference("userInfo")
                .child(mMyId);
        refMe.addListenerForSingleValueEvent(mChildListener);
    }
}
