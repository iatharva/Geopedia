package com.example.geopedia.extras;

public class Events {

    // unique IDs
    private String eventId;
    private String userId;

    private Double eventLatitude;
    private Double eventLongitude;

    //details
    private String eventTitle;
    private String eventDesc;
    private String eventAddr;
    private String eventType;
    private String eventRecurringOption;
    private String eventDate;
    private String eventTime;

    //additional details
    private String eventStatus;
    private String date;
    private String time;
    private String isDeleted;
    private String isActive;

    public Events(){}

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getEventLatitude() {
        return eventLatitude;
    }

    public void setEventLatitude(Double eventLatitude) {
        this.eventLatitude = eventLatitude;
    }

    public Double getEventLongitude() {
        return eventLongitude;
    }

    public void setEventLongitude(Double eventLongitude) {
        this.eventLongitude = eventLongitude;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventDesc() {
        return eventDesc;
    }

    public void setEventDesc(String eventDesc) {
        this.eventDesc = eventDesc;
    }

    public String getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(String eventStatus) {
        this.eventStatus = eventStatus;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEventAddr() {
        return eventAddr;
    }

    public void setEventAddr(String eventAddr) {
        this.eventAddr = eventAddr;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventRecurringOption() {
        return eventRecurringOption;
    }

    public void setEventRecurringOption(String eventRecurringOption) {
        this.eventRecurringOption = eventRecurringOption;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public String getIsActive() {
        return isActive;
    }


}
