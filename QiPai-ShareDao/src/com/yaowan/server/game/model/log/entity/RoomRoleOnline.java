/**
 * Project Name:dfh3_server
 * File Name:RoleOnline.java
 * Package Name:data.log
 * Date:2016年5月24日下午3:50:42
 * Copyright (c) 2016, jiangming@qq.com All Rights Reserved.
 *
*/

package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Index;
import com.yaowan.framework.database.annotation.Table;



/**
 * 游戏房间在线人数
 */
@Table(name = "room_role_online", comment = "游戏房间在线人数 ")
@Index(names = {"date_time","game_type"}, indexs = {"date_time","game_type"})
public class RoomRoleOnline {
	/**
	 * 记录时间
	 */
	@Column(comment = "记录时间")
	private int dateTime;
	/**
	 * 在线人数
	 */
	@Column(comment = "在线人数")
	private int num;
	
	/**
	 * 
	 */
	@Column(comment = "游戏类型 1焖鸡 2斗地主 3麻将")
	private int gameType;
	
	
	
	public int getDateTime() {
		return dateTime;
	}
	public void setDateTime(int dateTime) {
		this.dateTime = dateTime;
	}
	public int getNum() {
		return num;
	}
	public int getGameType() {
		return gameType;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

}

