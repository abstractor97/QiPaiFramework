package com.yaowan.push;

import java.util.HashMap;
import java.util.Map;

import com.yaowan.framework.push.channel.AbstractChannel;
import com.yaowan.framework.push.channel.ChannelType;
import com.yaowan.framework.push.model.PushPayload;
import com.yaowan.framework.push.model.PushResult;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.util.MD5Util;

public class YaowanChannel extends AbstractChannel {

	public YaowanChannel() {
		super(ChannelType.YAOWAN);
	}

	@Override
	public PushResult push(PushPayload payload) {
		int now = TimeUtil.time();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("action", "push.add");
		data.put("rid", payload.getRid());
		data.put("uid", payload.getOpenId());
		data.put("content", payload.getContent());
		data.put("push_time", now);
		data.put("time", now);
		String sign = MD5Util.makeSign(data);
		data.put("sign", sign);
		System.out.println(payload.toString());
		// 发送请求
		//Http.sendGet(GameApp.andriodPushUrl, data);
		return null;
	}

	@Override
	public PushResult clean(PushPayload payload) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("uid", payload.getOpenId());
		data.put("time", TimeUtil.time());
		data.put("action", "push.clean");
//		GameApp.sendHttpRequest(GameApp.andriodPushUrl, data);
		return null;
	}
	
}
