package otradotra.models;

import java.text.DecimalFormat;
import java.util.Date;




public class Market {

	private Date gotRequestFromServerDate;
	
	// parsed date
	Date date;
	
	// type
	MarketType type;
	
	// list of orders volumen | price | total
    MarketOrderDataHolder[] orders;
	String marketName;
	
	// string BTC -> USD  or USD -> BTC
	String from;
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	String to;
	
	double transactionFee;

	
	private  DecimalFormat priceFormat = new DecimalFormat();

	private int priceDecimalPrecision = 3;
// returns best order bid-Lowest & ask-Highest
MarketOrderDataHolder getMeBestOrder(){
	// sorted ASK & BID
	return orders[0];
}



// get inverse sell-ask buy-bid  for trader
public String getMeTheType(){
	// inverse to make 
	if(type == MarketType.BID){
		return "sell";
	}else{
		return "buy";
	}
}

public Date getDate() {
	return date;
}

public void setDate(Date date) {
	this.date = date;
}

public MarketType getType() {
	return type;
}

public void setType(MarketType type) {
	this.type = type;
}

public MarketOrderDataHolder[] getOrders() {
	return orders;
}

public void setOrders(MarketOrderDataHolder[] orders) {
	this.orders = orders;
}

public String getMarketName() {
	return marketName;
}

public void setMarketName(String marketName) {
	this.marketName = marketName;
}

public double getTransactionFee() {
	return transactionFee;
}

public void setTransactionFee(double transactionFee) {
	this.transactionFee = transactionFee;
}

public Date getGotRequestFromServerDate() {
	return gotRequestFromServerDate;
}

public void setGotRequestFromServerDate(Date gotRequestFromServerDate) {
	this.gotRequestFromServerDate = gotRequestFromServerDate;
}

public DecimalFormat getPriceFormat() {
	return priceFormat;
}

public void setPriceFormat(DecimalFormat priceFormat) {
	this.priceFormat = priceFormat;
}

public int getPriceDecimalPrecision() {
	return priceDecimalPrecision;
}

public void setPriceDecimalPrecision(int priceDecimalPrecision) {
	this.priceDecimalPrecision = priceDecimalPrecision;
	priceFormat.setMaximumFractionDigits(priceDecimalPrecision);
}

// TODO:Needed 
// 1. Compute price for certain volume 
// to get overview of possible thru put

// 2. Compute price for 



}
