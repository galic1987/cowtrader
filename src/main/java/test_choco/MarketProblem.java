package test_choco;




import java.math.BigDecimal;
import java.util.Map;

import models.Order;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.LoggerFactory;

import otradotra.Market;
import otradotra.MarketOrder;
import otradotra.PropMarketsEvalObj;
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


public class MarketProblem extends AbstractProblem  {

	double precision = 1.0e-3;


    double low;
    // graph variable
    private DirectedGraphVar tc;  
	IntVar nbArcs;
	IntVar nbNodes;

    
    //############# static 
    protected Map <Integer,String> valueMapping; // node(n) -> resource
    protected Map <String,Integer> keyMapping; // resource -> node(n)  
    private int n;

    
    //############# changes on iteration 
    protected Market[][] market; //contains orders from markets / buy sell
    protected Map <Integer,Double> resources; // contains available resources node
    
    
    //#dynamic calculation
    private double[] price;

    
    
    public MarketProblem(
    		Market[][]solverdata,
    		Map <Integer,String> valueMapping, 
    		Map <String,Integer> keyMapping,
    		Map <Integer,Double> resources,
    		int n,
    		double low) {
    	market = solverdata;
    	this.valueMapping = valueMapping;
    	this.keyMapping = keyMapping;
    	this.resources = resources;
    	this.n = n;
    	this.low = low;
    }

    @Override
    public void createSolver() {
        solver = new Solver("MarketProblem");
    }

    @Override
    public void buildModel() {
         
    	// 1. make calculation array (where to buy and sell)
       // calculation = VariableFactory.realArray("calculation", n, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, precision, solver);
        price = new double[market.length];
        
        // 2. make directed graph
        tc = VariableFactory.directedGraph("marketGraph", n, solver);
		for (int i = 0; i < n; i++) {
			tc.getEnvelopGraph().activateNode(i);		// potential node
			for (int j = 0; j < n; j++) {
				if (market[i][j]==null){
					
				}else{
					// if not null 
					tc.getEnvelopGraph().addArc(i, j);
				}
			}
		}
		
		// 3. lot of constraints 
		// 3.1 number of arcs can be 3 or n*n (max if everything connected)
		nbArcs = VF.bounded("edgeCount", 3, n * n, solver);
		
		// 3.2 node cound can be 3 or n 
		nbNodes = VF.bounded("nodeCount", 3, n, solver);

		
		// 3.3 in cycle number of nodes must be equal number of arcs
		solver.post(IntConstraintFactory.arithm(nbNodes, "=", nbArcs));
		
		
		//Propagator inv = new PropPathNoCycle(tc, 0, 3);
        //solver.post(inv.getConstraint().getOpposite());

		
		// 3.4 incoming outcoming nodedegree can be 1 in cycle
        Constraint gc = new Constraint("graphy", 
        		new PropKArcs(tc,nbArcs), 
        		new PropKNodes(tc,nbNodes),
        		new PropNodeDegree_AtMost(tc,Orientation.SUCCESSORS,1),
        		new PropNodeDegree_AtMost(tc,Orientation.PREDECESSORS,1),
        		new PropKCC(tc, VF.fixed(1, solver))
        		); // add check if all active nodes in plus
        
      
        
        solver.post(gc);
        
        solver.post(new Constraint("makreti",new PropMarketsEvalObj(tc, market,valueMapping,resources)));
        
       // VF.eq

        
        // 3.5 redundant optimality constraint calculation needs to be 0 or positive everywhere
       if(true)return;
        

     

    }


    @Override
    public void configureSearch() {
//        solver.set(IntStrategyFactory.firstFail_InDomainMin(letters));
    solver.set(GraphStrategyFactory.graphLexico(tc));
    	//solver.set(GraphStrategyFactory.graphRandom(tc, 11));
    }

    @Override
    public void solve() {
        solver.findAllSolutions();
        
        //SMF.log(solver, true, false);

       // for(int i = 1;i<calculation.length;i++){
        	//solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, calculation[1], precision);        
        //}
        	//System.out.println(solver.isSatisfied());

        
        //solver.findSolution();
        //solver.getIbex().release();
		//solver.getIbex().release();

    }

    @Override
    public void prettyOut() {
    	if(true)return;
        

    	
//        LoggerFactory.getLogger("bench").info("Alpha");
//        StringBuilder st = new StringBuilder();
//        st.append("\t");
//        for (int i = 0; i < 26; i++) {
//            st.append(letters[i].getName()).append("= ").append(letters[i].getValue()).append(" ");
//            if (i % 6 == 5) {
//                st.append("\n\t");
//            }
//        }
//        st.append("\n");
//        LoggerFactory.getLogger("bench").info(st.toString());
    }
    
    public void start(){
    	this.execute("-log","QUIET");
    }

    
}
