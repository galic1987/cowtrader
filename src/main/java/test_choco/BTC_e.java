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

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Hex;

import otradotra.Market;
import otradotra.MarketJsonConnector;
import otradotra.MarketType;
import otradotra.helper.ExplanationSingleton;
import otradotra.helper.HttpUtils;
import otradotra.helper.MarketNameHelper;
import otradotra.helper.NetworkOptimizatorSingleton;
import otradotra.helper.ReporterSingleton;

/**
 * Hello world!
 * 
 */

public class BTC_e {

	// Market connection data
	private static long _nonce;
	private static String _key;
	private static String _secret;

	// setup optimization costs
	private static MarketNameHelper[] allMarkets;
	private static Map<Integer, String> valueMapping;
	private static Map<String, Integer> keyMapping;
	private static MarketJsonConnector[] connector;
	private static int numberOfNodes;
	private static final double low = -10; // lowest difference

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
	
	
	
	
	
	public static void main(String[] args) throws InterruptedException {
		
		
		// 0. Market connection data
		_key = "FLSE6TVR-4JDIPJB8-S7WH5EZ6-HDFNXFXG-RR1FYGEG";
		_secret = "00fe262a56e0225d9b50c197e62556ef39a0d1f0ba3f81b814c45af962d0c9f3";

		Map<String, String> arguments = new HashMap<String, String>();
		// arguments.put("getInfo", "");
		// low = -100;
		// my info
		// System.out.println(authenticatedHTTPRequest("getInfo", arguments));

		// myactive orders
		// System.out.println(authenticatedHTTPRequest("ActiveOrders",
		// arguments));
		
		
		

		// init setup
		setup();
		NetworkOptimizatorSingleton.getInstance();
		
		// tuning java
				Properties props = System.getProperties();
				props.setProperty("http.keepAlive", "true");
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
			
			// 
			

			// lets execute multithreading (get data from internet)

			resources = getResources(numberOfNodes, valueMapping);
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

				CalculateOptimalVolumeProblem problem = new CalculateOptimalVolumeProblem(
						solverData, ReporterSingleton.roundHigh,
						ReporterSingleton.roundCurrency,
						ReporterSingleton.roundAround,
						ReporterSingleton.roundAround.size(), valueMapping,
						keyMapping, resources);
				problem.start();
			}
			
			//System.out.println(
			//		NetworkOptimizatorSingleton.getPool().getTotalStats());

			// Thread.sleep(100);
			// printNice();

		}

	}

	public static void tryCyclesEvaluation() {
		boolean explain = true;

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
				explanator = new StringBuffer();
				explanator.append("############ ");

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

						// TODO: calculation
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
						explanator.append("############");

						// System.out.println("Found soulution "+calc[i]);
						System.out.println(explanator.toString());

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

							ReporterSingleton.addInvolvedCount(calc[i1],
									valueMapping.get(i1), solverData,
									nodeMapping); 
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
						dataTransferError.set(true);
					}
					
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
						// TODO Auto-generated catch block
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
		resources.put(4, 4.44444);
		resources.put(5, 7.39);
		resources.put(6, 17.857);
		resources.put(7, 17.69);

		// make 5 of each // release -> set each to 0

		// release -> invoke market get data set iterate and fill if not

		return resources;
	}

	public static final void setup() {
		/*****
		 * START : start of the program - not changeable
		 */

		// truststore fix multithreading
		String fixble = HttpUtils
				.httpGet("https://btc-e.com/api/2/btc_usd/depth");
		// 1. List of markets / links / marketdata
		allMarkets = new MarketNameHelper[15];
		// btc
		allMarkets[0] = new MarketNameHelper("btc", "usd",
				"https://btc-e.com/api/2/btc_usd/depth");
		allMarkets[1] = new MarketNameHelper("btc", "rur",
				"https://btc-e.com/api/2/btc_rur/depth");
		allMarkets[2] = new MarketNameHelper("btc", "eur",
				"https://btc-e.com/api/2/btc_eur/depth");

		// ltc
		allMarkets[3] = new MarketNameHelper("ltc", "btc",
				"https://btc-e.com/api/2/ltc_btc/depth");
		allMarkets[4] = new MarketNameHelper("ltc", "usd",
				"https://btc-e.com/api/2/ltc_usd/depth");
		allMarkets[5] = new MarketNameHelper("ltc", "rur",
				"https://btc-e.com/api/2/ltc_rur/depth");
		allMarkets[6] = new MarketNameHelper("ltc", "eur",
				"https://btc-e.com/api/2/ltc_eur/depth");
		// fiat

		allMarkets[7] = new MarketNameHelper("usd", "rur",
				"https://btc-e.com/api/2/usd_rur/depth");
		allMarkets[8] = new MarketNameHelper("eur", "usd",
				"https://btc-e.com/api/2/eur_usd/depth");

		// nvc
		allMarkets[9] = new MarketNameHelper("nvc", "btc",
				"https://btc-e.com/api/2/nvc_btc/depth");
		allMarkets[10] = new MarketNameHelper("nvc", "usd",
				"https://btc-e.com/api/2/nvc_usd/depth");

		// nmc
		allMarkets[11] = new MarketNameHelper("nmc", "btc",
				"https://btc-e.com/api/2/nmc_btc/depth");
		allMarkets[12] = new MarketNameHelper("nmc", "usd",
				"https://btc-e.com/api/2/nmc_usd/depth");
		// ppc
		allMarkets[13] = new MarketNameHelper("ppc", "btc",
				"https://btc-e.com/api/2/ppc_btc/depth");
		allMarkets[14] = new MarketNameHelper("ppc", "usd",
				"https://btc-e.com/api/2/ppc_usd/depth");

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
					allMarkets[i].bid);

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

	/**
	 * Execute a authenticated query on btc-e.
	 * 
	 * @param method
	 *            The method to execute.
	 * @param arguments
	 *            The arguments to pass to the server.
	 * 
	 * @return The returned data as JSON or null, if the request failed.
	 * 
	 * @see http://pastebin.com/K25Nk2Sv
	 */
	public static final JSONObject authenticatedHTTPRequest(String method,
			Map<String, String> arguments) {
		_nonce = 5 + (long) (Math.random() * ((100000000 - 5) + 1));

		HashMap<String, String> headerLines = new HashMap<String, String>(); // Create
																				// a
																				// new
																				// map
																				// for
																				// the
																				// header
																				// lines.
		Mac mac;
		SecretKeySpec key = null;

		if (arguments == null) { // If the user provided no arguments, just
									// create an empty argument array.
			arguments = new HashMap<String, String>();
		}

		arguments.put("method", method); // Add the method to the post data.
		arguments.put("nonce", "" + _nonce); // Add the dummy nonce.

		String postData = "";

		for (Iterator argumentIterator = arguments.entrySet().iterator(); argumentIterator
				.hasNext();) {
			Map.Entry argument = (Map.Entry) argumentIterator.next();

			if (postData.length() > 0) {
				postData += "&";
			}
			postData += argument.getKey() + "=" + argument.getValue();
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

		// Encode the post data by the secret and encode the result as base64.
		try {
			headerLines.put("Sign", Hex.encodeHexString(mac.doFinal(postData
					.getBytes("UTF-8"))));
		} catch (UnsupportedEncodingException uee) {
			System.err.println("Unsupported encoding exception: "
					+ uee.toString());
			return null;
		}

		// Now do the actual request
		String requestResult = HttpUtils.httpPost("https://btc-e.com/tapi",
				headerLines, postData);

		if (requestResult != null) { // The request worked

			try {
				// Convert the HTTP request return value to JSON to parse
				// further.
				JSONObject jsonResult = JSONObject.fromObject(requestResult);

				// Check, if the request was successful
				int success = jsonResult.getInt("success");

				if (success == 0) { // The request failed.
					String errorMessage = jsonResult.getString("error");

					System.err.println("btc-e.com trade API request failed: "
							+ errorMessage);

					return null;
				} else { // Request succeeded!
					return jsonResult.getJSONObject("return");
				}

			} catch (JSONException je) {
				System.err.println("Cannot parse json request result: "
						+ je.toString());

				return null; // An error occured...
			}
		}

		return null; // The request failed.
	}

}
