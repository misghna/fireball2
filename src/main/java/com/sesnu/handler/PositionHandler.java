package com.sesnu.handler;

import java.util.HashMap;
import java.util.Map;

import com.ib.client.Contract;
import com.ib.controller.ApiController.IPositionHandler;
import com.sesnu.model.FBPosition;
import com.sesnu.service.Processor;

public class PositionHandler implements IPositionHandler {


	private Map<String,Processor> processorMap;	
	private volatile Map<String,FBPosition> positionMap;
	
	
	public PositionHandler() {
		processorMap = new HashMap<String,Processor>();
		positionMap = new HashMap<String,FBPosition>();
	}
	
	@Override
	public void position(String account, Contract contract, double pos, double avgCost) {
		positionMap.put(contract.symbol(), new FBPosition((int)pos,avgCost));
		updatePosition(contract.symbol());
		updateTotalAmount();
	}



	private void updateTotalAmount(){
		double totalPositionAmount=0;
		for(Map.Entry<String, FBPosition> entry : positionMap.entrySet()){
			totalPositionAmount += Math.abs(entry.getValue().getShares()) * entry.getValue().getAvgCost();
		}
		for(Map.Entry<String,Processor> entry : processorMap.entrySet()){
			entry.getValue().updateTotalUsedAmount(totalPositionAmount);
		}
	}
	
	public void addProcessor(String ticker,Processor proccesor) {
		processorMap.put(ticker,proccesor);
		updatePosition(ticker);
	}
	
	private void updatePosition(String ticker){
		if(processorMap.containsKey(ticker) && positionMap.containsKey(ticker)){
			processorMap.get(ticker).updatePosition(positionMap.get(ticker));
		}
	}

	
	public Map<String,FBPosition> getPositions(){
		return positionMap;
	}
	
	@Override
	public void positionEnd() {}
}
