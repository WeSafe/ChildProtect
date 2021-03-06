package com.example.yinqinghao.childprotect.entity;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by yinqinghao on 12/8/17.
 */

public class Notification {
    private Date datetime;
    private String childId;
    private double lat;
    private double lng;
    private String msg;

    public Notification() {
    }

    public Notification(Date datetime, String childId, double lat, double lng, String msg) {
        this.datetime = datetime;
        this.childId = childId;
        this.lat = lat;
        this.lng = lng;
        this.msg = msg;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
