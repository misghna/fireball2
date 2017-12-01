package com.sesnu.handler;

import com.ib.client.TickAttr;
import com.ib.client.TickType;
import com.ib.controller.ApiController.ITopMktDataHandler;
import com.sesnu.service.Processor;

public class TopMktHandler implements ITopMktDataHandler {

	private Processor pr;
	
	public TopMktHandler(Processor pr) {
		this.pr = pr;
	}
	
	@Override
	public void tickPrice(TickType tickType, double price, TickAttr attribs) {
	
		if(tickType.name().equals(TickType.ASK)){
//			pr.setAsk(price);
			System.out.println("ask " + price);
		}else if(tickType.name().equals(TickType.BID)){
//			pr.setBid(price);
			System.out.println("bid " + price);
		}

	}

	@Override
	public void tickSize(TickType tickType, int size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickString(TickType tickType, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickSnapshotEnd() {
		// TODO Auto-generated method stub

	}

	@Override
	public void marketDataType(int marketDataType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {
		// TODO Auto-generated method stub

	}

}
