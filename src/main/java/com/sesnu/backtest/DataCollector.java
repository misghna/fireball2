package com.sesnu.backtest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sesnu.model.FBBar;
import com.sesnu.service.Indicators;


public class DataCollector {
	
	private final String USER_AGENT = "Mozilla/5.0";
	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	public List<FBBar> get(Integer days,Integer interval,String ticker,boolean dailyBollinger) {
		List<FBBar> barList = new ArrayList<FBBar>();
		try{		
		List<FBBar> dailyBarList = null;
		String url = "https://finance.google.com/finance/getprices?i=" + interval + "&p=" + days + "d&f=d,o,h,l,c,v&df=cpct&q=" + ticker;

		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);

		// add request header
		request.addHeader("User-Agent", USER_AGENT);
		HttpResponse response = client.execute(request);

		if(response.getStatusLine().getStatusCode()!=200){
			return null;
		}

		BufferedReader rd = new BufferedReader(
			new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		Indicators chartCalc = new Indicators();
			Long initTime = 0l;
			while ((line = rd.readLine()) != null) {
				String[] data = line.split(",");
				if(line.indexOf("VOLUME")<0 && data.length>4){
					if(data[0].indexOf("a")>-1){
						initTime = Long.parseLong(data[0].replace("a", ""))*1000;
						data[0]= initTime.toString();
						dailyBarList = new ArrayList<FBBar>();
					}else{
						Long t =  initTime + Integer.parseInt(data[0])*interval*1000;
						data[0] = t.toString();
					}
					
					result.append(line);
					FBBar currentBar = new FBBar(ticker,interval,data);
					currentBar = chartCalc.addIndicators(barList, currentBar);	
//					if(dailyBollinger){
//						currentBar = chartCalc.addIndicators(dailyBarList, currentBar);
//						dailyBarList.add(currentBar);
//					}else{
//						currentBar = chartCalc.addIndicators(barList, currentBar);						
//					}
					barList.add(currentBar);

				//	System.out.println(currentBar.toCSV());
				}
			}
		}catch (Exception e){
			mainL.error("data for {} not found in google historical",ticker);
		}
		return barList;
		
	}

}
 