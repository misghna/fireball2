package com.sesnu.service;

import com.ib.controller.Position;

public class Portofolio {
	
	private double avgPrice;
	private int pos;
	private double realizedPnl;
	private double unrealizedPnl;
	
	
	public Portofolio(Position pos) {
		if(pos!=null){
			this.avgPrice = pos.averageCost();
			this.pos = (int) pos.position();
			this.realizedPnl= pos.realPnl();
			this.unrealizedPnl = pos.unrealPnl();
		}
	}

	public double getAvgPrice() {
		return avgPrice;
	}
	
	public void setAvgPrice(double avgPrice) {
		this.avgPrice = avgPrice;
	}
	
	public int getPos() {
		return pos;
	}

	public double getRealizedPnl() {
		return realizedPnl;
	}

	public void setRealizedPnl(double realizedPnl) {
		this.realizedPnl = realizedPnl;
	}

	public double getUnrealizedPnl() {
		return unrealizedPnl;
	}

	public void setUnrealizedPnl(double unrealizedPnl) {
		this.unrealizedPnl = unrealizedPnl;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}
		

}
