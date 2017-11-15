package com.example.bot.spring;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.sql.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.example.bot.spring.model.*;
import lombok.extern.slf4j.Slf4j;

import javax.swing.text.html.Option;
import javax.validation.constraints.NotNull;

@Slf4j
abstract class DatabaseEngine {
    abstract public Connection getConnection();

    // Get
    abstract public Optional<Booking> getCurrentBooking(String cid);
    abstract public ArrayList<Booking> getBookings(String customerId);
    abstract public Optional<Customer> getCustomer(String cid);
    abstract public ArrayList<Dialogue> getDialogues(String cid);
    abstract public Optional<FAQ> getFAQ(String questionId);
    abstract public Optional<Plan> getPlan(String pid);
    abstract public ArrayList<Plan> getPlans(); //TODO: Perhaps take a date range?
    abstract public ArrayList<Tag> getTags(String cid);
    abstract public Optional<Tour> getTour(String pid, Date date);

    // Other get
    abstract public BigDecimal getAmountOwed(String customerId);
    abstract public boolean isTourFull(String pid, Date date);

    // Put
    abstract public void insertBooking(String cid, String pid);
    abstract public void insertCustomer(String cid);
    abstract public void insertDialogue(Dialogue dlg);
    // abstract public void insertFAQ(FAQ);
    // abstract public void insertPlan(Plan);
    abstract public void insertTag(Tag tag);
    // abstract public void insertTour(Tour)

    // Update
    abstract public void updateBooking(String cid, String pid, Date date, String field, String value); //TODO: what is 'value'?
    abstract public void updateBooking(String cid, String pid, Date date, String field, int value); // TODO: what is 'value'? why is it int now?
    abstract public void updateBookingDate(String cid, String pid, Date date);
    abstract public void updateBooking(String cid, String pid, Date date, String field, BigDecimal value);
    abstract public void updateCustomerState(String cid, String state);
    abstract public void updateCustomer(String cid, String field, String value);
    abstract public void updateCustomer(String cid, String field, int value);
    abstract public void updateCustomer(String cid, String field, BigDecimal value);

}
