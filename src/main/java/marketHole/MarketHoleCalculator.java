package marketHole;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import otradotra.models.Market;

public class MarketHoleCalculator {
	
	// in - constructor
	private Market[][] solverData; // market data
	private int howDeepWillSearchBe; // limit of search
	private int numberOfNodes;
	private Map<Integer,Integer> cycle; // cycle for hole calculationg
	private Map<Integer,String> valueMapping; // node currency mapping
	// for some algorithms needed
	private Map<Integer,Double> resources; // resources
	
	
	// internal for calculation
	private Map<Integer,Integer> nodeDepthTracker; // node -> failure depth
	
	
	// out - filled by recursion
	// data about holes
	// market configuration
	private ArrayList<CycleConfigurationModel> solutionConfigurations; // out

		
	// this is called to try recursive search of markets
	public void calculatePriceOnlyWithFixedVolume(){
		// start recursive function
		// TODO: find out start node when calling solution
		//calculatePriceOnlyWithFixedVolumeRecursion(0, 0, 0 ,0 ,0);
		numberOfNodes = 3;
		
		howDeepWillSearchBe = 2;
		
		calculatePriceOnlyWithFixedVolumeRecursion(0, 0);
		System.out.println(fac); 

	}
	
	
	
	int fac = 0;
	private void calculatePriceOnlyWithFixedVolumeRecursion(int level, int num){
		
		
		/*
		Iterator it = cycle.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			// go throught full cycle beginning with actual node
			int actualNode = (Integer) pairs.getKey();
		}
		*/
		
		// FIX: totalNodeNumber

		
		// calculate here 
		
		for(int i = 0; i<numberOfNodes;i++){
			for(int j = 0; j<howDeepWillSearchBe;j++){
				//for(int a = 0; a<numberOfNodes;a++){
					
				//}
			}
		}
		
		if(true)return;
			for(int i = 0;i<howDeepWillSearchBe ;i++){
				System.out.println("Node "+level+" Value"+ i); 
				// here calculate all node possibilites
				fac ++;
				if(level>=numberOfNodes) return;
				calculatePriceOnlyWithFixedVolumeRecursion(level+1, i);
			}
			
		
	}

	
	private void calculatePriceOnlyWithFixedVolumeRecursion(int level, double totalCummulativeVolume, int startNode, int currentNode, int comingFromNode){
		// 1. calculate value add solution on > 0 dont break 
		
		// 2. auto break depth + propagator market depth
		for(int i = level;i< howDeepWillSearchBe;i++){
			 calculatePriceOnlyWithFixedVolumeRecursion(i, totalCummulativeVolume+1, startNode, currentNode, comingFromNode);
		}
		
		
		
		// 3. auto break propagate next node 
		// rest of nodes
			
		int actualNode = currentNode;
		do{
			
			
		actualNode = cycle.get(actualNode);// return next
		// Value
		} while (actualNode != startNode); // if equal to
					// begin
					// node then
					// destroy
					// the cycle
		
		
	}
	  
	
	public MarketHoleCalculator(Market[][] solverData, int howDeepWillSearchBe,
			Map<Integer, Integer> cycle, Map<Integer, String> valueMapping,Map<Integer,Double> resources) {
		super();
		this.solverData = solverData;
		this.howDeepWillSearchBe = howDeepWillSearchBe;
		this.cycle = cycle;
		this.valueMapping = valueMapping;
		this.resources = resources;
	}

}
