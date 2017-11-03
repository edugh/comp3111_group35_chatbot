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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Booking booking = (Booking) o;

        if (customerId != null ? !customerId.equals(booking.customerId) : booking.customerId != null) return false;
        if (planId != null ? !planId.equals(booking.planId) : booking.planId != null) return false;
        if (tourDate != null ? !tourDate.equals(booking.tourDate) : booking.tourDate != null) return false;
        if (adults != null ? !adults.equals(booking.adults) : booking.adults != null) return false;
        if (children != null ? !children.equals(booking.children) : booking.children != null) return false;
        if (toddlers != null ? !toddlers.equals(booking.toddlers) : booking.toddlers != null) return false;
        if (fee != null ? !fee.equals(booking.fee) : booking.fee != null) return false;
        if (paid != null ? !paid.equals(booking.paid) : booking.paid != null) return false;
        return specialRequest != null ? specialRequest.equals(booking.specialRequest) : booking.specialRequest == null;
    }

    @Override
    public int hashCode() {
        int result = customerId != null ? customerId.hashCode() : 0;
        result = 31 * result + (planId != null ? planId.hashCode() : 0);
        result = 31 * result + (tourDate != null ? tourDate.hashCode() : 0);
        result = 31 * result + (adults != null ? adults.hashCode() : 0);
        result = 31 * result + (children != null ? children.hashCode() : 0);
        result = 31 * result + (toddlers != null ? toddlers.hashCode() : 0);
        result = 31 * result + (fee != null ? fee.hashCode() : 0);
        result = 31 * result + (paid != null ? paid.hashCode() : 0);
        result = 31 * result + (specialRequest != null ? specialRequest.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "Booking{" +
                "customerId='" + customerId + '\'' +
                ", planId='" + planId + '\'' +
                ", tourDate=" + tourDate +
                ", adults=" + adults +
                ", children=" + children +
                ", toddlers=" + toddlers +
                ", fee=" + fee +
                ", paid=" + paid +
                ", specialRequest='" + specialRequest + '\'' +
                '}';
    }
}
