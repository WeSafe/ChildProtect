package com.example.yinqinghao.childprotect.entity;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by yinqinghao on 12/8/17.
 */

public class Group {
    private String id;
    private String name;
    private String userID;
    private Map<String, String> users;

    public Group() {
    }

    public Group(String id, String name, String userID, Map<String, String> users) {
        this.id = id;
        this.name = name;
        this.userID = userID;
        this.users = users;
    }

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
}
