package com.example.bot.spring.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.omg.CORBA.PUBLIC_MEMBER;

import com.sun.corba.se.pept.transport.Connection;
import com.sun.jmx.remote.util.OrderClassLoaders;

public class Order extends Ordering{
	public final double fee;
	public final double paid;
	
	public Order(String cid, String tid, int nA, int nC, int nT,
			double fee, double paid, String state="ordered", String sR=null) {
		this.custID = cid;
		this.TID = tid;
		this.nAdult = nA;
		this.nChild = nC;
		this.nToddler = nT;
		this.fee = fee;
		this.paid = paid;
		this.state = state;
		this.spclRqst = sR;	
		
	}
	
	 public boolean isFullPaid() {
		 if(this.paid >= this.fee)
			 return true;
		 else
			 return false;
	 }
	
}
