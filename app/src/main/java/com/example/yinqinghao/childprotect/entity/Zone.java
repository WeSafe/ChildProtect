package com.example.yinqinghao.childprotect.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by yinqinghao on 12/8/17.
 */

public class Zone implements Parcelable{
    private Date createDate;
    private String name;
    private double lat;
    private double lng;
    private long radius;
    private String status;

    public Zone() {
    }

    public Zone(Date createDate, String name, double lat, double lng, long radius, String status) {
        this.createDate = createDate;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        this.status = status;
    }

    public Zone(Parcel in) {
        createDate = new Date(in.readLong());
        name = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        radius = in.readLong();
        status = in.readString();
    }

    public static final Parcelable.Creator<Zone> CREATOR = new Parcelable.Creator<Zone>() {

        @Override
        public Zone createFromParcel(Parcel source) {
            return new Zone(source);
        }

        @Override
        public Zone[] newArray(int size) {
            return new Zone[size];
        }
    };

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(createDate.getTime());
        dest.writeString(name);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeLong(radius);
        dest.writeString(status);
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public long getRadius() {
        return radius;
    }

    public void setRadius(long radius) {
        this.radius = radius;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }


}
