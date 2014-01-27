package otradotra;

import java.util.Date;




public class Market {

	private Date gotRequestFromServerDate;
	
	// parsed date
	Date date;
	
	// type
	MarketType type;
	
	// list of orders volumen | price | total
	MarketOrder[] orders;
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

// returns best order bid-Lowest & ask-Highest
MarketOrder getMeBestOrder(){
	// sorted ASK & BID
	return orders[0];
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

public MarketOrder[] getOrders() {
	return orders;
}

public void setOrders(MarketOrder[] orders) {
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

// TODO:Needed 
// 1. Compute price for certain volume 
// to get overview of possible thru put

// 2. Compute price for 



}
