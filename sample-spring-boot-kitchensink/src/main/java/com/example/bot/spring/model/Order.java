package com.example.bot.spring.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.omg.CORBA.PUBLIC_MEMBER;

import com.sun.jmx.remote.util.OrderClassLoaders;

public class Order {
	public final String custID;
	public final String tourid;
	public int nAdult;
	public int nChild;
	public int nToodler;
	public double fee;
	public double paid;
	public double spclRqst;
	
	public Tour tour;
	
	public Order(String cid, String tid, int nA=0, int nC=0, int nT=0, double fee=0, double paid=0, String sR=null) {
		this.custID = cid;
		this.custID = tid;
		this.nAdult = nA;
		this.nChild = nC;
		this.nToodler = nT;
		this.fee = fee;
		this.paid = paid;
		this.spclRqst = sR;	
		//TODO: grab tour with slice of tourid(id+date)
		this.tour = new Tour()
		
	}
	
	public double calFee() {
		//Not sure the child price
		double feeAdult = tour.price;
		double feeChild = tour.price/2;
		double feeToodler =0;
		this.fee = this.nAdult*feeAdult + this.nChild*feeChild + this.nToodler*feeToodler;
		return fee;
	}
	
	 public boolean isFullPaid() {
		 if(this.paid >= this.fee)
			 return true;
		 else
			 return false;
	 }
	
	
	
	
}
