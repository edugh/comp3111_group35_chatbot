package com.example.bot.spring.model;

public class Tour {
    public String planId;
    public String tourDays;
    public String guideName;
    public String guideAccount;
    public String hotel;
    public int capacity;
    public int minimum;

    public Tour(String planId, String tourDays, String guideName, String guideAccount, String hotel, int capacity, int minimum) {
        this.planId = planId;
        this.tourDays = tourDays;
        this.guideName = guideName;
        this.guideAccount = guideAccount;
        this.hotel = hotel;
        this.capacity = capacity;
        this.minimum = minimum;
    }
}
