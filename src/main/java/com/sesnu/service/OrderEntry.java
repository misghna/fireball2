package com.sesnu.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderType;
import com.ib.client.Types.Action;
import com.ib.controller.ApiController;
import com.ib.controller.Bar;
//import com.sesnu.fireBall.Utilities.Util;
//import com.sesnu.fireBall.model.HotOrder;
import com.sesnu.handler.OrderHandler;
import com.sesnu.model.FBBar;
import com.sesnu.model.FBPosition;
import com.sesnu.model.SubmitedOrder;


public class OrderEntry {
	
	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	
	static final double MAX_AMOUNT_PER_TRADE = 25000; //USD
	static final double MAX_BOX_WEIGHT = 100; // USD
	
	private String ticker;
	private ApiController api;
	private OrderHandler orderHandler;
	private int parentId;
	private long lastClose=0;
	private long lastMainOrder=0;
//	private Util util;
	
	public OrderEntry(String ticker,ApiController api,OrderHandler orderHandler) {
		this.ticker = ticker;
		this.api=api;
//		util = new Util();
	}


	public SubmitedOrder placeMainOrder(String orderType,double entryPrice,double stopLossLimit,double exitPrice, int pos,double boxSize){
		
		if(System.currentTimeMillis()-lastMainOrder<50000) return null;
		
		int shares = 0; 
		if(pos!=0){
			shares = Math.abs(pos) * 2; // reversal
		}else{
			shares = (int) (MAX_BOX_WEIGHT/boxSize);
			int maxShares = (int) (MAX_AMOUNT_PER_TRADE/entryPrice);
			shares = shares < maxShares? shares:maxShares;
		}

		SubmitedOrder fbOrder = new SubmitedOrder(ticker,System.currentTimeMillis(),"Main");
				
		List<Order> bracketOrders = getBracketOrder(orderType,shares,Util.roundTo2D(entryPrice),Util.roundTo2D(stopLossLimit),Util.roundTo2D(exitPrice),fbOrder);

	       for (int i=0; i<bracketOrders.size(); i++) {
	    	   Order order = bracketOrders.get(i);
	    	   if(!Util.isDevMode())api.placeOrModifyOrder(getContract(ticker), order, orderHandler);
	       }
	       return fbOrder;
	}
	
	
	public SubmitedOrder placeMktOrder(Action action,int shares){
		
		if(System.currentTimeMillis()-lastMainOrder<50000) return null;

			SubmitedOrder fbOrder = new SubmitedOrder(ticker,System.currentTimeMillis(),"Main");
				
			int orderId = api.getNextOrderId();
			Order order = new Order();
			order.orderId(orderId);
			order.action(action);	        
			order.totalQuantity(shares);	        
			order.orderType(OrderType.MKT);		
			order.transmit(true);
			fbOrder.setOrder(order);
			if(!Util.isDevMode()){
				api.placeOrModifyOrder(getContract(ticker), order, orderHandler);
			}
	       return fbOrder;
	}

	
    public List<Order> getBracketOrder(String action, double quantity, double entryPrice,double stopLossPrice,double exitPrice,SubmitedOrder fbOrder) {
			List<Order> bracketOrder = new ArrayList<Order>();
			
			//This will be our main or "parent" order
			int parentOrderId = api.getNextOrderId();
			Order parentOrder = new Order();
			parentOrder.orderId(parentOrderId);
			parentOrder.action(action);	        
			parentOrder.totalQuantity(quantity);	        
			parentOrder.orderType(OrderType.MKT);
			parentOrder.lmtPrice(entryPrice);			
//			long expireTime = System.currentTimeMillis() + 600000; // wait 50 sec
//			parentOrder.goodTillDate(Util.formatTimeStamp(expireTime));
//			parentOrder.tif("GTD");
			parentOrder.transmit(true);
			fbOrder.setOrder(parentOrder);

//			Order takeProfit = new Order();
//			int exitId = api.getNextOrderId();
//	        takeProfit.orderId(exitId);
//	        takeProfit.action(action.equals("BUY") ? "SELL" : "BUY");
//	        takeProfit.orderType("LMT");
//	        takeProfit.totalQuantity(quantity);
//	        takeProfit.lmtPrice(exitPrice);
//	        takeProfit.parentId(parentOrderId);
//	        takeProfit.transmit(false);
	        
//			Order stopLossOrder = new Order();
//			int stpId = api.getNextOrderId();
//			stopLossOrder.orderId(stpId);
//			stopLossOrder.action(action.equals("BUY") ? "SELL" : "BUY");
//			stopLossOrder.orderType(OrderType.STP);
//			//Stop trigger price
//			stopLossOrder.auxPrice(stopLossPrice);
//			stopLossOrder.totalQuantity(quantity);
//			stopLossOrder.parentId(parentOrderId);
//			stopLossOrder.transmit(true);
//			fbOrder.setStpOrder(stopLossOrder);
								
			bracketOrder.add(parentOrder);	
//			bracketOrder.add(takeProfit);
//			bracketOrder.add(stopLossOrder);
			
			
			mainL.info("{} ~ Order details- orderId: {},stpOrderId: {}, exitOrderId: {}, action: {}, entry-price: {}, stopPrice: {}, exitPrice: {}",ticker,
									parentOrderId,0,0,action,entryPrice,stopLossPrice,exitPrice);
			return bracketOrder;
    }
	
    
    
	private static Contract getContract(String ticker){
		   Contract contract = new Contract();
	       contract.symbol(ticker); 

	       contract.secType("STK");

	       contract.exchange("SMART"); 
	       
	       contract.currency("USD");
	       
	       contract.primaryExch("ISLAND");
	       
	       return contract;
	}

	public int getParentId(){
		return parentId;
	}


//	public void setPos(Portofolio pos) {
//		this.pos = pos;
//	}


	
	public void updateStp(Order order, double stpLoss) {

		if(stpLoss != order.auxPrice()){
			mainL.info("{} orderId {} STP moving from {} To ",ticker, order.orderId(),order.auxPrice(),stpLoss);
			order.auxPrice(stpLoss);
			if(!Util.isDevMode())api.placeOrModifyOrder(Util.getContract(ticker), order, new OrderHandler());
		}

	}
	
	public synchronized Order closePosition(FBPosition pos){
//		if(System.currentTimeMillis()-lastClose > 15000){
			lastClose = System.currentTimeMillis();
			Order closeOrder = new Order();
			closeOrder.orderId(api.getNextOrderId());
			closeOrder.action(pos.getShares() < 0 ? "BUY" : "SELL");
			closeOrder.orderType(OrderType.MKT);
			closeOrder.totalQuantity(Math.abs(pos.getShares()));	
			closeOrder.transmit(true);
			if(!Util.isDevMode()){
				api.placeOrModifyOrder(Util.getContract(ticker), closeOrder, new OrderHandler());
			}
			lastClose = System.currentTimeMillis();
			return closeOrder;
//		}else{			
//			mainL.info("{} ! closing position rejected, closing is allowed once every 15 seconds",ticker);
//			return null;
//		}
	}
	
	public synchronized Order closePosition(FBPosition pos,double price){
//		if(System.currentTimeMillis()-lastClose > 15000){
			lastClose = System.currentTimeMillis();
			Order closeOrder = new Order();
			closeOrder.orderId(api.getNextOrderId());
			closeOrder.action(pos.getShares() < 0 ? "BUY" : "SELL");
			closeOrder.orderType(OrderType.MKT);
//			closeOrder.lmtPrice(price);
			closeOrder.totalQuantity(Math.abs(pos.getShares()));	
			closeOrder.transmit(true);
			if(!Util.isDevMode())api.placeOrModifyOrder(Util.getContract(ticker), closeOrder, new OrderHandler());
			lastClose = System.currentTimeMillis();
			return closeOrder;
//		}else{
//			mainL.info("{} ! closing position rejected, closing is allowed once every 15 seconds",ticker);
//			return null;
//		}
	}
	
}
