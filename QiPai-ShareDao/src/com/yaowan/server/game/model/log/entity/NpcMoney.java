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
@Table(name = "npc_money", comment = "货币日志 ")
@Index(names = {"rid","time"}, indexs = {"rid","time"})
public class NpcMoney {
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
	@Column(comment = "货币类型(金币:1,钻石:2,水晶:3)")
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
	@Column(comment = "事件前盈亏线")
	private long beforeGainOrLoss;

	public long getId() {
		return id;
	}

	public long getRid() {
		return rid;
	}

	public byte getType() {
		return type;
	}

	public long getValue() {
		return value;
	}

	public int getEvent() {
		return event;
	}

	public int getTime() {
		return time;
	}

	public long getBeforeGainOrLoss() {
		return beforeGainOrLoss;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public void setEvent(int event) {
		this.event = event;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public void setBeforeGainOrLoss(long beforeGainOrLoss) {
		this.beforeGainOrLoss = beforeGainOrLoss;
	}
	
	
}
