package com.sesnu.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.jfree.ui.RectangleInsets;

import com.sesnu.model.CandleType;
import com.sesnu.model.FBBar;
import com.sesnu.model.MiniBar;


@SuppressWarnings("serial")
public class ChartController extends JPanel {

	private List<Long> addedPerios;
	
	private static final DateFormat READABLE_TIME_FORMAT = new SimpleDateFormat("kk:mm:ss");

	private OHLCSeries ohlcSeries;
	private TimeSeries volumeSeries;
	private TimeSeries stochasticKSeries;
	private TimeSeries stochasticDSeries;
	private TimeSeriesCollection volumeDataset;
	private TimeSeries upperBollingerSeries;
	private TimeSeries lowerBollingerSeries;
	private TimeSeries smaSeries;
	private CombinedDomainXYPlot mainPlot;
	private ChartPanel chartPanel;
	

	public ChartController(String title) {
		addedPerios = new ArrayList<Long>();
		// Create new chart
		final JFreeChart candlestickChart = createChart(title);
		candlestickChart.setBackgroundPaint( Color.DARK_GRAY);
		// Create new chart panel
		chartPanel = new ChartPanel(candlestickChart);
		chartPanel.setPreferredSize(new java.awt.Dimension(900, 300));
        chartPanel.setHorizontalAxisTrace(true);
        chartPanel.setVerticalAxisTrace(true);
		// Enable zooming
		chartPanel.setMouseZoomable(true);
		chartPanel.setMouseWheelEnabled(true);
//		chartPanel.setBackground(Color.DARK_GRAY);
//		chartPanel.setBorder(BorderFactory.createEmptyBorder(100, 100, 100, 100));
	      //RectangleInsets chartRectangle = new RectangleInsets(TOP,LEFT,BOTTOM,RIGHT);
		
		add(chartPanel, BorderLayout.CENTER);
	}

	public void addMargin(JFreeChart jChart){
	      RectangleInsets chartRectangle = new RectangleInsets(28F,30F,30F,30F);
	      //RectangleInsets chartRectangle = new RectangleInsets(TOP,LEFT,BOTTOM,RIGHT);
	      jChart.setPadding(chartRectangle);
	}
	
	private JFreeChart createChart(String chartTitle) {

		/**
		 * Creating candlestick subplot
		 */
		// Create OHLCSeriesCollection as a price dataset for candlestick chart
		OHLCSeriesCollection candlestickDataset = new OHLCSeriesCollection();
		ohlcSeries = new OHLCSeries("Price");
		candlestickDataset.addSeries(ohlcSeries);
		// Create candlestick chart priceAxis
		NumberAxis priceAxis = new NumberAxis("Price");
		priceAxis.setAutoRangeIncludesZero(false);
		priceAxis.setTickLabelPaint(Color.LIGHT_GRAY);
		// Create candlestick chart renderer
		CustomCandlestickRenderer candlestickRenderer = new CustomCandlestickRenderer(CustomCandlestickRenderer.WIDTHMETHOD_AVERAGE,
				false, new CustomHighLowItemLabelGenerator(new SimpleDateFormat("kk:mm"), new DecimalFormat("0.000")));
		// Create candlestickSubplot
		candlestickRenderer.setCandleWidth(10);
		
		XYPlot candlestickSubplot = new XYPlot(candlestickDataset, null, priceAxis, candlestickRenderer);
		candlestickSubplot.setBackgroundPaint(Color.black);
		upperBollingerSeries = addSeries(candlestickSubplot,"Upper Bol");
		lowerBollingerSeries = addSeries(candlestickSubplot,"Lower Bol");
		smaSeries = addSeries(candlestickSubplot,"SMA");
		

		
		/**
		 * Creating volume subplot
		 */
		// creates TimeSeriesCollection as a volume dataset for volume chart
	//	TimeSeriesCollection 
		volumeDataset = new TimeSeriesCollection();
		volumeSeries = new TimeSeries("Volume");
		volumeDataset.addSeries(volumeSeries);
		// Create volume chart volumeAxis
		NumberAxis volumeAxis = new NumberAxis("Volume");
		volumeAxis.setTickLabelPaint(Color.LIGHT_GRAY);
		volumeAxis.setAutoRangeIncludesZero(false);
		// Set to no decimal
		volumeAxis.setNumberFormatOverride(new DecimalFormat("0"));
		// Create volume chart renderer
		XYBarRenderer timeRenderer = new XYBarRenderer(-2);
		timeRenderer.setShadowVisible(false);
		
		timeRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("Volume--> Time={1} Size={2}",
				new SimpleDateFormat("kk:mm"), new DecimalFormat("0")));
		// Create volumeSubplot
		XYPlot volumeSubplot = new XYPlot(volumeDataset, null, volumeAxis, timeRenderer);
		volumeSubplot.setBackgroundPaint(Color.black);
		enableCrosshair(volumeSubplot);
		
		/**
		 * Creating stochastic subplot
		 */
		// creates TimeSeriesCollection as a stochastic dataset for stochastic chart
		TimeSeriesCollection stochasticDataset = new TimeSeriesCollection();
		stochasticKSeries = new TimeSeries("%k");
		stochasticDataset.addSeries(stochasticKSeries);
		// Create stochastic chart stochasticAxis
		NumberAxis stochasticAxis = new NumberAxis("Stochastic");
		stochasticAxis.setTickLabelPaint(Color.LIGHT_GRAY);
		stochasticAxis.setRange(0, 100);
		stochasticAxis.setTickUnit(new NumberTickUnit(50));
		stochasticAxis.setAutoRangeIncludesZero(false);
		// Set to no decimal
		stochasticAxis.setNumberFormatOverride(new DecimalFormat("0"));
		// Create stochastic chart renderer
		XYSplineRenderer stchTimeRenderer = new XYSplineRenderer();
		stchTimeRenderer.setBaseShapesVisible(false);
		stchTimeRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("Volume--> Time={1} Size={2}",
				new SimpleDateFormat("kk:mm"), new DecimalFormat("0")));
		// Create stochasticSubplot
		XYPlot stochasticSubplot = new XYPlot(stochasticDataset, null, stochasticAxis, stchTimeRenderer);
		stochasticSubplot.setBackgroundPaint(Color.black);
		stochasticDSeries = addSeries(stochasticSubplot,"% D");
		enableCrosshair(stochasticSubplot);

		
		/**
		 * Create chart main plot with two subplots (candlestickSubplot,
		 * volumeSubplot) and one common dateAxis
		 */
		// Creating charts common dateAxis
		DateAxis dateAxis = new DateAxis("Time");
		dateAxis.setTickLabelPaint(Color.LIGHT_GRAY);
		dateAxis.setDateFormatOverride(new SimpleDateFormat("kk:mm"));
		// reduce the default left/right margin from 0.05 to 0.02
		dateAxis.setLowerMargin(0.02);
		dateAxis.setUpperMargin(0.02);
		dateAxis.setTimeZone(TimeZone.getTimeZone("America/New_York"));
		// Create mainPlot
		CombinedDomainXYPlot mainPlot = new CombinedDomainXYPlot(dateAxis);
		mainPlot.setGap(10.0);
		mainPlot.add(candlestickSubplot, 3);
		mainPlot.add(volumeSubplot, 1);
		mainPlot.add(stochasticSubplot, 1);
		mainPlot.setOrientation(PlotOrientation.VERTICAL);
		
		enableCrosshair(mainPlot);
		
		JFreeChart chart = new JFreeChart(chartTitle, JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true);		
		chart.removeLegend();
        final XYPlot plot = chart.getXYPlot();
        ValueAxis xAxis = plot.getDomainAxis();
        xAxis.setAutoRange(true);
        xAxis.setFixedAutoRange(3600000.0);  // 60 seconds
        
		return chart;
	}
	
	private void enableCrosshair(XYPlot plot){
//		plot.setDomainCrosshairVisible(true);
//		plot.setDomainCrosshairLockedOnData(false);
//		plot.setRangeCrosshairVisible(true);
//		plot.setRangeCrosshairLockedOnData(false);
	}
	
	private TimeSeries addSeries(XYPlot plot,String name){
		TimeSeriesCollection overaySeries = new TimeSeriesCollection();
		TimeSeries series = new TimeSeries(name);       
        overaySeries.addSeries(series);       
        int index = 1;
        plot.setDataset(plot.getRendererCount(), overaySeries);
        plot.mapDatasetToRangeAxis(index, 0);
        XYSplineRenderer renderer = new XYSplineRenderer();
        plot.setRenderer(plot.getRendererCount(), renderer);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        renderer.setBaseShapesVisible(false);
        enableCrosshair(plot);
        
        return series;
	}

	
	public void updateCandle(FBBar bar,CandleType candleType,double noise) {
		try{
			FixedMillisecond t = new FixedMillisecond(bar.getStartTime());
			if(addedPerios.contains(bar.getStartTime())){
				System.out.println("time already exists " + bar.getStartTime());
				
				return;
			}
					volumeSeries.addOrUpdate(t, bar.getVolume());
					stochasticKSeries.addOrUpdate(t,bar.getStcPerK());
					stochasticDSeries.addOrUpdate(t,bar.getStcPerD());
//					upperBollingerSeries.add(t,bar.getUpperBollinger()); //upper bol
//					lowerBollingerSeries.add(t,bar.getLowerBollinger()); // lower bol
//					smaSeries.add(t,bar.getSma());  // sma
					upperBollingerSeries.addOrUpdate(t,bar.getEmaSlow()+noise); 
					lowerBollingerSeries.addOrUpdate(t,bar.getEmaSlow()-noise); 
					smaSeries.addOrUpdate(t,bar.getEmaSlow());  
							
				if(candleType.equals(CandleType.HeikenAshi)){												
						ohlcSeries.add(t, bar.getHkaBar().open(), bar.getHkaBar().high(), bar.getHkaBar().low(), bar.getHkaBar().close());					
				}else if(candleType.equals(CandleType.Regular)){
						ohlcSeries.add(t, bar.open(), bar.high(), bar.low(), bar.close());
				}
			
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void updateRenko(MiniBar bar,CandleType candleType) {
		try{
			FixedMillisecond t = new FixedMillisecond(bar.getTime());
			if(addedPerios.contains(bar.getTime())){
				System.out.println("time already exists " + bar.getTime());
				
				return;
			}
//					volumeSeries.addOrUpdate(t, bar.getVolume());
//					stochasticKSeries.addOrUpdate(t,bar.getStcPerK());
//					stochasticDSeries.addOrUpdate(t,bar.getStcPerD());
//					upperBollingerSeries.add(t,bar.getUpperBollinger()); //upper bol
//					lowerBollingerSeries.add(t,bar.getLowerBollinger()); // lower bol
//					smaSeries.add(t,bar.getSma());  // sma
//					upperBollingerSeries.addOrUpdate(t,bar.getEmaFast()); 
//					lowerBollingerSeries.addOrUpdate(t,bar.getEmaMedium()); 
//					smaSeries.addOrUpdate(t,bar.getEmaSlow());  
							
				if(candleType.equals(CandleType.HeikenAshi)){												
//						ohlcSeries.add(t, bar.getHkaBar().open(), bar.getHkaBar().high(), bar.getHkaBar().low(), bar.getHkaBar().close());					
				}else{
//					MiniBar hBar = bar.getHkaBar();
						ohlcSeries.add(t, bar.open(), bar.high(), bar.low(), bar.close());
				}
			
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	

}
