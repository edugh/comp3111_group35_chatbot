package com.example.bot.spring.model;

import java.math.BigDecimal;

public class Plan {
    public final String id;
    public final String name;
    public final String shortDescription;
    public final int length;
    public final String departure;
    public final BigDecimal weekdayPrice;
    public final BigDecimal weekendPrice;

    public Plan(String id,
                String name,
                String shortDescription,
                int length,
                String departure,
                BigDecimal price1,
                BigDecimal price2) {
        this.id = id;
        this.name = name;
        this.shortDescription = shortDescription;
        this.length = length;
        this.departure = departure;
        this.weekdayPrice = price1;
        this.weekendPrice = price2;
    }
}
