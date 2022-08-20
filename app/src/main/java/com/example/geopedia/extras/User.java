package com.example.geopedia.extras;

public class User {

    //unique IDs
    private String Uid;

    //Profile string details
    private String FName;
    private String LName;
    private String Email;
    private String Dob;
    private String JoinedOn;

    //Other parameters
    private String IsAdmin;
    private String IsPaid;

    //Last known location
    private double LastLongitude;
    private double LastLatitude;

    public User(){ }

    public User(String uid, String FName, String LName, String email, String dob, String isAdmin, String isPaid, double lastLongitude, double lastLatitude, String joinedOn) {
        this.Uid = uid;
        this.FName = FName;
        this.LName = LName;
        this.Email = email;
        this.Dob = dob;
        this.IsAdmin = isAdmin;
        this.IsPaid = isPaid;
        this.LastLatitude = lastLatitude;
        this.LastLongitude = lastLongitude;
        this.JoinedOn = joinedOn;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getFName() {
        return FName;
    }

    public void setFName(String FName) {
        this.FName = FName;
    }

    public String getLName() {
        return LName;
    }

    public void setLName(String LName) {
        this.LName = LName;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getDob() {
        return Dob;
    }

    public void setDob(String dob) {
        Dob = dob;
    }

    public String getIsAdmin() {
        return IsAdmin;
    }

    public void setIsAdmin(String isAdmin) {
        IsAdmin = isAdmin;
    }

    public String getIsPaid() {
        return IsPaid;
    }

    public void setIsPaid(String isPaid) {
        IsPaid = isPaid;
    }

    public double getLastLongitude() {
        return LastLongitude;
    }

    public void setLastLongitude(double lastLongitude) {
        LastLongitude = lastLongitude;
    }

    public double getLastLatitude() {
        return LastLatitude;
    }

    public void setLastLatitude(double lastLatitude) {
        LastLatitude = lastLatitude;
    }

    public String getJoinedOn() {
        return JoinedOn;
    }

    public void setJoinedOn(String joinedOn) {
        JoinedOn = joinedOn;
    }

}
