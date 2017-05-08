package com.yaowan.framework.push.channel;

import com.yaowan.framework.push.model.PushPayload;
import com.yaowan.framework.push.model.PushResult;

public class XiaomiChannel extends AbstractChannel {

	public XiaomiChannel() {
		super(ChannelType.XIAOMI);
	}

	@Override
	public PushResult push(PushPayload payload) {
//		CMiPushKey cMiPushKey = CMiPushKey.config(token.getPush_id());
//		if(cMiPushKey == null){
//			return;
//		}
//		// 1是安卓，用小米推送
//		Constants.useOfficial(); // 使用正式环境
//		String PACKAGENAME = cMiPushKey.getPush_package_name().trim();
//		String messagePayload = msg;
//		String title = msg;
//		String description = msg;
//		Message message = new Message.Builder().title(title)
//				.description(description).payload(messagePayload)
//				.restrictedPackageName(PACKAGENAME).passThrough(0) // 1表示透传消息，0表示通知栏消息
//				.notifyType(1) // 使用默认提示音提示
//				.extra(Constants.EXTRA_PARAM_NOTIFY_EFFECT, Constants.NOTIFY_LAUNCHER_ACTIVITY)
//				.build();
//		Sender sender = new Sender(cMiPushKey.getSecret_key().trim());
//		Result result = sender.send(message, token.getToken(), 3);
		throw new UnsupportedOperationException();
	}

	@Override
	public PushResult clean(PushPayload payload) {
		throw new UnsupportedOperationException();
	}
	
}
