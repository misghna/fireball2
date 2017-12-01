package com.sesnu.main;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController;
import com.sesnu.handler.AccountHandler;
import com.sesnu.handler.ConnectionHandler;
import com.sesnu.handler.LiveOrdersHandler;
import com.sesnu.handler.PositionHandler;
import com.sesnu.model.CandidateTicker;
import com.sesnu.service.DAOService;
import com.sesnu.service.Runner;
import com.sesnu.service.SmartScanner;
import com.sesnu.service.Util;
import com.sesnu.service.WatchDog;

public class App {

	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	private static final int PORT_NO = 4002;
	private static final int CONNECTION_ID = 2;

	
	public static void main(String[] args) throws InterruptedException {
		mainL.info("Initializing ...");
		double dTime = Util.getDoubleTime();
		
		
		if(!Util.isDevMode()){
			mainL.info("now is {} Waiting for 9:35AM mark to scan pre market ...", dTime);
			while(!(args.length>0 && args[0].equals("manual")) && (dTime < 9.35 || dTime > 16)){
				Thread.sleep(60000);
				dTime = Util.getDoubleTime();
			}
			Util.sendMail("Fireball",Util.getString("notificationEmail") ,"Started @ " + Util.getDoubleTime());
		}

		
		mainL.info("Starting now is {} ", dTime);
		
		mainL.info("Scanning Historical ...");
		
		ConnectionHandler conHandler = new ConnectionHandler();
		ApiController api = new ApiController(conHandler,new LoggerIn(),new LoggerOut());
		api.connect("localhost", PORT_NO, CONNECTION_ID, null);
		
		while(!conHandler.isConnected() && !conHandler.isIbControllerOff()){
			mainL.info("waiting for connection to establish ...");
			Thread.sleep(5000);		
		}
		
		if(conHandler.isIbControllerOff()){
			mainL.error("IB Controller is OFF, please turn on TWS and try again!");
			Util.sendMail("Fireball",Util.getString("notificationEmail") ,"Terminated @ " + Util.getDoubleTime());
			System.exit(0);
		}
		mainL.info("API Connection established on Port No {} and connection Id {}",PORT_NO,CONNECTION_ID);
		
		Map<String,Double> tickerMap = new HashMap<String,Double>();
		
		boolean autoFetchTickerCandidates = !(args.length>0 && args[0].equals("manual"));
		

		tickerMap = new HashMap<String,Double>();
		if(args!=null && args.length>0 && args[1].indexOf("=")>0){
			for (int i=1; i<args.length;i++) {
				String ticker = args[i].split("=")[0];
				Double boxSize = Double.parseDouble(args[i].split("=")[1]);
				tickerMap.put(ticker.toUpperCase(), boxSize);
			}

		}

		
		if(!autoFetchTickerCandidates && tickerMap.size()==0){
			mainL.info("add viable tickers as arguments");
			System.exit(0);
		}
		
		mainL.info("{} tickers are ready for trade, total amount roughly {}",tickerMap.toString());
		DAOService daoService = new DAOService();
		
		
		AccountHandler acctHandler = new AccountHandler(daoService);
		api.reqAccountUpdates(true,Util.getAcctNo(),acctHandler);
		
		PositionHandler posHand = new PositionHandler();
		api.reqPositions(posHand);

		
		LiveOrdersHandler liveOrdersHand = new LiveOrdersHandler(daoService);
		if(!Util.isDevMode())api.reqLiveOrders(liveOrdersHand);
		
		ScheduledExecutorService schedulerThread = Executors.newScheduledThreadPool(1);
		WatchDog watchDog = new WatchDog(posHand, api,daoService);
		schedulerThread.scheduleAtFixedRate(watchDog, 0, 5, TimeUnit.MINUTES);
		
		// run the tickers
		
		List<Runner> runners = new ArrayList<Runner>();
		ExecutorService executor = Executors.newFixedThreadPool(50);
		Set<String> tickersAdded = new HashSet<String>();
		int callId=1;
		while(true){
			if(autoFetchTickerCandidates)tickerMap = new SmartScanner().process(api,callId,tickersAdded);
			if(tickerMap!=null && tickerMap.size()>0){
				for (Map.Entry<String, Double> entry : tickerMap.entrySet()) {
					if(!tickersAdded.contains(entry.getKey())){
						tickersAdded.add(entry.getKey());
						mainL.info("Initializing '{}' Ticker",entry.getKey());
						Runner runner = new Runner(entry.getKey(),entry.getValue(),api,
												acctHandler,posHand,liveOrdersHand,daoService);					
						executor.execute(runner);
						runners.add(runner);
						CandidateTicker ct = new CandidateTicker(entry.getKey(),Util.getDateStr(System.currentTimeMillis()),entry.getValue());
						daoService.save(ct);
						Thread.sleep(10000);	// giving room for historical pace
					}
				}
			}
			mainL.info("Total tickers running reached {}",tickersAdded.size());
			callId ++;
		  Thread.sleep(Util.getInt("sannerIntervalSec")*1000);
	  }
		

	}

	
}
