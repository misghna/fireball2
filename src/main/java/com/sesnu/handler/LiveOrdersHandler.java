package com.sesnu.handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.OrderStatus;
import com.ib.controller.ApiController.ILiveOrderHandler;
import com.sesnu.model.FBOrderState;
import com.sesnu.model.FBOrderStatus;
import com.sesnu.service.DAOService;
import com.sesnu.service.Processor;

public class LiveOrdersHandler implements ILiveOrderHandler {

	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	
	private Map<String,Processor> processorMap;	
	private Map<String,FBOrderState> stateMap;
	private Set<String> msgSet;
	private Set<Integer> filledSet;
	private DAOService daoService;
	
	public LiveOrdersHandler(DAOService daoService){
		this.daoService = daoService;
		processorMap = new HashMap<String,Processor>();
		stateMap = new HashMap<String,FBOrderState>();
		msgSet = new HashSet<String>();
		filledSet = new HashSet<Integer>();
	}
	
	
	public void addProcessor(String ticker,Processor proccesor) {
		processorMap.put(ticker,proccesor);
		updateState(ticker);
	}
	
	private void updateState(String ticker){
		if(processorMap.containsKey(ticker) && stateMap.containsKey(ticker)){
			processorMap.get(ticker).setOrderStatus(stateMap.get(ticker));
		}
	}
	
	@Override
	public void openOrder(Contract contract, Order order, OrderState orderState) {
		String msg = contract.symbol()  + order.getOrderType() + orderState.getStatus() + order.orderId();
		
		if(!msgSet.contains(msg)){
	
			mainL.info("{} ~ Order Type {}, Order Status {}, Order Id {}",
					contract.symbol(), order.getOrderType() ,orderState.getStatus(),order.orderId());
			msgSet.add(msg);
			
			stateMap.put(contract.symbol(),new FBOrderState(order,orderState));
			updateState(contract.symbol());
		}	

	}



	@Override
	public void orderStatus(int orderId, OrderStatus status, double filled, double remaining, double avgFillPrice,
			long permId, int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
		
		if(status.equals(OrderStatus.Filled) && !filledSet.contains(orderId)){
			daoService.update(orderId,avgFillPrice);
			filledSet.add(orderId);
		}
	}




	@Override
	public void handle(int orderId, int errorCode, String errorMsg) {
		mainL.error("Order Id {}, Error Messag ~ {}", orderId,errorMsg);
	}

	@Override
	public void openOrderEnd() {
	}
}
