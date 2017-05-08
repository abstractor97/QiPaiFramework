

package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Index;
import com.yaowan.framework.database.annotation.Table;



/**
 * 角色创建日志 
 * @author zane 2016年10月11日 下午12:53:39
 */
@Table(name = "register_log", comment = "角色创建日志  ")
@Index(names = {"rid","time","nick"}, indexs = {"rid","time","nick"})
public class RegisterLog {
	/**
	 * 角色id
	 */
	@Id
	@Column(comment = "角色id")
	private long rid;
	/**
	 * 创号时间
	 */
	@Column(comment = "创号时间")
	private int time;
	/**
	 * 角色名
	 */
	@Column(length = 64, comment = "角色名")
	private String nick;
	/**
	 * 创号设备
	 */
	@Column(length = 128, comment = "创号设备")
	private String device;
	/**
	 * 创号ip
	 */
	@Column(length = 15, comment = "创号ip")
	private String ip;
	
	@Column(comment="注册平台")
	private String platform;
	
	public long getRid() {
		return rid;
	}
	public void setRid(long rid) {
		this.rid = rid;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public String getNick() {
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	
}

