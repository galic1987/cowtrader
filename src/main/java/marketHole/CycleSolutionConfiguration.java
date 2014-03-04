package marketHole;

import java.util.ArrayList;
import java.util.Map;

import otradotra.models.MarketOrderToSend;
import otradotra.models.MarketOrderToSendCollection;


// try just highest potential with this one 
public class CycleSolutionConfiguration {
	
	private Map<Integer,Integer> cycle; // node -> node (arc)
	
	// how much this all transactions made money
		private double value;
		private String currency;

		// balancing currency Value - used to compare total gain between currencies
		private double valueInBalancingCurrency;
		private String balancingCurrency;
		
		// max order throughtput
		private ArrayList<MarketOrderToSendCollection> orders;
		
		private ArrayList<MarketOrderToSendCollection> ordersPossibleForResources;

		
		
		// sum up the orders and resources 
		public ArrayList<MarketOrderToSend> maximizeWithResources(ArrayList<ArrayList<MarketOrderToSend>> orders, Map<Integer, Double> resources){
			// for each marketOrderToSend - get resources
				// sumup the ordes get buy volume 
				// calculate if cuts needed (to pass the order)
			
			
		return null;
		}
		
		
		
	public Map<Integer, Integer> getCycle() {
		return cycle;
	}




	public void setCycle(Map<Integer, Integer> cycle) {
		this.cycle = cycle;
	}




	public double getValue() {
		return value;
	}




	public void setValue(double value) {
		this.value = value;
	}




	public String getCurrency() {
		return currency;
	}




	public void setCurrency(String currency) {
		this.currency = currency;
	}




	public double getValueInBalancingCurrency() {
		return valueInBalancingCurrency;
	}




	public void setValueInBalancingCurrency(double valueInBalancingCurrency) {
		this.valueInBalancingCurrency = valueInBalancingCurrency;
	}




	public String getBalancingCurrency() {
		return balancingCurrency;
	}




	public void setBalancingCurrency(String balancingCurrency) {
		this.balancingCurrency = balancingCurrency;
	}




	public ArrayList<MarketOrderToSendCollection> getOrders() {
		return orders;
	}




	public void setOrders(ArrayList<MarketOrderToSendCollection> orders) {
		this.orders = orders;
	}



	public ArrayList<MarketOrderToSendCollection> getOrdersPossibleForResources() {
		return ordersPossibleForResources;
	}



	public void setOrdersPossibleForResources(
			ArrayList<MarketOrderToSendCollection> ordersPossibleForResources) {
		this.ordersPossibleForResources = ordersPossibleForResources;
	}




	
}
