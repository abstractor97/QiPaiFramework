package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Index;
import com.yaowan.framework.database.annotation.Table;

/**
 * 商城购买日志  
 * @author zane 2016年10月9日 下午9:28:54
 */
@Table(name = "shop_buy_log", comment = "商城购买")
@Index(names = {"rid","item_id"}, indexs = {"rid","item_id"})
public class ShopBuyLog {
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
	 * 消耗类型
	 */
	@Column(comment = "消耗类型")
	private int type;
	
	/**
	 * 商店类型
	 */
	@Column(comment = "商店类型")
	private int shopType;
	
	/**
	 * 消耗数值
	 */
	@Column(comment = "消耗数值")
	private int value;
	/**
	 * 时间
	 */
	@Column(comment = "时间")
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

	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public int getShopType() {
		return shopType;
	}
	public void setShopType(int shopType) {
		this.shopType = shopType;
	}
	
}

