package com.example.yinqinghao.childprotect.entity;

import com.google.android.gms.maps.model.Polyline;

/**
 * Created by yinqinghao on 2/9/17.
 */

public class SafeHouse {
    private String roadType;
    private double lng;
    private String distance;
    private String road;
    private String streetNum;
    private int postcode;
    private int id;
    private String state;
    private String placeName;
    private String oType;
    private double lat;
    private double dis;
    private Polyline polyline;

    public SafeHouse() {
    }

    public SafeHouse(String roadType, double lng, String distance, String road, String streetNum, int postcode, int id, String state, String placeName, String oType, double lat, double dis, Polyline polyline) {
        this.roadType = roadType;
        this.lng = lng;
        this.distance = distance;
        this.road = road;
        this.streetNum = streetNum;
        this.postcode = postcode;
        this.id = id;
        this.state = state;
        this.placeName = placeName;
        this.oType = oType;
        this.lat = lat;
        this.dis = dis;
        this.polyline = polyline;
    }

    public String getRoadType() {
        return roadType;
    }

    public void setRoadType(String roadType) {
        this.roadType = roadType;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getRoad() {
        return road;
    }

    public void setRoad(String road) {
        this.road = road;
    }

    public String getStreetNum() {
        return streetNum;
    }

    public void setStreetNum(String streetNum) {
        this.streetNum = streetNum;
    }

    public int getPostcode() {
        return postcode;
    }

    public void setPostcode(int postcode) {
        this.postcode = postcode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getoType() {
        return oType;
    }

    public void setoType(String oType) {
        this.oType = oType;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getDis() {
        return dis;
    }

    public void setDis(double dis) {
        this.dis = dis;
    }

    public Polyline getPolyline() {
        return polyline;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }
}
