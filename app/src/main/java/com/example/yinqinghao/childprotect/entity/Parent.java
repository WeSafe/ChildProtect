package com.example.yinqinghao.childprotect.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yinqinghao on 12/8/17.
 */

public class Parent extends User {

    public Parent(String uid, String email, String firstName, String lastName, long phoneNumber, boolean isOnline) {
        super(uid, email, firstName, lastName, phoneNumber, isOnline);
    }

    public Parent() {
    }

    public Parent(Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator<Parent> CREATOR = new Parcelable.Creator<Parent>() {

        @Override
        public Parent createFromParcel(Parcel source) {
            return new Parent(source);
        }

        /**
         * Create a new array of the Parcelable class.
         *
         * @param size Size of the array.
         * @return Returns an array of the Parcelable class, with every entry
         * initialized to null.
         */
        @Override
        public Parent[] newArray(int size) {
            return new Parent[size];
        }
    };

    public int describeContents() {
        return 0;
    }
}
