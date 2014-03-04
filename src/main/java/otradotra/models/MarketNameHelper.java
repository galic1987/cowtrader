package otradotra.models;

public class MarketNameHelper {
public String ask;
public String bid;
public String url;
public double transactionFee;
public MarketNameHelper( String ask,String bid, String url, double transactionFee) {
	super();
	this.ask = ask;
	this.bid = bid;
	this.url = url;
	this.transactionFee = transactionFee;
}
}
