package com.yaowan.server.game.model.struct;

import java.util.ArrayList;
import java.util.List;


/**
 * @author zane
 *
 */
public  class MenjiBill {
	

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
	 * 是否已比牌，1是0不是
	 */
	private int compete;
	
	/**
	 * 玩家名称
	 */
	private String name;
	
	/**
	 * 是否是机器人
	 */
	private boolean isRobot;
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCompete() {
		return compete;
	}

	public void setCompete(int compete) {
		this.compete = compete;
	}

	/**
	 * 花色
	 */
	private List<Integer> handPai = new ArrayList<Integer>();
	

	public List<Integer> getHandPai() {
		return handPai;
	}

	public void setHandPai(List<Integer> handPai) {
		this.handPai = handPai;
	}

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


	public boolean isRobot() {
		return isRobot;
	}


	public void setRobot(boolean isRobot) {
		this.isRobot = isRobot;
	}



	
}
