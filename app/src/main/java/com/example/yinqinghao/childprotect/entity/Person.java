package com.example.yinqinghao.childprotect.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by yinqinghao on 12/8/17.
 */

public class Person extends User {
    private String status;
    private Map<String,Map<String, LocationData>> locationDatas;

    public Person(String uid, String email, String firstName, String lastName, long phoneNumber, boolean isOnline, String status, Map<String, Map<String, LocationData>> locationDatas) {
        super(uid, email, firstName, lastName, phoneNumber, isOnline);
        this.status = status;
        this.locationDatas = locationDatas;
    }

    public Person(String uid, String email, String firstName, String lastName, long phoneNumber) {
        super(uid, email, firstName, lastName, phoneNumber, true);
        this.status = "normal";
        locationDatas = new HashMap<>();
    }

    public Person() {
    }

    public Person(Parcel in) {
        super(in);
        status = in.readString();
    }

    public static final Parcelable.Creator<Person> CREATOR = new Parcelable.Creator<Person> (){

        @Override
        public Person createFromParcel(Parcel source) {
            return new Person(source);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(status);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

//    public boolean isStop() {
//        return isStop;
//    }
//
//    public void setStop(boolean stop) {
//        isStop = stop;
//    }

//    public LocationData getMostRecentLocation() {
//        long today = getDatetime();
//        String key = today + "";
//        if (locationDatas.containsKey(today+"")) {
//            List<LocationData> locations = locationDatas.get(key);
//            sortLocations(locations);
//            return locations.get(0);
//        }
//        return null;
//
//    }

    public LocationData getMostRecentLocation() {
        long today = getDatetime();
        String key = today + "";
        if (locationDatas != null && locationDatas.containsKey(today+"")) {
            Map<String,LocationData> locations = locationDatas.get(key);
            String keyRecet = sortLocations(locations.keySet());
            return locations.get(keyRecet);
        }
        return null;
    }

//    public static void sortLocations(List<LocationData> locations) {
//        Collections.sort(locations, new Comparator<LocationData>() {
//            @Override
//            public int compare(LocationData o1, LocationData o2) {
//                return o1.getDatetime().after(o2.getDatetime()) ? -1 : 1;
//            }
//        });
//    }

    public static String sortLocations(Set<String> keys) {
        long max = 0;
        for (String key : keys) {
            long keyL = Long.parseLong(key);
            max = Math.max(max, keyL);
        }
        return max+"";
    }

    public static TreeMap<String, LocationData> sortLocationsAESC(Map<String,LocationData> locations) {
//        Collections.sort(locations, new Comparator<LocationData>() {
//            @Override
//            public int compare(LocationData o1, LocationData o2) {
//                return o1.getDatetime().after(o2.getDatetime()) ? 1 : -1;
//            }
//        });

//        long min = Long.MAX_VALUE;
//        for (String key : keys) {
//            long keyL = Long.parseLong(key);
//            min = Math.min(min, keyL);
//        }
//        return min+"";

        MyComparator comp = new MyComparator(locations);
        TreeMap<String, LocationData> res = new TreeMap(comp);
        res.putAll(locations);
        return res;
    }

    public static long getDatetime() {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        String date = format.format(now);
        Date temp = null;
        try {
            temp = format.parse(date);
        } catch (Exception ex) {

        }
        return temp.getTime();
    }

    public Map<String, Map<String, LocationData>> getLocationDatas() {
        if (locationDatas == null) {
            locationDatas = new HashMap<>();
        }
        return locationDatas;
    }

    public void setLocationDatas(Map<String, Map<String, LocationData>> locationDatas) {
        this.locationDatas = locationDatas;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    static class MyComparator implements Comparator {
        Map map;
        public MyComparator(Map map) {
            this.map = map;
        }

        public int compare(Object o1, Object o2) {
            return ((LocationData)map.get(o1)).getDatetime()
                    .after(((LocationData)map.get(o2)).getDatetime()) ? 1 : -1;
        }
    }
}
