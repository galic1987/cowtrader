package otradotra.helper;

import otradotra.Market;
import otradotra.MarketOrder;
import otradotra.MarketType;

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
	
	
	public static String lastNOrdersFromMarket(int n , Market m1){
		MarketOrder [] m = m1.getOrders();
		String returnme = "------------Market Data Raw Start----\n";
		double cummulative = 0;
		if(m1.getType().equals(MarketType.BID)){
			// bid
		returnme += "Price("+m1.getFrom()+") | Volume("+m1.getTo()+") | Total("+m1.getFrom()+") | Cummulative \n";
		for(int i =0;i<n;i++){
			cummulative += m[i].volume*m[i].price;
			returnme += ""+m[i].price+" | "+m[i].volume+" | "+(m[i].volume*m[i].price)+" | "+cummulative+"\n";
		}
		}else{
			// ask
			returnme = "Price("+m1.getFrom()+") | Volume("+m1.getTo()+") | Total("+m1.getFrom()+")| Cummulative \n";
			for(int i =0;i<n;i++){
				cummulative += m[i].volume/m[i].price;
				returnme += ""+m[i].price+" | "+m[i].volume+" | "+(m[i].volume/m[i].price)+" | "+cummulative+"\n";
			}
		}
		
		returnme += "------------Market Data Raw End----\n";

			
		return returnme;
	}
}
