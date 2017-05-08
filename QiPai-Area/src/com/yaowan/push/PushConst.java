package com.yaowan.push;

import java.util.HashSet;
import java.util.Set;

/**
 * 推送信息的常量
 *
 */
public class PushConst {
	private static final int BASE = 800000;
	/**
	 * 采购提示 :老板，部分店铺的货物库存快要销售完毕了，再不采购货物就没有入啦。
	 */
	public static final int STORE_GOODS_NOT_ENOUGH = BASE + 2010;
	/**
	 * 被抢生意 :报告老板，我们的店铺被其他企业恶心抢夺生意，店铺人流量下滑了。
	 */
	public static final int STORE_ROBBED_BUSINESS= BASE + 2020;
	/**
	 * 收集店铺G币  ：老板，您的店铺已卖出大量货物，快来收集G币吧。
	 */
	//TODO 策划数值没配
	public static final int STORE_GATHER_G_COIN = BASE + 2030;
	/**
	 * 出现竞争 :根据调查，我们的营业辐射范围内出现了竞争店铺，收入受到一定的影响。
	 */
	public static final int STORE_COMPETITION = BASE + 2040;
	/**
	 * 满意度过低 :部分店铺满意度过低，满意度为0将被强制拆除。
	 */
	public static final int STORE_SATISFACTION_TOO_LOW = BASE + 2050;
	/**
	 * 员工被挖 :不好了不好了，我们公司的员工被其他企业高薪挖走了。
	 */
	public static final int EMPLOYEE_JOB_HOPPING = BASE + 3010;
	/**
	 * 商会基金负数 :%s的商会基金已消耗殆尽，请尽快做出处理，不然商会加成就会消失了喔。
	 */
	public static final int CLAN_FUND_MINUS = BASE + 4010;
	/**
	 * 团购胜利 ：通过每个成员的努力，%s商会成功获得本次%s消费团的竞标胜利。
	 */
	public static final int CLAN_TUAN_SUCCESS = BASE + 4020;
	/**
	 * 地标抢夺 :%s商会成功获得%s的经营权，全体商会成员获得了大幅度的销售加成。
	 */
	public static final int CLAN_LANDMARK_LOOT = BASE + 4030;
	/**
	 * 地标被抢 :%s商会失去了对%s的经营权，请及时做出处理。
	 */
	public static final int CLAN_LANDMARK_ROBBED= BASE + 4040;
	/**
	 * 到达港口 :老板，您的%s贸易货船，已顺利到达%s。
	 */
//	public static final int TRADE_BOAT_ARRIVE = BASE + 5010;
	/**
	 * 船只到期 :老板，您的%s贸易货船租借期限已到，请及时续费。
	 */
	public static final int TRADE_BOAT_EXPIRE = BASE + 5020;
	/**
	 * 贷款到期 :老板，您在梦想岛银行申请的抵押贷款还款期限已到了。
	 */
	public static final int BANK_LOAN_EXPIRE = BASE + 6010;
	/**
	 * 官员竞选 :%s已经开始竞选了，老板您要去参加竞选吗？
	 */
	public static final int OFFICER_CAMPAIGN = BASE + 7010;
	/**
	 * 官员当选 :老板收到人民的爱戴，已被选举为%s。
	 */
	public static final int OFFICER_BE_ELECTED = BASE + 7020;
	/**
	 * 被弹劾 :%s发起了对您当任的%s的职位弹劾。
	 */
	public static final int OFFICER_IMPEACHMENT = BASE + 7030;
	/**
	 * 彩票中奖 :恭喜老板，您购买的这期慈善彩票中奖啦！！！
	 */
	public static final int CHARITY_CENTER_WINNING = BASE + 8010;
	/**
	 * 成功竞拍 :老板，您在交易中心竞拍的%s已经成功拍下，请查收吧。
	 */
	public static final int TRADING_CENTER_AUCTION_SUCCEED = BASE + 9010;
	/**
	 * 拍卖开始 :本轮拍卖将会在%s准时举行，内容丰富，敬请期待。
	 */
	public static final int AUCTION_START = BASE + 10010;
	/**
	 * 挽回提示 :老板，公司的发展离不开你的支持，请回来做出下一步的发展规划吧~
	 */
	public static final int SYSTEM_REDEEM = BASE + 11010;
	/**
	 * 道具过期 :老板，您的背包中有道具快要过期啦，还不用就浪费了喔！
	 */
	// TODO 等待策划确定
	public static final int SYSTEM_ITEM_EXPIRE= BASE + 11020;

	/**
	 * @param lang 角色语言版本
	 * @param pushId  {@link constant.PushConst}
	 * @param args 组成msg的参数
	 * @return
	 */
	public static String getMsg(int pushId,Object... args){
		String msg = getMsg(pushId);
		if(null != args && args.length > 0 && null != msg){
			msg = String.format(msg, args);
		}
		return msg;
	}
	
	/**
	 * 获取语言包内容
	 * @author zhaozhiheng 2016年6月22日
	 * @param lang
	 * @param langKey
	 * @return
	 */
	public static String getMsg(int langKey) {
		/*ILanguageBean languageBean = LanguageFactory.getInstance().getLanguage(lang, langKey);
		if(languageBean == null || StringUtils.isEmpty(languageBean.getValue())){
			return null;
		}*/
		String msg = "3333444";
		return msg;
	}
	private static Set<Integer> pushEvent = new HashSet<>();
//	private static Set<Integer> pushEvent = new HashSet<>(Arrays.asList(TRADE_BOAT_EXPIRE, BANK_LOAN_EXPIRE));
	static {
		pushEvent.add(TRADE_BOAT_EXPIRE);
		pushEvent.add(BANK_LOAN_EXPIRE);
	}
	
	public static Set<Integer> getPushEvent() {
		return pushEvent;
	}
	
}
