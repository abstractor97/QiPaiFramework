package com.yaowan.server.game.model.data.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Table;

@Table(name = "role_lottery_task", comment = "每日任务表")
public class RoleLotteryTask {

	/**
	 * 角色ID
	 */
	@Column(comment = "角色ID")
	private long rid;
	
	/**
	 * 游戏类型
	 */
	@Column( comment = "日常任务类型")
	private int gameType;
	

	@Column(comment = "完成次数")
	private int complementTimes;


	public long getRid() {
		return rid;
	}


	public int getGameType() {
		return gameType;
	}


	public int getComplementTimes() {
		return complementTimes;
	}


	public void setRid(long rid) {
		this.rid = rid;
	}


	public void setGameType(int gameType) {
		this.gameType = gameType;
	}


	public void setComplementTimes(int complementTimes) {
		this.complementTimes = complementTimes;
	}


	
}
