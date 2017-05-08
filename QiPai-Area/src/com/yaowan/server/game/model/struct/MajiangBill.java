package com.yaowan.server.game.model.struct;

import java.util.ArrayList;
import java.util.List;

import com.yaowan.protobuf.game.GMahJong.WinType;


/**
 * @author zane
 *
 */
public  class MajiangBill {
	

	/**
	 * 
	 */
	private long rid;
	
	
	/**
	 * 座位
	 */
	private int seat;
	
	/**
	 * 金币
	 */
	private int gold;
	
	/**
	 * 番种类
	 */
	private int winTimes;
	
	/**
	 * 番类型
	 */
	private List<WinType> winTypes = new ArrayList<WinType>();

	public long getRid() {
		return rid;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}

	public int getWinTimes() {
		return winTimes;
	}

	public void setWinTimes(int winTimes) {
		this.winTimes = winTimes;
	}


	public List<WinType> getWinTypes() {
		return winTypes;
	}

	public void setWinTypes(List<WinType> winTypes) {
		this.winTypes = winTypes;
	}


	
}
