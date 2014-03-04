package otradotra.helper;

import java.lang.reflect.Array;
import java.util.Arrays;

public class DataMarketIntegrityCheck {

	
	// it is sorted
	public static double maxDistanceMs(long[] receivedData){
		// recieved time 
		Arrays.sort(receivedData);
		long diff =  receivedData[receivedData.length-1] - receivedData[0];
		
		double maxDiff = diff; 
		return maxDiff;
	}
	
	
}
