package com.example.bot.spring.model;

import java.sql.Timestamp;

public class Message {
    public final String customerId;
    public final Timestamp sendTime;
    public final String content;
    
    public Message(
    		String customerId,
    		Timestamp sendTime,
    		String content
    		) {
    	this.customerId = customerId;
    	this.sendTime = sendTime;
    	this.content = content;
    	
    }
    
}
