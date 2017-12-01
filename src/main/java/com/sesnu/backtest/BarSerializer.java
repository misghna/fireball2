package com.sesnu.backtest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.sesnu.model.FBBar;
import com.sesnu.model.MiniBar;
import com.sesnu.service.Indicators;
import com.sesnu.service.Util;
import com.sesnu.ui.RenkoChart;

public class BarSerializer {
	
	
	private static String dataPath = "/Users/mgebreki/Documents/fireball_doc/";
	private static String deserDate = "Oct7";
	private static double BAR_SIZE = .05;
	private static int DAYS = 10;
	
	public void writeToFile(List<FBBar> data, String fileName,boolean overWrite){
		String date = new Date().toLocaleString().split(",")[0].replace(" ", "");
		BufferedWriter bw = null;
		FileWriter fw = null;

		try {
			File theDir = new File(dataPath + date);
			if (!theDir.exists()) {
			        theDir.mkdir();
			}
			fw = new FileWriter(dataPath + date + "/" + fileName + ".csv",!overWrite);			
			bw = new BufferedWriter(fw);
			for (FBBar bar : data) {
				bw.write(bar.toCSV() + "\n");
			}
			

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}
		}	
//		System.out.println("Bars are serialized!");
}

public Map<String,List<FBBar>> deSerStock(String folderName,int maxRowsPerTicker){
	
		Map<String,List<FBBar>> barMap = new HashMap<String,List<FBBar>>();
		BufferedReader br = null;
		FileReader fr = null;
		Indicators indicator = new Indicators();
		try {
			File folder = new File(dataPath + folderName);
			if(!folder.exists() || folder.listFiles().length<1){
				return null;
			}
			
			File[] listOfFiles = folder.listFiles();

			List<FBBar> barList =null;List<MiniBar> renkoList =null;
			System.out.println("Loading and adding technical indicators ...");
			System.out.println("ticker,boxSize,AvgBlockCount,block/day,curveCount,Avg curve/day,DollarValue,profitRatio,dubCount,spread");
			for (File f : listOfFiles) {
				if(!f.getName().endsWith(".DS_Store")){
					String ticker = f.getName().split("\\.")[0];
					String sCurrentLine;
					br = new BufferedReader(new FileReader(f));
					barList = new ArrayList<FBBar>();
					renkoList = new ArrayList<MiniBar>();
					float reported =0f;
					Map<Double,Integer> heightFreq = new HashMap<Double,Integer>();
					
					while ((sCurrentLine = br.readLine()) != null ) { // && barList.size() < maxRowsPerTicker
						if(sCurrentLine.indexOf("Symbol")==-1){
//							FBBar bar = new FBBar(sCurrentLine,"eoddata");
							FBBar bar = new FBBar(sCurrentLine);
							double height = Util.roundTo2D(bar.getBodyHeight());
							Integer count = 0;
							if(heightFreq.containsKey(height)){
								count = heightFreq.get(height);
							}
							heightFreq.put(height, count+1);
							bar = indicator.addIndicators(barList, bar);					
							barList.add(bar);
							float progress = (float) ((float)barList.size()/62000.0*100);
							if(progress%10==0 && progress!=reported){
								System.out.println("progress " + progress + "%");
								reported= progress;
							}
						}
					}
					heightFreq = Util.sortByValue(heightFreq);
					double avgSpread = 0;
					for (Map.Entry<Double, Integer> entry : heightFreq.entrySet()) {
						if(entry.getValue()*100/heightFreq.size()>90){
							avgSpread = entry.getKey();
							break;
						}
					}
//					analyseRenko(barList,avgSpread);
//					System.out.println("*********** " + ticker + " **************");
//					analyseRenko(renkoList);
					barMap.put(barList.get(0).getTicker(), barList);
					Integer count = barList==null?0:barList.size();
//					System.out.println("total data count " +  count.toString());
				}
				
			}
			
		} catch (IOException e) {e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
				if (fr != null)
					fr.close();
			} catch (IOException ex) {ex.printStackTrace();
			}
		}

		return barMap;
	}

	private List<Double> boxSizeList = Arrays.asList(new Double[]{1.5,1.0,0.5,0.3,0.1,0.05});
//	private List<Double> boxSizeList = Arrays.asList(new Double[]{0.05});
	
	private void analyseRenko(List<FBBar> list,double spread ){
		Indicators indicator = new Indicators();
		double boxSize = spread*2;
		if(boxSize ==0.0)return;
		String ticker = list.get(0).getTicker();
	//	System.out.println("Ticker " + ticker + " totalBarCount " + list.size() + " boxSize -> " + boxSize);
		
//		for (Double barSize : boxSizeList) {
			List<MiniBar> renkoList = new ArrayList<MiniBar>();
			for (FBBar fbBar : list) {
				indicator.addRenkoOC(renkoList, fbBar, boxSize,null);
			}
			
//			Draw Renko chart
//			RenkoChart renko = new RenkoChart(list.get(0).getTicker());
//			new Thread(renko).start();
//			renko.updateRenkoChart(renkoList);
			
			int curveCount =0;int dubCount =0;
			for (int i=1;i<renkoList.size();i++) {
				if(!renkoList.get(i-1).getCandleType().equals(renkoList.get(i).getCandleType())){
					curveCount ++;
				}
				if(renkoList.get(i-1).getTime()==renkoList.get(i).getTime())dubCount ++;
			}
			int avgCurvePerDay = curveCount/DAYS;
			double profiteDollar = Util.roundTo2D((renkoList.size()-curveCount*2) * boxSize);
			double priftRatio = (renkoList.size() - curveCount *2)*100/renkoList.size();
			Integer blockPerDay = renkoList.size()/DAYS;
			if(avgCurvePerDay>10 && priftRatio>75){
				String report = ticker  + "," + boxSize + "," + renkoList.size() + "," + 
						blockPerDay + "," + curveCount + "," +  avgCurvePerDay + "," + profiteDollar + "," +
						priftRatio + "," + dubCount + "," + spread;
				System.out.println(report);
			}
//		}			
	}

	private static float getDate(long time){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("EST"));
		calendar.setTimeInMillis(time);
		int date = calendar.get(Calendar.DATE);
        return date;
	}
	
	public static Map<String,List<FBBar>> deSerFinam(){
		
		Map<String,List<FBBar>> barMap = new HashMap<String,List<FBBar>>();
		BufferedReader br = null;
		FileReader fr = null;
		Indicators indicator = new Indicators();
		try {
			File folder = new File(dataPath + "finam");
			if(!folder.exists() || folder.listFiles().length<1){
				return null;
			}
			
			File[] listOfFiles = folder.listFiles();
			for (File f : listOfFiles) {
				if(!f.getName().endsWith(".DS_Store")){
					String sCurrentLine;
					br = new BufferedReader(new FileReader(f));
					List<FBBar> barList = new ArrayList<FBBar>();
					while ((sCurrentLine = br.readLine()) != null && barList.size()<5000) {
						if(sCurrentLine.indexOf(">")==-1){
							FBBar bar = new FBBar(sCurrentLine);
							bar = indicator.addIndicators(barList, bar);					
							barList.add(bar);
						}
					}
					barMap.put(barList.get(0).getTicker(), barList);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
				if (fr != null)
					fr.close();
			} catch (IOException ex) 
			{ex.printStackTrace();
			}
		}
	
		return barMap;
	}
	
	public static void main (String [] args){
		deSerFinam();
	}

}
