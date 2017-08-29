package com.example.yinqinghao.childprotect.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yinqinghao on 29/8/17.
 */

public class SharedData {
    private static boolean startedService = false;
    private static Route route;
    private static boolean onPath = true;
    private static Map<String, List<String>> tokens = new HashMap<>();
    private static Map<String, Person> user = new HashMap<>();
    private static Map<String, Map<String, Zone>> zones = new HashMap<>();

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

    public static Map<String, List<String>> getTokens() {
        return tokens;
    }

    public static void setTokens(Map<String, List<String>> tokens) {
        SharedData.tokens = tokens;
    }

    public static Map<String, Person> getUser() {
        return user;
    }

    public static void setUser(Map<String, Person> user) {
        SharedData.user = user;
    }

    public static void addUser(String uid, Person me) {
        user.put(uid,me);
    }

    public static void addToken(String gid, List<String> t) {
        tokens.put(gid,t);
    }

    public static Map<String, Map<String, Zone>> getZones() {
        return zones;
    }

    public static void setZones(Map<String, Map<String, Zone>> zones) {
        SharedData.zones = zones;
    }

    public static void addZone(String gid, Map<String, Zone> z) {
        zones.put(gid,z);
    }

    public static void clear() {
        startedService = false;
        route = null;
        onPath = true;
        tokens = new HashMap<>();
        user = new HashMap<>();
        zones = new HashMap<>();
    }
}
