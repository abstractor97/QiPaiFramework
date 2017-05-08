
package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Index;
import com.yaowan.framework.database.annotation.Table;
import com.yaowan.framework.database.orm.UpdateProperty;



/**
 * 充值
 * 
 */
@Table(name = "charge", comment = "充值 ")
@Index(names = {"rid","time"}, indexs = {"rid","time"})
public class Charge extends UpdateProperty{
	/**
	 * 
	 */
	@Id
	@Column(comment = "在要玩平台生成订单id")
	private String id;
	/**
	 * 玩家id
	 */
	@Column(comment = "玩家id")
	private long rid;

	/**
	 * yaowan
	 */
	@Column(comment = "yaowan")
	private String platform;
	
	/**
	 * 渠道id
	 */
	@Column(comment = "渠道id")
	private int channel;
	
	/**
	 * 游戏服id
	 */
	@Column(comment = "游戏服id")
	private int serverId;
	
	/**
	 * 充值活动类型
	 */
	@Column(comment = "充值活动类型")
	private int type;
	
	
	/**
	 * 充值人民币
	 */
	@Column(comment = "充值人民币")
	private int rmb;
	
	@Column(comment = "充值状态")
	private byte status;
	/**
	 * 充值钻石
	 */
	@Column(comment = "充值钻石")
	private long value;
	/**
	 * 渠道订单id
	 */
	@Column(comment = "渠道订单id")
	private int orderId;
	/**
	 * 记录时间
	 */
	@Column(comment = "记录时间")
	private int time;
	
	
	@Column(comment = "是否破产")
	private byte isEmpty;
	
	@Column(comment = "U8平台对应的订单编号")
	private String u8OrderId;
	
	public byte getIsEmpty() {
		return isEmpty;
	}
	public void setIsEmpty(byte isEmpty) {
		this.isEmpty = isEmpty;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getRid() {
		return rid;
	}
	public void setRid(long rid) {
		this.rid = rid;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}

	public int getRmb() {
		return rmb;
	}
	public void setRmb(int rmb) {
		this.rmb = rmb;
	}
	public byte getStatus() {
		return status;
	}
	public void setStatus(byte status) {
		this.status = status;
	}
	public long getValue() {
		return value;
	}
	public void setValue(long value) {
		this.value = value;
	}
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public int getChannel() {
		return channel;
	}
	public void setChannel(int channel) {
		this.channel = channel;
	}
	public int getServerId() {
		return serverId;
	}
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
	public String getU8OrderId() {
		return u8OrderId;
	}
	public void setU8OrderId(String u8OrderId) {
		this.u8OrderId = u8OrderId;
	}
	
	
	
}

