package otradotra.helper;

import java.util.HashMap;
import java.util.Map;

import otradotra.models.Market;

public class SolutionEvaluator {
	double number;
	String valuta;
	Map <Integer,Integer> nodeMapping = new HashMap <Integer,Integer> ();
	Market[][] m = null;
	
	
	public SolutionEvaluator(double number, String valuta,
			Map<Integer, Integer> nodeMapping, Market[][] m) {
		super();
		this.number = number;
		this.valuta = valuta;
		this.nodeMapping = nodeMapping;
		this.m = m;
	}
	
}
