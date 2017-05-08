/**
 * 
 */
package com.yaowan.constant;

/**
 * @author zane
 *
 */
public class GameError {
	/**
	 * 系统系统(01)
	 */

	// 系统错误
	public static final int SYSTEM_ERROR = 2502001;
	// 参数错误
	public static final int WRONG_PARAMETER = 2502002;
	// 系统异常
	public static final int SYSTEM_EXCEPTION = 2502003;
	// 没有权限
	public static final int NO_AUTHOR = 2502004;
	// 系统维护下线
	public static final int SYSTEM_MAINTAIN = 2502005;
	// 验证信息错误
	public static final int LOGIN_WRONG_SIGN = 2503001;
	// 没有登录
	public static final int LOGIN_NOT_LOG = 2503002;
	// 重登
	public static final int LOGIN_REPEAT = 2503003;
	// 充值失败
	public static final int CHARGER_FAIL = 2503004;
	// 已创建角色
	public static final int ROLE_EXIST = 2504001;
	// 角色名已存在
	public static final int ROLE_NICK_EXIST = 2504002;
	// 钱不够
	public static final int ROLE_MONEY_LACK = 2504003;
	// 角色解锁了
	public static final int AVATAR_HAS_UNLOCK = 2504004;
	// 名字非法
	public static final int NICK_ILLEGALITY = 2504005;
	// 不在游戏中
	public static final int MATCH_NOT_MATCHING = 2505001;
	// 在游戏中
	public static final int MATCH_UNDER_MATCHING = 2505002;
	// 已在游戏排队队列中
	public static final int MATCH_IN_QUEUE = 2505003;

	public static final int MATCH_GRID_USED = 2505004;
	// 已经在游戏战败
	public static final int MATCH_LOSE = 2505005;

	// 该道具正在冷却中
	public static final int MATCH_ITEM_CD = 2505007;

	// 游戏金币不足
	public static final int MATCH_MONEY_LACK = 2505013;

	// 你已经退出了游戏
	public static final int MATCH_QUIT = 2505016;
	
	// 游戏人数已达上限
	public static final int GAME_FULL = 2505017;
	//同账号多手机登录
	public static final int SAME_LOGIN = 101010;

}
