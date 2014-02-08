package marketHole;

import java.util.Map;


// try just highest potential with this one 
public class CycleConfigurationModel {
	private Map<Integer,Integer> cycle; // node -> node (arc)
	private Map<Integer,Integer> lowestHoleOrder; // node -> lowestHoleorder
	private double total; // total number
}
