package com.yaowan.framework.push.model;

/**
 * 推送数据结构
 * 
 */
public class DPushNotification {
	private long rid;//角色ID
	private long extendsionId;
	private String msg = "";
	private int pushTime;
	private boolean isRemove = false;
	private String deviceTokens;
	private int type;
	private String ticker;
	private String title;
	private int eventId;

	public long getRid() {
		return rid;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}

	public long getExtendsionId() {
		return extendsionId;
	}

	public void setExtendsionId(long extendsionId) {
		this.extendsionId = extendsionId;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getPushTime() {
		return pushTime;
	}

	public void setPushTime(int pushTime) {
		this.pushTime = pushTime;
	}

	public boolean getIsRemove() {
		return isRemove;
	}

	public void setIsRemove(boolean isRemove) {
		this.isRemove = isRemove;
	}

	public boolean equals(Object o) {
		if (o instanceof DPushNotification) {
			DPushNotification d = (DPushNotification) o;
			return d.getExtendsionId() == extendsionId && d.eventId == eventId;
		}
		return false;
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public String toString() {
		return String.valueOf(extendsionId) + eventId;
	}
	
	public String getDeviceTokens() {
		return deviceTokens;
	}

	public void setDeviceTokens(String deviceTokens) {
		this.deviceTokens = deviceTokens;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}
}