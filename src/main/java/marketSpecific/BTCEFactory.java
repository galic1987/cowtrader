package marketSpecific;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import marketHole.CycleSolutionConfiguration;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Hex;

import otradotra.models.Market;
import otradotra.models.MarketOrderDataHolder;
import otradotra.models.MarketOrderToSend;
import otradotra.models.MarketOrderToSendCollection;
import otradotra.models.MarketTraderKey;
import otradotra.network.HttpUtils;



import otradotra.network.NetworkOptimizatorSingleton;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;



public class BTCEFactory extends AbstractMarketFactory {

	// Market connection data
	private static long _nonce;
//	private static String _key;
//	private static String _secret;
	private static String _urlApi = "https://btc-e.com/tapi";
private static     ObjectMapper objectMapper = null;
private static ExecutorService executor;
private static DecimalFormat df = new DecimalFormat();
private static ArrayList<MarketTraderKey> marketTraders = new ArrayList<MarketTraderKey>();

	public BTCEFactory (){
		// 0. Market connection data
//		_key = "EWEZMRO1-GLWSOJ4E-XRHPKABY-F2TO4FU4-JZ2DCE3R";
//		_secret = "64cb9bef1ece72d589b4a08b8b293f1b4622b0e7d4bac2cff616731a8d7386b1";
	    objectMapper = new ObjectMapper();
	    df.setMaximumFractionDigits(7);
		df.setGroupingUsed(false);
		
		marketTraders.add(new MarketTraderKey("EWEZMRO1-GLWSOJ4E-XRHPKABY-F2TO4FU4-JZ2DCE3R", "64cb9bef1ece72d589b4a08b8b293f1b4622b0e7d4bac2cff616731a8d7386b1", "traderTest"));
		marketTraders.add(new MarketTraderKey("LK52LJCU-SIOOYQQ5-HMYUZHW3-6BKQW52W-RG8RW9JM", "a515fcfa988b0f019287c35c94ddcd22f334f806037d0783a2d980f8ec4b6972", "jedan"));
		marketTraders.add(new MarketTraderKey("ZBFZPZJS-IP25TLPI-N2QES1VO-YCOJPFRP-JZTKD1MV", "5cfa491624000853f6f4aea85a9550b7eb15ce22b4f3d7456c04d9cea3989835", "dva"));
		marketTraders.add(new MarketTraderKey("G0W1WIJT-YNM81M4L-1KEJOLSP-WSWU7Q1T-5ZQ8U08H", "7d076d0c4812dee1d1f69fbb7727817e8ec30b1f0db650397c07c8df5a171785", "tri"));
		marketTraders.add(new MarketTraderKey("V29WSSS7-DXQZXASR-ZDAJ1BIJ-6OPI6B60-Y3GDPRVZ", "58137e63917e96d2125ac5a70751ee727081faca04de6ca519cc92dd6ce14aa6", "cetiri"));
		marketTraders.add(new MarketTraderKey("5N7ZNKMH-3KNT6BKX-XOB3WAG3-0B9G2V26-YUQ0M3GN", "9f2411b6da10cabf2edf18e809794158d57094fafbd2f3f9d327208f8a8728eb", "pet"));
		marketTraders.add(new MarketTraderKey("30DVV0BS-A9XGUUP1-FDV6WEOD-IL1R9KGC-DA526UTX", "21e70552183bfe95cf9561b59cdf774b5328563b3c7f5b6053b21cc13f29d8dc", "sest"));
		marketTraders.add(new MarketTraderKey("11IUPTL1-X778ISLH-FBBRLLGG-W1X4HHTC-P4VL2XA3", "880ad6ae2c9aec2a781dd3c7d7c4a62ff19cd2678c05e9e2a038fb62b3e59605", "sedam"));

	} 
	
	
	
	
	
//	public String nounceTest() {
//		// TODO Auto-generated method stub
//
//		// build order and return
//		 executor = Executors.newFixedThreadPool(7);
//
//		for(int i =0;i<7;i++){
//			final MarketTraderKey mkey = marketTraders.get(i); 
//			
//			// TODO: arrayoutofbounds exception
//
//			executor.execute(new Runnable() {
//				public void run() {
//					//sendTemp.minMaxRate;
//					
//					try {
//						Map<String, String> arguments = new HashMap<String, String>();
//						arguments.put("getInfo", "");
//						String info = apacheHttpPost(_urlApi,arguments, "getInfo",mkey.getKey(),mkey.getSecret());
//
//						System.out.println(mkey.getName()+" _ "+info);
//
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			});
//			
//		}
//		
//		executor.shutdown();
//		try {
//		if (executor.awaitTermination(9000, TimeUnit.MILLISECONDS)) {
//			// all threads good
////			Map<String, String> arguments = new HashMap<String, String>();
////			arguments.put("getInfo", "");
////			String info = apacheHttpPost(_urlApi,arguments, "getInfo");
////			
//		} else {
//			// timeout occured 
// 
//		}
//		} catch (Exception e) {
//
//		}
//		
//		
//		
//
//		return null;
//	}
	
	
	
	
	
	
	
	
	
	
	

	@Override
	public String executeCycleOrders(CycleSolutionConfiguration cc) {
		// TODO Auto-generated method stub

		// build order and return
		 executor = Executors.newFixedThreadPool(cc.getOrdersPossibleForResources().size());

		for(int i =0;i<cc.getOrdersPossibleForResources().size();i++){
			final MarketOrderToSendCollection sendTemp = cc.getOrdersPossibleForResources().get(i);
			final MarketTraderKey mkey = marketTraders.get(i); 
			
			// TODO: arrayoutofbounds exception

			executor.execute(new Runnable() {
				public void run() {
					//sendTemp.minMaxRate;
					
					try {
						MarketOrderToSendCollection marketOrder =  sendTemp;
						
						/*
						 * price tolerance to sell , not to get stuck
						 */
						double priceTolerance = 0;
						double tolerance = 0.005; // 0.5% Tolerance
						
						if(marketOrder.type.equals("sell")){
							// sell bring down the price 
							priceTolerance = marketOrder.minMaxRate - (tolerance*marketOrder.minMaxRate );
						}else{
							priceTolerance = marketOrder.minMaxRate + (tolerance*marketOrder.minMaxRate );

						}
						
						DecimalFormat priceformat = marketOrder.orders.get(0).getMarket().getPriceFormat();
						
						/*
						 * amount tolerance to sell , not to get rejected
						 */						
						double amount = marketOrder.amount;
						
						if(marketOrder.resourcesAvailable<0.0001){
							amount = amount - (marketOrder.amount * marketOrder.orders.get(0).getMarket().getTransactionFee() * 1.0001);
						}
						
						
						
						String info = trade(marketOrder.pair,marketOrder.type,priceformat.format(priceTolerance),df.format(amount),mkey);
						System.out.println(info);

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			
		}
		
		executor.shutdown();
		try {
		if (executor.awaitTermination(9000, TimeUnit.MILLISECONDS)) {
			// all threads good
//			Map<String, String> arguments = new HashMap<String, String>();
//			arguments.put("getInfo", "");
//			String info = apacheHttpPost(_urlApi,arguments, "getInfo");
//			
		} else {
			// timeout occured 
 
		}
		} catch (Exception e) {

		}
		
		
		

		return null;
	}
	
	

//	pair 	Yes 	pair 	btc_usd (example) 	-
//	type 	Yes 	The transaction type 	buy or sell 	-
//	rate 	Yes 	The rate to buy/sell 	numerical 	-
//	amount
	
	public String trade(String pair, String type, String rate, String amount, MarketTraderKey mkey) throws Exception{
		Map<String, String> arguments = new HashMap<String, String>();
		//arguments.put("Trade", "");
		arguments.put("pair", pair);
		arguments.put("type", type);
		arguments.put("rate", rate);
		arguments.put("amount",amount);

		String info = apacheHttpPost(_urlApi,arguments, "Trade",mkey.getKey(),mkey.getSecret());
		System.out.println(info);
		System.out.println(arguments);
		return info;
	}
	
	// returns true if ok 
	public boolean minimumAmountForTransaction(CycleSolutionConfiguration cc, Map<String, Integer> keyMapping){
		
		
		for(int i =0;i<cc.getOrdersPossibleForResources().size();i++){
			 MarketOrderToSendCollection sendTemp = cc.getOrdersPossibleForResources().get(i);
			if(sendTemp.currency.equals("btc")){
				if(sendTemp.amount<0.01) return false;
			}else{
				if(sendTemp.amount<0.1) return false;
			}
		}
		 
		return true;

	}



	// in url 
	// 
	@Override
	public Map<Integer, Double> updateResources(Map<String, Integer> keyMapping) {
		// TODO Auto-generated method stub



		Map<String, String> arguments = new HashMap<String, String>();
		arguments.put("getInfo", "");
		// low = -100;
		// my info
		Map<Integer, Double> resources = new HashMap<Integer,Double>();
		String info ="";
		//System.out.println(authenticatedHTTPRequest("getInfo", arguments));
		try {
			  info = apacheHttpPost(_urlApi,arguments, "getInfo",marketTraders.get(0).getKey(),marketTraders.get(0).getSecret());
			//System.out.println(info);
			// get the parsing success or not
			resources = parseResources(info,keyMapping);
			//System.out.println(resources);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//System.out.println(info);
			return null;
		}

		// myactive orders _urlApi
		// System.out.println(authenticatedHTTPRequest("ActiveOrders",
		// arguments));




		return resources;
	}
	
	// used to display change in trade 
	public String resourcesCompare(Map<Integer, Double> oldResources,Map<String, Integer> keyMapping,Map<Integer,String > valueMapping) {
		// TODO Auto-generated method stub

		String report = "Report: ";

		Map<String, String> arguments = new HashMap<String, String>();
		arguments.put("getInfo", "");
		// low = -100;
		// my info
		Map<Integer, Double> resources = new HashMap<Integer,Double>();
		
		//System.out.println(authenticatedHTTPRequest("getInfo", arguments));
		try {
			String info = apacheHttpPost(_urlApi,arguments, "getInfo",marketTraders.get(0).getKey(),marketTraders.get(0).getSecret());
			//System.out.println(info);
			// get the parsing success or not
			resources = parseResources(info,keyMapping);
			//System.out.println(resources);
			
			Iterator it = oldResources.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pairs = (Map.Entry)it.next();
		        System.out.println(pairs.getKey() + " = " + pairs.getValue());
		        double balance = resources.get(pairs.getKey()) - (Double) pairs.getValue();
		        String currency = valueMapping.get(pairs.getKey());
		        report += currency+": "+df.format(balance) + " ";

		    }			

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// myactive orders _urlApi
		// System.out.println(authenticatedHTTPRequest("ActiveOrders",
		// arguments));




		return report;
	}



	// mapping of array current 
	// value mapping 1->valuta
	// data from website 
	public Map<Integer, Double> parseResources(String data,Map<String, Integer> keyMapping) throws JsonProcessingException, IOException{
		//mark.orders = new MarketOrder[data.size()];
 
		Map<Integer, Double> resources = new HashMap<Integer,Double>();
		JsonNode rootNode = objectMapper.readTree(data);

		  
      	// ASK 
      	JsonNode successNode = rootNode.path("success");

		
	    int success = successNode.asInt();
	    
	    if(success > 0){
	    	// this is ok 
	      	JsonNode returnNode = rootNode.path("return");

	    	//System.out.println(success);
	      	JsonNode funds = returnNode.path("funds");
			 Iterator<JsonNode> elements = funds.getElements();
			 Iterator<String> elementsNames = funds.getFieldNames();
			 while(elements.hasNext()){
			 JsonNode value = elements.next();
			String name = elementsNames.next();
			
 			// System.out.println(name+" - " + value.getDoubleValue());
			 if(keyMapping.get(name)!=null){
				 // this resource is included in running 

				 resources.put(keyMapping.get(name) , value.getDoubleValue());
				 
			 }
			
			 }
	    	
	    	
	    }else{
	    	// fail
	    	
	      	JsonNode returnNode = rootNode.path("error");

	    	System.out.println("Failinformation: " +returnNode.asText());
	    }
	    
 
	    
	    return resources;
	}
	
	
	public String apacheHttpPost(String url,Map<String, String> arguments, String method,String _key, String _secret) throws Exception{
		

		
		_nonce = System.currentTimeMillis() % 1000000000;
		
		BufferedReader reader;
		String currentLine;
		StringBuilder result = new StringBuilder();

		InputStream instream = null;
	 
		HttpPost httpPost = new HttpPost(url);
		
		 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();


		HashMap<String, String> headerLines = new HashMap<String, String>();  
		Mac mac;
		SecretKeySpec key = null;

		if (arguments == null) { // If the user provided no arguments, just
			// create an empty argument array.
			arguments = new HashMap<String, String>();
		}

		arguments.put("method", method); // Add the method to the post data.
		arguments.put("nonce", "" + ++ _nonce); // Add the dummy nonce.

		String postData = "";

		for (@SuppressWarnings("rawtypes")
		Iterator argumentIterator = arguments.entrySet().iterator(); argumentIterator
				.hasNext();) {
			@SuppressWarnings("rawtypes")
			Map.Entry argument = (Map.Entry) argumentIterator.next();

			if (postData.length() > 0) {
				postData += "&";
			}
			postData += argument.getKey() + "=" + argument.getValue();
			 nameValuePairs.add(new BasicNameValuePair((String)argument.getKey(),
					 (String)argument.getValue()));
		}

		// Create a new secret key
		try {
			key = new SecretKeySpec(_secret.getBytes("UTF-8"), "HmacSHA512");
		} catch (UnsupportedEncodingException uee) {
			System.err.println("Unsupported encoding exception: "
					+ uee.toString());
			return null;
		}

		// Create a new mac
		try {
			mac = Mac.getInstance("HmacSHA512");
		} catch (NoSuchAlgorithmException nsae) {
			System.err.println("No such algorithm exception: "
					+ nsae.toString());
			return null;
		}

		// Init mac with key.
		try {
			mac.init(key);
		} catch (InvalidKeyException ike) {
			System.err.println("Invalid key exception: " + ike.toString());
			return null;
		}

		// Add the key to the header lines.
		headerLines.put("Key", _key);

		httpPost.addHeader("Key", _key);
		
		// Encode the post data by the secret and encode the result as base64.
		try {
			headerLines.put("Sign", Hex.encodeHexString(mac.doFinal(postData
					.getBytes("UTF-8"))));
			
			httpPost.addHeader("Sign", Hex.encodeHexString(mac.doFinal(postData
					.getBytes("UTF-8"))));


		} catch (UnsupportedEncodingException uee) {
			System.err.println("Unsupported encoding exception: "
					+ uee.toString());
			return null;
		}

		// Now do the actual request
//		String requestResult = HttpUtils.httpPost("https://btc-e.com/tapi",
//				headerLines, postData);

	     
	 
		
		
		
		httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		CloseableHttpClient httpClient = NetworkOptimizatorSingleton.createConnetor();
		CloseableHttpResponse response = httpClient.execute(httpPost);
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

	
	


}
