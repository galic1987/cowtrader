
package otradotra;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.LoggerFactory;

import otradotra.helper.ReporterSingleton;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.RealVar;
import solver.variables.Variable;
import solver.variables.delta.monitor.GraphDeltaMonitor;
import solver.variables.graph.DirectedGraphVar;
import solver.variables.graph.GraphVar;
import util.ESat;
import util.objects.setDataStructures.ISet;

/**
 * Compute the cost of the graph by summing edge costs
 * Supposes that each node must have two neighbors (cycle)
 * - For minimization problem
 */
public class PropMarketsEvalObj extends Propagator {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected DirectedGraphVar g;
    protected int n;
    
    
    //############# static 
    protected Map <Integer,String> valueMapping; // node(n) -> resource
    protected Map <String,Integer> keyMapping; // resource -> node(n)  
    
    //############# changes on iteration 
    protected Market[][] market; //contains orders from markets / buy sell
    protected Map <Integer,Double> resources; // contains available resources node
    
    //############ each node recalculate
    // this could be RealVar[] with constraint each >= 0
    
    // additionally tryvalue can be bounded between - soft constraints for each arc
    // protected double[][] tryvalue;

    GraphDeltaMonitor gdm;
    double lowest_diff;


    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    
	public PropMarketsEvalObj(DirectedGraphVar graph, Market[][] markett, Map <Integer,String> valueMapping, Map <Integer,Double> resources) {
        super(new Variable[]{graph}, PropagatorPriority.LINEAR, true);
        
        g = graph;
        n = g.getEnvelopGraph().getNbNodes();
        market = markett;
        gdm = (GraphDeltaMonitor) g.monitorDelta(this);
        this.valueMapping = valueMapping;
        this.resources = resources;

    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
    	if(idxVarInProp==0){
			gdm.freeze();
			if ((mask & EventType.ENFORCEARC.mask) != 0) {

				//gdm.forEachArc(arcEnforced, EventType.ENFORCEARC);
			}
			if ((mask & EventType.REMOVEARC.mask) != 0) {
				//gdm.forEachArc(arcRemoved, EventType.REMOVEARC);
			}
			gdm.unfreeze();
		}        //idempotent
    	/*if(EventType.REMOVEARC.mask == mask){
    		System.out.println("remove arc " + idxVarInProp );
    	return;
    	}
    	
    	if(EventType.ENFORCEARC.mask == mask){
    		System.out.println("enforce arc " + idxVarInProp );
return;
    	}
    	
    		System.out.println("both en remove arc operation mask "+ mask + " var" + idxVarInProp );

*/
    		
    }

    @Override
    public int getPropagationConditions(int vIdx) {
    	// add 
        return EventType.REMOVEARC.mask+ EventType.ENFORCEARC.mask;
    }

    @Override
    public ESat isEntailed() {
    	
    	// init set calculation to default
    	double [] calc = new double[market.length];
    	double [] kol = new double[market.length];
    	double volumina = 1; // try volumina to spin

    	for(int i=0;i<calc.length;i++){
    		calc[i] = 0;
    		kol[i] = 0;
        }
    	
    	// make hashmap
    	Map <Integer,Integer> nodeMapping  = new HashMap <Integer,Integer> (); //list of nodes 
    	ISet act1 = g.getKernelGraph().getActiveNodes();
        for (int i = act1.getFirstElement(); i >= 0; i = act1.getNextElement()) {
        	
        	ISet arcs = g.getKernelGraph().getSuccessorsOf(i);
        	for (int j = arcs.getFirstElement(); j >= 0; j = arcs.getNextElement()) {
        		nodeMapping.put(i, j);
        	}
        }
        
        // go throught cycle
        Iterator it = nodeMapping.entrySet().iterator();
        while (it.hasNext()){
        	Map.Entry pairs = (Map.Entry)it.next();
        		// go throught full cycle beginning with actual node
        		int actualNode = (Integer) pairs.getKey();
        		volumina = resources.get(actualNode);
        		for(int i=0;i<calc.length;i++){
            		calc[i] = 0;
            		kol[i] = 0;
                }
        		do {
        			int i = actualNode; // actual node
        			int j = nodeMapping.get(actualNode); // next node
					//.. do here the calculation
            		//System.out.println(i+"->"+j);
            		Market m = market[i][j];
                	// last calculation
            		if(calc[i]==0){
            			//System.out.println(m.type);
            			
            			double tempBuy = 0;
            			if(m.type==MarketType.BID){
            				 tempBuy =  (m.orders[0].price * volumina) -  (m.orders[0].price *volumina * m.transactionFee);
            			}else{
                			 tempBuy =  (volumina/ m.orders[0].price) -  ((volumina/ m.orders[0].price) * m.transactionFee);
            			}
            			
            			calc[j] += tempBuy;
                    	//System.out.println("BUY:" +valueMapping.get(j)+" With "+volumina +" "+valueMapping.get(i)+" = " + tempBuy);

                		calc[i] -= volumina;

            		}else{
            			double tempBuy = 0;
            			if(m.type==MarketType.BID){
            				 tempBuy =  (m.orders[0].price * calc[i]) -  (m.orders[0].price *calc[i] * m.transactionFee);
            			}else{
                			 tempBuy =  (calc[i]/ m.orders[0].price) -  ((calc[i]/ m.orders[0].price) * m.transactionFee);
            			}
            			
                    	calc[j] += tempBuy;
                    	//System.out.println("BUY:" +valueMapping.get(j)+" With "+calc[i] +" "+valueMapping.get(i)+" = " + tempBuy);

                    	calc[i] -= calc[i];	

            		}
                	
            		//System.out.println(i+"->"+j);
                	//kol[j] = m.orders[0].price;
                	
        			//.. end calculation
        			actualNode = nodeMapping.get(actualNode);// return next Value
				} while (actualNode != (Integer) pairs.getKey()); // if equal to begin node then destroy the cycle
        	//it.remove();
        		boolean t = false;
                // evaluate the solution
                for(int i=0;i<calc.length;i++){
                	if(calc[i]>0) {
                		t = true;
                		//System.out.println("Found soulution "+calc[i]);

                		break;
                	}else{
                		//System.out.println("Found soulution "+calc[i]);
                		//for(int gg=0;gg<calc.length;gg++){
                		if(calc[i]!=0){
                    		//System.out.print(valueMapping.get(i)+":"+new BigDecimal(calc[i]).toString()+" ");
                    		//if(calc[gg]>lowest_diff) lowest_diff = calc[gg];
                    	//}
                		//System.out.println("");
                		}

                	}
                }
                
                if(t){
					
                	for(int i=0;i<calc.length;i++){
                		if(calc[i]>0)ReporterSingleton.newSolution(calc[i], valueMapping.get(i), market, nodeMapping);
                		//System.out.print(valueMapping.get(i)+":"+new BigDecimal(calc[i]).toString());
                		//System.out.println("");
                		//if(calc[i]<lowest_diff) lowest_diff = calc[i];

                	}
                }

        }

        //System.out.println("lowest "+lowest_diff);
        
        for(int gg=0;gg<calc.length;gg++){
    		//System.out.print(valueMapping.get(gg)+":"+new BigDecimal(calc[gg]).toString()+" ");
    		if(calc[gg]!=0)ReporterSingleton.downRound(calc[gg],nodeMapping,valueMapping.get(gg));
    	}
		//System.out.println("");
        
        if (!g.instantiated()) {
            return ESat.UNDEFINED;
        }
        return ESat.TRUE;       
    }
    
    

    @Override
    public void propagate(int evtmask) throws ContradictionException {
    	// update whole graph
    	// for every node 
    	// make input rules 
    	// make output rules
    	//
    	
    	//for(int i = 0; i<n;)
        // calculate
    }

    protected void filter(int minSum) throws ContradictionException {
        
    }

    
   

}