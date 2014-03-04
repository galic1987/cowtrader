package otradotra.helper;

import marketHole.CycleSolutionConfiguration;
import otradotra.models.Market;
import otradotra.models.MarketOrderDataHolder;
import otradotra.models.MarketOrderToSend;
import otradotra.models.MarketOrderToSendCollection;
import otradotra.models.MarketType;

// contains pretty print etc
public class ExplanationSingleton {
	
	private static ExplanationSingleton instance;
	
	protected ExplanationSingleton() {
		// Exists only to defeat instantiation.
	}

	public static ExplanationSingleton getInstance() {
		if (instance == null) {
			instance = new ExplanationSingleton();
		}
		return instance;
	}
	
	
	public static String explainCycleSolutionConfiguration(CycleSolutionConfiguration cs){
		StringBuffer buff = new StringBuffer();
		
		for(int i =0;i<cs.getOrders().size();i++){
			MarketOrderToSendCollection ma = cs.getOrders().get(i);
			
			String currency = ma.orders.get(0).getMarket().getTo();
			buff.append(currency+" : ");
			double sum = 0;
			for(int j =0;j<ma.orders.size();j++){
				MarketOrderToSend order = ma.orders.get(0);
				sum += order.getAmount();
			}
			buff.append(sum + " \n");

		}
		
		return buff.toString();
	}
	
	
	public static String lastNOrdersFromMarket(int n , Market m1){
		MarketOrderDataHolder [] m = m1.getOrders();
		String returnme = "------------Market Data Raw Start----\n";
		double cummulative = 0;
		if(m1.getType().equals(MarketType.BID)){
			// bid
		returnme += "Price("+m1.getFrom()+") | Volume("+m1.getTo()+") | Total("+m1.getFrom()+") | Cummulative \n";
		for(int i =0;i<=n;i++){
			cummulative += m[i].volume*m[i].price;
			returnme += ""+m[i].price+" | "+m[i].volume+" | "+(m[i].volume*m[i].price)+" | "+cummulative+"\n";
		}
		}else{
			// ask
			returnme = "Price("+m1.getFrom()+") | Volume("+m1.getTo()+") | Total("+m1.getFrom()+")| Cummulative \n";
			for(int i =0;i<=n;i++){
				cummulative += m[i].volume/m[i].price;
				returnme += ""+m[i].price+" | "+m[i].volume+" | "+(m[i].volume/m[i].price)+" | "+cummulative+"\n";
			}
		}
		
		returnme += "------------Market Data Raw End----\n";

			
		return returnme;
	}
	
	
	// need to  be moved into new helper singleton
	
	public static double lastNOrdersFromMarketCummulative(int n , Market m1){
		MarketOrderDataHolder [] m = m1.getOrders();
		double cummulative = 0;
		if(m1.getType().equals(MarketType.BID)){
			// bid
		for(int i =0;i<=n;i++){
			cummulative += m[i].volume*m[i].price;
		}
		}else{
			// ask
			for(int i =0;i<=n;i++){
				cummulative += m[i].volume/m[i].price;
			}
		}	
		return cummulative;
	}
}
