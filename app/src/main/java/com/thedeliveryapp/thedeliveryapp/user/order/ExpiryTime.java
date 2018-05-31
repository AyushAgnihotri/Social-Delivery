package com.thedeliveryapp.thedeliveryapp.user.order;

public class ExpiryTime {
    public int hour = -1;
    public int minute;

    public  ExpiryTime() {

    }
    public ExpiryTime(int hour,int minute) {
        this.hour = hour;
        this.minute = minute;
    }
}
