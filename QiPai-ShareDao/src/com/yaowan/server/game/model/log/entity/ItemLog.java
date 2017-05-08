package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Index;
import com.yaowan.framework.database.annotation.Table;

/**
 * 物品日志  
 * @author zane 2016年10月9日 下午9:28:54
 */
@Table(name = "item_log", comment = "物品日志")
@Index(names = {"rid","event"}, indexs = {"rid","event"})
public class ItemLog {
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
	 * 物品id
	 */
	@Column(comment = "物品id")
	private int itemId;
	/**
	 * 物品数量
	 */
	@Column(comment = "物品数量")
	private int itemNum;
	/**
	 * 物品事件
	 */
	@Column(comment = "物品事件")
	private int event;
	/**
	 * 时间
	 */
	@Column(comment = "时间")
	private int time;
	
	@Column(comment = "1 产出， 2消耗")
	private byte type;
	
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
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	public int getItemNum() {
		return itemNum;
	}
	public void setItemNum(int itemNum) {
		this.itemNum = itemNum;
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
	public byte getType() {
		return type;
	}
	public void setType(byte type) {
		this.type = type;
	}
	public long getBeforeValue() {
		return beforeValue;
	}
	public long getAfterValue() {
		return afterValue;
	}
	public void setBeforeValue(long beforeValue) {
		this.beforeValue = beforeValue;
	}
	public void setAfterValue(long afterValue) {
		this.afterValue = afterValue;
	}
	
	
}

