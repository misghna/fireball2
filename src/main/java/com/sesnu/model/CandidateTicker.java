package com.sesnu.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity(name="candidate_ticker")
public class CandidateTicker {
	
	
	@Id
	@SequenceGenerator(name="ticker_seq",sequenceName="ticker_seq")
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="ticker_seq")
	private long id;
	
	private String ticker;
	private String date;
	@Column(name="noise_level")
	private double noiseLevel;
	
	
		
	public CandidateTicker(String ticker, String date, double noiseLevel) {
		this.ticker = ticker;
		this.date = date;
		this.noiseLevel = noiseLevel;
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
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public double getNoiseLevel() {
		return noiseLevel;
	}
	public void setNoiseLevel(double noiseLevel) {
		this.noiseLevel = noiseLevel;
	}
	
	

}
