package com.example.yinqinghao.childprotect.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yinqinghao on 12/8/17.
 */

public abstract class User implements Parcelable {
    private String uid;
    private String email;
    private String firstName;
    private String lastName;
    private long phoneNumber;
    private boolean isOnline;

    protected User() {
    }

    protected User(String uid, String email, String firstName, String lastName, long phoneNumber, boolean isOnline) {
        this.uid = uid;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.isOnline = isOnline;
    }

    protected User(Parcel in) {
        uid = in.readString();
        email = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        phoneNumber = in.readLong();
        isOnline = in.readByte() == 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(email);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeLong(phoneNumber);
        dest.writeByte((byte) (isOnline ? 0 : 1));
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
