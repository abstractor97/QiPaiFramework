/**
 * Project Name:dfh3_server
 * File Name:Money.java
 * Package Name:data.log
 * Date:2016年5月24日下午2:59:12
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
 * 货币日志 
 * 
 */
@Table(name = "money", comment = "货币日志 ")
@Index(names = {"rid","time"}, indexs = {"rid","time"})
public class Money {
	/**
	 * 自增id
	 */
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "自增id")
	private long id;
	/**
	 * 玩家id
	 */
	@Column(comment = "玩家id")
	private long rid;
	/**
	 * 
	 */
	@Column(comment = "货币类型(金币:1,钻石:2,水晶:3,)")
	private byte type;
	/**
	 * 数量
	 */
	@Column(comment = "数量")
	private long value;
	/**
	 * 事件
	 */
	@Column(comment = "事件")
	private int event;
	/**
	 * 记录时间
	 */
	@Column(comment = "记录时间")
	private int time;
	
	/**
	 * 事件前数量
	 */
	@Column(comment = "事件前数量")
	private long beforeValue;
	
	/**
	 * 事件后数量
	 */
	@Column(comment = "事件后数量")
	private long afterValue;
	
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
	public byte getType() {
		return type;
	}
	public void setType(byte type) {
		this.type = type;
	}
	public long getValue() {
		return value;
	}
	public void setValue(long value) {
		this.value = value;
	}
	public int getEvent() {
		return event;
	}
	public void setEvent(int event) {
		this.event = event;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public long getBeforeValue() {
		return beforeValue;
	}
	public void setBeforeValue(long beforeValue) {
		this.beforeValue = beforeValue;
	}
	public long getAfterValue() {
		return afterValue;
	}
	public void setAfterValue(long afterValue) {
		this.afterValue = afterValue;
	}

	
}

