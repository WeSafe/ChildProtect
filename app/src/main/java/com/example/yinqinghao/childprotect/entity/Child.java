package com.example.yinqinghao.childprotect.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yinqinghao on 12/8/17.
 */

public class Child extends User {
    private String status;

    public Child(String uid, String email, String firstName, String lastName, long phoneNumber, boolean isOnline, String status) {
        super(uid, email, firstName, lastName, phoneNumber, isOnline);
        this.status = status;
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

    @Override
    public int describeContents() {
        return 0;
    }
}
