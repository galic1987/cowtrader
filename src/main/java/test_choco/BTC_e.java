package test_choco;

import java.awt.font.NumericShaper;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.client.utils.HttpClientUtils;

import otradotra.Market;
import otradotra.MarketJsonConnector;
import otradotra.MarketOrder;
import otradotra.helper.HttpUtils;
import otradotra.helper.MarketNameHelper;
import otradotra.helper.ReporterSingleton;
import otradotra.helper.SolutionEvaluator;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Hello world!
 *
 */

public class BTC_e 
{
	
	// Market connection data
	private static long _nonce;
	private static String _key;
	private static String _secret;

	// setup optimization costs
	private static MarketNameHelper [] allMarkets;
	private static Map <Integer,String> valueMapping;
	private static Map <String,Integer> keyMapping;
	private static MarketJsonConnector[] connector;
	private static int numberOfNodes;
	private static final double low = -10; // lowest difference
	
	// changable
	private static Market [] [] solverData;
	private static Map <Integer,Double> resources = null; // can be asked what via value mapping
	
    public static void main( String[] args ) throws InterruptedException
    {
        // 0. Market connection data
        _key = "FLSE6TVR-4JDIPJB8-S7WH5EZ6-HDFNXFXG-RR1FYGEG";
        _secret = "00fe262a56e0225d9b50c197e62556ef39a0d1f0ba3f81b814c45af962d0c9f3";
        
        Map<String, String> arguments = new HashMap<String,String>();
        //arguments.put("getInfo", "");
        //low = -100;
        // my info
        //System.out.println(authenticatedHTTPRequest("getInfo", arguments));
        
        // myactive orders
        //System.out.println(authenticatedHTTPRequest("ActiveOrders", arguments));
      
    	ReporterSingleton.getInstance();

        // init setup
        setup();
        
        //for(int i = 0; i<100; i++){
        while(true){
    		ReporterSingleton.roundDown = -10; // reset the round settings
    		ReporterSingleton.roundAround = null;
    		ReporterSingleton.roundValuta = "";
            
    	// lets execute multithreading (get data from internet)
        executeThis();
        resources = getResources(numberOfNodes, valueMapping);
        
        MarketProblem problem = new MarketProblem(solverData, valueMapping, keyMapping, resources,numberOfNodes,low);
        problem.start();
        System.out.println("Number of solutions " + ReporterSingleton.numberOfSoultions);
        System.out.println("Lowest " + new BigDecimal(ReporterSingleton.roundDown));
        System.out.println("Cycle " + ReporterSingleton.roundAround);
        Iterator it =  ReporterSingleton.roundAround.entrySet().iterator();
        while (it.hasNext()) {
        	
        	Map.Entry pairs = (Map.Entry)it.next();
        	int node = (Integer) pairs.getKey();
        	int arc = (Integer) pairs.getValue();

            System.out.print(valueMapping.get(node)+"-->"+valueMapping.get(arc));
        }
        System.out.println("");
        System.out.println("Valuta " + ReporterSingleton.roundValuta);

        //Thread.sleep(100);
       // printNice();

        }
       
        // solver needs
        // Market [int] [int] -  arc connections, null if no connection
        // HashMap valueMapping <int, String> ->> 0 = USD
        // HashMap keyMapping   <String, int> ->> USD = 0
        
        //stream
       // Market[] mark =  connector.parseBTCeOrders("https://btc-e.com/api/2/ltc_usd/depth", "ltc_usd");
       // Market[] mark2 =  connector.parseBTCeOrders("https://btc-e.com/api/2/btc_eur/depth", "btc_eur");
        
        
    	//System.out.println("total = "+ mark[1].getOrders()[0].total +" = " + mark[1].getOrders()[0].price + "*" + mark[1].getOrders()[0].volume);

        
        // 3. build market matrix
        /////////
        // market iteration
//    	MarketOrder[] ord = mark[1].getOrders();
//    	for(int i = 0; i<ord.length;i++){
//    		MarketOrder m1 = ord[i];
//        	System.out.println(i +" total = "+ m1.total +" = " + m1.price + "(price) * " + m1.volume + " (vol)");
//
//    	}
//    	
//    	
//    	ord = mark[0].getOrders();
//    	for(int i = 0; i<ord.length;i++){
//    		MarketOrder m1 = ord[i];
//        	System.out.println(i +" total = "+ m1.total +" = " + m1.price + "(price) * " + m1.volume + " (vol)");
//
//    	}
    	
    	
    	// 4. start solver 
    	
    	
    	// 5. display possible solutions
    	
    	
        

    }
    
    public static void executeThis(){
        // 2. Get stream threadpool market executive - try to get consistent data (important-> thread execution!!)
        // number of threads == number of markets
        ExecutorService executor = Executors.newFixedThreadPool(allMarkets.length);
        //ExecutorService executor = Executors.newCachedThreadPool();
        
    	long nanos = System.nanoTime();

        for (int i = 0; i < allMarkets.length; i++) {
        	// put matrix addresses 
        	final int trans = i;
        	

        	executor.execute(new Runnable() {
			    public void run() {
        	       // System.out.println("Thread executing " + trans);
        	        //solverData.clone();
        	        // ask/bid
        	        // btc/usd
        	        Market[] marketTemp = connector[trans].parseBTCeOrders(allMarkets[trans].url, allMarkets[trans].bid);
        	        solverData[keyMapping.get(marketTemp[0].getTo())][keyMapping.get(marketTemp[0].getFrom())] = marketTemp[0]; 
        	        solverData[keyMapping.get(marketTemp[1].getTo())][keyMapping.get(marketTemp[1].getFrom())] = marketTemp[1]; 
        	        
        	        
        	        // System.out.println("--> Thread ending " + trans);

        	    }
        	});

           // Runnable worker = new WorkerThread('' + i);
           // executor.execute(worker);
          }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        
        
        long duration = System.nanoTime() - nanos;
        int seconds = (int) (duration / 1000000000);
        int milliseconds = (int) (duration / 1000000) % 1000;
        int nanoseconds = (int) (duration % 1000000);
        System.out.printf("%d seconds, %d milliseconds en %d nanoseconds\n", seconds, milliseconds, nanoseconds);
        
        
    }
    
    public static void printNice(){
    	System.out.print("   ");
		
        for(int i=0;i<solverData.length;i++){
        	System.out.print(" "+valueMapping.get(i)+" ");
        }
        
        for(int i=0;i<solverData.length;i++){
            System.out.println("");
        	System.out.print(valueMapping.get(i)+"");

            for(int j=0;j<solverData[i].length;j++){
             Market m = solverData[i][j];
             
             //if(m != null)System.out.print(m.getFrom() + "->" + m.getTo() + " ");
             if(m != null)System.out.print("  1  ");
             if(m == null)System.out.print("  x  ");

            }
        }
    }
    
    public static final Map <Integer,Double> getResources(int numberOfResources,Map <Integer,String> valueMap){
    	
    	
    	Map <Integer,Double> resources = new HashMap <Integer,Double> ();
    	//TODO: impelement the function 
    	// make example 
    	
    	for(int i=0;i<numberOfResources;i++){
    		
    	}
    	///[nmc=6, eur=3, rur=2, usd=1, ltc=4, ppc=7, nvc=5, btc=0]
    	resources.put(0, 0.1216);// btc
    	resources.put(1, 100.0); //
    	resources.put(2, 3511.10);
    	resources.put(3, 133.20);
    	resources.put(4, 4.44444);
    	resources.put(5, 7.39);
    	resources.put(6, 17.857);
    	resources.put(7, 17.69);

    	
    	// make 5 of each // release -> set each to 0
    	
    	
    	// release -> invoke market get data set iterate and fill if not 
    	
    	return resources;
    }
    
    public static final void setup(){
    	 /***** 
         * START : start of the program - not changeable
         */
        
     // truststore fix multithreading 
        String fixble = HttpUtils.httpGet("https://btc-e.com/api/2/btc_usd/depth");
        // 1. List of markets / links / marketdata
        allMarkets = new MarketNameHelper[15];
        	// btc
        allMarkets[0] = new MarketNameHelper("btc", "usd", "https://btc-e.com/api/2/btc_usd/depth");
        allMarkets[1] = new MarketNameHelper("btc", "rur", "https://btc-e.com/api/2/btc_rur/depth");
        allMarkets[2] = new MarketNameHelper("btc", "eur", "https://btc-e.com/api/2/btc_eur/depth");
        
       
        
        	// ltc
        allMarkets[3] = new MarketNameHelper("ltc", "btc", "https://btc-e.com/api/2/ltc_btc/depth");
        allMarkets[4] = new MarketNameHelper("ltc", "usd", "https://btc-e.com/api/2/ltc_usd/depth");
        allMarkets[5] = new MarketNameHelper("ltc", "rur", "https://btc-e.com/api/2/ltc_rur/depth");
        allMarkets[6] = new MarketNameHelper("ltc", "eur", "https://btc-e.com/api/2/ltc_eur/depth");
    		// fiat
        
        allMarkets[7] = new MarketNameHelper("usd", "rur", "https://btc-e.com/api/2/usd_rur/depth");
        allMarkets[8] = new MarketNameHelper("eur", "usd", "https://btc-e.com/api/2/eur_usd/depth");
        
        

        	// nvc
        allMarkets[9] = new MarketNameHelper("nvc", "btc", "https://btc-e.com/api/2/nvc_btc/depth");
        allMarkets[10] = new MarketNameHelper("nvc", "usd", "https://btc-e.com/api/2/nvc_usd/depth");
        
        
    	// nmc
        allMarkets[11] = new MarketNameHelper("nmc", "btc", "https://btc-e.com/api/2/nmc_btc/depth");
        allMarkets[12] = new MarketNameHelper("nmc", "usd", "https://btc-e.com/api/2/nmc_usd/depth");
        	//ppc
        allMarkets[13] = new MarketNameHelper("ppc", "btc", "https://btc-e.com/api/2/ppc_btc/depth");
        allMarkets[14] = new MarketNameHelper("ppc", "usd", "https://btc-e.com/api/2/ppc_usd/depth");
		
        /*
        // rest vs btc - deadend not interesting 
        allMarkets[17] = new MarketNameHelper("trc", "btc", "https://btc-e.com/api/2/trc_btc/depth");
        allMarkets[18] = new MarketNameHelper("ftc", "btc", "https://btc-e.com/api/2/ftc_btc/depth");
        allMarkets[19] = new MarketNameHelper("xpm", "btc", "https://btc-e.com/api/2/xpm_btc/depth");
        	// forgotten one
        	 
        	*/ 

        
        
        // 1.1 count all distinct values of the market and put them into hashmaps
        valueMapping = new HashMap<Integer,String>();
        keyMapping = new HashMap<String,Integer>();
        connector = new MarketJsonConnector[allMarkets.length];

        numberOfNodes = 0;
        for (int i = 0; i < allMarkets.length; i++) {
        	// create connector object - make preparation
        	connector[i] = new MarketJsonConnector(allMarkets[i].ask,allMarkets[i].bid);
        	
        	// ask
        	if(!keyMapping.containsKey(allMarkets[i].ask)){
        		keyMapping.put(allMarkets[i].ask, numberOfNodes);
        		valueMapping.put(numberOfNodes, allMarkets[i].ask);
        		numberOfNodes ++;
        	}      
        	
        	// bid
        	if(!keyMapping.containsKey(allMarkets[i].bid)){
        		keyMapping.put(allMarkets[i].bid, numberOfNodes);
        		valueMapping.put(numberOfNodes, allMarkets[i].bid);
        		numberOfNodes ++;
        	}      
        }
        System.out.println("Number of nodes: " + numberOfNodes);
        // number of nodes , init the  data for market
       solverData = new Market[numberOfNodes][numberOfNodes];
        
        /***** 
         * END : start of the program - not changeable
         */
    }
    
    
    // Create a unixtime nonce for the new API.
  
     /**
      * Execute a authenticated query on btc-e.
      *
      * @param method The method to execute.
      * @param arguments The arguments to pass to the server.
      *
      * @return The returned data as JSON or null, if the request failed.
      *
      * @see http://pastebin.com/K25Nk2Sv
      */
     public static final JSONObject authenticatedHTTPRequest( String method, Map<String, String> arguments) {
         _nonce = 5 + (long)(Math.random() * ((100000000 - 5) + 1));

    	 HashMap<String, String> headerLines = new HashMap<String, String>();  // Create a new map for the header lines.
         Mac mac;
         SecretKeySpec key = null;
  
         if( arguments == null) {  // If the user provided no arguments, just create an empty argument array.
             arguments = new HashMap<String, String>();
         }
        
         arguments.put( "method", method);  // Add the method to the post data.
         arguments.put( "nonce",  ""+_nonce);  // Add the dummy nonce.
  
         String postData = "";
  
         for( Iterator argumentIterator = arguments.entrySet().iterator(); argumentIterator.hasNext(); ) {
             Map.Entry argument = (Map.Entry)argumentIterator.next();
            
             if( postData.length() > 0) {
                 postData += "&";
             }
             postData += argument.getKey() + "=" + argument.getValue();
         }
  
         // Create a new secret key
         try {
             key = new SecretKeySpec( _secret.getBytes( "UTF-8"), "HmacSHA512" );
         } catch( UnsupportedEncodingException uee) {
             System.err.println( "Unsupported encoding exception: " + uee.toString());
             return null;
         }
  
         // Create a new mac
         try {
             mac = Mac.getInstance( "HmacSHA512" );
         } catch( NoSuchAlgorithmException nsae) {
             System.err.println( "No such algorithm exception: " + nsae.toString());
             return null;
         }
  
         // Init mac with key.
         try {
             mac.init( key);
         } catch( InvalidKeyException ike) {
             System.err.println( "Invalid key exception: " + ike.toString());
             return null;
         }
  
         // Add the key to the header lines.
         headerLines.put( "Key", _key);
  
         // Encode the post data by the secret and encode the result as base64.
         try {
             headerLines.put( "Sign", Hex.encodeHexString( mac.doFinal( postData.getBytes( "UTF-8"))));
         } catch( UnsupportedEncodingException uee) {
             System.err.println( "Unsupported encoding exception: " + uee.toString());
             return null;
         }
        
         // Now do the actual request
         String requestResult = HttpUtils.httpPost( "https://btc-e.com/tapi", headerLines, postData);
  
         if( requestResult != null) {   // The request worked
  
             try {
                 // Convert the HTTP request return value to JSON to parse further.
                 JSONObject jsonResult = JSONObject.fromObject( requestResult);
  
                 // Check, if the request was successful
                 int success = jsonResult.getInt( "success");
  
                 if( success == 0) {  // The request failed.
                     String errorMessage = jsonResult.getString( "error");
                    
                     System.err.println( "btc-e.com trade API request failed: " + errorMessage);
  
                     return null;
                 } else {  // Request succeeded!
                     return jsonResult.getJSONObject( "return");
                 }
  
             } catch( JSONException je) {
                 System.err.println( "Cannot parse json request result: " + je.toString());
  
                 return null;  // An error occured...
             }
         }
  
         return null;  // The request failed.
     }
     
     
     public static long generateNonce() { 
    	  try {
    	   // Create a secure random number generator
    	   SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");

    	   // Get 1024 random bits
    	   byte[] bytes = new byte[1024/8];
    	   sr.nextBytes(bytes);

    	   // Create two secure number generators with the same seed
    	   int seedByteCount = 10;
    	   byte[] seed = sr.generateSeed(seedByteCount);

    	   sr = SecureRandom.getInstance("SHA1PRNG");
    	   sr.setSeed(seed);
    	   
    	   long ra = sr.nextLong();
    	   if(ra<0) ra=ra*(-1);
    	  
     	  return ra;

    	   } catch (NoSuchAlgorithmException e) {
    	  }
    	  return 0;
    	 }
}
