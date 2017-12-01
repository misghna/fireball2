package com.sesnu.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.sesnu.model.FBBar;
import com.sesnu.service.Util;


public class MultiLineChart{


	private Map<String,Double> minMax = new HashMap<String,Double>();
	
	private Map<Integer,XYSeries> closeMap = new HashMap<Integer,XYSeries>();
	private Map<Integer,XYSeries> bolUpMap = new HashMap<Integer,XYSeries>();
	private Map<Integer,XYSeries> bolLowMap = new HashMap<Integer,XYSeries>();
	private List<Map<String, List<FBBar>>> groupedData;
	private Map<Integer, JFreeChart> chartMap = new HashMap<Integer, JFreeChart>();
	private int loadedDataIndex=0;
	
	public MultiLineChart() {
	     //   super("XY Line Chart Example with JFreechart");
	 

	    }
	 
	 
//	    private JButton next() {
//	        final JButton auto = new JButton(new AbstractAction("Next ->") {
//
//	            @Override
//	            public void actionPerformed(ActionEvent e) {
//	             //   chartPanel.restoreAutoBounds();
//	            	System.out.println("Nexr");
//	            }
//	        });
//	        return auto;
//	    }
//	    
//	    private JButton prev() {
//	        final JButton auto = new JButton(new AbstractAction("<- Prev") {
//
//	            @Override
//	            public void actionPerformed(ActionEvent e) {
//	             //   chartPanel.restoreAutoBounds();
//	            	System.out.println("Prev");
//	            }
//	        });
//	        return auto;
//	    }
	    
	 private void addMouseList(JPanel panel,int i,String key){
		 panel.addMouseListener(new MouseAdapter() {
		     @Override
		     public void mousePressed(MouseEvent e) {
		    	 if(i==1){
		    		 clearChart();
		    	 }
		    	 
		    	 if(i==0 && loadedDataIndex>0){
		    		 loadNewData(-1,key);
			         loadedDataIndex --;
		    	 }else if(i==11 && loadedDataIndex<groupedData.size()-1){
		    		 loadNewData(1,key);
			         loadedDataIndex ++;
		    	 }
		     }
		  });
	 }
	 
	 
	 private void loadNewData(int direction,String ticker){
	        Map<String, List<FBBar>> barMap = groupedData.get(loadedDataIndex+direction);
	        int j=0;
	        for (Map.Entry<String, List<FBBar>> entry : barMap.entrySet()) {
	        	XYSeries  closeData = closeMap.get(j);
	        	XYSeries  bolUpData = bolUpMap.get(j);
	        	XYSeries  bolLowData = bolLowMap.get(j);
	        	List<Double> closePrice = new ArrayList<Double>();
	        	int counter =0;
	        	for (FBBar bar : entry.getValue()) {
		        	Date d = new Date(bar.getStartTime());	        			
		        	double time = Util.roundTo2D(Double.parseDouble(d.getHours() + "." + d.getMinutes()));				        	
		        	closeData.add(counter,bar.close());			        	
		        	bolUpData.add(counter,bar.getUpperBollinger());				        	
		        	bolLowData.add(counter,bar.getLowerBollinger());
		        	closePrice.add(bar.getUpperBollinger());
		        	closePrice.add(bar.getLowerBollinger());
		        	counter ++;
		        	
				}
	        	JFreeChart fChart = chartMap.get(j);
	        	fChart.getXYPlot().getRangeAxis().
	        		setRange(Collections.min(closePrice)-0.01, Collections.max(closePrice)+0.01);
	        	fChart.setTitle(entry.getKey() + "(" + entry.getValue().get(20).close()  +")");
		        j++;
	        }
	 }
	 
	 
	 private void clearChart(){
    	 int allLoadedChartsCount = closeMap.size();
    	 for (int j = 0; j < allLoadedChartsCount; j++) {
	    	 XYSeries  closeData = closeMap.get(j);
	    	 int recordCount = closeData.getItemCount();
	    	 for (int k = 0; k < recordCount; k++) {
	    		 closeData.remove(0);
			 }
	    	 XYSeries  bolUpData = bolUpMap.get(j);
	    	 for (int k = 0; k < recordCount; k++) {
	    		 bolUpData.remove(0);
			 }
	    	 XYSeries  bolLowData = bolLowMap.get(j);
	    	 for (int k = 0; k < recordCount; k++) {
	    		 bolLowData.remove(0);
			 }
		 }
	 }
	 
	 
	    public void draw(Map<String,List<FBBar>> fullBarMap) {
	    	groupData(fullBarMap);
	    	
	    	JFrame.setDefaultLookAndFeelDecorated(true);
	        JFrame frame = new JFrame("Bollinger squeez line charts (total " + fullBarMap.size() + ")");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        frame.setLayout(new GridLayout(3, 4));
	        
	        int i = 0;
	        
	        Map<String, List<FBBar>> barMap = groupedData.get(0);
	        for (Map.Entry<String, List<FBBar>> entry : barMap.entrySet()) {	        	
		    	   JPanel chartPanel = createChartPanel(entry.getValue(),entry.getKey(),i);
		    	   addMouseList(chartPanel,i,entry.getKey());
		    	   frame.add(chartPanel, BorderLayout.CENTER);
		    	   frame.add(chartPanel);
		    	   i++;
	        }
	 
	        
//	        setSize(640, 480);
//	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        frame.setLocationRelativeTo(null);
	        
	        
	       
	        frame.pack();
	        frame.setVisible(true);
	        
	        
//	        SwingUtilities.invokeLater(new Runnable() {
//	            @Override
//	            public void run() {
//	                new MultiLineChart().setVisible(true);
//	            }
//	        });
	    }
	    
	    
	    
	    private XYDataset createDataset(List<FBBar> barList,String key,Integer i) {
	    	XYSeriesCollection dataset = new XYSeriesCollection();
	    	
	        XYSeries close = new XYSeries("close");
	        XYSeries bolUp = new XYSeries("Bol-Up");
	        XYSeries bolLow = new XYSeries("Bol-Low");
	        List<Double> closePrice = new ArrayList<Double>();
	        Calendar calendar = Calendar.getInstance();
	        int counter =0;
	        for (FBBar bar : barList) {
	     //   	Date d = new Date(bar.getStartTime());	        			
	     //   	double time = Util.roundTo2D(Double.parseDouble(d.getHours() + "." + d.getMinutes()));

	        	calendar.setTimeInMillis(bar.getStartTime());
//	        	int hours = calendar.get(Calendar.HOUR_OF_DAY);
	        	int minutes = calendar.get(Calendar.MINUTE);
	        	close.add(counter,bar.close());
	        	bolUp.add(counter,bar.getUpperBollinger());
	        	bolLow.add(counter,bar.getLowerBollinger());
	        	closePrice.add(bar.getUpperBollinger());
	        	closePrice.add(bar.getLowerBollinger());
	        	
//	        	bolUp.add(counter,bar.getEmaSlow());
//	        	bolLow.add(counter,bar.getEmaFast());
//	        	closePrice.add(bar.getEmaSlow());
//	        	closePrice.add(bar.getEmaFast());
//	        	closePrice.add(bar.close());
	        	
	        	counter++;
			}

	        minMax.put(key + "_min", Collections.min(closePrice));
	        minMax.put(key + "_max", Collections.max(closePrice));
	        dataset.addSeries(close);
	        dataset.addSeries(bolUp);
	        dataset.addSeries(bolLow);
	        closeMap.put(i, close);
	        bolUpMap.put(i, bolUp);
	        bolLowMap.put(i, bolLow);
	        
	        return dataset;
	    }
	    
	    private JPanel createChartPanel(List<FBBar> barList,String key,Integer i) {
	        String chartTitle = key + "(" + barList.get(0).close()  +")";
//	        String chartTitle = key + "(" + barList.size()  +")";
	        String xAxisLabel = "time";
	        String yAxisLabel = "price";
	        
	        XYDataset dataset = createDataset(barList,key,i);
	     
	        JFreeChart chart = ChartFactory.createXYLineChart(chartTitle,
	                xAxisLabel, yAxisLabel, dataset,PlotOrientation.VERTICAL,true,true,false);
	       
	        double lowestLow = minMax.get(key + "_min");
	        double highestHigh =  minMax.get(key + "_max");

	        chart.getXYPlot().getRangeAxis().setRange(lowestLow-0.01, highestHigh+0.01);
	        chartMap.put(i, chart);
	        return new ChartPanel(chart);
	    }
	    
	    
	    private void groupData(Map<String, List<FBBar>> fullData){
	    	groupedData = new ArrayList<Map<String, List<FBBar>>>();
	    	Map<String, List<FBBar>> map = new HashMap<String, List<FBBar>>();

	    	for (Map.Entry<String, List<FBBar>> entry : fullData.entrySet()) {
	    		map.put(entry.getKey(), entry.getValue());
	    		if(map.size()==12){
	    			groupedData.add(map);
	    			 map = new HashMap<String, List<FBBar>>();
	    		}
		     }
	    	if(map.size()>0){
	    		groupedData.add(map);
	    	}

	    }
}
