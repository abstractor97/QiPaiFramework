package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Table;
import com.yaowan.framework.database.orm.UpdateProperty;

@Table(name = "exchange_log",comment="兑换记录表")
public class ExchangeLog extends UpdateProperty{
	
	@Id(strategy = Strategy.IDENTITY)
	@Column(comment = "我方流水号", length=32)
	private String id;
	
	@Column(comment = "合作方流水号:针对手机充值卡")
	private String serialno = "";
	
	@Column(comment = "用户id")
	private long rid;
	
	@Column(comment = "用户昵称")
	private String nick;
	
	@Column(comment = "充值手机号码/收货的手机号码")
	private String phone = "";
	
	@Column(comment = "收货地址")
	private String address = "";
	
	@Column(comment = "收货人")
	private String consignee = "";
	
	@Column(comment = "状态：0未未处理， 1处理中 2成功 3失败  ")
	private int status;
	
	@Column(comment = "兑换商品id")
	private long exchangeId;
	
	@Column(comment = "物品id")
	private int itemId;
	
	@Column(comment = "兑换时间")
	private int exchangeTime;
	
	@Column(comment = "兑换数量")
	private int exchangeNum;
	
	@Column(comment = "兑换价格")
	private int exchangePrice;
	
	@Column(comment = "使用时间")
	private int useTime;
	
	@Column(comment = "针对充值接口回调情况")
	private String resultInfo = "";
	
	@Column(comment = "针对充值状态报告回调信息")
	private String statusInfo = "";
	
	@Column(comment = "备注")
	private String remark = "";
	
	@Column(comment = "服务器id")
	private int serverId;
	
	@Column(comment = "商品的名称")
	private String itemName;
	
	@Column(comment = "GM后台：商品图标")
	private String iconInfo;


	public String getSerialno() {
		return serialno;
	}

	public long getRid() {
		return rid;
	}

	public String getNick() {
		return nick;
	}

	public String getPhone() {
		return phone;
	}

	public String getAddress() {
		return address;
	}

	public String getConsignee() {
		return consignee;
	}

	public int getStatus() {
		return status;
	}

	public long getExchangeId() {
		return exchangeId;
	}

	public int getExchangeTime() {
		return exchangeTime;
	}

	public int getUseTime() {
		return useTime;
	}

	public String getResultInfo() {
		return resultInfo;
	}

	public String getStatusInfo() {
		return statusInfo;
	}

	public String getRemark() {
		return remark;
	}

	public void setSerialno(String serialno) {
		this.serialno = serialno;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setConsignee(String consignee) {
		this.consignee = consignee;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setExchangeId(long exchangeId) {
		this.exchangeId = exchangeId;
	}

	public void setExchangeTime(int exchangeTime) {
		this.exchangeTime = exchangeTime;
	}

	public void setUseTime(int useTime) {
		this.useTime = useTime;
	}

	public void setResultInfo(String resultInfo) {
		this.resultInfo = resultInfo;
	}

	public void setStatusInfo(String statusInfo) {
		this.statusInfo = statusInfo;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public int getExchangeNum() {
		return exchangeNum;
	}

	public int getExchangePrice() {
		return exchangePrice;
	}

	public void setExchangeNum(int exchangeNum) {
		this.exchangeNum = exchangeNum;
	}

	public void setExchangePrice(int exchangePrice) {
		this.exchangePrice = exchangePrice;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public String getItemName() {
		return itemName;
	}

	public String getIconInfo() {
		return iconInfo;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public void setIconInfo(String iconInfo) {
		this.iconInfo = iconInfo;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	
}
