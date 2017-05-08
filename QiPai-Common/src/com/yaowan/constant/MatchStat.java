package com.yaowan.constant;

/**
 * 比赛状态
 */
public class MatchStat {
	/**
	 * 未初始化
	 */
	public static final int UNINIT = 0;
	
	/**
	 * 准备
	 */
	public static final int PREPARE = 1;
	
	/**
	 * 正在报名
	 */
	public static final int APPLY = 2;
	
	/**
	 * 预赛
	 */
	public static final int MATCH01 = 3;
	
	/**
	 * 复赛
	 */
	public static final int MATCH02 = 4;
	
	/**
	 * 决赛
	 */
	public static final int MATCH03 = 5;
	
	/**
	 * 已经终止，无法再举行下一次，比如一次性的活动
	 */
	public static final int END = 6;
}
