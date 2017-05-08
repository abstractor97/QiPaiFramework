package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Table;



/**
 * 升级日志
 * 
 *
 */
@Table(name = "role_up_log", comment = "升级日志")
public class RoleUpLog {
	/**
	 * 自增ID
	 */
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "自增ID")
	private long id;
	/**
	 * 玩家角色id
	 */
	@Column(comment = "玩家角色id")
	private long rid;
	/**
	 * 角色昵称
	 */
	@Column(length = 64, comment = "角色昵称")
	private String nick;
	/**
	 * 升到多少级
	 */
	@Column(comment = "升到多少级")
	private int upToLevel;
	/**
	 * 升级时间
	 */
	@Column(comment = "升级时间")
	private int time;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getRid() {
		return rid;
	}
	public void setRid(long rid) {
		this.rid = rid;
	}
	public String getNick() {
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	public int getUpToLevel() {
		return upToLevel;
	}
	public void setUpToLevel(int upToLevel) {
		this.upToLevel = upToLevel;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}

}
