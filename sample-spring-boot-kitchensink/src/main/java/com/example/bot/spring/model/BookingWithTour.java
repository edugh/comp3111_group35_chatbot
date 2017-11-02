package com.example.bot.spring.model;

import java.sql.Date;

public class BookingWithTour {
    public final Date tourDate;
    public final String planName;
    public final String planDescription;

    public BookingWithTour(Date tourDate, String planName, String planDescription) {
        this.tourDate = tourDate;
        this.planName = planName;
        this.planDescription = planDescription;
    }
}
