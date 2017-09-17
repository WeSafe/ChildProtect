package com.example.yinqinghao.childprotect.entity;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

/**
 * Created by yinqinghao on 29/8/17.
 */

public class SharedData {
    private static final String TAG = "SharedData";
    private static boolean startedService = false;
    private static Route route;
    private static boolean onPath = true;
    private static String randomStr;
    private static boolean showTutorial1 = false;
    private static boolean showTutorial2 = false;
    private static boolean showTutorial3 = false;
    private static boolean isShown = false;
    private static boolean isSos = false;
    private static boolean isSplashed = false;
    private static LatLng lastLatLng;
    private static double distance = 0;
    private static long time = 0;
    private static Stack<Context> mContexts = new Stack<>();
    private static List<String> mGroupId;

    public static boolean isStartedService() {
        return startedService;
    }

    public static void setStartedService(boolean startedService) {
        SharedData.startedService = startedService;
    }

    public static Route getRoute() {
        return route;
    }

    public static void setRoute(Route route) {
        SharedData.route = route;
    }

    public static boolean isOnPath() {
        return onPath;
    }

    public static void setOnPath(boolean onPath) {
        SharedData.onPath = onPath;
    }

    public static LatLng getLastLatLng() {
        return lastLatLng;
    }

    public static void setLastLatLng(LatLng lastLatLng) {
        SharedData.lastLatLng = lastLatLng;
    }

    public static double getDistance() {
        return distance;
    }

    public static void setDistance(double distance) {
        SharedData.distance = distance;
    }

    public static long getTime() {
        return time;
    }

    public static double getSpeed(LatLng curLatLng) {
        if (lastLatLng == null) {
            lastLatLng = curLatLng;
            return 0;
        }
        distance = getDistance(lastLatLng, curLatLng);
        time = 5;
        lastLatLng = curLatLng;
        double speed = distance / time;
        return speed;
    }

    private static double getDistance(LatLng latLng1, LatLng latLng2) {
//        double R = 6371000; // for haversine use R = 6372.8 km instead of 6371 km
        float[] result = new float[1];
        double lat1 = latLng1.latitude;
        double lon1 = latLng1.longitude;
        double lat2 = latLng2.latitude;
        double lon2 = latLng2.longitude;

        Location.distanceBetween(lat1,lon1, lat2, lon2,result);
        return  result[0];
//        double dLat = lat2 - lat1;
//        double dLon = lon2 - lon1;
//        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
//                Math.cos(lat1) * Math.cos(lat2) *
//                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
//        //double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
//        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    public static void setTime(long time) {
        SharedData.time = time;
    }

    public static boolean isShowTutorial1() {
        boolean a = showTutorial1;
        showTutorial1 = false;
        return a;
    }

    public static boolean isShowTutorial2() {
        boolean a = showTutorial2;
        showTutorial2 = false;
        return a;
    }

    public static boolean isShowTutorial3() {
        boolean a = showTutorial3;
        showTutorial3 = false;
        return a;
    }

    public static String getRandomStr() {
        Random random = new Random();
        randomStr = random.nextInt(50000) + "";
        return randomStr;
    }

    public static boolean isShown() {
        return isShown;
    }

    public static void setIsShown(boolean isShown) {
        SharedData.isShown = isShown;
    }

    public static void pushContext(Context context) {
        mContexts.push(context);
    }

    public static Context peekContext() {
        if (mContexts.isEmpty())
            return null;
        else
            return mContexts.peek();
    }

    public static Context popContext() {
        if (mContexts.isEmpty())
            return null;
        else
            return mContexts.pop();
    }

    public static void clearContext() {
        mContexts = new Stack<>();
    }

    public static boolean isSplashed() {
        return isSplashed;
    }

    public static void setIsSplashed(boolean isSplashed) {
        SharedData.isSplashed = isSplashed;
    }

    public static boolean isSos() {
        return isSos;
    }

    public static void setIsSos(boolean isSos) {
        SharedData.isSos = isSos;
    }

    public static void clear() {
        startedService = false;
        route = null;
        onPath = true;
        mGroupId = null;
        clearSpeedData();
    }

    public static void clearSpeedData() {
        lastLatLng = null;
        distance = 0;
        time = 0;
    }

    public static List<String> getmGroupId() {
        return mGroupId;
    }

    public static void setmGroupId(List<String> mGroupId) {
        SharedData.mGroupId = mGroupId;
    }
}
