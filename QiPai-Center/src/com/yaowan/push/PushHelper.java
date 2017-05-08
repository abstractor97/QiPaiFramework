package com.yaowan.push;

import java.util.Collection;

import com.yaowan.server.game.model.data.entity.Role;

public class PushHelper {
	
	protected final static int ANDROIDUNICAST = 0;
	protected final static int IOSUNICAST = 1;
	protected final static int ANDROIDLISTCAST = 2;
	protected final static int IOSLISTCAST = 3;

	/**
	 * 有效时间
	 */
	public static final int VALID_TIME = 15;

	/**
	 * 单播推送
	 * @author zhaozhiheng 2016年6月20日
	 * @param role
	 * @param msgId
	 * @throws Exception
	 */
	public static void unicastPush(Role role, int msgId, Object... args) {
		/*IPush thirdPush = ServerAppContext.getBean(PushService.class);
		try {
			thirdPush.unicastPush(role, msgId, args);
		} catch (Exception e) {
			LogUtil.error(e);
		}*/
	}
	
	/**
	 * 单播推送
	 * @author zhaozhiheng 2016年6月20日
	 * @param role
	 * @param msgId
	 * @return 
	 * @throws Exception
	 */
	public static void unicastPush(Role role, String msg, String ticker, String title) {
		/*IPush thirdPush = ServerAppContext.getBean(PushService.class);
		try {
			thirdPush.unicastPush(role, msg, ticker, title);
		} catch (Exception e) {
			LogUtil.error(e);
		}*/
	}
	
	/**
	 * 列播推送
	 * @author zhaozhiheng 2016年6月20日
	 * @param role
	 * @param msgId
	 * @throws Exception
	 */
	public static void listcastPush(Collection<Role> roles, int msgId, Object...args) {
		/*IPush thirdPush = ServerAppContext.getBean(PushService.class);
		try {
			thirdPush.listcastPush(roles, msgId, args);
		} catch (Exception e) {
			LogUtil.error(e);
		}*/
	}

	
}
