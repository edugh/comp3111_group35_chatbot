package com.example.bot.spring.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.omg.CORBA.PUBLIC_MEMBER;

import com.sun.corba.se.pept.transport.Connection;
import com.sun.jmx.remote.util.OrderClassLoaders;

public class Order {
	public final String custID;
	public final String tourID;
	public final int nAdult;
	public final int nChild;
	public final int nToddler;
	public final double fee;
	public final double paid;
	public final String spclRqst;
	
	public Order(String cid, String tid, int nA, int nC, int nT, double fee, double paid, String sR=null) {
		this.custID = cid;
		this.TID = tid;
		this.nAdult = nA;
		this.nChild = nC;
		this.nToddler = nT;
		this.fee = fee;
		this.paid = paid;
		this.spclRqst = sR;	
		
	}
	
	public double calFee() {
		//Not sure the child price
		double res = 0;
		double feeAdult = tour.price;
		double feeChild = tour.price/2;
		double feeToodler =0;
		//will not change this.fee, invoked when update ordering
		res = this.nAdult*feeAdult + this.nChild*feeChild + this.nToodler*feeToodler;
		return res;
	}
	
	 public boolean isFullPaid() {
		 if(this.paid >= this.fee)
			 return true;
		 else
			 return false;
	 }
	
	
	
	
}
