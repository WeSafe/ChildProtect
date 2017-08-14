package com.example.yinqinghao.childprotect.entity;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by yinqinghao on 12/8/17.
 */

public class LocationData {
    private Date datetime;
    private double lat;
    private double lng;
    private int batteryStatus;

    public LocationData() {
    }

    public LocationData(Date datetime, double lat, double lng, int batteryStatus) {
        this.datetime = datetime;
        this.lat = lat;
        this.lng = lng;
        this.batteryStatus = batteryStatus;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }


    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public int getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(int batteryStatus) {
        this.batteryStatus = batteryStatus;
    }
}
