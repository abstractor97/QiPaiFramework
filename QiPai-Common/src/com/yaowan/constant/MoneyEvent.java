package com.yaowan.constant;

import java.util.HashMap;



/**
 * 货币事件
 *
 * <pre>
 * 货币事件定义规则：
 *  (1)		(001)
 * 模块编号  	 事件编号
 * </pre>
 *
 */
/**
 * @author zane
 *
 */

public enum MoneyEvent {
	//
	/**
	 * 角色购买(10001)
	 */
	AVATAR(10001),
	/**
	 * 修改昵称
	 */
	NICK(10002),

	/**
	 * 购买道具
	 */
	BUYITEM(10003),
	/**
	 * 购买vip
	 */
	BUYVIP(10004),
	
	/**
	 * 闷鸡税收
	 */
	MENJI_TAX(10005),
	
	/**
	 * 昭通斗地主税收
	 */
	DOUDIZHU_TAX(10006),
	
	/**
	 * 昭通麻将税收
	 */
	MAJIANG_TAX(10007),
	
	/**
	 * 镇雄麻将税收
	 */
	ZXMAJIANG_TAX(10008),
	
	/**
	 * 牛牛税收
	 */
	NIUNIU_TAX(10009),
	
	/**
	 * 成都麻将税收
	 */
	CDMAJIANG_TAX(10010),
	
	/**
	 * 昭通三宝税收
	 */
	TZSANBAO_TAX(10011),

	/**
	 * 充值
	 */
	CHARGE(20001),
	/**
	 * 使用道具
	 */
	USEITEM(20002),
	/**
	 * 救济金
	 */
	GOLDRESUCE(20003),
	/**
	 * 签到奖励
	 */
	SIGNREWARD(20004),
	/**
	 * 兑换奖励
	 */
	EXCHANGE(20005),
	/**
	 * 邮件发放
	 */
	GM_MAIL(20006),
	/**
	 * 注册初始化金币
	 */
	REGISTER(20007),
	
	/**
	 * 任务奖励
	 */
	DAILYTASK(20011), 
	/**
	 * 活动获取
	 */
	ACTIVITY(20012),
	
	/**
	 * 比赛报名
	 */
	MATCH_APPLY(20020),
	/**
	 * 比赛排名奖励
	 */
	MATCH_RANKING(20021),
	
	/**
	 * 斗地主输赢
	 */
	DOUDIZHU(30001),
	/**
	 * 闷鸡输赢
	 */
	MENJI(30002),
	/**
	 * 南充闷鸡输赢
	 */
	NCMENJI(30003),
	/**
	 * 昭通麻将输赢
	 */
	MAJIANG(30004), 
	/**
	 * 牛牛输赢
	 */
	NIUNIU(30005), 
	
	/**
	 * 成都麻将输赢
	 */
	CDMAJIANG(30006),
	
	/**
	 * 镇雄麻将输赢
	 */
	ZXMAJIANG(30007),
	//德州
	DEZHOU(30008),
	
	/**
	 * 被使用道具获得
	 */
	BEUSEITEM(30009),
	
	/**
	 * 昭通三宝输赢
	 */
	ZTSANBAO(30010),
	
	/**
	 * 焖鸡抽水
	 */
	MENJI_CHOUSHUI(30011),
	

	
	/**
	 * 金币满了
	 */
	GOLD_FULL(40000),
	/**
	 * 任务奖励
	 */
	MAINTASK(40001),
	/**
	 * 任务奖励
	 */
	LOTTERYTASK(40002),
	/**
	 * 好友房
	 */
	FRIENDROOM(40003),
	
	/**
	 * 红包奖励
	 */
	REDBAG(40004),
	
	/**
	 * 推广奖励
	 */
	RECOMMEND(40005);

	private final int value;

	private static HashMap<Integer, MoneyEvent> map = new HashMap<Integer, MoneyEvent>();

	static {
		for (MoneyEvent elem : MoneyEvent.values()) {
			map.put(elem.getValue(), elem);
		}
	}

	private MoneyEvent(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static final MoneyEvent valueOf(int moneyEvent) {
		return map.get(moneyEvent);
	}
}
