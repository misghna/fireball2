package com.sesnu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Contract;
import com.ib.client.Types.BarSize;
import com.ib.client.Types.DurationUnit;
import com.ib.client.Types.WhatToShow;
import com.ib.controller.ApiController;
import com.sesnu.handler.AccountHandler;
import com.sesnu.handler.HistoricalHandler;
import com.sesnu.handler.LiveOrdersHandler;
import com.sesnu.handler.PositionHandler;
import com.sesnu.ui.ChartController;
import com.sesnu.ui.ChartViewer;

public class Runner implements Runnable{

	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	
	private String ticker;
	private ApiController api;
	private double boxSize;
	private AccountHandler acctHandler;
	private PositionHandler posHand;
	private LiveOrdersHandler liveOrdersHand;
	private DAOService daoService;
	
	public Runner(String ticker,double boxSize,ApiController api,
			AccountHandler acctHandler,PositionHandler posHand,
			LiveOrdersHandler liveOrdersHand,DAOService daoService){
		this.ticker=ticker;
		this.api=api;
		this.boxSize=boxSize;
		this.acctHandler=acctHandler;
		this.posHand=posHand;
		this.liveOrdersHand=liveOrdersHand;
		this.daoService=daoService;
	}
	
	public void run(){
		try{

						
			OrderExit exitOrder = new OrderExit(ticker,api);
			new Thread(exitOrder).start();;
						
			Processor pr = new Processor(api,ticker,boxSize,daoService);
			posHand.addProcessor(ticker,pr);
			acctHandler.addProcessor(ticker,pr);
			liveOrdersHand.addProcessor(ticker,pr);
								
			HistoricalHandler tradesHist =  new HistoricalHandler(pr,ticker);
	//		api.reqPnLSingle(account, modelCode, conId, handler);

						
			api.reqHistoricalData(getContract(ticker,null), "",  2, DurationUnit.DAY,BarSize._1_min, WhatToShow.TRADES, true, true, tradesHist);

	
		}catch(Exception e){
			mainL.error("{} ~ terminated due to ", ticker, e);
		}
		
	}
	
	private static Contract getContract(String ticker,String ex){
		   Contract contract = new Contract();
	       contract.symbol(ticker); 
	       contract.secType("STK");
	       contract.currency("USD");	       
	       if(ex==null){
	    	   contract.exchange("SMART"); 
	       }else{
	    	   contract.exchange(ex); 
	       }
	       contract.primaryExch("ISLAND");
	       return contract;
	}
	
}

//RealTimeHandler bidReal = new RealTimeHandler(WhatToShow.BID,pr,exitOrder);
//RealTimeHandler askReal = new RealTimeHandler(WhatToShow.ASK,pr,exitOrder);
//TopMktHandler topMrkHand = new TopMktHandler(pr);
//DeepMktHandler depthMrkNyseHand = new DeepMktHandler(pr,"NYSE");
//DeepMktHandler depthMrkNsdaqHand = new DeepMktHandler(pr,"NASDAQ");


//api.reqTopMktData(getContract(ticker), "", false, false, topMrkHand);
//api.reqDeepMktData(getContract(ticker,"NYSE"), 10, depthMrkNyseHand);
//api.reqDeepMktData(getContract(ticker,"ISLAND"), 10, depthMrkNsdaqHand);
//api.reqRealTimeBars(getContract(ticker,null), WhatToShow.BID, true, bidReal); 
//api.reqRealTimeBars(getContract(ticker,null), WhatToShow.ASK, true, askReal); 
//while(true){}