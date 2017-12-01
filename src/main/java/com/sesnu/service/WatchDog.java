package com.sesnu.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Order;
import com.ib.client.OrderType;
import com.ib.controller.ApiController;
import com.sesnu.handler.OrderHandler;
import com.sesnu.handler.PositionHandler;
import com.sesnu.model.FBPosition;
import com.sesnu.model.Trade;

public class WatchDog implements Runnable{
	
	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	
	private PositionHandler positionHand;
	private ApiController api;
//	private Util util;
	private boolean positionsClosed;
	private DAOService daoService;
	private double forcedClosePositionTime;
	private double shutdownTime;
	
	public WatchDog(PositionHandler positionHand,ApiController api,DAOService daoService){
		this.positionHand=positionHand;
		this.api=api;
		this.daoService=daoService;
//		this.util=new Util();
		forcedClosePositionTime = Util.getDouble("forcedClosePositionTime");
		shutdownTime = Util.getDouble("shutdownTime");
	}


	@Override
	public void run() {
	
		try{

			if(!positionsClosed && Util.getDoubleTime() >= forcedClosePositionTime){
				close();
			}
	
			if(positionsClosed && Util.getDoubleTime() >= shutdownTime){
				shutdown();
			}
		} catch (Exception e) {
			mainL.error("Error in watchdog",e);
		}	
	}
	
	private void shutdown() {
		if(!Util.isDevMode()){
			mainL.info("shutting down at {}",Util.getDoubleTime());
			Util.sendMail("Fireball",Util.getString("notificationEmail") ,"Shutdown @ " + Util.getDoubleTime());
			System.exit(0);
		}
		
	}


	private void close(){
		Map<String, FBPosition> positions = positionHand.getPositions();
		if(Util.isDevMode() || positions==null)return;
		for(Map.Entry<String, FBPosition> entry : positions.entrySet()){
			if(entry.getValue().getShares()!=0){
				mainL.info("{} ~ closing positions {}, end of day time {}",
						entry.getKey(),entry.getValue(),Util.getDoubleTime());
				closePosition(entry.getKey(),entry.getValue());
			}
		}
		positionsClosed = true;
	}
	
	
	public void closePosition(String ticker,FBPosition pos){
			Order closeOrder = new Order();
			int orderId = api.getNextOrderId();
			closeOrder.orderId(api.getNextOrderId());
			closeOrder.action(pos.getShares() < 0 ? "BUY" : "SELL");
			closeOrder.orderType(OrderType.MKT);
			closeOrder.totalQuantity(Math.abs(pos.getShares()));	
			closeOrder.transmit(true);
			if(!Util.isDevMode()){				
				api.placeOrModifyOrder(Util.getContract(ticker), closeOrder, new OrderHandler());
				String action = pos.getShares()>0? "SELL":"BUY";
				Trade trade = new Trade(orderId,ticker, System.currentTimeMillis(), action, "Close", 
									0d,pos.getShares(),0d,orderId,"End day, watchDog","UNIV-CLOSE");
				daoService.save(trade);
			}
	}
}
