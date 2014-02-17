package test_choco;




import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.LoggerFactory;

import otradotra.helper.ReporterSingleton;
import otradotra.models.Market;
import otradotra.models.MarketOrderDataHolder;
import otradotra.models.MarketType;
import samples.AbstractProblem;
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
import solver.objective.ObjectiveStrategy;
import solver.objective.OptimizationPolicy;
import solver.search.loop.monitors.SMF;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.solution.LastSolutionRecorder;
import solver.search.solution.Solution;
import solver.search.strategy.GraphStrategyFactory;
import solver.search.strategy.ISF;
import solver.search.strategy.IntStrategyFactory;
import solver.search.strategy.selectors.values.RealDomainMiddle;
import solver.search.strategy.selectors.variables.Cyclic;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.AssignmentInterval;
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



public class CalculateOptimalVolumeProblem extends AbstractProblem  {

	private double precision = 0.00001d;

    //############# static 
	private Map <Integer,String> valueMapping; // node(n) -> resource
	private Map <String,Integer> keyMapping; // node(n) -> resource

    
    //############# changes on iteration 
    private Market[][] market; //contains orders from markets / buy sell
    private Map <Integer,Double> resources; // contains available resources node
    private int numberOfNodesInCycle;
    private Map <Integer,Integer> nodeMapping;
    
    
    //############# dynamic calculation
    //how much transfer to each node 
    private RealVar[] transfer;
    private RealVar sum; // optimum 
    
    private double lower = 0;
    private double upper = 1000;
    
    
    private String[] transferMappingString;

    
    // decimal format settings 
    private DecimalFormat df = new DecimalFormat();
    
    
    public CalculateOptimalVolumeProblem(
    		Market[][]solverdata,
    		double value, // value 
    		String currency, // currency
    		Map <Integer,Integer> nodeMapping, // the round
    		int numberOfNodesInCycle,
    		Map <Integer,String> valueMapping,
    		Map <String,Integer> keyMapping,
    		Map <Integer,Double> resources
    		) {
    	this.market = solverdata;
    	this.numberOfNodesInCycle = numberOfNodesInCycle;
    	this.nodeMapping = nodeMapping;
    	this.valueMapping = valueMapping;
    	this.resources = resources;
    	this.keyMapping = keyMapping;
    	
    }

    @Override
    public void createSolver() {
        solver = new Solver("CalculateOptimalVolumeProblem");
    }

    @Override
    public void buildModel() {
    	// 0. settigns 
		df.setMaximumFractionDigits(10);
		df.setGroupingUsed(false);
         
    	// 1. make calculation transfer
    	transfer = VariableFactory.realArray("transfer", numberOfNodesInCycle, lower, upper, precision, solver);
    	transferMappingString = new String[numberOfNodesInCycle];
    	
    	// 2. optimality sum
    	sum = VariableFactory.real("sum", -10, upper, precision, solver);
    	

    	// 3. transfer constraint
    	double volumina = 0;
    	// go throught cycle
        Iterator it = nodeMapping.entrySet().iterator();
		int realArr = 0;

        while (it.hasNext()){
        	Map.Entry pairs = (Map.Entry)it.next();
        		// go throught full cycle beginning with actual node
        		int actualNode = (Integer) pairs.getKey();
        		do {
            		
            		int i = actualNode; // actual node
        			int j = nodeMapping.get(actualNode); // next node
        			
					//.. do here the calculation
            		Market m = market[i][j];
            		volumina = resources.get(actualNode);
            		RealVar[]vars = null;
            		
            		if(realArr == numberOfNodesInCycle - 1){
            			// ending cycle
                 		 vars = new RealVar[]{transfer[realArr],transfer[0]};
            		}else{
            			// cycle not ending
               		 vars = new RealVar[]{transfer[realArr],transfer[realArr+1]};

            		}
            		transferMappingString[realArr] = valueMapping.get(i);
            		
            		
            		
            		String price = df.format(m.getOrders()[0].price);
            		String volume = df.format(m.getOrders()[0].volume);

            		// constraint 1 transfer
            		if(m.getType()==MarketType.BID){
            			// multiply bid
            			solver.post(new RealConstraint(
                				"C_TransferASK"+ realArr,
                				"{1} = ({0} * "+price+") - (({0} * "+price+") * "+m.getTransactionFee()+")",
                    	        Ibex.COMPO, 
                    	        vars)
                    			);
            		}else{
            			// multiply ask
            			solver.post(new RealConstraint(
                				"C_TransferBID"+realArr,
                				"{1} = ({0} / "+price+") - (({0} / "+price+") * "+m.getTransactionFee()+")",
                    	        Ibex.COMPO, 
                    	        vars)
                    			);            		
            		}
            		
            		
            		// constraint 2 resource limit
            		solver.post(new RealConstraint(
            				"C_ResourceLimit"+realArr,
            				"{0} <= "+resources.get(i),
                	        Ibex.COMPO, 
                	        vars)
                			);
            		
            		solver.post(new RealConstraint(
            				"C_ResourceLimitLower"+realArr,
            				"{0} >= 0.1",
                	        Ibex.COMPO, 
                	        vars)
                			);
            		
            		
            		// constraint 3  maxVolumen
            		solver.post(new RealConstraint(
            				"C_MaxVolumen"+realArr,
            				"{0} <= "+volume,
                	        Ibex.COMPO, 
                	        vars)
                			);
            		
        			realArr++; // increment the mapping of realVars
        		} while (actualNode != (Integer) pairs.getKey()); // if equal to begin node then destroy the cycle
               // break; // do cycle once it is enoguh
                

        }
        
        
        // 4. optimality constraint
        RealVar[] packed = new RealVar[transfer.length+1];
    	StringBuffer constraintSum = new StringBuffer("");
    	for(int i =0;i<transfer.length;i++){
			double weightDouble = ReporterSingleton.getValue(transferMappingString[i], 1, ReporterSingleton.balancingCurrency, market, keyMapping);
			String weight = df.format(weightDouble);
			if(i<transfer.length-1){
    			constraintSum.append(" ({"+i+"} * "+weight+" ) * +");
        		packed[i] = transfer[i];
    		}else{
    			// end + sum in packed array
    			constraintSum.append(" ({"+i+"} * "+weight+" ) = " + "{"+(i+1)+"}");
        		packed[i] = transfer[i];
    		}
    	}
    	
		packed[transfer.length] = sum;

        
    	solver.post(new RealConstraint("C_Optimality",
    	        constraintSum.toString(),
    	                Ibex.COMPO, packed)
    			);

    }


    @Override
    public void configureSearch() {
      //  solver.set(IntStrategyFactory.random(hack, 24124124));
   // solver.set(GraphStrategyFactory.graphLexico(tc));
    	//solver.set(GraphStrategyFactory.graphRandom(tc, 11));
       solver.set(new AssignmentInterval(transfer, new Cyclic(transfer), new RealDomainMiddle()));
//solver.set(n);
		SearchMonitorFactory.limitTime(solver,10000);

    }

    @Override
    public void solve() {
        //solver.findAllSolutions();
        
        //SMF.log(solver, true, false);

       // for(int i = 1;i<calculation.length;i++){
    	try{
        	solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, sum, precision);        
        //}
    	}catch(Exception e){
    		System.out.println(e.toString()); 
    	}
        	//System.out.println(solver.isSatisfied());

        	for(int i = 0;i<transfer.length;i++){
        		System.out.println(transferMappingString[i] +" " + transfer[i]);
        	}
       // System.out.println(transfer[1]);
        //solver.findSolution();
		//solver.getIbex().release();

    }

    @Override
    public void prettyOut() {
		solver.getIbex().release();


    	
    }
    
    public void start(){
    	this.execute("-log","SOLUTION", "-ee", "NONE");
    }

    
}
