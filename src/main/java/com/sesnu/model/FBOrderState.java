package com.sesnu.model;

import com.ib.client.Order;
import com.ib.client.OrderState;

public class FBOrderState {

	private final Order order;
	private final OrderState orderState;
	
	public FBOrderState(Order order, OrderState orderState) {
		this.order = order;
		this.orderState = orderState;
	}

	public Order getOrder() {
		return order;
	}

	public OrderState getOrderState() {
		return orderState;
	}
	
	
	
	
}
