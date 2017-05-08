package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Table;
import com.yaowan.framework.database.annotation.Id.Strategy;

/**
 * 礼物赠送流水日志
 * 
 * @author lijintao 2017年2月24日
 */

@Table(name = "gift_log", comment = "礼物日志")
public class GiftLog {
	
	/**
	 * 自增ID
	 */
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "自增ID")
	private int id;
	
	/**
	 * 购买者ID
	 */
	@Column(comment = "购买者ID")
	private long buyerId;
	
	/**
	 * 接受者ID
	 */
	@Column(comment = "接受者ID")
	private long recipientId;
	
	/**
	 * 物品ID
	 */
	@Column(comment = "物品ID")
	private int itemId;
	
	/**
	 * 获得物品的货币类型
	 */
	@Column(comment = "获得物品的货币类型")
	private int getTheCurrencyType;
	
	/**
	 * 购买物品的货币类型
	 */
	@Column(comment = "购买物品的货币类型")
	private int buyerCurrencyType;
	
	/**
	 * 道具购买价格
	 */
	@Column(comment = "道具购买价格")
	private int price;
	
	/**
	 * 道具获得价格
	 */
	@Column(comment = "道具获得价格")
	private int giftPrice;
	
	/**
	 * 道具差价
	 */
	@Column(comment = "道具差价")
	private int difference;
	
	/**
	 * 事件发生时间
	 */
	@Column(comment = "事件发生时间")
	private int eventTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getBuyerId() {
		return buyerId;
	}

	public void setBuyerId(long buyerId) {
		this.buyerId = buyerId;
	}

	public long getRecipientId() {
		return recipientId;
	}

	public void setRecipientId(long recipientId) {
		this.recipientId = recipientId;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getGetTheCurrencyType() {
		return getTheCurrencyType;
	}

	public void setGetTheCurrencyType(int getTheCurrencyType) {
		this.getTheCurrencyType = getTheCurrencyType;
	}

	public int getBuyerCurrencyType() {
		return buyerCurrencyType;
	}

	public void setBuyerCurrencyType(int buyerCurrencyType) {
		this.buyerCurrencyType = buyerCurrencyType;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getGiftPrice() {
		return giftPrice;
	}

	public void setGiftPrice(int giftPrice) {
		this.giftPrice = giftPrice;
	}

	public int getDifference() {
		return difference;
	}

	public void setDifference(int difference) {
		this.difference = difference;
	}

	public int getEventTime() {
		return eventTime;
	}

	public void setEventTime(int eventTime) {
		this.eventTime = eventTime;
	}
	
}
