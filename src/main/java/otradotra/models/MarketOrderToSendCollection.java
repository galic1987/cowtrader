package otradotra.models;

import java.util.ArrayList;

public class MarketOrderToSendCollection {
	public ArrayList<MarketOrderToSend> orders;
	public double totalValueBy;
	public double totalValueGot;
	
	
	// important for resources check
	public double resourcesAvailable;
	public double resourcesBalance;
	
	public double totalNextNode; // this positive is important
	// last one in arraylist will containt total balance after transactions 
	
	public String currency;
	
	
	
	
	// for trade 
	public String pair;
	public String type;
	public double amount;
	public double minMaxRate; // price to buy summed  rate

	public int maxDepth; // used when unable to sell then jump to next depth
	public int currentDepth; // showes current depth

	
//	pair 	Yes 	pair 	btc_usd (example) 	-
//	type 	Yes 	The transaction type 	buy or sell 	-
//	rate 	Yes 	The rate to buy/sell 	numerical 	-
//	amount
	
	// trading information
	// minMax Rate to put
	
	
	
	
}
