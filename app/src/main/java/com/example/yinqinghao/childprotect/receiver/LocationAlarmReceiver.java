package com.example.yinqinghao.childprotect.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.widget.Toast;

import com.example.yinqinghao.childprotect.LoginActivity;
import com.example.yinqinghao.childprotect.asyncTask.GetLocationTask;
import com.example.yinqinghao.childprotect.entity.LocationData;
import com.example.yinqinghao.childprotect.entity.Person;
import com.example.yinqinghao.childprotect.entity.Zone;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LocationAlarmReceiver extends BroadcastReceiver
        implements GetLocationTask.LocationResponse {
    private GetLocationTask locationTask;
    private Location mLocation;
    private String mMyId;
    private String mFamilyId;
    private int mBatteryLevel;
    private FirebaseDatabase mDb;
    private PowerManager.WakeLock mWl;
    private Context mContext;

    private Map<String, Zone> mZones;
    private List<String> mParentTokens;
    private List<String> mHistroyZones;
    private Person mMe;
    private ValueEventListener mZonesLinstener;
    private ValueEventListener mParentTokenListener;
    private ValueEventListener mChildListener;
    private String mStatus;

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
        mMyId = intent.getStringExtra("parentId");
        mFamilyId = intent.getStringExtra("familyId");
        if (mMyId == null || mFamilyId == null) {
            Intent intent1 = new Intent(context, LoginActivity.class);
            context.startActivity(intent1);
            return;
        }

        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mContext.getApplicationContext().registerReceiver(mBatInfoReceiver, batteryLevelFilter);
        SharedPreferences sp = context.getSharedPreferences("ID", Context.MODE_PRIVATE);
        Type type = new TypeToken<List<String>>(){}.getType();
        String str = sp.getString("historyZones", null);
        if (str == null)
            mHistroyZones = new ArrayList<>();
        else
            mHistroyZones = new Gson().fromJson(str,type);
        initLinstener();
        getData();

    }

    public void setAlarm(Context context, String childId, String familyId) {
        AlarmManager am =(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, LocationAlarmReceiver.class);
        i.putExtra("parentId", childId);
        i.putExtra("familyId", familyId);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (2 * 1000), 1000 * 60, pi);
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, LocationAlarmReceiver.class);
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

        mZonesLinstener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mZones = new HashMap<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Zone zone = ds.getValue(Zone.class);
                        mZones.put(zone.getId(), zone);
                    }
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
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.child("uid").getValue().toString().equals(mMyId)) continue;
                        for (DataSnapshot ds1 : ds.child("notificationTokens").getChildren()) {
                            String token = ds1.getKey();
                            mParentTokens.add(token);
                            break;
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
        DatabaseReference refZones = mDb.getReference("zone")
                .child(mFamilyId);
        refZones.addValueEventListener(mZonesLinstener);
        refZones.addListenerForSingleValueEvent(mZonesLinstener);

        DatabaseReference refParents = mDb.getReference("family")
                .child(mFamilyId);
        refParents.addValueEventListener(mParentTokenListener);
        refParents.addListenerForSingleValueEvent(mParentTokenListener);

        DatabaseReference refChild = mDb.getReference("family")
                .child(mFamilyId)
                .child(mMyId);
        refChild.addListenerForSingleValueEvent(mChildListener);
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
        LocationData locationData = new LocationData(new Date(),
                location.getLatitude(),location.getLongitude(), mBatteryLevel, mMyId);
        DatabaseReference refLocation = mDb.getReference("family")
                .child(mFamilyId)
                .child(mMyId)
                .child("locationDatas")
                .child(today)
                .child(locationData.getDatetime().getTime()+"");
        refLocation.setValue(locationData);
        checkIfInZone(location);
        mWl.release();
    }

    private void checkIfInZone(Location location) {
        float[] distance;
        List<String> notifiedZone = new ArrayList<>();
        for (Zone zone : mZones.values()) {
            distance = new float[2];
            Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                    zone.getLat(), zone.getLng(), distance);
            if (distance[0] <= zone.getRadius()) {
                notifiedZone.add(zone.getId());
            }
        }
        List<String> enterZones = nonOverlap(notifiedZone, interselect(notifiedZone, mHistroyZones));
        List<String> leaveZones = nonOverlap(mHistroyZones, interselect(notifiedZone, mHistroyZones));
        sendNotification(enterZones,leaveZones);
        mHistroyZones = notifiedZone;
        SharedPreferences sp = mContext.getSharedPreferences("ID", Context.MODE_PRIVATE);
        SharedPreferences.Editor eLogin= sp.edit();
        String historyZones = new Gson().toJson(mHistroyZones);
        eLogin.putString("historyZones", historyZones);
        eLogin.apply();
    }

    public static List<String> interselect(List<String> coll1, List<String> coll2) {
        Set<String> set = new HashSet<>(coll1);
        set.retainAll(new HashSet<>(coll2));
        return new ArrayList<>(set);
    }

    public static List<String> nonOverlap(List<String> coll1, List<String> coll2) {
        Set<String> set = new HashSet<>(coll1);
        set.removeAll(new HashSet<>(coll2));
        return new ArrayList<>(set);
    }

    private void sendNotification(List<String> enterZones, List<String> leaveZones) {
        Zone zone;
        for (String s : enterZones) {
            if (mZones.containsKey(s)) {
                zone = mZones.get(s);
                send("entered into", zone);
            }
        }

        for (String s : leaveZones) {
            if (mZones.containsKey(s)) {
                zone = mZones.get(s);
                send("leaved", zone);
            }
        }
    }

    private void send(String action, Zone zone) {
        if (zone != null) {
            for (String token : mParentTokens) {
                DatabaseReference refNotification = mDb.getReference("notification")
                        .child(token);
                String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                        .format(new Date().getTime());
                String msg = mMe.getFirstName() + " " + action + " the " + zone.getStatus()
                        + " zone (" + zone.getDes() +") at " + date ;
                refNotification.push().child(msg).setValue(true);
            }
        }
    }
}
