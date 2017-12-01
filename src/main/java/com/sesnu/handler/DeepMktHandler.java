package com.sesnu.handler;

import com.ib.client.TickAttr;
import com.ib.client.TickType;
import com.ib.client.Types.DeepSide;
import com.ib.client.Types.DeepType;
import com.ib.controller.ApiController.IDeepMktDataHandler;
import com.ib.controller.ApiController.ITopMktDataHandler;
import com.sesnu.service.Processor;

public class DeepMktHandler implements IDeepMktDataHandler {

	private Processor pr;
	private String exchange;
	
	public DeepMktHandler(Processor pr,String exchange) {
		this.pr = pr;
		this.exchange=exchange;
	}

	@Override
	public void updateMktDepth(int position, String marketMaker, DeepType operation, DeepSide side, double price,int size) {

//		System.out.println(exchange + " side" +  side.name() + "rowNO " + position +  " op " +  operation.name() + " size " + size + " " + price);
	}
	
	
}
