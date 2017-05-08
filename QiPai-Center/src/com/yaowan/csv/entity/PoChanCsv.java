package com.yaowan.csv.entity;

public class PoChanCsv {

	private int Id;
	
	private int goldLowerLimit;
	
	private int receiveTime;
	
	private String receiveCD;
	
	private String receiveQuantity;

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}

	public int getGoldLowerLimit() {
		return goldLowerLimit;
	}

	public void setGoldLowerLimit(int goldLowerLimit) {
		this.goldLowerLimit = goldLowerLimit;
	}

	public int getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(int receiveTime) {
		this.receiveTime = receiveTime;
	}

	public String getReceiveCD() {
		return receiveCD;
	}

	public void setReceiveCD(String receiveCD) {
		this.receiveCD = receiveCD;
	}

	public String getReceiveQuantity() {
		return receiveQuantity;
	}

	public void setReceiveQuantity(String receiveQuantity) {
		this.receiveQuantity = receiveQuantity;
	}
	
	
}
