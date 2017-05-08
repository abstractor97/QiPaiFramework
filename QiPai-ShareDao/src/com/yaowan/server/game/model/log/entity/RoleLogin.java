/**
 * Project Name:dfh3_server
 * File Name:RoleLogin.java
 * Package Name:data.log
 * Date:2016年5月24日下午3:32:01
 * Copyright (c) 2016, jiangming@qq.com All Rights Reserved.
 *
*/

package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Index;
import com.yaowan.framework.database.annotation.Table;



/**
 * 登陆日志 
 */
@Table(name = "role_login", comment = "登陆日志 ")
@Index(names = {"rid","login_time"}, indexs = {"rid","login_time"})
public class RoleLogin {
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
	@Column(length = 50, comment = "登录ip")
	private String ip;
	
	/**
	 * 设备码
	 */
	@Column(length = 100, comment = "imei")
	private String device;
	
	@Column(comment = "登录方式 1 游客， 2手机，3微信 ，4 qq")
	private byte loginType; 
	
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}
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
	
	public byte getLoginType() {
		return loginType;
	}
	public void setLoginType(byte loginType) {
		this.loginType = loginType;
	}
	
	
}

