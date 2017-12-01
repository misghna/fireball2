package com.sesnu.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sesnu.model.CandlePatternType;
import com.sesnu.model.FBBar;
import com.sesnu.model.MiniBar;


public class Indicators {

//	private Util util;
	public Indicators(){
//		util = new Util();
	}
	
	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	
	public strictfp FBBar addIndicators(List<FBBar> allBarList, FBBar currentFBBar) {

		List<FBBar>  barList = new ArrayList<FBBar>();
		allBarList.add(currentFBBar);
    	if(allBarList.size()>20){
    		barList = allBarList.subList(allBarList.size()-20, allBarList.size());
		}else{
			barList=allBarList;
		}

        double mean = mean(barList);
        double stdv = populationStDV(barList,mean);
        double upperBollinger = mean + stdv*2;
        double lowerBollinger = mean - stdv*2;
        currentFBBar.setAvg(calcAvgVolume(barList));
        currentFBBar.setSma(mean);
        currentFBBar.setEmaFast(calcEma(allBarList,Util.getInt("emaFast"),"fast"));
        currentFBBar.setEma20(calcEma(allBarList,20,"20"));
        currentFBBar.setEmaMedium(calcEma(allBarList,Util.getInt("emaMedium"),"medium"));
        currentFBBar.setEmaSlow(calcEma(allBarList,Util.getInt("emaSlow"),"slow"));
        currentFBBar.setEmaSlowChange(calcEmaChange(allBarList,Util.getInt("emaSlow")));
        currentFBBar.setEmaSlope(calcEmaSlope(allBarList,Util.getInt("emaSlow"),Util.getInt("emaSlopeBarCount")));
        currentFBBar.setNoiseLevel(calcNoise(allBarList,Util.getInt("emaSlow")));
        currentFBBar.setTrend(calcTrend(barList));
        currentFBBar.setBollingerBarCount(barList.size());
        currentFBBar.setUpperBollinger(Util.roundTo2D(upperBollinger));
        currentFBBar.setLowerBollinger(Util.roundTo2D(lowerBollinger));
        currentFBBar.setDayOpen(barList.get(0).open());
        currentFBBar.setSupport(support(barList));
        currentFBBar.setResistance(resistance(barList));
        currentFBBar.setStcPerK(calcStochasticPerK(barList));
        currentFBBar.setStcPerD(calcStochasticPerD(barList,currentFBBar));
        currentFBBar.setBolTan(calcBolTang(currentFBBar));
        currentFBBar.setHkaBar(getHeikinBar(barList));
        currentFBBar.setMomentum(calcMomentum(barList));
        currentFBBar.setRecom(calcRecom(allBarList));
        currentFBBar.setTangency(calcTangency(currentFBBar));
        barList.remove(barList.size()-1);
        return currentFBBar;
        
	}
	
//	public synchronized strictfp List<MiniBar> addRenkoHL(List<MiniBar> renkoList, FBBar fBBar, double boxSize) {
//		if(renkoList==null)renkoList= new ArrayList<MiniBar>();
//		long time = fBBar.getStartTime();
//		if(renkoList.size()==0){
//			double highR = fBBar.high()%boxSize;
//			double lowR = fBBar.low()%boxSize;
//			if(highR<lowR){
//				double highVal = ((int)(fBBar.high()/boxSize)) * boxSize;
//				MiniBar renko = new MiniBar(time,highVal-boxSize,highVal,null);	
//				renkoList.add(renko);
//			}else{
//				double lowVal = ((int)(fBBar.low()/boxSize)) * boxSize;
//				MiniBar renko = new MiniBar(time,lowVal+boxSize,lowVal,null);		
//				renkoList.add(renko);
//			}
//		}else{
//
//			MiniBar renko = renkoList.get(renkoList.size()-1);
//			double bull = fBBar.close() - renko.high();
//			double bear = renko.low() - fBBar.close();
//			if(bull >=boxSize){				
//					int noOfRenko = (int) (bull/boxSize);
//					if(noOfRenko>60){
//						mainL.error("{} too big gap or too small boxsize {} BULL @ {}, noOfRenko in gapSize {}",fBBar.getTicker(),boxSize,time,bull);
//						return null;
//					}
//					addRenkoHL(renkoList,noOfRenko,boxSize,CandlePatternType.BULLISH,time);
//			}else if(bear >= boxSize){				
//					int noOfRenko = (int) (bear/boxSize);
//					if(noOfRenko>60){
//						mainL.error("{} too big gap or too small boxsize {} BEAR @ {}, noOfRenko in gapSize {}",fBBar.getTicker(),boxSize,time,bear);
//						return null;
//					}
//					addRenkoHL(renkoList,noOfRenko,boxSize,CandlePatternType.BEARISH,time);
//			}		
//			
//		}
//		return renkoList;
//	}
//
//	private synchronized strictfp List<MiniBar> addRenkoHL(List<MiniBar> renkoList,int noOfRenko,double boxSize,CandlePatternType candleType,long time){
//		long maxTime = getMaxTimeAssigned(renkoList,time);
//
//		for (int i = 0; i < noOfRenko; i++) {
//			long modTime  = maxTime > 0? (maxTime + (i+1)*1000):(time + i*1000);
//			 MiniBar renko = renkoList.get(renkoList.size()-1);
//
//			 if(candleType.equals(CandlePatternType.BULLISH)){
//				 MiniBar newRenko = new MiniBar(modTime,renko.high(),renko.high()+boxSize,null);
//				 renkoList.add(newRenko);
//			 }else{
//				 MiniBar newRenko = new MiniBar(modTime,renko.low(),renko.low()-boxSize,null);
//				 renkoList.add(newRenko);
//			 }		 
//		}
//		return renkoList;
//	}
	
	
	private double calcEmaSlope(List<FBBar> allBarList, int emaType, int barCount) {
		if(allBarList.size() < emaType + barCount)return -1;	// not enough data
		else{
			double firstValue = allBarList.get(allBarList.size()-barCount).getEmaSlow();
			double lastValue = allBarList.get(allBarList.size()-1).getEmaSlow();
			double slope = Util.roundTo2D((lastValue-firstValue)/firstValue*100);
			return slope;
		}
	}

	private double calcNoise(List<FBBar> allBarList,int noiseBarCount) {
		if(allBarList.size()<noiseBarCount)return -1;
		List<Double> heightList = new ArrayList<Double>();
		for(int i = allBarList.size()-1; i>=allBarList.size()-noiseBarCount; i--){
			heightList.add(allBarList.get(i).getHeight());
		}
		Collections.sort(heightList);
		int idx = (int) ((heightList.size()-1)*0.98);
		return Util.roundTo2D(heightList.get(idx));

	}

	private long getRenkoVolume(List<FBBar> barList, long time){
		long vol =0;
		if(barList==null)return 0;
		for (int i= barList.size()-1; i>=0; i--) {
			FBBar bar = barList.get(i);
			vol += bar.getVolume();
			if(bar.getStartTime()==time){
				break;
			}
		}
		return vol;
	}
	
	public synchronized strictfp List<MiniBar> addRenkoOC(List<MiniBar> renkoList, FBBar fBBar, double boxSize,List<FBBar> barList) {
		if(renkoList==null)renkoList= new ArrayList<MiniBar>();
		long time = fBBar.getStartTime();
		String ticker = barList!=null ?barList.get(0).getTicker():"";
		if(renkoList.size()==0){
			double highR = fBBar.high()%boxSize;
			double lowR = fBBar.low()%boxSize;
			if(highR<lowR){
				double highVal = ((int)(fBBar.high()/boxSize)) * boxSize;
				MiniBar renko = new MiniBar(ticker,time,highVal-boxSize,highVal,0);	
				renkoList.add(renko);
			}else{
				double lowVal = ((int)(fBBar.low()/boxSize)) * boxSize;
				MiniBar renko = new MiniBar(ticker,time,lowVal+boxSize,lowVal,0);		
				renkoList.add(renko);
			}
		}else{

			MiniBar renko = renkoList.get(renkoList.size()-1);
			double bull = fBBar.close() - renko.high();
			double bear = renko.low() - fBBar.close();
			if(bull >=boxSize){				
					int noOfRenko = (int) (bull/boxSize);
					if(noOfRenko>60){
						mainL.error("{} too big gap or too small boxsize {} BULL @ {}, noOfRenko in gapSize {}",fBBar.getTicker(),boxSize,time,bull);
						return null;
					}
					addRenkoOC(renkoList,noOfRenko,boxSize,CandlePatternType.BULLISH,time,barList);
			}else if(bear >= boxSize){				
					int noOfRenko = (int) (bear/boxSize);
					if(noOfRenko>60){
						mainL.error("{} too big gap or too small boxsize {} BEAR @ {}, noOfRenko in gapSize {}",fBBar.getTicker(),boxSize,time,bear);
						return null;
					}
					addRenkoOC(renkoList,noOfRenko,boxSize,CandlePatternType.BEARISH,time,barList);
			}		
			
		}
		return renkoList;
	}

	private synchronized strictfp List<MiniBar> addRenkoOC(List<MiniBar> renkoList,int noOfRenko,
			double boxSize,CandlePatternType candleType,long time,List<FBBar> barList){
		long maxTime = getMaxTimeAssigned(renkoList,time);
		String ticker = barList!=null?barList.get(0).getTicker():"";
		for (int i = 0; i < noOfRenko; i++) {
			long modTime  = maxTime > 0? (maxTime + (i+1)*1000):(time + i*1000);
			 MiniBar renko = renkoList.get(renkoList.size()-1);
			 long vol = getRenkoVolume(barList,renko.getTime());
			 if(candleType.equals(CandlePatternType.BULLISH)){
				 MiniBar newRenko = new MiniBar(ticker,modTime,renko.high(),renko.high()+boxSize,vol);
				 renkoList.add(newRenko);
			 }else{
				 MiniBar newRenko = new MiniBar(ticker,modTime,renko.low(),renko.low()-boxSize,vol);
				 renkoList.add(newRenko);
			 }		 
		}
		return renkoList;
	}
	
	private synchronized long getMaxTimeAssigned(List<MiniBar> renkoList, long time){
		long maxTime =0;
		for (int i = renkoList.size()-1; i >=0; i--) {
			MiniBar bar = renkoList.get(i);
			if(bar.getTime()/10000 == time/10000){
				maxTime = maxTime < bar.getTime()?bar.getTime():maxTime;
			}else{
				break;
			}
		}
		return maxTime;
	}
	
	
	private Integer getSec(long time){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("America/New_York"));
		calendar.setTimeInMillis(time);
        Integer sec = calendar.get(Calendar.SECOND);
        return sec;
	}
	
	private double calcEma(List<FBBar> allBarList, int emaNo,String type) {
		
		if(allBarList.size()<emaNo)return -1;	// not enough data
		else if(allBarList.size()==emaNo){ // sma init
			return mean(allBarList);
		}else{			
			FBBar barA= allBarList.get(allBarList.size()-2);
			double prevEma =0;
			switch(type){
			case "fast":
				prevEma= barA.getEmaFast();
				break;
			case "medium":
				prevEma= barA.getEmaMedium();
				break;
			case "slow":
				prevEma= barA.getEmaSlow();
				break;
			case "20":
				prevEma= barA.getEma20();
				break;
			}
			float alpha= (float) (2.0/(emaNo+1.0));
			FBBar barB= allBarList.get(allBarList.size()-1);
			return alpha*(barB.close() - prevEma) + prevEma;
		}
		
	}

	private double calcEmaChange(List<FBBar> allBarList, int emaNo) {
		if(allBarList.size() < emaNo*2)return -1;	// not enough data
		else{
			List<Double> values = new ArrayList<Double>();
			for (int i=allBarList.size()-1;i>=allBarList.size()-100; i--) {
				values.add(allBarList.get(i).getEmaSlow());
			}
			double min = Collections.min(values);
			double max = Collections.max(values);
			return ((max-min)/min)*100;
		}
	}

	private MiniBar getHeikinBar(List<FBBar> barList){
		if(barList.size()<1){
			return null;
		}
				
		FBBar barB = barList.get(barList.size()-1);
	
		double hClose = (barB.open() + barB.high() + barB.low() + barB.close())/4;
		double hOpen = (barB.open() + barB.close())/2;
		double hHigh = barB.high();
		double hLow = barB.low();
		
		if(barList.size()>1){
			 FBBar barA = barList.get(barList.size()-2);
			 MiniBar prvHeikin = barA.getHkaBar();;
			 hOpen = (prvHeikin.open()+prvHeikin.close())/2;
			 hHigh = Math.max(barB.high(), Math.max(hOpen, hClose));
			 hLow = Math.min(barB.low(),Math.min(hOpen, hClose));	
		}

		MiniBar bar = new MiniBar(hOpen,hClose,hLow,hHigh);
		bar.setDir(getDirection(barList,bar));
		return bar;
	}
	
	
	private String calcRecom(List<FBBar> barLast20){
		List<Double> min = new ArrayList<Double>();
		List<Double> max = new ArrayList<Double>();
		Calendar calendar = Calendar.getInstance();
		boolean supCalculated=false;
		double support = 0;double resistance =0;
		int downBrCount =0;int upBrCount=0;
		for (FBBar bar : barLast20) {
			calendar.setTimeInMillis(bar.getStartTime());
		//	calendar.setTimeZone(TimeZone.getTimeZone("EST"));
			int hr = calendar.get(Calendar.HOUR_OF_DAY);
	        int mint = calendar.get(Calendar.MINUTE);
	        if(hr == 8 && mint >=30 && mint <=40){
	        	min.add(bar.low());
	        	max.add(bar.high());
	        }else if(hr == 8 && mint > 40 && mint <42 && 
	        		supCalculated==false & min.size()>0 && max.size()>0){
	    		support = Collections.min(min);
	    		resistance = Collections.max(max);
//	    		System.out.println(bar.getTicker() + ": support : " + support + " resistance : " + resistance);
	    		supCalculated = true;
	        }else if(supCalculated){
	        	if(bar.close()<support){
	        		downBrCount ++;
	        	}else if(bar.close()>resistance){
	        		upBrCount ++;
	        	}
	        }
		}
		
		if(downBrCount > 3 && upBrCount ==0){
			return "SELL";
		}else if(upBrCount >3 && downBrCount==0){
			return "BUY";
		}else{
			return "DON'T TRADE";
		}
		
	}
	
	
	private static double calcAvgVolume(List<FBBar> barLast20){
		List<FBBar> subList =null;
		if(barLast20.size()>10){
			subList = barLast20.subList(barLast20.size()-10, barLast20.size());	
		}else{
			subList =barLast20;
		}
		
		List<Double> dList = toDoubleList(subList, "volume");
		
		return calcAvg(dList);
    	
	}
	
	private static double calcBolTang(FBBar bar){

		double bollingerDiff= bar.getUpperBollinger()-bar.getLowerBollinger();
		double bollingerSum= bar.getUpperBollinger()+bar.getLowerBollinger();
		double lowTangency = (2*bar.low()-bollingerSum)/(bollingerDiff)*100;
		double highTangency = (2*bar.high()-bollingerSum)/bollingerDiff*100;
		return Math.abs(lowTangency)>=highTangency?lowTangency:highTangency;
	}
	
	
	private double calcTangency(FBBar bar){
		double bollingerDiff= bar.getUpperBollinger()-bar.getLowerBollinger();
		double bollingerSum= bar.getUpperBollinger()+bar.getLowerBollinger();
		double lowTangency = (2*bar.low()-bollingerSum)/(bollingerDiff)*100;
		double highTangency = (2*bar.high()-bollingerSum)/bollingerDiff*100;
		return Math.abs(lowTangency)>Math.abs(highTangency)?lowTangency:highTangency;
	}
	
	
//	private static String calcRecom(List<FBBar> barLast20,FBBar bar){
//		if(barLast20.size()<20)return "WAIT";
//		
//		String recom = "WAIT";
//		double bollingerDiff= bar.getUpperBollinger()-bar.getLowerBollinger();
//		double bollingerSum= bar.getUpperBollinger()+bar.getLowerBollinger();
//		double lowTangency = (2*bar.low()-bollingerSum)/(bollingerDiff)*100;
//		double highTangency = (2*bar.high()-bollingerSum)/bollingerDiff*100;
//		double k1,k2,d1,d2=0;
//		FBBar prevFBBar = barLast20.get(barLast20.size()-2);
//		k1=prevFBBar.getStcPerK();d1=prevFBBar.getStcPerD();
//		k2=bar.getStcPerK();d2=bar.getStcPerD();
//		if(lowTangency<-100 && k2<20 && d2<20 && bar.getTrend()>0 && bollingerDiff>=0.1 && k1<=d1 && k2>=d2){
//			recom ="BUY";
//			//k1>=d1 && k2<=d2
//		}else if(highTangency > 100 && k2>80 && d2>80 && bollingerDiff>=0.1 && bar.getTrend()<0 &&
//				prevFBBar.getUpperBollinger()<prevFBBar.high() && bar.getUpperBollinger()>bar.high()){
//			recom = "SELL";
//		}
//		return recom;
//	}
	
	private static double calcStochasticPerK(List<FBBar> barLast20){
		if(barLast20.size()<14)return 0;
				
    	List<FBBar> dList = barLast20.subList(barLast20.size()-14, barLast20.size());		
    	double high14 = Collections.max(toDoubleList(dList,"high"));
    	double low14 = Collections.min(toDoubleList(dList,"low"));
    	double stcPerK= (barLast20.get(barLast20.size()-1).close()-low14) /
    			(high14-low14);
		return Math.round(stcPerK*100);
	}

	private static double calcMomentum(List<FBBar> barLast20){
		if(barLast20.size()<14)return 0;
				
    	List<FBBar> dList14 = barLast20.subList(barLast20.size()-14, barLast20.size());	
    	double sumBody14 = sum(toDoubleList(dList14,"body"));
    	double sumBodyBull14 = sum(toDoubleList(dList14,"bodyBull"));
    	double momentum= sumBody14 /sumBodyBull14*100;
		return Math.round(momentum);
	}
	
	private static double calcStochasticPerD(List<FBBar> barLast20, FBBar currentFBBar){
		int size = barLast20.size();
		if(size<17)return 0;
		double sum=0;
		sum += barLast20.get(size-3).getStcPerK();
		sum += barLast20.get(size-2).getStcPerK();
		sum += currentFBBar.getStcPerK();

		return Math.round(sum/3);
	}
	
	private static double calcTrend(List<FBBar> barLast20){
		if(barLast20.size()<20){
			return 0;
		}
		double shortTrend = calcSlope(barLast20,10);
		double middleTrend = calcSlope(barLast20,20);
		if(barLast20.size()>40){
			double longTrend = calcSlope(barLast20,40);
			if((shortTrend>0 && longTrend>0) || (shortTrend<0 && longTrend<0)){
				return shortTrend;
			}
		}
		if((shortTrend>0 && middleTrend>0) || (shortTrend<0 && middleTrend<0)){
			return shortTrend;
		}
		return 0;
	}
	
	private static double calcSlope(List<FBBar> barList,int n){
		double trend =0;
		List<FBBar> bars = barList.subList(barList.size()-n,barList.size()-1);
		n= bars.size();
		double sumY = sum(toDoubleList(bars,"sma"));
		double sumX = (n+n*n)/2;
		double sumXSq= 0;
		double sumXY= 0;
		for (int i=0;i<bars.size();i++) {
			sumXY += (i+1) * bars.get(i).getSma();
			sumXSq += (i+1)*(i+1);
		}
		trend = (n*sumXY-sumX*sumY)/(n*sumXSq-sumX*sumX)*100;
		return trend;
	}
	
	private static double sum(List<Double> vals){
		double sum=0;
		for (Double val : vals) {
			sum +=val;
		}
		return sum;
	}

	
    private static strictfp double support(List<FBBar> barLast20) {
    	List<FBBar> dList=null;
    	if(barLast20.size()>19){
    		dList= barLast20.subList(barLast20.size()-20, barLast20.size());
    	}else{
    		dList= barLast20;
    	}
		
    	return Collections.min(toDoubleList(dList,"close"));
    }
    
	
    private static strictfp double resistance(List<FBBar> barLast20) {
    	List<FBBar> dList=null;
    	if(barLast20.size()>19){
    		dList= barLast20.subList(barLast20.size()-20, barLast20.size());
    	}else{
    		dList= barLast20;
    	}
    	return Collections.max(toDoubleList(dList,"close"));
    }
    
    private static strictfp List<Double> toDoubleList(List<FBBar> barList,String type){
    	List<Double> dList = new ArrayList<Double>(barList.size());
    	for (int i=0; i< barList.size();i++) {
    		switch(type){
    		case "close" :
    			dList.add(barList.get(i).close());
    			break;
    		case "open" :
    			dList.add(barList.get(i).open());
    			break;
    		case "high" :
    			dList.add(barList.get(i).high());
    			break;
    		case "low" :
    			dList.add(barList.get(i).low());
    			break;
    		case "sma" :
    			dList.add(barList.get(i).getSma());
    			break;
    		case "body" :
    			dList.add(barList.get(i).getBodyHeight());
    			break;
    		case "bodyBull" :
    			if(barList.get(i).getCandleType().equals(CandlePatternType.BULLISH)){
    				dList.add(barList.get(i).getBodyHeight());
    			}
    			break;
    		case "volume" :
    			dList.add((double) barList.get(i).getVolume());
    			break;
    		}
    		
		}
    	return dList;
    }
    
	/**
     * Standard deviation is a statistical measure of spread or variability.The
     * standard deviation is the root mean square (RMS) deviation of the values
     * from their arithmetic mean.
     *
     * <b>populationStandardDeviation</b> normalizes values by N, where N is the sample size. This the
     * <i>Population Standard Deviation</i>
     * @param values
     * @return
     */
    private static strictfp double populationStDV(List<FBBar> bars,double mean) {

        double dv = 0;
        for (FBBar bar : bars) {
            double dm = bar.close() - mean;
            dv += dm * dm;
        }
        return Math.sqrt(dv / bars.size());

    }

    
    private static strictfp double calcAvg(List<Double> values) {
    	double sum=0;
    	for (Double v : values) {
    		sum += v;
		}
        return sum / values.size();
    }
    

    /**
     * Calculate the mean of an array of values
     *
     * @param values The values to calculate
     * @return The mean of the values
     */
    private static strictfp double mean(List<FBBar> bars) {
    	double sum=0;
    	for (FBBar bar : bars) {
    		sum += bar.close();
		}
        return sum / bars.size();
    }

    
    
	public Direction getDirection(List<FBBar> barList, MiniBar barB){
		
		if(barList.size()<3){
			return Direction.SiDE;
		}
		
		MiniBar barC = barList.get(barList.size()-3).getHkaBar();
		MiniBar barA = barList.get(barList.size()-2).getHkaBar();
		
		Direction dir = Direction.SiDE;

		if(barB.getCandleType().equals(CandlePatternType.BEARISH) && barA.getCandleType().equals(CandlePatternType.BULLISH ) &&
				(barB.getHeight() > barA.getHeight() || barB.getBodyHeight() > barA.getBodyHeight())){
			dir= Direction.REVERSED_DOWNWARD;		
	
		}else if(barA.getCandleType().equals(CandlePatternType.BEARISH) && barB.getCandleType().equals(CandlePatternType.BEARISH) && 
				barC.getCandleType().equals(CandlePatternType.BULLISH) &&
				barB.close()>barC.low()){
			dir= Direction.REVERSED_DOWNWARD;	
			
		}else if(barB.getCandleType().equals(CandlePatternType.BULLISH) && barA.getCandleType().equals(CandlePatternType.BEARISH) &&
				(barB.getHeight() > barA.getHeight() || barB.getBodyHeight() > barA.getBodyHeight())){
			dir= Direction.REVERSED_UPWARD;
			
		}else if(barA.getCandleType().equals(CandlePatternType.BULLISH) && barB.getCandleType().equals(CandlePatternType.BULLISH) && 
				barC.getCandleType().equals(CandlePatternType.BEARISH) &&
				barB.close()>barC.high()){
			dir= Direction.REVERSED_UPWARD;			
		}else if (barA.getCandleType().equals(CandlePatternType.BULLISH) && barB.getCandleType().equals(CandlePatternType.BULLISH) && 
				(barB.high()>barA.high() || barB.high()>barC.high())){
			dir= Direction.UP;
		
		}else if (barA.getCandleType().equals(CandlePatternType.BEARISH) && barB.getCandleType().equals(CandlePatternType.BEARISH) && 
				(barB.low()<barA.low() || barB.low()<barC.low())){
			dir= Direction.DOWN;
		
		}else if (barA.getCandleType().equals(CandlePatternType.BULLISH) && barB.getCandleType().equals(CandlePatternType.BEARISH)){
			dir= Direction.UP;
		
		}else if (barA.getCandleType().equals(CandlePatternType.BULLISH) && barB.getCandleType().equals(CandlePatternType.BEARISH)){
			dir= Direction.DOWN;
		}
		return dir;
	}	
	
	
}


//public strictfp List<MiniBar> addRenko(List<MiniBar> renkoList, FBBar fBBar, double boxSize) {
//	double close = 0;double open=0;
//	MiniBar newRenko = null;
//	if(renkoList==null)renkoList= new ArrayList<MiniBar>();
//	
//	if(renkoList.size()>0 && renkoList.get(renkoList.size()-1).getBodyHeight()==boxSize){
//		MiniBar renko = renkoList.get(renkoList.size()-1);
//		close = renko.close();open = renko.open();
//	
//	}else if(renkoList.size()>0 && renkoList.get(renkoList.size()-1).getBodyHeight()<boxSize){
//		MiniBar renko = renkoList.get(renkoList.size()-1);
//		if(renko.high()<fBBar.close() && renko.low()>fBBar.low() && fBBar.getCandleType().equals(CandlePatternType.BEARISH)){
//			if(renko.low()fBBar.close())
//		}else if(renko.high()<fBBar.close() && renko.low()>fBBar.low() && fBBar.getCandleType().equals(CandlePatternType.BULLISH)){
//			
//		}else if(renko.high()<fBBar.close()){
//			
//		}else if(renko.low()>fBBar.low()){
//			
//		}else{
//			
//		}
//	}else if(renkoList.size()==0){
//		if(fBBar.getHeight()<boxSize && fBBar.getCandleType().equals(CandlePatternType.BEARISH)){
//			 newRenko = new MiniBar(fBBar.high(),fBBar.low(),fBBar.low(),fBBar.high());
//			 renkoList.add(newRenko);
//		}else if(fBBar.getHeight()<boxSize && fBBar.getCandleType().equals(CandlePatternType.BULLISH)){
//			 newRenko = new MiniBar(fBBar.low(),fBBar.high(),fBBar.low(),fBBar.high());
//			 renkoList.add(newRenko);
//		}else if(fBBar.getHeight()>boxSize && fBBar.getCandleType().equals(CandlePatternType.BEARISH)){
//			int noOfRenko = (int) (fBBar.getHeight()/boxSize);
//			for (int i = 0; i < noOfRenko; i++) {
//				 newRenko = new MiniBar(fBBar.high()-i*boxSize,fBBar.low()-i*boxSize,fBBar.low()-i*boxSize,fBBar.high()-i*boxSize);
//				 renkoList.add(newRenko);
//			}
//			if(fBBar.getHeight()%boxSize>0){
//				 double remainder = Util.roundTo2D(fBBar.getHeight()%boxSize);
//				 newRenko = new MiniBar(newRenko.low(),newRenko.low()-remainder,newRenko.low()-remainder,newRenko.low());
//				 renkoList.add(newRenko);
//			}
//		}else if(fBBar.getHeight()>boxSize && fBBar.getCandleType().equals(CandlePatternType.BULLISH)){
//			int noOfRenko = (int) (fBBar.getHeight()/boxSize);
//			for (int i = 0; i < noOfRenko; i++) {
//				 newRenko = new MiniBar(fBBar.high()+i*boxSize,fBBar.low()+i*boxSize,fBBar.low()+i*boxSize,fBBar.high()+i*boxSize);
//				 renkoList.add(newRenko);
//			}
//			if(fBBar.getHeight()%boxSize>0){
//				 double remainder = Util.roundTo2D(fBBar.getHeight()%boxSize);
//				 newRenko = new MiniBar(newRenko.high(),newRenko.high()+remainder,newRenko.high(),newRenko.high()+remainder);
//				 renkoList.add(newRenko);
//			}
//		}
//		
//	}
//	
//	return renkoList;
//}

