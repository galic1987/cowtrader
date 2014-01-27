package otradotra;

import java.util.Date;
import java.util.List;

import otradotra.helper.HttpUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class MarketJsonConnector {
	
	
	Market [] markets;

	// constructor is final
public MarketJsonConnector(String from, String to) {
		super();
	    markets = new Market[2];
	    
	 // ASK
	    markets[0] = new Market();
	    markets[0].setTransactionFee(0.002D);
	    markets[0].setType( MarketType.ASK);
	    markets[0].setFrom(from);
	    markets[0].setTo(to);
	    markets[0].setMarketName(from+"_"+to);

	    
	    // BID
	    markets[1] = new Market();
	    markets[1].setTransactionFee(0.002D);
	    markets[1].setType( MarketType.BID);
	    markets[1].setFrom(to); //### inverse
	    markets[1].setTo(from);//### inverse
	    markets[1].setMarketName(to+"_"+from);

		// TODO Auto-generated constructor stub
	}

public Market[] parseBTCeOrders(String url, String name){
	// open url 
	// create markets
  //  String[]name1 = name.split("_");
    
    // time fast
    markets[0].setGotRequestFromServerDate(new Date());
    markets[1].setGotRequestFromServerDate(new Date());    
    
    String requestResult = HttpUtils.httpGet(url);



    try {
        // Convert the HTTP request return value to JSON to parse further.
        //System.out.println( "result " + requestResult);

    	
        JSONObject jsonResult = JSONObject.fromObject(requestResult);

        	// ASK
            JSONArray ask = jsonResult.getJSONArray("asks");            
            markets[0] = this.parse(markets[0], ask);
            markets[0].setDate(new Date());

            
            // BID
            JSONArray bid = jsonResult.getJSONArray("bids");
            markets[1] = this.parse(markets[1], bid);
            markets[1].setDate(new Date());

            // alles klar
            return markets;

    }	 catch( JSONException je) {
        System.err.println( "Cannot parse json request result: " + je.toString());

        return null;  // An error occured...
    }
    
	// parse JsonData
	// return 
	//return null;
}

@SuppressWarnings("unchecked")
public Market parse(Market mark, JSONArray data){
	mark.orders = new MarketOrder[data.size()];
    int it = 0;
    for (Object iterable_element : data) {
    	MarketOrder m = new MarketOrder();
    	//System.out.println("- " + iterable_element);
    	List<Object> o = (List<Object>)iterable_element;
    	for (int i = 0; i < o.size(); i++) {
    		if (i == 0){
    			try{
    				m.price = (Double) o.get(i);
    			}catch(ClassCastException a){
    				Integer number = (Integer)o.get(i);
    				m.price = number.doubleValue();
    			}
    		}
    		
    		if(i==1){
    			try{
    				m.volume = (Double) o.get(i);
    			}catch(ClassCastException a){
    				Integer number = (Integer)o.get(i);
    				m.volume = number.doubleValue();
    			}    		}
    	}
    	m.total = m.price * m.volume;
    	//System.out.println("total = "+ m.total +" = " + m.price + "*" + m.volume);
    	mark.orders[it] = m;
    	it++;
    }
    
    return mark;
}
	
}
