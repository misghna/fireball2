package com.sesnu.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.ib.controller.Bar;
import com.sesnu.service.CandleSubType;
import com.sesnu.service.Util;
public class FBBar implements Serializable{
	
	private long startTime;
	private long endTime;
	private double low;
	private double high;
	private double avg;
	private long volume;
	private double open;
	private double close;
	private String ticker;
	private CandlePatternType candleType;
	private CandleSubType candleSubType;
	private double height;
	private int count;
	private double bodyHeight;
	private double upperBollinger;
	private double lowerBollinger;
	private double dayOpen;
	private int bollingerBarCount;
	private double sma;
	private double support;    //in 20
	private double resistance; // in 20
	private double trend; // in 20
	private double stcPerK; // stochastic % K
	private double stcPerD; // stochastic % D
	private double momentum;
	private String recom;
	private double bolTan;	//  bollinger tangency percentage
	private MiniBar hkaBar;
	private double tangency;
	private double emaFast;
	private double ema20;
	private double emaMedium;
	private double emaSlow;
	private double emaSlowChange;
	private double emaSlope;
	private double noiseLevel;

	public FBBar(long startTime, long endTime, double min, double max,
			double avg, long volume, double startPrice,double endPrice,
			String ticker,int count) {
		this.count=count;
		this.startTime = startTime;
		this.endTime = endTime;
		this.low = min;
		this.high = max;
		this.avg = avg;
		this.volume = volume;
		this.open = startPrice;
		this.close = endPrice;
		this.ticker=ticker;
		this.height=Util.roundTo2D(max-min);
		
//		if(Math.abs(endPrice-startPrice)<0.01) this.candleType = CandleTypes.DOJI;
		if(endPrice==startPrice) this.candleType = CandlePatternType.DOJI;
		
		else if(endPrice>startPrice) this.candleType = CandlePatternType.BULLISH;
		
		else if(endPrice<startPrice) this.candleType = CandlePatternType.BEARISH;

		this.bodyHeight= Math.abs(startPrice-endPrice);
	}

	
	public FBBar(String ticker,int interval, String[] data) {
		this.count=0;
		this.avg = 0;
		
		this.startTime = Long.parseLong(data[0]);
		this.endTime = startTime + interval*60000;
		this.open = Double.parseDouble(data[4]);
		this.high = Double.parseDouble(data[2]);;	
		this.low = Double.parseDouble(data[3]);;
		this.close = Double.parseDouble(data[1]);;	
		this.volume = Long.parseLong(data[5]);;
		this.ticker=ticker;
		this.height=Util.roundTo2D(high-low);
		
		if(Math.abs(close-open)<0.01) this.candleType = CandlePatternType.DOJI;
		else if(close>open) this.candleType = CandlePatternType.BULLISH;
		else if(close<open) this.candleType = CandlePatternType.BEARISH;

		this.bodyHeight= Math.abs(open-close);
	}

	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public double low() {
		return low;
	}
	public void setLow(double min) {
		this.low = min;
	}
	public double high() {
		return high;
	}
	public void setHigh(double max) {
		this.high = max;
	}
	public double getAvg() {
		return avg;
	}
	public void setAvg(double avg) {
		this.avg = avg;
	}
	public long getVolume() {
		return volume;
	}
	public void setVolume(long volume) {
		this.volume = volume;
	}
	public double open() {
		return open;
	}
	public void setOpen(double startPrice) {
		this.open = startPrice;
	}
	public double close() {
		return close;
	}
	public void setClose(double endPrice) {
		this.close = endPrice;
	}
	
	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}
	
	

	public void setHkaBar(MiniBar hkaBar) {
		this.hkaBar = hkaBar;
	}


	public MiniBar getHkaBar() {
		return hkaBar;
	}


	public CandlePatternType getCandleType() {
		return candleType;
	}


	public void setCandleType(CandlePatternType candleType) {
		this.candleType = candleType;
	}


	public CandleSubType getCandleSubType() {
		return candleSubType;
	}


	public void setCandleSubType(CandleSubType candleSubType) {
		this.candleSubType = candleSubType;
	}


	public double getHeight() {
		return height;
	}


	public void setHeight(double height) {
		this.height = height;
	}

	

	public int getCount() {
		return count;
	}


	public void setCount(int count) {
		this.count = count;
	}


	public double getBodyHeight() {
		return bodyHeight;
	}


	public void setBodyHeight(double body) {
		this.bodyHeight = body;
	}


	public int getBollingerBarCount() {
		return bollingerBarCount;
	}


	public void setBollingerBarCount(int bollingerBarCount) {
		this.bollingerBarCount = bollingerBarCount;
	}


	public double getUpperBollinger() {
		return upperBollinger;
	}


	public void setUpperBollinger(double upperBollinger) {
		this.upperBollinger = upperBollinger;
	}
	
	public double getLowerBollinger() {
		return lowerBollinger;
	}


	public void setLowerBollinger(double lowerBollinger) {
		this.lowerBollinger = lowerBollinger;
	}


	public double getDayOpen() {
		return dayOpen;
	}


	public void setDayOpen(double dayOpen) {
		this.dayOpen = dayOpen;
	}


	
	public double getSma() {
		return sma;
	}


	public void setSma(double sma) {
		this.sma = sma;
	}


	public double getTrend() {
		return trend;
	}


	public void setTrend(double trend) {
		this.trend = trend;
	}

	

	public double getSupport() {
		return support;
	}


	public void setSupport(double support) {
		this.support = support;
	}


	public double getResistance() {
		return resistance;
	}


	public void setResistance(double resistance) {
		this.resistance = resistance;
	}

	
	
	public double getStcPerK() {
		return stcPerK;
	}


	public void setStcPerK(double stcPerK) {
		this.stcPerK = stcPerK;
	}


	public double getStcPerD() {
		return stcPerD;
	}


	public void setStcPerD(double stcPerD) {
		this.stcPerD = stcPerD;
	}



	public String getRecom() {
		return recom;
	}


	public void setRecom(String recom) {
		this.recom = recom;
	}


	public double getBolTan() {
		return bolTan;
	}


	public void setBolTan(double bolTan) {
		this.bolTan = bolTan;
	}

	

	public double getMomentum() {
		return momentum;
	}


	public void setMomentum(double momentum) {
		this.momentum = momentum;
	}

	

	public double getTangency() {
		return tangency;
	}


	public void setTangency(double tangency) {
		this.tangency = tangency;
	}

	
	

	public double getEmaFast() {
		return emaFast;
	}


	public void setEmaFast(double emaFast) {
		this.emaFast = emaFast;
	}


	public double getEmaMedium() {
		return emaMedium;
	}


	public void setEmaMedium(double emaMedium) {
		this.emaMedium = emaMedium;
	}


	public double getEmaSlow() {
		return emaSlow;
	}


	public void setEmaSlow(double emaSlow) {
		this.emaSlow = emaSlow;
	}

	

	public double getEmaSlope() {
		return emaSlope;
	}


	public void setEmaSlope(double emaSlope) {
		this.emaSlope = emaSlope;
	}


	public double getLow() {
		return low;
	}


	public double getHigh() {
		return high;
	}


	public double getOpen() {
		return open;
	}


	public double getClose() {
		return close;
	}


	public double getEmaSlowChange() {
		return emaSlowChange;
	}


	public void setEmaSlowChange(double emaSlowChange) {
		this.emaSlowChange = emaSlowChange;
	}


	public double getNoiseLevel() {
		return noiseLevel;
	}


	public void setNoiseLevel(double noiseLevel) {
		this.noiseLevel = noiseLevel;
	}


	public double getEma20() {
		return ema20;
	}


	public void setEma20(double ema20) {
		this.ema20 = ema20;
	}


	public String toString(){
		StringBuilder str = new StringBuilder();
		str.append(" ticker " + "-" + this.ticker);
		str.append(" starttime " + "-" + this.startTime);		
		str.append(" min " + "-" + this.low);
		str.append(" max " + "-" + this.high);
		str.append(" height " + "-" + this.height);
		str.append(" volume " + "-" + this.volume);
		str.append(" startPrice " + "-" + this.open);
		str.append(" endPrice " + "-" + this.close);
		str.append(" avg " + "-" + this.avg);
		str.append(" direction " + "-" + this.candleType.name());
		str.append(" count " + "-" + this.count);
		str.append(" bollingerBarCount " + "-" + this.bollingerBarCount);
		str.append(" upperBollinger " + "-" + this.getUpperBollinger());
		str.append(" lowerBollinger " + "-" + this.getLowerBollinger());
		str.append(" stcPerK " + "-" + this.getStcPerK());
		str.append(" stcPerD " + "-" + this.getStcPerD());
		return str.toString();
	}
	
	public String toCSV(){
		StringBuilder str = new StringBuilder();
		str.append(this.ticker + ",");
		str.append(this.startTime + ",");		
		str.append(this.open + ",");	
		str.append(this.high + ",");
		str.append(this.low + ",");
		str.append(this.close + ",");
		str.append(this.volume + ",");
		str.append(this.height + ",");
		str.append(this.avg + ",");
		str.append(this.candleType.name() +",");
		str.append(this.count +",");
		str.append(this.bollingerBarCount +",");
		str.append(this.upperBollinger +",");
		str.append(this.lowerBollinger +",");
		str.append(this.sma +",");
		str.append(this.dayOpen +",");
		str.append(this.trend +",");
		str.append(this.support +",");
		str.append(this.resistance +",");
		str.append(this.stcPerK +",");
		str.append(this.stcPerD +",");
		str.append(this.recom+",");
//		str.append(this.getHkaBar().open() + ",");	
//		str.append(this.getHkaBar().high() + ",");
//		str.append(this.getHkaBar().low() + ",");
//		str.append(this.getHkaBar().close());
		str.append(this.getEmaFast() + ",");	
		str.append(this.getEmaMedium() + ",");
		str.append(this.getEmaSlow());
		
		return str.toString();
	}
	


	public FBBar (String barCsv){
		try{
			String[] barC = barCsv.split(",");
			this.ticker=barC[0];	
//			if(this.ticker.startsWith("US")){
//				SimpleDateFormat dateFormatter = new SimpleDateFormat("d/M/y h:m:s");
//				dateFormatter.setTimeZone(TimeZone.getTimeZone("EST"));
//				Date d = dateFormatter.parse(barC[2] + " " +  barC[3]);
//				this.startTime = d.getTime();
//				this.open = Double.parseDouble(barC[4]);		
//				this.high = Double.parseDouble(barC[5]);
//				this.low = Double.parseDouble(barC[6]);	
//				this.close = Double.parseDouble(barC[7]);
//				this.volume = Long.parseLong(barC[8]);
//			}else{
				this.startTime = Long.parseLong(barC[1]);
				this.open = Double.parseDouble(barC[2]);		
				this.high = Double.parseDouble(barC[3]);
				this.low = Double.parseDouble(barC[4]);	
				this.close = Double.parseDouble(barC[5]);
				this.volume = Long.parseLong(barC[6]);
//			}
			this.height=Util.roundTo2D(high-low);
			
			if(close==open) this.candleType = CandlePatternType.DOJI;
			
			else if(close>open) this.candleType = CandlePatternType.BULLISH;
			
			else if(close<open) this.candleType = CandlePatternType.BEARISH;

			this.bodyHeight= Math.abs(open-close);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	//1/2/2015  9:00:00 AM  Symbol
	
	
	public FBBar (String barCsv,String type){
		try{
			String[] barC = barCsv.split(",");
			this.ticker=barC[0];	
			
			if(type.equals("eoddata")){
				SimpleDateFormat dateFormatter = new SimpleDateFormat("d-MMMM-yyyy h:m");
				dateFormatter.setTimeZone(TimeZone.getTimeZone("EST"));
				Date d = dateFormatter.parse(barC[1]);
				this.startTime = d.getTime();
				this.open = Double.parseDouble(barC[2]);		
				this.high = Double.parseDouble(barC[3]);
				this.low = Double.parseDouble(barC[4]);	
				this.close = Double.parseDouble(barC[5]);
				this.volume = Long.parseLong(barC[6]);
			}
			this.height=Util.roundTo2D(high-low);
			
			if(close==open) this.candleType = CandlePatternType.DOJI;
			
			else if(close>open) this.candleType = CandlePatternType.BULLISH;
			
			else if(close<open) this.candleType = CandlePatternType.BEARISH;

			this.bodyHeight= Math.abs(open-close);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
