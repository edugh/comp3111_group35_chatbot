package com.example.bot.spring.model;

import java.sql.Time;
import java.time.ZonedDateTime;

public class Message {
    public final String customerId;
    public final ZonedDateTime sendTime;
    public final String content;
    
    public Message(
    		String customerId,
    		ZonedDateTime sendTime,
    		String content
    		) {
    	this.customerId = customerId;
    	this.sendTime = sendTime;
    	this.content = content;
    }
    
}
