package com.yaowan.framework.push.model;

public class PushPayload {
	private String rid;//角色ID
	private String openId;//平台对应的用户ID
	private String content;//要推送的内容
	
	public String getRid() {
		return rid;
	}
	public void setRid(String rid) {
		this.rid = rid;
	}
	public String getOpenId() {
		return openId;
	}
	public void setOpenId(String openId) {
		this.openId = openId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	@Override
	public String toString() {
		return "PushPayload [rid=" + rid + ", uid=" + openId + ", content=" + content + "]";
	}
}
