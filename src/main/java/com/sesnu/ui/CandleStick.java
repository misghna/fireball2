package com.sesnu.ui;


import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.date.DateUtilities;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import com.sesnu.model.FBBar;

	
	
	public class CandleStick  {

		private String candleType;
		
		public CandleStick(String candleType){
			this.candleType=candleType;
		}

		private Map<String,Double> minMax = new HashMap<String,Double>();
	    /**
	     * A demonstration application showing a candlestick chart.
	     *
	     * @param title  the frame title.
	     */
	    public CandleStick() {

	    //    super(title);

	       

	    }

	    /**
	     * Creates a chart.
	     * 
	     * @param dataset  the dataset.
	     * 
	     * @return The dataset.
	     */
	    private JFreeChart createChart(final DefaultHighLowDataset dataset,String ticker) {
	        final JFreeChart chart = ChartFactory.createCandlestickChart(
	            "",
	            "Time", 
	            "price",
	            dataset, 
	            true
	        );
	        double lowestLow = minMax.get(ticker + "_min");
	        double highestHigh =  minMax.get(ticker + "_max");

	        chart.getXYPlot().getRangeAxis().setRange(lowestLow-0.01, highestHigh+0.01);
	        return chart;        
	    }
	    
	    /**
	     * Starting point for the demonstration application.
	     *
	     * @param args  ignored.
	     */
	    public void draw(Map<String,List<FBBar>> barMap) {

	 //       demo.pack();
	        JFrame.setDefaultLookAndFeelDecorated(true);
	        JFrame frame = new JFrame("Candle Sticks");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        int rows = barMap.size()/3+1;
	        frame.setLayout(new GridLayout(4, 3));
	        
//	        JButton b=new JButton("Click Here");  
//	        b.setBounds(50,100,95,30);  
//	        frame.add(b);
	        
	        int i = 0;
	        for (Map.Entry<String, List<FBBar>> entry : barMap.entrySet()) {
	        	i++;
		       if(i<13){
		        	DefaultHighLowDataset dataset = parseToDataSet(entry.getValue(),entry.getKey());
		        //	DefaultHighLowDataset dataset = createHighLowDataset();
			        JFreeChart chart = createChart(dataset,entry.getKey());
			        chart.getXYPlot().setOrientation(PlotOrientation.VERTICAL);
			        ChartPanel chartPanel = new ChartPanel(chart);
			        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
			        frame.add(chartPanel);
		       }
			}

	        System.out.println("total found " + barMap.size());


	        frame.pack();
	        frame.setVisible(true);
	    //    RefineryUtilities.centerFrameOnScreen(demo);
	    //    demo.setVisible(true);

	    }
	    
	    
	    
	    
	    private DefaultHighLowDataset parseToDataSet(List<FBBar> barList,String key){
	        Date[] date = new Date[barList.size()];
	        double[] high = new double[barList.size()];
	        double[] low = new double[barList.size()];
	        double[] open = new double[barList.size()];
	        double[] close = new double[barList.size()];
	        double[] volume = new double[barList.size()];
	        List<Double> max = new ArrayList<Double>();
	        List<Double> min = new ArrayList<Double>();
	        for (int i = 0; i < barList.size();i++) {
	        	FBBar bar = barList.get(i);
	        	if(i>4){
	        		date[i]  = new Date(bar.getStartTime());
	        	}else{
	        		 date[i]  = new Date(barList.get(5).getStartTime()-(5-i)*60000);
	        	}
	        	
	        	volume[i] = bar.getVolume();
	        	switch(candleType){	        	
		        	case "Regular":
			            high[i]  = bar.high();
			            max.add(bar.high());
			            low[i]   = bar.low();
			            min.add(bar.low());
			            open[i]  = bar.open();
			            close[i] = bar.close();			            
			            break;
		        	case "HeikenAshi":
			            high[i]  = bar.getHkaBar().high();
			            max.add(bar.getHkaBar().high());
			            low[i]   = bar.getHkaBar().low();
			            min.add(bar.getHkaBar().low());
			            open[i]  = bar.getHkaBar().open();
			            close[i] = bar.getHkaBar().close();
			            break;    
	        	}

			}
	        minMax.put(key + "_min", Collections.min(min));
	        minMax.put(key + "_max", Collections.max(max));
	        return new DefaultHighLowDataset(key+"(" + barList.get(0).close() + ")", date, high, low, open, close, volume);
	    }
	    


	}


