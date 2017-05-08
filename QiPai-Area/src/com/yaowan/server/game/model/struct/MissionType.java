package com.yaowan.server.game.model.struct;

// 任务类型
public enum MissionType {
	NULL, // 1：朋友圈分享
	SHARE_WX_QUAN, // 1：朋友圈分享
	SHARE_WX_FRIEND, // 2：微信好友分享
	SHARE_QQ, // //3：QQ分享
	WIN, // 4：牌局胜利任务 需要传递游戏类型
	CONTINUE_WIN, // 5：连胜任务 需要传递游戏类型
	TIMES, // 6：完成局数任务 需要传递游戏类型
	CARD_TYPE, // 7：牌型任务 需要传递游戏类型和牌型
	LOGIN,// 8：登录任务 
	GANG,// 9：麻将杠
	
	/*******************************主线任务类型***************************************/
	/**
	 * //10角色等级达到10级
	 */
	LEVEL,
	/**
	 * //11拍照（选择图片）上传头像
	 */
	PHOTO,
	/**
	 * //12金币数量达到1000000
	 */
	GOLD_NUM,
	/**
	 * //13钻石数量达到10000
	 */
	DIAMOND,
	/**
	 * //14奖券数量达到10000
	 */
	LOTTERIES,
	/**
	 * //15在商场消费1次
	 */
	SHOP_BUY,
	/**
	 * //16使用奖券兑换1个物品
	 */
	LOTTER_EXCHANGE,
}