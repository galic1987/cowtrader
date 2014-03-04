package marketSpecific;

import java.util.Map;

import marketHole.CycleSolutionConfiguration;
import otradotra.models.MarketOrderToSend;

public abstract class AbstractMarketFactory {
	public abstract String executeCycleOrders(CycleSolutionConfiguration cc);
	public abstract Map<Integer, Double> updateResources(Map<String, Integer> keyMapping);
	public abstract Map<Integer, Double> minimumAmountForTransaction(Map<String, Integer> keyMapping);
}
