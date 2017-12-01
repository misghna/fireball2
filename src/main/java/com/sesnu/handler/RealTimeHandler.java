package com.sesnu.handler;

import com.ib.controller.ApiController.IRealTimeBarHandler;
import com.sesnu.service.OrderExit;
import com.sesnu.service.Processor;
import com.ib.client.Types.WhatToShow;
import com.ib.controller.Bar;

public class RealTimeHandler implements IRealTimeBarHandler {

	private WhatToShow wts;
	private Processor pr;
	private OrderExit exitOrder;
	
	
	
	public RealTimeHandler(WhatToShow wts,Processor pr,OrderExit exitOrder) {
		this.wts=wts;
		this.pr=pr;
		this.exitOrder=exitOrder;
	}



	@Override
	public void realtimeBar(Bar bar) {
		if(wts.equals(WhatToShow.ASK)){
//			pr.setAsk(bar.close());
			exitOrder.setAsk(bar.close());
		}else if(wts.equals(WhatToShow.BID)){
//			pr.setBid(bar.open());
			exitOrder.setAsk(bar.open());
		}
	
	}

}
