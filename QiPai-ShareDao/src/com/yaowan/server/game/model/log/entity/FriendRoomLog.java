package com.yaowan.server.game.model.log.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Table;

/**
 * 货币日志 
 * 
 */
@Table(name = "friendroom_log", comment = "好友房日志")
public class FriendRoomLog {
	/**
	 * 自增id
	 */
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "自增id")
	private long id;
	
	/**
	 * 游戏类型
	 */
	@Column(comment = "游戏类型")
	private int gameType;
	
	/**
	 * 当日时间
	 */
	@Column(comment = "当日时间")
	private int dayTime;
	
	/**
	 * 付费类型
	 */
	@Column(comment = "付费类型(1是房卡,2是钻石)")
	private int payType;
	
	/**
	 * 轮数
	 */
	@Column(comment = "轮数")
	private int round;
	
	/**
	 * 房间类型
	 */
	@Column(comment = "房间类型")
	private int roomType;
	
	/**
	 * 消耗的数量
	 */
	@Column(comment = "消耗的数量")
	private int consumeNum;
	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getGameType() {
		return gameType;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public int getDayTime() {
		return dayTime;
	}

	public void setDayTime(int dayTime) {
		this.dayTime = dayTime;
	}

	public int getPayType() {
		return payType;
	}

	public void setPayType(int payType) {
		this.payType = payType;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public int getRoomType() {
		return roomType;
	}

	public void setRoomType(int roomType) {
		this.roomType = roomType;
	}

	public int getConsumeNum() {
		return consumeNum;
	}

	public void setConsumeNum(int consumeNum) {
		this.consumeNum = consumeNum;
	}

	
	
	
		
}
