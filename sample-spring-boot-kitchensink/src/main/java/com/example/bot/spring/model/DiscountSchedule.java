package com.example.bot.spring.model;

import java.sql.Date;
import java.time.ZonedDateTime;

public class DiscountSchedule {
    public final String planId;
    public final Date tourDate;
    public final ZonedDateTime sendTime;

    public DiscountSchedule(
            String planId,
            Date tourDate,
            ZonedDateTime sendTime
    ){
        this.planId = planId;
        this.tourDate = tourDate;
        this.sendTime = sendTime;
    }
}
