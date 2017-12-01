package com.sesnu.handler;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController.IHistoricalDataHandler;
import com.sesnu.model.CandleType;
import com.sesnu.model.FBBar;
import com.sesnu.model.MiniBar;
import com.sesnu.service.Indicators;
import com.sesnu.service.Processor;
import com.sesnu.service.Util;
import com.sesnu.ui.ChartController;
import com.sesnu.ui.RenkoChart;
import com.ib.controller.Bar;

public class HistoricalHandler implements IHistoricalDataHandler {

	private static final Logger liveBarLog = LoggerFactory.getLogger("BarLiveLog");
	
	
	private Processor pr;
	private Bar barA;
	private String ticker;
	private Util util;
	
	public HistoricalHandler(Processor pr,String ticker) {
		this.pr = pr;
		this.ticker=ticker;
		this.util=new Util();
	}

	@Override
	public void historicalData(Bar bar) {
		if(barA!=null && bar.time()!=barA.time()){
			FBBar fbBar =new FBBar(barA.time()*1000,0l,barA.low(),barA.high(),0,
					barA.volume(),barA.open(),barA.close(),ticker,0);			
			pr.addHistBarList(fbBar);
			if(util.isDevMode()){
				pr.updateLive(fbBar);
			}
		}else{
			FBBar fbBar =new FBBar(bar.time()*1000,0l,bar.low(),bar.high(),0,
					bar.volume(),bar.open(),bar.close(),ticker,0);	
			pr.updateLive(fbBar);
			liveBarLog.info("{}, {}", ticker,bar.toString());
		}		
		barA = bar;
	}

	@Override
	public void historicalDataEnd() {

	}


	
}
