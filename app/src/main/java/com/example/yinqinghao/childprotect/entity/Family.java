package com.example.yinqinghao.childprotect.entity;

import java.util.Date;
import java.util.List;

/**
 * Created by yinqinghao on 12/8/17.
 */

public class Family {
    private String id;
    private Date createDate;
    private List<Parent> parents;
    private List<Child> children;
    private List<Zone> zones;

    public Family() {
    }

    public Family(String id, Date createDate, List<Parent> parents, List<Child> children, List<Zone> zones) {
        this.id = id;
        this.createDate = createDate;
        this.parents = parents;
        this.children = children;
        this.zones = zones;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public List<Parent> getParents() {
        return parents;
    }

    public void setParents(List<Parent> parents) {
        this.parents = parents;
    }

    public List<Child> getChildren() {
        return children;
    }

    public void setChildren(List<Child> children) {
        this.children = children;
    }

    public List<Zone> getZones() {
        return zones;
    }

    public void setZones(List<Zone> zones) {
        this.zones = zones;
    }
}
