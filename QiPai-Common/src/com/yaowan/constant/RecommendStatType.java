package com.yaowan.constant;

/**
 * 推荐结果
 * @author YW0981
 *
 */
public class RecommendStatType {


	/**
	 * 用户是游客
	 */
	public static final int ISVISITER = 0;
	
	/**
	 * 成功
	 */
	public static final int SUCCESS = 3;
	
	/**
	 * 用户已经被推荐
	 */
	public static final int ISBERECOMMEND = 1;
	
	/**
	 * 
	 * 推荐码今天推荐次数已超过上限
	 */
	public static final int ISOVERTIME = 2;
	
	/**
	 * 推荐码不存在
	 */
	public static final int NOTEXIT = 4;
	
	/**
	 * 用户推荐被关闭
	 */
	public static final int ISBECLOSE = 5;
	
	
	
	
	
	/**
	 * 领取推荐奖励失败
	 */
	public static final int ISGETMONEYFAIL = 0;
	
	/**
	 * 领取推荐奖励成功
	 */
	public static final int ISGETMONEYSYCCESS = 1;
	
	
}
