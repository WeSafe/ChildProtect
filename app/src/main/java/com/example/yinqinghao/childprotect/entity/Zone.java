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
    private LatLng latLng;
    private long radius;
    private String status;

    public Zone() {
    }

    public Zone(Date createDate, String name, LatLng latLng, long radius, String status) {
        this.createDate = createDate;
        this.name = name;
        this.latLng = latLng;
        this.radius = radius;
        this.status = status;
    }

    public Zone(Parcel in) {
        createDate = new Date(in.readLong());
        name = in.readString();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
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
        dest.writeParcelable(latLng,flags);
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

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
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
