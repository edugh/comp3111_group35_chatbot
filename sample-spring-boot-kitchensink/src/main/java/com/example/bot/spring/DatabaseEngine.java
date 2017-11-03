package com.example.bot.spring;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;

import com.example.bot.spring.model.Booking;
import com.example.bot.spring.model.Customer;
import com.example.bot.spring.model.FAQ;
import com.example.bot.spring.model.Plan;
import com.example.bot.spring.model.Tour;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatabaseEngine {
	// TODO(Jason): decide better way to mock requests and use here
	ArrayList<Plan> getPlans() {
		return null;
	}

	ArrayList<FAQ> getFAQs() {
		return null;
	}

    ArrayList<Booking> getBookings(String customerId) {
        return null;
    }

	BigDecimal getAmmountOwed(String customerId) {
		return BigDecimal.ZERO;
	}

	//TODO(Shuo): workflow functions
	public Customer getCustomer(String cid){ return null; }
	public void insertCustomer(String cid){ }
    public void updateCustomerState(String cid, String state){ }
    public void updateCustomer(String cid, String field, String value){ }
    public void updateCustomer(String cid, String field, int value){ }
    public void updateCustomer(String cid, String field, BigDecimal value) { }

    public Booking getCurrentBooking(String cid){ return null; }

    public void insertBooking(String cid, String pid){}
    public void updateBookingDate(String cid, String pid, Date date){}
    public void updateBooking(String cid, String pid, Date date, String field, String value){ }
    public void updateBooking(String cid, String pid, Date date, String field, int value){ }
    public void updateBooking(String cid, String pid, Date date, String field, BigDecimal value){ }

    public Tour getTour(String pid, Date date){ return null; }
    public Plan getPlan(String pid){ return null; }
    public boolean isTourFull(String pid, Date date){ return false; }

}
