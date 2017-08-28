package com.example.yinqinghao.childprotect.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by yinqinghao on 26/8/17.
 */

public class Route implements Parcelable {
    private Date createDate;
    private String des;
    private String points;
    private String id;
    private int distance;
    private int duration;
    private String mode;
    private String latlngs;

    public Route() {
    }

    public Route(Date createDate, String des, String points, String id, int distance, int duration, String mode, String latlngs) {
        this.createDate = createDate;
        this.des = des;
        this.points = points;
        this.id = id;
        this.distance = distance;
        this.duration = duration;
        this.mode = mode;
        this.latlngs = latlngs;
    }

    public Route(Parcel in) {
        createDate = new Date(in.readLong());
        des = in.readString();
        points = in.readString();
        id = in.readString();
        distance = in.readInt();
        duration = in.readInt();
        mode = in.readString();
        latlngs = in.readString();
    }

    public static final Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>() {

        @Override
        public Route createFromParcel(Parcel source) {
            return new Route(source);
        }

        @Override
        public Route[] newArray(int size) {
            return new Route[size];
        }
    };

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(createDate.getTime());
        dest.writeString(des);
        dest.writeString(points);
        dest.writeString(id);
        dest.writeInt(distance);
        dest.writeInt(duration);
        dest.writeString(mode);
        dest.writeString(latlngs);
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

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getLatlngs() {
        return latlngs;
    }

    public void setLatlngs(String latlngs) {
        this.latlngs = latlngs;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
