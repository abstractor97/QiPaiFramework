package com.yaowan.csv.entity;

import java.util.List;

import com.yaowan.framework.util.StringUtil;

//
public class NiuniuRoomCsv {
	/**
	 * 

	 */
	private int Id;
	/**
	 * 
	 */
	private int RoomLv;
	
	/**
	 * 
	 */
	private int RoomType;
	
	/**
	 * 
	 */
	private int Max;
	
	/**
	 * 
	 */
	private int PlayerMax;
	
	
	/**
	 * 
	 */
	private int EnterLowerLimit;
	
	/**
	 * 
	 */
	private int QwnerEnterLowerLimit;
	
	/**
	 * 输抽水
	 */
	private int TaxPerGame;
	
	
	/**
	 * 
	 */
	private int OpeningCountDown;
	/**
	 * 
	 */
	private int BetCountDown;
	/**
	 * 
	 */
	private String BetValue;
	
	/**
	 * 
	 */
	private int PleaseLeave;
	
	/**
	 * 
	 */
	private int Hp;
	
	/**
	 * 
	 */
	private int OwnerMax;
	
	/**
	 * 
	 */
	private List<Integer> betValueList;
	
	/**
	 * 庄家赢抽水
	 */
	private int BankerTaxPerGame;
	
	/**
	 *  闲家赢抽水
	 */
	private int FreeTaxPerGame;
	


	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}

	public int getRoomLv() {
		return RoomLv;
	}

	public void setRoomLv(int roomLv) {
		RoomLv = roomLv;
	}

	public int getRoomType() {
		return RoomType;
	}

	public void setRoomType(int roomType) {
		RoomType = roomType;
	}

	public int getMax() {
		return Max;
	}

	public void setMax(int max) {
		Max = max;
	}

	public int getPlayerMax() {
		return PlayerMax;
	}

	public void setPlayerMax(int playerMax) {
		PlayerMax = playerMax;
	}

	public int getEnterLowerLimit() {
		return EnterLowerLimit;
	}

	public void setEnterLowerLimit(int enterLowerLimit) {
		EnterLowerLimit = enterLowerLimit;
	}

	public int getQwnerEnterLowerLimit() {
		return QwnerEnterLowerLimit;
	}

	public void setQwnerEnterLowerLimit(int qwnerEnterLowerLimit) {
		QwnerEnterLowerLimit = qwnerEnterLowerLimit;
	}

	public int getTaxPerGame() {
		return TaxPerGame;
	}

	public void setTaxPerGame(int taxPerGame) {
		TaxPerGame = taxPerGame;
	}

	public int getOpeningCountDown() {
		return OpeningCountDown;
	}

	public void setOpeningCountDown(int openingCountDown) {
		OpeningCountDown = openingCountDown;
	}

	public int getBetCountDown() {
		return BetCountDown;
	}

	public void setBetCountDown(int betCountDown) {
		BetCountDown = betCountDown;
	}

	public String getBetValue() {
		return BetValue;
	}

	public void setBetValue(String betValue) {
		BetValue = betValue;
	}

	public int getPleaseLeave() {
		return PleaseLeave;
	}

	public void setPleaseLeave(int pleaseLeave) {
		PleaseLeave = pleaseLeave;
	}

	public int getHp() {
		return Hp;
	}

	public void setHp(int hp) {
		Hp = hp;
	}

	public List<Integer> getBetValueList() {
		if (betValueList == null) {
			betValueList = StringUtil.stringToList(BetValue, "|", Integer.class);
		}
		return betValueList;
	}

	public void setBetValueList(List<Integer> betValueList) {
		this.betValueList = betValueList;
	}

	public int getBankerTaxPerGame() {
		return BankerTaxPerGame;
	}

	public void setBankerTaxPerGame(int bankerTaxPerGame) {
		BankerTaxPerGame = bankerTaxPerGame;
	}

	public int getFreeTaxPerGame() {
		return FreeTaxPerGame;
	}

	public void setFreeTaxPerGame(int freeTaxPerGame) {
		FreeTaxPerGame = freeTaxPerGame;
	}

	public int getOwnerMax() {
		return OwnerMax;
	}

	public void setOwnerMax(int ownerMax) {
		OwnerMax = ownerMax;
	}




}

