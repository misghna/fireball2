package com.sesnu.handler;

import java.util.ArrayList;
import java.util.List;

import com.ib.client.ContractDetails;
import com.ib.controller.ApiController.IScannerHandler;
import com.sesnu.service.SmartScanner;

public class ScannerHandler implements IScannerHandler {

	private List<String> tickersList = new ArrayList<String>();
	private boolean done;
	
	public ScannerHandler(){
		tickersList = new ArrayList<String>();
	}
	
	@Override
	public void scannerParameters(String xml) {

	}

	@Override
	public void scannerData(int rank, ContractDetails contractDetails, String legsStr) {
		if(contractDetails.contract().symbol()!=null && contractDetails.contract().symbol().indexOf(" ")<0 &&
				!tickersList.contains(contractDetails.contract()) 
				&& !contractDetails.contract().symbol().isEmpty()){
		
			tickersList.add(contractDetails.contract().symbol());
		
		}
	}

	@Override
	public void scannerDataEnd() {
		this.done=true;
	}

	
	public boolean isDone(){
		return this.done;
	}
	
	public List<String> getScannResult(){
		return this.tickersList;
	}
}
