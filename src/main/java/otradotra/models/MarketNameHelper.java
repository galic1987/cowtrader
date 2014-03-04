package otradotra.models;

public class MarketNameHelper {
public String ask;
public String bid;
public String url;
public double transactionFee;
public int maxDecimalPlaces;

public MarketNameHelper( String ask,String bid, String url, double transactionFee, int decimal) {
	super();
	this.ask = ask;
	this.bid = bid;
	this.url = url;
	this.transactionFee = transactionFee;
	this.maxDecimalPlaces =  decimal;
}
}
