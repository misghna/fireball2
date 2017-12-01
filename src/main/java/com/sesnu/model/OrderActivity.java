package com.sesnu.model;

import com.ib.client.OrderStatus;

public class OrderActivity {

	private final int parentId;
	private final int orderId;
	private final long time;
	private final OrderStatus status;
	private final double currentPosition;
	private final double avgFillPrice;
	
	
	public OrderActivity(int parentId, int orderId, long time, OrderStatus status, double currentPosition, double avgFillPrice) {
		this.parentId = parentId;
		this.orderId = orderId;
		this.time = time;
		this.status = status;
		this.currentPosition = currentPosition;
		this.avgFillPrice = avgFillPrice;
	}


	public int getParentId() {
		return parentId;
	}


	public int getOrderId() {
		return orderId;
	}


	public long getTime() {
		return time;
	}


	public OrderStatus getStatus() {
		return status;
	}


	public double getCurrentPosition() {
		return currentPosition;
	}


	public double getAvgFillPrice() {
		return avgFillPrice;
	}
	
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("parentId " + parentId + ", ");
		sb.append("orderId " + orderId + ", ");
		sb.append("time " + time + ", ");
		sb.append("status " + status.name() + ", ");
		sb.append("currentPosition " + currentPosition + ", ");
		sb.append("avgFillPrice " + avgFillPrice + ", ");
		
		return sb.toString();
	}
	
	
	
	
}
