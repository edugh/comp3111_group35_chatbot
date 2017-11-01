import com.sun.corba.se.spi.orbutil.fsm.State;

public class Ordering extends Order {
	public final String state;
	
	public Ordering(String cid, String tid, int nA, int nC, int nT,
			double fee, double paid, String state, String sR=null) {
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
	
}
