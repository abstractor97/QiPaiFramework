/**
 * Project Name:dfh3_server
 * File Name:SlowLogic.java
 * Package Name:data.log
 * Date:2016年5月24日下午3:56:34
 * Copyright (c) 2016, jiangming@qq.com All Rights Reserved.
 *
*/

package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Index;
import com.yaowan.framework.database.annotation.Table;



/**
 * 慢请求记录 
 * @author jiangming 2016年5月24日
 */
@Table(name = "slow_logic", comment = "慢请求记录")
@Index(names = {"action"}, indexs = {"action"})
public class SlowLogic {
	/**
	 * 自增ID
	 */
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "自增ID")
	private long id;
	/**
	 * action
	 */
	@Column(length = 128, comment = "action")
	private String action;
	/**
	 * 角色ID
	 */
	@Column(comment = "角色ID")
	private long rid;
	/**
	 * 执行时间
	 */
	@Column(comment = "执行时间")
	private long startTime;
	/**
	 * 运行时长
	 */
	@Column(comment = "运行时长")
	private int exeTime;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public long getRid() {
		return rid;
	}
	public void setRid(long rid) {
		this.rid = rid;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public int getExeTime() {
		return exeTime;
	}
	public void setExeTime(int exeTime) {
		this.exeTime = exeTime;
	}
	
}

