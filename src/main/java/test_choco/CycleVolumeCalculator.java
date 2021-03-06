package test_choco;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import marketHole.CycleSolutionConfiguration;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.LoggerFactory;

import otradotra.helper.ExplanationSingleton;
import otradotra.helper.ReporterSingleton;
import otradotra.models.Market;
import otradotra.models.MarketOrderDataHolder;
import otradotra.models.MarketOrderToSend;
import otradotra.models.MarketOrderToSendCollection;
import otradotra.models.MarketType;
import otradotra.propagators.OrderDepthPropagator;
import samples.AbstractProblem;
import solver.ICause;
//import samples.integer.Alpha;
//import samples.integer.Knapsack;
//import samples.integer.Knapsack.Data;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.LogicalConstraintFactory;
import solver.constraints.Propagator;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.gary.basic.PropKArcs;
import solver.constraints.gary.basic.PropKCC;
import solver.constraints.gary.basic.PropKNodes;
import solver.constraints.gary.basic.PropTransitivity;
import solver.constraints.gary.channeling.PropGraphRelation;
import solver.constraints.gary.channeling.PropRelationGraph;
import solver.constraints.gary.channeling.relations.GraphRelation;
import solver.constraints.gary.channeling.relations.GraphRelationFactory;
import solver.constraints.gary.degree.PropNodeDegree_AtLeast;
import solver.constraints.gary.degree.PropNodeDegree_AtMost;
import solver.constraints.gary.path.PropPathNoCycle;
import solver.constraints.nary.nValue.Differences;
import solver.constraints.real.Ibex;
import solver.constraints.real.RealConstraint;
import solver.constraints.set.SetConstraintsFactory;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.objective.ObjectiveStrategy;
import solver.objective.OptimizationPolicy;
import solver.search.limits.SolutionCounter;
import solver.search.loop.monitors.IMonitorSolution;
import solver.search.loop.monitors.SMF;
import solver.search.solution.LastSolutionRecorder;
import solver.search.solution.Solution;
import solver.search.strategy.GraphStrategyFactory;
import solver.search.strategy.ISF;
import solver.search.strategy.IntStrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.RealVar;
import solver.variables.SetVar;
import solver.variables.VF;
import solver.variables.Variable;
import solver.variables.VariableFactory;
import solver.variables.graph.DirectedGraphVar;
import solver.variables.graph.UndirectedGraphVar;
import util.objects.graphs.Orientation;
import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetFactory;

// one market cycle calculator for high potential solution
// delivers configurations 
public class CycleVolumeCalculator extends AbstractProblem {

	// in - constructor
	private Market[][] solverData; // market data
	private int howDeepWillSearchBe; // limit of search
	private Map<Integer, Integer> cycle; // cycle for hole calculating
	private Map<Integer, String> valueMapping; // node currency mapping
	private Map<String, Integer> keyMapping;
	
	// for some algorithms needed
	private Map<Integer, Double> resources; // resources
	private OrderDepthPropagator p;

	
	// out - filled by recursion
	// data about holes
	// market configuration
	private CycleSolutionConfiguration solutionConfiguration;
	private CycleSolutionConfiguration solutionConfigurationForAvailableResources;


	// problem stuff
	IntVar[] variations;
	double [] tempCalc;

	public CycleVolumeCalculator(Market[][] solverData,
			int howDeepWillSearchBe, Map<Integer, Integer> cycle,
			Map<Integer, String> valueMapping, Map<Integer, Double> resources,Map<String, Integer> keyMapping) {
		super();
		this.solverData = solverData;
		this.howDeepWillSearchBe = howDeepWillSearchBe;
		this.cycle = cycle;
		this.valueMapping = valueMapping;
		this.resources = resources;
		this.keyMapping = keyMapping;


	}

	@Override
	public void createSolver() {
		solver = new Solver("VariationsCacheCalculator");
	}

	@Override
	public void buildModel() {

		// 1. variations
		variations = VariableFactory.boundedArray("nodes", cycle.size(), 0,
				howDeepWillSearchBe, solver);

		p = new OrderDepthPropagator(solverData, howDeepWillSearchBe, cycle,
				valueMapping, resources, variations);
		solver.post(new Constraint("maxOrderDepth", p));

	}

	@Override
	public void configureSearch() {
		// solver.set(IntStrategyFactory.firstFail_InDomainMin(letters));
		// solver.set(GraphStrategyFactory.graphLexico(tc));
		// solver.set(GraphStrategyFactory.graphRandom(tc, 11));
	}

	@Override
	public void solve() {

		/*solver.getSearchLoop().plugSearchMonitor(new IMonitorSolution() {
			public void onSolution() {

				// TODO: optimization point , put it away it is not beeing used
				// only debug
				/*
				 * String configuration = ""; int orderDepth[] = new
				 * int[variations.length]; for(int i
				 * =0;i<variations.length;i++){ IntVar var = variations[i];
				 * orderDepth[i] = var.getValue(); configuration +=
				 * "-"+orderDepth[i]; }
				
				// System.out.println("*** Solution " +configuration + " " );

			}
		});
*/
		solver.findAllSolutions();

 
		
			// try to sell it with max starting with that current node
			// find out best results
			// TODO: optimization point volume compare if not exceeding other
			// parts of cycle then break

			// TODO: optimization point cut cyclic to end at the certain point
			// rewrite the calculating loop

			// make clear orders in this loop so trade can be condcuted
			// put it to the ordering agent
		
		
		ArrayList<CycleSolutionConfiguration> tempSolutions = new ArrayList<CycleSolutionConfiguration>();
		Iterator ita = cycle.entrySet().iterator();
		while (ita.hasNext()) {
			Map.Entry pairs = (Map.Entry) ita.next();

			int startNode = (Integer)pairs.getKey();
			//System.out.print(startNode+ " tryingCycles ");
 			//maxCyclesEvaluationDecision(tryingCycles,0);
			
			// this is max cycle 
			ArrayList<MarketOrderToSendCollection> maxSendGlobal = maxCyclesEvaluationDecision(startNode,0,0,false);
			
			

			if (maxSendGlobal!=null){
				CycleSolutionConfiguration cc = new CycleSolutionConfiguration();
				cc.setOrders(maxSendGlobal);
				

				// calculate all for my resources 
				// only if i cannot buy
				boolean canIBuy = canIBuy(cc, resources);
				if(!canIBuy){
					// you cannot , need recalculate 
					ArrayList<MarketOrderToSendCollection> maxMyResourcesSend = maxCyclesEvaluationDecision(startNode,0,resources.get(startNode),true); 
 					if(maxMyResourcesSend == null){
 						System.out.println("Mega error unable to resolve maxMyResourcesToSend");
 					}
					cc.setOrdersPossibleForResources(maxMyResourcesSend); 

				}else{
					// you can it is ok
					cc.setOrdersPossibleForResources(maxSendGlobal); 
				}
				
				
				
				// get total calculation
				double value = 0;
				String thisCurrency = "";
				for(int i =0;i<cc.getOrdersPossibleForResources().size();i++){
					MarketOrderToSendCollection sendTemp = cc.getOrdersPossibleForResources().get(i);
					if(i == cc.getOrdersPossibleForResources().size()-1){
						value = sendTemp.totalNextNode; // this is gain total
					}
 
				}
				
				thisCurrency = valueMapping.get(startNode);

				
				// update all 
				cc.setCycle(this.cycle); 
				cc.setValue(value);
				cc.setCurrency(thisCurrency);
				cc.setBalancingCurrency(ReporterSingleton.balancingCurrency);
				double valbal = ReporterSingleton.getValue(thisCurrency, value, ReporterSingleton.balancingCurrency, solverData, keyMapping);
				cc.setValueInBalancingCurrency(valbal);
				
				
				tempSolutions.add(cc);
			}
			
			
			/*for(int i=0;i<tempCalc.length;i++){
				System.out.print(tempCalc[i] + " ");
			}
			System.out.println("");
			*/

		}
		
		
		// if no solutions , this should not happen
		if(tempSolutions.size()==0){
			System.out.println("Mega error LOL");
			//System.exit(-1);
			return;
		}
		
		// if is the only one then is the best one 
		if(tempSolutions.size()<2){
			solutionConfiguration = tempSolutions.get(0);
		}else{
			// otherwise compare highest earning
			double highest = 0;
			for(int i=0;i<tempSolutions.size();i++){
				// compare
				
				CycleSolutionConfiguration cc = tempSolutions.get(i);
				
				boolean canIBuy = canIBuy(cc, resources);
				if(!canIBuy){
					continue;
				}
				
				// get highest 
				if(cc.getValueInBalancingCurrency() > highest){
					highest = cc.getValueInBalancingCurrency();
					solutionConfiguration = tempSolutions.get(i);
				}
				System.out.println("Solutions highest: " + cc.getBalancingCurrency() +" " +cc.getValueInBalancingCurrency());

			}
		}
		
		
		
 

	}
	
	public ArrayList<MarketOrderToSendCollection> cutToResources(){
		double maxValue = 0;

		ArrayList<MarketOrderToSendCollection>maxMyResourcesSend = null;
		Iterator ita = cycle.entrySet().iterator();
		while (ita.hasNext()) {
			Map.Entry pairs = (Map.Entry) ita.next();

			int startNode = (Integer)pairs.getKey();
			 ArrayList<MarketOrderToSendCollection> max = maxCyclesEvaluationDecision(startNode,0,resources.get(startNode),true); 

			 // if this is ok
			if(canIBuy(max, resources)){
				// get best from it calculation
				double value = 0;
				for(int i =0;i<max.size();i++){
					MarketOrderToSendCollection sendTemp = max.get(i);
					if(i == max.size()-1){
						value = sendTemp.totalNextNode; // this is gain total
					}
 
				}
				if(value>= maxValue){
					maxMyResourcesSend = max;
				}
			}
			
		}
		return maxMyResourcesSend;
		
		
	}

	// returns 0 or bigger value if true
	public ArrayList<MarketOrderToSendCollection>  maxCyclesEvaluationDecision(
			int startNode, 
			int bottelNeckRelaxing, 
			double volumeToTry, 
			boolean limitFirstToVolumeToTry) {
		
		// optimize this one 
		double[] calc = new double[solverData.length];
		double[] calcTo = new double[solverData.length];

		//System.out.println("");  
		
		ArrayList<MarketOrderToSendCollection> totalOrders = new ArrayList<MarketOrderToSendCollection>();
		

		for (int i = 0; i < calc.length; i++) {
			calc[i] = 0;
		}

		boolean cuttingResouces = false;
		if(limitFirstToVolumeToTry){
			cuttingResouces = true;
		}
		// count number of most involved nodes
		// go throught cycle
		Map<Integer, Integer> nodeMapping = cycle;
		
			
			
			// go throught full cycle beginning with actual node
			int actualNode = startNode;

			for (int i = 0; i < calc.length; i++) {
				calc[i] = 0;
				calcTo[i] = 0;
			}

			int nodeCounter = 0;

 			
		
			// try with the maximum 
			do {
				
			 
				int i = actualNode; // actual node
				int j = nodeMapping.get(actualNode); // next node
				// actual market
				Market m = solverData[i][j];
				int maxDepth = p.getNodeDepthTracker().get(nodeCounter);
				ArrayList<MarketOrderToSend> tempOrders = new  ArrayList<MarketOrderToSend>();
				
				MarketOrderToSendCollection marketOrderCollection = new MarketOrderToSendCollection();
				
				// calculation
				if(cuttingResouces){
					cuttingResouces = false;
					// TODO: cut to the resources
					if(marketOrderCollection.resourcesBalance<=0){
						// limit resources 
						volumeToTry = ( (marketOrderCollection.resourcesBalance)+volumeToTry );
						//marketOrderCollection.resourcesBalance = marketOrderCollection.resourcesBalance
						// make hole for transaction fee
						//volumeToTry = volumeToTry - (volumeToTry*marketOrderCollection.orders.get(0).getMarket().getTransactionFee());
					}
				}else{
					volumeToTry = calc[i];
				}
					
					if(volumeToTry == 0){
					//	System.out.println(""+volumeToTry);
					}
					
					// if first try 0 fill it up total 
					tempOrders = fillMaximumBuy(m, volumeToTry, maxDepth, bottelNeckRelaxing);
					
					marketOrderCollection.orders = tempOrders;
					marketOrderCollection.maxDepth = maxDepth;
					marketOrderCollection.currentDepth = tempOrders.size();
					
 					// J
					double totalBought = getTotalBought(tempOrders); // calculate one round more with 
					calc[j] += totalBought; // next node
					marketOrderCollection.totalValueGot = totalBought;

					// trade data
					marketOrderCollection.minMaxRate = getOrderPriceFor(marketOrderCollection);
					marketOrderCollection.pair = tempOrders.get(0).getPair();
					marketOrderCollection.type = tempOrders.get(0).getType();
					
					marketOrderCollection.currency = valueMapping.get(i); // used for min

					
					// I
					double totalBuyWith = getTotalBuyWith(tempOrders);
					double totalBuyDeductionsIncludingFees = getTotalDeduction(tempOrders);

					marketOrderCollection.amount = totalBuyWith;
					calc[i] -= totalBuyDeductionsIncludingFees;
					marketOrderCollection.totalValueBy = totalBuyDeductionsIncludingFees;
					marketOrderCollection.totalNextNode = calc[j]; // important only last element
					marketOrderCollection.resourcesAvailable = resources.get(i);
					marketOrderCollection.resourcesBalance = marketOrderCollection.resourcesAvailable - marketOrderCollection.totalValueBy;
				
					

					// .. end calculation
					
				// save the situation
					totalOrders.add(marketOrderCollection);	
					
				nodeCounter++;
				actualNode = nodeMapping.get(actualNode);// return next
															// Value
			} while (actualNode != startNode); // if equal to
																// begin
																// node then
																// destroy
															// the cycle
			tempCalc = calc;
			
			
			
			boolean solution = false;
			for(int i=0;i<tempCalc.length;i++){
				if(tempCalc[i]<0){
					//solution = i;
					//System.out.println("no Solution" + tempCalc[i]);
					if(limitFirstToVolumeToTry){
						return null;
					}else{
						return null;
					}
				}
				
				if(tempCalc[i]>0){
					solution = true;
					//solution = startNode;
				}
			}		
			
			
			
			
			// special case if everything == 0
			if(!solution)return null;
			
			
		return totalOrders;
	}
	
	

	
	
	
	/*
	 * Algorithms for checking the max resources available
	 */
	
	private boolean canIBuy(CycleSolutionConfiguration cc,  Map<Integer, Double> availResource){
		
		int length =  cc.getOrders().size();
		for(int i =0;i<length;i++){
			MarketOrderToSendCollection co = cc.getOrders().get(i);
			if(co.resourcesBalance<0) return false;
		}
		
		
		return true;
	}
	
	private boolean canIBuy( ArrayList<MarketOrderToSendCollection> cc,  Map<Integer, Double> availResource){
		
		int length =  cc.size();
		for(int i =0;i<length;i++){
			MarketOrderToSendCollection co = cc.get(i);
			if(co.resourcesBalance<0) return false;
		}
		
		
		return true;
	}
	

	
	
	
	
	
	
	
	/*
	 * 
	 * Algorithms for filling the order array
	 * 
	 */
	

	private ArrayList<MarketOrderToSend> fillMaximumBuy(Market m, double incomingVolume,
			int maxDepthOrderProfitable, int relaxingTheBottelneck) {
		ArrayList<MarketOrderToSend> orders = new ArrayList<MarketOrderToSend>();
		
		// order.setMarket(m);
		// order.setAmount(amount);
		double tempBuy = 0;
		double transfee = 0;
		double buyFromToFillVoluminaWithFee = 0;
		
		if (incomingVolume == 0) {
			// maxBuy this is begin
			// do it for every single one
			// relaxing optimization widen the bottelneck
			for (int i = 0; i <= maxDepthOrderProfitable + relaxingTheBottelneck; i++) {
				MarketOrderToSend orderTemp = new MarketOrderToSend();
				// TODO: transaction fee 
				
				if(m.getType() == MarketType.BID){
					transfee = m.getOrders()[i].volume -((m.getOrders()[i].volume /(1+m.getTransactionFee())));
					buyFromToFillVoluminaWithFee =  (m.getOrders()[i].volume - transfee);
					tempBuy = (m.getOrders()[0].price * buyFromToFillVoluminaWithFee);
				}else{
					transfee = m.getOrders()[i].volume -(m.getOrders()[i].volume /(1+m.getTransactionFee()));
					buyFromToFillVoluminaWithFee =  m.getOrders()[i].volume - transfee;
					tempBuy = (buyFromToFillVoluminaWithFee / m.getOrders()[0].price);
				}
				
				orderTemp.setAmount(buyFromToFillVoluminaWithFee);
				orderTemp.setRate(m.getOrders()[i].price);
				orderTemp.setType(m.getMeTheType());
				orderTemp.setPair(m.getMarketName());
				orderTemp.setMarket(m); 
				orderTemp.setOrderDepth(i);		
				orderTemp.setFeeOnAmount(transfee); 
				orders.add(orderTemp);
				orderTemp.setTotal(tempBuy);
			}

		} else {
			// calculate the max throughput
			int a = 1; 
			a = 3;
			double volumeLeft = Math.abs(incomingVolume);
			boolean notFilled = true;
			int i = 0;
			
			while (notFilled) {
				// dont get overflow
				if(i>m.getOrders().length) break;
				
				// optimality
				if(i> maxDepthOrderProfitable) break;

				
				double percentageFilled = precentageOfVolumeOrderFilled(m.getOrders()[i], volumeLeft);
				
				// breaking condition
				if(volumeLeft <= 0){
					notFilled = false;
					break;
				}
				
				// -> try max fill
				if(percentageFilled <= 1){
					// smaller than  (100%) , fill the full order
					MarketOrderToSend orderTemp = new MarketOrderToSend();
					
					if(m.getType() == MarketType.BID){
						transfee = m.getOrders()[i].volume -((m.getOrders()[i].volume /(1+m.getTransactionFee())));
						buyFromToFillVoluminaWithFee =  (m.getOrders()[i].volume - transfee);
						tempBuy = (m.getOrders()[0].price * buyFromToFillVoluminaWithFee);
					}else{
						transfee = m.getOrders()[i].volume -(m.getOrders()[i].volume /(1+m.getTransactionFee()));
						buyFromToFillVoluminaWithFee =  m.getOrders()[i].volume - transfee;
						tempBuy = (buyFromToFillVoluminaWithFee / m.getOrders()[0].price);
					}
					
					orderTemp.setAmount(buyFromToFillVoluminaWithFee);
					orderTemp.setRate(m.getOrders()[i].price);
					orderTemp.setType(m.getMeTheType());
					orderTemp.setPair(m.getMarketName());
					orderTemp.setMarket(m);
					orderTemp.setOrderDepth(i);
					orderTemp.setTotal(tempBuy);
					//orderTemp.recalculate();
					orders.add(orderTemp);
					orderTemp.setFeeOnAmount(transfee); 
					volumeLeft = volumeLeft - buyFromToFillVoluminaWithFee;
				}else{
					// bigger than it is too much need to cut it 
					double maxVolumeToTheEnd = getMaxOfOrder(m.getOrders()[i],volumeLeft);
					MarketOrderToSend orderTemp = new MarketOrderToSend();
					
					if(m.getType() == MarketType.BID){
						transfee = maxVolumeToTheEnd -((maxVolumeToTheEnd /(1+m.getTransactionFee())));
						buyFromToFillVoluminaWithFee =  (maxVolumeToTheEnd - transfee);
						tempBuy = (m.getOrders()[0].price * buyFromToFillVoluminaWithFee);
					}else{
						transfee = maxVolumeToTheEnd -(maxVolumeToTheEnd /(1+m.getTransactionFee()));
						buyFromToFillVoluminaWithFee =  maxVolumeToTheEnd - transfee;
						tempBuy = (buyFromToFillVoluminaWithFee / m.getOrders()[0].price);
					}
					
					orderTemp.setAmount(buyFromToFillVoluminaWithFee);
					orderTemp.setRate(m.getOrders()[i].price);
					orderTemp.setType(m.getMeTheType());
					orderTemp.setPair(m.getMarketName());
					orderTemp.setMarket(m);
					orderTemp.setOrderDepth(i);
					orderTemp.setTotal(tempBuy);
					orderTemp.setFeeOnAmount(transfee); 
					orders.add(orderTemp);
					volumeLeft = volumeLeft - maxVolumeToTheEnd;
				}

				
				i++;
			}
		}
		return orders;
	}

	// when you buy something, what do you get at the and inclusive fees 
	private double getTotalBought(ArrayList<MarketOrderToSend> orders) {
		double total = 0; 

		int size = orders.size();
		for(int i = 0; i<size; i++){
			
			if(orders.get(i).getType().equals("buy")){
			total += orders.get(i).getTotal(); // total bought
			}else{
			total += orders.get(i).getTotal(); // total bought
	
			}
		}
		
		return total;
	}
	

	
	// when you buy something how much resources do you need 
	private double getTotalBuyWith(ArrayList<MarketOrderToSend> orders) {
		double total = 0;
		int size = orders.size();
		for(int i = 0; i<size; i++){
//				if(orders.get(i).getType().equals("buy")){
					total += orders.get(i).getAmount(); // total bought
//				}else{
//					// addup transaction fee
//					total += orders.get(i).getTotal() ;
//				}
		}
		
		return total;
	}
	
	
	
	private double getTotalDeduction(ArrayList<MarketOrderToSend> orders) {
		double total = 0;
		int size = orders.size();
		for(int i = 0; i<size; i++){
//				if(orders.get(i).getType().equals("buy")){
					total += orders.get(i).getAmount()+orders.get(i).getFeeOnAmount(); // total bought
//				}else{
//					// addup transaction fee
//					total += orders.get(i).getTotal() ;
//				}
		}
		
		return total;
	}
	
	
	
	
	/*
	 * getMaxOfOrder - try with volume
	 */
	private double getMaxOfOrder(MarketOrderDataHolder order, double volume){		
		// buying volume cant be bigger than order volume 
		if(order.volume >= volume){
			return volume;
		}else{
			// if volume bigger than order
			return order.volume;
		}
	}
	
	/*
	 * get percentage of volume order filled 
	 * between 0 .... n 
	 * if 1 it is 100% filled 
	 */
	private double precentageOfVolumeOrderFilled(MarketOrderDataHolder order, double restVolume) {
		if (restVolume <= 0) return 1;
		double temp = 0;
		temp = order.volume / restVolume;
		return temp;
	}
	
	
	// order price for sum of orders
	public double getOrderPriceFor(MarketOrderToSendCollection ord){
		double price = 0;
		for(int i = 0;i<ord.orders.size();i++){
			price =  ord.orders.get(i).getRate();
		}
		return price;
	}

	@Override
	public void prettyOut() {
		if (true)
			return;

	}

	public void start() {
		this.execute("-log", "SILENT", "-ee", "NONE");
	}

	public CycleSolutionConfiguration getSolutionConfiguration() {
		return solutionConfiguration;
	}

	public void setSolutionConfiguration(CycleSolutionConfiguration solutionConfiguration) {
		this.solutionConfiguration = solutionConfiguration;
	}

}
