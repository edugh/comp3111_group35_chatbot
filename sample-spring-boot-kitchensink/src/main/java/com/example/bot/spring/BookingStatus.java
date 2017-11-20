package com.example.bot.spring;

import com.example.bot.spring.model.Customer;
import com.example.bot.spring.model.Plan;
import com.example.bot.spring.model.Tour;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the booking status of customers on a particular tour.
 */
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class BookingStatus {
    /**
    The particular tour this booking status relates to.
     */
    public final Tour tour;
    /**
    The plan of this tour.
     */
    public final Plan plan;
    /**
    A list of customers who have booked the tour.
     */
    public final List<Customer> booked;

    /**
     * Determines whether a tour meets minimum participation quota
     * @return Whether or not the tour has enough people to be confirmed
     */
    public boolean isConfirmed() {
        return booked.size() >= tour.minimum;
    }
}