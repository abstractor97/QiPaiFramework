package com.yaowan.server.game.model.struct;

/**
 * 活动类型
 * @author G_T_C
 */
public class ActivityType {
	private ActivityType() {

	}
	
	/**
	 * 每日/登录就送活动
	 */
	public static final int DAYLOGIN = 0;
	
	/**
	 * 充值活动
	 */
	public static final int RECHARGE = 2;
	
	/**
	 * 某时间段活动
	 */
	public static final int TIMELIMIT = 3;


}
