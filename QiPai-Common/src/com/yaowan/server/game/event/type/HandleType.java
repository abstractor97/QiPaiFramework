/**
 * 
 */
package com.yaowan.server.game.event.type;

/**
 * 事件类型
 * 
 * @author Thomas Zheng
 * 
 */
public class HandleType {

	/**
	 * 1-焖鸡
	 */
	public final static int MENJI_INIT = 1001;
	
	/**
	 * 1-焖鸡
	 */
	public final static int MENJI_GAME = 1002;
	
	/**
	 * 1-焖鸡
	 */
	public final static int MENJI_QI_PAI = 1003;
	
	/**
	 * 1-焖鸡
	 */
	public final static int MENJI_END = 1004;
	
	/**
	 * 1-焖鸡
	 */
	public final static int GAME_EXIT = 1005;
	
	/**
	 * 1-焖鸡看牌
	 */
	public final static int MENJI_LOOK = 1006;
	
	/**
	 * 1-焖鸡比牌
	 */
	public final static int MENJI_COMPETE = 1007;
	
	/**
	 * 1-焖鸡加注
	 */
	public final static int MENJI_ADD = 1008;
	
	/**
	 * 1-焖鸡跟注
	 */
	public final static int MENJI_FOLLOW = 1009;
	
	/**
	 * 1-焖鸡
	 */
	public final static int MENJI_GAME_AUTO = 1010;
	
	/**
	 * 2-斗地主看牌
	 */
	public final static int DOUDIZHU_LOOK = 2001;
	
	/**
	 * 2-斗地主等待抓
	 */
	public final static int DOUDIZHU_ZHUA = 2002;
	
	/**
	 * 2-斗地主不抓
	 */
	public final static int DOUDIZHU_ZHUA_NO = 2003;
	
	/**
	 * 2-斗地主默认闷抓
	 */
	public final static int DOUDIZHU_LAST_MEN_ZHUA = 2004;
	
	/**
	 * 2-斗地主等待倒
	 */
	public final static int DOUDIZHU_DAO = 2005;
	
	/**
	 * 2-斗地主默认农民不倒
	 */
	public final static int DOUDIZHU_DAO_NO = 2006;
	
	/**
	 * 2-斗地主等待拉
	 */
	public final static int DOUDIZHU_LA = 2007;
	
	/**
	 * 2-斗地主默认补不拉
	 */
	public final static int DOUDIZHU_LA_NO = 2008;
	
	/**
	 * 2-斗地主开牌
	 */
	public final static int DOUDIZHU_PAI = 2009;
	
	/**
	 * 2-斗地主等待打牌
	 */
	public final static int DOUDIZHU_OUT_PAI = 2010;
	
	/**
	 * 2-斗地主等待打牌
	 */
	public final static int DOUDIZHU_OUT_PAI_AUTO = 2011;
	
	/**
	 * 2-斗地主必抓
	 */
	public final static int DOUDIZHU_BI_ZHUA = 2012;
	
	/**
	 * 2-斗地主必倒
	 */
	public final static int DOUDIZHU_BI_DAO = 2013;
	//斗地主闷抓
	public final static int DOUDIZHU_MEN_ZHUA = 2014;
	//进行倒
	public final static int DOUDIZHU_TO_DAO = 2015;
	//进行拉
	public final static int DOUDIZHU_TO_LA = 2016;
	/**
	 * 3-麻将
	 */
	public final static int MAJIANG_INIT = 3001;
	
	/**
	 * 3-麻将摸牌
	 */
	public final static int MAJIANG_GET_PAI = 3002;
	
	/**
	 * 3-麻将出牌
	 */
	public final static int MAJIANG_OUT_PAI = 3003;
	
	/**
	 * 3-等待中断操作
	 */
	public final static int MAJIANG_WAIT = 3004;
	
	/**
	 * 3-麻将碰牌
	 */
	public final static int MAJIANG_PENG = 3005;
	
	/**
	 * 3-麻将胡牌
	 */
	public final static int MAJIANG_HU = 3006;
	
	/**
	 * 3-麻将杠牌
	 */
	public final static int MAJIANG_GANG = 3007;
	
	/**
	 * 3-麻将暗杠牌
	 */
	public final static int MAJIANG_AN_GANG = 3008;
	

	/**
	 * 3-麻将暗杠牌
	 */
	public final static int MAJIANG_EXTRA_GANG = 3009;
	
	/**
	 * 3-麻将多人胡牌
	 */
	public final static int MAJIANG_MANY_HU = 3010;
	
	/**
	 * 3-等待显示定缺
	 */
	public final static int MAJIANG_WAIT_SHOW_DING_QUE = 3011;
	
	/**
	 * 3-等待设置定缺
	 */
	public final static int MAJIANG_WAIT_DING_QUE_TYPE = 3012;

	/**
	 * 3-麻将飞碰牌
	 */
	public final static int MAJIANG_FREE_PENG = 3013;
	
	/**
	 * 3-麻将飞杠牌
	 */
	public final static int MAJIANG_FREE_GANG = 3014;
	
	/**
	 * 3-麻将等待领取救济金
	 */
	public final static int MAJIANG_KICK_PLAYER = 3015;
	
	/**
	 * 4-游戏初始化
	 */
	public final static int GAME_INIT = 4001;
	
	/**
	 * 4-机器人
	 */
	public final static int ROBOT_START = 4002;
	
	/**
	 * 4-游戏超时退出
	 */
	public final static int GAME_NO_READY = 4003;
	
	/**
	 * 4-游戏结束
	 */
	public final static int GAME_END = 4004;
	
	/**
	 * 4-重新进入房间
	 */
	public final static int GAME_ENTER = 4005;
	
	/**
	 * 4-重置
	 */
	public final static int GAME_RESET = 4006;
	
	/**
	 * 4-运行时检测
	 */
	public final static int GAME_RUNNING = 4007;
	
	//游戏配对
	public final static int GAME_MATCHING = 4008;
	//游戏退出桌子
	public final static int GAME_EXIT_TABLE = 4009;
	//游戏改变桌子
	public final static int GAME_CHANGE_TABLE = 4010;
	
	public static final int ROOM_ONLINE = 4011;
	
	//游戏启动时，对配置的加载
	public static final int GAME_SERVER_START = 4012;
	/**
	 * 5-南充焖鸡
	 */
	public final static int NCMENJI_INIT = 5001;
	
	/**
	 * 5-南充焖鸡
	 */
	public final static int NCMENJI_GAME = 5002;
	
	/**
	 * 5-南充焖鸡
	 */
	public final static int NCMENJI_QI_PAI = 5003;
	
	/**
	 * 5-南充焖鸡
	 */
	public final static int NCMENJI_END = 5004;
	/**
	 * 10-AI结束后要走
	 */
	public final static int AI_END_EXIT = 10001;
	
	/**
	 * 10-AI 结束后要准备
	 */
	public final static int AI_END_READY = 10002;
	/**
	 * 6-斗牛
	 */
	public final static int DOUNIU_RESULT = 6001;
	
	/**
	 * 6-斗牛发送开局
	 */
	public final static int DOUNIU_START = 6002;
	
	//德州游戏开局
	public final static int DEZHOU_START = 51001;
	
	//德州游戏一局结束
	public final static int DEZHOU_END = 51002;

	
}
