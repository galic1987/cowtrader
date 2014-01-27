package otradotra.helper;

import java.util.HashMap;
import java.util.Map;

import otradotra.Market;


public class ReporterSingleton {
	   private static ReporterSingleton instance = null;
	   
	   
	   public static int numberOfSoultions = 0;
	   public static double highestValue = 0;
	   public static double totalValue = 0;
	   public static Map <Integer,SolutionEvaluator> snapShot = new HashMap <Integer,SolutionEvaluator> ();
	   
	   // prazniti 
	   public static double roundDown = -10;
	   public static Map <Integer,Integer> roundAround = new HashMap <Integer,Integer> ();
	   public static String roundValuta;
	   
	   
	   protected ReporterSingleton() {
	      // Exists only to defeat instantiation.
	   }
	   public static ReporterSingleton getInstance() {
	      if(instance == null) {
	         instance = new ReporterSingleton();
	      }
	      return instance;
	   }
	   
	   public static void newSolution(double value, String valuta, Market[][]m, Map <Integer,Integer> nodeMapping){
		   if(value!=ReporterSingleton.highestValue){
			   snapShot.put(numberOfSoultions, new SolutionEvaluator(value, valuta, nodeMapping, m));
			   ReporterSingleton.numberOfSoultions++;
			   ReporterSingleton.totalValue += value;
		   }	
		   if(value>ReporterSingleton.highestValue){
			   ReporterSingleton.highestValue = value;
		   }
		   
	   }
	   
	   public static void downRound(double value,Map <Integer,Integer> roundAround,String roundValuta){
		   if(value>ReporterSingleton.roundDown){
			   ReporterSingleton.roundDown = value;
			   ReporterSingleton.roundAround = roundAround;
			   ReporterSingleton.roundValuta = roundValuta;
		   }
	   }
	   
	   
	   
	}