/**
 * 
 */
package com.yaowan.constant;

/**
 * 
 * 0-未初始化
 * 1-准备前
 * 2-进行
 * 3-结束
 * 4-已经结算
 * 5-超时结束 
 * @author zane
 *
 */
public class GameStatus {
	
	// 
	public static final int NO_INIT = 0;
	//
	public static final int WAIT_READY = 1;
	//2-进行
	public static final int RUNNING = 2;
	
	//3-结束
	public static final int END = 3;
	
	//4-结算(等待重新开始)
	public static final int END_REWARD = 4;
	
	//5-超时结束
	public static final int NO_READY = 5;
	
	//
	public static final int CLEAR = 6;
	
}
