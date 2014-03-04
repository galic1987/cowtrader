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
	private static String _key;
	private static String _secret;
	private static String _urlApi = "https://btc-e.com/tapi";
private static     ObjectMapper objectMapper = null;
private static ExecutorService executor;
private static DecimalFormat df = new DecimalFormat();


	public BTCEFactory (){
		// 0. Market connection data
		_key = "EWEZMRO1-GLWSOJ4E-XRHPKABY-F2TO4FU4-JZ2DCE3R";
		_secret = "64cb9bef1ece72d589b4a08b8b293f1b4622b0e7d4bac2cff616731a8d7386b1";
	    objectMapper = new ObjectMapper();
	    df.setMaximumFractionDigits(10);
		df.setGroupingUsed(false);
	} 

	@Override
	public String executeCycleOrders(CycleSolutionConfiguration cc) {
		// TODO Auto-generated method stub

		// build order and return
		 executor = Executors.newFixedThreadPool(cc.getOrdersPossibleForResources().size());

		for(int i =0;i<cc.getOrdersPossibleForResources().size();i++){
			final MarketOrderToSendCollection sendTemp = cc.getOrdersPossibleForResources().get(i);
			
			executor.execute(new Runnable() {
				public void run() {
					//sendTemp.minMaxRate;
					
					try {
						MarketOrderToSendCollection marketOrder =  sendTemp;
						
						Map<String, String> arguments = new HashMap<String, String>();
						arguments.put("Trade", "");
						arguments.put("pair", marketOrder.pair);
						arguments.put("type", marketOrder.type);
						arguments.put("rate",df.format(marketOrder.minMaxRate));
						arguments.put("amount", df.format(marketOrder.amount));

//						pair 	Yes 	pair 	btc_usd (example) 	-
//						type 	Yes 	The transaction type 	buy or sell 	-
//						rate 	Yes 	The rate to buy/sell 	numerical 	-
//						amount
						
						//String info = apacheHttpPost(_urlApi,arguments, "Trade");
						//System.out.println(info);
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
			Map<String, String> arguments = new HashMap<String, String>();
			arguments.put("getInfo", "");
			String info = apacheHttpPost(_urlApi,arguments, "getInfo");
			
		} else {
			// timeout occured 
 
		}
		} catch (Exception e) {

		}
		
		
		

		return null;
	}
	
	
	public Map<Integer, Double> minimumAmountForTransaction(Map<String, Integer> keyMapping){
		Map<Integer, Double> minResources = new HashMap<Integer,Double>();
		
		/*
		 * min resources changable
		 */
		Map<String, Double> minResourcesKeyDef = new HashMap<String,Double>();
		minResourcesKeyDef.put("ltc", 0.1);
		minResourcesKeyDef.put("btc", 0.01);
		
		
		
		

		
		
		return minResources;

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
		
		//System.out.println(authenticatedHTTPRequest("getInfo", arguments));
		try {
			String info = apacheHttpPost(_urlApi,arguments, "getInfo");
			//System.out.println(info);
			// get the parsing success or not
			resources = parseResources(info,keyMapping);
			//System.out.println(resources);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// myactive orders _urlApi
		// System.out.println(authenticatedHTTPRequest("ActiveOrders",
		// arguments));




		return resources;
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
	
	
	public String apacheHttpPost(String url,Map<String, String> arguments, String method) throws Exception{
		

		
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
