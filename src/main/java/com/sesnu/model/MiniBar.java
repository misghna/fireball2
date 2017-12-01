package com.sesnu.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sesnu.service.Direction;
import com.sesnu.service.Util;

public class MiniBar {

	private static final Logger barRenkoLog = LoggerFactory.getLogger("BarRenkoLog");
	
	private long time;
	private double open;
	private double close;
	private double low;
	private double high;
	private double height;
	private double bodyHeight;
	private CandlePatternType candleType;
	private Direction dir;
	private long vol;
	private String ticker;
	
	
	
	public MiniBar(double open, double close, double low, double high) {
		
		this.open = Util.roundTo3D(open);
		this.close = Util.roundTo3D(close);
		this.low = Util.roundTo3D(low);
		this.high = Util.roundTo3D(high);
		this.vol=vol;
		if(close==open) this.candleType = CandlePatternType.DOJI;		
		else if(close>open) this.candleType = CandlePatternType.BULLISH;	
		else if(close<open) this.candleType = CandlePatternType.BEARISH;
		
		this.bodyHeight = Util.roundTo3D(Math.abs(open-close));
		this.height = Util.roundTo3D(high-low);
		
	}
	
	
//	public MiniBar(long time,double high, double low,CandlePatternType candleType ) {
//		
//		if(candleType.equals(CandlePatternType.BEARISH)){
//			this.open = Util.roundTo3D(high);
//			this.close = Util.roundTo3D(low);
//		}else{
//			this.open = Util.roundTo3D(low);
//			this.close = Util.roundTo3D(high);
//		}
//		this.low = Util.roundTo3D(low);
//		this.high = Util.roundTo3D(high);
//		this.time=time;
//
//		this.candleType = candleType;
//		
//		this.bodyHeight = Util.roundTo3D(Math.abs(open-close));
//		this.height = Util.roundTo3D(high-low);
////		printRenko();
//	}


	public MiniBar(String ticker,long time,double open, double close,long vol) {
		
		if(open>close){
			this.high = Util.roundTo3D(open);
			this.low = Util.roundTo3D(close);
			this.candleType = CandlePatternType.BEARISH;
		}else{
			this.high = Util.roundTo3D(close);
			this.low = Util.roundTo3D(open);
			this.candleType = CandlePatternType.BULLISH;
		}
		this.vol=vol;
		this.ticker=ticker;
		
		this.open = Util.roundTo3D(open);
		this.close = Util.roundTo3D(close);
		this.time=time;
	
		this.bodyHeight = Util.roundTo3D(Math.abs(open-close));
		this.height = Util.roundTo3D(high-low);
		printRenko();
	}
	
	public double open() {
		return open;
	}


	public double close() {
		return close;
	}


	public double low() {
		return low;
	}


	public double high() {
		return high;
	}


	public CandlePatternType getCandleType() {
		return candleType;
	}


	public double getHeight() {
		return height;
	}


	public double getBodyHeight() {
		return bodyHeight;
	}


	public Direction getDir() {
		return dir;
	}


	public void setDir(Direction dir) {
		this.dir = dir;
	}
	
	
	public long getTime() {
		return time;
	}


	public void setTime(long time) {
		this.time = time;
	}


	public void printRenko(){
		String csv = ticker + "," + time + "," + open + "," + high + "," + low + "," + close + "," + vol + "," +
				height + "," + bodyHeight + "," + candleType ;
		
		barRenkoLog.info(csv);
	}
	
	public void printCSV(){
		String csv = open + "," + high + "," + low + "," + close + "," + 
				height + "," + bodyHeight + "," + candleType + "," + dir.name();
//		bar5Min.info(csv);
	}
	
	
	
}
