package marketSpecific;

import java.util.Map;

import marketHole.CycleSolutionConfiguration;
import otradotra.models.MarketOrderToSend;

public abstract class AbstractMarketFactory {
	public abstract String executeCycleOrders(CycleSolutionConfiguration cc);
	public abstract Map<Integer, Double> updateResources(Map<String, Integer> keyMapping);
	public abstract boolean minimumAmountForTransaction(CycleSolutionConfiguration cc, Map<String, Integer> keyMapping);
	public abstract String resourcesCompare(Map<Integer, Double> oldResources,Map<String, Integer> keyMapping,Map<Integer,String > valueMapping);
}
