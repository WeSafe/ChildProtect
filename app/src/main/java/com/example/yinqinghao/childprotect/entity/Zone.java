package com.example.yinqinghao.childprotect.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by yinqinghao on 12/8/17.
 */

public class Zone implements Parcelable{
    private Date createDate;
    private String des;
    private double lat;
    private double lng;
    private long radius;
    private String status;
    private String id;
    private String gid;

    public Zone() {
    }

    public Zone(Date createDate, String des, double lat, double lng, long radius, String status, String id, String gid) {
        this.createDate = createDate;
        this.des = des;
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        this.status = status;
        this.id = id;
        this.gid = gid;
    }

    public Zone(Parcel in) {
        createDate = new Date(in.readLong());
        des = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        radius = in.readLong();
        status = in.readString();
        id = in.readString();
        gid = in.readString();
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
        dest.writeString(des);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeLong(radius);
        dest.writeString(status);
        dest.writeString(id);
        dest.writeString(gid);
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    @Override
    public int describeContents() {
        return 0;
    }


}
