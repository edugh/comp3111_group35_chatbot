package com.example.bot.spring.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.sun.jmx.remote.util.OrderClassLoaders;

public class Customer {
    public final String id;
    public String name;
    public String gender;
    public int age;
    public String phoneNumber;
    public String tourJoined;
    public String tag;
    public Order order;

    public public Customer(String id) {
    	this.id = id;
	}


}