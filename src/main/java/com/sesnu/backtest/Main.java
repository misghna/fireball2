package com.sesnu.backtest;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;


import com.sesnu.model.FBBar;
import com.sesnu.model.CandlePatternType;
import com.sesnu.ui.CandleStick;
import com.sesnu.ui.MultiLineChart;
import com.sesnu.service.Util;

public class Main {

	static String [] tickers = new String[]{"BAC","XLF","EEM","VALE","T","F","CMCSA","GE","AMD","ABEV","MU","WFC","RAD","C","LBIX","PCG","TEVA","HPQ","X","SNAP","CLF","AKS","GDX","EWZ","UVXY","VXX","RIG","V","HMNY","USO","ESV","BABA","QQQ","S","KR","INTC","UGAZ","AAPL","NKE"};
//	static String [] tickers = new String[]{"ADS","ULTA","CMG","JBT","EDC","RCL","EZJ","MON","GWW","SOXL","MIDD","NVDA","XIV","NFLX","SVXY","CCF","COST","AZO","LRCX","SWKS","TREE","ATHN","JBHT","DPZ","ANTM","LFUS","CHE","MCK","GHC","TECH","STMP","EW","DTO","ODFL","AYI","SIVB","PBYI","PXD","HUM","NSC"}; //"TEVA","SNAP",    ,"TEVA","VRX","M","BAC"
//	static String [] tickers = new String[]{"NKE","AAPL","MU","IWM","AMAT","WMT","VXX","GPS","FOXA","SPLK","JD","SQ"};
	
	static boolean writeResult= false;
	static boolean dailyBollinger = false;
	
	static boolean getData = false;	
	static boolean analyze= false;
	
	
	public static void main(String[] args) throws ClientProtocolException, IOException, NoSuchMethodException,
				SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		int noOfDaysToCollect = 3;
		int interval = 60; // 1 min
		if(getData){
			BarSerializer barSerializer = new BarSerializer();
			for (int i = 0; i < tickers.length; i++) {
				DataCollector dc = new DataCollector();	
				List<FBBar> barList = dc.get(noOfDaysToCollect,interval,tickers[i],dailyBollinger);
				barSerializer.writeToFile(barList, tickers[i], true);
				double progress = (double)(i+1)/(double)tickers.length;
				System.out.println("progress " + Util.roundTo2D(progress)*100 + "%");
			}
			System.out.println(tickers.length + " stocks have been collected!");
		}
		
		Strategies.runStrategy(StrategyTypes.Star, writeResult);
		
		if(analyze){
			Map<String,List<FBBar>>  result = Strategies.runStrategy(StrategyTypes.movingAverage, writeResult);
			if(result.size()>0){
				
					CandleStick cs = new CandleStick("HeikenAshi");
					cs.draw(result);
					
//					MultiLineChart mcs = new MultiLineChart();
//					mcs.draw(result);
				
				}else{
					System.out.println("Nothing to draw!");
				}
		}
	}

	



	
	
}
