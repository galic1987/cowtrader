package test_choco;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import marketHole.MarketHoleCalculator;
import marketSpecific.AbstractMarketFactory;
import marketSpecific.BTCEFactory;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Hex;

import otradotra.helper.DataMarketIntegrityCheck;
import otradotra.helper.ExplanationSingleton;
import otradotra.helper.ReporterSingleton;
import otradotra.models.Market;
import otradotra.models.MarketNameHelper;
import otradotra.models.MarketType;
import otradotra.network.HttpUtils;
import otradotra.network.JSONParsingOptimizationSingleton;
import otradotra.network.MarketJsonConnector;
import otradotra.network.NetworkOptimizatorSingleton;

/**
 * Hello world!
 * 
 */

public class BTC_e {



	// setup optimization costs
	private static MarketNameHelper[] allMarkets;
	private static Map<Integer, String> valueMapping;
	private static Map<String, Integer> keyMapping;
	private static MarketJsonConnector[] connector; // changes time start end
	private static int numberOfNodes;
	private static final double low = -10; // lowest difference
	private static AbstractMarketFactory factory = null;

	// changable
	private static Market[][] solverData;
	private static Map<Integer, Double> resources = null; // can be asked what
															// via value mapping

	// extra
	private static MarketProblem problem;
	// change says if cache-ing is well performed for this market data (cycles
	// of solver)) Market solver

	
	// multithreading options
	private static boolean cache;
	private static AtomicBoolean dataTransferError;

	private static Phaser phaserBlockMainThread;
	private static AtomicBoolean blockWorkerThreadsAtLoopEnd;
	private static ExecutorService executor;
	private static boolean multiThreadingSetup;
	private static AtomicBoolean mainThreadWait;
	private static CountDownLatch mainToOtherThreads;
	
	
	// data time integrity array
	private static long [] startData;
	private static long [] endData;
	
	
	
	
	
	public static void main(String[] args) throws InterruptedException {
		
		
		/*
		 * Put all market specific code 
		 * onto one place so migration
		 * can be done easaly
		 */
		factory = new BTCEFactory();
		
		
//		BTCEFactory test = (BTCEFactory) factory;
//		
//		boolean go = true;
//		try {
//			test.nounceTest();
//			//test.trade("ltc_usd", "sell", "15.284719", "0.108219");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		while(go){
//			
//		}
		
		
		// init setup
		setup();
		
		 // network data integrity timestamp measuerment
		 startData = new long[connector.length];
		 endData = new long[connector.length];
		 
		 
		
		for(int i =0; i<allMarkets.length;i++){
			//System.out.println(allMarkets[i].url);
			//HttpUtils.httpGet(allMarkets[i].url);
		}
		NetworkOptimizatorSingleton.getInstance();
		JSONParsingOptimizationSingleton.getInstance();
		
		
	 
		
		// tuning java
				Properties props = System.getProperties();
				//props.setProperty("http.keepAlive", "true");
				//props.setProperty("http.maxConnections", allMarkets.length+"" );
				props.setProperty("http.maxConnections", "256" );
				
		// multithreading
		 phaserBlockMainThread = new Phaser();
		 blockWorkerThreadsAtLoopEnd = new AtomicBoolean();
		 executor = Executors.newFixedThreadPool(allMarkets.length);

		 dataTransferError = new AtomicBoolean();
		 mainThreadWait = new AtomicBoolean();
		mainToOtherThreads = new CountDownLatch(allMarkets.length + 1);
		 
		 // do setup run 
		 multiThreadingSetup = true;
		 getDataFromInternet();
		 multiThreadingSetup = false;

		

		 
		// set reporter mappings

		double profit = 0;

		ReporterSingleton.getInstance();
		ReporterSingleton.keyMapping = keyMapping;
		ReporterSingleton.valueMapping = valueMapping;
		ReporterSingleton.balancingCurrency = "usd";

		// for(int i = 0; i<100; i++){
		while (true) {
			ReporterSingleton.roundHigh = -10; // reset the round settings
			ReporterSingleton.roundAround = null;
			ReporterSingleton.roundCurrency = "usd";
			
			// lets execute multithreading (get data from internet)

		    Map<Integer,Double>res = factory.updateResources(keyMapping);
		    
		    if(res!=null){
		    	resources=res;
		    }
		    
			//resources = getResources(numberOfNodes, valueMapping);
			ReporterSingleton.resources = resources; // / this needs to be
														// removed

			// get data from internet
			getDataFromInternet();

			if (!cache) {
				// if data inconsistent from last loop then is cache fail
				// create problem again
				problem = new MarketProblem(solverData, valueMapping,
						keyMapping, resources, numberOfNodes);
				problem.start();
			}
			cache = true;
			// otherwise just proceed with activites
			tryCyclesEvaluation();
			
			// here come heuristics 
			ReporterSingleton.printInvolvedCount();
			ReporterSingleton.resetInvolvedCounter();
			
			

			System.out.println("Total # of solutions "
					+ ReporterSingleton.numberOfSoultions);
			System.out.println("Highest round "
					+ ReporterSingleton.roundCurrency + " "
					+ new BigDecimal(ReporterSingleton.roundHigh) + " ");
			System.out
					.println("Highest balancing currency  "
							+ ReporterSingleton.balancingCurrency
							+ " "
							+ new BigDecimal(
									ReporterSingleton.roundhighestValueBalancingCurrency)
							+ " ");
			System.out.println("Cycle " + ReporterSingleton.roundAround);
			// we have solution print it out
			if (ReporterSingleton.roundhighestValueBalancingCurrency > 0) {
				profit += ReporterSingleton.roundhighestValueBalancingCurrency;
			}
			System.out.println("Total Profit "
					+ ReporterSingleton.balancingCurrency + " " + profit);

			Iterator it = ReporterSingleton.roundAround.entrySet().iterator();
			while (it.hasNext()) {

				Map.Entry pairs = (Map.Entry) it.next();
				int node = (Integer) pairs.getKey();
				int arc = (Integer) pairs.getValue();

				System.out.print(valueMapping.get(node) + "-->"
						+ valueMapping.get(arc));
			}
			System.out.println("");

			if (ReporterSingleton.roundhighestValueBalancingCurrency > 0) {

			/*	CalculateOptimalVolumeProblem problem = new CalculateOptimalVolumeProblem(
						solverData, ReporterSingleton.roundHigh,
						ReporterSingleton.roundCurrency,
						ReporterSingleton.roundAround,
						ReporterSingleton.roundAround.size(), valueMapping,
						keyMapping, resources);
				problem.start();*/
			}
			
			//System.out.println(
			//		NetworkOptimizatorSingleton.getPool().getTotalStats());

			// Thread.sleep(100);
			// printNice();

		}

	}

	public static void tryCyclesEvaluation() {
		boolean explain = false;

		double[] calc = new double[solverData.length];
		double[] kol = new double[solverData.length];
		double volumina = 1; // try volumina to spin

		for (int i = 0; i < calc.length; i++) {
			calc[i] = 0;
			kol[i] = 0;
		}
		
		// count number of most involved nodes
			//Map<Integer, Integer> numberOfOccurences [] = new (Map<Integer, Integer> nodeMapping)[10];
		
		// go throught cycle
		for (int cycleNumber = 0; cycleNumber < problem.getNumberOfSolutions(); cycleNumber++) {
			StringBuffer explanator = null;
			
			Map<Integer, Integer> nodeMapping = problem.getSolutions()[cycleNumber];
			Iterator it = nodeMapping.entrySet().iterator();
			while (it.hasNext()) {
				if(explain){
				explanator = new StringBuffer();
				explanator.append("############ ");
				}
				
				Map.Entry pairs = (Map.Entry) it.next();
				// go throught full cycle beginning with actual node
				int actualNode = (Integer) pairs.getKey();
				volumina = resources.get(actualNode);
				for (int i = 0; i < calc.length; i++) {
					calc[i] = 0;
					kol[i] = 0;
				}
				do {
					int i = actualNode; // actual node
					int j = nodeMapping.get(actualNode); // next node
					// .. do here the calculation
					// System.out.println(i+"->"+j);
					Market m = solverData[i][j];
					//  calculation
					if (calc[i] == 0) {
						// System.out.println(m.type);
						
						// head explanation
						if(explain){
							explanator.append("("+m.getType()+")First buy with volumen "+volumina+"  ("+valueMapping.get(i)+")->("+valueMapping.get(j)+")\n");
						}
						double tempBuy = 0;
						if (m.getType() == MarketType.BID) {
							tempBuy = (m.getOrders()[0].price * volumina)
									- (m.getOrders()[0].price * volumina * m
											.getTransactionFee());
							if(explain){
								// formula explanation
								explanator.append("calculation("+valueMapping.get(j)+") = (price("+valueMapping.get(j)+")*volumen("+valueMapping.get(i)+")) -"
										+ "((price("+valueMapping.get(j)+") *volumen("+valueMapping.get(i)+") * fee("+m.getMarketName()+"))\n");
								// concrete numbers
								explanator.append("calculation("+valueMapping.get(j)+") = ("+m.getOrders()[0].price+"("+valueMapping.get(j)+") * "+volumina+"("+valueMapping.get(i)+")) -"
										+ "(("+m.getOrders()[0].price+"("+valueMapping.get(j)+") *"+volumina+"("+valueMapping.get(i)+")) * "+m.getTransactionFee()+"("+m.getMarketName()+"))\n");
								// concrete number at the end
								explanator.append("Bought volume("+valueMapping.get(j)+") = "+tempBuy);
								explanator.append("\n");
								explanator.append(ExplanationSingleton.lastNOrdersFromMarket(5, m));

							}
						} else {
							tempBuy = (volumina / m.getOrders()[0].price)
									- ((volumina / m.getOrders()[0].price) * m
											.getTransactionFee());
							
							if(explain){
								// formula explanation
							explanator.append("calculation("+valueMapping.get(j)+") = (volumen("+valueMapping.get(i)+") / price("+valueMapping.get(j)+")) -"
									+ "((volumen("+valueMapping.get(i)+") / price("+valueMapping.get(j)+")) * fee("+m.getMarketName()+"))\n");
							// concrete numbers
							explanator.append("calculation("+valueMapping.get(j)+") = ("+volumina+"("+valueMapping.get(i)+") / "+m.getOrders()[0].price+"("+valueMapping.get(j)+")) -"
									+ "(("+volumina+"("+valueMapping.get(i)+") / "+m.getOrders()[0].price+"("+valueMapping.get(j)+")) * "+m.getTransactionFee()+"("+m.getMarketName()+"))\n");
							// concrete number at the end
							explanator.append("Bought volume("+valueMapping.get(j)+") = "+tempBuy+"\n");
							explanator.append(ExplanationSingleton.lastNOrdersFromMarket(5, m));

							}
							
						}

						calc[j] += tempBuy;
						// System.out.println("BUY:"
						// +valueMapping.get(j)+" With "+volumina
						// +" "+valueMapping.get(i)+" = " + tempBuy);

						calc[i] -= volumina;

					} else {
						double tempBuy = 0;
						
						if(explain)explanator.append("("+m.getType()+") buy with volumen "+calc[i]+"  ("+valueMapping.get(i)+")->("+valueMapping.get(j)+")\n");

						if (m.getType() == MarketType.BID) {
							tempBuy = (m.getOrders()[0].price * calc[i])
									- (m.getOrders()[0].price * calc[i] * m
											.getTransactionFee());
							if(explain){
								// formula explanation
							explanator.append("calculation("+valueMapping.get(j)+") = (price("+valueMapping.get(j)+")*volumen("+valueMapping.get(i)+")) -"
									+ "((price("+valueMapping.get(j)+") *volumen("+valueMapping.get(i)+") * fee("+m.getMarketName()+"))\n");
							// concrete numbers
							explanator.append("calculation("+valueMapping.get(j)+") = ("+m.getOrders()[0].price+"("+valueMapping.get(j)+") * "+calc[i]+"("+valueMapping.get(i)+")) -"
									+ "(("+m.getOrders()[0].price+"("+valueMapping.get(j)+") *"+calc[i]+"("+valueMapping.get(i)+")) * "+m.getTransactionFee()+"("+m.getMarketName()+"))\n");
							// concrete number at the end
							explanator.append("Bought volume("+valueMapping.get(j)+") = "+tempBuy);
							explanator.append("\n");
							explanator.append(ExplanationSingleton.lastNOrdersFromMarket(5, m));

							}
						} else {
							tempBuy = (calc[i] / m.getOrders()[0].price)
									- ((calc[i] / m.getOrders()[0].price) * m
											.getTransactionFee());
							
							if(explain){
								// formula explanation
							explanator.append("calculation("+valueMapping.get(j)+") = (volumen("+valueMapping.get(i)+") / price("+valueMapping.get(j)+")) -"
									+ "((volumen("+valueMapping.get(i)+") / price("+valueMapping.get(j)+")) * fee("+m.getMarketName()+"))\n");
							// concrete numbers
							explanator.append("calculation("+valueMapping.get(j)+") = ("+calc[i]+"("+valueMapping.get(i)+") / "+m.getOrders()[0].price+"("+valueMapping.get(j)+")) -"
									+ "(("+calc[i]+"("+valueMapping.get(i)+") / "+m.getOrders()[0].price+"("+valueMapping.get(j)+")) * "+m.getTransactionFee()+"("+m.getMarketName()+"))\n");
							// concrete number at the end
							explanator.append("Bought volume("+valueMapping.get(j)+") = "+tempBuy+"\n");
							explanator.append(ExplanationSingleton.lastNOrdersFromMarket(5, m));

							}

						}

						calc[j] += tempBuy;
						// System.out.println("BUY:"
						// +valueMapping.get(j)+" With "+calc[i]
						// +" "+valueMapping.get(i)+" = " + tempBuy);

						calc[i] = 0;
						calc[i] -= calc[i];

					}

					// System.out.println(i+"->"+j);
					// kol[j] = m.orders[0].price;

					// .. end calculation
					actualNode = nodeMapping.get(actualNode);// return next
																// Value
				} while (actualNode != (Integer) pairs.getKey()); // if equal to
																	// begin
																	// node then
																	// destroy
																	// the cycle
				// it.remove();
				boolean t = false;
				// evaluate the solution
				for (int testCount = 0; testCount < calc.length; testCount++) {
					if (calc[testCount] > 0) {
						t = true;

						// System.out.println("Found soulution "+calc[i]);
						if(explain){
						explanator.append("############");
						System.out.println(explanator.toString());
						}

						break;
					} else {
						// System.out.println("Found soulution "+calc[i]);
						// for(int gg=0;gg<calc.length;gg++){
						if (calc[testCount] != 0) {
							// System.out.print(valueMapping.get(i)+":"+new
							// BigDecimal(calc[i]).toString()+" ");
							// if(calc[gg]>lowest_diff) lowest_diff = calc[gg];
							// }
							// System.out.println("");
						}

					}
				}

				// if solution
				if (t) {					
					for (int i1 = 0; i1 < calc.length; i1++) {
						// report every node higher than 0
						if (calc[i1] > 0){
							// report solution heuristic
							CycleVolumeCalculator c = new CycleVolumeCalculator(solverData, 14, nodeMapping, valueMapping, resources,keyMapping);
							c.start();
							
							// here get max earnings from this cycle 
							// TODO: c.getMax -> report to Reporter as value']
							
							if(c.getSolutionConfiguration() == null) break;
							
							ReporterSingleton.addInvolvedCount(c.getSolutionConfiguration().getValue(),
									valueMapping.get(i1), solverData,
									nodeMapping); 
							
							String g = valueMapping.get(i1);
							String m = c.getSolutionConfiguration().getCurrency();
							
							System.out.println(ExplanationSingleton.explainCycleSolutionConfiguration(c.getSolutionConfiguration()));
							
							if(factory.minimumAmountForTransaction(c.getSolutionConfiguration(), keyMapping)){
							factory.executeCycleOrders(c.getSolutionConfiguration());
							ReporterSingleton.highVolumeRound(c.getSolutionConfiguration().getValue(), valueMapping.get(i1), c.getSolutionConfiguration(), solverData);
							// need to exit this and move to the next
							System.out.println(factory.resourcesCompare(resources, keyMapping, valueMapping));
							
							return;
							}else{
								System.out.println("volume too low - not trading");
							}
							break;
						}
						// System.out.print(valueMapping.get(i)+":"+new
						// BigDecimal(calc[i]).toString());
						// System.out.println("");
						// if(calc[i]<lowest_diff) lowest_diff = calc[i];

					}
				}

			}

			// System.out.println("lowest "+lowest_diff);

			for (int gg = 0; gg < calc.length; gg++) {
				// System.out.print(valueMapping.get(gg)+":"+new
				// BigDecimal(calc[gg]).toString()+" ");
				if (calc[gg] != 0)
					ReporterSingleton.highRound(calc[gg], valueMapping.get(gg),
							nodeMapping, solverData);

			}

		}// end iteration of cycleNumber
	}

	public static void getDataFromInternet() throws InterruptedException {

		// 2. Get stream threadpool market executive - try to get consistent
		// data (important-> thread execution!!)
		// number of threads == number of markets
		
		// ExecutorService executor = Executors.newCachedThreadPool();

		phaserBlockMainThread = new Phaser();
		phaserBlockMainThread.register(); // register self (main thread)

		
			
		dataTransferError.set(false);
		
		blockWorkerThreadsAtLoopEnd.set(false); // unblock all threads at the end **** (awaitAdvance(1))
		mainToOtherThreads.countDown();
		
		mainThreadWait.set(true);
		
		long nanos = System.nanoTime();
		
		if(multiThreadingSetup){
		for (int i = 0; i < allMarkets.length; i++) {
			// put matrix addresses
			final int trans = i;

			executor.execute(new Runnable() {
				public void run() {
					
					
					if(multiThreadingSetup){
						Thread t = Thread.currentThread();
						t.setPriority(Thread.MAX_PRIORITY);
						Thread.yield();
					}

					while(true){
						
					
						
					// if not setup then do normal 
					phaserBlockMainThread.register(); // register task
					if(mainThreadWait.get())mainThreadWait.set(false);// send unblocking for main

					// System.out.println("Thread executing " + trans);
					// solverData.clone();
					// ask/bid
					// btc/usd

					Market[] marketTemp = null;
					try {
						startData[trans] = System.currentTimeMillis();
						marketTemp = connector[trans].parseBTCeOrders(
								allMarkets[trans].url, allMarkets[trans].bid);
						
						blockWorkerThreadsAtLoopEnd.set(true); // unblock all threads at the end **** (awaitAdvance(1))

						solverData[keyMapping.get(marketTemp[0].getTo())][keyMapping
								.get(marketTemp[0].getFrom())] = marketTemp[0];
						solverData[keyMapping.get(marketTemp[1].getTo())][keyMapping
								.get(marketTemp[1].getFrom())] = marketTemp[1];

						// cycle cache not correct if null values, then do
						// problem again
						if (cache) {
							if (solverData[keyMapping.get(marketTemp[0].getTo())][keyMapping
									.get(marketTemp[0].getFrom())] == null) {
								cache = false;
							}
							if (solverData[keyMapping.get(marketTemp[1].getTo())][keyMapping
									.get(marketTemp[1].getFrom())] == null) {
								cache = false;
							}
						}
						
						
					} catch (Exception e) {
						cache = false; // if one thread fails do
										// MarketProblemCycle again
						e.printStackTrace();
						dataTransferError.set(true);
					}
					
					endData[trans] = System.currentTimeMillis();

					marketTemp = null;

					phaserBlockMainThread.arrive(); // say main thread finished
					
					long milis = 10 + (System.currentTimeMillis() % 50);

					// System.out.println("--> Thread ending " + trans);
					while(blockWorkerThreadsAtLoopEnd.get()){
						try {
							
							//Thread.sleep(System.currentTimeMillis() % 1000);
							// put them wait in latch
							
							Thread.sleep(41);
						//	milis = (milis/2) +5;
							
						} catch (InterruptedException e) {
							// 
							e.printStackTrace();
						}
					}
					//phaserBlockWorkerThreads.register();
					
					
					mainToOtherThreads.countDown();
					try {
						mainToOtherThreads.await();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					}
					// first thread unset sync time
					//if(threadSynchonizationTime.get()>0)threadSynchonizationTime.set(0);
					
				
					}
				}
			});

			// Runnable worker = new WorkerThread('' + i);
			// executor.execute(worker);
		}
		}
		// classical shutdown 
		/*executor.shutdown();
		while (!executor.isTerminated()) {
			
		}
		*/
		


		while(mainThreadWait.get()){
			
			// try to sleep next 100ms or 50ms to sleep
			// synchronize sleeping of threads to get paralell execution
			//System.out.println(milis); 

			Thread.sleep(150);
			

		}; // wait for register
		mainToOtherThreads = new CountDownLatch(allMarkets.length + 1);



		//wait for motherfuckers
		phaserBlockMainThread.arriveAndAwaitAdvance(); 
		
		
		
		double startIntegrity = DataMarketIntegrityCheck.maxDistanceMs(startData);
		System.out.println("Integrity Start differece " + startIntegrity);
		
		double endIntegrity = DataMarketIntegrityCheck.maxDistanceMs(endData);
		System.out.println("Integrity End differece " + endIntegrity);
		//phaserBlockMainThread.(); // register self (main thread)

		
		
		/*try {
			if (executor.awaitTermination(900, TimeUnit.MILLISECONDS)) {
				// all threads good
				
				
			} else {
				// timeout occured 
			}
		} catch (Exception e) {

		}*/

		

		// try again  
		if (dataTransferError.get()) {
			//getDataFromInternet();
		}

		long duration = System.nanoTime() - nanos;
		int seconds = (int) (duration / 1000000000);
		int milliseconds = (int) (duration / 1000000) % 1000;
		int nanoseconds = (int) (duration % 1000000);
		System.out
				.printf("Download Time: %d seconds, %d milliseconds en %d nanoseconds\n",
						seconds, milliseconds, nanoseconds);

	}

	public static void printNice() {
		System.out.print("   ");

		for (int i = 0; i < solverData.length; i++) {
			System.out.print(" " + valueMapping.get(i) + " ");
		}

		for (int i = 0; i < solverData.length; i++) {
			System.out.println("");
			System.out.print(valueMapping.get(i) + "");

			for (int j = 0; j < solverData[i].length; j++) {
				Market m = solverData[i][j];

				// if(m != null)System.out.print(m.getFrom() + "->" + m.getTo()
				// + " ");
				if (m != null)
					System.out.print("  1  ");
				if (m == null)
					System.out.print("  x  ");

			}
		}
	}

	public static final Map<Integer, Double> getResources(
			int numberOfResources, Map<Integer, String> valueMap) {

		Map<Integer, Double> resources = new HashMap<Integer, Double>();
		// TODO: impelement the function
		// make example

		for (int i = 0; i < numberOfResources; i++) {

		}
		// /[nmc=6, eur=3, rur=2, usd=1, ltc=4, ppc=7, nvc=5, btc=0]
		resources.put(0, 0.1216);// btc
		resources.put(1, 100.0); //
		resources.put(2, 3511.10);
		resources.put(3, 133.20);
		resources.put(4, 7.44444);
		resources.put(5, 7.39);
		resources.put(6, 17.857);
		resources.put(7, 17.69);

		
		
//		resources.put(0, 0.0216);// btc
//		resources.put(1, 10.0); //
//		resources.put(2, 311.10);
//		resources.put(3, 13.20);
//		resources.put(4, 0.44444);
//		resources.put(5, 0.739);
//		resources.put(6, 1.857);
//		resources.put(7, 1.69);

		
		
		// make 5 of each // release -> set each to 0

		// release -> invoke market get data set iterate and fill if not

		return resources;
	}

	public static final void setup() {
		/*****
		 * START : start of the program - not changeable
		 */

		// truststore fix multithreading
		/*String fixble = HttpUtils
				.httpGet("https://btc-e.com/api/2/btc_usd/depth");*/
		// 1. List of markets / links / marketdata
		allMarkets = new MarketNameHelper[16];
		// btc
		allMarkets[0] = new MarketNameHelper("btc", "usd",
				"https://btc-e.com/api/2/btc_usd/depth", 0.002,3);
		allMarkets[1] = new MarketNameHelper("btc", "rur",
				"https://btc-e.com/api/2/btc_rur/depth", 0.002,5);
		allMarkets[2] = new MarketNameHelper("btc", "eur",
				"https://btc-e.com/api/2/btc_eur/depth", 0.002,5);

		// ltc
		allMarkets[3] = new MarketNameHelper("ltc", "btc",
				"https://btc-e.com/api/2/ltc_btc/depth", 0.002,5);
		allMarkets[4] = new MarketNameHelper("ltc", "usd",
				"https://btc-e.com/api/2/ltc_usd/depth", 0.002,6);
		allMarkets[5] = new MarketNameHelper("ltc", "rur",
				"https://btc-e.com/api/2/ltc_rur/depth", 0.002,5);
		allMarkets[6] = new MarketNameHelper("ltc", "eur",
				"https://btc-e.com/api/2/ltc_eur/depth", 0.002,3);
		// fiat

		allMarkets[7] = new MarketNameHelper("usd", "rur",
				"https://btc-e.com/api/2/usd_rur/depth", 0.005,5);
		allMarkets[8] = new MarketNameHelper("eur", "usd",
				"https://btc-e.com/api/2/eur_usd/depth", 0.002,5);

		// nvc
		allMarkets[9] = new MarketNameHelper("nvc", "btc",
				"https://btc-e.com/api/2/nvc_btc/depth", 0.002,5);
		allMarkets[10] = new MarketNameHelper("nvc", "usd",
				"https://btc-e.com/api/2/nvc_usd/depth", 0.002,3);

		// nmc
		allMarkets[11] = new MarketNameHelper("nmc", "btc",
				"https://btc-e.com/api/2/nmc_btc/depth", 0.002,5);
		allMarkets[12] = new MarketNameHelper("nmc", "usd",
				"https://btc-e.com/api/2/nmc_usd/depth", 0.002,3);
		// ppc
		allMarkets[13] = new MarketNameHelper("ppc", "btc",
				"https://btc-e.com/api/2/ppc_btc/depth", 0.002,5);
		allMarkets[14] = new MarketNameHelper("ppc", "usd",
				"https://btc-e.com/api/2/ppc_usd/depth", 0.002,3);
		
		allMarkets[15] = new MarketNameHelper("eur", "rur",
				"https://btc-e.com/api/2/eur_rur/depth", 0.005,5);

		/*
		 * // rest vs btc - deadend not interesting allMarkets[17] = new
		 * MarketNameHelper("trc", "btc",
		 * "https://btc-e.com/api/2/trc_btc/depth"); allMarkets[18] = new
		 * MarketNameHelper("ftc", "btc",
		 * "https://btc-e.com/api/2/ftc_btc/depth"); allMarkets[19] = new
		 * MarketNameHelper("xpm", "btc",
		 * "https://btc-e.com/api/2/xpm_btc/depth"); // forgotten one
		 */

		// 1.1 count all distinct values of the market and put them into
		// hashmaps
		valueMapping = new HashMap<Integer, String>();
		keyMapping = new HashMap<String, Integer>();
		connector = new MarketJsonConnector[allMarkets.length];

		numberOfNodes = 0;
		for (int i = 0; i < allMarkets.length; i++) {
			// create connector object - make preparation
			connector[i] = new MarketJsonConnector(allMarkets[i].ask,
					allMarkets[i].bid, allMarkets[i].transactionFee,allMarkets[i].maxDecimalPlaces);

			// ask
			if (!keyMapping.containsKey(allMarkets[i].ask)) {
				keyMapping.put(allMarkets[i].ask, numberOfNodes);
				valueMapping.put(numberOfNodes, allMarkets[i].ask);
				numberOfNodes++;
			}

			// bid
			if (!keyMapping.containsKey(allMarkets[i].bid)) {
				keyMapping.put(allMarkets[i].bid, numberOfNodes);
				valueMapping.put(numberOfNodes, allMarkets[i].bid);
				numberOfNodes++;
			}
		}
		System.out.println("Number of nodes: " + numberOfNodes);
		// number of nodes , init the data for market
		solverData = new Market[numberOfNodes][numberOfNodes];
		cache = false;
		/*****
		 * END : start of the program - not changeable
		 */
	}

	

}
