package com.sesnu.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.ib.client.Order;
import com.ib.controller.ApiController;
import com.sesnu.handler.OrderHandler;
import com.sesnu.model.FBBar;
import com.sesnu.model.MiniBar;
import com.sesnu.model.SubmitedOrder;

public class OrderExit implements Runnable{
	
	
	private String ticker;;
	private ApiController api;
	private Portofolio pos;
	private double ask;
	private double bid;
	private Direction dir;
	private Map<Integer,Order> stpOrder;
	private long lastClose=0;
	private List<Integer> parentIds;
	private Map<String,List<SubmitedOrder>> closeOrderMap;
	private Map<Long,Double> stpLossPrices;
	
	public OrderExit(String ticker,ApiController api){
		this.api=api;
		this.ticker=ticker;
//		stpIds = new ArrayList<Integer>();
		stpOrder = new HashMap<Integer,Order>();
		parentIds = new ArrayList<Integer>();
		closeOrderMap = new HashMap<String,List<SubmitedOrder>>();
		stpLossPrices= new HashMap<Long,Double>();
	}


	@Override
	public void run() {
		try{
			while(true){
				if(pos!=null && pos.getPos()!=0 && parentIds.size()>0){
//					if(pos.getPos()>0 && dir!=null && dir.name().indexOf("DOWN")>-1){	//Sell
//						closeOrder("SELL",(int)pos.getPos());					
//					}else if(pos.getPos()<0 && dir!=null && dir.name().indexOf("UP")>-1){ //buy
//						closeOrder("BUY",(int)pos.getPos());					
//					}
					Integer parentId = parentIds.get(parentIds.size()-1);
					String key = parentId + "_1stcloseAtDelta";
					if(!closeOrderMap.containsKey(key) && stpLossPrices.containsKey(parentId)){
						double actualDelta = Math.abs(stpLossPrices.get(parentId)-pos.getAvgPrice());
						if(pos.getPos()<0 ){							
							double exitPrice = pos.getAvgPrice() - actualDelta;
			//				submitExitOrder("BUY",(int)(pos.getPos()/2),exitPrice,);	
						}
						
					}
				}
				Thread.sleep(1000);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	
	

    public void submitExitOrder(String action, double quantity, double exitPrice,SubmitedOrder fbOrder) {

			int parentOrderId = api.getNextOrderId();
			Order exitOrder = new Order();
			exitOrder.orderId(parentOrderId);
			exitOrder.action(action);	        
			exitOrder.totalQuantity(quantity);	        
			exitOrder.orderType("LMT");
			exitOrder.lmtPrice(exitPrice);
			exitOrder.tif("GTD");
			exitOrder.allOrNone(true);
			exitOrder.transmit(true);
//			fbOrder.setOrderId(parentOrderId);
			api.placeOrModifyOrder(Util.getContract(ticker), exitOrder, new OrderHandler());
    }
	
    
	private void closeOrder(String action,int quantity){
		if(lastClose!=0 && System.currentTimeMillis()-lastClose<60000){
			return;
		}
		Order closeOrder = new Order();
		closeOrder.orderId(api.getNextOrderId());
		closeOrder.action(action.equals("BUY") ? "BUY" : "SELL");
		closeOrder.orderType("MKT");
		closeOrder.totalQuantity(Math.abs(quantity));
		cancelStp();
		closeOrder.transmit(true);
		api.placeOrModifyOrder(Util.getContract(ticker), closeOrder, new OrderHandler());
		System.out.println("close for orderQty " + quantity + "order sent ...");
		lastClose = System.currentTimeMillis();

	}
	

	public void updateStp(List<FBBar> barList) {
		MiniBar barB = barList.get(barList.size()-1).getHkaBar();
		double stpLoss=0;
		if(pos.getPos()>0){
			stpLoss = barB.low();
			for(int i=barList.size()-1; i>0;i--){
				if(stpLoss > barList.get(i-1).getHkaBar().low()){
					stpLoss = barList.get(i-1).getHkaBar().low();
					break;
				}
			}
		}else if(pos.getPos()<0){
			stpLoss = barB.high();
			for(int i=barList.size()-1; i>0;i--){
				if(stpLoss < barList.get(i-1).getHkaBar().high()){
					stpLoss = barList.get(i-1).getHkaBar().high();
					break;
				}
			}
		}
		stpLoss = Util.roundTo2D(stpLoss);
		for(Map.Entry<Integer, Order>  entry : stpOrder.entrySet()){
			Order order = entry.getValue();
			if((pos.getPos()>0 && stpLoss > order.auxPrice()) || (pos.getPos()<0 && stpLoss < order.auxPrice())){
				System.out.println("orderId " + order.orderId() + " moving STP from " + order.auxPrice() + " To " + stpLoss);
				order.auxPrice(stpLoss);
				api.placeOrModifyOrder(Util.getContract(ticker), order, new OrderHandler());
			}
		}
	}
	
	public void updateStp(Order order, double stpLoss) {

		if(stpLoss != order.auxPrice()){
			System.out.println("orderId " + order.orderId() + " moving STP from " + order.auxPrice() + " To " + stpLoss);
			order.auxPrice(stpLoss);
			api.placeOrModifyOrder(Util.getContract(ticker), order, new OrderHandler());
		}

	}
	

	
	private void cancelStp(){
//		if(stpOrder!=null && stpOrder.orderId()>0){
//			api.cancelOrder(stpOrder.orderId());
//		}
	}
	
	public synchronized void setDirection(Direction dir){
		this.dir=dir;
	}

	public void setPos(Portofolio pos) {
		this.pos = pos;
	}



	public void setAsk(double ask) {
		this.ask = ask;
	}


	public void setBid(double bid) {
		this.bid = bid;
	}

	public void setStpOrder(Order order){
		this.stpOrder.put(order.orderId(), order);
	}
	
	public void removeStpOrder(Integer orderId){
		if(stpOrder.containsKey(orderId)){
			stpOrder.remove(orderId);
		}
	}

	public void addParentId(int parentId){
		parentIds.add(parentId);
	}

	public void addStpLoss(Long parentId,double price){
		stpLossPrices.put(parentId, price);
	}

}