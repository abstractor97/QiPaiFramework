package com.yaowan.server.game.model.data.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Table;

/**
 * 更新公告表
 * 
 * @author G_T_C
 */
@Table(name = "game_update_notice", comment = "更新公告表")
public class GameUpdateNotice {

	@Id(strategy = Strategy.IDENTITY)
	@Column(comment = "游戏类型")
	private int gameType;

	@Column(comment = "内容", length = 400)
	private String content;

	@Column(comment = "时间")
	private int time;

	public int getGameType() {
		return gameType;
	}

	public String getContent() {
		return content;
	}

	public int getTime() {
		return time;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setTime(int time) {
		this.time = time;
	}

}
