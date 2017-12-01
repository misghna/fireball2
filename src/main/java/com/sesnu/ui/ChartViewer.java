package com.sesnu.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.sesnu.handler.HistoricalHandler;
import com.sesnu.model.CandleType;

//import com.fx.jfree.chart.candlestick.JfreeCandlestickChart;
//import com.fx.jfree.chart.common.FxMarketPxFeeder;


@SuppressWarnings("serial")
public class ChartViewer extends JPanel implements Runnable {

	private ChartController chartController;
	private CandleType candleType;
	private String ticker;
	
	public ChartViewer(ChartController chartController,String ticker){
		this.chartController=chartController;
		this.ticker=ticker;
		
	}
	
    private void createAndShowGUI() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        JFrame frame = new JFrame("Active Trading Monitor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setContentPane(chartController);

        //Disable the resizing feature
        frame.setResizable(false);
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }


	@Override
	public void run() {
		 createAndShowGUI();
		
	}
}
