package com.example.yinqinghao.childprotect.entity;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by yinqinghao on 12/8/17.
 */

public class Notification {
    private Date datetime;
    private String childId;
    private LatLng latLng;
    private String msg;

    public Notification() {
    }

    public Notification(Date datetime, String childId, LatLng latLng, String msg) {
        this.datetime = datetime;
        this.childId = childId;
        this.latLng = latLng;
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

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
