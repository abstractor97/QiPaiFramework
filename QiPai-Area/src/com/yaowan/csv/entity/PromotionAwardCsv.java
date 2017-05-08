package com.yaowan.csv.entity;

public class PromotionAwardCsv {

	private int id;
	private String shareId;
	private int dailyBindingLimit;
	private String sponsorAward;
	private String recipientReward;
	
	
	public int getID() {
		return id;
	}
	public void setID(int id) {
		this.id = id;
	}
	public String getShareId() {
		return shareId;
	}
	public void setShareId(String shareId) {
		this.shareId = shareId;
	}
	public int getDailyBindingLimit() {
		return dailyBindingLimit;
	}
	public void setDailyBindingLimit(int dailyBindingLimit) {
		this.dailyBindingLimit = dailyBindingLimit;
	}
	public String getSponsorAward() {
		return sponsorAward;
	}
	public void setSponsorAward(String sponsorAward) {
		this.sponsorAward = sponsorAward;
	}
	public String getRecipientReward() {
		return recipientReward;
	}
	public void setRecipientReward(String recipientReward) {
		this.recipientReward = recipientReward;
	}
	
	
}
