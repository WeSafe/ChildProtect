package com.example.yinqinghao.childprotect.entity;

import java.util.Date;

/**
 * Created by yinqinghao on 18/9/17.
 */

public class Feedback {
    private Date datetime;
    private int functionScore;
    private int designScore;
    private int easeOfUseScore;
    private int overallScore;
    private String comments;
    private String uid;
    private String email;

    public Feedback() {
    }

    public Feedback(Date datetime, int functionScore, int designScore, int easeOfUseScore, int overallScore, String comments, String uid, String email) {
        this.datetime = datetime;
        this.functionScore = functionScore;
        this.designScore = designScore;
        this.easeOfUseScore = easeOfUseScore;
        this.overallScore = overallScore;
        this.comments = comments;
        this.uid = uid;
        this.email = email;
    }

    public Feedback(Date datetime, int functionScore, int designScore, int easeOfUseScore, int overallScore, String comments, String uid) {
        this.datetime = datetime;
        this.functionScore = functionScore;
        this.designScore = designScore;
        this.easeOfUseScore = easeOfUseScore;
        this.overallScore = overallScore;
        this.comments = comments;
        this.uid = uid;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public int getFunctionScore() {
        return functionScore;
    }

    public void setFunctionScore(int functionScore) {
        this.functionScore = functionScore;
    }

    public int getDesignScore() {
        return designScore;
    }

    public void setDesignScore(int designScore) {
        this.designScore = designScore;
    }

    public int getEaseOfUseScore() {
        return easeOfUseScore;
    }

    public void setEaseOfUseScore(int easeOfUseScore) {
        this.easeOfUseScore = easeOfUseScore;
    }

    public int getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(int overallScore) {
        this.overallScore = overallScore;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
