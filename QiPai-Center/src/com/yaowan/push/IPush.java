

package com.yaowan.push;

import java.util.Collection;

import com.yaowan.framework.push.model.DPushNotification;
import com.yaowan.server.game.model.data.entity.Role;



/**
 * 
 * @author zhaozhiheng 2016年6月20日
 */
public interface IPush {
	/**
	 * 发送推送请求
	 * @author zhaozhiheng 2016年6月27日
	 * @param dPushNotification
	 * @throws Exception
	 */
	void send(DPushNotification dPushNotification) throws Exception;
	
	/**
	 * 单播推送
	 * @author zhaozhiheng 2016年6月20日
	 * @param role
	 * @param msgId
	 * @throws Exception
	 */
	void unicastPush(Role role, int msgId, Object... args) throws Exception;
	
	/**
	 * 单播推送
	 * @author zhaozhiheng 2016年8月7日
	 * @param role
	 * @param msg
	 * @param ticker
	 * @param title
	 * @throws Exception
	 */
	void unicastPush(Role role, String msg, String ticker, String title) throws Exception;
	
	/**
	 * 列播推送
	 * @author zhaozhiheng 2016年6月20日
	 * @param role
	 * @param msgId
	 * @throws Exception
	 */
	void listcastPush(Collection<Role> roles, int msgId, Object...args) throws Exception;
}

