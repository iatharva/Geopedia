package com.example.geopedia.extras;

public class User {

    //unique IDs
    private String Uid;

    //Profile string details
    private String FName;
    private String LName;
    private String Email;
    private String Dob;

    //Other parameters
    private String IsAdmin;
    private String IsPaid;

    public User(){ }

    public User(String uid, String FName, String LName, String email, String dob, String isAdmin, String isPaid) {
        this.Uid = uid;
        this.FName = FName;
        this.LName = LName;
        this.Email = email;
        this.Dob = dob;
        this.IsAdmin = isAdmin;
        this.IsPaid = isPaid;
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

}
