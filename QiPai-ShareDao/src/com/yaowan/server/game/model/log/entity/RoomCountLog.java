package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Index;
import com.yaowan.framework.database.annotation.Table;



/**
 * 游戏数据统计
 */
@Table(name = "room_count_log", comment = "游戏数据统计 ")
@Index(names = { "create_time", "game_type", "room_type"}, indexs = {
		"create_time", "game_type", "room_type"})
public class RoomCountLog {

	/**
	 * 统计时间
	 */
	@Column(comment = "统计时间")
	private int createTime;
	
	/**
	 * 
	 */
	@Column(comment = "游戏类型 1焖鸡 2斗地主 3麻将")
	private byte gameType;
	
	/**
	 * 
	 */
	@Column(comment = "房间类型 1低级房 2中级 3高级")
	private int roomType;
	
	/**
	 * 
	 */
	@Column(comment = "当日总对战局数")
	private long drawCount;
	
	/**
	 * 
	 */
	@Column(comment = "当日玩家累计破产次数")
	private long brokenCount;
	
	/**
	 * 
	 */
	@Column(comment = "房费")
	private long tax;
	
	/**
	 * 困难闲家破产人数
	 */
	@Column(comment = "困难闲家破产人数")
	private int difficultyPlayerCount;
	
	/**
	 * 普通闲家破产人数
	 */
	@Column(comment = "普通闲家破产人数")
	private int generalPlayerCount;
	
	/**
	 * 庄家破产人数
	 */
	@Column(comment = "困难庄家破产人数")
	private int difficultyBankerCount;
	
	
	/**
	 * 闲家破产人数
	 */
	@Column(comment = "普通庄家破产人数")
	private int generalBankerCount;
	
	/**
	 * 抽水
	 */
	@Column(comment = "抽水")
	private int choushui;


	public int getCreateTime() {
		return createTime;
	}

	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}

	public byte getGameType() {
		return gameType;
	}

	public void setGameType(byte gameType) {
		this.gameType = gameType;
	}

	public int getRoomType() {
		return roomType;
	}

	public void setRoomType(int roomType) {
		this.roomType = roomType;
	}

	public long getDrawCount() {
		return drawCount;
	}

	public void setDrawCount(long drawCount) {
		this.drawCount = drawCount;
	}

	public long getBrokenCount() {
		return brokenCount;
	}

	public void setBrokenCount(long brokenCount) {
		this.brokenCount = brokenCount;
	}

	public long getTax() {
		return tax;
	}

	public void setTax(long tax) {
		this.tax = tax;
	}

	public int getDifficultyPlayerCount() {
		return difficultyPlayerCount;
	}

	public void setDifficultyPlayerCount(int difficultyPlayerCount) {
		this.difficultyPlayerCount = difficultyPlayerCount;
	}

	public int getGeneralPlayerCount() {
		return generalPlayerCount;
	}

	public void setGeneralPlayerCount(int generalPlayerCount) {
		this.generalPlayerCount = generalPlayerCount;
	}

	public int getDifficultyBankerCount() {
		return difficultyBankerCount;
	}

	public void setDifficultyBankerCount(int difficultyBankerCount) {
		this.difficultyBankerCount = difficultyBankerCount;
	}

	public int getGeneralBankerCount() {
		return generalBankerCount;
	}

	public void setGeneralBankerCount(int generalBankerCount) {
		this.generalBankerCount = generalBankerCount;
	}

	public int getChoushui() {
		return choushui;
	}

	public void setChoushui(int choushui) {
		this.choushui = choushui;
	}

	
	
}

