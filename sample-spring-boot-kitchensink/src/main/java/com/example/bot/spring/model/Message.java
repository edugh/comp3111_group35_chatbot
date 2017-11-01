package com.example.bot.spring.model;

import java.sql.Time;

import javax.swing.text.AbstractDocument.Content;

import com.sun.jmx.snmp.Timestamp;

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
