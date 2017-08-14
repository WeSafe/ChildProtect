package com.example.yinqinghao.childprotect.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * Created by yinqinghao on 12/8/17.
 */

public class Child extends User {
    private String status;
    private Map<String,LocationData> locationDatas;

    public Child(String uid, String email, String firstName, String lastName, long phoneNumber, boolean isOnline, String status) {
        super(uid, email, firstName, lastName, phoneNumber, isOnline);
        this.status = status;
    }

    public Child(String uid, String email, String firstName, String lastName, long phoneNumber, boolean isOnline, String status, Map<String,LocationData> locationDatas) {
        super(uid, email, firstName, lastName, phoneNumber, isOnline);
        this.status = status;
        this.locationDatas = locationDatas;
    }

    public Child() {
    }

    public Child(Parcel in) {
        super(in);
        status = in.readString();
    }

    public static final Parcelable.Creator<Child> CREATOR = new Parcelable.Creator<Child> (){

        @Override
        public Child createFromParcel(Parcel source) {
            return new Child(source);
        }

        @Override
        public Child[] newArray(int size) {
            return new Child[size];
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

    public Map<String,LocationData> getLocationDatas() {
        return locationDatas;
    }

    public LocationData getMostRecentLocation() {
        Long maxKey = null;
        Long temp = null;
        for (String k: locationDatas.keySet()) {
            if (maxKey == null)
                maxKey = Long.parseLong(k);
            temp = Long.parseLong(k);
            maxKey = Math.max(temp,maxKey);
        }
        return locationDatas.get(maxKey.toString());
    }

    public void setLocationDatas(Map<String,LocationData> locationDatas) {
        this.locationDatas = locationDatas;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
