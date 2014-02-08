package otradotra.helper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import otradotra.MarketType;
import otradotra.models.Market;
import test_choco.CalculateOptimalVolumeProblem;
import test_choco.MarketProblem;

public class ReporterSingleton {
	private static ReporterSingleton instance = null;

	// external configuration - this is used to balance everything in this one
	public static String balancingCurrency = "usd";

	// **************** REMOVE THIS FROM HERE
	public static Map<Integer, Double> resources;

	// static on start change
	public static Map<Integer, String> valueMapping; // node(n) -> resource
	public static Map<String, Integer> keyMapping; // resource -> node(n)

	// dynamic on solution change
	public static int numberOfSoultions = 0;
	public static double highestValue = 0;
	public static double roundhighestValueBalancingCurrency = 0;
	public static double totalValue = 0;
	public static Map<Integer, SolutionEvaluator> snapShot = new HashMap<Integer, SolutionEvaluator>();

	// on round change
	public static double roundHigh = -10;
	public static Map<Integer, Integer> roundAround = new HashMap<Integer, Integer>();
	public static String roundCurrency;

	// special heuristic for most involved nodes
	public static Map<String, Integer> involvedNodesCounter = new HashMap<String, Integer>();
	public static Map<Map<Integer, Integer>, Boolean> involvedDupes = new HashMap<Map<Integer, Integer>, Boolean>();


	protected ReporterSingleton() {
		// Exists only to defeat instantiation.
	}

	public static ReporterSingleton getInstance() {
		if (instance == null) {
			instance = new ReporterSingleton();
		}
		return instance;
	}

	// not used
	public static void newSolution(double value, String currency, Market[][] m,
			Map<Integer, Integer> nodeMapping) {

	}

	public static void highRound(double value, String currency,
			Map<Integer, Integer> roundAround, Market[][] m) {

		double currentHighest = ReporterSingleton.getValue(roundCurrency,
				roundHigh, balancingCurrency, m, keyMapping);
		double tryHighest = ReporterSingleton.getValue(currency, value,
				balancingCurrency, m, keyMapping);

		/*
		 * System.out.println("##currentHighest "+roundHigh + " " +
		 * ReporterSingleton.roundCurrency);
		 * System.out.println("##tryHighest "+value + " " + currency);
		 * System.out.println("-currentHighest "+currentHighest + " " +
		 * balancingCurrencyValue); System.out.println("-tryHighest "+tryHighest
		 * + " " + balancingCurrencyValue);
		 */
		if (tryHighest > currentHighest) {
			ReporterSingleton.roundHigh = value;
			ReporterSingleton.roundAround = roundAround;
			ReporterSingleton.roundCurrency = currency;
			ReporterSingleton.roundhighestValueBalancingCurrency = tryHighest;

			// CalculateOptimalVolumeProblem problem = new
			// CalculateOptimalVolumeProblem(m, value, currency, roundAround,
			// roundAround.size(), valueMapping, resources);
			// problem.start();

		}
	}

	// returns 0 if no direct connection to that currency
	// returns amount on from==to
	public static double getValue(String fromCurrency, double amount,
			String toCurrency, Market[][] m, Map<String, Integer> keyMapp) {

		// if you try to change from to the same
		if (fromCurrency.equals(toCurrency))
			return amount;

		// if there is that market at all
		if (m[keyMapp.get(fromCurrency)][keyMapp.get(toCurrency)] != null) {
			Market market = m[keyMapp.get(fromCurrency)][keyMapp
					.get(toCurrency)];
			if (market.getType() == MarketType.BID) {
				// BID if(amount == 0) return -1;
				return market.getOrders()[0].price
						* amount
						- (market.getOrders()[0].price * amount * market
								.getTransactionFee());
			} else {
				// ASK
				return amount
						/ market.getOrders()[0].price
						- (amount / market.getOrders()[0].price * market
								.getTransactionFee());

			}
		} else {
			// null error

			return -1;

		}

	}

	public static void resetInvolvedCounter() {
		involvedNodesCounter = null;
		involvedNodesCounter = new HashMap<String, Integer>();
		involvedDupes = null;
		involvedDupes = new HashMap<Map<Integer, Integer>, Boolean>();


	}

	// only on solution
	public static void addInvolvedCount(double value, String currency, Market[][] m,Map<Integer, Integer> nodeMapping) {
		
		if(involvedDupes.get(nodeMapping)!=null) return; // return on duplicates
		
		involvedDupes.put(nodeMapping, true); // add it so we dont add it again

		
		// add snapshot solution
			snapShot.put(numberOfSoultions, new SolutionEvaluator(value,
					currency, nodeMapping, m));
			ReporterSingleton.numberOfSoultions++;
			ReporterSingleton.totalValue += value;
		
		// add highest value 
		if (value > ReporterSingleton.highestValue) {
			ReporterSingleton.highestValue = value;
		}
		
		
		Iterator it = nodeMapping.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			// go throught full cycle beginning with actual node
			int actualNode = (Integer) pairs.getKey();

			int i = actualNode; // actual node
			int j = nodeMapping.get(actualNode); // next node

			String node = i + "->" + j;
			// if new add +1
			if (involvedNodesCounter.get(node)!=null) {
				involvedNodesCounter.put(node,
						involvedNodesCounter.get(node) + 1);
			} else {
				involvedNodesCounter.put(node, 1);
			}
			// if old +1
		}
	}
	
	public static void printInvolvedCount() {

		Iterator it = involvedNodesCounter.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			// go throught full cycle beginning with actual node
			String node = (String) pairs.getKey();
			int occurences = involvedNodesCounter.get(node); // next node
 
			System.out.println(node +" "+occurences);
		}
	}

}