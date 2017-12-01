package com.sesnu.handler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IConnectionHandler;

public class ConnectionHandler implements IConnectionHandler {

	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	
	private boolean connected;
	private boolean ibControllerOff;
	
	@Override
	public void connected() {
		mainL.info("connected");
		connected = true;

	}

	@Override
	public void disconnected() {
		mainL.info("disconnected");

	}

	@Override
	public void accountList(List<String> list) {
		// TODO Auto-generated method stub

	}

	@Override
	public void error(Exception e) {

			if (e.getMessage()!=null && !e.getMessage().equals("Socket closed")){
				mainL.error("Error while connecting to Ib",e);
			}else if(e.toString().equals("java.lang.NullPointerException")){
				ibControllerOff = true;
			}
		
	}

	@Override
	public void message(int id, int msgCode, String msg) {
		mainL.info("Message from IB controller, code: {} , message: {}",msgCode,msg);
	}

	@Override
	public void show(String showStr) {
		mainL.info(showStr);

	}

	public boolean isIbControllerOff(){
		return ibControllerOff;
	}
	
	public boolean isConnected() {
		return connected;
	}

	
}
