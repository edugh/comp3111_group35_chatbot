package com.example.bot.spring.model;

import java.sql.Date;
import java.sql.Timestamp;

public class DiscountSchedule {
    public final String planId;
    public final Date tourDate;
    public final Timestamp sendTime;

    public DiscountSchedule(
            String planId,
            Date tourDate,
            Timestamp sendTime
    ){
        this.planId = planId;
        this.tourDate = tourDate;
        this.sendTime = sendTime;
    }
}
