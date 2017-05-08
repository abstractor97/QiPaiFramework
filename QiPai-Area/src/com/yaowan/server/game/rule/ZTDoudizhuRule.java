/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yaowan.constant.GameType;
import com.yaowan.csv.cache.DoudizhuDrawCardCache;
import com.yaowan.csv.entity.DoudizhuDrawCardCsv;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.MathUtil;
import com.yaowan.server.game.function.NPCFunction;
import com.yaowan.server.game.model.struct.Card;
import com.yaowan.server.game.model.struct.CardBigType;
import com.yaowan.server.game.model.struct.CardComparator;
import com.yaowan.server.game.model.struct.CardSmallType;
import com.yaowan.server.game.model.struct.CardType;
import com.yaowan.server.game.model.struct.ZTDoudizhuRole;
import com.yaowan.server.game.model.struct.ZTDoudizhuTable;

/**
 * 昭通麻将算法
 *
 * @author zane
 */
public class ZTDoudizhuRule {

	/**
	 * 对牌进行排序，从小到大，比较器为CardComparator
	 * 
	 * @param cards
	 *            牌的集合
	 */
	public static void sortCards(List<Card> cards) {
		// 策略模式；复用已有类；
		Collections.sort(cards, new CardComparator());
	}

	/**
	 * 根据牌的id获得一张牌的大类型：方块，梅花,红桃,黑桃,小王,大王
	 * 
	 * @param id
	 *            牌的id
	 * 
	 * @return 牌的大类型：方块，梅花,红桃,黑桃,小王,大王
	 */
	public static CardBigType getBigType(int id) {
		CardBigType bigType = null;
		if (id >= 1 && id <= 13) {
			bigType = CardBigType.FANG_KUAI;
		} else if (id >= 14 && id <= 26) {
			bigType = CardBigType.MEI_HUA;
		} else if (id >= 27 && id <= 39) {
			bigType = CardBigType.HONG_TAO;
		} else if (id >= 40 && id <= 52) {
			bigType = CardBigType.HEI_TAO;
		} else if (id == 53) {
			bigType = CardBigType.XIAO_WANG;
		} else if (id == 54) {
			bigType = CardBigType.DA_WANG;
		} else if (id == 55) {
			bigType = CardBigType.LAI_ZI;
		}
		return bigType;
	}

	/**
	 * 根据牌的id，获取牌的小类型：2_10,A,J,Q,K
	 * 
	 * @param id
	 *            牌的id
	 * 
	 * @return 牌的小类型：2_10,A,J,Q,K
	 */
	public static CardSmallType getSmallType(int id) {
		if (id < 1 || id > 55) {
			throw new RuntimeException("牌的数字不合法");
		}

		CardSmallType smallType = null;

		if (id >= 1 && id <= 52) {
			smallType = numToType(id % 13);
		} else if (id == 53) {
			smallType = CardSmallType.XIAO_WANG;
		} else if (id == 54) {
			smallType = CardSmallType.DA_WANG;
		} else if (id == 55) {
			smallType = CardSmallType.LAI_ZI;
		} else {
			smallType = null;
		}
		return smallType;
	}

	/**
	 * 将阿拉伯数字0到12转换成对应的小牌型,被getSmallType方法调用
	 * 
	 * @param num
	 *            数字（0到12）
	 * @return 牌的小类型
	 */
	private static CardSmallType numToType(int num) {
		CardSmallType type = null;
		switch (num) {
		case 0:
			type = CardSmallType.K;
			break;
		case 1:
			type = CardSmallType.A;
			break;
		case 2:
			type = CardSmallType.ER;
			break;
		case 3:
			type = CardSmallType.SAN;
			break;
		case 4:
			type = CardSmallType.SI;
			break;
		case 5:
			type = CardSmallType.WU;
			break;
		case 6:
			type = CardSmallType.LIU;
			break;
		case 7:
			type = CardSmallType.QI;
			break;
		case 8:
			type = CardSmallType.BA;
			break;
		case 9:
			type = CardSmallType.JIU;
			break;
		case 10:
			type = CardSmallType.SHI;
			break;
		case 11:
			type = CardSmallType.J;
			break;
		case 12:
			type = CardSmallType.Q;
			break;

		}
		return type;
	}

	/**
	 * 根据牌的id，获得一张牌的等级
	 * 
	 * @param id
	 *            牌的id
	 * @return 与牌数字对应的等级
	 */
	public static int getGrade(int id) {

		if (id < 1 || id > 55) {
			throw new RuntimeException("牌的数字不合法");
		}

		int grade = 0;

		// 2个王必须放在前边判断
		if (id == 53) {
			grade = 16;
		} else if (id == 54) {
			grade = 17;
		} else if (id == 55) {
			grade = 18;
		}

		else {
			int modResult = id % 13;

			if (modResult == 1) {
				grade = 14;
			} else if (modResult == 2) {
				grade = 15;
			} else if (modResult == 3) {
				grade = 3;
			} else if (modResult == 4) {
				grade = 4;
			} else if (modResult == 5) {
				grade = 5;
			} else if (modResult == 6) {
				grade = 6;
			} else if (modResult == 7) {
				grade = 7;
			} else if (modResult == 8) {
				grade = 8;
			} else if (modResult == 9) {
				grade = 9;
			} else if (modResult == 10) {
				grade = 10;
			} else if (modResult == 11) {
				grade = 11;
			} else if (modResult == 12) {
				grade = 12;
			} else if (modResult == 0) {
				grade = 13;
			}

		}

		return grade;
	}

	/**
	 * 比较我的牌和上家的牌的大小，决定是否可以出牌
	 * 
	 * @param myCards
	 *            我想出的牌
	 * 
	 * @param myCardType
	 *            我的牌的类型
	 * @param prevCards
	 *            上家的牌
	 * @param prevCardType
	 *            上家的牌型
	 * @return 可以出牌，返回true；否则，返回false。
	 */
	public static boolean isOvercomePrev(List<Card> myCards,
			CardType myCardType, List<Card> prevCards, CardType prevCardType) {
		// 我的牌和上家的牌都不能为null
		if (myCards == null || prevCards == null) {
			return false;
		}

		if (myCardType == null || prevCardType == null) {
			LogUtil.info("上家出的牌不合法，所以不能出。");
			return false;
		}

		// 上一首牌的个数
		int prevSize = prevCards.size();
		int mySize = myCards.size();

		// 我先出牌，上家没有牌
		if (prevSize == 0 && mySize != 0) {
			return true;
		}

		// 集中判断是否王炸，免得多次判断王炸
		if (prevCardType == CardType.WANG_ZHA) {
			LogUtil.info("上家王炸，肯定不能出。");
			return false;
		} else if (myCardType == CardType.WANG_ZHA) {
			LogUtil.info("我王炸，肯定能出。");
			return true;
		}

		// 集中判断对方不是炸弹，我出炸弹的情况
		if (prevCardType != CardType.ZHA_DAN && myCardType == CardType.ZHA_DAN) {
			return true;
		}

		// 默认情况：上家和自己想出的牌都符合规则
		sortCards(myCards);// 对牌排序
		sortCards(prevCards);// 对牌排序

		int myGrade = myCards.get(0).grade;
		int prevGrade = prevCards.get(0).grade;

		// 比较2家的牌，主要有2种情况，1.我出和上家一种类型的牌，即对子管对子；
		// 2.我出炸弹，此时，和上家的牌的类型可能不同
		// 王炸的情况已经排除

		// 单
		if (prevCardType == CardType.DAN && myCardType == CardType.DAN) {
			// 一张牌可以大过上家的牌
			return compareGrade(myGrade, prevGrade);
		}
		// 对子
		else if (prevCardType == CardType.DUI_ZI
				&& myCardType == CardType.DUI_ZI) {
			// 2张牌可以大过上家的牌
			return compareGrade(myGrade, prevGrade);

		}
		// 3不带
		else if (prevCardType == CardType.SAN_BU_DAI
				&& myCardType == CardType.SAN_BU_DAI) {
			// 3张牌可以大过上家的牌
			return compareGrade(myGrade, prevGrade);
		}
		// 炸弹
		else if (prevCardType == CardType.ZHA_DAN
				&& myCardType == CardType.ZHA_DAN) {
			// 4张牌可以大过上家的牌
			return compareGrade(myGrade, prevGrade);

		}
		// 3带1
		else if (prevCardType == CardType.SAN_DAI_YI
				&& myCardType == CardType.SAN_DAI_YI) {

			// 3带1只需比较第2张牌的大小
			myGrade = myCards.get(1).grade;
			prevGrade = prevCards.get(1).grade;
			return compareGrade(myGrade, prevGrade);

		}
		// 4带2
		else if (prevCardType == CardType.SI_DAI_ER
				&& myCardType == CardType.SI_DAI_ER) {

			// 4带2只需比较第3张牌的大小
			myGrade = myCards.get(2).grade;
			prevGrade = prevCards.get(2).grade;

		}
		// 顺子
		else if (prevCardType == CardType.SHUN_ZI
				&& myCardType == CardType.SHUN_ZI) {
			if (mySize != prevSize) {
				return false;
			} else {
				// 顺子只需比较最大的1张牌的大小
				myGrade = myCards.get(mySize - 1).grade;
				prevGrade = prevCards.get(prevSize - 1).grade;
				return compareGrade(myGrade, prevGrade);
			}

		}
		// 连对
		else if (prevCardType == CardType.LIAN_DUI
				&& myCardType == CardType.LIAN_DUI) {
			if (mySize != prevSize) {
				return false;
			} else {
				// 顺子只需比较最大的1张牌的大小
				myGrade = myCards.get(mySize - 1).grade;
				prevGrade = prevCards.get(prevSize - 1).grade;
				return compareGrade(myGrade, prevGrade);
			}

		}
		// 飞机
		else if (prevCardType == CardType.FEI_JI
				&& myCardType == CardType.FEI_JI) {
			if (mySize != prevSize) {
				return false;
			} else {
				// 顺子只需比较第5张牌的大小(特殊情况333444555666没有考虑，即12张的飞机，可以有2种出法)
				myGrade = myCards.get(4).grade;
				prevGrade = prevCards.get(4).grade;
				return compareGrade(myGrade, prevGrade);
			}
		}

		// 默认不能出牌
		return false;
	}

	/**
	 * 比较2个grade的大小
	 * 
	 * @param grade1
	 * @param grade2
	 * @return
	 */
	private static boolean compareGrade(int grade1, int grade2) {
		return grade1 > grade2;
	}

	public static List<Integer[]> AI_ORDER = null;

	static {
		AI_ORDER = new ArrayList<Integer[]>();
		AI_ORDER.add(new Integer[] { CardType.SHUN_ZI.ordinal(), 10 });
		AI_ORDER.add(new Integer[] { CardType.SHUN_ZI.ordinal(), 9 });
		AI_ORDER.add(new Integer[] { CardType.SHUN_ZI.ordinal(), 8 });
		AI_ORDER.add(new Integer[] { CardType.SHUN_ZI.ordinal(), 7 });
		AI_ORDER.add(new Integer[] { CardType.SHUN_ZI.ordinal(), 6 });
		AI_ORDER.add(new Integer[] { CardType.LIAN_DUI.ordinal(), 6 });
		AI_ORDER.add(new Integer[] { CardType.SHUN_ZI.ordinal(), 5 });
		AI_ORDER.add(new Integer[] { CardType.SAN_DAI_YI.ordinal(), 4 });
		AI_ORDER.add(new Integer[] { CardType.DUI_ZI.ordinal(), 2 });
		AI_ORDER.add(new Integer[] { CardType.DAN.ordinal(), 1 });
	}

	public static Map<CardType, List<List<Card>>> reArrangePai(List<Card> list,
			CardType cardType) {
		// TODO 一定要排序
		ZTDoudizhuRule.sortCards(list);
		Map<CardType, List<List<Card>>> map = new HashMap<CardType, List<List<Card>>>();
		// 顺子
		for (int j = 0; j < list.size(); j++) {
			Card card = list.get(j);
			List<Card> temp = new ArrayList<Card>();
			temp.add(card);
			for (int k = j; k < list.size() - 1; k++) {
				if (list.get(k + 1).getSmallType().ordinal() == CardSmallType.ER
						.ordinal()) {
					break;
				} else if (list.get(k + 1).getGrade() - list.get(k).getGrade() == 1) {
					temp.add(list.get(k + 1));
				} else if (list.get(k + 1).getGrade() == list.get(k).getGrade()) {
					// temp.add(list.get(k + 1));
				} else {
					break;
				}
				if (temp.size() == 5)
					break;
			}
			if (temp.size() > 4) {
				List<List<Card>> clist = map.get(CardType.SHUN_ZI);
				if (clist == null) {
					clist = new ArrayList<List<Card>>();
					map.put(CardType.SHUN_ZI, clist);
				}
				clist.add(temp);
			}
		}
		// 一张 二张 三张 四张 带鬼 王炸
		Card maxCard = list.get(list.size() - 1);
		for (int j = 0; j < list.size(); j++) {
			Card card = list.get(j);
			List<Card> temp = new ArrayList<Card>();
			temp.add(card);
			int k = j;
			for (; k < list.size() - 1; k++) {
				if (list.get(k + 1).getGrade() == list.get(k).getGrade()) {
					temp.add(list.get(k + 1));
					j = k + 1;
				} else if (list.get(k + 1).getBigType() == CardBigType.DA_WANG
						&& list.get(k).getBigType() == CardBigType.XIAO_WANG) {
					List<Card> wang = new ArrayList<Card>();
					wang.add(list.get(k));
					wang.add(list.get(k + 1));
					List<List<Card>> clist = map.get(CardType.WANG_ZHA);
					if (clist == null) {
						clist = new ArrayList<List<Card>>();
						map.put(CardType.WANG_ZHA, clist);
					}
					clist.add(wang);
					break;
				} else {
					break;
				}
				if (temp.size() == 1 && cardType == CardType.DAN) {
					if (maxCard.getBigType() == CardBigType.LAI_ZI
							&& maxCard.getId() == card.getId()) {

					} else if (maxCard.getBigType() == CardBigType.LAI_ZI
							&& card.getBigType() == CardBigType.DA_WANG) {
						temp.add(maxCard);
						List<List<Card>> clist = map.get(CardType.WANG_ZHA);
						if (clist == null) {
							clist = new ArrayList<List<Card>>();
							map.put(CardType.WANG_ZHA, clist);
						}
						clist.add(temp);
					} else if (maxCard.getBigType() == CardBigType.LAI_ZI
							&& card.getBigType() == CardBigType.XIAO_WANG) {
						temp.add(maxCard);
						List<List<Card>> clist = map.get(CardType.WANG_ZHA);
						if (clist == null) {
							clist = new ArrayList<List<Card>>();
							map.put(CardType.WANG_ZHA, clist);
						}
						clist.add(temp);
					} else {
						List<List<Card>> clist = map.get(CardType.DAN);
						if (clist == null) {
							clist = new ArrayList<List<Card>>();
							map.put(CardType.DAN, clist);
						}
						clist.add(temp);
					}
					break;

				} else if (temp.size() == 2
						&& (cardType == CardType.DUI_ZI || cardType == CardType.LIAN_DUI)) {
					/*
					 * if (maxCard.getBigType() == CardBigType.LAI_ZI) {
					 * temp.add(maxCard); List<List<Card>> clist =
					 * map.get(CardType.SAN_BU_DAI); if (clist == null) { clist
					 * = new ArrayList<List<Card>>();
					 * map.put(CardType.SAN_BU_DAI, clist); } clist.add(temp); }
					 * else {
					 */
					List<List<Card>> clist = map.get(CardType.DUI_ZI);
					if (clist == null) {
						clist = new ArrayList<List<Card>>();
						map.put(CardType.DUI_ZI, clist);
					}
					clist.add(temp);
					break;
				} else if (temp.size() == 3 && cardType == CardType.SAN_BU_DAI) {
					if (maxCard.getBigType() == CardBigType.LAI_ZI) {
						temp.add(maxCard);
						List<List<Card>> clist = map.get(CardType.ZHA_DAN);
						if (clist == null) {
							clist = new ArrayList<List<Card>>();
							map.put(CardType.ZHA_DAN, clist);
						}
						clist.add(temp);
					} else {
						List<List<Card>> clist = map.get(CardType.SAN_BU_DAI);
						if (clist == null) {
							clist = new ArrayList<List<Card>>();
							map.put(CardType.SAN_BU_DAI, clist);
						}
						clist.add(temp);
					}
					break;
				} else if (temp.size() == 4 && cardType == CardType.ZHA_DAN) {
					List<List<Card>> clist = map.get(CardType.ZHA_DAN);
					if (clist == null) {
						clist = new ArrayList<List<Card>>();
						map.put(CardType.ZHA_DAN, clist);
					}
					clist.add(temp);
					break;
				}

			}
		}
		// 连对
		List<List<Card>> clist = map.get(CardType.DUI_ZI);
		if (clist != null) {
			for (int j = 0; j < clist.size(); j++) {
				List<Card> temp = new ArrayList<Card>();
				temp.addAll(clist.get(j));
				int k = j;
				for (; k < clist.size() - 1; k++) {
					if (clist.get(k + 1).get(0).getSmallType().ordinal() == CardSmallType.ER
							.ordinal()) {
						break;
					} else if (clist.get(k + 1).get(0).grade
							- clist.get(k).get(0).grade == 1) {
						temp.addAll(clist.get(k + 1));
					} else if (clist.get(k + 1).get(0).grade
							- clist.get(k).get(0).grade == 0) {
					} else {
						break;
					}
					if (temp.size() == 6)
						break;
				}
				if (temp.size() >= 6) {
					List<List<Card>> feilist = map.get(CardType.LIAN_DUI);
					if (feilist == null) {
						feilist = new ArrayList<List<Card>>();
						map.put(CardType.LIAN_DUI, feilist);
					}
					feilist.add(temp);
				}
			}
		}
		return map;
	}

	/**
	 * 所有可能牌型
	 */
	public static Map<CardType, List<List<Card>>> arrangePai(List<Card> list) {
		Map<CardType, List<List<Card>>> map = new HashMap<CardType, List<List<Card>>>();
		// 顺子
		for (int j = 0; j < list.size(); j++) {
			Card card = list.get(j);
			List<Card> temp = new ArrayList<Card>();
			temp.add(card);
			for (int k = j; k < list.size() - 1; k++) {
				if (list.get(k + 1).getSmallType().ordinal() == CardSmallType.ER
						.ordinal()) {
					break;
				} else if (list.get(k + 1).getGrade() - list.get(k).getGrade() == 1) {
					temp.add(list.get(k + 1));
				} else if (list.get(k + 1).getGrade() == list.get(k).getGrade()) {
					// temp.add(list.get(k + 1));
				} else {
					break;
				}
			}
			if (temp.size() > 4) {
				List<List<Card>> clist = map.get(CardType.SHUN_ZI);
				if (clist == null) {
					clist = new ArrayList<List<Card>>();
					map.put(CardType.SHUN_ZI, clist);
				}
				clist.add(temp);
			}
		}
		// 一张 二张 三张 四张 带鬼 王炸
		Card maxCard = list.get(list.size() - 1);
		for (int j = 0; j < list.size(); j++) {
			Card card = list.get(j);
			List<Card> temp = new ArrayList<Card>();
			temp.add(card);
			int k = j;
			for (; k < list.size() - 1; k++) {
				if (list.get(k + 1).getGrade() == list.get(k).getGrade()) {
					temp.add(list.get(k + 1));
					j = k + 1;
				} else if (list.get(k + 1).getBigType() == CardBigType.DA_WANG
						&& list.get(k).getBigType() == CardBigType.XIAO_WANG) {
					List<Card> wang = new ArrayList<Card>();
					wang.add(list.get(k));
					wang.add(list.get(k + 1));
					List<List<Card>> clist = map.get(CardType.WANG_ZHA);
					if (clist == null) {
						clist = new ArrayList<List<Card>>();
						map.put(CardType.WANG_ZHA, clist);
					}
					clist.add(wang);
					break;
				} else {
					break;
				}
			}
			if (temp.size() == 1) {
				if (maxCard.getBigType() == CardBigType.LAI_ZI
						&& maxCard.getId() == card.getId()) {

				} else if (maxCard.getBigType() == CardBigType.LAI_ZI
						&& card.getBigType() == CardBigType.DA_WANG) {
					temp.add(maxCard);
					List<List<Card>> clist = map.get(CardType.WANG_ZHA);
					if (clist == null) {
						clist = new ArrayList<List<Card>>();
						map.put(CardType.WANG_ZHA, clist);
					}
					clist.add(temp);
				} else if (maxCard.getBigType() == CardBigType.LAI_ZI
						&& card.getBigType() == CardBigType.XIAO_WANG) {
					temp.add(maxCard);
					List<List<Card>> clist = map.get(CardType.WANG_ZHA);
					if (clist == null) {
						clist = new ArrayList<List<Card>>();
						map.put(CardType.WANG_ZHA, clist);
					}
					clist.add(temp);
				} else {
					List<List<Card>> clist = map.get(CardType.DAN);
					if (clist == null) {
						clist = new ArrayList<List<Card>>();
						map.put(CardType.DAN, clist);
					}
					clist.add(temp);
				}

			} else if (temp.size() == 2) {
				/*
				 * if (maxCard.getBigType() == CardBigType.LAI_ZI) {
				 * temp.add(maxCard); List<List<Card>> clist =
				 * map.get(CardType.SAN_BU_DAI); if (clist == null) { clist =
				 * new ArrayList<List<Card>>(); map.put(CardType.SAN_BU_DAI,
				 * clist); } clist.add(temp); } else {
				 */
				List<List<Card>> clist = map.get(CardType.DUI_ZI);
				if (clist == null) {
					clist = new ArrayList<List<Card>>();
					map.put(CardType.DUI_ZI, clist);
				}
				clist.add(temp);
			} else if (temp.size() == 3) {
				if (maxCard.getBigType() == CardBigType.LAI_ZI) {
					temp.add(maxCard);
					List<List<Card>> clist = map.get(CardType.ZHA_DAN);
					if (clist == null) {
						clist = new ArrayList<List<Card>>();
						map.put(CardType.ZHA_DAN, clist);
					}
					clist.add(temp);
				} else {
					List<List<Card>> clist = map.get(CardType.SAN_BU_DAI);
					if (clist == null) {
						clist = new ArrayList<List<Card>>();
						map.put(CardType.SAN_BU_DAI, clist);
					}
					clist.add(temp);
				}
			} else if (temp.size() == 4) {
				List<List<Card>> clist = map.get(CardType.ZHA_DAN);
				if (clist == null) {
					clist = new ArrayList<List<Card>>();
					map.put(CardType.ZHA_DAN, clist);
				}
				clist.add(temp);
			}
		}
		// 飞机
		List<List<Card>> clist = map.get(CardType.SAN_BU_DAI);
		if (clist != null) {
			int i = 0;
			for (List<Card> flist : clist) {
				if (i + 1 <= clist.size() - 1) {
					if (clist.get(i + 1).get(0).grade - flist.get(0).grade == 1) {
						if (clist.get(i + 1).get(2).getBigType() == CardBigType.LAI_ZI
								|| flist.get(2).getBigType() == CardBigType.LAI_ZI) {

						} else {
							List<List<Card>> feilist = map.get(CardType.FEI_JI);
							if (feilist == null) {
								feilist = new ArrayList<List<Card>>();
								map.put(CardType.FEI_JI, feilist);
							}
							List<Card> temp = new ArrayList<Card>();
							temp.addAll(flist);
							temp.addAll(clist.get(i + 1));
							feilist.add(temp);
						}
					}
				}
				i++;
			}
		}

		clist = map.get(CardType.DUI_ZI);
		if (clist != null) {
			for (int j = 0; j < clist.size(); j++) {
				List<Card> temp = new ArrayList<Card>();
				temp.addAll(clist.get(j));
				int k = j;
				for (; k < clist.size() - 1; k++) {
					if (clist.get(k + 1).get(0).getSmallType().ordinal() == CardSmallType.ER
							.ordinal()) {
						break;
					} else if (clist.get(k + 1).get(0).grade
							- clist.get(k).get(0).grade == 1) {
						temp.addAll(clist.get(k + 1));
					} else {
						break;
					}
				}
				if (temp.size() >= 6) {
					List<List<Card>> feilist = map.get(CardType.LIAN_DUI);
					if (feilist == null) {
						feilist = new ArrayList<List<Card>>();
						map.put(CardType.LIAN_DUI, feilist);
					}
					feilist.add(temp);
				}
			}
		}
		// 连对
		return map;
	}

	/**
	 * 出牌规则
	 * 
	 * @param table
	 * @param role
	 * @param allPai
	 * @param relate
	 * @param targetSize
	 * @param friendSize
	 * @param recyclePai
	 * @return
	 */
	public static boolean getOutPai(ZTDoudizhuTable table,
			List<Integer> allPai, int relate, int targetSize, int friendSize,
			List<Integer> recyclePai, List<Integer> out) {
		List<Integer> data = out;
		List<Integer> lastPai = table.getLastPai();
		int lastSeat = table.getLastPlaySeat();

		int size = lastPai.size();
		List<Card> list = new ArrayList<Card>();
		for (Integer id : allPai) {
			list.add(new Card(id));
		}
		ZTDoudizhuRule.sortCards(list);

		Map<CardType, List<List<Card>>> map = ZTDoudizhuRule.arrangePai(list);

		if (size > 0
				&& (map.containsKey(CardType.ZHA_DAN) || map
						.containsKey(CardType.WANG_ZHA))) {
			// 1.判断是否只剩炸弹, 直接出炸弹
			// 2.上除炸弹外只剩下一手牌，打出炸弹

			int num = 0;
			List<List<Card>> zhaDanList = new ArrayList<List<Card>>();
			if (map.containsKey(CardType.ZHA_DAN)) {
				zhaDanList.addAll(map.get(CardType.ZHA_DAN));
				num++;
			}
			if (map.containsKey(CardType.WANG_ZHA)) {
				zhaDanList.addAll(map.get(CardType.WANG_ZHA));
				num++;
			}
			if (num == map.size() || num + 1 == map.size()) {

				List<Card> lastCards = new ArrayList<Card>();
				for (Integer lid : lastPai) {
					lastCards.add(new Card(lid));
				}
				CardType lastCardType = ZTDoudizhuRule.getCardType(lastCards);
				if (lastCardType != CardType.ZHA_DAN
						&& lastCardType != CardType.WANG_ZHA) {
					if(map.containsKey(CardType.DAN)&& map.get(CardType.DAN).size()>1){ //不止一张单牌
						return false;
					}
					if(map.containsKey(CardType.DUI_ZI) && map.get(CardType.DUI_ZI).size()>1){//不止一个对子
						return false;
					}
					if(map.containsKey(CardType.DAN) && map.containsKey(CardType.DUI_ZI)){
						return false;
					}
					for (Card card : zhaDanList.get(0)) {
						data.add(card.getId());
					}
				} else if (lastCardType == CardType.ZHA_DAN) {
					
					if(map.containsKey(CardType.DAN)&& map.get(CardType.DAN).get(0).size()>1){ //不止一张单牌
						return false;
					}
					if(map.containsKey(CardType.DUI_ZI) && map.get(CardType.DUI_ZI).size()>1){//不止一个对子
						return false;
					}
					if(map.containsKey(CardType.DAN) && map.containsKey(CardType.DUI_ZI)){
						return false;
					}
					
					for (List<Card> myCards : zhaDanList) {
						if (myCards.get(0).getGrade() > lastCards.get(0)
								.getGrade()) {
							for (Card card : myCards) {
								data.add(card.getId());
							}
						}
						if (data.size() > 0) {
							break;
						}
					}
				}
				LogUtil.info("new getOutPai-- 只剩炸弹:" + data);
				return true;
			}

		} else {

			if (table.getLastPlaySeat() == table.getXiajia()) {
				if (relate == 2
						&& table.getMembers().get(table.getShangjia() - 1)
								.getPai().size() < 3) {
					// 若打出上手牌的是上家农民并且其手牌小于3张，则下家农民直接pass
					// TODO 此时直接pass
					LogUtil.info("new getOutPai-- 若打出上手牌的是上家农民并且其手牌小于3张，则下家农民直接pass");
					return true;
				}

				if (relate == 1
						&& table.getMembers().get(table.getOwner() - 1)
								.getPai().size() < 3) {
					List<Card> lastList = new ArrayList<Card>();
					for (Integer id : lastPai) {
						lastList.add(new Card(id));
					}
					ZTDoudizhuRule.sortCards(lastList);
					CardType cardType = ZTDoudizhuRule.getCardType(lastList);

					int ownerCardNum = table.getMembers()
							.get(table.getOwner() - 1).getPai().size();
					if ((ownerCardNum == 1 && cardType == CardType.DAN)
							|| (ownerCardNum == 2 && cardType == CardType.DUI_ZI)) {
						// 打对应牌型最大的牌
						if (map.containsKey(cardType)) {// 在10到Q之间，则打出最大牌型
							int grade = ZTDoudizhuRule.getGrade(lastPai
									.get(lastPai.size() - 1));
							for (int k = map.get(cardType).size() - 1; k >= 0; k--) {
								List<Card> cards = map.get(cardType).get(k);
								if ((cardType == CardType.DAN || cardType == CardType.DUI_ZI)
										&& cards.get(0).getGrade() > grade) {
									for (Card card : cards) {
										data.add(card.getId());
									}
									LogUtil.info("new getOutPai-- 10<=grade<=Q:"
											+ data);
									return true;
								}
							}
						}
					}
				}

			} else if (table.getLastPlaySeat() == table.getShangjia()) {// 上家农民出牌

				List<Card> lastList = new ArrayList<Card>();
				for (Integer id : lastPai) {
					lastList.add(new Card(id));
				}
				ZTDoudizhuRule.sortCards(lastList);
				CardType cardType = ZTDoudizhuRule.getCardType(lastList);

				int ownerCardNum = table.getMembers().get(table.getOwner() - 1)
						.getPai().size();
				if ((ownerCardNum == 1 && cardType == CardType.DAN)
						|| (ownerCardNum == 2 && cardType == CardType.DUI_ZI)) {
					// 打对应牌型最大的牌
					if (map.containsKey(cardType)) {// 在10到Q之间，则打出最大牌型
						int grade = ZTDoudizhuRule.getGrade(lastPai.get(lastPai
								.size() - 1));
						for (int k = map.get(cardType).size() - 1; k >= 0; k--) {
							List<Card> cards = map.get(cardType).get(k);
							if ((cardType == CardType.DAN || cardType == CardType.DUI_ZI)
									&& cards.get(0).getGrade() > grade) {
								for (Card card : cards) {
									data.add(card.getId());
								}
								LogUtil.info("new getOutPai-- 10<=grade<=Q:"
										+ data);
								return true;
							}
						}
					}
				}
				
				if(relate == 2 && table.getMembers().get(table.getXiajia()-1).getPai().size()<=2){//如果下家农民只有1到两张牌，则直接pass
					return true;
				}

				if (relate == 2
						&& !(cardType == CardType.FEI_JI
								|| cardType == CardType.ZHA_DAN || cardType == CardType.WANG_ZHA)) {
					//  若打出上手牌的是下家农民：最大的牌小于10，则打出相同的最小牌型
					// 最大的牌大于Q，则pass
					// 在10到Q之间，则打出最大牌型
					int grade = ZTDoudizhuRule.getGrade(lastPai.get(lastPai
							.size() - 1));
					if (grade < 10) {// 最大的牌小于10
						if (map.containsKey(cardType)) {
							for (List<Card> cards : map.get(cardType)) {
								if ((cardType == CardType.DAN || cardType == CardType.DUI_ZI)
										&& cards.get(0).getGrade() > grade) {
									for (Card card : cards) {
										data.add(card.getId());
									}
									return true;
								} else if (cardType == CardType.SHUN_ZI
										&& cards.size() >= lastPai.size()) {
									int i = 0;
									for (Card card : cards) {
										if (card.getGrade() > grade) {
											break;
										}
										i++;
									}
									if (cards.size() - i >= lastPai.size()) {
										for (int j = i; j < i + lastPai.size(); j++) {
											out.add(cards.get(j).getId());
										}
										return true;
									}

								} else if ((cardType == CardType.SAN_BU_DAI || cardType == CardType.SAN_DAI_YI)
										) {
									grade = ZTDoudizhuRule.getGrade(lastPai.get(1));
									if(cards.get(1).getGrade() > grade)
									for (Card card : cards) {
										data.add(card.getId());
									}
									return true;
								}
							}
						}
					} else if (grade > 12) {

						LogUtil.info("new getOutPai-- grade>12:pass");
						return true;
					} else { // 10<=grade<=Q
						if (map.containsKey(cardType)) {// 在10到Q之间，则打出最大牌型
							for (int k = map.get(cardType).size() - 1; k >= 0; k--) {
								List<Card> cards = map.get(cardType).get(k);

								// List<Card> cards =
								// map.get(cardType).get(map.get(cardType).size()-1);
								if ((cardType == CardType.DAN || cardType == CardType.DUI_ZI)
										&& cards.get(0).getGrade() > grade) {
									for (Card card : cards) {
										data.add(card.getId());
									}
									LogUtil.info("new getOutPai-- 10<=grade<=Q:"
											+ data);
									return true;
								} else if (cardType == CardType.SHUN_ZI
										&& cards.size() >= lastPai.size()) {
									int i = 0;
									for (Card card : cards) {
										if (card.getGrade() > grade) {
											break;
										}
										i++;
									}
									if (cards.size() - i >= lastPai.size()) {
										for (int j = i; j < i + lastPai.size(); j++) {
											out.add(cards.get(j).getId());
										}
										return true;
									}

								} else if ((cardType == CardType.SAN_BU_DAI || cardType == CardType.SAN_DAI_YI)
										) {
									grade = ZTDoudizhuRule.getGrade(lastPai.get(1));
									if(cards.get(1).getGrade() > grade){
										for (Card card : cards) {
											data.add(card.getId());
										}
										return true;
									}
								}
							}
						}
					}
				}
			} else if (table.getLastPlaySeat() == table.getOwner()) { // 地主出牌
				if (table.getMembers().get(table.getShangjia() - 1).getPai()
						.size() <= 2
						|| table.getMembers().get(table.getXiajia() - 1)
								.getPai().size() <= 2) {// 农民任何一个牌<=2时，出对应牌型最大的牌
					List<Card> lastList = new ArrayList<Card>();
					for (Integer id : lastPai) {
						lastList.add(new Card(id));
					}
					ZTDoudizhuRule.sortCards(lastList);
					CardType cardType = ZTDoudizhuRule.getCardType(lastList);

					if (map.containsKey(cardType)) {// 在10到Q之间，则打出最大牌型

						int grade = ZTDoudizhuRule.getGrade(lastPai.get(lastPai
								.size() - 1));

						for (int k = map.get(cardType).size() - 1; k >= 0; k--) {
							List<Card> cards = map.get(cardType).get(k);

							// List<Card> cards =
							// map.get(cardType).get(map.get(cardType).size()-1);
							if ((cardType == CardType.DAN || cardType == CardType.DUI_ZI)
									&& cards.get(0).getGrade() > grade) {
								for (Card card : cards) {
									data.add(card.getId());
								}
								LogUtil.info("new getOutPai-- 10<=grade<=Q:"
										+ data);
								return true;
							} else if (cardType == CardType.SHUN_ZI
									&& cards.size() >= lastPai.size()) {
								int i = 0;
								for (Card card : cards) {
									if (card.getGrade() > grade) {
										break;
									}
									i++;
								}
								if (cards.size() - i >= lastPai.size()) {
									for (int j = i; j < i + lastPai.size(); j++) {
										out.add(cards.get(j).getId());
									}
									return true;
								}

							} else if ((cardType == CardType.SAN_BU_DAI || cardType == CardType.SAN_DAI_YI)
									) {
								grade = ZTDoudizhuRule.getGrade(lastPai.get(1));
								if(cards.get(1).getGrade() > grade){
									for (Card card : cards) {
										data.add(card.getId());
									}
									return true;
								}
							}
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * 自动跟牌ai
	 */
	public static List<Integer> getOutPai(List<Integer> lastPai,
			List<Integer> allPai, int relate, int targetSize, int friendSize,
			List<Integer> recyclePai) {
		List<Integer> data = new ArrayList<Integer>();

		LogUtil.info("lastPai" + lastPai + "allPai" + allPai + "relate"
				+ relate + "targetSize" + targetSize);

		int size = lastPai.size();
		List<Card> list = new ArrayList<Card>();
		for (Integer id : allPai) {
			list.add(new Card(id));
		}
		ZTDoudizhuRule.sortCards(list);

		Map<CardType, List<List<Card>>> map = ZTDoudizhuRule.arrangePai(list);

		if (size == 0) {
			data.add(list.get(0).getId());
		} else {

			if (allPai.size() == 2
					&& size == 1
					&& list.get(list.size() - 1).getBigType() == CardBigType.LAI_ZI) {
				//
			} else if (allPai.size() >= size) {
				List<Card> lastList = new ArrayList<Card>();
				for (Integer id : lastPai) {
					lastList.add(new Card(id));
				}
				ZTDoudizhuRule.sortCards(lastList);
				CardType cardType = ZTDoudizhuRule.getCardType(lastList);

				// 如果只剩下四张炸
				if (allPai.size() == 4 && map.containsKey(CardType.ZHA_DAN)) {
					if (cardType != CardType.ZHA_DAN
							&& cardType != CardType.WANG_ZHA) {
						for (Card card : map.get(CardType.ZHA_DAN).get(0)) {
							data.add(card.getId());
						}
						return data;
					}
				}
				// 如果只剩下2张炸
				if (allPai.size() == 8 && map.containsKey(CardType.ZHA_DAN)) {
					if (cardType != CardType.ZHA_DAN
							&& cardType != CardType.WANG_ZHA) {
						if (map.get(CardType.ZHA_DAN).size() == 2) {
							for (Card card : map.get(CardType.ZHA_DAN).get(0)) {
								data.add(card.getId());
							}
							return data;
						}
					}

				}
				// 如果只剩下王炸炸弹
				if (allPai.size() == 6 && map.containsKey(CardType.WANG_ZHA)
						&& map.containsKey(CardType.ZHA_DAN)) {
					if (cardType != CardType.ZHA_DAN
							&& cardType != CardType.WANG_ZHA) {
						for (Card card : map.get(CardType.ZHA_DAN).get(0)) {
							data.add(card.getId());
						}
						return data;
					}

				}
				if (allPai.size() == 2 && map.containsKey(CardType.WANG_ZHA)) {
					for (Card card : map.get(CardType.WANG_ZHA).get(0)) {
						data.add(card.getId());
					}
					return data;
				}

				// 单的打高些
				if (relate == 2 && targetSize == 1 && size == 1
						&& map.containsKey(CardType.DAN)
						&& map.get(CardType.DAN).size() <= 2) {
					List<List<Card>> danList = map.get(CardType.DAN);
					List<Card> best = danList.get(danList.size() - 1);
					if (best.get(0).getGrade() > lastList.get(0).getGrade()) {
						data.add(best.get(0).getId());
						return data;
					}
				}

				// // 炸弹的用途 高价值才用炸弹
				// if (relate == 1
				// && ((cardType == CardType.DUI_ZI && lastList.get(0).grade ==
				// 15)
				// || (cardType == CardType.SAN_DAI_YI && lastList
				// .get(1).grade >= 14)
				// || (cardType == CardType.SAN_BU_DAI && lastList
				// .get(1).grade >= 14)
				// || (cardType == CardType.LIAN_DUI && lastList
				// .get(0).grade >= 10) || (cardType == CardType.FEI_JI &&
				// lastList
				// .get(0).grade >= 8))) {
				//
				// if (map.containsKey(CardType.ZHA_DAN)
				// || map.containsKey(CardType.WANG_ZHA)) {
				// double rate = 0.5;
				// if (targetSize < 8) {
				// rate = 0.8;
				// } else if (allPai.size() < 8) {
				// rate = 0.7;
				// }
				// if (Math.random() < rate) {
				// if (map.containsKey(CardType.ZHA_DAN)) {
				// for (Card card : map.get(CardType.ZHA_DAN).get(
				// 0)) {
				// data.add(card.getId());
				// }
				// } else {
				// for (Card card : map.get(CardType.WANG_ZHA)
				// .get(0)) {
				// data.add(card.getId());
				// }
				// }
				// return data;
				// }
				//
				// }
				//
				// }
				// 非炸弹同牌数 循环对比 避开有用牌型
				for (int i = 0; i <= allPai.size() - size; i++) {
					List<Card> sub = list.subList(i, i + size);
					CardType myType = ZTDoudizhuRule.getCardType(sub);
					boolean zha = (myType == CardType.WANG_ZHA || myType == CardType.ZHA_DAN);

					if (zha && (cardType != CardType.ZHA_DAN)) {
						continue;
					}
					boolean flag = ZTDoudizhuRule.isOvercomePrev(sub, myType,
							lastList, cardType);
					if (flag) {
						if (myType == CardType.SAN_DAI_YI) {// 三带1要重新组合
							boolean good = false;
							if (map.containsKey(CardType.ZHA_DAN)) {// 尽量不拆炸弹
								for (List<Card> cards : map
										.get(CardType.ZHA_DAN)) {
									if (cards.get(0).getGrade() == sub.get(0)
											.getGrade()
											|| cards.get(0).getGrade() == sub
													.get(3).getGrade()) {
										good = true;
										break;
									}
								}
								if (good) {
									continue;
								}
							}
							if (cardType == CardType.SAN_DAI_YI
									&& sub.get(0).getId() != 53
									&& sub.get(3).getId() != 53
									&& sub.get(0).getId() != 54
									&& sub.get(3).getId() != 54) {
								for (Card card : sub) {
									data.add(card.getId());
								}
								return data;
							}

							if (map.containsKey(CardType.DAN)) {
								List<Card> cards = map.get(CardType.DAN).get(0);
								if (cards.get(0).getId() == 53
										|| cards.get(0).getId() == 54) {
									if (map.containsKey(CardType.DUI_ZI)) {
										// 三带一中带的单牌是王的，优先拆对
										List<Card> cards2 = map.get(
												CardType.DUI_ZI).get(0);
										if (sub.get(0).getGrade() == sub.get(1)
												.getGrade()) {
											sub.remove(3);
										} else {
											sub.remove(0);
										}
										for (Card card : sub) {
											data.add(card.getId());
										}
										data.add(cards2.get(0).getId());
										return data;
									} else if (allPai.size() / 3 >= 2
											&& allPai.size() % 3 == 1
											&& map.get(CardType.SAN_BU_DAI)
													.size() == allPai.size() / 3) {
										// 剩下的牌型是多个（2个以上）三不带加一个王
										if (allPai.size() == 7
												&& map.get(CardType.SAN_BU_DAI)
														.get(1).get(0)
														.getGrade() >= 13
												&& targetSize <= 10) {
											// 剩下2个三不带和一个王，并且最大的三不打是K以上，并且对方的手牌小于10张，则出三个大的带王
											data.add(sub.get(3).getId());
											for (Card card : map.get(
													CardType.SAN_BU_DAI).get(1)) {
												data.add(card.getId());
											}
											return data;

										} else {
											// 拆掉三不带
											sub.remove(3);
											if (sub.get(0).getGrade() == map
													.get(CardType.SAN_BU_DAI)
													.get(0).get(0).getGrade()) {
												// 打出的三不带刚好是最小的那个,拆掉第二个三不带
												if (map.get(CardType.SAN_BU_DAI)
														.size() == 2) {
													sub.remove(0);
													sub.remove(0);
													for (Card card : map
															.get(CardType.SAN_BU_DAI)
															.get(1)) {
														sub.add(card);
													}
												} else {
													sub.add(map
															.get(CardType.SAN_BU_DAI)
															.get(1).get(0));
												}
											} else {
												// 打出的三不带不是最小的，拆掉最小的那个
												sub.add(map
														.get(CardType.SAN_BU_DAI)
														.get(0).get(1));
											}
											for (Card card : sub) {
												data.add(card.getId());
											}
											return data;
										}
									} else if (map
											.containsKey(CardType.SHUN_ZI)) {
										boolean isShunChai = false;// 是否拆顺子
										for (List<Card> cardlist : map
												.get(CardType.SHUN_ZI)) {
											if (cardlist.size() >= 6) {
												// 有超过6张的顺子，拆顺子
												sub.remove(3);
												sub.add(cardlist.get(0));
												for (Card card : sub) {
													data.add(card.getId());
												}
												isShunChai = true;
												break;
											}
										}
										if (isShunChai) {
											return data;
										}
									}

								}
								// 选择低价值的
								if (cards.get(0).grade > 13) {
									if (map.containsKey(CardType.DUI_ZI)) {
										cards = map.get(CardType.DUI_ZI).get(0);
										if (cards.get(0).grade > 12) {
											continue;
										} else {
											//
											LogUtil.info("su1b" + sub);
											if (sub.get(0).getGrade() == sub
													.get(1).getGrade()) {
												sub.remove(3);
											} else {
												sub.remove(0);
											}
											for (Card card : sub) {
												data.add(card.getId());
											}
											data.add(cards.get(0).getId());
											return data;
										}
									} else {
										continue;
									}

								} else {
									LogUtil.info("su2b" + sub);
									if (sub.get(0).getGrade() == sub.get(1)
											.getGrade()) {
										sub.remove(3);
									} else {
										sub.remove(0);
									}
									for (Card card : sub) {
										data.add(card.getId());
									}
									data.add(cards.get(0).getId());
									return data;
								}

							} else if (map.containsKey(CardType.DUI_ZI)) {
								List<Card> cards = map.get(CardType.DUI_ZI)
										.get(0);
								if (cards.get(0).grade > 12) {
									continue;
								} else {
									//
									LogUtil.info("su3b" + sub);
									if (sub.get(0).getGrade() == sub.get(1)
											.getGrade()) {
										sub.remove(3);
									} else {
										sub.remove(0);
									}
									for (Card card : sub) {
										data.add(card.getId());
									}
									data.add(cards.get(0).getId());
									return data;
								}
							} else {
								for (Card card : sub) {
									data.add(card.getId());
								}
							}
							return data;
						} else if (myType == CardType.SI_DAI_ER) {// 三带1要重新组合{
							if (sub.get(0).getGrade() == sub.get(0).getGrade()) {

							}
						} else if (myType == CardType.DAN) {
							boolean good = false;
							if (map.containsKey(CardType.ZHA_DAN)) {// 尽量不拆顺子
								for (List<Card> cards : map
										.get(CardType.ZHA_DAN)) {
									if (cards.get(0).getGrade() == sub.get(0)
											.getGrade()) {
										good = true;
										break;
									}
								}
								if (good) {
									continue;
								}
							}
							if (map.containsKey(CardType.SAN_BU_DAI)) {// 尽量不拆三
								if (sub.get(0).getGrade() < 13) {
									for (List<Card> cards : map
											.get(CardType.SAN_BU_DAI)) {
										if (cards.get(0).getGrade() == sub.get(
												0).getGrade()) {
											good = true;
											break;
										}
									}
									if (good) {
										continue;
									}
								}
							}

							if (map.containsKey(CardType.SHUN_ZI)) {// 尽量不拆顺子
								if (relate == 1 && targetSize > 10) {
									for (List<Card> cards : map
											.get(CardType.SHUN_ZI)) {
										for (Card card : cards) {
											if (card.getId() == sub.get(0)
													.getId()) {
												good = true;
												break;
											}
										}
										if (good) {
											break;
										}
									}
									if (good) {
										continue;
									}
								}
							}
							if (map.containsKey(CardType.DUI_ZI)) {// 尽量不拆对子
								if (targetSize > 10) {
									for (List<Card> cards : map
											.get(CardType.DUI_ZI)) {
										for (Card card : cards) {
											if (card.getId() == sub.get(0)
													.getId()) {
												good = true;
												break;
											}
										}
										if (good) {
											break;
										}
									}
									if (good) {
										continue;
									}
								}
							}
							// 牌型暂时都完美的不打后面再勉强拆分
							if (!good) {
								if (!(myType == CardType.WANG_ZHA || myType == CardType.ZHA_DAN)) {
									for (Card card : sub) {
										data.add(card.getId());
									}
								}

							}
						} else if (myType == CardType.DUI_ZI) {
							boolean good = false;
							if (map.containsKey(CardType.ZHA_DAN)) {// 尽量不拆炸弹
								for (List<Card> cards : map
										.get(CardType.ZHA_DAN)) {
									if (cards.get(0).getGrade() == sub.get(0)
											.getGrade()) {
										good = true;
										break;
									}
								}
								if (good) {
									continue;
								}
							}
							if (map.containsKey(CardType.SAN_BU_DAI)) {// 尽量不拆三
								if (sub.get(0).grade < 12) {
									for (List<Card> cards : map
											.get(CardType.SAN_BU_DAI)) {
										if (cards.get(1).getGrade() == sub.get(
												0).getGrade()) {
											good = true;
											break;
										}
									}
									if (good) {
										continue;
									}
								}
							}
							if (map.containsKey(CardType.SHUN_ZI)) {// 尽量不拆顺子
								if (targetSize > 10) {
									for (List<Card> cards : map
											.get(CardType.SHUN_ZI)) {
										for (Card card : cards) {
											if (card.getId() == sub.get(0)
													.getId()) {
												good = true;
												break;
											}
										}
										if (good) {
											break;
										}
									}
									if (good) {
										continue;
									}
								}

							}

							// 牌型暂时都完美的不打后面再勉强拆分
							if (!good) {
								if (!(myType == CardType.WANG_ZHA || myType == CardType.ZHA_DAN)) {
									for (Card card : sub) {
										data.add(card.getId());
									}
								}
							}

						} else if (myType == CardType.SHUN_ZI) {
							boolean good = false;
							if (map.containsKey(CardType.ZHA_DAN)) {// 尽量不拆炸弹
								for (List<Card> cards : map
										.get(CardType.ZHA_DAN)) {
									if (cards.get(0).getGrade() == sub.get(0)
											.getGrade()) {
										good = true;
										break;
									}
								}
								if (good) {
									continue;
								}
							}

							// 牌型暂时都完美的不打后面再勉强拆分
							if (!good) {
								if (!(myType == CardType.WANG_ZHA || myType == CardType.ZHA_DAN)) {
									for (Card card : sub) {
										data.add(card.getId());
									}
								}
							}

						} else if (myType == CardType.WANG_ZHA) {

						} /*
						 * else if (myType == CardType.ZHA_DAN) {
						 * 
						 * if (relate == 1) { double rate = 0.4; if (targetSize
						 * < 10) { rate = 0.8; }
						 * 
						 * if (Math.random() < rate) { for (Card card : sub) {
						 * data.add(card.getId()); } } } }
						 */else if (myType == CardType.FEI_JI) {
							if (!map.containsKey(CardType.ZHA_DAN)
									&& !map.containsKey(CardType.WANG_ZHA)
									&& (sub.size() == 6 || sub.size() == 8)) {
								for (Card card : sub) {
									data.add(card.getId());
								}
							}
						} else {
							if ((myType == CardType.ZHA_DAN || myType == CardType.WANG_ZHA)) {
								return data;
							}
							for (Card card : sub) {
								data.add(card.getId());
							}
						}

						// 找到解决方案
						if (data.size() > 0) {
							return data;
						}

					}
				}
				if (data.size() > 0) {
					return data;
				}
				if (cardType == CardType.DUI_ZI || cardType == CardType.DAN) {
					LogUtil.info("太完美");
					// 这里是瑕疵出牌
					for (int i = 0; i <= allPai.size() - size; i++) {
						List<Card> sub = list.subList(i, i + size);
						CardType myType = ZTDoudizhuRule.getCardType(sub);
						boolean flag = ZTDoudizhuRule.isOvercomePrev(sub,
								myType, lastList, cardType);
						if (flag && myType != CardType.WANG_ZHA) {
							boolean good = false;
							if (map.containsKey(CardType.ZHA_DAN)) {// 尽量不拆炸弹
								for (List<Card> cards : map
										.get(CardType.ZHA_DAN)) {
									if (cards.get(0).getId() == sub.get(0)
											.getId()) {
										good = true;
										break;
									}
								}
								if (good) {
									continue;
								}
							}
							if ((myType == CardType.ZHA_DAN || myType == CardType.WANG_ZHA)) {
								return data;
							}
							for (Card card : sub) {
								data.add(card.getId());
							}
							break;
						}
					}
				}

				// if (data.size() == 0 && relate == 1) {
				// // 假设上面没有方案试试炸弹
				// // 假如
				// boolean flag = true;
				// if (map.containsKey(CardType.ZHA_DAN)
				// && cardType != CardType.ZHA_DAN
				// && cardType != CardType.WANG_ZHA
				// && (allPai.size() <= 7 || targetSize <= 5)) {
				// for (Card card : map.get(CardType.ZHA_DAN).get(0)) {
				// data.add(card.getId());
				// }
				// flag = false;
				//
				// }
				// // 假如
				// if (flag && map.containsKey(CardType.WANG_ZHA)
				// && (allPai.size() <= 7 || targetSize <= 5)) {
				// for (Card card : map.get(CardType.WANG_ZHA).get(0)) {
				// data.add(card.getId());
				// }
				// }
				// }
			}
		}
		// 赖子最后不要打
		if (allPai.size() - data.size() == 1
				&& list.get(list.size() - 1).getBigType() == CardBigType.LAI_ZI) {
			data.clear();
		}
		// 一半的牌要省着点
		if (data.size() > 0 && targetSize > 10) {
			List<Card> lastList = new ArrayList<Card>();
			for (Integer id : data) {
				lastList.add(new Card(id));
			}
			ZTDoudizhuRule.sortCards(lastList);
			CardType cardType = ZTDoudizhuRule.getCardType(lastList);
			CardSmallType type = lastList.get(lastList.size() - 1)
					.getSmallType();
			if (type == CardSmallType.A) {
				if (cardType != CardType.DAN) {
					if (Math.random() > 0.8) {
						data.clear();
					}
				} else {
					if (Math.random() > 0.8) {
						data.clear();
					}
				}

			} else if (type == CardSmallType.ER) {
				if (cardType != CardType.DAN) {
					if (Math.random() > 0.7) {
						data.clear();
					}
				} else {
					if (Math.random() > 0.8) {
						data.clear();
					}
				}
			} else if (type == CardSmallType.LAI_ZI
					|| type == CardSmallType.DA_WANG
					|| type == CardSmallType.XIAO_WANG) {
				if (cardType != CardType.DAN) {
					if (Math.random() > 0.5) {
						data.clear();
					}
				} else {
					if (Math.random() > 0.8) {
						data.clear();
					}
				}

			}
		}
		return data;
	}

	/**
	 * 优先出好牌
	 * 
	 * @param paiList
	 * @param map
	 * @return
	 */
	public static List<Integer> aiWithGood(List<Card> paiList,
			Map<CardType, List<List<Card>>> map) {
		List<Integer> data = new ArrayList<Integer>();
		Card big = paiList.get(paiList.size() - 1);
		if (map.containsKey(CardType.SHUN_ZI)) {
			// 检测顺子造成的单牌
			List<List<Card>> slist = map.get(CardType.SHUN_ZI);
			List<List<Card>> dlist = map.get(CardType.DUI_ZI);
			List<List<Card>> zdlist = map.get(CardType.ZHA_DAN);
			List<List<Card>> sdlist = map.get(CardType.SAN_BU_DAI);
			for (List<Card> shunList : slist) {
				int count = 0;
				for (Card card : shunList) {
					if (zdlist != null) {
						for (List<Card> zhadanlist : zdlist) {
							if (card.getGrade() == zhadanlist.get(0).getGrade()) {
								if (shunList.size() == 5) {
									count = count + 2;
									continue;
								} else {
									count = count + 3;
									break;
								}
							}
						}
					}
					if (dlist != null) {
						for (List<Card> duiList : dlist) {
							for (Card duizi : duiList) {
								if (card.getGrade() == duizi.getGrade()) {
									count++;
									break;
								}
							}
						}
					}
					if (sdlist != null) {
						for (List<Card> sanList : sdlist) {
							for (Card san : sanList) {
								if (card.getGrade() == san.getGrade()) {
									count++;
									break;
								}
							}
						}
					}
				}
				LogUtil.info("zdcount" + count + ":" + slist.size());
				if (count < 3) {
					for (Card fix : shunList) {
						data.add(fix.getId());
					}
					return data;
				}
			}

			if (data.size() > 0) {
				return data;
			}
		}
		if (map.containsKey(CardType.FEI_JI)) {
			List<List<Card>> slist = map.get(CardType.FEI_JI);
			List<List<Card>> zdlist = map.get(CardType.ZHA_DAN);
			if (slist.get(0).get(0).grade < 12) {
				// 炸弹不拆
				boolean isGood = false;
				// 规则： 不拆炸弹
				// if (zdlist != null) {
				// for (Card card : slist.get(0)) {
				// for (List<Card> zhadanlist : zdlist) {
				// if (card.getGrade() == zhadanlist.get(0)
				// .getGrade()) {
				// isGood = true;
				// break;
				// }
				// }
				// if(isGood){
				// break;
				// }
				// }
				//
				// }
				if (!isGood) {
					for (Card card : slist.get(0)) {
						data.add(card.getId());
					}

					List<List<Card>> dlist = map.get(CardType.DAN);

					if (dlist != null && dlist.size() > 1) {
						data.add(dlist.get(0).get(0).getId());
						data.add(dlist.get(1).get(0).getId());
					} else {
						dlist = map.get(CardType.DUI_ZI);
						if (dlist != null && dlist.size() > 0) {
							data.add(dlist.get(0).get(0).getId());
							data.add(dlist.get(0).get(1).getId());
						}
					}
				}

			}
			if (data.size() > 0) {
				return data;
			}
		}

		if (map.containsKey(CardType.LIAN_DUI)) {
			List<List<Card>> slist = map.get(CardType.LIAN_DUI);
			List<List<Card>> zdlist = map.get(CardType.ZHA_DAN);
			if (slist.get(0).get(0).grade < 9) {
				boolean isGood = false;
				// 规则： 不拆炸弹
				// if (zdlist != null) {
				// for (List<Card> zhadanlist : zdlist) {
				// for (Card card : slist.get(0)) {
				// if (card.getGrade() == zhadanlist.get(0)
				// .getGrade()) {
				// isGood = true;
				// break;
				// }
				// }
				// if(isGood){
				// break;
				// }
				// }
				// }
				if (!isGood) {
					for (Card card : slist.get(0)) {
						data.add(card.getId());
					}
				}

			}
			if (data.size() > 0) {
				return data;
			}

		}
		if (map.containsKey(CardType.SAN_BU_DAI)) {
			List<List<Card>> slist = map.get(CardType.SAN_BU_DAI);
			if (paiList.size() > 11) {
				if (slist.get(0).get(1).grade < 9) {

					List<List<Card>> dlist = map.get(CardType.DAN);
					if (dlist != null) {
						for (Card card : slist.get(0)) {
							data.add(card.getId());
						}
						data.add(dlist.get(0).get(0).getId());
					} else {
						dlist = map.get(CardType.DUI_ZI);
						if (dlist != null) {

							for (List<Card> duilist : dlist) {
								if (slist.get(0).contains(
										duilist.get(0).getId())
										&& slist.get(0).get(0).grade > 11) {
									continue;
								} else {
									for (Card card : slist.get(0)) {
										data.add(card.getId());
									}
									data.add(duilist.get(0).getId());
									break;
								}
							}

						}
					}
				}
			} else {
				if (slist.get(0).get(1).grade < 12) {

					List<List<Card>> dlist = map.get(CardType.DAN);
					if (dlist != null) {
						for (Card card : slist.get(0)) {
							data.add(card.getId());
						}
						data.add(dlist.get(0).get(0).getId());
					} else {
						dlist = map.get(CardType.DUI_ZI);
						if (dlist != null) {

							for (List<Card> duilist : dlist) {
								if (slist.get(0).contains(
										duilist.get(0).getId())
										&& slist.get(0).get(0).grade > 11) {
									continue;
								} else {
									for (Card card : slist.get(0)) {
										data.add(card.getId());
									}
									data.add(duilist.get(0).getId());
									break;
								}
							}

						}
					}
				} else if (paiList.size() <= 6) {

					if (big.getBigType() != CardBigType.LAI_ZI) {
						for (Card card : slist.get(0)) {
							data.add(card.getId());
						}
						List<List<Card>> dlist = map.get(CardType.DAN);
						if (dlist != null) {
							data.add(dlist.get(0).get(0).getId());
						} else {
							dlist = map.get(CardType.DUI_ZI);
							if (dlist != null) {
								for (List<Card> duilist : dlist) {
									data.add(duilist.get(0).getId());
									break;
								}

							}
						}
					}

				}
			}

			if (data.size() > 0) {
				return data;
			}
		}
		return data;
	}

	/**
	 * 单独对炸弹和赖子进行优化前提判断
	 * 
	 * @param paiList
	 * @param map
	 * @return
	 */
	public static List<Integer> aiWithZha(List<Card> paiList,
			Map<CardType, List<List<Card>>> map) {
		Card big = paiList.get(paiList.size() - 1);
		List<Integer> data = new ArrayList<Integer>();
		// 指正有炸弹的ai
		if (paiList.size() == 4 && map.containsKey(CardType.ZHA_DAN)) {
			for (Card card : map.get(CardType.ZHA_DAN).get(0)) {
				data.add(card.getId());
			}
			return data;
		}

		// 如果只剩下2张炸
		if (paiList.size() == 8 && map.containsKey(CardType.ZHA_DAN)) {
			if (map.get(CardType.ZHA_DAN).size() == 2) {
				for (Card card : map.get(CardType.ZHA_DAN).get(0)) {
					data.add(card.getId());
					return data;
				}
			}
		}
		// 如果只剩下王炸炸弹
		if (paiList.size() == 6 && map.containsKey(CardType.WANG_ZHA)
				&& map.containsKey(CardType.ZHA_DAN)) {
			for (Card card : map.get(CardType.ZHA_DAN).get(0)) {
				data.add(card.getId());
				return data;
			}
		}

		// 指正有网炸有赖子的ai
		if (paiList.size() == 3 && map.containsKey(CardType.WANG_ZHA)) {
			if (big.getBigType() == CardBigType.LAI_ZI) {
				data.add(paiList.get(0).getId());
				data.add(paiList.get(2).getId());
			} else {
				data.add(paiList.get(1).getId());
				data.add(paiList.get(2).getId());
			}
			return data;
		}
		// 指正有对子有赖子的ai
		if (paiList.size() == 3 && big.getBigType() == CardBigType.LAI_ZI) {
			if (map.containsKey(CardType.DUI_ZI)) {
				for (Card card : paiList) {
					data.add(card.getId());
				}

			} else {
				data.add(paiList.get(1).getId());
				data.add(paiList.get(2).getId());
			}
			return data;
		}
		// 指正有炸弹有赖子的ai
		if (paiList.size() == 5 && map.containsKey(CardType.ZHA_DAN)) {
			if (big.getBigType() == CardBigType.LAI_ZI
					&& big.getId() != map.get(CardType.ZHA_DAN).get(0).get(3)
							.getId()) {
				for (Card card : map.get(CardType.ZHA_DAN).get(0)) {
					data.add(card.getId());
				}
				data.remove(3);
				data.add(big.getId());
				return data;
			}

		}
		// 指正有炸弹有赖子的ai
		if (paiList.size() == 6 && map.containsKey(CardType.ZHA_DAN)) {
			boolean flag = false;
			if (big.getBigType() == CardBigType.LAI_ZI
					&& big.getId() == map.get(CardType.ZHA_DAN).get(0).get(3)
							.getId()) {
				flag = true;
			}
			if (flag) {
				if (map.containsKey(CardType.DAN)) {
					List<List<Card>> slist = map.get(CardType.DAN);
					for (Card card : slist.get(0)) {
						data.add(card.getId());
					}
					return data;
				} else if (map.containsKey(CardType.DUI_ZI)) {
					List<List<Card>> slist = map.get(CardType.DUI_ZI);
					for (Card card : slist.get(0)) {
						data.add(card.getId());
					}
					return data;
				}

			} else {
				if (map.containsKey(CardType.DAN)) {
					if (big.getBigType() == CardBigType.LAI_ZI) {
						List<List<Card>> slist = map.get(CardType.DAN);
						for (Card card : slist.get(0)) {
							data.add(card.getId());
						}
						data.add(big.getId());
						return data;
					} else {
						List<List<Card>> slist = map.get(CardType.DAN);
						for (Card card : slist.get(0)) {
							data.add(card.getId());
						}
						return data;
					}

				}
			}
		}

		// 指正有炸弹有赖子的ai
		if (paiList.size() <= 8 && big.getBigType() == CardBigType.LAI_ZI) {
			if (map.containsKey(CardType.ZHA_DAN)) {
				boolean flag = false;
				for (Card card : map.get(CardType.ZHA_DAN).get(0)) {
					if (big.getId() == card.getId()) {
						flag = true;
						break;
					}
				}

				// 处理下非赖子炸弹
				if (!flag) {
					if (map.containsKey(CardType.DUI_ZI)) {
						List<List<Card>> slist = map.get(CardType.DUI_ZI);
						for (Card card : slist.get(0)) {
							data.add(card.getId());
						}
						data.add(big.getId());
						if (map.containsKey(CardType.DAN)) {
							slist = map.get(CardType.DAN);
							for (Card card : slist.get(0)) {
								data.add(card.getId());
							}
						}
						return data;
					}
					if (map.containsKey(CardType.DAN)) {
						List<List<Card>> slist = map.get(CardType.DAN);
						for (Card card : slist.get(0)) {
							data.add(card.getId());
						}
						data.add(big.getId());
						return data;
					}

				}
			}

		}

		// 指正有对2有三张的ai
		if (paiList.size() == 5 && map.containsKey(CardType.SAN_BU_DAI)
				&& map.containsKey(CardType.DUI_ZI)) {
			if (map.get(CardType.DUI_ZI).get(0).get(0).getGrade() == 14) {
				for (Card card : map.get(CardType.DUI_ZI).get(0)) {
					data.add(card.getId());
				}
				return data;
			}
		}

		return data;
	}

	// 托管后手
	public static List<Integer> tuoGuanOutPai(List<Integer> lastPai,
			List<Integer> allpai) {
		List<Integer> data = new ArrayList<Integer>();
		List<Card> lastlist = new ArrayList<Card>();
		List<Card> list = new ArrayList<Card>();
		int size = lastPai.size();

		for (Integer id : lastPai) {
			lastlist.add(new Card(id));
		}

		for (Integer id : allpai) {
			list.add(new Card(id));
		}
		ZTDoudizhuRule.sortCards(list);
		Map<CardType, List<List<Card>>> map = arrangePai(list);

		ZTDoudizhuRule.sortCards(lastlist);
		CardType cardType = ZTDoudizhuRule.getCardType(lastlist);

		if (cardType == CardType.DAN) {
			if (map.containsKey(CardType.DAN)) {
				for (int num = 0; num < map.get(CardType.DAN).size(); num++) {
					List<Card> cardList = map.get(CardType.DAN).get(num);
					if (cardList.get(0).getGrade() > lastlist.get(0).getGrade()) {
						data.add(cardList.get(0).getId());
						break;
					}
				}

				if (data.size() > 0) {
					return data;
				}
			}

			if (map.containsKey(CardType.DUI_ZI)) {
				for (int num = 0; num < map.get(CardType.DUI_ZI).size(); num++) {
					List<Card> cardList = map.get(CardType.DUI_ZI).get(num);
					if (getCardType(cardList) == CardType.WANG_ZHA) {
						break;
					}
					Card card = cardList.get(0);
					if (card.getGrade() > lastlist.get(0).getGrade()) {
						data.add(card.getId());
						break;
					}
				}

				if (data.size() > 0) {
					return data;
				}
			}

			if (map.containsKey(CardType.SAN_BU_DAI)) {
				for (int num = 0; num < map.get(CardType.SAN_BU_DAI).size(); num++) {
					List<Card> cardList = map.get(CardType.SAN_BU_DAI).get(num);
					Card card = cardList.get(0);
					if (card.getGrade() > lastlist.get(0).getGrade()) {
						data.add(card.getId());
						break;
					}
				}

				if (data.size() > 0) {
					return data;
				}
			}

			if (map.containsKey(CardType.SHUN_ZI)) {
				for (int num = 0; num < map.get(CardType.SHUN_ZI).size(); num++) {
					List<Card> cardList = map.get(CardType.SHUN_ZI).get(num);
					Card card = cardList.get(0);
					if (card.getGrade() > lastlist.get(0).getGrade()) {
						data.add(card.getId());
						break;
					}
				}

				if (data.size() > 0) {
					return data;
				}
			}
		} else if (cardType == CardType.DUI_ZI) {

			if (map.containsKey(CardType.DUI_ZI)) {
				for (int num = 0; num < map.get(CardType.DUI_ZI).size(); num++) {
					List<Card> cardList = map.get(CardType.DUI_ZI).get(num);
					if (cardList.get(0).getGrade() > lastlist.get(0).getGrade()) {
						for (Card card : cardList) {
							data.add(card.getId());
						}
						break;
					}
				}

				if (data.size() > 0) {
					return data;
				}
			}

			if (map.containsKey(CardType.SAN_BU_DAI)) {
				for (int num = 0; num < map.get(CardType.SAN_BU_DAI).size(); num++) {
					List<Card> cardList = map.get(CardType.SAN_BU_DAI).get(num);
					if (cardList.get(0).getGrade() > lastlist.get(0).getGrade()) {
						cardList.remove(2);
						for (Card card : cardList) {
							data.add(card.getId());
						}
						break;
					}
				}

				if (data.size() > 0) {
					return data;
				}
			}
		} else if (cardType == CardType.SAN_BU_DAI) {

			if (map.containsKey(CardType.SAN_BU_DAI)) {
				for (int num = 0; num < map.get(CardType.SAN_BU_DAI).size(); num++) {
					List<Card> cardList = map.get(CardType.SAN_BU_DAI).get(num);
					if (cardList.get(0).getGrade() > lastlist.get(0).getGrade()) {
						for (Card card : cardList) {
							data.add(card.getId());
						}
						break;
					}
				}

				if (data.size() > 0) {
					return data;
				}
			}

		} else if (cardType == CardType.FEI_JI || cardType == CardType.SHUN_ZI
				|| cardType == CardType.LIAN_DUI) {
			if (map.containsKey(cardType)) {
				for (int num = 0; num < map.get(cardType).size(); num++) {
					List<Card> cardList = map.get(cardType).get(num);
					if (cardList.get(0).getGrade() > lastlist.get(0).getGrade()
							&& cardList.size() == lastlist.size()) {
						for (Card card : cardList) {
							data.add(card.getId());
						}
						break;
					}

					if (data.size() > 0) {
						return data;
					}
				}
			}
		}

		if (cardType == CardType.SAN_DAI_YI && allpai.size() >= size) {
			for (int i = 0; i <= allpai.size() - size; i++) {
				List<Card> sub = new ArrayList<Card>();
				if(i == allpai.size() - size) {
					sub = list.subList(i, i + size - 1);
					sub.add(list.get(i + size -1));
				}else {
					sub = list.subList(i, i + size);
					
				}
				
				CardType myType = ZTDoudizhuRule.getCardType(sub);
				boolean flag = ZTDoudizhuRule.isOvercomePrev(sub, myType,
						lastlist, cardType);
				if (flag) {

					if (myType == CardType.SAN_DAI_YI) {

						boolean good = false;
						if (map.containsKey(CardType.ZHA_DAN)) {// 尽量不拆炸弹
							for (List<Card> cards : map.get(CardType.ZHA_DAN)) {
								if (cards.get(0).getGrade() == sub.get(1)
										.getGrade()) {
									good = true;
									break;
								}
							}
							if (good) {
								continue;
							}
						}
						
						if (sub.get(0).getGrade() == sub.get(1).getGrade()) {
							sub.remove(3);
						} else {
							sub.remove(0);
						}

						if (map.containsKey(CardType.DAN)) {
							List<Card> cardList = map.get(CardType.DAN).get(0);
							for (Card card : sub) {
								data.add(card.getId());
							}
							for (Card card : cardList) {
								data.add(card.getId());
							}
							if (data.size() > 0) {
								return data;
							}
						}

						if (map.containsKey(CardType.DUI_ZI)) {
							List<Card> cardList = map.get(CardType.DUI_ZI).get(
									0);
							for (Card card : sub) {
								data.add(card.getId());
							}
							data.add(cardList.get(0).getId());
							if (data.size() > 0) {
								return data;
							}
						}

						if (map.containsKey(CardType.SAN_BU_DAI)) {
							List<Card> cardList = map.get(CardType.SAN_BU_DAI)
									.get(0);
							for (Card card : sub) {
								data.add(card.getId());
							}
							data.add(cardList.get(0).getId());
							if (data.size() > 0) {
								return data;
							}
						}

						if (map.containsKey(CardType.SHUN_ZI)) {
							List<Card> cardList = map.get(CardType.SHUN_ZI)
									.get(0);
							for (Card card : sub) {
								data.add(card.getId());
							}
							data.add(cardList.get(0).getId());
							if (data.size() > 0) {
								return data;
							}
						}
					}
				}
			}
		}

		if (map.containsKey(CardType.ZHA_DAN)) {
			List<Card> cardlist = map.get(CardType.ZHA_DAN).get(0);
			ZTDoudizhuRule.sortCards(cardlist);
			boolean flag = ZTDoudizhuRule.isOvercomePrev(cardlist,
					CardType.ZHA_DAN, lastlist, cardType);
			if (flag) {
				for (Card card : cardlist) {
					data.add(card.getId());
				}
				if (data.size() > 0) {
					return data;
				}
			}
		}

		if (map.containsKey(CardType.WANG_ZHA)) {
			List<Card> cardlist = map.get(CardType.WANG_ZHA).get(0);
			ZTDoudizhuRule.sortCards(cardlist);
			for (Card card : cardlist) {
				data.add(card.getId());
			}
			if (data.size() > 0) {
				return data;
			}
		}

		return data;
	}

	// 托管先手
	public static List<Integer> tuoGuanselfOutPai(List<Integer> allpai) {
		List<Integer> data = new ArrayList<Integer>();
		List<Card> list = new ArrayList<Card>();
		for (Integer id : allpai) {
			list.add(new Card(id));
		}
		ZTDoudizhuRule.sortCards(list);

		if (allpai.size() <= 9) {

			Map<CardType, List<List<Card>>> map = arrangePai(list);
			if (allpai.size() == 8 || allpai.size() == 6) {
				if (aiWithZha(list, map).size() > 0) {
					List<Card> cardList = new ArrayList<Card>();
					cardList = map.get(CardType.ZHA_DAN).get(0);
					for (Card card : cardList) {
						data.add(card.getId());
					}
					return data;
				}
			}

			if (getCardType(list) != null) {
				data.addAll(allpai);
				return data;
			}
		}

		int sanBuDaiNumber = 0;
		List<Card> sanBuDaiList = new ArrayList<Card>();
		List<Card> smallPai = new ArrayList<Card>();
		// 循环得到最小的牌型
		for (int i = 0; i < list.size(); i++) {
			List<Card> paiList = new ArrayList<Card>();
			paiList.addAll(smallPai);
			paiList.add(list.get(i));

			if (isDuiZi(paiList) || isDan(paiList)) {
				smallPai.add(list.get(i));
			} else if (isDuiWang(paiList)) {
				smallPai.clear();
			} else if (isSanBuDai(paiList)) {
				if (allpai.size() == 3) {
					smallPai.add(list.get(i));
				} else {
					if(i < list.size() - 1) {
						paiList.add(list.get(i + 1));
						if (isZhaDan(paiList)) {
							i++;
						}else {
							sanBuDaiNumber++;
							if (sanBuDaiNumber == 1) {
								paiList.remove(3);
								sanBuDaiList.addAll(paiList);
							}
						}
					}
					smallPai.clear();
				}
			} else {
				break;
			}
		}

		if (smallPai == null || smallPai.size() == 0) {
			if(sanBuDaiList.size() > 0) {
				data.add(sanBuDaiList.get(0).getId());
				return data;
			}else { //只有炸弹且三个炸弹或以上
				data.add(list.get(0).getId());
				return data;
			}
		}

		for (Card card : smallPai) {
			data.add(card.getId());
		}

		return data;
	}

	public static boolean selfOutPai(ZTDoudizhuTable table,
			List<Integer> allPai, int targetSize, int friendSize,
			List<Integer> recyclePai, List<Integer> out) {
		List<Integer> data = out;
		List<Card> list = new ArrayList<Card>();
		for (Integer id : allPai) {
			list.add(new Card(id));
		}
		ZTDoudizhuRule.sortCards(list);
	
		// //获取农民剩余牌数量
		// Map<Integer, Integer> remainCardNumMap = new
		// HashMap<Integer,Integer>();
		//
		// 获取最小数量牌的农民
		int nmseat1 = table.getXiajia();
		int nmcardNum1 = table.getMembers().get(table.getXiajia() - 1).getPai()
				.size();

		int nmseat2 = table.getShangjia();
		int nmcardNum2 = table.getMembers().get(table.getShangjia() - 1)
				.getCards().size();

		int nmseat = nmcardNum1 > nmcardNum2 ? nmseat2 : nmseat1;
		int nmcardNum = table.getMembers().get(nmseat - 1).getCards().size();

		if (table.getOwner() == table.getLastPlaySeat()) {// 当前出牌者为地主
			if (nmcardNum1 + nmcardNum2 == 3) {// 一个是1张， 一个是2张
				Map<CardType, List<List<Card>>> map = arrangePai(list);
				int num = 0;
				if (map.containsKey(CardType.DAN)) {
					num++;
				}
				if (map.containsKey(CardType.DUI_ZI)) {
					num++;
				}
				if (num < map.size()) {// 还有其他牌型
					for (CardType cardType : CardType.values()) {

						if (cardType != CardType.DAN
								&& cardType != CardType.DUI_ZI
								&& map.containsKey(cardType)) {
							List<List<Card>> slist = map.get(cardType);
							for (Card card : slist.get(0)) {// 从大往小打
								data.add(card.getId());
								return true;
							}
						}
					}

				} else {// 挑大的出
					for (CardType cardType : CardType.values()) {

						if ((cardType == CardType.DAN || cardType == CardType.DUI_ZI)
								&& map.containsKey(cardType)) {
							List<List<Card>> slist = map.get(cardType);
							for (Card card : slist.get(slist.size() - 1)) {// 从大往小打
								data.add(card.getId());
								return true;
							}
						}
					}
				}

			} else if (nmcardNum == 1) {// 1张
				Map<CardType, List<List<Card>>> map = arrangePai(list);

				int num = 0;
				if (map.containsKey(CardType.DAN)) {
					num++;
				}
				if (num < map.size()) {// 还有其他牌型
					for (CardType cardType : CardType.values()) {

						if (cardType != CardType.DAN
								&& map.containsKey(cardType)) {
							List<List<Card>> slist = map.get(cardType);
							for (Card card : slist.get(0)) {// 从大往小打
								data.add(card.getId());
								return true;
							}
						}
					}

				} else {// 挑大的出
					for (CardType cardType : CardType.values()) {

						if ((cardType == CardType.DAN)
								&& map.containsKey(cardType)) {
							List<List<Card>> slist = map.get(cardType);
							for (Card card : slist.get(slist.size() - 1)) {// 从大往小打
								data.add(card.getId());
								return true;
							}
						}
					}
				}

			} else if (nmcardNum == 2) {
				Map<CardType, List<List<Card>>> map = arrangePai(list);

				int num = 0;
				if (map.containsKey(CardType.DUI_ZI)) {
					num++;
				}
				if (num < map.size()) {// 还有其他牌型
					for (CardType cardType : CardType.values()) {

						if (cardType != CardType.DUI_ZI
								&& map.containsKey(cardType)) {
							List<List<Card>> slist = map.get(cardType);
							for (Card card : slist.get(0)) {// 从大往小打
								data.add(card.getId());
								return true;
							}
						}
					}

				} else {// 挑大的出
					for (CardType cardType : CardType.values()) {

						if ((cardType == CardType.DUI_ZI)
								&& map.containsKey(cardType)) {
							List<List<Card>> slist = map.get(cardType);
							for (Card card : slist.get(slist.size() - 1)) {// 从大往小打
								data.add(card.getId());
								return true;
							}
						}
					}
				}
			}
		} else if (table.getLastPlaySeat() == table.getXiajia()) {// 下家

			if (nmcardNum2 == 1) {// 1张
				Map<CardType, List<List<Card>>> map = arrangePai(list);
				if (map.containsKey(CardType.DAN)) {
					List<List<Card>> slist = map.get(CardType.DAN);
					for (Card card : slist.get(0)) {// 从小往大
						data.add(card.getId());
						return true;
					}
				}
			} else if (nmcardNum2 == 2) {
				Map<CardType, List<List<Card>>> map = arrangePai(list);
				if (map.containsKey(CardType.DUI_ZI)) {
					List<List<Card>> slist = map.get(CardType.DUI_ZI);
					for (Card card : slist.get(0)) {// 从小往大
						data.add(card.getId());
						return true;
					}
				}
			} else {// 地主
				int ownerCardNum = table.getMembers().get(table.getOwner() - 1)
						.getCards().size();
				if (ownerCardNum == 1) {// 地主只有1张牌
					Map<CardType, List<List<Card>>> map = arrangePai(list);

					int num = 0;
					if (map.containsKey(CardType.DAN)) {
						num++;
					}
					if (num > 0 && num < map.size()) {
						for (CardType cardType : CardType.values()) {
							if (cardType != CardType.DAN
									&& map.containsKey(cardType)) {
								List<List<Card>> slist = map.get(cardType);
								for (Card card : slist.get(0)) {// 从小往大
									data.add(card.getId());
									return true;
								}
							}
						}
					} else if (map.containsKey(CardType.DAN)) {
						List<List<Card>> slist = map.get(CardType.DAN);
						for (Card card : slist.get(slist.size() - 1)) {// 从大往小
							data.add(card.getId());
							return true;
						}
					}
				} else if (ownerCardNum == 2) {

					Map<CardType, List<List<Card>>> map = arrangePai(list);
					int num = 0;
					if (map.containsKey(CardType.DUI_ZI)) {
						num++;
					}
					if (num > 0 && num < map.size()) {// 优先出之外的牌
						for (CardType cardType : CardType.values()) {
							if (cardType != CardType.DUI_ZI
									&& map.containsKey(cardType)) {
								List<List<Card>> slist = map.get(cardType);
								for (Card card : slist.get(0)) {// 从小往大
									data.add(card.getId());
									return true;
								}
							}
						}
					} else if (map.containsKey(CardType.DUI_ZI)) {
						List<List<Card>> slist = map.get(CardType.DUI_ZI);
						for (Card card : slist.get(slist.size() - 1)) {// 从大往小
							data.add(card.getId());
							return true;
						}
					}
				}
			}
		} else if (table.getLastPlaySeat() == table.getShangjia()) {// 上家出牌
			int ownerCardNum = table.getMembers().get(table.getOwner() - 1)
					.getCards().size();
			if (ownerCardNum == 1) {// 地主只有1张牌
				Map<CardType, List<List<Card>>> map = arrangePai(list);

				int num = 0;
				if (map.containsKey(CardType.DAN)) {
					num++;
				}
				if (num > 0 && num < map.size()) {
					for (CardType cardType : CardType.values()) {
						if (cardType != CardType.DAN
								&& map.containsKey(cardType)) {
							List<List<Card>> slist = map.get(cardType);
							for (Card card : slist.get(0)) {// 从小往大
								data.add(card.getId());
								return true;
							}
						}
					}
				} else if (map.containsKey(CardType.DAN)) {
					List<List<Card>> slist = map.get(CardType.DAN);
					for (Card card : slist.get(slist.size() - 1)) {// 从大往小
						data.add(card.getId());
						return true;
					}
				}
			} else if (ownerCardNum == 2) {
				Map<CardType, List<List<Card>>> map = arrangePai(list);
				int num = 0;
				if (map.containsKey(CardType.DUI_ZI)) {
					num++;
				}
				if (num > 0 && num < map.size()) {// 优先出之外的牌
					for (CardType cardType : CardType.values()) {
						if (cardType != CardType.DUI_ZI
								&& map.containsKey(cardType)) {
							List<List<Card>> slist = map.get(cardType);
							for (Card card : slist.get(0)) {// 从小往大
								data.add(card.getId());
								return true;
							}
						}
					}
				} else if (map.containsKey(CardType.DUI_ZI)) {
					List<List<Card>> slist = map.get(CardType.DUI_ZI);
					for (Card card : slist.get(slist.size() - 1)) {// 从大往小
						data.add(card.getId());
						return true;
					}
				}
			}
		}
		return false;
	}

	public static List<Integer> selfOutPai(List<Integer> allPai,
			int targetSize, int friendSize, List<Integer> recyclePai) {
		List<Integer> data = new ArrayList<Integer>();
		List<Card> list = new ArrayList<Card>();
		for (Integer id : allPai) {
			list.add(new Card(id));
		}
		ZTDoudizhuRule.sortCards(list);
		// 最大的牌
		Card big = list.get(list.size() - 1);

		if (allPai.size() == 1) {
			data.add(list.get(0).getId());
			return data;
		} else if (allPai.size() == 2) {
			if (big.getBigType() == CardBigType.LAI_ZI) {
				data.addAll(allPai);
			} else {
				CardType type = ZTDoudizhuRule.getCardType(list);
				if (type == null) {
					data.add(allPai.get(0));
				} else {
					for (Card card : list) {
						data.add(card.getId());
					}
				}
			}
			return data;
		} else if (allPai.size() >= 3) {

			// 地主单干
			Map<CardType, List<List<Card>>> map = arrangePai(list);

			// 强势炸弹判断区

			data = aiWithZha(list, map);

			if (data.size() > 0) {
				return data;
			}

			data = aiWithGood(list, map);
			if (data.size() > 0) {
				return data;
			}

			// 杂牌逻辑

			// 剩下一张的ai
			if (targetSize == 1) {
				if (map.containsKey(CardType.DUI_ZI)) {
					List<List<Card>> slist = map.get(CardType.DUI_ZI);
					for (Card card : slist.get(0)) {
						data.add(card.getId());
					}
				} else if (map.containsKey(CardType.DAN)) {
					List<List<Card>> slist = map.get(CardType.DAN);
					List<Card> card = slist.get(slist.size() - 1);
					if (card.get(0).grade < 53) {
						data.add(card.get(0).getId());
					} else {
						if (big.getBigType() == CardBigType.LAI_ZI) {
							data.add(big.getId());
							data.add(slist.get(0).get(0).getId());
						} else {
							card = slist.size() > 1 ? slist
									.get(slist.size() - 2) : card;
							data.add(card.get(0).getId());
						}
					}
				} else {
					data.add(allPai.get(0));
				}
				return data;
			}
			LogUtil.info("map.containsKey(CardType.DAN)"
					+ map.containsKey(CardType.DAN));
			// 肯定存在单张或者对子
			if (map.containsKey(CardType.DAN)
					&& map.containsKey(CardType.DUI_ZI)) {
				List<List<Card>> danlist = map.get(CardType.DAN);
				List<List<Card>> duilist = map.get(CardType.DUI_ZI);
				if (danlist.get(0).get(0).grade < duilist.get(0).get(0).grade) {
					for (Card card : danlist.get(0)) {
						data.add(card.getId());
					}
				} else {
					for (Card card : duilist.get(0)) {
						data.add(card.getId());
					}
				}
			} else if (map.containsKey(CardType.DAN)) {
				List<List<Card>> slist = map.get(CardType.DAN);
				for (Card card : slist.get(0)) {
					data.add(card.getId());
				}
			} else if (map.containsKey(CardType.DUI_ZI)) {
				List<List<Card>> slist = map.get(CardType.DUI_ZI);
				for (Card card : slist.get(0)) {
					data.add(card.getId());
				}

			}
			if (data.size() == 0) {
				if (big.getBigType() == CardBigType.LAI_ZI) {
					data.add(big.getId());
					data.add(list.get(0).getId());
				} else {
					data.add(list.get(0).getId());
				}
			}

		}
		return data;
	}

	/**
	 * 检测牌的类型
	 * 
	 * @param myCards
	 *            我出的牌
	 * @return 如果遵守规则，返回牌的类型，否则，返回null。
	 */
	public static CardType getCardType(List<Card> myCards) {
		CardType cardType = null;
		if (myCards != null) {
			// 大概率事件放前边，提高命中率
			if (isDan(myCards)) {
				cardType = CardType.DAN;
			} else if (isDuiWang(myCards)) {
				cardType = CardType.WANG_ZHA;
			} else if (isDuiZi(myCards)) {
				cardType = CardType.DUI_ZI;
			} else if (isZhaDan(myCards)) {
				cardType = CardType.ZHA_DAN;
			} else if (isSanDaiYi(myCards) != -1) {
				cardType = CardType.SAN_DAI_YI;
			} else if (isSanBuDai(myCards)) {
				cardType = CardType.SAN_BU_DAI;
			} else if (isShunZi(myCards)) {
				cardType = CardType.SHUN_ZI;
			} else if (isLianDui(myCards)) {
				cardType = CardType.LIAN_DUI;
			} else if (isSiDaiEr(myCards)) {
				cardType = CardType.SI_DAI_ER;
			} else if (isFeiJi(myCards)) {
				cardType = CardType.FEI_JI;
			}else if(isWangZha(myCards)){
				cardType = CardType.WANG_ZHA;
			}
		}

		return cardType;

	}
	//是否为王炸
	public static boolean isWangZha(List<Card> myCards){
		if(myCards!=null && isWang(myCards.get(0)) && isWang(myCards.get(1))){
			return true;
		}
		return false;
	}
	//当前牌是否为王
	public static boolean isWang(Card card){
		if(card.getBigType() == CardBigType.DA_WANG || card.getBigType() == CardBigType.XIAO_WANG){
			return true;
		}
		return false;
	}

	/**
	 * 判断牌是否为单
	 * 
	 * @param myCards
	 *            牌的集合
	 * @return 如果为单，返回true；否则，返回false。
	 */
	public static boolean isDan(List<Card> myCards) {
		// 默认不是单
		boolean flag = false;
		if (myCards != null && myCards.size() == 1
				&& myCards.get(0).getBigType() != CardBigType.LAI_ZI) {
			flag = true;
		}
		return flag;
	}

	/**
	 * 判断牌是否为对子
	 * 
	 * @param myCards
	 *            牌的集合
	 * @return 如果为对子，返回true；否则，返回false。
	 */
	public static boolean isDuiZi(List<Card> myCards) {
		// 默认不是对子
		boolean flag = false;

		if (myCards != null && myCards.size() == 2) {

			int grade1 = myCards.get(0).grade;
			int grade2 = myCards.get(1).grade;
			if (grade1 == grade2) {
				flag = true;
			}
			if (myCards.get(0).getBigType() == CardBigType.LAI_ZI
					|| myCards.get(1).getBigType() == CardBigType.LAI_ZI) {
				flag = true;
			}
		}

		return flag;

	}
	//获取三带一的Grade值
	public int  getGradeOfSanDaiYi(List<Card> myCards){
		if(isSanDaiYi(myCards)>-1){
			sortCards(myCards);
			return myCards.get(0).getGrade();
		}
		return 0;
	}
	//获取三带一的单牌
	public Card getDanOfSanDaiYi(List<Card> myCards){
		if(isSanDaiYi(myCards)>-1){
			if(myCards.get(0).getGrade() != myCards.get(1).getGrade()){
				return myCards.get(0);
			}else {
				return myCards.get(3);
			}
		}
		return null;
	}

	/**
	 * 判断牌是否为3带1
	 * 
	 * @param myCards
	 *            牌的集合
	 * @return 如果为3带1，被带牌的位置，0或3，否则返回-1。炸弹返回-1。
	 */
	public static int isSanDaiYi(List<Card> myCards) {
		int flag = -1;
		// 默认不是3带1
		if (myCards != null && myCards.size() == 4) {
			// 对牌进行排序
			sortCards(myCards);

			int[] grades = new int[4];
			grades[0] = myCards.get(0).grade;
			grades[1] = myCards.get(1).grade;
			grades[2] = myCards.get(2).grade;
			grades[3] = myCards.get(3).grade;

			if (grades[3] == 18) {
				if (grades[1] == grades[0]) {
					return 2;
				} else if (grades[2] == grades[1]) {
					return 1;
				} else {
					return -1;
				}
			} else {
				// 暂时认为炸弹不为3带1
				if ((grades[1] == grades[0]) && (grades[2] == grades[0])
						&& (grades[3] == grades[0])) {
					return -1;
				}
				// 3带1，被带的牌在牌头
				else if ((grades[1] == grades[0] && grades[2] == grades[0])) {
					if ((myCards.get(0).getId() == 53 || myCards.get(0).getId() == 54)
							&& myCards.size() > 4) {// 当剩余牌数量大于4时，
						return -1;
					}
					return 0;
				}
				// 3带1，被带的牌在牌尾
				else if (grades[1] == grades[3] && grades[2] == grades[3]) {
					if ((myCards.get(3).getId() == 53 || myCards.get(3).getId() == 54)
							&& myCards.size() > 4) {
						return -1;
					}
					return 3;
				}
			}

		}
		return flag;
	}

	/**
	 * 判断牌是否为3不带
	 * 
	 * @param myCards
	 *            牌的集合
	 * @return 如果为3不带，返回true；否则，返回false。
	 */
	public static boolean isSanBuDai(List<Card> myCards) {
		// 默认不是3不带
		boolean flag = false;

		if (myCards != null && myCards.size() == 3) {
			int grade0 = myCards.get(0).grade;
			int grade1 = myCards.get(1).grade;
			int grade2 = myCards.get(2).grade;
			if (grade2 == 18) {
				if (grade0 == grade1) {
					flag = true;
				}
			}
			if (grade0 == grade1 && grade2 == grade0) {
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * 判断牌是否为顺子
	 * 
	 * @param myCards
	 *            牌的集合
	 * @return 如果为顺子，返回true；否则，返回false。
	 */
	public static boolean isShunZi(List<Card> myCards) {
		// 默认是顺子
		boolean flag = true;

		if (myCards != null) {

			int size = myCards.size();
			// 顺子牌的个数在5到12之间
			if (size < 5 || size > 12) {
				return false;
			}

			// 对牌进行排序
			sortCards(myCards);

			for (int n = 0; n < size - 1; n++) {
				int prev = myCards.get(n).grade;
				int next = myCards.get(n + 1).grade;
				// 小王、大王、2不能加入顺子
				if (prev == 17 || prev == 16 || prev == 15 || next == 17
						|| next == 16 || next == 15) {
					flag = false;
					break;
				} else {
					if (prev - next != -1) {
						flag = false;
						break;
					}

				}
			}
		}

		return flag;
	}

	/**
	 * 判断牌是否为炸弹
	 * 
	 * @param myCards
	 *            牌的集合
	 * @return 如果为炸弹，返回true；否则，返回false。
	 */
	public static boolean isZhaDan(List<Card> myCards) {
		// 默认不是炸弹
		boolean flag = false;
		if (myCards != null && myCards.size() == 4) {

			int[] grades = new int[4];
			grades[0] = myCards.get(0).grade;
			grades[1] = myCards.get(1).grade;
			grades[2] = myCards.get(2).grade;
			grades[3] = myCards.get(3).grade;
			if (grades[3] == 18 && grades[0] == grades[1]
					&& grades[1] == grades[2]) {
				flag = true;
			}
			if ((grades[1] == grades[0]) && (grades[2] == grades[0])
					&& (grades[3] == grades[0])) {
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * 判断牌是否为王炸
	 * 
	 * @param myCards
	 *            牌的集合
	 * @return 如果为王炸，返回true；否则，返回false。
	 */
	public static boolean isDuiWang(List<Card> myCards) {
		// 默认不是对王
		boolean flag = false;

		if (myCards != null && myCards.size() == 2) {

			int gradeOne = myCards.get(0).grade;
			int gradeTwo = myCards.get(1).grade;
			if (gradeTwo == 18 && (gradeOne == 16 || gradeOne == 17)) {
				flag = true;
			}
			// 只有小王和大王的等级之后才可能是33
			if (gradeOne > 15 && gradeOne + gradeTwo == 33) {
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * 判断牌是否为连对
	 * 
	 * @param myCards
	 *            牌的集合
	 * @return 如果为连对，返回true；否则，返回false。
	 */
	public static boolean isLianDui(List<Card> myCards) {
		// 默认是连对
		boolean flag = true;
		if (myCards == null) {
			flag = false;
			return flag;
		}

		int size = myCards.size();
		if (size < 6 || size % 2 != 0) {
			flag = false;
		} else {
			// 对牌进行排序
			sortCards(myCards);
			for (int i = 0; i < size; i = i + 2) {
				if (myCards.get(i).grade != myCards.get(i + 1).grade) {
					flag = false;
					break;
				}

				if (i < size - 2) {
					if (myCards.get(i).grade - myCards.get(i + 2).grade != -1) {
						flag = false;
						break;
					}
				}

			}
		}
		if (myCards.get(myCards.size() - 1).grade == 15) {
			flag = false;
		}
		return flag;
	}

	/**
	 * 判断牌是否为飞机
	 * 
	 * @param myCards
	 *            牌的集合
	 * @return 如果为飞机，返回true；否则，返回false。
	 */
	public static boolean isFeiJi(List<Card> myCards) {
		boolean flag = false;
		// 默认不是单
		if (myCards != null) {

			int size = myCards.size();
			if (size >= 6) {
				// 对牌进行排序
				sortCards(myCards);

				if (size % 3 == 0 && size % 4 != 0) {
					flag = isFeiJiBuDai(myCards);
				} else if (size % 3 != 0 && size % 4 == 0) {
					flag = isFeiJiDai(myCards);
				} else if (size == 12) {
					flag = isFeiJiBuDai(myCards) || isFeiJiDai(myCards);
				}
			}
		}
		return flag;
	}

	/**
	 * 判断牌是否为飞机不带
	 * 
	 * @param myCards
	 *            牌的集合
	 * @return 如果为飞机不带，返回true；否则，返回false。
	 */
	public static boolean isFeiJiBuDai(List<Card> myCards) {
		if (myCards == null) {
			return false;
		}

		if (myCards.size() < 6) {
			return false;
		}

		int size = myCards.size();
		int n = size / 3;

		int[] grades = new int[n];

		if (size % 3 != 0) {
			return false;
		} else {
			for (int i = 0; i < n; i++) {
				if (!isSanBuDai(myCards.subList(i * 3, i * 3 + 3))) {
					return false;
				} else {
					// 如果连续的3张牌是一样的，记录其中一张牌的grade
					grades[i] = myCards.get(i * 3).grade;
				}
			}
		}

		for (int i = 0; i < n - 1; i++) {
			if (grades[i] == 15 || grades[i + 1] == 15) {// 不允许出现2
				return false;
			}

			if (grades[i + 1] - grades[i] != 1) {
				System.out.println("等级连续,如 333444"
						+ (grades[i + 1] - grades[i]));
				return false;// grade必须连续,如 333444
			}
		}

		return true;
	}

	/**
	 * 判断牌是否为飞机带
	 * 
	 * @param myCards
	 *            牌的集合
	 * @return 如果为飞机带，返回true；否则，返回false。
	 */
	public static boolean isFeiJiDai(List<Card> myCards) {
		int size = myCards.size();
		int n = size / 4;// 此处为“除”，而非取模
		int i = 0;
		ArrayList<Card> cards = new ArrayList<Card>();
		for (i = 0; i + 2 < size; i++) {
			int grade1 = myCards.get(i).grade;
			int grade2 = myCards.get(i + 1).grade;
			int grade3 = myCards.get(i + 2).grade;
			if (grade1 == grade2 && grade3 == grade1) {
				cards.add(myCards.get(i));
				cards.add(myCards.get(i + 1));
				cards.add(myCards.get(i + 2));
				i = i + 2;
				// return isFeiJiBuDai(myCards.subList(i, i + 3 *
				// n));8张牌时，下标越界,subList不能取到最后一个元素

				// ArrayList<Card> cards = new ArrayList<Card>();
				// for (int j = i; j < i + 3 * n && j < myCards.size(); j++) {//
				// 取字串
				// cards.add(myCards.get(j));
				// }

			}

		}
		for (i = 0; i + 3 < size; i++) {
			if (myCards.get(i).grade == myCards.get(i + 3).grade) {
				return false;
			}
		}
		if (cards.size() > 0) {
			if (isFeiJiBuDai(cards)) {
				if (myCards.size() - cards.size() == cards.size() / 3) {
					// 飞机的长度只能跟单牌的长度一致才是飞机
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 判断牌是否为4带2
	 * 
	 * @param myCards
	 *            牌的集合
	 * @return 如果为4带2，返回true；否则，返回false。
	 */
	public static boolean isSiDaiEr(List<Card> myCards) {
		boolean flag = false;
		if (myCards != null && myCards.size() == 6) {

			// 对牌进行排序
			sortCards(myCards);
			for (int i = 0; i < 3; i++) {
				int grade1 = myCards.get(i).grade;
				int grade2 = myCards.get(i + 1).grade;
				int grade3 = myCards.get(i + 2).grade;
				int grade4 = myCards.get(i + 3).grade;

				if (grade2 == grade1 && grade3 == grade1 && grade4 == grade1) {
					flag = true;
				}
			}
		}
		return flag;
	}

	// 抓综合
	public static boolean zhua(List<Card> cards) {

		if (include2WangAnd4ErAndZhaOfGt3(cards) == true) {
			return true;
		}
		if(wangZha(cards) == true) {
			return true;
		}
		if(twoZha(cards) == true) {
			return true;
		}

		return false;
	}

	// 倒综合
	public static boolean dao(List<Card> cards) {
		if (twoErAndOneZha(cards) == true) {
			return true;
		}
		if (fourEr(cards) == true) {
			return true;
		}
		if (twoZha(cards) == true) {
			return true;
		}
		if (wangZha(cards) == true) {
			return true;
		}
		return false;
	}

	/***
	 * 是否手上存在大王、小王、2x4中大于3张
	 * 
	 * @param cards
	 * @return
	 */
	public static boolean include2WangAnd4ErOfGt3(List<Card> cards) {
		int num = 0;
		for (Card card : cards) {
			if ((card.getBigType() == CardBigType.DA_WANG)
					|| (card.getBigType() == CardBigType.XIAO_WANG)) {
				num++;
			}
			if (card.getSmallType() == CardSmallType.ER) {
				num++;
			}
			if (num > 3) {
				return true;
			}
		}
		return false; 
	}
	
	
	/***
	 * 上手有3个主牌以上必抓
	 * 主牌：大王、小王、2x4
	 * 炸弹也算一个主牌（4条2除外）。
	 * 
	 * @param cards
	 * @return
	 */
	public static boolean include2WangAnd4ErAndZhaOfGt3(List<Card> cards) {
		int num = 0;
		for (Card card : cards) {
			if ((card.getBigType() == CardBigType.DA_WANG)
					|| (card.getBigType() == CardBigType.XIAO_WANG)) {
				num++;
			}
			if (card.getSmallType() == CardSmallType.ER) {
				num++;
			}
			if (num > 2) {
				return true;
			}
		}
		
		
		Map<CardSmallType, Integer> map = new HashMap<CardSmallType, Integer>();
		for (Card card : cards) {
			if (map.get(card.getSmallType()) == null) {
				map.put(card.getSmallType(), 1);
			} else {
				map.put(card.getSmallType(), map.get(card.getSmallType()) + 1);
			}
		}
		for (Map.Entry<CardSmallType, Integer> entry : map.entrySet()) {
			if (entry.getValue() == 4) {
				num++;
				if (num > 2) {
					return true;
				}
			}
		}
		
		
		return false;
	}

	// 1王带2二(必抓)
	private static boolean oneWangAndTwoEr(List<Card> cards) {
		int num = 0;
		boolean Wang = false;
		boolean Two = false;
		for (Card card : cards) {
			if ((card.getBigType() == CardBigType.DA_WANG)
					|| (card.getBigType() == CardBigType.XIAO_WANG)) {
				Wang = true;
			}
			if (card.getSmallType() == CardSmallType.ER) {
				num++;
				if (num >= 2) {
					Two = true;
				}
			}
		}
		if (Wang == true && Two == true) {
			return true;
		} else {
			return false;
		}
	}

	// 1王1二1炸(必抓)
	private static boolean oneWangAndOneErAndOneZha(List<Card> cards) {
		boolean Wang = false;
		boolean Two = false;
		boolean Zha = false;
		Map<CardSmallType, Integer> map = new HashMap<CardSmallType, Integer>();
		for (Card card : cards) {
			if ((card.getBigType() == CardBigType.DA_WANG)
					|| (card.getBigType() == CardBigType.XIAO_WANG)) {
				Wang = true;
			} else if (card.getSmallType() == CardSmallType.ER) {
				Two = true;
			} else {
				if (map.get(card.getSmallType()) == null) {
					map.put(card.getSmallType(), 1);
				} else {
					map.put(card.getSmallType(),
							map.get(card.getSmallType()) + 1);
				}
			}
		}
		for (Map.Entry<CardSmallType, Integer> entry : map.entrySet()) {
			if (entry.getValue() == 4) {
				Zha = true;
				break;
			}
		}
		if (Wang == true && Two == true && Zha == true) {
			return true;
		} else {
			return false;
		}
	}

	// 三个二(必抓)
	private static boolean threeEr(List<Card> cards) {
		int num = 0;
		for (Card card : cards) {
			if (card.getSmallType() == CardSmallType.ER) {
				num++;
				if (num >= 3) {
					break;
				}
			}
		}
		if (num >= 3) {
			return true;
		} else {
			return false;
		}
	}

	// 2个2加1炸(必抓)
	private static boolean twoErAndOneZha(List<Card> cards) {
		boolean er = false;
		boolean zha = false;
		Map<CardSmallType, Integer> map = new HashMap<CardSmallType, Integer>();
		for (Card card : cards) {
			if (map.get(card.getSmallType()) == null) {
				map.put(card.getSmallType(), 1);
			} else {
				map.put(card.getSmallType(), map.get(card.getSmallType()) + 1);
			}
		}
		for (Map.Entry<CardSmallType, Integer> entry : map.entrySet()) {
			if (entry.getKey() == CardSmallType.ER) {
				if (entry.getValue() >= 2) {
					er = true;
				}
			} else {
				if (entry.getValue() >= 4) {
					zha = true;
				}
			}
		}
		if (er == true && zha == true) {
			return true;
		} else {
			return false;
		}

	}

	// 4个2(必抓)(必倒)
	private static boolean fourEr(List<Card> cards) {
		int num = 0;
		for (Card card : cards) {
			if (card.getSmallType() == CardSmallType.ER) {
				num++;
				if (num >= 4) {
					break;
				}
			}
		}
		if (num >= 4) {
			return true;
		} else {
			return false;
		}
	}

	// 2个炸（必抓）（必倒）
	private static boolean twoZha(List<Card> cards) {
		int num = 0;
		Map<CardSmallType, Integer> map = new HashMap<CardSmallType, Integer>();
		for (Card card : cards) {
			if (map.get(card.getSmallType()) == null) {
				map.put(card.getSmallType(), 1);
			} else {
				map.put(card.getSmallType(), map.get(card.getSmallType()) + 1);
			}
		}
		for (Map.Entry<CardSmallType, Integer> entry : map.entrySet()) {
			if (entry.getValue() == 4) {
				num++;
				if (num >= 2) {
					break;
				}
			}
		}
		if (num >= 2) {
			return true;
		} else {
			return false;
		}

	}


	// 王炸(必抓)(必倒)
	private static boolean wangZha(List<Card> cards) {
		int num = 0;
		for (Card card : cards) {
			if ((card.getBigType() == CardBigType.DA_WANG)
					|| (card.getBigType() == CardBigType.XIAO_WANG)) {
				num++;
				if (num >= 2) {
					break;
				}
			}
		}
		if (num >= 2) {
			return true;
		} else {
			return false;
		}
	}

	private static int getRoleType(ZTDoudizhuRole doudizhuMember,
			NPCFunction npcFunction, int roomType) {
		if (doudizhuMember.getRole().isRobot()) {
			long rid = doudizhuMember.getRole().getRole().getRid();
			int cardId = npcFunction.getDrawCardId(GameType.DOUDIZHU, roomType,
					rid);
			LogUtil.info("斗地主机器人npcid = " + rid + " 的发牌Id=" + cardId);
			return cardId;
		} else {
			return 1;
		}
	}

	// 分牌
	public static void fenPai(ZTDoudizhuTable table,
			DoudizhuDrawCardCache doudizhuDrawCardCache, NPCFunction npcFunction) {
		List<Integer> pais = table.getPais();
		// 分牌池
		List<Integer> teshuPais = new ArrayList<>();
		// 剔除4个2,大小王，听牌
		for (Integer pai : pais) {
			if (pai.intValue() < 52 && pai.intValue() % 13 == 2)
				teshuPais.add(pai);
			if (pai.intValue() == 55 || pai.intValue() == 54
					|| pai.intValue() == 53)
				teshuPais.add(pai);
		}
		pais.removeAll(teshuPais);
		Collections.shuffle(teshuPais);
		// 处理普通牌
		List<Card> list = new ArrayList<Card>();
		for (Integer id : pais) {
			Card card = new Card(id);
			list.add(card);
			// System.out.println(card.getSmallType());
		}
		int teshuWeight = 0;
		int teshuWeights[] = new int[table.getMembers().size()];
		for (int ii = 0; ii < table.getMembers().size(); ii++) {
			int roleType = getRoleType(table.getMembers().get(ii), npcFunction,
					table.getGame().getRoomType());
			//机器人作弊
			if(table.getMembers().get(ii).getRole().isRobot()) {
				int judge = doudizhuDrawCardCache.getConfig(roleType).getJudge();
				if(judge == 2) {
					LogUtil.info("ai ：" + table.getMembers().get(ii).getRole().getRole().getNick() + " 作弊出牌");
					table.getMembers().get(ii).getRole().setCheat(true);
				}
				LogUtil.info("ai ：" + table.getMembers().get(ii).getRole().getRole().getNick() + " 不作弊出牌");
			}
			teshuWeight += doudizhuDrawCardCache.getConfig(roleType)
					.getDrawCardRate();
			teshuWeights[ii] = doudizhuDrawCardCache.getConfig(roleType)
					.getDrawCardRate();
		}
		// 处理特殊牌,现假设三个玩家都是普通类型玩家
		// int teshuPaiSize = teshuPais.size();
		for (Integer pai : teshuPais) {
			int rdnum = MathUtil.randomNumber(1, teshuWeight);
			int roleIndex = getWeightIndex(teshuWeights, rdnum);
			table.getMembers().get(roleIndex).getPai().add(pai);
		}
		teshuPais.clear();
		for (int j = 0; j < table.getMembers().size(); j++) {
			ZTDoudizhuRole doudizhuMember = table.getMembers().get(j);
			int roleType = getRoleType(doudizhuMember, npcFunction, table
					.getGame().getRoomType());
			DoudizhuDrawCardCsv doudizhuCardCsv = doudizhuDrawCardCache
					.getConfig(roleType);
			// 用来存放动态权值表
			List<Weight> wList = new ArrayList<>();
			initDrawCardWeight(doudizhuCardCsv, wList);
			List<Integer> mePais = doudizhuMember.getPai();
			for (int i = 0; i < doudizhuCardCsv.getDrawCardTimes(); i++) {
				// 根据玩家类型选择对应的权值表，默认正常玩家
				CardType cardType = getCardsTypeByWeight(doudizhuCardCsv, wList);
				int leftPaiNum = doudizhuCardCsv.getCardLimit()
						- doudizhuMember.getPai().size();
				if (leftPaiNum < 2)
					break;
				Map<CardType, List<List<Card>>> map = reArrangePai(list,
						cardType);
				// 当达到drawCardLimit上限停止配牌
				List<List<Card>> listCards = map.get(cardType);
				// 当发现通过权值取得的牌形，已经无法从牌池中取得时，从权值表中删去该权值选项，并回退再选
				if (listCards == null || map.isEmpty()
						|| leftPaiNum < getMinNumByCardType(cardType)) {
					int wsize = wList.size();
					for (int kkk = 0; kkk < wsize; kkk++) {
						if (wList.get(kkk).type == cardType) {
							wList.remove(wList.get(kkk));
							i--;
							break;
						}
					}
					continue;
				}
				int random = MathUtil.randomNumber(0, listCards.size() - 1);
				List<Card> cards = listCards.get(random);
				for (Card card : cards) {
					if (pais.remove(Integer.valueOf(card.getId()))) {
						doudizhuMember.getPai().add(
								Integer.valueOf(card.getId()));
					}
				}
				listCards.removeAll(cards);
				list.removeAll(cards);
				cards.clear();
				listCards.clear();
			}

		}
		// 补单牌
		for (int jj = 0; jj < table.getMembers().size(); jj++) {
			ZTDoudizhuRole doudizhuMember = table.getMembers().get(jj);
			int msize = doudizhuMember.getPai().size();
			for (int jk = 0; jk < 17 - msize; jk++) {
				if (!pais.isEmpty())
					doudizhuMember.getPai().add(pais.remove(0));
			}
		}
	}

	private static int getMinNumByCardType(CardType cardType) {
		switch (cardType) {
		case ZHA_DAN:
			return 4;
		case SAN_BU_DAI:
			return 3;
		case LIAN_DUI:
			return 6;
		case SHUN_ZI:
			return 5;
		case DUI_ZI:
			return 2;
		case DAN:
			return 1;
		default:
			break;
		}
		return 1;
	}

	private static class Weight {
		private CardType type;
		private int value;

		public Weight(CardType type, int value) {
			this.type = type;
			this.value = value;
		}

	}

	static void initDrawCardWeight(DoudizhuDrawCardCsv doudizhuCardCsv,
			List<Weight> wList) {
		Weight weight[] = new Weight[6];
		weight[0] = new Weight(CardType.ZHA_DAN,
				doudizhuCardCsv.getZhadanRate());
		weight[1] = new Weight(CardType.SAN_BU_DAI,
				doudizhuCardCsv.getSantiaoRate());
		weight[2] = new Weight(CardType.LIAN_DUI,
				doudizhuCardCsv.getLianduiRate());
		weight[3] = new Weight(CardType.SHUN_ZI,
				doudizhuCardCsv.getShunziRate());
		weight[4] = new Weight(CardType.DUI_ZI, doudizhuCardCsv.getDuiziRate());
		weight[5] = new Weight(CardType.DAN, doudizhuCardCsv.getDanpaiRate());
		for (Weight w : weight)
			wList.add(w);
	}

	// 创建一个权值数组，里面装有所有权值，产生一个随机0到totalWeight的随机数，
	private static CardType getCardsTypeByWeight(
			DoudizhuDrawCardCsv doudizhuCardCsv, List<Weight> wList) {
		// int totalWeight =
		// doudizhuCardCsv.getZhadanRate()+doudizhuCardCsv.getDanpaiRate()+doudizhuCardCsv.getDuiziRate()+
		// doudizhuCardCsv.getLianduiRate()+doudizhuCardCsv.getSantiaoRate()+doudizhuCardCsv.getShunziRate();
		int totalWeight = 0;
		for (Weight w : wList)
			totalWeight += w.value;
		// 随机产生一个数
		int randomInt = MathUtil.randomNumber(1, totalWeight);
		Weight[] weights = new Weight[wList.size()];
		wList.toArray(weights);
		if (weights.length <= 1)
			return CardType.DAN;
		CardType cardType = getWeightIndex(weights, randomInt);
		// switch(index){
		// case 0:
		// return CardType.ZHA_DAN;
		// case 1:
		// return CardType.SAN_BU_DAI;
		// case 2:
		// return CardType.LIAN_DUI;
		// case 3:
		// return CardType.SHUN_ZI;
		// case 4:
		// return CardType.DUI_ZI;
		// case 5:
		// return CardType.DAN;
		// }
		return cardType;
	}

	private static int getWeightIndex(int[] weight, int randomInt) {
		int sumWeight[] = new int[weight.length];
		int sumW = 0;
		for (int j = 0; j < weight.length; j++) {
			sumW += weight[j];
			sumWeight[j] = sumW;
		}
		int i = 0;
		for (i = 0; i < sumWeight.length; i++)
			if (randomInt < sumWeight[i])
				break;
		// 以防万一，实际上应该不可能
		if (i > sumWeight.length - 1)
			i = MathUtil.randomNumber(0, sumWeight.length - 1);
		return i;
	}

	// TODO 日后优化
	private static CardType getWeightIndex(Weight[] weight, int randomInt) {
		int sumWeight[] = new int[weight.length];
		int sumW = 0;
		for (int j = 0; j < weight.length; j++) {
			sumW += weight[j].value;
			sumWeight[j] = sumW;
		}
		int i = 0;
		for (i = 0; i < sumWeight.length; i++)
			if (randomInt < sumWeight[i])
				break;
		// 以防万一，实际上应该不可能
		if (i > sumWeight.length - 1)
			i = MathUtil.randomNumber(0, sumWeight.length - 1);
		return weight[i].type;
	}
}
