package otradotra.propagators;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import marketHole.CycleSolutionConfiguration;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.LoggerFactory;

import otradotra.helper.ExplanationSingleton;
import otradotra.helper.ReporterSingleton;
import otradotra.models.Market;
import otradotra.models.MarketType;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.RealVar;
import solver.variables.Variable;
import solver.variables.delta.monitor.GraphDeltaMonitor;
import solver.variables.graph.DirectedGraphVar;
import solver.variables.graph.GraphVar;
import util.ESat;
import util.objects.setDataStructures.ISet;

public class OrderDepthPropagator  extends  Propagator<IntVar> {

	
	
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
//	private int [] snapshotWorking; // last working depth
	
	// out - filled by recursion
	// data about holes
	// market configuration
	private ArrayList<CycleSolutionConfiguration> solutionConfigurations; // out

	//IntVar[] variations;
	
	
	public OrderDepthPropagator(Market[][] solverData,
			int howDeepWillSearchBe, Map<Integer, Integer> cycle,
			Map<Integer, String> valueMapping, Map<Integer, Double> resources,IntVar[] variations) {
		super(variations, PropagatorPriority.LINEAR, false);
		this.solverData = solverData;
		this.howDeepWillSearchBe = howDeepWillSearchBe;
		this.cycle = cycle;
		this.valueMapping = valueMapping;
		this.resources = resources;
		//this.variations = variations;
		
		nodeDepthExplainer = new HashMap<Integer, String>();
		nodeDepthTracker = new HashMap<Integer, Integer>();
		nodeDepthMarket = new HashMap<Integer, Market>();

		for(int i =0;i<vars.length;i++){
			nodeDepthTracker.put(i, 0);
		}
	}
	
	@Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INCLOW.mask;
    }
	
	@Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
	
	//System.out.println(idxVarInProp); 
		
	//System.out.println("EVMASK "+mask);
		
	
		// TODO: optimization point , put it away it is not beeing used
		// only debug
		String configuration = "";
		int orderDepth[] = new int[vars.length];
		for(int i =0;i<vars.length;i++){
			IntVar var = vars[i];
			orderDepth[i] = var.getValue();
			configuration += "-"+orderDepth[i];
		}
		
		
		double value = tryCyclesEvaluation();
		//System.out.println(configuration + " " +value );

		//if value bigger than 0 it is part of solution
		if(value>0){
			
			// put the lowest node tracker
			for(int i =0;i<vars.length;i++){
					if(nodeDepthTracker.get(i)<vars[i].getValue()) nodeDepthTracker.put(i, vars[i].getValue());
			}
			// save snapshotWorking
			
		//System.out.println(configuration + " " +value );
		//System.out.println(nodeDepthExplainer.toString());

		// build configuration and add to array of configurations 
		
		}else{

			
				//variations[failing].updateUpperBound(variations[failing].getValue(), this);
				try {
					int maxVal = vars[idxVarInProp].getValue();
					//System.out.println(configuration + " Setting UpperBound " +maxVal +" Var "+idxVarInProp);

					vars[idxVarInProp].updateUpperBound(maxVal, aCause);
				} catch (ContradictionException e) {
					e.printStackTrace();
				}
				//System.out.println("Prune the depth " + idxVarInProp);					

			}
		
	}
	
		
		// check for depth of market
		// TODO Auto-generated method stub    }
	
	@Override
	public void propagate(int evtmask) throws ContradictionException {
		
	
		
	}
	


	
	
	
	// returns 0 or bigger value if true 
	public double tryCyclesEvaluation() {
		boolean explain = false;

		double[] calc = new double[solverData.length];
		double[] cummulativeToStep = new double[solverData.length];
		double volumina = 1; // try volumina to spin

		for (int i = 0; i < calc.length; i++) {
			calc[i] = 0;
			cummulativeToStep[i] = 0;
		}
		
		// count number of most involved nodes
			//Map<Integer, Integer> numberOfOccurences [] = new (Map<Integer, Integer> nodeMapping)[10];
		
		// go throught cycle
			StringBuffer explanator = null;
			
			// dyno nodes
			int orderDepth[] = new int[vars.length];
			for(int i =0;i<vars.length;i++){
				IntVar var = vars[i];
				orderDepth[i] = var.getValue();
			}
			
			Map<Integer, Integer> nodeMapping = cycle;
			Iterator it = nodeMapping.entrySet().iterator();
			while (it.hasNext()) {
				 
				
				
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
					
					double transfee = 0;
					double buyFromToFillVoluminaWithFee = 0;
					
					int i = actualNode; // actual node
					int j = nodeMapping.get(actualNode); // next node
					
					
					// .. do here the calculation
					// System.out.println(i+"->"+j);
					Market m = solverData[i][j];
					
					// save performance 
					if(!(nodeDepthMarket.get(nodeCounter)!=null)){
						nodeDepthExplainer.put(nodeCounter, valueMapping.get(j));
						nodeDepthMarket.put(nodeCounter	, m);
					}
					
					//  calculation
					if (calc[i] == 0) {
						// head explanation
						 
						double tempBuy = 0;
						if (m.getType() == MarketType.BID) {
							
							///1. BEGIN start node BID
//							tempBuy = (m.getOrders()[orderDepth[nodeCounter]].price * volumina) // calc
//									- (m.getOrders()[orderDepth[nodeCounter]].price * volumina * m
//											.getTransactionFee());    
							
							transfee = volumina -(volumina /(1+m.getTransactionFee()));
							buyFromToFillVoluminaWithFee =  volumina - transfee;
							tempBuy = (m.getOrders()[orderDepth[nodeCounter]].price * buyFromToFillVoluminaWithFee);
							
							// cumulative calc
							for(int cumCounter = -1; cumCounter<orderDepth[nodeCounter];cumCounter++){
							cummulativeToStep[i] += m.getOrders()[orderDepth[nodeCounter]].volume;
							}
							
							 
						} else {
							///2. BEGIN start node ASK
							
							
//							tempBuy = (volumina / m.getOrders()[orderDepth[nodeCounter]].price)
//									- ((volumina / m.getOrders()[orderDepth[nodeCounter]].price) * m
//											.getTransactionFee());
							
							transfee = volumina -(volumina /(1+m.getTransactionFee()));
							buyFromToFillVoluminaWithFee =  volumina - transfee;
							tempBuy = (buyFromToFillVoluminaWithFee / m.getOrders()[orderDepth[nodeCounter]].price);
							
							
							// cumulative calc
							for(int cumCounter = -1; cumCounter<orderDepth[nodeCounter];cumCounter++){
							cummulativeToStep[i] += m.getOrders()[orderDepth[nodeCounter]].volume;
							}
							
							 
							
						}

						calc[j] += tempBuy;
						calc[i] -= volumina;

					} else {
						double tempBuy = 0;
						
 
						if (m.getType() == MarketType.BID) {
							///3. Further node BID

//							tempBuy = (m.getOrders()[orderDepth[nodeCounter]].price * calc[i])
//									- (m.getOrders()[orderDepth[nodeCounter]].price * calc[i] * m
//											.getTransactionFee());
							
							
							transfee =  calc[i] -( calc[i] /(1+m.getTransactionFee()));
							buyFromToFillVoluminaWithFee =   calc[i] - transfee;
							tempBuy = (m.getOrders()[orderDepth[nodeCounter]].price * buyFromToFillVoluminaWithFee);
							
							
							// cumulative calc
							for(int cumCounter = -1; cumCounter<orderDepth[nodeCounter];cumCounter++){
							cummulativeToStep[i] += m.getOrders()[orderDepth[nodeCounter]].volume;
							}
							 
						} else {
							///4. Further node ASK

//							tempBuy = (calc[i] / m.getOrders()[orderDepth[nodeCounter]].price)
//									- ((calc[i] / m.getOrders()[orderDepth[nodeCounter]].price) * m
//											.getTransactionFee());
							
							
							transfee = calc[i] -(calc[i] /(1+m.getTransactionFee()));
							buyFromToFillVoluminaWithFee =  calc[i] - transfee;
							tempBuy = (buyFromToFillVoluminaWithFee / m.getOrders()[orderDepth[nodeCounter]].price);
							
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
						calc[i] -= calc[i];
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
				// it.remove();
				boolean t = false;
				// evaluate the solution
				for (int testCount = 0; testCount < calc.length; testCount++) {
					if (calc[testCount] > 0) {
						t = true;
						// System.out.println("Found soulution "+calc[i]);
						 
						return calc[testCount];

						//break;
					} 
				}
				// TODO: make break here 
				break;
	
	}
			return 0;
	}


	
	
	@Override
	public ESat isEntailed() {
		// TODO Auto-generated method stub
		return ESat.TRUE;
	}
	

	public Map<Integer, Integer> getNodeDepthTracker() {
		return nodeDepthTracker;
	}

	public void setNodeDepthTracker(Map<Integer, Integer> nodeDepthTracker) {
		this.nodeDepthTracker = nodeDepthTracker;
	}

	public Map<Integer, String> getNodeDepthExplainer() {
		return nodeDepthExplainer;
	}

	public void setNodeDepthExplainer(Map<Integer, String> nodeDepthExplainer) {
		this.nodeDepthExplainer = nodeDepthExplainer;
	}

	public Map<Integer, Market> getNodeDepthMarket() {
		return nodeDepthMarket;
	}

	public void setNodeDepthMarket(Map<Integer, Market> nodeDepthMarket) {
		this.nodeDepthMarket = nodeDepthMarket;
	}



}
