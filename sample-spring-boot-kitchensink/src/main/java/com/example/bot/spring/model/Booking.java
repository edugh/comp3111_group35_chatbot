package com.example.bot.spring.model;

import java.math.BigDecimal;

public class Booking {
    public final String customerId;
    public final String planId;
    public final String tourDays;
    public final Integer adults;
    public final Integer children;
    public final Integer toddlers;
    public final BigDecimal fee;
    public final BigDecimal paid;
    public final String state;
    public final String specialRequest;

    public Booking(String customerId,
                   String planId,
                   String tourDays,
                   Integer adults,
                   Integer children,
                   Integer toddlers,
                   BigDecimal fee,
                   BigDecimal paid,
                   String state,
                   String specialRequest) {
        this.customerId = customerId;
        this.planId = planId;
        this.tourDays = tourDays;
        this.adults = adults;
        this.children = children;
        this.toddlers = toddlers;
        this.fee = fee;
        this.paid = paid;
        this.state = state;
        this.specialRequest = specialRequest;
    }
}
