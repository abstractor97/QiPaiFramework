package com.yaowan.server.game.model.data.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Table;
import com.yaowan.framework.database.orm.UpdateProperty;

/**
 * 兑换表
 * 
 * @author G_T_C
 */
@Table(name = "exchange")
public class Exchange extends UpdateProperty {

	@Id(strategy = Strategy.AUTO)
	@Column(comment = "兑换id")
	private long id;

	@Column(comment = "1.游戏道具 2.实物", length = 2)
	private int goodType;

	@Column(comment = "序号，数字最小，排序最前")
	private int itemOrder;

	@Column(comment = "商品的名称")
	private String itemName;

	@Column(comment = "兑换物品的类型")
	private int exchangeType;

	@Column(comment = "需要消耗的对应兑换物品数量")
	private int itemPrice;

	@Column(comment = "购买道具的ID")
	private int itemId;

	@Column(comment = "道具库存数量")
	private int stock;

	@Column(comment = "限购天数：隔多少天才能兑换")
	private int quotaDay;

	@Column(comment = "一次购买行为获得的道具数量")
	private int oneQuotaNum;

	@Column(comment = "数量： 针对如果是金币或者钻石就有值")
	private int quantity = 0;

	@Column(comment = "兑换开始时间")
	private int startTime;

	@Column(comment = "兑换结束时间")
	private int endTime;

	@Column(comment = "商品的注释，用于折扣显示，道具介绍等")
	private String itemInfo;

	@Column(comment = "GM后台：商品图标")
	private String iconInfo;
	
	@Column(comment = "服务器id")
	private int serverId;
	
	@Column(comment = "删除标记，1为删除了，0为未删除")
	private byte delFlag ;
	
	public long getId() {
		return id;
	}

	public int getGoodType() {
		return goodType;
	}

	public int getItemOrder() {
		return itemOrder;
	}

	public String getItemName() {
		return itemName;
	}

	public int getExchangeType() {
		return exchangeType;
	}

	public int getItemPrice() {
		return itemPrice;
	}

	public int getItemId() {
		return itemId;
	}

	public int getStock() {
		return stock;
	}

	public int getQuotaDay() {
		return quotaDay;
	}

	public int getOneQuotaNum() {
		return oneQuotaNum;
	}

	public int getQuantity() {
		return quantity;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	public String getItemInfo() {
		return itemInfo;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setGoodType(int goodType) {
		this.goodType = goodType;
	}

	public void setItemOrder(int itemOrder) {
		this.itemOrder = itemOrder;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public void setExchangeType(int exchangeType) {
		this.exchangeType = exchangeType;
	}

	public void setItemPrice(int itemPrice) {
		this.itemPrice = itemPrice;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}

	public void setQuotaDay(int quotaDay) {
		this.quotaDay = quotaDay;
	}

	public void setOneQuotaNum(int oneQuotaNum) {
		this.oneQuotaNum = oneQuotaNum;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public void setItemInfo(String itemInfo) {
		this.itemInfo = itemInfo;
	}

	public String getIconInfo() {
		return iconInfo;
	}

	public void setIconInfo(String iconInfo) {
		this.iconInfo = iconInfo;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public byte getDelFlag() {
		return delFlag;
	}

	public void setDelFlag(byte delFlag) {
		this.delFlag = delFlag;
	}

	
}
