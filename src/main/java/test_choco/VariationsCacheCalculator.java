package test_choco;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import marketHole.CycleConfigurationModel;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.LoggerFactory;

import otradotra.MarketType;
import otradotra.helper.ExplanationSingleton;
import otradotra.helper.ReporterSingleton;
import otradotra.models.Market;
import otradotra.models.MarketOrderDataHolder;
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
public class VariationsCacheCalculator extends AbstractProblem {

	// in - constructor
	private Market[][] solverData; // market data
	private int howDeepWillSearchBe; // limit of search
	private int numberOfNodes;
	private Map<Integer, Integer> cycle; // cycle for hole calculating
	private Map<Integer, String> valueMapping; // node currency mapping
	// for some algorithms needed
	private Map<Integer, Double> resources; // resources

	// internal for calculation / 
	private Map<Integer, Integer> nodeDepthTracker; // node -> depth solution
	private Map<Integer, String> nodeDepthExplainer; // node -> currency
	private Map<Integer, Market> nodeDepthMarket; // node -> market


	// upperbound optimization / not to be messed with
	private int [] snapshotWorking; // last working depth
	
	// out - filled by recursion
	// data about holes
	// market configuration
	private ArrayList<CycleConfigurationModel> solutionConfigurations; // out
	private OrderDepthPropagator p;

	IntVar[] variations;

	public VariationsCacheCalculator(Market[][] solverData,
			int howDeepWillSearchBe, Map<Integer, Integer> cycle,
			Map<Integer, String> valueMapping, Map<Integer, Double> resources) {
		super();
		this.solverData = solverData;
		this.howDeepWillSearchBe = howDeepWillSearchBe;
		this.cycle = cycle;
		this.valueMapping = valueMapping;
		this.resources = resources;

	}
	
	
	@Override
    public void createSolver() {
        solver = new Solver("VariationsCacheCalculator");
    }

	@Override
	public void buildModel() {

		// 1. variations
		variations = VariableFactory.boundedArray("nodes", cycle.size(), 0, howDeepWillSearchBe, solver);

		
		nodeDepthExplainer = new HashMap<Integer, String>();
		nodeDepthTracker = new HashMap<Integer, Integer>();
		nodeDepthMarket = new HashMap<Integer, Market>();
		
	    p = new OrderDepthPropagator(solverData, howDeepWillSearchBe, cycle, valueMapping, resources, variations);
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

		final Map<Integer, Integer>[] myArray = (Map<Integer, Integer>[]) new Map[1000];

		solver.getSearchLoop().plugSearchMonitor(new IMonitorSolution() {
			public void onSolution() {
				
				
				// TODO: optimization point , put it away it is not beeing used
				// only debug
				String configuration = "";
				int orderDepth[] = new int[variations.length];
				for(int i =0;i<variations.length;i++){
					IntVar var = variations[i];
					orderDepth[i] = var.getValue();
					configuration += "-"+orderDepth[i];
				}
				
				
				//System.out.println("*** Solution " +configuration + " " );

				
				
			}
		});

		solver.findAllSolutions();

		
		//System.out.println(p.getNodeDepthTracker());
		

		// calculate max 
		Iterator it = p.getNodeDepthExplainer().entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        
	        String marketName = (String) pairs.getValue();
	        Market marketObject = p.getNodeDepthMarket().get(pairs.getKey());
	        int marketMax = p.getNodeDepthTracker().get(pairs.getKey());
	        
	        
	       double max =  ExplanationSingleton.lastNOrdersFromMarketCummulative(marketMax, marketObject);
	        
	       System.out.println("market name "+marketName+ " " + marketObject.getMarketName() +" calc " +max);
	       
	        // try to sell it with max starting with that current node
	       	// find out best results 
	       	// TODO: optimization point volume compare if not exceeding other
	        // parts of cycle then break 
	       
	       // TODO: optimization point cut cyclic to end at the certain point
	       // rewrite the calculating loop 
	       
	       // make clear orders in this loop so trade can be condcuted  
	       // put it to the ordering agent 
	       
			
	    
	    }		

	}

	/*

	// returns 0 or bigger value if true 
	public double maxCyclesEvaluationDecision() {
		boolean explain = false;

		double[] calc = new double[solverData.length];
		double volumina = 1; // try volumina to spin

		for (int i = 0; i < calc.length; i++) {
			calc[i] = 0;
		}
		
		// count number of most involved nodes		
		// go throught cycle
			StringBuffer explanator = null;
			Map<Integer, Integer> nodeMapping = cycle;
			Iterator it = nodeMapping.entrySet().iterator();
			while (it.hasNext()) {
				if(explain){
				explanator = new StringBuffer();
				explanator.append("############ ");
				}
				
				Map.Entry pairs = (Map.Entry) it.next();
				// go throught full cycle beginning with actual node
				int actualNode = (Integer) pairs.getKey();
				volumina = resources.get(actualNode);
				for (int i = 0; i < calc.length; i++) {
					calc[i] = 0;
					cummulativeToStep[i] = 0;
				}
				int nodeCounter = 0;

				
				do {
					int i = actualNode; // actual node
					int j = nodeMapping.get(actualNode); // next node
					
					
					// .. do here the calculation
					// System.out.println(i+"->"+j);
					Market m = solverData[i][j];
					
					// save performance 
					if(nodeDepthMarket.get(nodeCounter)==null){
						nodeDepthExplainer.put(nodeCounter, valueMapping.get(j));
						nodeDepthMarket.put(nodeCounter	, m);
					}
					
					//  calculation
					if (calc[i] == 0) {
						// head explanation
						if(explain){
							explanator.append("("+m.getType()+")First buy with volumen "+volumina+"  ("+valueMapping.get(i)+")->("+valueMapping.get(j)+")\n");
						}
						double tempBuy = 0;
						if (m.getType() == MarketType.BID) {
							
							///1. BEGIN start node BID
							tempBuy = (m.getOrders()[orderDepth[nodeCounter]].price * volumina) // calc
									- (m.getOrders()[orderDepth[nodeCounter]].price * volumina * m
											.getTransactionFee());
							// cumulative calc
							for(int cumCounter = -1; cumCounter<orderDepth[nodeCounter];cumCounter++){
							cummulativeToStep[i] += m.getOrders()[orderDepth[nodeCounter]].volume;
							}
							
							
							
							
						} else {
							///2. BEGIN start node ASK
							
							
							tempBuy = (volumina / m.getOrders()[orderDepth[nodeCounter]].price)
									- ((volumina / m.getOrders()[orderDepth[nodeCounter]].price) * m
											.getTransactionFee());
							// cumulative calc
							for(int cumCounter = -1; cumCounter<orderDepth[nodeCounter];cumCounter++){
							cummulativeToStep[i] += m.getOrders()[orderDepth[nodeCounter]].volume;
							}
							
							
							
						}

						calc[j] += tempBuy;
						// System.out.println("BUY:"
						// +valueMapping.get(j)+" With "+volumina
						// +" "+valueMapping.get(i)+" = " + tempBuy);

						calc[i] -= volumina;

					} else {
						double tempBuy = 0;
						
						if(explain)explanator.append("("+m.getType()+") buy with volumen "+calc[i]+"  ("+valueMapping.get(i)+")->("+valueMapping.get(j)+")\n");

						if (m.getType() == MarketType.BID) {
							///3. Further node BID

							tempBuy = (m.getOrders()[orderDepth[nodeCounter]].price * calc[i])
									- (m.getOrders()[orderDepth[nodeCounter]].price * calc[i] * m
											.getTransactionFee());
							// cumulative calc
							for(int cumCounter = -1; cumCounter<orderDepth[nodeCounter];cumCounter++){
							cummulativeToStep[i] += m.getOrders()[orderDepth[nodeCounter]].volume;
							}
							
						} else {
							///4. Further node ASK

							tempBuy = (calc[i] / m.getOrders()[orderDepth[nodeCounter]].price)
									- ((calc[i] / m.getOrders()[orderDepth[nodeCounter]].price) * m
											.getTransactionFee());
							
							// cumulative calc
							for(int cumCounter = -1; cumCounter<orderDepth[nodeCounter];cumCounter++){
							cummulativeToStep[i] += m.getOrders()[orderDepth[nodeCounter]].volume;
							}
							
							

						}

						calc[j] += tempBuy;
						// System.out.println("BUY:"
						// +valueMapping.get(j)+" With "+calc[i]
						// +" "+valueMapping.get(i)+" = " + tempBuy);

						// TODO: calculation
						calc[i] = 0;
						//calc[i] -= calc[i];

					}

					// System.out.println(i+"->"+j);
					// kol[j] = m.orders[0].price;

					// .. end calculation
					nodeCounter++;
					actualNode = nodeMapping.get(actualNode);// return next
																// Value
				} while (actualNode != (Integer) pairs.getKey()); // if equal to
																	// begin
																	// node then
																	// destroy
																	// the cycle
				
	
	}
			return 0;
	}

*/
	
	
	
	@Override
	public void prettyOut() {
		if (true)
			return;

		
	}

	public void start() {
		this.execute("-log", "SILENT", "-ee", "NONE");
	}
	
	



	

}
