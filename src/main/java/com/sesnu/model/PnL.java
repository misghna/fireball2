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
@Table(name = "pnl")
public class PnL implements java.io.Serializable {


	private static final long serialVersionUID = 1551260615597988208L;
	private long id;
	private long time;
	private double unrealPnl;
	private double realPnl;
	private Double timeStr;

	public PnL() {
	}

	public PnL(long time, double unrealPnl,double realPnl) {
		this.time=time;
		this.unrealPnl=unrealPnl;
		this.realPnl=realPnl;
		this.timeStr= Util.getDoubleTime(time);
	}

	@Id
	@SequenceGenerator(name="pnl_seq",sequenceName="pnl_seq")
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="pnl_seq")
	public long getId() {
		return this.id;
	}
	

	public void setId(long id) {
		this.id = id;
	}
	

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	@Column(name="unreal_pnl")
	public double getUnrealPnl() {
		return unrealPnl;
	}

	public void setUnrealPnl(double unrealPnl) {
		this.unrealPnl = unrealPnl;
	}

	@Column(name="real_pnl")
	public double getRealPnl() {
		return realPnl;
	}

	public void setRealPnl(double realPnl) {
		this.realPnl = realPnl;
	}
	
	@Column(name="time_str")
	public Double getTimeStr() {
		return timeStr;
	}

	public void setTimeStr(Double timeStr) {
		this.timeStr = timeStr;
	}

	
	

	
}