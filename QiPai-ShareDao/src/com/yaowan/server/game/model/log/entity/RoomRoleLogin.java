package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Index;
import com.yaowan.framework.database.annotation.Table;



/**
 * 游戏vip和充值活跃日志
 */
@Table(name = "room_role_login", comment = "游戏vip和充值活跃日志 ")
@Index(names = { "rid", "login_time", "game_type"}, indexs = {
		"rid", "login_time", "game_type"})
public class RoomRoleLogin {
	/**
	 * 角色ID
	 */
	@Column(comment = "角色ID")
	private long rid;
	/**
	 * 登录时间
	 */
	@Column(comment = "登录时间")
	private int loginTime;
	/**
	 * 在线时长(单位：秒)
	 */
	@Column(comment = "在线时长(单位：秒)")
	private int onlineTime;
	/**
	 * 登录ip
	 */
	@Column(length = 15, comment = "登录ip")
	private String ip;
	
	/**
	 * 
	 */
	@Column(comment = "游戏类型 1焖鸡 2斗地主 3麻将")
	private byte gameType;
	
	/**
	 * 
	 */
	@Column(comment = "vip类型 1是vip 0不是")
	private byte userVip;
	
	/**
	 * 
	 */
	@Column(comment = "是否今日充值过 1是 0否")
	private byte todayCharge;
	
	
	public long getRid() {
		return rid;
	}
	public void setRid(long rid) {
		this.rid = rid;
	}
	public int getLoginTime() {
		return loginTime;
	}
	public void setLoginTime(int loginTime) {
		this.loginTime = loginTime;
	}
	public int getOnlineTime() {
		return onlineTime;
	}
	public void setOnlineTime(int onlineTime) {
		this.onlineTime = onlineTime;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public byte getGameType() {
		return gameType;
	}
	public void setGameType(byte gameType) {
		this.gameType = gameType;
	}
	
	public byte getUserVip() {
		return userVip;
	}
	public void setUserVip(byte userVip) {
		this.userVip = userVip;
	}
	public byte getTodayCharge() {
		return todayCharge;
	}
	public void setTodayCharge(byte todayCharge) {
		this.todayCharge = todayCharge;
	}

	
}

