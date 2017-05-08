package com.yaowan.framework.push.channel;

import com.yaowan.framework.push.model.PushPayload;
import com.yaowan.framework.push.model.PushResult;

public abstract class AbstractChannel {
	
	private int channelType;
	
	public AbstractChannel(int channelType) {
		this.channelType = channelType;
	}
	/**
	 * 把信息推送到平台服务器
	 * @param msg
	 */
	public abstract PushResult push(PushPayload payload);
	/**
	 * 根据用户信息清除平台对应信息
	 * @param msg
	 */
	public abstract PushResult clean(PushPayload payload);
	
	public int getChannelType() {
		return channelType;
	}
}
