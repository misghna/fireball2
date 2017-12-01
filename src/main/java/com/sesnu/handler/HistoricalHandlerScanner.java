package com.sesnu.handler;

import java.util.ArrayList;
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
import com.sesnu.ui.ChartController;
import com.sesnu.ui.RenkoChart;
import com.ib.controller.Bar;

public class HistoricalHandlerScanner implements IHistoricalDataHandler {

	private static final Logger liveBarLog = LoggerFactory.getLogger("BarLiveLog");

	private boolean done;
	private String ticker;
	private List<FBBar> barList;
	
	public HistoricalHandlerScanner(String ticker) {
		this.ticker=ticker;
		this.barList= new ArrayList<FBBar>();
	}

	@Override
	public void historicalData(Bar bar) {
			FBBar fbBar =new FBBar(bar.time()*1000,0l,bar.low(),bar.high(),0,
					bar.volume(),bar.open(),bar.close(),ticker,0);			
			barList.add(fbBar);
	}

	@Override
	public void historicalDataEnd() {
		this.done=true;
	}

	private boolean isDone(){
		return done;
	}
	
	private List<FBBar> getBarList(){
		return barList;
	}

	
}
