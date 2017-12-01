package com.sesnu.model;

public class FBPosition {
	
	private final int shares;
	private final double avgCost;
	
	
	public FBPosition(int shares, double avgCost) {
		this.shares = shares;
		this.avgCost = avgCost;
	}
	
	public int getShares() {
		return shares;
	}


	public double getAvgCost() {
		return avgCost;
	}


}
