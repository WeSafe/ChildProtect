package com.example.yinqinghao.childprotect.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.example.yinqinghao.childprotect.R;
import com.example.yinqinghao.childprotect.receiver.LocationAlarmReceiver;
import com.example.yinqinghao.childprotect.receiver.RouteAlarmReceiver;

/**
 * Created by yinqinghao on 29/8/17.
 */

public class RouteService extends Service {

    final static String TAG = "RouteService";
    private String mChildId;
    private String mFamilyId;
    private RouteAlarmReceiver mAlarmReceiver;
    private Notification notification;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationCompat.Builder bBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.ic_android_black_24dp)
                .setContentTitle("Route Geofencing")
                .setPriority(Notification.PRIORITY_MAX)
                .setContentText("ChildProtect")
                .setOngoing(true);
        notification = bBuilder.build();
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        startForeground(23456, notification);

        SharedPreferences sp = getSharedPreferences("ID", Context.MODE_PRIVATE);
        mChildId = sp.getString("uid",null);
        mFamilyId = sp.getString("currentGid", null);
        mAlarmReceiver = new RouteAlarmReceiver();
        mAlarmReceiver.setAlarm(this,mChildId,mFamilyId);
        return Service.START_STICKY;
    }

    public void stopUpload() {
        if (mAlarmReceiver != null) {
            mAlarmReceiver.cancelAlarm(this);
            stopForeground(true);
        }
    }

    @Override
    public void onDestroy() {
        if (mAlarmReceiver != null) {
            mAlarmReceiver.cancelAlarm(this);
        }
        super.onDestroy();
    }
}
