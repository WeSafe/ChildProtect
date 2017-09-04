package com.example.yinqinghao.childprotect.entity;

import android.content.Context;
import android.util.Log;

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
    private static boolean showTutorial1 = true;
    private static boolean showTutorial2 = true;
    private static boolean showTutorial3 = true;
    private static boolean isShown = false;
    private static boolean isSos = false;
    private static boolean isSplashed = false;
    private static Stack<Context> mContexts = new Stack<>();
//    private static Map<String, List<String>> tokens = new HashMap<>();
//    private static Map<String, Person> user = new HashMap<>();
//    private static Map<String, Map<String, Zone>> zones = new HashMap<>();

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

//    public static Map<String, List<String>> getTokens() {
//        return tokens;
////    }
//
//    public static void setTokens(Map<String, List<String>> tokens) {
//        SharedData.tokens = tokens;
//    }
//
//    public static Map<String, Person> getUser() {
//        return user;
//    }
//
//    public static void setUser(Map<String, Person> user) {
//        SharedData.user = user;
//    }
//
//    public static void addUser(String uid, Person me) {
//        user.put(uid,me);
//    }
//
//    public static void addToken(String gid, List<String> t) {
//        tokens.put(gid,t);
//    }

//    public static Map<String, Map<String, Zone>> getZones() {
//        return zones;
//    }

//    public static void setZones(Map<String, Map<String, Zone>> zones) {
//        SharedData.zones = zones;
//    }
//
//    public static void addZone(String gid, Map<String, Zone> z) {
//        zones.put(gid,z);
//    }


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
//        tokens = new HashMap<>();
//        user = new HashMap<>();
//        zones = new HashMap<>();
    }
}
