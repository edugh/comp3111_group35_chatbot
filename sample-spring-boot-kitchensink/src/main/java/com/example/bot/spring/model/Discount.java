package com.example.bot.spring.model;

import java.sql.Date;

public class Discount {
    public final String customerId;
    public final String planId;
    public final Date tourDate;
    public final Integer seats;

    public Discount(
            String customerId,
            String planId,
            Date tourDate,
            Integer seats
    ){
        this.customerId = customerId;
        this.planId = planId;
        this.tourDate = tourDate;
        this.seats = seats;
    }
}
