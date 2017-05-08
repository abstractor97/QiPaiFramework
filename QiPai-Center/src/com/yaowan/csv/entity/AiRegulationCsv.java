package com.yaowan.csv.entity;

/**
 * ai 盈亏调控
 * 
 * @author G_T_C
 */
public class AiRegulationCsv {

	private int regulationID;//

	private int ascriptionGameId;//归属游戏1.昭通闷鸡2.昭通斗地主3.昭通麻将4.镇雄麻将5.爆笑牛牛

	private int roomID;//房间编号

	private int controlMoney;//真实原始金额（只是用来与当前实际盈亏金钱做比较来调控盈亏的，与AI身上的钱无关）

	private int minimumLine;//最低调控线（万分比）0为无限

	private int highestLine;//最高调控线（万分比）0为无限

	private int drawCardId;//调用发牌机制ID
	
	private float minimumValue;
	
	private float highestValue;

	public int getRegulationID() {
		return regulationID;
	}

	public void setRegulationID(int regulationID) {
		this.regulationID = regulationID;
	}

	public int getAscriptionGameId() {
		return ascriptionGameId;
	}

	public void setAscriptionGameId(int ascriptionGameId) {
		this.ascriptionGameId = ascriptionGameId;
	}

	public int getRoomID() {
		return roomID;
	}

	public void setRoomID(int roomID) {
		this.roomID = roomID;
	}

	public int getControlMoney() {
		return controlMoney;
	}

	public void setControlMoney(int controlMoney) {
		this.controlMoney = controlMoney;
	}

	public int getDrawCardId() {
		return drawCardId;
	}

	public void setDrawCardId(int drawCardId) {
		this.drawCardId = drawCardId;
	}

	public int getMinimumLine() {
		return minimumLine;
	}

	public void setMinimumLine(int minimumLine) {
		this.minimumLine = minimumLine;
	}

	public int getHighestLine() {
		return highestLine;
	}

	public void setHighestLine(int highestLine) {
		this.highestLine = highestLine;
	}

	public float getMinimumValue() {
		return minimumValue;
	}

	public float getHighestValue() {
		return highestValue;
	}

	public void setMinimumValue(float minimumValue) {
		this.minimumValue = minimumValue;
	}

	public void setHighestValue(float highestValue) {
		this.highestValue = highestValue;
	}

	@Override
	public String toString() {
		return "AiRegulationCsv [regulationID=" + regulationID
				+ ", ascriptionGameId=" + ascriptionGameId + ", roomID="
				+ roomID + ", controlMoney=" + controlMoney + ", minimumLine="
				+ minimumLine + ", highestLine=" + highestLine
				+ ", drawCardId=" + drawCardId + ", minimumValue="
				+ minimumValue + ", highestValue=" + highestValue + "]";
	}



 }
