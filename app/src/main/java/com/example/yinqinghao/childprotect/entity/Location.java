package com.example.yinqinghao.childprotect.entity;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by yinqinghao on 12/8/17.
 */

public class Location {
    private Date datetime;
    private LatLng latLng;
    private String batteryStatus;

    public Location() {
    }

    public Location(Date datetime, LatLng latLng, String batteryStatus) {
        this.datetime = datetime;
        this.latLng = latLng;
        this.batteryStatus = batteryStatus;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(String batteryStatus) {
        this.batteryStatus = batteryStatus;
    }
}
