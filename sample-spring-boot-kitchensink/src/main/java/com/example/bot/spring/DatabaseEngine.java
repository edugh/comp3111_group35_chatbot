package com.example.bot.spring;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import com.example.bot.spring.model.Booking;
import com.example.bot.spring.model.Customer;
import com.example.bot.spring.model.FAQ;
import com.example.bot.spring.model.Plan;
import com.example.bot.spring.model.Tour;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatabaseEngine {
	String search(String text) throws Exception {
		String result = null;
		BufferedReader br = null;
		InputStreamReader isr = null;
		try {
			isr = new InputStreamReader(
                    this.getClass().getResourceAsStream(FILENAME));
			br = new BufferedReader(isr);
			String sCurrentLine;

			while (result == null && (sCurrentLine = br.readLine()) != null) {
				String[] parts = sCurrentLine.split(":");
				if (text.toLowerCase().equals(parts[0].toLowerCase())) {
					result = parts[1];
				}
			}
		} catch (IOException e) {
			log.info("IOException while reading file: {}", e.toString());
		} finally {
			try {
				if (br != null)
					br.close();
				if (isr != null)
					isr.close();
			} catch (IOException ex) {
				log.info("IOException while closing file: {}", ex.toString());
			}
		}
		if (result != null)
			return result;
		throw new Exception("NOT FOUND");
    }
	
	private final String FILENAME = "/static/database.txt";

	// TODO(Jason): decide better way to mock requests and use here
	ArrayList<Plan> getPlans() {
		return null;
	}

	ArrayList<FAQ> getFAQs() {
		return null;
	}

	ArrayList<Booking> getEnrolledTours(String customerId) {
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

    public Booking getCurrentBooking(String cid){ return null; }

    public void insertBooking(String cid, String pid){}
    public void updateBookingDate(String cid, String pid, Date date){}
    public void updateBooking(String cid, String pid, Date date, String field, String value){ }
    public void updateBooking(String cid, String pid, Date date, String field, int value){ }

    public Tour getTour(String pid, Date date){ return null;}
    public boolean isTourFull(String pid, Date date){ return false; }

}
