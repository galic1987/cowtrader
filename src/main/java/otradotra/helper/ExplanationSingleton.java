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
		String returnme = "";
		
		if(m1.getType().equals(MarketType.BID)){
			// bid
		returnme = "Price("+m1.getFrom()+") | Volume("+m1.getTo()+") | Total("+m1.getTo()+") \n";
		for(int i =0;i<n;i++){
			returnme += ""+m[i].price+" | "+m[i].volume+" | "+(m[i].volume*m[i].price)+"\n";
		}
		}else{
			// ask
			returnme = "Price("+m1.getFrom()+") | Volume("+m1.getTo()+") | Total("+m1.getTo()+") \n";
			for(int i =0;i<n;i++){
				returnme += ""+m[i].price+" | "+m[i].volume+" | "+(m[i].volume/m[i].price)+"\n";
			}
		}
			
		return returnme;
	}
}
