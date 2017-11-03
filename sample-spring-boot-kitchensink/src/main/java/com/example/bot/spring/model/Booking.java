package com.example.bot.spring.model;

import java.math.BigDecimal;
import java.sql.Date;

public class Booking {
    public final String customerId;
    public final String planId;
    public final Date tourDate;
    public final Integer adults;
    public final Integer children;
    public final Integer toddlers;
    public final BigDecimal fee;
    public final BigDecimal paid;
    public final String specialRequest;

    public Booking(String customerId,
                   String planId,
                   Date tourDate,
                   Integer adults,
                   Integer children,
                   Integer toddlers,
                   BigDecimal fee,
                   BigDecimal paid,
                   String specialRequest) {
        this.customerId = customerId;
        this.planId = planId;
        this.tourDate = tourDate;
        this.adults = adults;
        this.children = children;
        this.toddlers = toddlers;
        this.fee = fee;
        this.paid = paid;
        this.specialRequest = specialRequest;
    }
}
