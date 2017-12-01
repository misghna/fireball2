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
@Table(name = "acct_history")
public class AcctHistory implements java.io.Serializable {


	private static final long serialVersionUID = 1551260615597988208L;
	private long id;
	private long time;
	private double amount;
	private String acctNo;

	public AcctHistory() {
	}

	public AcctHistory(long time, double amount,String acctNo) {
		this.time=time;
		this.amount=amount;
		this.acctNo=acctNo;
	}

	@Id
	@SequenceGenerator(name="act_seq",sequenceName="act_seq")
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="act_seq")
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

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	@Column(name="acct_no")
	public String getAcctNo() {
		return acctNo;
	}

	public void setAcctNo(String acctNo) {
		this.acctNo = acctNo;
	}

	

	
}