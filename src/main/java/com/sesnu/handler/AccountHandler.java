package com.sesnu.handler;

import java.util.HashMap;
import java.util.Map;

import com.ib.controller.ApiController.IAccountHandler;
import com.ib.controller.Position;
import com.sesnu.model.AcctHistory;
import com.sesnu.model.PnL;
import com.sesnu.service.DAOService;
import com.sesnu.service.Portofolio;
import com.sesnu.service.Processor;
import com.sesnu.service.Util;

public class AccountHandler implements IAccountHandler {

	
	private Map<String,Processor> processorMap;
	
	private Map<String,Portofolio> positionMap;
	
	private Map<String,Double> realPnlMap;
	private Map<String,Double> unRealPnlMap;
	private double lastTotal=0;
	private DAOService daoService;
	private long lastAcctLog=0;
	private boolean wrapUp;
	
	public AccountHandler(DAOService daoService){
		processorMap = new HashMap<String,Processor>();
		this.positionMap = new HashMap<String,Portofolio>();
		this.realPnlMap = new HashMap<String,Double>();
		this.unRealPnlMap = new HashMap<String,Double>();
		this.daoService=daoService;
	}
	
	
	@Override
	public void updatePortfolio(Position pos) {
		String ticker = pos.contract().symbol();
		positionMap.put(ticker, new Portofolio(pos));
		updatePosition(ticker);
		
		realPnlMap.put(ticker, pos.realPnl());
		unRealPnlMap.put(ticker, pos.unrealPnl());
		
		double newTotal = getTotal(realPnlMap) + getTotal(unRealPnlMap);
		if(Math.abs(newTotal-lastTotal)>50){
			lastTotal = newTotal;
			PnL pnl = new PnL(System.currentTimeMillis(),getTotal(unRealPnlMap),getTotal(realPnlMap));
			daoService.save(pnl);
		}

	}
	
	private double getTotal(Map<String,Double> pnl){
		double total =0;
		for(Map.Entry<String, Double> entry : pnl.entrySet()){
			total +=entry.getValue();
		}
		return total;
	}
	
	public void addProcessor(String ticker,Processor proccesor) {
		processorMap.put(ticker,proccesor);
		updatePosition(ticker);
	}
	
	private void updatePosition(String ticker){
		if(processorMap.containsKey(ticker) && positionMap.containsKey(ticker)){
			processorMap.get(ticker).updatePortofolio(positionMap.get(ticker));
		}
	}
		
	
	@Override
	public void accountDownloadEnd(String arg0) {

	}

	@Override
	public void accountTime(String arg0) {


	}

	@Override
	public void accountValue(String acctNo, String type, String amount, String currency) {
		long now = System.currentTimeMillis();
		if(type.equals("NetLiquidation") ){
			if(now-lastAcctLog > 1800000 || (!wrapUp && Util.getDoubleTime()>=15.30) ){
				wrapUp = true;
				lastAcctLog = now;
				AcctHistory acctHist = new AcctHistory(now,Double.parseDouble(amount),acctNo);
				daoService.save(acctHist);
			}
		}
	}


	

}
