package com.yaowan.server.game.model.struct;


public class LotteryTaskType {

	/************************ 麻将奖券任务 **********************/
	public final static int ZIMO_1S = 1; // 在本场对局中自摸1次
	public final static int HU_1S = 2; // 在本场对局中胡对手1次
	public final static int HU_GTE_5S = 3; // 在本场对局中胡出5番或以上番数
	public final static int DA_DUI_ZI = 4; // 在本场对局中胡出大对子
	public final static int QING_YI_SE = 5; // 在本场对局中胡出清一色
	public final static int QIAO_QI_DUI = 6; // 在本场对局中胡出巧七对

	/************************ 斗地主奖券任务 **********************/
	public final static int WANG_ZHA_1S = 7; // 打出1次王炸
	public final static int SHUN_ZI_1S = 8; // 打出1次顺子
	public final static int LIAN_DUI__1S = 9; // 打出1次连对
	public final static int ZHA_DAN_1S = 10; // ,打出1个炸弹
	public final static int WIN = 11; // ,获得本局胜利
	public final static int FEI_JI_1S = 12;// ,打出1个飞机

	
}
