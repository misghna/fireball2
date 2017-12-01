package com.sesnu.model;

import com.ib.client.Order;

public class SubmitedOrder {
	
	private String ticker;
	private long time;
	private String type;	// Main, 1stcloseAtDelta, 2ndcloseAt1%, 3rdclose@30EMA
	private String orderType;  //Sell, Buy
	private Order order;
	private Order stpOrder;
	private int candleId;
	private boolean isFilled;
	
	
	public SubmitedOrder(String ticker, long time, String type) {
		
		this.ticker = ticker;
		this.time = time;
		this.type = type;
	}


	public String getTicker() {
		return ticker;
	}


	public void setTicker(String ticker) {
		this.ticker = ticker;
	}


	public long getTime() {
		return time;
	}


	public void setTime(long time) {
		this.time = time;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getOrderType() {
		return orderType;
	}


	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public Order getOrder() {
		return order;
	}


	public void setOrder(Order order) {
		this.order = order;
	}


	public Order getStpOrder() {
		return stpOrder;
	}


	public void setStpOrder(Order stpOrder) {
		this.stpOrder = stpOrder;
	}


	public int getCandleId() {
		return candleId;
	}


	public void setCandleId(int candleId) {
		this.candleId = candleId;
	}


	
	public void setFilled(boolean isFilled) {
		this.isFilled = isFilled;
	}


	public boolean isFilled() {
		return isFilled;
	}


	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("ticker " + ticker + ";");
		sb.append("time " + time + ";");
		sb.append("Origination type " + type + ";");
		sb.append("orderType " + orderType + ";");
		sb.append("orderPrice " + order.lmtPrice() + ";");
		sb.append("parentId " + order.parentId() + ";");
		sb.append("orderId " + order.orderId() + ";");
		if(stpOrder!=null)sb.append("stpId " + stpOrder.orderId() + ";");
		if(stpOrder!=null)sb.append("stopLossPrice " + stpOrder.auxPrice() + ";");
		sb.append("orderQty " + order.totalQuantity());
		return sb.toString();
	}
	

}
