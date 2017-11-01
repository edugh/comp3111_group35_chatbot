import java.util.function.Predicate;

import com.sun.corba.se.spi.orbutil.fsm.State;

public class Ordering {
	public final String custID;
	public final String tourID;
	public final int nAdult;
	public final int nChild;
	public final int nToddler;
	public final String state;
	public final String spclRqst;
	
	public Ordering(String cid, String tid, int nA, int nC, int nT,
			double fee, String state="new", String sR=null) {
		this.custID = cid;
		this.TID = tid;
		this.nAdult = nA;
		this.nChild = nC;
		this.nToddler = nT;
		this.fee = fee;
		this.paid = 0;
		this.state = state;
		this.spclRqst = sR;
	}
	
	public double calFee(double price) {
		//Not sure the child price
		double res = 0;
		double feeAdult = price;
		double feeChild = price/2;
		double feeToddler =0;
		//will not change this.fee, invoked when update ordering
		res = this.nAdult*feeAdult + this.nChild*feeChild + this.nToddler*feeToddler;
		return res;
	}
	
}
