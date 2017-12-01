package com.sesnu.handler;

import com.ib.client.OrderState;
import com.ib.client.OrderStatus;
import com.ib.controller.ApiController.IOrderHandler;
import com.sesnu.service.Processor;

public class OrderHandler implements IOrderHandler {


	@Override
	public void orderState(OrderState orderState) {
		// TODO Auto-generated method stub
//		System.out.println("order status " + orderState.getStatus());
	}

	@Override
	public void orderStatus(OrderStatus status, double filled, double remaining, double avgFillPrice, long permId,
			int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
//		System.out.println("state " + status.name() + " clientId " + clientId + " parentId " + parentId);

	}

	@Override
	public void handle(int errorCode, String errorMsg) {
		// TODO Auto-generated method stub
		System.out.println("order handler " + errorMsg);

	}

}
