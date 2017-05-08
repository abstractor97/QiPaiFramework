package com.yaowan.csv.entity;

//
public class MajiangZhenXiongRoomCsv {
	/**
	 * 
	 */
	private int roomID;
	/**
	 * 
	 */
	private int potOdds;
	/**
	 * 
	 */
	private int enterLowerLimit;
	
	/**
	 * 
	 */
	private int enterUpperLimit;
	
	/**
	 * 
	 */
	private int round;
	
	/**
	 * 
	 */
	
	private int maxFanShu;
	/**
	 * 
	 */
	private int TurnDuration;//第一轮等待时间
	/**
	 * 
	 */
	private int Turn2Duration;//第二轮等待时间
	
	/**
	 * 
	 */
	private int	raiseOdds;
		
	/**
	 * 
	 */
	private String potLimit;

	
	/**
	 * 
	 */
	private int taxPerGame;
	
	/**
	 * 
	 */
	private int actionDuration;
	
	/**
	 * 可等待次数
	 */
	private int OTPunishment;

	/**
	 * 可等待次数
	 */
	private int exchangeLottery;

	public int getTurn2Duration() {
		return Turn2Duration;
	}

	public void setTurn2Duration(int turn2Duration) {
		Turn2Duration = turn2Duration;
	}

	public int getOTPunishment() {
		return OTPunishment;
	}

	public void setOTPunishment(int oTPunishment) {
		OTPunishment = oTPunishment;
	}

	public int getRoomID() {
		return roomID;
	}

	public void setRoomID(int roomID) {
		this.roomID = roomID;
	}

	public int getPotOdds() {
		return potOdds;
	}

	public void setPotOdds(int potOdds) {
		this.potOdds = potOdds;
	}

	public int getEnterLowerLimit() {
		return enterLowerLimit;
	}

	public void setEnterLowerLimit(int enterLowerLimit) {
		this.enterLowerLimit = enterLowerLimit;
	}

	public int getEnterUpperLimit() {
		return enterUpperLimit;
	}

	public void setEnterUpperLimit(int enterUpperLimit) {
		this.enterUpperLimit = enterUpperLimit;
	}

	public int getTaxPerGame() {
		return taxPerGame;
	}

	public void setTaxPerGame(int taxPerGame) {
		this.taxPerGame = taxPerGame;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public int getTurnDuration() {
		return TurnDuration;
	}

	public void setTurnDuration(int turnDuration) {
		TurnDuration = turnDuration;
	}

	public int getRaiseOdds() {
		return raiseOdds;
	}

	public void setRaiseOdds(int raiseOdds) {
		this.raiseOdds = raiseOdds;
	}

	public String getPotLimit() {
		return potLimit;
	}

	public void setPotLimit(String potLimit) {
		this.potLimit = potLimit;
	}

	public int getActionDuration() {
		return actionDuration;
	}

	public void setActionDuration(int actionDuration) {
		this.actionDuration = actionDuration;
	}

	public int getMaxFanShu() {
		return maxFanShu;
	}

	public void setMaxFanShu(int maxFanShu) {
		this.maxFanShu = maxFanShu;
	}

	public int getExchangeLottery() {
		return exchangeLottery;
	}

	public void setExchangeLottery(int exchangeLottery) {
		this.exchangeLottery = exchangeLottery;
	}
	


}

