package com.example.bot.spring.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.sun.jmx.remote.util.OrderClassLoaders;

public class Customer {
    public final String id;
    public final String name;
    public final String gender;
    public final int age;
    public final String phoneNumber;
    public final String tourJoined;
    public final String tag;
    //TODO(shuo)
    public public Customer(String id) {
    	this.id = id;
	}


}