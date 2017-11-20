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

@AllArgsConstructor
@EqualsAndHashCode
@ToString
/*
Represents the booking status of customers on a particular tour.
 */
public class BookingStatus {
    /*
    The particular tour this booking status relates to.
     */
    public final Tour tour;
    /*
    The plan of this tour.
     */
    public final Plan plan;
    /*
    A list of customers who have booked the tour.
     */
    public final ArrayList<Customer> booked;

    public boolean isConfirmed() {
        return booked.size() >= tour.minimum;
    }
}