package com.yaowan.server.game.model.data.entity;

public class PushBean {

	private String device_token;
	
	private int device_type;  //设备类型  1：安卓  2：ios  3：其他

	public String getDevice_token() {
		return device_token;
	}

	public void setDevice_token(String device_token) {
		this.device_token = device_token;
	}

	public int getDevice_type() {
		return device_type;
	}

	public void setDevice_type(int device_type) {
		this.device_type = device_type;
	}
	
	
}
