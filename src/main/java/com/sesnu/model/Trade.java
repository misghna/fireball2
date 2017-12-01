package com.sesnu.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.sesnu.service.Util;


@Entity
@Table(name = "trade")
public class Trade {
	
	@Id
	@SequenceGenerator(name="trade_seq",sequenceName="trade_seq")
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="trade_seq")
	private long id;
	private String ticker;
	private long time;
	
	@Column(name="init_price")
	private Double initPrice;
	
	@Column(name="avg_price")
	private Double avgPrice;
	
	private int shares;
	private String action;
	private String type;
	
	@Column(name="order_id")
	private Integer orderId;
	
	@Column(name="noise_level")
	private Double noiseLevel;
	
	@Column(name="open_order_id")
	private Integer openOrderId;
	
	private String reason;
	
	private String strategy;
	
	@Column(name="trade_time")
	private Double tradeTime;
	
	private String mode;
	
	public Trade(){}
	
	public Trade(Integer orderId,String ticker, Long time,String action, 
			String type, Double initPrice,Integer shares,Double noiseLevel,
			Integer openOrderId,String reason,String strategy){
		this.ticker = ticker;
		this.time = time;
		this.action = action;
		this.type = type;
		this.orderId = orderId;
		this.initPrice=initPrice;
		this.shares=shares;
		this.noiseLevel=noiseLevel;
		this.openOrderId=openOrderId;
		this.reason=reason;
		this.strategy=strategy;
		this.tradeTime = Util.getDoubleTime(time);
		this.mode= Util.getMode();
		if(Util.getMode().equals("DEV-MODE")){
			this.avgPrice=initPrice;
		}
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public Double getInitPrice() {
		return initPrice;
	}

	public void setInitPrice(Double initPrice) {
		this.initPrice = initPrice;
	}

	public Double getAvgPrice() {
		return avgPrice;
	}

	public void setAvgPrice(Double avgPrice) {
		this.avgPrice = avgPrice;
	}

	public int getShares() {
		return shares;
	}

	public void setShares(int shares) {
		this.shares = shares;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public Double getNoiseLevel() {
		return noiseLevel;
	}

	public void setNoiseLevel(Double noiseLevel) {
		this.noiseLevel = noiseLevel;
	}

	public Integer getOpenOrderId() {
		return openOrderId;
	}

	public void setOpenOrderId(Integer openOrderId) {
		this.openOrderId = openOrderId;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

	public Double getTradeTime() {
		return tradeTime;
	}

	public void setTradeTime(Double tradeTime) {
		this.tradeTime = tradeTime;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}


	
	

}
