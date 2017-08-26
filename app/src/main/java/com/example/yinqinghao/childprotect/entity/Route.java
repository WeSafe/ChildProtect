package com.example.yinqinghao.childprotect.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by yinqinghao on 26/8/17.
 */

public class Route implements Parcelable {
    private Date createDate;
    private String des;
    private String points;
    private String id;

    public Route() {
    }

    public Route(Date createDate, String des, String points, String id) {
        this.createDate = createDate;
        this.des = des;
        this.points = points;
        this.id = id;
    }

    public Route(Parcel in) {
        createDate = new Date(in.readLong());
        des = in.readString();
        points = in.readString();
        id = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }
}
