package com.sesnu.ui;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.TextAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sesnu.model.CandlePatternType;
import com.sesnu.model.FBPosition;
import com.sesnu.model.MiniBar;
import com.sesnu.service.Portofolio;
import com.sesnu.service.Util;

public class RenkoChart extends ApplicationFrame implements Runnable {

	private static final Logger mainL = LoggerFactory.getLogger("MainLog");

  private static final int maxRenkoBoxsDisplayed =25;
  private CategoryDataset dataset;
  private CategoryPlot plot;
  private ValueMarker currentPrice;
  private ValueMarker inPrice;
  private ValueMarker stpPrice;
  private String ticker;
  private Double initBoxSize;
  private JFreeChart chart;
  
  public RenkoChart(String ticker,double boxSize) {
  super("Renko Bar chart");
  this.ticker=ticker;
  this.initBoxSize = boxSize;
  
  dataset = createDataset();
  final JFreeChart chart = createChart(dataset);
  final ChartPanel chartPanel = new ChartPanel(chart);
  chartPanel.setPreferredSize(new java.awt.Dimension(900, 350));
  chartPanel.setHorizontalAxisTrace(true);
  chartPanel.setVerticalAxisTrace(true);
  setContentPane(chartPanel);
  }

  private CategoryDataset createDataset() {
  double[][] data = new double[][]{
	  {0},
	  {0},
	  {0} 
  };
  
  
  return DatasetUtilities.createCategoryDataset("S", "T", data);
  
  }
  
  private JFreeChart createChart(final CategoryDataset dataset) {

  final String title = ticker + "(" + initBoxSize + ")";
  chart = ChartFactory.createStackedBarChart(
		  title, "", "Price",
  dataset, PlotOrientation.VERTICAL, true, true, false);

  chart.setBackgroundPaint(Color.white);
  

  plot = chart.getCategoryPlot();
 // Color c = new 
  plot.getRenderer().setSeriesPaint(0, new Color(1f,0f,0f,0f));
  plot.getRenderer().setSeriesPaint(1, new Color(0, 202, 121,95));
  plot.getRenderer().setSeriesPaint(2, new Color(255, 0, 0,95));

  
  currentPrice =  new ValueMarker(0, Color.RED, new BasicStroke(1.0f));
  currentPrice.setLabel("");
  plot.addRangeMarker(currentPrice);
  
  Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
  inPrice =   new ValueMarker(0, Color.blue, dashed);
  plot.addRangeMarker(inPrice);
  
  stpPrice =   new ValueMarker(0, Color.blue, dashed);
  plot.addRangeMarker(stpPrice);
  
  ValueAxis yAxis = plot.getRangeAxis();
  yAxis.setRange(1, 10);

  Font font3 = new Font("xAxis", Font.PLAIN, 8); 
  plot.getDomainAxis().setTickLabelFont(font3);
  
  Font font1 = new Font("xAxis", Font.PLAIN, 12); 
  plot.getDomainAxis().setLabelFont(font1);
  plot.getRangeAxis().setLabelFont(font1);
  plot.getRangeAxis().setTickLabelFont(font3);
  
  
  plot.setDomainGridlinesVisible(true);
  plot.setDomainGridlinePaint(Color.lightGray);
  plot.setRangeGridlinePaint(Color.lightGray);
  plot.setBackgroundPaint(Color.white);
  return chart;
  }

  public void run() {
	  try{
		  this.pack();
		  RefineryUtilities.centerFrameOnScreen(this);
		  this.setVisible(true);
	  }catch(Exception e){
		  mainL.error("Error while updating renko chart ",e);
	  }
	  
  }
  
  public void updateRenkoChart(List<MiniBar> barList){
	  List<Double> lowList = new ArrayList<Double>();
	  List<Double> highList = new ArrayList<Double>();
	  if(barList.size()<2)return;
	  List<MiniBar> subList=null;
	  if(barList.size()>maxRenkoBoxsDisplayed){
		  subList = new ArrayList(barList.subList(barList.size()-maxRenkoBoxsDisplayed, barList.size()));
	  }else{
		  subList = new ArrayList(barList);
	  }

	  
	  DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	  
	  Integer date = 0;
	  for (int i=0; i< subList.size(); i++) {
		  MiniBar bar = subList.get(i);
		  
		  String time = getTime(bar.getTime());
		  Integer newDate = getDate(bar.getTime());
		  if(!newDate.equals(date)){
			  time = newDate + "/" + time;
			  date = newDate;
		  }
		  double boxSize = bar.getBodyHeight();
		  double bear =bar.getCandleType().equals(CandlePatternType.BEARISH)?boxSize:0;
		  double bull =bar.getCandleType().equals(CandlePatternType.BULLISH)?boxSize:0;
		  dataset.addValue(bar.low(), " ", time.toString());
		  dataset.addValue(bull, "Up", time.toString());
		  dataset.addValue(bear, "Down", time.toString());
		  
		  
		  lowList.add(bar.low());
		  highList.add(bar.high());
	  }
	  
	  ValueAxis yAxis = plot.getRangeAxis();
	  double minAxis = Util.roundTo3D(Collections.min(lowList) - 0.5);
	  double maxAxis = Util.roundTo3D(Collections.max(highList) + 0.5);
	  yAxis.setRange(minAxis, maxAxis);
	  
	  plot.setDataset(dataset);
  }
  
  
	private Integer getDate(long time){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("America/New_York"));
		calendar.setTimeInMillis(time);
		Integer date = calendar.get(Calendar.DATE);
		return date;
	}
  
	private String getTime(long time){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("America/New_York"));
		calendar.setTimeInMillis(time);
		Integer hr = calendar.get(Calendar.HOUR_OF_DAY);
        Integer min = calendar.get(Calendar.MINUTE);
        Integer sec = calendar.get(Calendar.SECOND);
        String result =  hr + ":" + min;
        if(!sec.equals(0)){
        	result = result + "." + sec;
        }
        return result;
	}
	
  public synchronized void movePriceLine(Double price,Double instProfit){
		  plot.removeRangeMarker(currentPrice);
		  currentPrice = 
		          new ValueMarker(price, Color.RED, new BasicStroke(1.0f));
		  currentPrice.setLabel(price.toString()  + " (" +  instProfit + ")");
		  currentPrice.setLabelPaint(Color.blue);
		  plot.addRangeMarker(currentPrice);
		  currentPrice.setLabelTextAnchor(TextAnchor.TOP_LEFT);		  
  }
  
  public void insertStpPrice(Double price){
	  price = Util.roundTo2D(price);
	  plot.removeRangeMarker(stpPrice);
	  Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
	  stpPrice = 
	          new ValueMarker(Util.roundTo2D(price), Color.blue, dashed);
	  stpPrice.setLabel("STP -> " + price);

	  stpPrice.setLabelPaint(Color.red);
	  stpPrice.setLabelTextAnchor(TextAnchor.TOP_LEFT);
	  plot.addRangeMarker(stpPrice);	 
  }
  
  public void insertInPrice(FBPosition pos){
	  plot.removeRangeMarker(inPrice);
	  Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
	  inPrice = 
	          new ValueMarker(Util.roundTo2D(pos.getAvgCost()), Color.blue, dashed);
	  if(pos.getShares()>0){
		  inPrice.setLabel("LONG -> " + Util.roundTo2D(pos.getAvgCost()) + " (" +  pos.getShares() + ")");
	  }else{
		  inPrice.setLabel("SHORT -> " + Util.roundTo2D(pos.getAvgCost())  + " (" +  pos.getShares() + ")");
	  }
	  inPrice.setLabelPaint(Color.red);
//	  inPrice.setLabelAnchor(RectangleAnchor.TOP);
	  inPrice.setLabelTextAnchor(TextAnchor.TOP_LEFT);
	  plot.addRangeMarker(inPrice);	 

  }
  
  
}