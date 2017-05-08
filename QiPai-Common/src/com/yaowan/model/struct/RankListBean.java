package com.yaowan.model.struct;


public class RankListBean {
	
	private long rid;

	private String openId;

	private String name;

	private byte head;

	private int isVip;

	private int totalMoney;
	
	private int lastWeekMoney;

	private int changeMoney;

	private int bureauCount;

	public long getRid() {
		return rid;
	}

	public String getOpenId() {
		return openId;
	}

	public String getName() {
		return name;
	}

	public byte getHead() {
		return head;
	}

	public int getIsVip() {
		return isVip;
	}

	public int getTotalMoney() {
		return totalMoney;
	}

	public int getLastWeekMoney() {
		return lastWeekMoney;
	}

	public int getChangeMoney() {
		return changeMoney;
	}

	public int getBureauCount() {
		return bureauCount;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setHead(byte head) {
		this.head = head;
	}

	public void setIsVip(int isVip) {
		this.isVip = isVip;
	}

	public void setTotalMoney(int totalMoney) {
		this.totalMoney = totalMoney;
	}

	public void setLastWeekMoney(int lastWeekMoney) {
		this.lastWeekMoney = lastWeekMoney;
	}

	public void setChangeMoney(int changeMoney) {
		this.changeMoney = changeMoney;
	}

	public void setBureauCount(int bureauCount) {
		this.bureauCount = bureauCount;
	}


	@Override
	public String toString() {
		return "RankListBean [rid=" + rid + ", openId=" + openId + ", name="
				+ name + ", head=" + head + ", isVip=" + isVip
				+ ", totalMoney=" + totalMoney + ", lastWeekMoney="
				+ lastWeekMoney + ", changeMoney=" + changeMoney
				+ ", bureauCount=" + bureauCount 
				+ "]";
	}
}
