package com.sesnu.backtest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.sesnu.model.CandlePatternType;
import com.sesnu.model.CandleType;
import com.sesnu.model.FBBar;
import com.sesnu.service.OrderEntry;
import com.sesnu.service.Util;
import com.sesnu.ui.MultiLineChart;

@SuppressWarnings("unused")
public class Strategies {
	
//	private static String folderName = "eoddata";
	private static String folderName = "Nov25";
	private static int  maxRowsPerTicker = 50000;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String,List<FBBar>> runStrategy(StrategyTypes type, boolean writeResult) 
			throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException{
		
		Class[] paramString = new Class[1];
		paramString[0] = Boolean.class;
		Class<Strategies> clazz = Strategies.class;
		Method method = clazz.getDeclaredMethod(type.name(), paramString);
		return (Map<String, List<FBBar>>) method.invoke(clazz, writeResult);
	
	}
	
	
	private static Map<String,List<FBBar>> Star(Boolean writeResult){
		BarSerializer barSerializer = new BarSerializer();
		Map<String,List<FBBar>> barMap = barSerializer.deSerStock(folderName,maxRowsPerTicker);
		Map<String,List<FBBar>> result = new HashMap<String,List<FBBar>> ();
		
		int counter=0;
		for (Map.Entry<String, List<FBBar>> entry : barMap.entrySet()) {			
				List<FBBar> list = entry.getValue();
				List<Double> minList =  new ArrayList<Double>();
				List<Double> maxList =  new ArrayList<Double>();
				double entryPrice = 0;
				boolean entered=false;boolean exited = false;
				int boundry =0;
				for (int i=1;i<list.size();i++) {					
					FBBar barB = list.get(i);FBBar barA = list.get(i-1);
					if(barA.getEmaFast() < barA.getEmaSlow() && barB.getEmaFast() > barB.getEmaSlow() && barB.getEmaSlow() !=-1 && i > boundry){
//						System.out.println(barB.getTicker() + ", slowEmaSlope: " + barB.getEmaSlope()  + ", date :"+ Util.getDate(barB.getStartTime()) + ", time: " + Util.getDoubleTime(barB.getStartTime()));
						int secondCrossIndx = getNextCrossIndx(list,i+1);
						if(secondCrossIndx > 0 && Util.getDoubleTime(barB.getStartTime())>9.3){
						FBBar secondCrossBar = list.get(secondCrossIndx);
							if((secondCrossIndx-i) > 60 && Util.getDate(barB.getStartTime())==Util.getDate(secondCrossBar.getStartTime())
										&& Util.getDoubleTime(secondCrossBar.getStartTime())<14){
								boundry = secondCrossIndx;
								int crossedEma = getCrossedEma(list,i+1,secondCrossIndx);
								if(crossedEma<100){
									System.out.println(barB.getTicker() + ", First cross @ : " + Util.getDateTime(barB.getStartTime()) + ", second cross:" + Util.getDateTime(secondCrossBar.getStartTime()) + ", crossed Ema: " + crossedEma);
								}
							}
						}
//						int crossIndx = i;double maxHeight=0;double vol=0;int count =0;
//						for (int j = crossIndx+1; j< list.size(); j++) {	
//							FBBar barCp = list.get(j-1); FBBar barC = list.get(j);
//							if(barC.getEmaFast() > barC.getEmaSlow()){								
//								if(barCp.getEmaFast() < barCp.getEmaMedium() && barC.getEmaFast() > barC.getEmaMedium() && barC.getEmaSlope()>0.05){
//									System.out.println(barB.getTicker() + ", slowEmaSlope: " + barC.getEmaSlope()  + ", crossed @ : " + Util.getDateTime(barB.getStartTime()) + ", entry: " + Util.getDateTime(barC.getStartTime()));
//									break;
//								}
//							}else{
//								break;
//							}
//						}
					}
					
				}
		}
	//	System.out.println(counter);
		return result;		
	}
	
	private static int getNextCrossIndx(List<FBBar> list, int start){
		for (int j = start; j< list.size(); j++) {	
			FBBar barC = list.get(j);
			if(barC.getEmaFast() < barC.getEmaSlow()){								
				return j;
			}
		}
		return -1;
	}
	
	private static int getCrossedEma(List<FBBar> list, int start,int end){
		List<Integer> crossedEmas = new ArrayList<Integer>();
		end = end-start >70?start+70:end;
		for (int j = start; j< end; j++) {	
			FBBar barC = list.get(j);
			if(barC.low() < barC.getEma20()){								
				crossedEmas.add(20);
				break;
			}
		}
		
		for (int j = start; j< end; j++) {	
			FBBar barC = list.get(j);
			if(barC.low() < barC.getEmaMedium()){								
				crossedEmas.add(50);
				break;
			}
		}
		
		for (int j = start; j< end; j++) {	
			FBBar barC = list.get(j);
			if(barC.low() < barC.getEmaSlow()){								
				crossedEmas.add(100);
				break;
			}
		}
		if(crossedEmas.size()>0){
			return crossedEmas.get(crossedEmas.size()-1);
		}
		return -1;
	}
	
//	private static Map<String,List<FBBar>> tester(Boolean writeResult){
//		BarSerializer barSerializer = new BarSerializer();
//		Map<String,List<FBBar>> barMap = barSerializer.deSerStock(folderName,maxRowsPerTicker);
//		Map<String,List<FBBar>> result = new HashMap<String,List<FBBar>> ();
//		
//		int counter=0;
//		OrderEntry orderEntry = new OrderEntry(null,null,null,null);
//		for (Map.Entry<String, List<FBBar>> entry : barMap.entrySet()) {			
//				List<FBBar> list = entry.getValue();
//				List<FBBar> list2 = new ArrayList<FBBar>();
//				boolean done=true;
//				for (int i=1;i<list.size();i++) {					
//					list2.add(list.get(i));
//					if(list2.size()>12){
////						orderEntry.submitOrder(list2);
//					}
//				}
//		}
//		System.out.println(counter);
//		return result;		
//	}

	private static Map<String,List<FBBar>> Renko(Boolean writeResult){
		BarSerializer barSerializer = new BarSerializer();
		barSerializer.deSerStock(folderName,maxRowsPerTicker);
		return null;
	}
	
	private static Map<String,List<FBBar>> HKMA2(Boolean writeResult){
		BarSerializer barSerializer = new BarSerializer();
		Map<String,List<FBBar>> barMap = barSerializer.deSerStock(folderName,maxRowsPerTicker);
		Map<String,List<FBBar>> result = new HashMap<String,List<FBBar>> ();
		
		int counter=0;
		System.out.println("Analaysing data ... ");
		Util.writeToFile("entry,min,close,minDiff,delta,IN-Time,Out-Time,BarCount,slowSlope", "hibrid_result.csv", true);
		for (Map.Entry<String, List<FBBar>> entry : barMap.entrySet()) {			
				List<FBBar> list = entry.getValue();
				List<Double> minList =  new ArrayList<Double>();
				List<Double> maxList =  new ArrayList<Double>();
				double entryPrice = 0;
				boolean entered=false;int barCount =0;double barHeight = 0;Float inHr =0f;int reported=0; double slowSlope=0;
				for (int i=2;i<list.size();i++) {					
					FBBar barB = list.get(i);FBBar barA = list.get(i-1);FBBar barC = list.get(i-2);
					Float time = getHr(barB);
					double slope = (barC.getEmaSlow()-barB.getEmaSlow())/barC.getEmaSlow()*100;
					if(entered==false && slope > 0 && barA.getHkaBar().getCandleType().equals(CandlePatternType.BULLISH) &&
							barB.getHkaBar().getCandleType().equals(CandlePatternType.BEARISH) && barB.getBodyHeight()>=0.01
							&& time<14){ //&& barB.open()-barB.close()<=0.1 
						inHr = time;
						slowSlope = slope;
						barHeight = barB.open()-barB.close();
						entryPrice = barB.low();
						entered = true;
						int progress = Math.round(((float)i/(float)list.size() *100));
						
						if(progress%5==0 && progress!=reported){
							System.out.println(progress + "%");
							reported=progress;
						}						
					}else if (entered && barB.getHkaBar().getCandleType().equals(CandlePatternType.BEARISH)){
						barCount ++;
						minList.add(barB.low());
					}else if (entered && (barB.getHkaBar().getCandleType().equals(CandlePatternType.BULLISH) || time>15)){
						double min = minList.size()>0?Collections.min(minList):entryPrice;
						double absoluteChange = Util.roundTo2D(entryPrice-min);
						double crossChange = Util.roundTo2D(entryPrice - barB.close());
						String outPut = entryPrice + "," +  min + "," + barB.close() + "," + absoluteChange + "," + barHeight + "," + inHr + "," + time + "," + barCount + "," + slowSlope;
						Util.writeToFile(outPut, "hibrid_result.csv", false);
//						System.out.println(outPut);
						entered =false; barHeight =0;time=0f;minList =  new ArrayList<Double>();barCount=0;slowSlope=0;
					}
					
				}
		}
		System.out.println(counter);
		return result;		
	}
	
	
	private static Map<String,List<FBBar>> HKMA(Boolean writeResult){
		BarSerializer barSerializer = new BarSerializer();
		Map<String,List<FBBar>> barMap = barSerializer.deSerStock(folderName,maxRowsPerTicker);
		Map<String,List<FBBar>> result = new HashMap<String,List<FBBar>> ();
		
		int counter=0;
		System.out.println("Analaysing data ... ");
		Util.writeToFile("entry,min,close,minDiff,delta,IN-Time,Out-Time,BarCount,slowSlope", "hibrid_result.csv", true);
		for (Map.Entry<String, List<FBBar>> entry : barMap.entrySet()) {			
				List<FBBar> list = entry.getValue();
				List<Double> minList =  new ArrayList<Double>();
				List<Double> maxList =  new ArrayList<Double>();
				double entryPrice = 0;
				boolean entered=false;int barCount =0;double barDiff = 0;Float inHr =0f;int reported=0; double slowSlope=0;
				for (int i=1;i<list.size();i++) {					
					FBBar barB = list.get(i);FBBar barA = list.get(i-1);
					Float time = getHr(barB);
					if(entered==false && barB.getEmaMedium()<barA.getEmaMedium() && 
							barB.getHkaBar().open()>barB.getEmaMedium() && barB.getHkaBar().close()<barB.getEmaMedium() 
							&& time<14){ //&& barB.open()-barB.close()<=0.1 
						inHr = time;
						barDiff = Util.roundTo3D(barB.open()-barB.close());
						entryPrice = barB.low();
						entered = true;
						int progress = Math.round(((float)i/(float)list.size() *100));
						slowSlope = Util.roundTo2D((barA.getEmaSlow()-barB.getEmaSlow())/barA.getEmaSlow()*100);
						if(progress%5==0 && progress!=reported){
							System.out.println(progress + "%");
							reported=progress;
						}						
					}else if (entered && barB.getHkaBar().close()<barB.getEmaMedium()){
						barCount ++;
						minList.add(barB.low());
					}else if (entered && (barB.getHkaBar().close()>barB.getEmaMedium() || time>15)){
						double min = minList.size()>0?Collections.min(minList):entryPrice;
						double absoluteChange = Util.roundTo2D(entryPrice-min);
						double crossChange = Util.roundTo2D(entryPrice - barB.close());
						String outPut = entryPrice + "," +  min + "," + barB.close() + "," + absoluteChange + "," + barDiff + "," + inHr + "," + time + "," + barCount + "," + slowSlope;
						Util.writeToFile(outPut, "hibrid_result.csv", false);
//						System.out.println(outPut);
						entered =false; barDiff =0;time=0f;minList =  new ArrayList<Double>();barCount=0;slowSlope=0;
					}
					
				}
		}
		System.out.println(counter);
		return result;		
	}
	
	private static float getHr(FBBar fbBar){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("America/New_York"));
		calendar.setTimeInMillis(fbBar.getStartTime());
		int hr = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        return hr + (float)min/100;
	}
	
	private static Map<String,List<FBBar>> movingAverage(Boolean writeResult){
		BarSerializer barSerializer = new BarSerializer();
		Map<String,List<FBBar>> barMap = barSerializer.deSerStock(folderName,maxRowsPerTicker);
		Map<String,List<FBBar>> result = new HashMap<String,List<FBBar>> ();
		
		int counter=0;
		for (Map.Entry<String, List<FBBar>> entry : barMap.entrySet()) {			
				List<FBBar> list = entry.getValue();
				List<Double> minList =  new ArrayList<Double>();
				List<Double> maxList =  new ArrayList<Double>();
				double entryPrice = 0;
				boolean entered=false;boolean exited = false;
				for (int i=1;i<list.size();i++) {					
					FBBar barB = list.get(i);FBBar barA = list.get(i-1);
					if(entered==false && barB.getEmaSlow()>barB.getEmaMedium() && barB.getEmaMedium()>barB.getEmaFast() && isGate(barB) && getSlope(barA,barB)==-1){
						double delta = barA.getEmaMedium()-barA.getEmaFast();
						if(barB.high()>=barA.getEmaFast()-delta){
							entered = true;
							entryPrice = barB.high();
						}
//						Date d = new Date(barB.getStartTime());
//						String time = d.getHours() + ":" + d.getMinutes();
//						int startIndex= i>20?i-20:0;
//						int lastIndex= list.size()>i+20?i+20:list.size();
//						List<FBBar> candList = list.subList(0, lastIndex);
//						result.put(entry.getKey() + " " + barB.getRecom() + " " + d.getDate() + " " + time, candList);
//						if(writeResult)barSerializer.writeToFile(candList, entry.getKey()+"movingAverage", false);
					}else if (entered && !exited){
						maxList.add(barB.high());
						minList.add(barB.low());
						if(barB.high()>barB.getEmaMedium()){
							exited = true;
						}
					}else if (entered && exited){
						double max = Collections.max(maxList);
						double min = Collections.min(minList);
						System.out.println(entryPrice + "," +  min + "," + max);
						entered =false; 
						exited = false;
					}
					
				}
		}
		System.out.println(counter);
		return result;		
	}
	
	
	private static int getSlope(FBBar fbBarA,FBBar fbBarB){
		if(fbBarB.getEmaFast()<fbBarA.getEmaFast() && fbBarB.getEmaMedium()<fbBarA.getEmaMedium() && fbBarB.getEmaSlow()<fbBarA.getEmaSlow()){
			return -1;
		}else if(fbBarB.getEmaFast()>fbBarA.getEmaFast() && fbBarB.getEmaMedium()>fbBarA.getEmaMedium() && fbBarB.getEmaSlow()>fbBarA.getEmaSlow()){
			return 1;
		}
		return 0;
	}
	
	private static boolean isGate(FBBar fbBar){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("America/New_York"));
		calendar.setTimeInMillis(fbBar.getStartTime());
		int hr = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        if(hr<10 || (hr==10 && min <=30))return true;  //12 bars, 1hr
        
        return false;
	}
	
	private boolean isSafeDistance(List<FBBar> histBarList){
		int crossPoint =-1;
		for (int i =histBarList.size()-1; i>= 0; i--) {
			FBBar fbBar = histBarList.get(i);
			if((fbBar.getEmaFast() < fbBar.getEmaMedium() && fbBar.getEmaMedium() > fbBar.getEmaSlow()) ||
					(fbBar.getEmaFast() > fbBar.getEmaMedium() && fbBar.getEmaMedium() < fbBar.getEmaSlow())){
				crossPoint = i;
				break;
			}
		}
		return crossPoint<=12;
	}
	
	private static Map<String,List<FBBar>> afterGateDrop(Boolean writeResult){
		BarSerializer barSerializer = new BarSerializer();
		Map<String,List<FBBar>> barMap = barSerializer.deSerStock(folderName,maxRowsPerTicker);
		Map<String,List<FBBar>> result = new HashMap<String,List<FBBar>> ();
		
		for (Map.Entry<String, List<FBBar>> entry : barMap.entrySet()) {			
			List<List<FBBar>> splittedBars = splitByDays(entry.getValue());
			for (List<FBBar> list : splittedBars) {
				int countBullish =0;int riseCount=0; int triCount =0;
				for (int i=0;i<list.size();i++) {					
					FBBar barB = list.get(i);
					if(barB.getHkaBar().getCandleType().equals(CandlePatternType.BULLISH)){
						countBullish ++;
						riseCount += (i>0 && barB.getHkaBar().close()>list.get(i-1).getHkaBar().close())?1:0;
						triCount += (i>0 && barB.getHkaBar().close()<list.get(i-1).getHkaBar().close() && barB.getHkaBar().open()>list.get(i-1).getHkaBar().open())?1:0;
					}else if (barB.getHkaBar().getCandleType().equals(CandlePatternType.BEARISH) && countBullish>3 && riseCount > i/2 && triCount <=2 && i<30){
						Date d = new Date(barB.getStartTime());
						String time = d.getHours() + ":" + d.getMinutes();
						int startIndex= i>20?i-20:i;
						int lastIndex= list.size()>i+20?i+20:list.size();
						List<FBBar> candList = list.subList(0, lastIndex);
						result.put(entry.getKey() + " " + barB.getRecom() + " " + d.getDate() + " " + time, candList);
						if(writeResult)barSerializer.writeToFile(candList, entry.getKey()+"gateDrop", false);
						break;
					}else{
						break;
					}
				}
			}

		}
		
		return result;		
	}
	
	private static Map<String,List<FBBar>> afterGateRaise(Boolean writeResult){
		BarSerializer barSerializer = new BarSerializer();
		Map<String,List<FBBar>> barMap = barSerializer.deSerStock(folderName,maxRowsPerTicker);
		Map<String,List<FBBar>> result = new HashMap<String,List<FBBar>> ();
		
		for (Map.Entry<String, List<FBBar>> entry : barMap.entrySet()) {
			
			List<List<FBBar>> splittedBars = splitByDays(entry.getValue());
			for (List<FBBar> list : splittedBars) {
				int countBearish =0;
				System.out.println("*********");
				for (int i=0;i<list.size();i++) {					
					FBBar barB = list.get(i);
					if(barB.getHkaBar().getCandleType().equals(CandlePatternType.BEARISH)){
						System.out.println(entry.getKey());
						countBearish ++;
					}else if (barB.getHkaBar().getCandleType().equals(CandlePatternType.BULLISH) && countBearish>3 && i<30){
						Date d = new Date(barB.getStartTime());
						String time = d.getHours() + ":" + d.getMinutes();
						int startIndex= i>20?i-20:i;
						int lastIndex= list.size()>i+20?i+20:list.size();
						List<FBBar> candList = list.subList(0, lastIndex);
						result.put(entry.getKey() + " " + barB.getRecom() + " " + d.getDate() + " " + time, candList);
						if(writeResult)barSerializer.writeToFile(candList, entry.getKey()+"gateDrop", false);
						break;
					}else{
						break;
					}
				}
			}

		}
		
		return result;		
	}
	
	
	private static Map<String,List<FBBar>> steelHammer(Boolean writeResult){
		BarSerializer barSerializer = new BarSerializer();
		Map<String,List<FBBar>> barMap =barSerializer.deSerStock(folderName,maxRowsPerTicker);
		Map<String,List<FBBar>> result = new HashMap<String,List<FBBar>> ();
		
		for (Map.Entry<String, List<FBBar>> entry : barMap.entrySet()) {			
			List<List<FBBar>> splittedBars = splitByDays(entry.getValue());
			for (List<FBBar> list : splittedBars) {
				for (int i=2;i<list.size();i++) {					
					FBBar barAPr = list.get(i-2);FBBar barA = list.get(i-1);FBBar barB = list.get(i);
					if(barAPr.getHkaBar().getCandleType().equals(CandlePatternType.BULLISH) && barAPr.getHkaBar().low()>barAPr.getSma() && 
					   barA.getHkaBar().getCandleType().equals(CandlePatternType.BULLISH) && barA.getHkaBar().low()>barA.getSma() &&
					   barB.getHkaBar().getCandleType().equals(CandlePatternType.BEARISH) && barB.getHkaBar().low()>barB.getSma() &&
					   barB.getHkaBar().close()<=barA.getHkaBar().open() && barB.getHkaBar().low()<=barAPr.getHkaBar().low()){
						Date d = new Date(barB.getStartTime());
						String time = d.getHours() + ":" + d.getMinutes();
						int startIndex= i>20?i-20:i;
						int lastIndex= list.size()>i+20?i+20:list.size();
						List<FBBar> candList = list.subList(startIndex, lastIndex);
						result.put(entry.getKey() + " " + barB.getRecom() + " " + time, candList);
						if(writeResult)barSerializer.writeToFile(candList, entry.getKey()+"squeez", false);
					}
				}
			}

		}
		
		return result;		
	}
	
	
	private static Map<String,List<FBBar>> fireball(Boolean writeResult){
		BarSerializer barSerializer = new BarSerializer();
		Map<String,List<FBBar>> barMap = barSerializer.deSerStock(folderName,maxRowsPerTicker);
		Map<String,List<FBBar>> result = new HashMap<String,List<FBBar>> ();
		for (Map.Entry<String, List<FBBar>> entry : barMap.entrySet()) {
			
			List<List<FBBar>> splittedBars = splitByDays(entry.getValue());
			for (List<FBBar> list : splittedBars) {
				for (int i=40;i<list.size();i++) {
					FBBar bar = list.get(i);
					if(bar.getRecom().equals("BUY") || bar.getRecom().equals("SELL")){
						Date d = new Date(bar.getStartTime());
						String time = d.getHours() + ":" + d.getMinutes();
						int lastIndex= list.size()>i+30?i+30:list.size();
						List<FBBar> candList = list.subList(i-30, lastIndex);
						result.put(entry.getKey() + " " + bar.getRecom() + " " + time, candList);
						if(writeResult)barSerializer.writeToFile(candList, entry.getKey()+"squeez", false);
					}

				}
			}

		}
		
		return result;
		
	}
	
	private static Map<String,List<FBBar>> bollingerBreakDown(Boolean writeResult){
		BarSerializer barSerializer = new BarSerializer();
		Map<String,List<FBBar>> barMap = barSerializer.deSerStock(folderName,maxRowsPerTicker);
		Map<String,List<FBBar>> result = new HashMap<String,List<FBBar>> ();
		for (Map.Entry<String, List<FBBar>> entry : barMap.entrySet()) {
			
			List<List<FBBar>> splittedBars = splitByDays(entry.getValue());
			for (List<FBBar> list : splittedBars) {
				for (int i=40;i<list.size();i++) {					
					List<FBBar> last20 = list.subList(i-20, i);
					FBBar bar1 = last20.get(last20.size()-1);
					Date d = new Date(bar1.getStartTime());
					String time = d.getHours() + ":" + d.getMinutes();
					double bollingerDiff= bar1.getUpperBollinger()-bar1.getLowerBollinger();
					double bollingerSum= bar1.getUpperBollinger()+bar1.getLowerBollinger();
					double lowTangency1 = (2*bar1.low()-bollingerSum)/(bollingerDiff)*100;
					if(lowTangency1<-90){
						for (int j = last20.size()-1;j>=0; j--) {
							FBBar bar2 = last20.get(j);
							double prevRes=10000;
							double bollingerDiffi= bar2.getUpperBollinger()-bar2.getLowerBollinger();
							double bollingerSumi= bar2.getUpperBollinger()+bar2.getLowerBollinger();
							double highTangencyi = (2*bar2.high()-bollingerSumi)/bollingerDiffi*100;
							if(highTangencyi>90 && bar2.getStcPerK()>80 && bar2.getStcPerD()>80){								
								for (int k = last20.size()-j-1;k>=0; k--) {
									if(prevRes>=last20.get(k).low()){
										prevRes=last20.get(k).high();										
									}else{
										break;
									}									
								}
								break;
							}
							if(prevRes!=10000 && prevRes>bar1.low()){
								int lastIndex= list.size()>i+30?i+30:list.size();
								List<FBBar> candList = list.subList(i-30, lastIndex);
								result.put(entry.getKey() + " BreakDown" + time , candList);
								if(writeResult)barSerializer.writeToFile(candList, entry.getKey()+"BreakDown", false);
							}
						}
					}
				}
			}

		}
		
		return result;

	}
	
	
	
	private static Map<String,List<FBBar>> shortCandid(Boolean writeResult){
		BarSerializer barSerializer = new BarSerializer();
		Map<String,List<FBBar>> barMap = barSerializer.deSerStock(folderName,maxRowsPerTicker);
		Map<String,List<FBBar>> result = new HashMap<String,List<FBBar>> ();
		for (Map.Entry<String, List<FBBar>> entry : barMap.entrySet()) {
			
			List<List<FBBar>> splittedBars = splitByDays(entry.getValue());
			for (List<FBBar> list : splittedBars) {
				for (int i=20;i<list.size();i++) {
					int lastIndex= list.size()>i+20?i+20:list.size();
					List<FBBar> candList = list.subList(i, lastIndex);
					double count=0;double tan=0;long vol=0;
					for (FBBar bar : candList) {
						if(bar.close()<bar.getSma()){
							count++;
						}
						tan += bar.getBolTan();
						vol += bar.getVolume();
					}
					if(count/candList.size()>=0.8){
						FBBar bar = list.get(i);
//						double avgTan = Util.roundTo2D(tan/candList.size());
						double avgTan = Util.roundTo2D(count/candList.size());
						long avgVol = vol/candList.size()/1000;
						System.out.println("avg vol (k)" + bar.getTicker() +" " + avgVol);
					
						Date d = new Date(bar.getStartTime());
						String time = d.getHours() + ":" + d.getMinutes();
						result.put(entry.getKey() + " " + bar.getRecom() + " " + avgTan, candList);
					}
				}
			}

		}
		
		return result;
	}
	
	
	
	private static void gapAnalyser(Boolean writeResult){
		BarSerializer barSerializer = new BarSerializer();
		Map<String,List<FBBar>> result = new HashMap<String,List<FBBar>> ();
		Map<String,List<FBBar>> barMap = barSerializer.deSerStock(folderName,maxRowsPerTicker);
		for (Map.Entry<String, List<FBBar>> entry : barMap.entrySet()) {
			for (int i = 1; i<entry.getValue().size()-1;i++) {
				FBBar barA = entry.getValue().get(i-1);
				FBBar barB = entry.getValue().get(i);
				if(barB.getStartTime() - barA.getStartTime() > 36000000){
					int trend = getTrend(entry.getValue(),i);
					Double gap = ((barB.close()-barA.open())/barA.close())*100;
					if(gap>1.1 && gap<1.3 && trend >=2){
						List<FBBar> candList = entry.getValue().subList(i-5, i+40);
						barSerializer.writeToFile(candList, entry.getKey()+"summary", false);
						result.put(entry.getKey() + "-" + Util.roundTo2D(gap), candList);
					}
					else
						if(gap<-1.1 && gap>-1.5 && trend<=-2){
						List<FBBar> candList = entry.getValue().subList(i-5, i+40);
						barSerializer.writeToFile(candList, entry.getKey()+"summary", false);
						result.put(entry.getKey() + "-" + Util.roundTo2D(gap), candList);
					}
	
				}
			}
		}
		
		
	//	CandleStick cs = new CandleStick("test");
	//	cs.draw(result);
	}
	
		// utility
		
		private static List<List<FBBar>> splitByDays(List<FBBar> barList){
			
			List<List<FBBar>> allList = new ArrayList<List<FBBar>>();
			int startPoint =0;
			for (int i = 0; i<barList.size()-1;i++) {
				
				FBBar barA = barList.get(i);
				FBBar barB = barList.get(i+1);
				if(barB.getStartTime() - barA.getStartTime() > 36000000){
					allList.add(barList.subList(startPoint, i));
					startPoint = i+1;
				}
			}
			allList.add(barList.subList(startPoint, barList.size()));
			return allList;
		}

		private static int getTrend(List<FBBar> barList, int index){
			int barDirection =0;
			for (int i = 0; i < 3; i++) {
				barDirection += barList.get(index+i).getCandleType().equals(CandlePatternType.BULLISH)?1:0;
				barDirection += barList.get(index+1).getCandleType().equals(CandlePatternType.BEARISH)?-1:0;
			}
			
			return barDirection;
		}
		
		private static int getMomentum(List<FBBar> list,int start){
			if(start==0)return -1;
			double trend = list.get(start).getTrend();
			int entry=-1;
			for (int i =start;i<list.size();i++) {
				double last10Close = list.get(start-10).close();
				double prevClose = list.get(start-1).close();
				double last9Close = list.get(start-9).close();				
				double nowClose = list.get(start).close();
				double mPrev = prevClose - last10Close;
				double mNow = nowClose - last9Close;
				double mSlope = mNow - mPrev;
				if(trend > 0 && mSlope > 0.125 && mPrev <0 && mNow <0){
					entry = i;
					break;
				}else if(trend<0 && mSlope < 0.125 && mPrev >0 && mNow >0){
					entry = i;
					break;
				}
			}
			return entry;
		}
		
		private static int getDivergenceIndex(List<FBBar> list,double squeez,int start){
			int idx =0;
			double startDiv = list.get(start).getUpperBollinger()-list.get(start).getLowerBollinger();
			for (int i =start;i<list.size();i++) {
				if(((list.get(i).getUpperBollinger()-list.get(i).getLowerBollinger())/startDiv) >= 1.15){
					idx = i;
					break;
				}			
			}
			return idx;
		}
		
		
		private static double calcShortTrend(List<FBBar> list,int start){
			double sum=0;
			if(start==0){
				return 0;
			}
			for (int i =start-3;i<=start;i++) {
				sum += list.get(i).close()-list.get(i).getSma();
			}
			Double result =  Util.roundTo3D(sum/4);
			if(Math.abs(result)>0.01){
				return 0;
			}else{
				return result;
			}
		}
		

		
		private static double getMax(List<FBBar> last20){
			List<Double> bolDiff = new ArrayList<Double>();
			for (FBBar bar : last20) {
				bolDiff.add(bar.getUpperBollinger()-bar.getLowerBollinger());
//				double diff =  bar.getUpperBollinger()-bar.getLowerBollinger();
//				System.out.println(bar.getTicker() + "-" + diff);
				}
			
			return Collections.max(bolDiff);
		}
}
