package com.sesnu.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Contract;
import com.ib.client.ScannerSubscription;
import com.ib.controller.ApiController;
import com.ib.controller.ScanCode;
import com.sesnu.backtest.DataCollector;
import com.sesnu.handler.ConnectionHandler;
import com.sesnu.handler.ScannerHandler;
import com.sesnu.main.LoggerIn;
import com.sesnu.main.LoggerOut;
import com.sesnu.model.FBBar;
import com.sesnu.model.MiniBar;


public class SmartScanner {
	
	
	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	
	private static final int PORT_NO = 4002;
	private static final int CONNECTION_ID = 5;
	private static final int HISTORICAL_DAYS =2;
	private static final int MAX_TOTAL_AMOUNT_TO_TRADE = 150000;  // in $
	private static final double MAX_BOX_WEIGHT = 100; // USD
	private static final double MIN_BOX_SIZE = 0.1;
	private static String topPerGain = "TOP_PERC_GAIN";
	private static String topPerLose = "TOP_PERC_LOSE";
	private static String mostActive = "MOST_ACTIVE";
	static final double MAX_AMOUNT_PER_TRADE = 25000; //USD
	
	
	public static void main(String[] args) throws InterruptedException {
		
		ConnectionHandler conHandler = new ConnectionHandler();
		ApiController api = new ApiController(conHandler,new LoggerIn(),new LoggerOut());
		api.connect("localhost", PORT_NO, CONNECTION_ID, null);
		
		while(!conHandler.isConnected()){
			Thread.sleep(5000);		
		}
		Map<String,Double> result = new SmartScanner().process(api,1,null);
	
		mainL.info("Scanner End Result -> " + result);
		
	}
	
	public Map<String,Double> process(ApiController api,int callId,Set<String> existingList) {
		
		try{
						
			ScannerHandler scannerHandlerTG = new ScannerHandler();
			
			api.reqScannerSubscription(getSubDetail(mostActive), scannerHandlerTG);
			while(!scannerHandlerTG.isDone() || !scannerHandlerTG.isDone()){
				mainL.info("waiting scanners to finish");
				Thread.sleep(1000);				
			}
			
			mainL.info("Getting most active tickers from IB ...");
			List<String> topTickers = scannerHandlerTG.getScannResult();
			mainL.info("Most Active Stocks list {}",topTickers.toString());
			
			// remove the already running tickers
			if(existingList!=null && existingList.size()>0){
				for (String ticker : existingList) {
					if(topTickers.contains(ticker)){
						topTickers.remove(ticker);
					}
				}
			}
			if(topTickers.size()==0){
				mainL.info("scanner run at {} for update check but found no new tickers", Util.getDoubleTime());
				return null;
			}
			
//			if(callId<2){
//				mainL.info("Getting Stocks must watch from wall street jornal ...");
//				List<String> wsjTickers = Util.getWSJ();			
//				mainL.info("Stocks must watch from WSJ {}",wsjTickers.toString());			
//				topTickers.addAll(wsjTickers);
//			}
			// adding daily ticker members
			String[] alwaysTickers = Util.getString("AlwaysTickers").split(",");
			for (String ticker : alwaysTickers) {
				topTickers.add(ticker);
			}
			
			Map<String,List<FBBar>> historicalMapTG = getHistorical(topTickers);
			
			mainL.info("analyzing renko data ...");
//			Map<String,Double> allResult = filterTickers(historicalMapTG);
			Map<String,Double> allResult = calculateNoise(historicalMapTG);
			mainL.info("Scanner Results at {} are {}",Util.getDoubleTime(),allResult.toString());
			
			api.cancelScannerSubscription(scannerHandlerTG);
			
			return allResult;
		}catch(Exception e){
			mainL.error("Error while preparing ticker candidates",e);
		}
		return null;
	}
	
	
	private Map<String,Double> calculateNoise(Map<String,List<FBBar>> historicalMap){
			Map<String,Double> candidateMap = new HashMap<String,Double>();
			Map<String,Double> priceMap = new HashMap<String,Double>();
			for (Map.Entry<String, List<FBBar>> entry : historicalMap.entrySet()) {
				if(entry.getValue().size()>0){
					String ticker = entry.getKey();
					Double boxSize = getBoxSize(entry.getValue());
					if(boxSize >= MIN_BOX_SIZE){
						candidateMap.put(ticker,boxSize);
					}
				}
			}
			return candidateMap;
	}
	
	private Map<String,Double> filterTickers(Map<String,List<FBBar>> historicalMap){
		Map<String,Double> candidateMap = new HashMap<String,Double>();
		Map<String,Double> priceMap = new HashMap<String,Double>();
		for (Map.Entry<String, List<FBBar>> entry : historicalMap.entrySet()) {
			String ticker = entry.getKey();
			Double boxSize = getBoxSize(entry.getValue());
			FBBar lastBar = entry.getValue().get(entry.getValue().size()-1);
			if(boxSize >= MIN_BOX_SIZE){
				priceMap.put(ticker + "_" + boxSize, lastBar.close());
				Double profitRatio = analyseRenko(entry.getValue(),boxSize);
				if(profitRatio>0)candidateMap.put(ticker + "_" + boxSize, profitRatio);
			}
		}
		
		// sort by profit ratio
		mainL.info("Presorted candidate list {}", candidateMap.toString());
		candidateMap = Util.sortByValue(candidateMap);
		mainL.info("sorted candidate list {}", candidateMap.toString());
		
		// select to limit max account trade limit
		double estTotal=0;
		Map<String,Double> finalCandidateMap = new HashMap<String,Double>();
		for(Map.Entry<String, Double> entry : candidateMap.entrySet()){
			String ticker = entry.getKey().split("_")[0];
			double boxSize = Double.parseDouble(entry.getKey().split("_")[1]);
			double estimatedTickerTradeValue = (MAX_BOX_WEIGHT/boxSize) * priceMap.get(entry.getKey());
			if(estimatedTickerTradeValue>MAX_AMOUNT_PER_TRADE)estimatedTickerTradeValue=MAX_AMOUNT_PER_TRADE;
			
			mainL.info("estimated share vlue for {} is {}", ticker,estimatedTickerTradeValue);
			if(estTotal + estimatedTickerTradeValue < MAX_TOTAL_AMOUNT_TO_TRADE){
				estTotal += estimatedTickerTradeValue;
				finalCandidateMap.put(ticker, boxSize);
			}
		}
		
		mainL.info("total estimated cash flow requred for the {} ticker is {} Dollars!",finalCandidateMap.size(),Math.round(estTotal));
		mainL.info("Scanner End Result -> {}", finalCandidateMap.toString());
		return finalCandidateMap;
		
	}
	
	private double getBoxSize(List<FBBar> barList){
		
		List<Double>heightList = new ArrayList<Double>();
		for (FBBar fbBar : barList) {
			heightList.add(fbBar.getBodyHeight());
		}
		Collections.sort(heightList);
		int indx = (int) ((heightList.size()-1)*0.98);
		
		return Util.roundTo2D(heightList.get(indx));
	}
	
	
	private Map<String,List<FBBar>> getHistorical(List<String> tickers) 
			throws ClientProtocolException, IOException{
	
		mainL.info("Fetching historical data from google finance api for {} tickers ",tickers.size());
		Map<String,List<FBBar>> historicalMap = new HashMap<String,List<FBBar>>();
		DataCollector dc = new DataCollector();
		if(tickers!=null && tickers.size()>0){
			for (int i=0; i< tickers.size();i++) {
				String ticker = tickers.get(i);
				List<FBBar> barList =  dc.get(HISTORICAL_DAYS, 60, ticker, false);
				historicalMap.put(ticker, barList);
			}
		}
		return historicalMap;
	}
	
	private ScannerSubscription getSubDetail(String scanCode){
		ScannerSubscription s = new ScannerSubscription();
		s.instrument("STK");
		s.locationCode("STK.US.MAJOR");
		s.scanCode(scanCode);
		s.numberOfRows(25);
		s.abovePrice(20);
		s.belowPrice(250);
//		s.scanCode(ScanCode.LOW_OPEN_GAP.name());
//		s.aboveVolume(1000000);
		s.marketCapAbove(1000000000);
		return s;
	}
	
	private List<Double> boxSizeList = Arrays.asList(new Double[]{1.0,0.5,0.3,0.25,0.1});
	
	private double analyseRenko(List<FBBar> list,double boxSize){
		Indicators indicator = new Indicators();
		String ticker = list.get(0).getTicker();

		//		for (Double boxSize : boxSizeList) {
			List<MiniBar> renkoList = null;
			for (FBBar fbBar : list) {
				renkoList = indicator.addRenkoOC(renkoList, fbBar, boxSize,null);
			}			
			if(renkoList!=null && renkoList.size()>0){
				int curveCount =0;int dubCount =0;
				for (int i=1;i<renkoList.size();i++) {
					if(!renkoList.get(i-1).getCandleType().equals(renkoList.get(i).getCandleType())){
						curveCount ++;
					}
					if(renkoList.get(i-1).getTime()/1000==renkoList.get(i).getTime()/1000)dubCount ++;
				}
				int avgCurvePerDay = curveCount/HISTORICAL_DAYS;
				double profiteDollar = Util.roundTo2D((renkoList.size()-curveCount*2) * boxSize);
				double profitRatio = (renkoList.size() - curveCount *2)*100/renkoList.size();
				Integer blockPerDay = renkoList.size()/HISTORICAL_DAYS;
				
					String report = ticker  + "," + boxSize + "," + renkoList.size() + "," + 
							blockPerDay + "," + curveCount + "," +  avgCurvePerDay + "," + profiteDollar + "," +
							profitRatio + "," + dubCount;
					mainL.info("PreMarket Analysis - {}",report);
//				if(blockPerDay>10){
					return profitRatio;					
//				}else{
//					return 0;
//				}
			}
			return 0;
//		}			
	}
}
