package com.example.bot.spring;

import java.math.BigDecimal;
import java.util.ArrayList;

import com.example.bot.spring.model.Booking;
import com.example.bot.spring.model.FAQ;
import com.example.bot.spring.model.Plan;
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

	ArrayList<Booking> getEnrolledTours(String customerId) {
		return null;
	}

	BigDecimal getAmmountOwed(String customerId) {
		return BigDecimal.ZERO;
	}
}
