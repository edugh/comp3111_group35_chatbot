package com.example.bot.spring.model;

import java.sql.Date;

public class Tour {
    public String planId;
    public Date tourDate;
    public String guideName;
    public String guideAccount;
    public String hotel;
    public int capacity;
    public int minimum;

    public Tour(String planId, Date tourDate, String guideName, String guideAccount, String hotel, int capacity, int minimum) {
        this.planId = planId;
        this.tourDate = tourDate;
        this.guideName = guideName;
        this.guideAccount = guideAccount;
        this.hotel = hotel;
        this.capacity = capacity;
        this.minimum = minimum;
    }

    @Override
    public String toString() {
        return "Tour{" +
                "planId='" + planId + '\'' +
                ", tourDate=" + tourDate +
                ", guideName='" + guideName + '\'' +
                ", guideAccount='" + guideAccount + '\'' +
                ", hotel='" + hotel + '\'' +
                ", capacity=" + capacity +
                ", minimum=" + minimum +
                '}';
    }
}
