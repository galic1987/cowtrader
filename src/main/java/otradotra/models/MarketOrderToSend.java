package otradotra.models;


public class MarketOrderToSend {
	//pair 	Yes 	pair 	btc_usd (example) 	-
	//type 	Yes 	The transaction type 	buy or sell 	-
	//rate 	Yes 	The rate to buy/sell 	numerical 	-
	//amount 	Yes 	The amount which is necessary to buy/sell 	numerical 	-
	
	private String pair; // usd_btc
	private String type; // buy or sell
	private double rate; // lowest rate 
	private double amount; // volume
	
	private double total = 0; // actual amount for BTC

	// not to be send
	private Market market; // realMarketData
	private int orderDepth;
	
	
	public double recalculate() {
		double tempBuy = 0;
		if (market.type == MarketType.BID) {
			// bid
			tempBuy = (amount * rate)
					- ((amount * rate) * market.getTransactionFee());
		} else {
			// ask
			tempBuy = (amount / rate)
					- ((amount / rate) * market.getTransactionFee());
		}

		setTotal(tempBuy);
		return tempBuy;
	}
	
	public String getPair() {
		return pair;
	}
	public void setPair(String pair) {
		this.pair = pair;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public double getRate() {
		return rate;
	}
	public void setRate(double rate) {
		this.rate = rate;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public Market getMarket() {
		return market;
	}
	public void setMarket(Market market) {
		this.market = market;
	}
	public int getOrderDepth() {
		return orderDepth;
	}
	public void setOrderDepth(int orderDepth) {
		this.orderDepth = orderDepth;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}
}
