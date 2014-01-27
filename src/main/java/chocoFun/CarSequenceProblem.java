package chocoFun;


import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;

import samples.AbstractProblem;
//import samples.integer.Alpha;
//import samples.integer.Knapsack;
//import samples.integer.Knapsack.Data;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.constraints.LogicalConstraintFactory;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.nary.nValue.Differences;
import solver.constraints.set.SetConstraintsFactory;
import solver.objective.ObjectiveStrategy;
import solver.objective.OptimizationPolicy;
import solver.search.strategy.IntStrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.RealVar;
import solver.variables.SetVar;
import solver.variables.VF;
import solver.variables.VariableFactory;
import solver.variables.graph.UndirectedGraphVar;
import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetFactory;


public class CarSequenceProblem extends AbstractProblem  {

    IntVar[] letters;
    
    
    IntVar[] assembly;

    
    // graph variable
    private UndirectedGraphVar graphvar;
    // five nodes are involved
    private int n = 5;
    RealVar x1;
    
    @Override
    public void createSolver() {
        solver = new Solver("Alpha");
    }

    @Override
    public void buildModel() {
    	 double precision = 1.0e-6;
        x1 = VariableFactory.real("x", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, precision, solver);
         
   /* IntVar t = VF.fixed(1, solver);
    IntVar [] types =  new IntVar[4];
    
    types[0] = VF.fixed("a", 0, solver);
    types[1] = VF.fixed("b", 1, solver);
    types[2] = VF.fixed("c", 2, solver);
    types[3] = VF.fixed("d", 3, solver);
    
    
    assembly = new IntVar[50];
    for (int i = 0; i < 50; i++) {
    	assembly[i] = VariableFactory.bounded("t" + i, 0, 50, solver);
    }
    
 // solver.post(IntConstraintFactory.nvalues(assembly, VF.bounded("c", 0, 3, solver),  Differences.NONE));
 // solver.post(IntConstraintFactory.nvalues(assembly, VF.bounded("c", 0, 5, solver),  Differences.ALL));
  
   // solver.post(IntConstraintFactory.among(VF.fixed(20, solver), types, new int[]{0,1,2,3}));
    //SetConstraintsFactory.cardinality(t, CARD);
   // IntConstraintFactory.s
   // LogicalConstraintFactory.se
   // GraphConstraintFactory.
   // SetConstraintsFactory.s
    
    */
    	
    // BTC/USD	
    int btcusd_buy [] = new int [] {801,1};
    int btcusd_sell [] = new int [] {790,1};
    
    // LTC/BTC
    int ltcbtc_buy [] = new int [] {11,1};
    int ltcbtc_sell [] = new int [] {9,1};

    // LTC/USD
    int ltcusd_buy [] = new int [] {30,1};
    int ltcusd_sell [] = new int [] {29,1};

    
    
    boolean[][] link = new boolean[n][n];
    link[1][2] = true;
    link[2][3] = true;
    link[2][4] = true;
    link[1][3] = true;
    //link[1][4] = true;
    link[3][4] = true;

    IntVar x = VF.bounded("x", 0, 1000, solver);
    IntVar y = VF.bounded("y", -1000, 1000, solver);
    IntVar z = VF.bounded("z", 0, 1000, solver);
    
    
    solver.post(IntConstraintFactory.arithm(x, "=", y));
    solver.post(IntConstraintFactory.distance(x, z, ">", 512));
   // solver.post(IntConstraintFactory.distance(y, z, "=", 1));

    	
    	

    }

    private IntVar[] extract(String word) {
        IntVar[] ivars = new IntVar[word.length()];
        for (int i = 0; i < word.length(); i++) {
            ivars[i] = letters[word.charAt(i) - 97];
        }
        return ivars;
    }

    @Override
    public void configureSearch() {
//        solver.set(IntStrategyFactory.firstFail_InDomainMin(letters));
    }

    @Override
    public void solve() {
        solver.findAllSolutions();
    }

    @Override
    public void prettyOut() {
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

    public static void main(String[] args) {
        new CarSequenceProblem().execute(args);
    }
}
