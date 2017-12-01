package com.sesnu.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.OrderType;
import com.ib.client.Types.Action;
import com.ib.controller.ApiController;
import com.sesnu.handler.OrderHandler;
import com.sesnu.model.CandleType;
import com.sesnu.model.FBBar;
import com.sesnu.model.FBOrderState;
import com.sesnu.model.FBPosition;
import com.sesnu.model.MiniBar;
import com.sesnu.model.Strategy;
import com.sesnu.model.SubmitedOrder;
import com.sesnu.model.Trade;
import com.sesnu.ui.ChartController;
import com.sesnu.ui.ChartViewer;
import com.sesnu.ui.RenkoChart;

public class Processor_old {

	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	private static final Logger Bar1minLog = LoggerFactory.getLogger("Bar5MinLog");

	private String ticker;
	private double noiseLevel;
	
	private OrderEntry orderEntry;

	private OrderHandler orderHandler;
	private List<MiniBar> renkoList;
	private Indicators indicator;
	private RenkoChart renkoChart;
	private DAOService daoService;
	private SubmitedOrder submitedOrder;
	private OrderState orderState;
	private List<FBBar> fbBarList;
	private boolean freezed;
	private boolean analyzed;
//	private Util util;
	private long launchTime=0;
	private FBPosition pos;
	private Portofolio portofolio;
	private ApiController api;
	private int lastParentId=0;
	private boolean crossedBreakEven;
	private double maxProfit=0;
	private ChartController chartController;
	private double totalUsedCashAmount;
	private long lastMainOrder;
	private long lastCloseOrder;
	private double maxLossPerTicker;
	private double maxNoiseDollarValue;
	private double instLossLimit;
	private double gapDown;
	private double entryEma;
	private Strategy strategy;
	
	public Processor_old(ApiController api,String ticker,
					double boxSize,DAOService daoService){
		this.api=api;
		this.ticker=ticker;
		this.noiseLevel=boxSize;
		this.pos = new FBPosition(0,0d);
		orderHandler = new OrderHandler();
		orderEntry = new OrderEntry(ticker,api,orderHandler);
		renkoList = new ArrayList<MiniBar>();
		this.indicator = new Indicators();
		this.renkoChart = new RenkoChart(ticker,boxSize);
		this.fbBarList = new ArrayList<FBBar>();
		this.daoService=daoService;
//		new Thread(renkoChart).start();		
		maxLossPerTicker = Util.getDouble("maxLossPerTicker");
		maxNoiseDollarValue = Util.getInt("maxNoiseDollarValue");
		instLossLimit = Util.getDouble("instLossLimit");
		
		launchTime = System.currentTimeMillis();
		
		initChart();
	}
	
	private void initChart(){
		if(Util.getBoolean("chartEnabled")){
			chartController = new ChartController(ticker);
			ChartViewer chartViewer = new ChartViewer(chartController,ticker);
			new Thread(chartViewer).start();
		}
	}
	
	public void setOrderStatus(FBOrderState orderStatus){
		if(orderStatus.getOrder().orderId()==lastParentId){
			this.orderState = orderStatus.getOrderState();
		}else if(orderStatus.getOrder().getOrderType().equals(OrderType.STP)){
//			renkoChart.insertStpPrice(orderStatus.getOrder().auxPrice());
		}

	}
	
	
	public void addHistBarList(FBBar bar) {
			
		FBBar richBar = indicator.addIndicators(fbBarList, bar);
		calcGapDown();
		Bar1minLog.info(richBar.toCSV());
		fbBarList.add(richBar);
		if(Util.getBoolean("dynamicNoise")){
			noiseLevel = richBar.getNoiseLevel();
		}
		if(Util.getBoolean("chartEnabled")){
			chartController.updateCandle(bar, CandleType.Regular,noiseLevel);
		}
	}

	
	private int lastIndx=0;
	private boolean lastValue;
	private boolean crossed(){
		if(fbBarList.size()==lastIndx)return lastValue;
		FBBar lastBar = fbBarList.get(fbBarList.size()-1);
		boolean longTrade = lastBar.getEmaFast()>lastBar.getEmaSlow();
		if(longTrade &&  (!(lastBar.getEmaFast()>lastBar.getEmaSlow()) || !(lastBar.getEmaSlow() > lastBar.getEmaMedium()))){
			return false;
		}else if(!longTrade &&  (!(lastBar.getEmaSlow()>lastBar.getEmaFast()) || !(lastBar.getEmaMedium()>lastBar.getEmaSlow()))){
			return false;
		}
		int idx=-1;boolean rightCross=true;
		for (int i=fbBarList.size()-2; i>=0;i--) {
			FBBar cBar = fbBarList.get(i);
			if(longTrade && cBar.getEmaFast()< cBar.getEmaSlow() - noiseLevel){ //Long
				idx =i;
				break;
			}else if(!longTrade && cBar.getEmaFast()>cBar.getEmaSlow() + noiseLevel){ //Short
				idx =i;
				break;
			}
		}
		if(idx==-1)return false;
		
		for (int i=fbBarList.size()-2; i>=idx;i--) {
			FBBar cBar = fbBarList.get(i);
			if(longTrade && cBar.close() > cBar.getEmaSlow() + noiseLevel){
				rightCross = false;
				break;
			}else if(!longTrade && cBar.close() < cBar.getEmaSlow() - noiseLevel){
				rightCross = false;
				break;
			}
		}
		lastIndx = fbBarList.size();
		lastValue = rightCross;
		return rightCross;
	}
	
	private int lastOpenOrderId;
	private boolean endOfDayClosed;
	private int rejectedIndx=0;
	
	public synchronized void updateLive(FBBar bar) {
	//	System.out.println(fbBarList.size());
		
		
		if(fbBarList.size()<2)return;
		FBBar fbBar = fbBarList.get(fbBarList.size()-1);
		if(Util.getDate(bar.getStartTime())!= Util.getDate(System.currentTimeMillis()))return;
		
		if(pos.getShares()==0 && checkFusion(bar) && bar.getStartTime()-lastMainOrder > 300000 && !freezed){
			maxProfit =0;
			crossedBreakEven = false;
			int shares = calcShares(bar.close(),bar);
			SubmitedOrder submitedOrder = orderEntry.placeMktOrder(Action.SELL,shares);
			lastMainOrder = bar.getStartTime();
			if(submitedOrder!=null){
				strategy = Strategy.FUSSION;
//				entryEma = 
				Integer orderId = submitedOrder.getOrder().orderId();
				lastOpenOrderId = orderId;
				Trade trade = new Trade(orderId,ticker, bar.getStartTime(), "SELL", "Open",
						bar.close(),shares,noiseLevel,orderId,"new Entry","FUSION");
				daoService.save(trade);
				addSimPosition(shares,bar.close(),Action.SELL);
				mainL.info("{} ~ New order submited, orderId:{}, Action: {}, shares: {},ema: {}, priceWhenPlaced:{}, slope% :{}, slopechange%: {}, time: {}",
						ticker,orderId, "SELL",shares,fbBar.getEmaSlow(),bar.close(),Util.roundTo2D(fbBar.getEmaSlope()),
						Util.roundTo2D(fbBar.getEmaSlowChange()),Util.getDoubleTime(bar.getStartTime()));
			}
		}else if(fbBarList.size()>100 && fbBarList.get(fbBarList.size()-1).getEmaSlow()!=-1 && !freezed &&
				pos.getShares()==0 && (Util.isDevMode() || Util.getDoubleTime() < 15) && bar.getStartTime()-lastMainOrder > 300000){		
			crossedBreakEven = false;
			maxProfit =0;
			double noiseBear = fbBar.getEmaSlow() - noiseLevel;
			double noiseBull = fbBar.getEmaSlow() + noiseLevel;
			double halfBearBufferPrice = fbBar.getEmaSlow() - noiseLevel*0.5;
			double halfBullBufferPrice = fbBar.getEmaSlow() + noiseLevel*0.5;
			Action action=null;

//		if(Util.getDate(bar.getStartTime())==14 && Util.getDoubleTime(bar.getStartTime())>=12.15){
			if(bar.close() <= halfBearBufferPrice && bar.close() > noiseBear && crossed()){	// && bar.low() > bearBufferPrice short
				System.out.println("SHORT Time -> " + Util.getDateTime(bar.getStartTime()) + " slope: " + fbBar.getEmaSlope());
				action = Action.SELL;			
			}else if(bar.close() >= halfBullBufferPrice && bar.close() < noiseBull && crossed()){ // && bar.high() < bullBufferPricelong
				System.out.println("LONG Date -> " + Util.getDateTime(bar.getStartTime()) + " slope: " + fbBar.getEmaSlope());
				action = Action.BUY;			
			}
//		}
			int shares = calcShares(bar.close(),bar);
			if(action !=null){				
				if(fbBar.getEmaSlowChange()!=-1 && fbBar.getEmaSlowChange()<0.11){
					rejectedIndx = fbBarList.size();
//					mainL.debug("{} ~ Entry rejected because emaSlope ({}) is too small, market is moving side ways at {}/{}",
//							ticker,Util.roundTo2D(fbBar.getEmaSlowChange()),
//							Util.getDate(bar.getStartTime()),Util.getDoubleTime(bar.getStartTime()));
				}else if(shares < 50){
					rejectedIndx = fbBarList.size();
//					mainL.debug("{} ~ viable Entry at time {} rejected because of insuficient funds, Dollar Used: {}",
//							ticker,Util.getDoubleTime(),Util.roundTo2D(totalUsedCashAmount));
				}else if(rejectedIndx != fbBarList.size()){
					SubmitedOrder submitedOrder = orderEntry.placeMktOrder(action,shares);
					lastMainOrder = bar.getStartTime();
					if(submitedOrder!=null){	
						strategy = Strategy.BLUE_MOON;
						Integer orderId = submitedOrder.getOrder().orderId();
						lastOpenOrderId = orderId;
						Trade trade = new Trade(orderId,ticker, bar.getStartTime(), action.name(), "Open",
								bar.close(),shares,noiseLevel,orderId,"new Entry","BLUE-MOON");
						daoService.save(trade);
						addSimPosition(shares,bar.close(),action);
						mainL.info("{} ~ New order submited, orderId:{}, Action: {}, shares: {},ema: {}, priceWhenPlaced:{}, slope% :{}, slopechange%: {}, time: {}",
								ticker,orderId, action.name(),shares,fbBar.getEmaSlow(),bar.close(),Util.roundTo2D(fbBar.getEmaSlope()),
										Util.roundTo2D(fbBar.getEmaSlowChange()),Util.getDoubleTime(bar.getStartTime()));
					}
				}
			}
	    	
		}else if (pos.getShares()!=0){
//			double entryRef= entryEma==0?pos.getAvgCost():entryEma;
			double instProfit = Util.roundTo2D((bar.close() - pos.getAvgCost())*pos.getShares());
			maxProfit = maxProfit > instProfit?maxProfit:instProfit;
			double breakEvenDelta = Math.abs(noiseLevel * Util.getDouble("breakEvenLineMultiplier") * pos.getShares());
		
			boolean blackJack = (maxProfit > 400);
			boolean exitLoss = false;
			if(pos.getShares()>0){
				exitLoss =  bar.close() < (fbBar.getEmaSlow() - noiseLevel);
			}else{
				exitLoss = bar.close()  > (fbBar.getEmaSlow() + noiseLevel);
			}
			
			
			if(instProfit >= breakEvenDelta && !crossedBreakEven){
				crossedBreakEven=true;			
			}else if(bar.getStartTime()-lastCloseOrder > 300000 && ((crossedBreakEven && maxProfit-instProfit >= noiseLevel* pos.getShares()) || blackJack)){
				lastCloseOrder = bar.getStartTime();
				String closeReason = "";
				if(blackJack)closeReason = "Profit reached beyond limit of $" + maxProfit;
				else closeReason = "profit fallback fullnoise from more than 3X noise, maxProfit:" + maxProfit;
				mainL.info("{} ~ closing at a profit of ${} because {} from {} to {} and profit dropped from {} to {}",
						ticker,instProfit,closeReason,pos.getAvgCost(),bar.close(),maxProfit,instProfit);
				Order submitedOrder = orderEntry.closePosition(pos);
				if(submitedOrder!=null){
					String action = pos.getShares()>0? "SELL":"BUY";
					Trade trade = new Trade(submitedOrder.orderId(),ticker, bar.getStartTime(), 
										action, "Close", bar.close(),pos.getShares(),noiseLevel,lastOpenOrderId,closeReason,"UNIV-CLOSE");
					daoService.save(trade);
					removeSimPosition();
				}		
				
			}else if (bar.getStartTime()-lastCloseOrder > 300000 && (freezed || exitLoss )){
				lastCloseOrder = bar.getStartTime();
				String closeReason="";
				if(freezed){
					mainL.info("{} ~ closing because ticker reached maxloss per day ({})",
							ticker,maxLossPerTicker);
					closeReason = "reached max loss per day, maxprofit:" + maxProfit;
				}else {
					mainL.info("{} ~ closing at a loss/profit of ${} because price crossed beyond the limit, price moved from {} to {} and profit change moved from {} to {}",
						ticker,instProfit,pos.getAvgCost(),bar.close(),maxProfit,instProfit);
					closeReason = "loss per trade passed beyond the limit maxProfit:"+ maxProfit;
				}
				Order submitedOrder = orderEntry.closePosition(pos);
				if(submitedOrder!=null){
					String action = pos.getShares()>0? "SELL":"BUY";
					Trade trade = new Trade(submitedOrder.orderId(),ticker, bar.getStartTime(),
										action, "Close", bar.close(),pos.getShares(),noiseLevel,lastOpenOrderId,closeReason,"UNIV-CLOSE");
					daoService.save(trade);
					removeSimPosition();
				}
			}else if(bar.getStartTime()-lastCloseOrder > 300000 && !endOfDayClosed && Util.getDoubleTime(bar.getStartTime())>15.45){
				lastCloseOrder = bar.getStartTime();
				mainL.info("{} ~ closing due to end of day, price moved from {} to {} and gain moved from {} to {}",
						ticker,pos.getAvgCost(),bar.close(),maxProfit,instProfit);
				String closeReason="end of day, maxProfit:" + maxProfit;
				Order submitedOrder = orderEntry.closePosition(pos);
				if(submitedOrder!=null){
					String action = pos.getShares()>0? "SELL":"BUY";
					Trade trade = new Trade(submitedOrder.orderId(),ticker, bar.getStartTime(),
											action, "Close", bar.close(),pos.getShares(),noiseLevel,lastOpenOrderId,closeReason,"UNIV-CLOSE");
					daoService.save(trade);
					endOfDayClosed = true;
					removeSimPosition();
				}
			}
		}		
	} 
	

	private boolean checkFusion(FBBar live){
		boolean goFusion=false;
		FBBar last = fbBarList.get(fbBarList.size()-1);
		if(Util.getDoubleTime(live.getStartTime()) >= 10.3) return false;
//		System.out.println(Util.getDoubleTime(live.getStartTime()));
		int count = (int) ((Util.getDoubleTime(live.getStartTime()) - 9.3d)*100);
		if(gapDown<-0.5 && gapDown>-10.5 && last.getEmaMedium() < last.getEmaSlow() && 
				live.close() < last.getEmaMedium() - noiseLevel / 2 &&
				live.close() > last.getEmaMedium() - noiseLevel ){
			for (int i= fbBarList.size()-1; i>=fbBarList.size()-count; i--) {
				FBBar iBar = fbBarList.get(i);
				if(iBar.close()>iBar.getEmaMedium()){
					goFusion = true;
					break;
				}
			}
		}else{
			return false;
		}
		return goFusion;
	}
	
	private void calcGapDown(){
		if(fbBarList.size()<2)return;
		FBBar yday = fbBarList.get(fbBarList.size()-2);
		FBBar openTday = fbBarList.get(fbBarList.size()-1);
		if(Util.getDate(yday.getStartTime()) != Util.getDate(openTday.getStartTime())){
			gapDown = Util.roundTo2D((openTday.getOpen()-yday.close())/openTday.getOpen()*100);
		}
	}
	
	private int calcShares(double tickerPrice,FBBar bar){
		int shares = (int) (maxNoiseDollarValue/noiseLevel);
		int maxShares = (int) (Util.getInt("maxAmoutPerTrade")/tickerPrice);
		shares = shares < maxShares? shares:maxShares;
		double availableFunds = Util.getDouble("maxCashFlow") * 0.95 - totalUsedCashAmount;
		shares = (int) (availableFunds < (shares * bar.close())? availableFunds/bar.close():shares);
		return shares;
	}


	public void updatePosition(FBPosition pos) {
		this.pos = pos;
//		renkoChart.insertInPrice(pos);
	}

	public void addSimPosition(int shares,double price,Action action){
		if(Util.getMode().equals("DEV-MODE")){
			shares = action.equals(Action.BUY)?shares:-1*shares;
			FBPosition pos = new FBPosition(shares,price);
			this.pos = pos;
		}
	}
	
	public void removeSimPosition(){
		if(Util.getMode().equals("DEV-MODE")){
			FBPosition pos = new FBPosition(0,0);
			this.pos = pos;
		}
	}
	
	public void updatePortofolio(Portofolio portofolio) {		
		this.portofolio=portofolio;		
//		double lossLimitPerTicker = Math.abs(pos.getShares()*noiseLevel*1.5*2.2);
		double lossLimitPerTicker = maxLossPerTicker; // < lossLimitPerTicker? maxLossPerTicker : lossLimitPerTicker;
		if(lossLimitPerTicker > 0 && !freezed && 
				(portofolio.getRealizedPnl() + portofolio.getUnrealizedPnl()) < -1 * lossLimitPerTicker){
			freezed=true;
			double loss = portofolio.getRealizedPnl() + portofolio.getUnrealizedPnl();
			mainL.info("{} ~ freezed because loss {} has crossed the daily limit loss",ticker,loss);
		}
		
	}

	private void analyse(List<MiniBar> renkoList){
		if(!analyzed && renkoList!=null){
			analyzed = true;
			int countCurve =0;
			double height = 0;
			for (int i =1; i< renkoList.size(); i++) {
				if(!renkoList.get(i-1).getCandleType().equals(renkoList.get(i).getCandleType())){
					countCurve++;
				}
				height +=renkoList.get(i).getBodyHeight();
			}
			height = height/renkoList.size();
			
			Double profitRatio = (renkoList.size()-2*countCurve) * height;
			mainL.info("Analysis - ticker: {} , profitable: {} noOfcurves: {}" , ticker, profitRatio.toString(),countCurve);
		}
	}
	
	
    
	public void updateTotalUsedAmount(double totalUsed){
		this.totalUsedCashAmount=totalUsed;
	}
}

