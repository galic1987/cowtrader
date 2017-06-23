package otradotra.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import otradotra.models.Market;
import otradotra.models.MarketOrderDataHolder;
import otradotra.models.MarketType;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class MarketJsonConnector {
	
    URL requestURL;
    HttpURLConnection connection = null;

	
    ObjectMapper objectMapper = null;
	int maxNumberOfOrders = 50;
	final Market [] markets;
	
	// start end in long 
	private long start = 0;
	private long end = 0;
	

	// constructor is final
public MarketJsonConnector(String from, String to, double fee, int pricePrecisionDecimal) {
		super();
	    markets = new Market[2];
	    objectMapper = new ObjectMapper();
	 // ASK
	    markets[0] = new Market();
	    markets[0].setTransactionFee(fee);
	    markets[0].setType( MarketType.ASK);
	    markets[0].setFrom(from);
	    markets[0].setTo(to);
	    markets[0].setMarketName(from+"_"+to);
	    markets[0].setPriceDecimalPrecision(pricePrecisionDecimal);


	    
	    // BID
	    markets[1] = new Market();
	    markets[1].setTransactionFee(fee);
	    markets[1].setType( MarketType.BID);
	    markets[1].setFrom(to); //### inverse
	    markets[1].setTo(from);//### inverse
	    markets[1].setMarketName(from+"_"+to);
	    markets[1].setPriceDecimalPrecision(pricePrecisionDecimal);

	    

		// TODO Auto-generated constructor stub
	}

public Market[] parseBTCeOrders(String url, String name) throws Exception{
	// open url 
	// create markets
  //  String[]name1 = name.split("_");
    
	
    // time fast
    
    
    //String requestResult = HttpUtils.httpGet(url);
    //String requestResult = this.httpGet(url);
    
    //markets[0].setGotRequestFromServerDate(new Date());
    //markets[1].setGotRequestFromServerDate(new Date());   
//    try{
//	long nanos = System.nanoTime();
//    
//    String requestResult = this.apacheHttpGet(url);

    
//    long duration = System.nanoTime() - nanos;
//	int seconds = (int) (duration / 1000000000);
//	int milliseconds = (int) (duration / 1000000) % 1000;
//	int nanoseconds = (int) (duration % 1000000);
//	System.out
//			.printf(markets[0].getMarketName()+" Internet Time: %d seconds, %d milliseconds en %d nanoseconds\n",
//					seconds, milliseconds, nanoseconds);

	setStart(System.currentTimeMillis());
	String strTemp = this.apacheHttpGet(url);
	Market [] mrTemp =jacksonWayParsing(strTemp);
	setEnd(System.currentTimeMillis());

	return mrTemp;
	
	
//    }catch(Exception e){
//    	e.printStackTrace();
//    	return null;
//    }
//    
    
    
	// parse JsonData
	// return 
	//return null;
}

public Market[] jacksonWayParsing(String requestResult) throws JsonProcessingException, IOException{
	        // Convert the HTTP request return value to JSON to parse further.
	        //System.out.println( "result " + requestResult);
		 // ObjectMapper objectMapper =  JSONParsingOptimizationSingleton.getMapper();
		  
		//read JSON like DOM Parser 
		  JsonNode rootNode = objectMapper.readTree(requestResult);

		  
	        	// ASK
	        	JsonNode ask = rootNode.path("asks");
	            markets[0] = this.parseJackson(markets[0], ask);
	            //markets[0].setDate(new Date()); set microtime instead TODO: micro

	            
	            // BID
	  		  	JsonNode bid = rootNode.path("bids");
	            markets[1] = this.parseJackson(markets[1], bid);
	           // markets[1].setDate(new Date());set microtime instead TODO: micro

	            rootNode = null;
	            requestResult = null;
	            
	            // alles klar
	            return markets;

	   
}



@SuppressWarnings("unchecked")
public Market parseJackson(Market mark, JsonNode data){
	//mark.orders = new MarketOrder[data.size()];
	mark.setOrders(new MarketOrderDataHolder[maxNumberOfOrders]);
    int it = 0;
    
    Iterator<JsonNode> elements = data.getElements();
	  while(elements.hasNext()){
	      JsonNode iterable_element = elements.next();

	  
    	MarketOrderDataHolder m = new MarketOrderDataHolder();
    	//System.out.println("- " + iterable_element);
    	Iterator<JsonNode> ordersPairs = iterable_element.getElements();
    	int i = 0;
   	   	while(ordersPairs.hasNext()){
  	      JsonNode myOrder = ordersPairs.next();

	   	   	if (i == 0){
	   	   		m.price = myOrder.getDoubleValue();
	   	   	}else{
	   	   		m.volume = myOrder.getDoubleValue();
	   	   	}
	   	i++;
	   	}
      	// TODO: total is nothing
    	//m.total = m.price * m.volume;
    	//System.out.println("total = "+ m.total +" = " + m.price + "*" + m.volume);
    	mark.getOrders()[it] = m;
	  
    	it++;
    	if(it==maxNumberOfOrders){
    		break;
    	}
    	
    }
    data = null;
    
    return mark;
}



public Market[] standardWayParsing(String requestResult){
		  try {
		        // Convert the HTTP request return value to JSON to parse further.
		        //System.out.println( "result " + requestResult);

		    	
		        JSONObject jsonResult = JSONObject.fromObject(requestResult);

		        	// ASK
		            JSONArray ask = jsonResult.getJSONArray("asks");         
		            
		            // BID
		            JSONArray bid = jsonResult.getJSONArray("bids");
		            
		            
		            
		            // swap  everything because 
		            // market(buy) <---- my sell
		            // market(sell) <---- my buy 
		            
		            markets[0] = this.parse(markets[0], ask); 
		           // markets[0].setDate(new Date());
		            markets[1] = this.parse(markets[1], bid);
		           // markets[1].setDate(new Date());

		            jsonResult = null;
		            requestResult = null;
		            
		            // alles klar
		            return markets;

		    }	 catch( JSONException je) {
		        System.err.println( "Cannot parse json request result: " + je.toString());

		        return null;  // An error occured...
		    }
}


@SuppressWarnings("unchecked")
public Market parse(Market mark, JSONArray data){
	//mark.orders = new MarketOrder[data.size()];
	mark.setOrders(new MarketOrderDataHolder[maxNumberOfOrders]);
    int it = 0;
    
    for (Object iterable_element : data) {
    	MarketOrderDataHolder m = new MarketOrderDataHolder();
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
    			}    		
    			}
    	}
    	// TODO: total is nothing
    	//m.total = m.price * m.volume;
    	//System.out.println("total = "+ m.total +" = " + m.price + "*" + m.volume);
    	mark.getOrders()[it] = m;
    	it++;
    	if(it==maxNumberOfOrders)break;
    }
    data = null;
    
    return mark;
}



public String httpGet( String url ) {
    String agent = "Mozilla/4.0";  // Bitstamp seems to require this as an example.
    BufferedReader reader;
    String currentLine;
    StringBuffer result = new StringBuffer();

    // Check, if we should trust all SSL certs and enable the fix if necessary.
   /* if( TRUST_ALL_SSL_CERTS && ( _trustAllCerts == null)) {
        installAllCertsTruster();
    }*/

    try {
    	requestURL = new URL( url);
       
        connection = (HttpURLConnection)requestURL.openConnection();
        connection.setUseCaches(false);
        

    } catch( MalformedURLException me) {
        System.err.println( "URL format error: " + url);

        return null;
    }catch( IOException ioe) {
        System.err.println( "Cannot open URL: " + url);

        return null;
    }

   

    connection.setRequestProperty( "User-Agent", agent );
   
    // Add the additional headerlines, if there were any given.
   /* if( headerlines != null) {
        for( Map.Entry<String, String> entry : headerlines.entrySet()) {
            connection.setRequestProperty( entry.getKey(), entry.getValue());
        }
    }
    */
     
    try {
        connection.setRequestMethod("GET");

        reader = new BufferedReader( new InputStreamReader( connection.getInputStream()));

        while( ( currentLine = reader.readLine()) != null) {
            result.append( currentLine);
        }
        reader.close();

    } catch( ProtocolException pe) {
        System.err.println( "Wrong protocol for URL: " + pe.toString());

        result = null;  // return null

    }  catch( IOException ioe) {

        System.err.println( "I/O error while reading from URL: " + url + "\n" + ioe.toString());

        Scanner scanner = new Scanner( connection.getErrorStream());  // Get a stream for the error message.

        scanner.useDelimiter("\\Z");

        String response = scanner.next();  // Get the error message as text.

        System.out.println( "DEBUG: Server error: " + response);

        result = null;  // return null
    } finally {
        if( connection != null) {
            //connection.disconnect();
        }
    }

    return result != null ? result.toString() : null;
 }
	


public String apacheHttpGet(String url) throws Exception{
	 BufferedReader reader;
	    String currentLine;
	    StringBuilder result = new StringBuilder();
	    
	  InputStream instream = null;
	HttpGet httpget = new HttpGet(url);
	CloseableHttpClient httpClient = NetworkOptimizatorSingleton.createConnetor();
	CloseableHttpResponse response = httpClient.execute(httpget);
	try {
	    HttpEntity entity = response.getEntity();
	    if (entity != null) {
	      instream =  entity.getContent();
	        
	        	
	            //box.setText("Getting data ...");
	            reader = new BufferedReader( new InputStreamReader( instream));

	            while( ( currentLine = reader.readLine()) != null) {
	                result.append( currentLine);
	            }
	            reader.close();
	          
	            
	            // do something useful
	        	//System.out.println(content);
	       
	    }
	} finally {
	    response.close();
        instream.close();
        //httpClient.close();
	}
	return result.toString();

}

public long getStart() {
	return start;
}

public void setStart(long start) {
	this.start = start;
}

public long getEnd() {
	return end;
}

public void setEnd(long end) {
	this.end = end;
}

}
