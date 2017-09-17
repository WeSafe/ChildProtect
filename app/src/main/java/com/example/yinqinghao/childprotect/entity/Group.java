package com.example.yinqinghao.childprotect.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yinqinghao on 12/8/17.
 */

public class Group implements Parcelable{
    private String id;
    private String name;
    private String userID;
    private Map<String, String> users;

    public Group() {
        users = new HashMap<>();
    }

    public Group(String id, String name, String userID, Map<String, String> users) {
        this.id = id;
        this.name = name;
        this.userID = userID;
        this.users = users;
    }

    public Group(Parcel in) {
        id = in.readString();
        name = in.readString();
        userID = in.readString();
        int size = in.readInt();
        users = new HashMap<>();
        for (int i =0; i < size; i ++) {
            String key = in.readString();
            String value = in.readString();
            users.put(key, value);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(userID);
        dest.writeInt(users.size());
        for (String key: users.keySet()) {
            String value = users.get(key);
            dest.writeString(key);
            dest.writeString(value);
        }
    }

    public static final Parcelable.Creator<Group> CREATOR = new Parcelable.Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel source) {
            return new Group(source);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getUsers() {
        return users;
    }

    public void setUsers(Map<String, String> users) {
        this.users = users;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
