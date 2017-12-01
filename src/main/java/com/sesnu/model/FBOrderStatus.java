package com.sesnu.model;

import com.ib.client.OrderStatus;

public class FBOrderStatus {

	private final int orderId;
	private final int parentOrderId;
	private final int filledShares;
	private final double avgPrice;
	private final OrderStatus status;
	
	
	public FBOrderStatus(int parentOrderId,int orderId, int filledShares, double avgPrice, OrderStatus status) {
		this.parentOrderId=parentOrderId;
		this.orderId = orderId;
		this.filledShares = filledShares;
		this.avgPrice = avgPrice;
		this.status = status;
	}


	public int getParentOrderId() {
		return parentOrderId;
	}


	public int getOrderId() {
		return orderId;
	}


	public int getFilledShares() {
		return filledShares;
	}


	public double getAvgPrice() {
		return avgPrice;
	}


	public OrderStatus getStatus() {
		return status;
	}
	
	
	
	
	
}
