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

    @Override
    public String toString() {
        return "Plan{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", length=" + length +
                ", departure='" + departure + '\'' +
                ", weekdayPrice=" + weekdayPrice +
                ", weekendPrice=" + weekendPrice +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Plan plan = (Plan) o;

        if (length != plan.length) return false;
        if (id != null ? !id.equals(plan.id) : plan.id != null) return false;
        if (name != null ? !name.equals(plan.name) : plan.name != null) return false;
        if (shortDescription != null ? !shortDescription.equals(plan.shortDescription) : plan.shortDescription != null)
            return false;
        if (departure != null ? !departure.equals(plan.departure) : plan.departure != null) return false;
        return price != null ? price.equals(plan.price) : plan.price == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (shortDescription != null ? shortDescription.hashCode() : 0);
        result = 31 * result + length;
        result = 31 * result + (departure != null ? departure.hashCode() : 0);
        result = 31 * result + (price != null ? price.hashCode() : 0);
        return result;
    }
}
