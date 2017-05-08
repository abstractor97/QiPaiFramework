package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Table;

/**
 * 玩家破产统计
 * @author G_T_C
 */
@Table(name = "role_broke_log", comment = "玩家破产统计")
public class RoleBrokeLog {
	
	@Column(comment = "玩家id")
	private long rid;
	
	@Column(comment = "游戏类型")
	private int gameType;
	
	@Column(comment = "房间类型")
	private int roomType;
	
	@Column(comment = "创建时间")
	private int time;

	public long getRid() {
		return rid;
	}

	public int getGameType() {
		return gameType;
	}

	public int getRoomType() {
		return roomType;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public void setRoomType(int roomType) {
		this.roomType = roomType;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}
	
	
	
}
