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
//		_key = "EWEZMRO1-GLWSOJ4E-XRHPKABY-F2TO4FU4-JZ2DCE3R"
//		_secret = "64cb9bef1ece72d589b4a08b8b293f1b4622b0e7d4bac2cff616731a8d7386b1";
	    objectMapper = new ObjectMapper();
	    df.setMaximumFractionDigits(7);
		df.setGroupingUsed(false);
		
		marketTraders.add(new MarketTraderKey("WXV11V8X-6AKJQ6FU-6FWOTPBZ-SWF6ZB04-FG1JUVVV", 
				"74f80949085afaa1947f786eada35d94f1f493ee4ee3d1bcb1779162b7f17de3", 
				"1",1301893455l));
		marketTraders.add(new MarketTraderKey("JGFW22YB-2V95KSC7-R5KITLE1-Q41VVYHQ-HYCKLPZ5", 
				"b6cf8e526244a9b0c9821312e86d69b349b32702c220c240bc81a8375ad97669", 
				"2",1301893451l));
		marketTraders.add(new MarketTraderKey("WYYZM7XM-LIVQGZU1-QVIDSD7Q-PE8VFGF7-UK2LF1YS", 
				"ca12bbcca569edcc70b17ed251ccb0e3505824da30bcc3cccc4dc82f6a39e34a", 
				"3",1301893451l));
		marketTraders.add(new MarketTraderKey("DRL4HPGC-Q5TFU0LQ-FP11B36N-BQ2K0JDG-8C84JSVR", 
				"07aa675e80b6b073de643d72f55338fb5fd3e6ae6c963409f1c7c976dd484446", 
				"4",1301893451l));
		marketTraders.add(new MarketTraderKey("NFOYKQKI-0RSSIC8L-4GOCY4CE-UT0DO9WC-DYZX6WF2", 
				"1dbfab94c23f40abe475cb9629fed20efa3a3913fc8b4694a32a057c7fb5e4b4", 
				"5",1301893451l));
		marketTraders.add(new MarketTraderKey("8E8XVNYT-PBRO40RA-L92L235Z-9IU9PHDQ-WS5VQO2F", 
				"d36062e0ee6f237ae87b35762e82f8264e3af5910a471beccb3b6f226354bf1f", 
				"6",1301893451l));
		marketTraders.add(new MarketTraderKey("HW0TPXA2-SFJGN6G5-DZ014AYE-HUSO2IXS-M9JG7KZM", 
				"84af505a230d22418b40469db469b88b82c0462ca41470ede1b3b76f214b347c", 
				"7",1301893451l));
		marketTraders.add(new MarketTraderKey("VR8OFKGX-XPOAJ2HM-KL6OAXC9-1BOTZA8C-X30Q917B", 
				"a6c3bfe231ed6fb1892f1868138b5a25ddf983d3e182451f3bc897d0e35eecce", 
				"8",1301893451l));
		
	} 
	
	

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
						double tolerance = 0.000; // 0.5% Tolerance
						
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
						double amount =0;
						
						if(marketOrder.type.equals("sell")){
							amount= marketOrder.amount;
						}else{
							amount= marketOrder.totalValueGot;
						}
						
						if(marketOrder.resourcesAvailable<0.0001){
							amount = amount - (amount * marketOrder.orders.get(0).getMarket().getTransactionFee() * 1.0001);
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
	
	
 public String trade(String pair, String type, String rate, String amount, MarketTraderKey mkey) throws Exception{
		Map<String, String> arguments = new HashMap<String, String>();
		//arguments.put("Trade", "");
		arguments.put("pair", pair);
		arguments.put("type", type);
		arguments.put("rate", rate);
		arguments.put("amount",amount);

		String info = "Trade offlineC";
				//apacheHttpPost(_urlApi,arguments, "Trade",mkey.getKey(),mkey.getSecret());
		//System.out.println(info);
		System.out.println(arguments);
		return info; // info return 
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
			  info = apacheHttpPost(_urlApi,arguments, "getInfo",marketTraders.get(0).getKey(),marketTraders.get(0).getSecret(),marketTraders.get(0).getCurrent_nonce());
			System.out.println(info);
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
			String info = apacheHttpPost(_urlApi,arguments, "getInfo",marketTraders.get(0).getKey(),marketTraders.get(0).getSecret(),marketTraders.get(0).getCurrent_nonce());
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
	
	
	public String apacheHttpPost(String url,Map<String, String> arguments, String method,String _key, String _secret, long nonce) throws Exception{
		

		final long timeFreeze = 1301893451L;
		_nonce =  nonce;
		
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
