package com.yaowan.server.game.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import com.yaowan.framework.util.LogUtil;
import com.yaowan.server.game.model.struct.Card;
import com.yaowan.server.game.model.struct.CardType;
import com.yaowan.server.game.model.struct.ZTDoudizhuRole;
import com.yaowan.server.game.model.struct.ZTDoudizhuTable;

public class ZTDoudizhuAiRule {

	// 不作弊出牌时的判断条件
	private final static int LIANDUINUM = 6;
	private final static int SANDAIYINUM = 10;
	private final static int SHUNZINUM = 10;
	private final static int DUIZINUM = 12;
	private final static int DANNUM = 13;
	
//	//含有大牌的概率
//	private final static List<Double> bigCardChance = new ArrayList<Double>(){{
//			add(0.3070);   //手上有两张大牌或以下 
//			add(0.2925);   //手上有三张大牌
//			add(0.2312);   //手上有四张大牌
//			add(0.1537);   //手上有五张大牌或以上
//	}};
	
	
	
	//手牌含有大牌的数量
	public static int getBigCardNum(List<Integer> allPai) {
		List<Card> allPaiList = ergodicList(allPai);
		Card bigCard = new Card(1);
		int bigCardNum = 0;
		for(Card card : allPaiList) {
			if(card.getGrade() >= bigCard.getGrade()) {
				bigCardNum++;
			}
		}
		return bigCardNum;
	}
	
	public static boolean isContainAce(List<Card> allPai) {
		Card bigCard = new Card(1);
		for(Card card : allPai) {
			if(card.getGrade() >= bigCard.getGrade()) {
				return true;
			}
		}
		return false;
	}
	
	
	//作弊情况下计算权重
	public static int getWeight(List<Integer> allPai, List<Integer> diPai, ZTDoudizhuTable table, ZTDoudizhuRole role) {
		int weight = 0;
		List<Card> copyAllPaiList = new ArrayList<Card>();
		List<Card> allPaiList = ergodicList(allPai);
		List<Card> diPaiList = ergodicList(diPai);
		copyAllPaiList.addAll(allPaiList);
		//手牌中的大牌数
		int bigCardNum = getBigCardNum(allPai);
		weight += bigCardNum;
		//底牌中的大牌数
		if(role.getRole().getSeat() == table.getOwner()) {
			int diPaiBigCardNum = getBigCardNum(diPai);
			weight += diPaiBigCardNum;
		}
//		//未加入底牌的散牌数
//		Map<CardType, List<List<Card>>> map = ZTDoudizhuRule.arrangePai(copyAllPaiList);
//		int danNum = map.get(CardType.DAN).size();
//		//加入底牌后的散牌数
//		copyAllPaiList.addAll(diPaiList);
//		Map<CardType, List<List<Card>>> afterAddmap = ZTDoudizhuRule.arrangePai(copyAllPaiList);
//		int danNumAfterAdd = afterAddmap.get(CardType.DAN).size();
//		//散牌数的变化
//		int change = danNumAfterAdd - danNum;
//		weight += change;
		return weight;
	}

	// 作弊时操作
	public static List<Integer> getCheatPai(ZTDoudizhuTable table,
			ZTDoudizhuRole role, List<Integer> allPai, List<Integer> lastPai, int relate) {
		
		List<Integer> data = new ArrayList<Integer>();
		List<Integer> shangJiaAllCard = new ArrayList<Integer>();
		List<Integer> xiaJiaAllCard = new ArrayList<Integer>();
		int size = table.getLastPai().size();
		
		// 朋友的手牌数量集合
		List<Integer> friendPaiSizeList = new ArrayList<Integer>();
		// 敌人的手牌数量集合
		List<Integer> enemyPaiSizeList = new ArrayList<Integer>();
		getFriendAndEnemyAllPaiSizeList(friendPaiSizeList, enemyPaiSizeList, table,
						role);
//		//朋友的数量
//		int friendNum = friendPaiSizeList.size(); 
		getShangXiaJiaAllPai(shangJiaAllCard, xiaJiaAllCard, table, role);
		
		//下家是否是敌人
		boolean isXiaJiaIsEnemy = false;
		isXiaJiaIsEnemy = isXiaJiaIsEnemy(table, role);
		//下家是否最新出牌
		boolean isXiaJiaLastOut = false;
		isXiaJiaLastOut = isXiaJiaLastOut(table, role);
		
		boolean isOwner = false;
		if(table.getOwner() == role.getRole().getSeat()) {
			isOwner = true;
		}
		
		if (size == 0 || table.getLastOutPai() == table.getLastPlaySeat()) {
			data = getCheatSelfOutPai(allPai, shangJiaAllCard, xiaJiaAllCard, isXiaJiaIsEnemy, enemyPaiSizeList, isOwner);
		} else {
			//出牌玩家的手牌
			List<Integer> lastOutPaiAllPai = table.getMembers().get(table.getLastOutPai() - 1).getPai();
			data = getCheatOutPai(allPai, shangJiaAllCard, xiaJiaAllCard, lastOutPaiAllPai,
					lastPai, relate, isXiaJiaIsEnemy, isXiaJiaLastOut, isOwner);
		}
		return data;
	}

	// 不作弊时操作
	public static List<Integer> getNoCheatPai(ZTDoudizhuTable table,
			ZTDoudizhuRole role, List<Integer> allPai, List<Integer> lastPai, int relate) {
		List<Integer> data = new ArrayList<Integer>();
		List<Integer> shangJiaAllCard = new ArrayList<Integer>();
		List<Integer> xiaJiaAllCard = new ArrayList<Integer>();
		// 最近出牌玩家的手牌数量
		int lastOutPaiSize = 0;
		// 朋友的手牌数量集合
		List<Integer> friendPaiSizeList = new ArrayList<Integer>();
		// 敌人的手牌数量集合
		List<Integer> enemyPaiSizeList = new ArrayList<Integer>();
		
		boolean isOwner = false;
		if(table.getOwner() == role.getRole().getSeat()) {
			isOwner = true;
		}

		getFriendAndEnemyAllPaiSizeList(friendPaiSizeList, enemyPaiSizeList, table,
				role);
		getShangXiaJiaAllPai(shangJiaAllCard, xiaJiaAllCard, table, role);

		if (table.getLastOutPai() != 0) {
			lastOutPaiSize = table.getMembers().get(table.getLastOutPai() - 1)
					.getPai().size();
		}
		int size = table.getLastPai().size();
		if (size == 0 || table.getLastOutPai() == table.getLastPlaySeat()) {
			data = getNoCheatSelfOutPai(allPai, enemyPaiSizeList, xiaJiaAllCard, table.getRecyclePai());
		} else {
			data = getNoCheatOutPai(lastPai, allPai, lastOutPaiSize,
					 table.getPais(), relate, xiaJiaAllCard.size(), isOwner);
		}
		return data;
	}
	
	private static boolean isXiaJiaIsEnemy(ZTDoudizhuTable table, ZTDoudizhuRole role) {
		if(role.getRole().getSeat() == table.getOwner()) {
			return true;
		}
		if(role.getRole().getSeat() == table.getShangjia()) {
			return true;
		}
		return false;
	}
	
	private static boolean isXiaJiaLastOut(ZTDoudizhuTable table, ZTDoudizhuRole role) {
		if(role.getRole().getSeat() == table.getOwner()) {
			if(table.getXiajia() == table.getLastOutPai()) {
				return true;
			}
		}
		if(role.getRole().getSeat() == table.getShangjia()) {
			if(table.getLastOutPai() == table.getOwner()) {
				return true;
			}
		}
		if(role.getRole().getSeat() == table.getXiajia()) {
			if(table.getLastOutPai() == table.getShangjia()) {
				return true;
			}
		}
		return false;
	}
	

	private static void getFriendAndEnemyAllPaiSizeList(
			List<Integer> friendPaiSizeList, List<Integer> enemyPaiSizeList,
			ZTDoudizhuTable table, ZTDoudizhuRole role) {
		if (role.getRole().getSeat() == table.getOwner()) {
			enemyPaiSizeList.add(table.getMembers()
					.get(table.getShangjia() - 1).getPai().size());
			enemyPaiSizeList.add(table.getMembers().get(table.getXiajia() - 1)
					.getPai().size());
		}

		if (role.getRole().getSeat() == table.getXiajia()) {
			friendPaiSizeList.add(table.getMembers()
					.get(table.getShangjia() - 1).getPai().size());
			enemyPaiSizeList.add(table.getMembers().get(table.getOwner() - 1)
					.getPai().size());
		}

		if (role.getRole().getSeat() == table.getShangjia()) {
			friendPaiSizeList.add(table.getMembers().get(table.getXiajia() - 1)
					.getPai().size());
			enemyPaiSizeList.add(table.getMembers().get(table.getOwner() - 1)
					.getPai().size());
		}
	}
	

	private static void getShangXiaJiaAllPai(List<Integer> shangJiaAllCard,
			List<Integer> xiaJiaAllCard, ZTDoudizhuTable table,
			ZTDoudizhuRole role) {
		if (role.getRole().getSeat() == table.getOwner()) {
			shangJiaAllCard.addAll(table.getMembers()
					.get(table.getShangjia() - 1).getPai());
			xiaJiaAllCard.addAll(table.getMembers().get(table.getXiajia() - 1)
					.getPai());
		}

		if (role.getRole().getSeat() == table.getXiajia()) {
			shangJiaAllCard.addAll(table.getMembers().get(table.getOwner() - 1)
					.getPai());
			xiaJiaAllCard.addAll(table.getMembers()
					.get(table.getShangjia() - 1).getPai());
		}

		if (role.getRole().getSeat() == table.getShangjia()) {
			shangJiaAllCard.addAll(table.getMembers()
					.get(table.getXiajia() - 1).getPai());
			xiaJiaAllCard.addAll(table.getMembers().get(table.getOwner() - 1)
					.getPai());
		}
	}

	/***
	 * 不作弊的出牌情况
	 * 
	 * @param cards
	 * @return
	 */
	private static List<Integer> getNoCheatSelfOutPai(List<Integer> allPai,
			List<Integer> enemyCardSizeList, List<Integer> xiaJiaAllCard, List<Integer> recyclePai) {

		List<Integer> data = null;
		List<Card> allPaiList = ergodicList(allPai);
		List<Card> xiaJiaPai = ergodicList(xiaJiaAllCard);
		List<Card> recyclePaiList = ergodicList(recyclePai);
		ZTDoudizhuRule.sortCards(allPaiList);
		ZTDoudizhuRule.sortCards(xiaJiaPai);
		//特殊处理
		data = dealNoCheatSpecialSelfOut(allPaiList, recyclePaiList);
		if(data == null) {
			//合适的出牌
			data = getNoCheatSuitableSelfOutPai(allPaiList, enemyCardSizeList);
		}
		if(data == null) {
			//最后找牌
			data = getNoCheatLastFindSelfOutPai(allPaiList, enemyCardSizeList);
		}
		//最后还是没有找到,特殊处理
		if(data == null) {
			data = dealLastSelfSpecialOut(allPaiList, enemyCardSizeList);
			if(data != null) {
				return data;
			}
		}

		return data;
	}

	/***
	 * 作弊的出牌情况
	 * 
	 * @param cards
	 * @return
	 */
	private static List<Integer> getCheatSelfOutPai(List<Integer> myCard,
			List<Integer> shangJiaAllCard, List<Integer> xiaJiaAllCard, boolean isXiaJiaEnemy,
			List<Integer> enemyCardSizeList, boolean isOwner) {
		List<Integer> data = new ArrayList<Integer>();
		List<Card> myCardList = ergodicList(myCard);
		List<Card> shangJiaCardList = ergodicList(shangJiaAllCard);
		List<Card> xiaJiaCardList = ergodicList(xiaJiaAllCard);
		ZTDoudizhuRule.sortCards(myCardList);
		ZTDoudizhuRule.sortCards(shangJiaCardList);
		ZTDoudizhuRule.sortCards(xiaJiaCardList);
		//判断是不是最后一手牌
		data = dealSelfLastShouPai(myCardList, shangJiaCardList, xiaJiaCardList, isOwner , isXiaJiaEnemy);
		if (data == null) {
			// 模拟出牌
			data = getCheatAnalogySelfOutPai(myCardList, shangJiaCardList,
					xiaJiaCardList, isOwner, isXiaJiaEnemy);
		}
		if (data == null) {
			//特殊情况处理 
			data = dealSelfSpecialOut(myCardList, xiaJiaCardList, isXiaJiaEnemy);
		}
		if(data == null) {
			//搜索合适的出牌
			data = getCheatSuitableSelfOutPai(myCardList, shangJiaCardList,
					xiaJiaCardList, isXiaJiaEnemy, false, isOwner);
		}
		if (data == null) {
			//最后一次找牌。。找一手能出的牌
			data = getCheatSuitableSelfOutPai(myCardList,
					shangJiaCardList, xiaJiaCardList, isXiaJiaEnemy, true, isOwner);
		}
		if (data == null) {
			// 根据上下家判断
			data = dealLastSelfSpecialOut(myCardList, enemyCardSizeList);
		}
		return data;
	}

	/***
	 * 不作弊的跟牌情况
	 * 
	 * @param cards
	 * @return
	 */
	private static List<Integer> getNoCheatOutPai(List<Integer> lastPai,
			List<Integer> allPai, int lastOutPaiSize, List<Integer> pai,
			int relate, int xiaJiaAllSize, boolean isOwner) {
		List<Card> lastList = ergodicList(lastPai);
		List<Card> allList = ergodicList(allPai);
		List<Card> paiList = ergodicList(pai);
		List<Integer> data = new ArrayList<Integer>();
		ZTDoudizhuRule.sortCards(lastList);
		ZTDoudizhuRule.sortCards(paiList);
		ZTDoudizhuRule.sortCards(allList);
		
		data = dealNoCheatSpecialOut(allList, lastList, paiList);
		if(data == null) {
			data = getNoCheatSuitableOutPai(lastList, allList, lastOutPaiSize, relate, xiaJiaAllSize, isOwner);
		}
		return data;
	}

	/***
	 * 作弊的跟牌情况
	 * 
	 * @param cards
	 * @return
	 */
	private static List<Integer> getCheatOutPai(List<Integer> myAllCard,
			List<Integer> shangJiaAllCard, List<Integer> xiaJiaAllCard,
			List<Integer> lastOutPaiAllCard, List<Integer> lastCard, int relate, boolean isXiaJiaIsEnemy,
			boolean isXiaJiaLastOut, boolean isOwner) {
		List<Integer> data = null;

		List<Card> myCardList = ergodicList(myAllCard);
		List<Card> shangJiaCardList = ergodicList(shangJiaAllCard);
		List<Card> xiaJiaCardList = ergodicList(xiaJiaAllCard);
		List<Card> lastOutPaiCardList = ergodicList(lastOutPaiAllCard);
		List<Card> lastCardList = ergodicList(lastCard);
		ZTDoudizhuRule.sortCards(myCardList);
		ZTDoudizhuRule.sortCards(shangJiaCardList);
		ZTDoudizhuRule.sortCards(xiaJiaCardList);
		ZTDoudizhuRule.sortCards(lastOutPaiCardList);
		ZTDoudizhuRule.sortCards(lastCardList);

		if(!isSpeicalOut(myCardList, lastCardList, shangJiaCardList, xiaJiaCardList, relate, isXiaJiaIsEnemy, isXiaJiaLastOut, isOwner)) {
			return data;
		}
		data = dealCheatSpecialOut(myCardList, lastCardList, shangJiaCardList, xiaJiaCardList, relate, isXiaJiaIsEnemy, isXiaJiaLastOut, isOwner);
		if (data == null) {
			data = getCheatAnalogyOutPai(myCardList, shangJiaCardList,
						xiaJiaCardList, lastCardList, isOwner, isXiaJiaIsEnemy);
			if (data == null) {
				data = getCheatSuitableOutPai(myCardList, lastCardList,
						lastOutPaiCardList, xiaJiaCardList, relate, isXiaJiaIsEnemy);
			}
		}
		return data;
	}
	
	

	private static List<Card> ergodicList(List<Integer> list) {
		List<Card> cardList = new ArrayList<Card>();
		for (Integer id : list) {
			Card card = new Card(id);
			cardList.add(card);
		}
		return cardList;
	}

	/***
	 * 不作弊的合适出牌情况
	 * 
	 * @param cards
	 * @return
	 */
	private static List<Integer> getNoCheatSuitableSelfOutPai(
			List<Card> cardList, List<Integer> enemyCardSizeList) {

		ZTDoudizhuRule.sortCards(cardList);

		Map<CardType, List<List<Card>>> map = ZTDoudizhuRule
				.arrangePai(cardList);

		List<Integer> data = aiWithLittle(map, cardList.size(),
				enemyCardSizeList);
		if (data.size() > 0) {
			return data;
		}
		return null;
	}

	/***
	 * 不作弊的最后一次找牌
	 * 
	 * @param cards
	 * @return
	 */
	private static List<Integer> getNoCheatLastFindSelfOutPai(List<Card> cardList, List<Integer> enemyCardSizeList) {

		ZTDoudizhuRule.sortCards(cardList);
		Map<CardType, List<List<Card>>> map = ZTDoudizhuRule
				.arrangePai(cardList);

		List<Integer> data = aiLastFindPai(map, cardList.size(), enemyCardSizeList);
		if (data.size() > 0) {
			return data;
		}

		return null;
	}

	/***
	 * 是否最后一手牌
	 * 
	 * @param cards
	 * @return
	 */

	private static boolean isLastPai(List<Card> cardList) {
		if (ZTDoudizhuRule.getCardType(cardList) != null) {
			return true;
		}
		return false;
	}

	// 优先找小牌打
	private static List<Integer> aiWithLittle(
			Map<CardType, List<List<Card>>> map, int paiSize,
			List<Integer> enemyPaiSizeList) {

		List<Integer> data = new ArrayList<Integer>();
		// List<Card> cardList = new ArrayList<Card>();

		if (map.containsKey(CardType.FEI_JI)) {
			for (List<Card> cardList : map.get(CardType.FEI_JI)) {
				// cardList = map.get(CardType.FEI_JI).get(0);
				boolean isBreakZhaDan = false;
				if (map.containsKey(CardType.ZHA_DAN)) {
					List<List<Card>> zhaDanList = map.get(CardType.ZHA_DAN);
					isBreakZhaDan = getBreakPaiNumber(cardList, zhaDanList) > 0;
				}
				if(map.containsKey(CardType.WANG_ZHA) && !isBreakZhaDan) {
					List<List<Card>> wangZhaList = map.get(CardType.WANG_ZHA);
					isBreakZhaDan = getBreakPaiNumber(cardList, wangZhaList) > 0;
				}
				boolean isOutFeiJi = isOutFeiJi(cardList, paiSize);
				if (!isBreakZhaDan && isOutFeiJi) {
					// 拼接飞机
					List<Card> result = getPingJieFeiJiOrSanDaiYiOrSiDaiErPai(
							CardType.FEI_JI, cardList, map);
					boolean flag = result != null;
					if (flag) {
						for (Card card : result) {
							data.add(card.getId());
						}
						return data;
					}
				}
			}
		}
		if (map.containsKey(CardType.SHUN_ZI)) {
			Card card = new Card(10);
			List<Card> cardList = new ArrayList<Card>();
			int size = map.get(CardType.SHUN_ZI).size();
			for (int i = 0; i < size; i++) {
				cardList = map.get(CardType.SHUN_ZI).get(i);
				boolean isBreakZhaDan = false;
				if (map.containsKey(CardType.ZHA_DAN)) {
					isBreakZhaDan = getBreakPaiNumber(cardList,
							map.get(CardType.ZHA_DAN)) > 0;
					if (isBreakZhaDan) {
						continue;
					}
				}
				if(map.containsKey(CardType.WANG_ZHA)) {
					isBreakZhaDan = getBreakPaiNumber(cardList, map.get(CardType.WANG_ZHA)) > 0;
					if(isBreakZhaDan) {
						continue;
					}
				}
				if (isOutPai(card, cardList, paiSize, SHUNZINUM)) {
					int outShunZiSize = getOutShunZiLength(cardList, paiSize,
							map);
					if (outShunZiSize == 0) {
						continue;
					}
					for (int num = 0; num < outShunZiSize; num++) {
						data.add(cardList.get(num).getId());
					}
					return data;
				}
			}
		}
		if (map.containsKey(CardType.SAN_BU_DAI)) {
			Card card = new Card(12);
			for (List<Card> cardList : map.get(CardType.SAN_BU_DAI)) {
				// cardList = map.get(CardType.SAN_BU_DAI).get(0);
				boolean isBreakZhaDan = false;
				if (map.containsKey(CardType.ZHA_DAN)) {
					isBreakZhaDan = getBreakPaiNumber(cardList,
							map.get(CardType.ZHA_DAN)) > 0;
				}
				if(map.containsKey(CardType.WANG_ZHA) && !isBreakZhaDan) {
					isBreakZhaDan = getBreakPaiNumber(cardList, map.get(CardType.WANG_ZHA)) > 0;
				}
				if (!isBreakZhaDan
						&& isOutPai(card, cardList, paiSize, SANDAIYINUM)) {
					// tool
					// 拼接三带一
					List<Card> result = getPingJieFeiJiOrSanDaiYiOrSiDaiErPai(
							CardType.SAN_BU_DAI, cardList, map);
					boolean flag = result != null;
					if (flag) {
						for (Card c : result) {
							data.add(c.getId());
						}
						return data;
					}
				}
			}

		}
		if (map.containsKey(CardType.LIAN_DUI)) {
			Card card = new Card(10);
			for (List<Card> cardList : map.get(CardType.LIAN_DUI)) {
				// cardList = map.get(CardType.LIAN_DUI).get(0);
				boolean isBreakZhaDan = false;
				if (map.containsKey(CardType.ZHA_DAN)) {
					isBreakZhaDan = getBreakPaiNumber(cardList,
							map.get(CardType.ZHA_DAN)) > 0;
				}
				if(map.containsKey(CardType.WANG_ZHA) && !isBreakZhaDan) {
					isBreakZhaDan = getBreakPaiNumber(cardList, map.get(CardType.WANG_ZHA)) > 0;
				}
				if (!isBreakZhaDan
						&& isOutPai(card, cardList, paiSize, LIANDUINUM)) {
					for (int i = 0; i < cardList.size(); i++) {
						data.add(cardList.get(i).getId());
					}
					return data;
				}
			}
		}
		if (map.containsKey(CardType.DUI_ZI)) {
			Card card = new Card(11);
			for (List<Card> cardList : map.get(CardType.DUI_ZI)) {
				boolean isBreakZhaDan = false;
				boolean isDuiShouPaiSizeEqualEr = false;
				for (Integer size : enemyPaiSizeList) {
					if (size == 2) {
						isDuiShouPaiSizeEqualEr = true;
						break;
					}
				}
				if (map.containsKey(CardType.ZHA_DAN)) {
					isBreakZhaDan = getBreakPaiNumber(cardList,
							map.get(CardType.ZHA_DAN)) > 0;
				}
				if(map.containsKey(CardType.WANG_ZHA) && !isBreakZhaDan) {
					isBreakZhaDan = getBreakPaiNumber(cardList, map.get(CardType.WANG_ZHA)) > 0;
				}
				if (!isBreakZhaDan
						&& isOutPai(card, cardList, paiSize, DUIZINUM)
						&& !isDuiShouPaiSizeEqualEr) {
					for (Card c : cardList) {
						data.add(c.getId());
					}
					return data;
				}
			}
		}
		if (map.containsKey(CardType.DAN)) {
			Card card = new Card(13);
			boolean isDuiShouPaiSizeEqualOne = false;
			for (Integer size : enemyPaiSizeList) {
				if (size == 1) {
					isDuiShouPaiSizeEqualOne = true;
					break;
				}
			}
			for (List<Card> cardList : map.get(CardType.DAN)) {
				boolean isBreakZhaDan = false;
				if (map.containsKey(CardType.ZHA_DAN)) {
					isBreakZhaDan = getBreakPaiNumber(cardList,
							map.get(CardType.ZHA_DAN)) > 0;
				}
				if(map.containsKey(CardType.WANG_ZHA) && !isBreakZhaDan) {
					isBreakZhaDan = getBreakPaiNumber(cardList, map.get(CardType.WANG_ZHA)) > 0;
				}
				if (!isBreakZhaDan && isOutPai(card, cardList, paiSize, DANNUM)
						&& !isDuiShouPaiSizeEqualOne) {
					for (Card c : cardList) {
						data.add(c.getId());
					}
					return data;
				}
			}
		}
		return data;
	}

	// 最后一次找牌
	private static List<Integer> aiLastFindPai(
			Map<CardType, List<List<Card>>> map, int paiSize, List<Integer> enemyCardSizeList) {

		List<Integer> data = new ArrayList<Integer>();
		if (map.containsKey(CardType.SI_DAI_ER)) {
			// tool
		}
		if (map.containsKey(CardType.FEI_JI)) {
			for (List<Card> cardList : map.get(CardType.FEI_JI)) {
				boolean isBreakZhaDan = false;
				if (map.containsKey(CardType.ZHA_DAN)) {
					List<List<Card>> zhaDanList = map.get(CardType.ZHA_DAN);
					isBreakZhaDan = getBreakPaiNumber(cardList, zhaDanList) > 0;
				}
				if(map.containsKey(CardType.WANG_ZHA) && !isBreakZhaDan) {
					isBreakZhaDan = getBreakPaiNumber(cardList, map.get(CardType.WANG_ZHA)) > 0;
				}
				if (!isBreakZhaDan) {
					// 拼接飞机
					boolean flag = getPingJieFeiJiOrSanDaiYiOrSiDaiErPai(
							CardType.FEI_JI, cardList, map) != null;
					if (flag) {
						for (Card card : cardList) {
							data.add(card.getId());
						}
						return data;
					}
				}
			}

		}
		if (map.containsKey(CardType.SHUN_ZI)) {
			List<Card> cardList = new ArrayList<Card>();
			int size = map.get(CardType.SHUN_ZI).size();
			for (int i = 0; i < size; i++) {
				cardList = map.get(CardType.SHUN_ZI).get(i);
				boolean isBreakZhaDan = false;
				if (map.containsKey(CardType.ZHA_DAN)) {
					isBreakZhaDan = getBreakPaiNumber(cardList,
							map.get(CardType.ZHA_DAN)) > 0;
					if (isBreakZhaDan) {
						continue;
					}
				}
				if(map.containsKey(CardType.WANG_ZHA)) {
					isBreakZhaDan = getBreakPaiNumber(cardList, map.get(CardType.WANG_ZHA)) > 0;
					if(isBreakZhaDan) {
						continue;
					}
				}
				int outShunZiSize = getOutShunZiLength(cardList, paiSize, map);
				if (outShunZiSize == 0) {
					continue;
				}
				for (int num = 0; num < outShunZiSize; num++) {
					data.add(cardList.get(num).getId());
				}
				return data;
			}
		}
		if (map.containsKey(CardType.SAN_DAI_YI)) {
			for (List<Card> cardList : map.get(CardType.SAN_BU_DAI)) {
				// cardList = map.get(CardType.SAN_BU_DAI).get(0);
				boolean isBreakZhaDan = false;
				if (map.containsKey(CardType.ZHA_DAN)) {
					isBreakZhaDan = getBreakPaiNumber(cardList,
							map.get(CardType.ZHA_DAN)) > 0;
				}
				if(map.containsKey(CardType.WANG_ZHA) && !isBreakZhaDan) {
					isBreakZhaDan = getBreakPaiNumber(cardList, map.get(CardType.WANG_ZHA)) > 0;
				}
				if (!isBreakZhaDan) {
					// tool
					// 拼接三带一
					boolean flag = getPingJieFeiJiOrSanDaiYiOrSiDaiErPai(
							CardType.SAN_DAI_YI, cardList, map) != null;
					if (flag) {
						for (Card c : cardList) {
							data.add(c.getId());
						}
						return data;
					}
				}
			}
		}
		if (map.containsKey(CardType.LIAN_DUI)) {
			// tool
			for (List<Card> cardList : map.get(CardType.LIAN_DUI)) {
				// cardList = map.get(CardType.LIAN_DUI).get(0);
				boolean isBreakZhaDan = false;
				if (map.containsKey(CardType.ZHA_DAN)) {
					isBreakZhaDan = getBreakPaiNumber(cardList,
							map.get(CardType.ZHA_DAN)) > 0;
				}
				if(map.containsKey(CardType.WANG_ZHA) && !isBreakZhaDan) {
					isBreakZhaDan = getBreakPaiNumber(cardList, map.get(CardType.WANG_ZHA)) > 0;
				}
				if (!isBreakZhaDan && !isLianDuiBreakPaiNumOver(cardList, map)) {
					for (int i = 0; i < cardList.size(); i++) {
						data.add(cardList.get(i).getId());
					}
					return data;
				}
			}
		}
		if (map.containsKey(CardType.DUI_ZI)) {
			// tool
			for (List<Card> cardList : map.get(CardType.DUI_ZI)) {
				boolean isBreakZhaDan = false;
				boolean isDuiShouPaiSizeEqualEr = false;
				for(Integer integer : enemyCardSizeList) {
					if(integer == 2) {
						isDuiShouPaiSizeEqualEr = true;
						break;
					}
				}
				if (map.containsKey(CardType.ZHA_DAN)) {
					isBreakZhaDan = getBreakPaiNumber(cardList,
							map.get(CardType.ZHA_DAN)) > 0;
				}
				if(map.containsKey(CardType.WANG_ZHA) && !isBreakZhaDan) {
					isBreakZhaDan = getBreakPaiNumber(cardList, map.get(CardType.WANG_ZHA)) > 0;
				}
				if (!isBreakZhaDan && !isDuiShouPaiSizeEqualEr) {
					for (Card c : cardList) {
						data.add(c.getId());
					}
					return data;
				}
			}
		}
		if (map.containsKey(CardType.DAN)) {
			// tool
			boolean isDuiShouPaiSizeEqualOne = false;
			for(Integer size : enemyCardSizeList) {
				if(size == 1) {
					isDuiShouPaiSizeEqualOne = true;
					break;
				}
			}
			for (List<Card> cardList : map.get(CardType.DAN)) {
				boolean isBreakZhaDan = false;
				if (map.containsKey(CardType.ZHA_DAN)) {
					isBreakZhaDan = getBreakPaiNumber(cardList,
							map.get(CardType.ZHA_DAN)) > 0;
				}
				if(map.containsKey(CardType.WANG_ZHA) && !isBreakZhaDan) {
					isBreakZhaDan = getBreakPaiNumber(cardList, map.get(CardType.WANG_ZHA)) > 0;
				}
				if (!isBreakZhaDan && !isDuiShouPaiSizeEqualOne) {
					for (Card c : cardList) {
						data.add(c.getId());
					}
					return data;
				}
			}
		}

		// else {
		// //tool
		// //判断下家是否是对手，根据情况出牌
		// }
		return data;
	}

	// 牌是否比二小
	private static boolean isPaiLessThanEr(List<Card> cardList) {
		Card card = new Card(2);
		if (cardList.get(0).getGrade() < card.getGrade()) {
			return true;
		}
		return false;
	}

	// 是否能出飞机
	private static boolean isOutFeiJi(List<Card> cardList, int paiSize) {
		Card card = new Card(13);
		boolean isOverK = false;
		int listSize = cardList.size();
		for (int i = 0; i < listSize; i = i + 3) {
			isOverK = cardList.get(i).getGrade() > card.getGrade();
			if (isOverK) {
				break;
			}
		}
		if (isOverK && paiSize > 8) {
			return false;
		}
		return true;
	}

	// 出顺子的长度
	private static int getOutShunZiLength(List<Card> cardList, int paiSize,
			Map<CardType, List<List<Card>>> map) {

		int listSize = cardList.size();
		while (listSize >= 5) {
			int breakSize = 0;
			if (map.containsKey(CardType.DUI_ZI)) {
				breakSize = getBreakPaiNumber(cardList,
						map.get(CardType.DUI_ZI));
			}

			if (listSize == 5) {
				if (breakSize > (listSize / 3)) {
					return 0;
				}
			}
			if (breakSize > (listSize / 3)) {
				listSize--;
			} else {
				break;
			}
		}

		if (listSize >= 6) {
			List<Card> lastCard = new ArrayList<Card>();
			lastCard.add(cardList.get(listSize - 1));
			if (map.containsKey(CardType.DUI_ZI)) {
				if (getBreakPaiNumber(lastCard, map.get(CardType.DUI_ZI)) > 0) {
					listSize--;
				}
			}

		}

		return listSize;
	}

	// 判断能否出牌
	private static boolean isOutPai(Card card, List<Card> cardList, int paiSize,
			int num) {
		boolean isOverCard = cardList.get(0).getGrade() > card.getGrade();
		if (isOverCard && paiSize > num) {
			return false;
		}
		return true;
	}

	private static boolean isLianDuiBreakPaiNumOver(List<Card> list,
			Map<CardType, List<List<Card>>> map) {
		int sub = 0;
		if (map.containsKey(CardType.DUI_ZI)) {
			sub += getBreakPaiNumber(list, map.get(CardType.DUI_ZI));
		}
		if (map.containsKey(CardType.SHUN_ZI)) {
			sub += getBreakPaiNumber(list, map.get(CardType.SHUN_ZI));
		}
		if (sub > (list.size() / 3)) {
			return true;
		}
		return false;
	}

	// 拆散牌组的数量
	private static int getBreakPaiNumber(List<Card> list,
			List<List<Card>> paiList) {
		int breakSize = 0;
		Map<Integer, Card> cardMap = new HashMap<Integer, Card>();
		for (Card card : list) {
			cardMap.put(card.getId(), card);
		}
		for (List<Card> cards : paiList) {
			for (Card card : cards) {
				if (cardMap.containsKey(card.getId())) {
					breakSize++;
					continue;
				}
			}
		}
		return breakSize;
	}
	
	
	
	//最后出牌时的特殊处理
	private static List<Integer> dealLastSelfSpecialOut(List<Card> allPai, List<Integer> enemyCardSizeList ) {
		
		List<Integer> data = new ArrayList<Integer>();
		Map<CardType, List<List<Card>>> map = ZTDoudizhuRule.arrangePai(allPai);
		boolean isEnemyCardSizeEqualOne = false;
		for(Integer size : enemyCardSizeList) {
			if(size == 1) {
				isEnemyCardSizeEqualOne = true;
				break;
			}
		}
		if(map.containsKey(CardType.DAN)) {
			List<List<Card>> danList = map.get(CardType.DAN);
			if(!isEnemyCardSizeEqualOne) {
				for(Card card : danList.get(0)) {
					data.add(card.getId());
				}
				return data;
			}else {
				int size = danList.size();
				Card card = danList.get(size - 1).get(0);
				if(card.getId() == 54) {
					if(map.containsKey(CardType.WANG_ZHA)) {
						if(size >= 3) {
							Card c = danList.get(size - 3).get(0);
							data.add(c.getId());
							return data;
						}
					}
				}else {
					data.add(card.getId());
					return data;
				}
			}
		}
		
		if(data.size() == 0) {
			List<Card> cardList = new ArrayList<Card>();
			for(int i = 0; i < allPai.size(); i++) {
				List<Card> copyList = new ArrayList<Card>();
				copyList.addAll(cardList);
				copyList.add(allPai.get(i));
				CardType cardType = ZTDoudizhuRule.getCardType(copyList);
				if(cardType != null) {
					cardList.add(allPai.get(i));
					continue;
				}else {
					CardType type = ZTDoudizhuRule.getCardType(cardList);
					if(type != CardType.ZHA_DAN && type != CardType.WANG_ZHA) {
						for(Card card :cardList) {
							data.add(card.getId());
						}
						return data;
					}
				}
			}
		}
		return data;
	}

	/***
	 * 作弊出牌特殊情况处理
	 * 
	 * @param cards
	 * @return
	 */
	private static List<Integer> dealSelfSpecialOut(List<Card> allPai, List<Card> xiaJiaPai, boolean isXiaJiaEnemy) {
		
		
		
		
		List<Integer> data = new ArrayList<Integer>();
		CardType xiaJiaCardType = ZTDoudizhuRule.getCardType(xiaJiaPai);
		Map<CardType, List<List<Card>>> map = ZTDoudizhuRule.arrangePai(allPai);
		
		//剩下王炸与两单牌
		if(allPai.size() == 4) {
			if(map.containsKey(CardType.WANG_ZHA) && map.containsKey(CardType.DAN)) {
				Card card = map.get(CardType.DAN).get(0).get(0);
				data.add(card.getId());
				return data;
			}
		}
		
		//王炸与一手牌
		boolean isOnlyZhaAndYiShouPai = isOnlyZhaAndYiShouPai(allPai, map);
		if(isOnlyZhaAndYiShouPai) {
			if(map.containsKey(CardType.WANG_ZHA)) {
				for(Card card : map.get(CardType.WANG_ZHA).get(0)) {
					data.add(card.getId());
				}
				return data;
			}
		}
		
		//地主下家--检测队友的牌
		if(!isXiaJiaEnemy) {
			if(xiaJiaCardType != null) {
				if(map.containsKey(xiaJiaCardType)) {
					List<Card> cardList = map.get(xiaJiaCardType).get(0);
					if(ZTDoudizhuRule.isOvercomePrev(xiaJiaPai, xiaJiaCardType, cardList, xiaJiaCardType)) {
						for(Card card : cardList) {
							data.add(card.getId());
						}
						return data;
					}
				}
			}
		}
		return null;
	}
	
	
	/***
	 * 不作弊出牌特殊情况处理
	 * 
	 * @param cards
	 * @return
	 */
	private static List<Integer> dealNoCheatSpecialSelfOut(List<Card> allPai, List<Card> recyclePai) {
		//直接出完
		List<Integer> data = new ArrayList<Integer>();
		if(ZTDoudizhuRule.getCardType(allPai) != null) {
			for (Card card : allPai) {
				data.add(card.getId());
			}
			return data;
		}
		
		//初始化所有牌
		List<Card> pai = initAllPai();
		//移除已出的牌
		removeMyPaiCard(pai, recyclePai);
		//移除自己的手牌
		removeMyPaiCard(pai, allPai);
		//排序
		ZTDoudizhuRule.sortCards(pai);
		
		Map<CardType, List<List<Card>>> map = ZTDoudizhuRule.arrangePai(allPai);
		
		//只有炸弹
		boolean isOnlyHasZha = isOnlyHasZha(map);
		if(isOnlyHasZha) {
			if(map.containsKey(CardType.ZHA_DAN)) {
				for(Card card : map.get(CardType.ZHA_DAN).get(0)) {
					data.add(card.getId());
				}
				return data;
			}
			
			if(map.containsKey(CardType.WANG_ZHA)) {
				for(Card card : map.get(CardType.WANG_ZHA).get(0)) {
					data.add(card.getId());
				}
				return data;
			}
		}
		
		//炸弹和一手牌
		boolean isOnlyZhaAndYiShouPai = isOnlyZhaAndYiShouPai(allPai, map);
		if(isOnlyZhaAndYiShouPai) {
			if(map.containsKey(CardType.WANG_ZHA)) {
				for(Card card : map.get(CardType.WANG_ZHA).get(0)) {
					data.add(card.getId());
				}
				return data;
			}else {
				//剩下的牌的所有牌型
				if(pai.size() > 0) {
					Map<CardType, List<List<Card>>> paiMap = ZTDoudizhuRule.arrangePai(pai);
					List<Card> zhaList = map.get(CardType.ZHA_DAN).get(0);
					if(paiMap.containsKey(CardType.ZHA_DAN)) {
						boolean isMax = true;
						for(List<Card> cards : paiMap.get(CardType.ZHA_DAN)) {
							if(ZTDoudizhuRule.isOvercomePrev(cards, CardType.ZHA_DAN, zhaList , CardType.ZHA_DAN)) {
								isMax = false;
								break;
							}
						}
						if(isMax) {
							for(Card card : zhaList) {
								data.add(card.getId());
							}
							return data;
						}
					}else {
						for(Card card : zhaList) {
							data.add(card.getId());
						}
						return data;
					}
				}
			}
		}		
		
		return null;
		
	}
	/***
	 * 不作弊跟牌特殊情况处理
	 * 
	 * @param cards
	 * @return
	 */
	private static List<Integer> dealNoCheatSpecialOut(List<Card> allPai,
			List<Card> lastPai, List<Card> recyclePai) {
		
		//直接出完
		List<Integer> data = new ArrayList<Integer>();
		CardType cardType = ZTDoudizhuRule.getCardType(allPai);
		CardType lastCardType = ZTDoudizhuRule.getCardType(lastPai);
		if (cardType != null) {
			if (ZTDoudizhuRule.isOvercomePrev(allPai, cardType, lastPai,
					lastCardType)) {
				for (Card card : allPai) {
					data.add(card.getId());
				}
				return data;
			}

		}
		
		//初始化所有牌
		List<Card> pai = initAllPai();
		//移除已出的牌
		removeMyPaiCard(pai, recyclePai);
		//移除自己的手牌
		removeMyPaiCard(pai, allPai);
		//排序
		ZTDoudizhuRule.sortCards(pai);
		
		//炸弹和一手牌
		Map<CardType, List<List<Card>>> map = ZTDoudizhuRule.arrangePai(allPai);
		boolean isOnlyZhaAndYiShouPai = isOnlyZhaAndYiShouPai(allPai, map);
		if(isOnlyZhaAndYiShouPai) {
			if(map.containsKey(CardType.WANG_ZHA)) {
				for(Card card : map.get(CardType.WANG_ZHA).get(0)) {
					data.add(card.getId());
				}
				return data;
			}else {
				//剩下的牌的所有牌型
				if(pai.size() > 0) {
					Map<CardType, List<List<Card>>> paiMap = ZTDoudizhuRule.arrangePai(pai);
					for(List<Card> zhaList : map.get(CardType.ZHA_DAN)) {
						if(ZTDoudizhuRule.isOvercomePrev(zhaList, CardType.ZHA_DAN, lastPai, lastCardType)) {
							if(paiMap.containsKey(CardType.ZHA_DAN)) {
								boolean isMax = true;
								for(List<Card> cards : paiMap.get(CardType.ZHA_DAN)) {
									if(ZTDoudizhuRule.isOvercomePrev(cards, CardType.ZHA_DAN, zhaList , CardType.ZHA_DAN)) {
										isMax = false;
										break;
									}
								}
								if(isMax) {
									for(Card card : zhaList) {
										data.add(card.getId());
									}
									return data;
								}
							}else {
								for(Card card : zhaList) {
									data.add(card.getId());
								}
								return data;
							}
						}
					}
				}
			}
		}
		//农民间的配合
		
		return null;
	}
	
	//初始化所有牌
	private static List<Card> initAllPai() {
		List<Card> initList = new ArrayList<Card>();
		for(int i = 1; i <= 54; i++) {
			Card card = new Card(i);
			initList.add(card);
		}
		return initList;
	}
	//得到剩下的牌
	private static List<Card> getRemainPai(List<Card> recyclePai) {
		//剩下的所有牌
		List<Card> pai = new ArrayList<Card>();
		List<Card> copyRecyclePai = new ArrayList<Card>();
		copyRecyclePai.addAll(recyclePai);
		for(int i = 1; i <= 54; i++) {
			Iterator<Card> it = copyRecyclePai.iterator();
			while(it.hasNext()) {
				Card card = it.next();
				if(card.getId() == i) {
					it.remove();
				}
			}
			pai.add(new Card(i));
		}
		return pai;
	}
	
	private static boolean isOnlyHasZha(Map<CardType, List<List<Card>>> map) {
		boolean isOnlyHasZha = true;
		for(Map.Entry<CardType, List<List<Card>>> entry : map.entrySet()) {
			if(entry.getKey() != CardType.ZHA_DAN && entry.getKey() != CardType.WANG_ZHA) {
				isOnlyHasZha = false;
			}
		}
		return isOnlyHasZha;
	}
	
	private static boolean isOnlyZhaAndYiShouPai(List<Card> allPai, Map<CardType, List<List<Card>>> map) {
		List<Card> copyCard = new ArrayList<Card>();
		copyCard.addAll(allPai);
		if(map.containsKey(CardType.ZHA_DAN)) {
			removeMyPaiCard(copyCard, map.get(CardType.ZHA_DAN).get(0));
			if(copyCard.size() != 0) {
				if(ZTDoudizhuRule.getCardType(copyCard) != null) {
					return true;
				}
			}
		}
		if(map.containsKey(CardType.WANG_ZHA)) {
			removeMyPaiCard(copyCard, map.get(CardType.WANG_ZHA).get(0));
			if(copyCard.size() != 0) {
				if(ZTDoudizhuRule.getCardType(copyCard) != null) {
					return true;
				}
			}
		}
		return false;
	}

	
	//作弊跟牌是否处理
	private static boolean isSpeicalOut(List<Card> allPai,
			List<Card> lastPai, List<Card> shangJiaPaiList, 
			List<Card> xiaJiaPaiList, int relate, boolean isXiaJiaEnemy, boolean isXiaJiaLastOut, boolean isOwner) {
		CardType lastCardType = ZTDoudizhuRule.getCardType(lastPai);
		Map<CardType, List<List<Card>>> xiaJiaMap = ZTDoudizhuRule.arrangePai(xiaJiaPaiList);
		if(isXiaJiaLastOut) {
			boolean canWin = getAnalogySelfOutPai(xiaJiaPaiList, allPai, shangJiaPaiList, new Stack<List<Card>>(), isOwner, isXiaJiaEnemy);
			if(isXiaJiaEnemy) {
				if(canWin) {
					return true;
				}
			}else {
				if(canWin || isContainAce(lastPai)) {
					return false;
				}
			}
		}else {
			boolean canWin = getAnalogySelfOutPai(shangJiaPaiList, xiaJiaPaiList, allPai, new Stack<List<Card>>(), isOwner, isXiaJiaEnemy);
			if(relate == 1 ) {
				if(canWin) {
					return true;
				}
			}else {
				if(!xiaJiaMap.containsKey(lastCardType) && canWin) {
					return false;
				}
				if(isContainAce(lastPai)) {
					return false;
				}
			}
		}
		return true;
	}

	/***
	 * 作弊跟牌特殊情况处理
	 * 
	 * @param cards
	 * @return
	 */
	private static List<Integer> dealCheatSpecialOut(List<Card> allPai,
			List<Card> lastPai, List<Card> shangJiaPaiList, 
			List<Card> xiaJiaPaiList, int relate, boolean isXiaJiaEnemy, boolean isXiaJiaLastOut, boolean isOwner) {
		List<Integer> data = new ArrayList<Integer>();
		CardType cardType = ZTDoudizhuRule.getCardType(allPai);
		CardType lastCardType = ZTDoudizhuRule.getCardType(lastPai);
		Map<CardType, List<List<Card>>> map = ZTDoudizhuRule.arrangePai(allPai);
		Map<CardType, List<List<Card>>> shangJiaMap = ZTDoudizhuRule.arrangePai(shangJiaPaiList);
		Map<CardType, List<List<Card>>> xiaJiaMap = ZTDoudizhuRule.arrangePai(xiaJiaPaiList);
		
		//直接出完
		if (cardType != null) {
			if (ZTDoudizhuRule.isOvercomePrev(allPai, cardType, lastPai,
					lastCardType)) {
				for (Card card : allPai) {
					data.add(card.getId());
				}
				return data;
			}
		}
		
		//炸弹（最大）与与炸弹数量相等的手数
		
		// 炸弹数量
		int zhaNum = 0; 
		//炸弹是否最大
		boolean isMax = true;
		//敌人的集合
		List<Map<CardType, List<List<Card>>>> enemyMapList = new ArrayList<Map<CardType,List<List<Card>>>>();
		if(map.containsKey(CardType.ZHA_DAN) || map.containsKey(CardType.WANG_ZHA)) {
			List<Card> copyMyPai = new ArrayList<Card>();
			copyMyPai.addAll(allPai);
			//存在炸弹
			if(map.containsKey(CardType.ZHA_DAN)) {
				zhaNum += map.get(CardType.ZHA_DAN).size();
				for(List<Card> zhaList : map.get(CardType.ZHA_DAN)) {
					if(ZTDoudizhuRule.isOvercomePrev(zhaList, CardType.ZHA_DAN, lastPai, cardType)) {
						removeMyPaiCard(copyMyPai, zhaList);
					}
				}
			}
			//存在王炸
			if(map.containsKey(CardType.WANG_ZHA)) {
				zhaNum++;
				removeMyPaiCard(copyMyPai, map.get(CardType.WANG_ZHA).get(0));
			}
			//得到最少手数
			int shouNum = getShouShuNum(copyMyPai);
			if(zhaNum == shouNum + 1) {
				if(isOwner) {
					enemyMapList.add(shangJiaMap);
					enemyMapList.add(xiaJiaMap);
					for(List<Card> zhaList : map.get(CardType.ZHA_DAN)) {
						if(!isZhaMax(zhaList, enemyMapList)) {
							isMax = false;
							break;
						}
					}
				}else {
					if(isXiaJiaEnemy) {
						enemyMapList.add(xiaJiaMap);
						for(List<Card> zhaList : map.get(CardType.ZHA_DAN)) {
							if(!isZhaMax(zhaList, enemyMapList)) {
								isMax = false;
								break;
							}
						}
					}else {
						enemyMapList.add(shangJiaMap);
						for(List<Card> zhaList : map.get(CardType.ZHA_DAN)) {
							if(!isZhaMax(zhaList, enemyMapList)) {
								isMax = false;
								break;
							}
						}
					}
				}
				
				if(isMax) {
					for(Card card : map.get(CardType.ZHA_DAN).get(0)) {
						data.add(card.getId());
					}
					return data;
				}
			}
		}
		
		//农民间配合
		if(isOwner) {
			
		}

		return null;
	}
	
	
	//得到炸弹是否比敌人大
	private static boolean isZhaMax(List<Card> zhaList, List<Map<CardType, List<List<Card>>>> enemyMapList) {
		for(Map<CardType, List<List<Card>>> map : enemyMapList) {
			if(map.containsKey(CardType.WANG_ZHA)) {
				return false;
			}
			if(map.containsKey(CardType.ZHA_DAN)) {
				for(List<Card> cardList : map.get(CardType.ZHA_DAN)) {
					if(cardList.get(0).getGrade() > zhaList.get(0).getGrade()) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	
	//得到该牌的最小手数
	private static int getShouShuNum(List<Card> allCard) {
		LogUtil.debug("计算最小手数");
		int minNum = 0;
		int number = 0;
		if(allCard.size() == 0) {
			return 0;
		}
		if(ZTDoudizhuRule.getCardType(allCard) != null) {
			return 1;
		}
		Map<CardType, List<List<Card>>> map = ZTDoudizhuRule.arrangePai(allCard);
		for(Map.Entry<CardType, List<List<Card>>> entry : map.entrySet()) {
			if(entry.getKey() == CardType.DAN) {
				if(map.size() != 1) {
					continue;	
				}else {
					number = number + map.get(CardType.DAN).size();
					return number;
				}
			}
			if(entry.getKey() == CardType.SAN_BU_DAI || entry.getKey() == CardType.FEI_JI) {
				for(List<Card> cardList : entry.getValue()) {
					List<Card> copyCard = new ArrayList<Card>();
					copyCard.addAll(allCard);
					cardList = getPingJieFeiJiOrSanDaiYiOrSiDaiErPai(entry.getKey(), cardList, map);
					if(cardList != null) {
						removeMyPaiCard(copyCard, cardList);
						number = getShouShuNum(copyCard) + 1;
						if(minNum == 0 || minNum > number) {
							minNum = number;
						}
					}
				}
			}else {
				for(List<Card> cardList : entry.getValue()) {
					List<Card> copyCard = new ArrayList<Card>();
					copyCard.addAll(allCard);
					removeMyPaiCard(copyCard, cardList);
					number = getShouShuNum(copyCard) + 1;
					if(minNum == 0 || minNum > number) {
						minNum = number;
					}
				}
			}
		}
		return minNum;
	}
	
	
	
	//分开拆牌与不拆牌的
	private static void splitChaiAndNoChai(List<List<Card>> chaiList, List<List<Card>> noChaiList, 
									Map<CardType, List<List<Card>>> map, List<Card> lastCard) {
		CardType cardType = ZTDoudizhuRule.getCardType(lastCard);
		if(cardType == CardType.ZHA_DAN) {
			return;
		}
		CardType type = cardType;
		if(cardType == CardType.SAN_DAI_YI) {
			type = CardType.SAN_BU_DAI;
		}
		if(map.containsKey(type)) {
			if(cardType == CardType.SAN_DAI_YI || cardType == CardType.FEI_JI) {
				List<Card> cardList = getFeiJiOrSanDaiYIOrSiDaiEr(cardType, lastCard);
				int num = cardList.size() / 3;
				if(cardType == CardType.SAN_DAI_YI) {
					if(map.containsKey(CardType.SAN_BU_DAI)) {
						for(List<Card> list : map.get(CardType.SAN_BU_DAI)){
							if(ZTDoudizhuRule.isOvercomePrev(list, CardType.SAN_BU_DAI, cardList, CardType.SAN_BU_DAI)) {
								if(map.containsKey(CardType.DAN)) {
									if(num > map.get(CardType.DAN).size()) {
										chaiList.add(list);
									}else {
										noChaiList.add(list);
									}
								}else {
									chaiList.add(list);
								}
							}
						}
					}
				}else {
					for(List<Card> list : map.get(CardType.FEI_JI)){
						if(ZTDoudizhuRule.isOvercomePrev(list, cardType, cardList, cardType)) {
							if(num > map.get(CardType.DAN).size()) {
								chaiList.add(list);
							}else {
								noChaiList.add(list);
							}
						}
					}
				}
			} else {
				boolean isBreakSanBuDai = false;
				boolean isBreakShunZi = false;
				boolean isBreakLianDui = false;
				for(List<Card> list : map.get(cardType)) {
					if(ZTDoudizhuRule.isOvercomePrev(list, cardType, lastCard, cardType)) {
						if(map.containsKey(CardType.SAN_BU_DAI)) {
							isBreakSanBuDai = getBreakPaiNumber(list, map.get(CardType.SAN_BU_DAI)) > 0;
							if(isBreakSanBuDai) {
								chaiList.add(list);
								continue;
							}
						}
						if(map.containsKey(CardType.SHUN_ZI) && cardType != CardType.SHUN_ZI) {
							isBreakShunZi = getBreakPaiNumber(list, map.get(CardType.SHUN_ZI)) > 0;
							if(isBreakShunZi) {
								chaiList.add(list);
								continue;
							}
						}
						if(map.containsKey(CardType.LIAN_DUI) && cardType != CardType.LIAN_DUI) {
							isBreakLianDui = getBreakPaiNumber(list, map.get(CardType.LIAN_DUI)) > 0;
							if(isBreakLianDui) {
								chaiList.add(list);
								continue;
							}
						}
						
						noChaiList.add(list);
					}
				}
			}
		}
	}
	
	private static void removeChaiZhaList(List<List<Card>> list, Map<CardType, List<List<Card>>> map) {
		Iterator<List<Card>> it = list.iterator();
		if(map.containsKey(CardType.ZHA_DAN) || map.containsKey(CardType.WANG_ZHA)) {
			while(it.hasNext()) {
				boolean isBreakZha = false;
				boolean isBreakWangZha = false;
				List<Card> cardList = it.next();
				if(map.containsKey(CardType.ZHA_DAN)) {
					isBreakZha = getBreakPaiNumber(cardList, map.get(CardType.ZHA_DAN)) > 0;
				}
				if(map.containsKey(CardType.WANG_ZHA)) {
					isBreakWangZha = getBreakPaiNumber(cardList, map.get(CardType.WANG_ZHA)) > 0;
				}
				if(isBreakZha || isBreakWangZha){
					it.remove();
				}
			}
		}
	}
	
	

	/***
	 * 不作弊的合适跟牌情况
	 * 
	 * @param cards
	 * @return
	 */
	private static List<Integer> getNoCheatSuitableOutPai(List<Card> lastPai,
			List<Card> allPai, int lastOutAllPaiSize, int relate, int xiaJiaSize, boolean isOwner) {
		CardType cardType = ZTDoudizhuRule.getCardType(lastPai);
		Map<CardType, List<List<Card>>> map = ZTDoudizhuRule.arrangePai(allPai);
		List<Integer> data = new ArrayList<Integer>();
		
		
		//拆牌的集合
		List<List<Card>> chaiList = new ArrayList<List<Card>>();
		//不拆牌的集合
		List<List<Card>> noChaiList = new ArrayList<List<Card>>();
		splitChaiAndNoChai(chaiList, noChaiList, map, lastPai);
		//移除拆了炸弹的牌
		removeChaiZhaList(chaiList, map);
		removeChaiZhaList(noChaiList, map);
		
		if(isOwner) { //下家牌数是1和2时,地主时的特殊处理
			if(map.containsKey(cardType)) {
				if(cardType == CardType.DAN) {
					if(xiaJiaSize == 1) {
						int danListSize = map.get(CardType.DAN).size();
						List<Card> cardList = map.get(CardType.DAN).get(danListSize - 1);
						if(ZTDoudizhuRule.isOvercomePrev(cardList, cardType, lastPai, cardType)) {
							for(Card card : cardList) {
								data.add(card.getId());
							}
							return data;
						}
					}
				}
				if(cardType == CardType.DUI_ZI) {
					if(xiaJiaSize == 2) {
						int duiListSize = map.get(CardType.DUI_ZI).size();
						List<Card> cardList =  map.get(CardType.DUI_ZI).get(duiListSize - 1);
						if(ZTDoudizhuRule.isOvercomePrev(cardList, cardType, lastPai, cardType)){
							for(Card card : cardList) {
								data.add(card.getId());
							}
							return data;
						}
					}
				}
			}
		}
		
		if(relate == 1) { //敌人
			if(noChaiList.size() > 0 || chaiList.size() > 0) {
				if(noChaiList.size() > 0) {
					int noChaiSize = noChaiList.size();
					if(lastOutAllPaiSize <= 2) {
						List<Card> cardList = noChaiList.get(noChaiSize - 1);
						if(cardType == CardType.SAN_DAI_YI || cardType == CardType.FEI_JI) {
							cardList = getPingJieFeiJiOrSanDaiYiOrSiDaiErPai(cardType, cardList, map);
						}
						for(Card card : cardList) {
							data.add(card.getId());
						}
					}else {
						List<Card> cardList = noChaiList.get(0);
						if(cardType == CardType.SAN_DAI_YI || cardType == CardType.FEI_JI) {
							cardList = getPingJieFeiJiOrSanDaiYiOrSiDaiErPai(cardType, cardList, map);
						}
						for(Card card : cardList) {
							data.add(card.getId());
						}
					}
					return data;
				}else {
					if(lastOutAllPaiSize > 10) {
						return null;
					}else {
						int minNum = 0;
						List<Card> minNumList = new ArrayList<Card>();
						for(List<Card> cardList : chaiList) {
							if(cardType == CardType.SAN_DAI_YI || cardType == CardType.FEI_JI) {
								cardList = getPingJieFeiJiOrSanDaiYiOrSiDaiErPai(cardType, cardList, map);
							}
							if(cardList != null) {
								List<Card> copyAllPai = new ArrayList<Card>();
								copyAllPai.addAll(allPai);
								removeMyPaiCard(copyAllPai, cardList);
								int shouNumAfterRemove = getShouShuNum(copyAllPai);
								if(minNum == 0 || minNum > shouNumAfterRemove) {
									minNum = shouNumAfterRemove;
									minNumList = cardList;
								}
							}
							
						}
						int shouNum = getShouShuNum(allPai);
						int num = minNum - shouNum;
						if(num > 2) {
							if(lastOutAllPaiSize <= 2) {
								for(Card card : minNumList) {
									data.add(card.getId());
								}
								if(data.size() == 0 && cardType == CardType.DAN) {
									Card card = allPai.get(allPai.size() - 1);
									if(card.getId() != 54) {
										data.add(card.getId());
									}
								}
								return data;
							}else {
								return null;
							}
						}else {
							for(Card card : minNumList) {
								data.add(card.getId());
							}
							return data;
						}
						
					}
				}
			}else {
				if(lastOutAllPaiSize > 2) {
					return null;
				}else {
					if(map.containsKey(CardType.ZHA_DAN)) {
						for(List<Card> zhaList : map.get(CardType.ZHA_DAN)) {
							if(ZTDoudizhuRule.isOvercomePrev(zhaList, CardType.ZHA_DAN, lastPai, cardType)) {
								for(Card card : zhaList) {
									data.add(card.getId());
								}
								return data;
							}
						}
					}
					if(map.containsKey(CardType.WANG_ZHA)) {
						for(List<Card> zhaList : map.get(CardType.WANG_ZHA)) {
							if(ZTDoudizhuRule.isOvercomePrev(zhaList, CardType.WANG_ZHA, lastPai, cardType)) {
								for(Card card : zhaList) {
									data.add(card.getId());
								}
								return data;
							}
						}
					}
				}
			}
			
		}else {//队友
			if(cardType == CardType.ZHA_DAN) { //队友出炸弹不出
				return null;
			}
			if(cardType == CardType.SAN_DAI_YI || cardType == CardType.FEI_JI) {
				CardType type = cardType;
				if(cardType == CardType.SAN_DAI_YI) {
					type = CardType.SAN_BU_DAI;
				}
				if(map.containsKey(type)) {
					for(List<Card> cardList : map.get(type)) {
						cardList = getPingJieFeiJiOrSanDaiYiOrSiDaiErPai(cardType, cardList, map);
						if(cardList != null) {
							if(ZTDoudizhuRule.isOvercomePrev(cardList, cardType, lastPai, cardType)) {
								int shouNum = getShouShuNum(allPai);
								if(shouNum <= 2) {
									for(Card card : cardList) {
										data.add(card.getId());
									}
									return data;
								}else {
									boolean isContainAce = isContainAce(lastPai);
									if(!isContainAce) {
										if (lastOutAllPaiSize > 2) {
											for(Card card : cardList) {
												data.add(card.getId());
											}
											return data;
										}
									}
								}
							}
						}
					}
				}
			}else {
				if(map.containsKey(cardType)) {
					if(cardType == CardType.DAN) {
						if(xiaJiaSize == 1) {
							int danListSize = map.get(CardType.DAN).size();
							List<Card> cardList = map.get(CardType.DAN).get(danListSize - 1);
							if(ZTDoudizhuRule.isOvercomePrev(cardList, cardType, lastPai, cardType)) {
								for(Card card : cardList) {
									data.add(card.getId());
								}
								return data;
							}
						}
					}
					if(cardType == CardType.DUI_ZI) {
						if(xiaJiaSize == 2) {
							int duiListSize = map.get(CardType.DUI_ZI).size();
							List<Card> cardList =  map.get(CardType.DUI_ZI).get(duiListSize - 1);
							if(ZTDoudizhuRule.isOvercomePrev(cardList, cardType, lastPai, cardType)){
								for(Card card : cardList) {
									data.add(card.getId());
								}
								return data;
							}
						}
					}
					for(List<Card> cardList : map.get(cardType)) {
						if(ZTDoudizhuRule.isOvercomePrev(cardList, cardType, lastPai, cardType)) {
							int shouNum = getShouShuNum(allPai);
							if(shouNum <= 2) {
								for(Card card : cardList) {
									data.add(card.getId());
								}
								return data;
							}else {
								boolean isContainAce = isContainAce(lastPai);
								if(!isContainAce) {
									if (lastOutAllPaiSize > 2) {
										for(Card card : cardList) {
											data.add(card.getId());
										}
										return data;
									}
								}
							}
						}
					}
				}
			}
			
		}
		return null;
	}


	// 如果手上剩下的牌刚好组成一个牌型，那就直接打出去
	private static List<Integer> dealSelfLastShouPai(List<Card> myCardList, List<Card> shangJiaList
										, List<Card> xiaJiaList ,boolean isXiaJiaEnemy, boolean isOwner) {
		List<Integer> data = new ArrayList<Integer>();
		CardType cardType = ZTDoudizhuRule.getCardType(myCardList);
		Map<CardType, List<List<Card>>> map = ZTDoudizhuRule.arrangePai(myCardList);
		if(map.size() == 2 ) { //一个炸弹与两个单的情况
			if(map.containsKey(CardType.ZHA_DAN) && map.containsKey(CardType.DAN)) {
				if(map.get(CardType.DAN).size() == 2 && map.get(CardType.ZHA_DAN).size() == 1) {
					boolean isMax = true;
					Map<CardType, List<List<Card>>> shangJiaMap = ZTDoudizhuRule.arrangePai(shangJiaList);
					Map<CardType, List<List<Card>>> xiaJiaMap = ZTDoudizhuRule.arrangePai(xiaJiaList);
					if(isOwner) {
						if(shangJiaMap.containsKey(CardType.ZHA_DAN)) {
							for(List<Card> zhaList : shangJiaMap.get(CardType.ZHA_DAN)) {
								if(map.get(CardType.ZHA_DAN).get(0).get(0).getGrade() < zhaList.get(0).getGrade()) {
									isMax = false;
								}
							}
						}
						if(xiaJiaMap.containsKey(CardType.ZHA_DAN)) {
							for(List<Card> zhaList : xiaJiaMap.get(CardType.ZHA_DAN)) {
								if(map.get(CardType.ZHA_DAN).get(0).get(0).getGrade() < zhaList.get(0).getGrade()) {
									isMax = false;
								}
							}
						}
					}else {
						if(isXiaJiaEnemy) {
							if(xiaJiaMap.containsKey(CardType.ZHA_DAN)) {
								for(List<Card> zhaList : xiaJiaMap.get(CardType.ZHA_DAN)) {
									if(map.get(CardType.ZHA_DAN).get(0).get(0).getGrade() < zhaList.get(0).getGrade()) {
										isMax = false;
									}
								}
							}
						}else {
							if(shangJiaMap.containsKey(CardType.ZHA_DAN)) {
								for(List<Card> zhaList : shangJiaMap.get(CardType.ZHA_DAN)) {
									if(map.get(CardType.ZHA_DAN).get(0).get(0).getGrade() < zhaList.get(0).getGrade()) {
										isMax = false;
									}
								}
							}
						}
					}
					if(isMax) {
						for(Card card : map.get(CardType.DAN).get(0)) {
							data.add(card.getId());
						}
						return data;
					}
				}
			}
		}
		if (cardType != null) {
			for (Card card : myCardList) {
				data.add(card.getId());
			}
			return data;
		}
		return null;
	}

	/***
	 * 作弊的模拟出牌情况
	 * 
	 * @param cards
	 * @return
	 */
	private static List<Integer> getCheatAnalogySelfOutPai(
			List<Card> myCardList, List<Card> shangJiaCardList,
			List<Card> xiaJiaCardList, boolean isOwner, boolean isXiaJiaEnemy) {
		Stack<List<Card>> stack = new Stack<List<Card>>();
		List<Integer> data = new ArrayList<Integer>();
		boolean flag = getAnalogySelfOutPai(myCardList, shangJiaCardList,
				xiaJiaCardList, stack, isOwner, isXiaJiaEnemy);
		if (flag) {
			if (stack.size() > 0) {
				List<Card> cardList = new ArrayList<Card>();
				
				//处理炸弹先出的情况
				for(int i = 0; i < stack.size(); i++) {
					CardType cardType = ZTDoudizhuRule.getCardType(stack.get(i));
					if(cardType == CardType.ZHA_DAN || cardType == CardType.WANG_ZHA) {
						if(i < stack.size() - 2) { 
							continue;
						}
					}
					cardList = stack.get(i);
					for (Card card : cardList) {
						data.add(card.getId());
					}
					return data;
				}
				
				
				
				//处理大小王分开出情况
				cardList = stack.get(0);
				if(cardList.size() == 1 && (cardList.get(0).getGrade() == 54 || cardList.get(0).getGrade() == 53)) { 
					if(stack.size() > 1) {
						List<Card> list = new ArrayList<Card>();
						list = stack.get(1);
						if(list.size() == 1 && (cardList.get(0).getGrade() == 54 || cardList.get(0).getGrade() == 53)) {
							cardList.addAll(list);
							ZTDoudizhuRule.sortCards(cardList);
						}
					}
				}
				
				for (Card card : cardList) {
					data.add(card.getId());
				}
				return data;
			}
		}
		return null;
	}

	/***
	 * 出牌作弊下搜索合适的牌
	 * 
	 * @param cards
	 * @return
	 */

	private static List<Integer> getCheatSuitableSelfOutPai(
			List<Card> myCardList, List<Card> shangJiaCardList,
			List<Card> xiaJiaCardList, boolean isXiaJiaEnemy, boolean isLast, boolean isOwner) {
		List<Integer> data = new ArrayList<Integer>();
		Map<CardType, List<List<Card>>> myPaiMap = ZTDoudizhuRule
				.arrangePai(myCardList);
		//最小的散牌数
		int danMinNum = -1;
		//牌的长度
		int paiSize = 0;
		List<Card> outPai = new ArrayList<Card>();
		
		
		for (Map.Entry<CardType, List<List<Card>>> entry : myPaiMap.entrySet()) {
			CardType cardType = entry.getKey();
			if (cardType == CardType.DAN || cardType == CardType.WANG_ZHA) {
				continue;
			}
			for (List<Card> cardList : entry.getValue()) {
				//是否拆炸弹
				boolean isBreakZhaDan = false;
				if (myPaiMap.containsKey(CardType.ZHA_DAN)) {
					List<List<Card>> zhaDanList = myPaiMap.get(CardType.ZHA_DAN);
					isBreakZhaDan = getBreakPaiNumber(cardList, zhaDanList) > 0;
				}
				if(myPaiMap.containsKey(CardType.WANG_ZHA)) {
					isBreakZhaDan = getBreakPaiNumber(cardList, myPaiMap.get(CardType.WANG_ZHA)) > 0;
				}
				if(isBreakZhaDan) {
					continue;
				}
				
				if (!isLast) { // 是否最后一次找牌
					if (cardType == CardType.DUI_ZI) {
						boolean isChai = false;
						if(myPaiMap.containsKey(CardType.SAN_BU_DAI)) {
							isChai = getBreakPaiNumber(cardList, myPaiMap.get(CardType.SAN_BU_DAI)) > 0;
						}
						if (isChai) {
							continue;
						}
					}
					if(cardType == CardType.ZHA_DAN) {
						continue;
					}
					if (isContainsBigCard(cardList)) {
						continue;
					}
				}

				if (cardType == CardType.SAN_BU_DAI
						|| cardType == CardType.FEI_JI) {
					List<Card> pingList = getPingJieFeiJiOrSanDaiYiOrSiDaiErPai(
							cardType, cardList, myPaiMap);
					if (pingList == null) {
						continue;
					}
					cardList = pingList;
				}
				
				//地主下家
				if(!isXiaJiaEnemy) {
					if(isAnalogyOutPaiCanWin(xiaJiaCardList, myCardList, shangJiaCardList, cardList, isOwner, isXiaJiaEnemy)) {
						for(Card card : cardList) {
							data.add(card.getId());
						}
						return data;
					}
				}else { //地主上家 地主
					if(isAnalogyOutPaiCanWin(xiaJiaCardList, myCardList, shangJiaCardList, cardList, isOwner, isXiaJiaEnemy)) {
						continue;
					}
				}


				int danNum = danPaiNumAfterOutPai(myCardList, cardList);
				
				if (danMinNum == -1) {
					danMinNum = danNum;
					outPai = cardList;
					paiSize = cardList.size();
				} else {
					//散牌数更少
					if (danNum < danMinNum) {
						danMinNum = danNum;
						outPai = cardList;
						paiSize = cardList.size();
					}else if(danNum == danMinNum) {//散牌数相同
						if(cardList.size() > paiSize) {
							outPai = cardList;
							paiSize = cardList.size();
						}
					}
				}
			}
		}
		if (danMinNum != -1 && outPai.size() > 0) {
			for (Card card : outPai) {
				data.add(card.getId());
			}
			return data;
		}
		return null;
	}


	/***
	 * 作弊的最后一次找牌
	 * 
	 * @param cards
	 * @return
	 */
	private static List<Integer> getCheatLastFindSelfOutPai() {

		return null;
	}

	/***
	 * 作弊的模拟跟牌情况
	 * 
	 * @param cards
	 * @return
	 */
	private static List<Integer> getCheatAnalogyOutPai(List<Card> myCardList,
			List<Card> shangJiaCardList, List<Card> xiaJiaCardList,
			List<Card> lastCard, boolean isOwner, boolean isXiaJiaEnemy) {
		Stack<List<Card>> stack = new Stack<List<Card>>();
		List<Integer> data = new ArrayList<Integer>();
		boolean flag = getAnalogyOutPai(myCardList, shangJiaCardList,
				xiaJiaCardList, stack, lastCard, isOwner, isXiaJiaEnemy);
		if (flag) {
			if (stack.size() > 0) {
				List<Card> cardList = new ArrayList<Card>();
				cardList = stack.get(0);
				ZTDoudizhuRule.sortCards(cardList);
				for (Card card : cardList) {
					data.add(card.getId());
				}
				return data;
			}
		}
		return null;
	}

	/***
	 * 作弊的合适跟牌情况
	 * 
	 * @param cards
	 * @return
	 */
	private static List<Integer> getCheatSuitableOutPai(List<Card> myCard,
			List<Card> lastCard, List<Card> lastOutPaiAllPai, List<Card> xiaJiaPaiList, int relate, boolean isXiaJiaIsEnemy) {

		Map<CardType, List<List<Card>>> map = ZTDoudizhuRule.arrangePai(myCard);
		Map<Integer, List<List<Card>>> canOutMap = getCheatSuitableOutPai(
				myCard, lastCard, map);
		List<Integer> data = new ArrayList<Integer>();
		List<List<Card>> canOutList = analysisPai(canOutMap, lastCard,
				lastOutPaiAllPai);
		CardType cardType = ZTDoudizhuRule.getCardType(lastCard);
		if (canOutList != null) {
			if(isXiaJiaIsEnemy) { //下家是敌人
				if(xiaJiaPaiList.size() == 1) { //下家剩下一张牌
					if(canOutList.get(0).size() == 1) { //跟单牌的时候
						for(Card card : canOutList.get(canOutList.size() - 1)) {
							data.add(card.getId());
						}
						return data;
					}
				}
				
				if(xiaJiaPaiList.size() == 2) { //下家剩下两张牌
					if(canOutList.get(0).size() == 2) { //跟对子的时候
						for(Card card : canOutList.get(canOutList.size() - 1)) {
							data.add(card.getId());
						}
						return data;
					}
				}
			}
			for (Card card : canOutList.get(0)) {
				data.add(card.getId());
			}
			return data;
		} 
		return null;
	}

	// 记下散牌数增加2以下能出的手牌数
	private static Map<Integer, List<List<Card>>> getCheatSuitableOutPai(
			List<Card> allPai, List<Card> lastPai,
			Map<CardType, List<List<Card>>> map) {
		// List<List<Card>> canOutPai = new ArrayList<List<Card>>();
		Map<Integer, List<List<Card>>> canOutMap = new HashMap<Integer, List<List<Card>>>();
		List<Card> copyMyCard = new ArrayList<Card>();
		copyMyCard.addAll(allPai);
		
		int danNum = 0;
		if (map.containsKey(CardType.DAN)) {
			danNum = map.get(CardType.DAN).size();
		}
		
		int allPaiSize = allPai.size();
		int size = lastPai.size();
		CardType cardType = ZTDoudizhuRule.getCardType(lastPai);
		
		for (int i = 0; i <= allPaiSize - size; i++) {
			List<Card> sub = allPai.subList(i, i + size);
			CardType myType = ZTDoudizhuRule.getCardType(sub);
			
			boolean zha = (myType == CardType.ZHA_DAN || myType == CardType.WANG_ZHA);
			
			if (zha && cardType != CardType.ZHA_DAN) {
				continue;
			}
			boolean flag = ZTDoudizhuRule.isOvercomePrev(sub, myType, lastPai,
						cardType);
			if(flag) {
				if(cardType == CardType.FEI_JI || cardType == CardType.SAN_DAI_YI) {
					List<Card> cardList = getFeiJiOrSanDaiYIOrSiDaiEr(cardType, sub);
					sub = getPingJieFeiJiOrSanDaiYiOrSiDaiErPai(cardType, cardList, map);
				}
				
				if(sub != null) {
					//是否拆炸弹
					boolean isBreakZhaDan = false;
					if (map.containsKey(CardType.ZHA_DAN)) {
						List<List<Card>> zhaDanList = map.get(CardType.ZHA_DAN);
						isBreakZhaDan = getBreakPaiNumber(sub, zhaDanList) > 0;
					}
					if(isBreakZhaDan) {
						continue;
					}
					
					if(sub != null) {
						int newDanNum = danPaiNumAfterOutPai(copyMyCard, sub);
						int num = newDanNum - danNum;
						if(cardType == CardType.DAN || cardType == CardType.DUI_ZI) {
							if(num > 0) {
								continue;
							}
						}else {
							if(num > 2) {
								continue;
							}
						}
						if (canOutMap.get(newDanNum) == null) {
							List<List<Card>> canOutList = new ArrayList<List<Card>>();
							canOutList.add(sub);
							canOutMap.put(newDanNum, canOutList);
						} else {
							List<List<Card>> canoutList = canOutMap
									.get(newDanNum);
							canoutList.add(sub);
							canOutMap.put(newDanNum, canoutList);
						}
					}
				}
				
			}
			
		}
		return canOutMap;
	}

	// 得到飞机或三带一或四带二中三张相同的牌
	private static List<Card> getFeiJiOrSanDaiYIOrSiDaiEr(CardType cardType,
			List<Card> sub) {
		List<Card> cardList = new ArrayList<Card>();
		if (cardType == CardType.SAN_DAI_YI || cardType == CardType.FEI_JI) {
			for (int i = 0; i < sub.size() - 2; i++) {
				if (sub.get(i).getGrade() == sub.get(i + 1).getGrade()
						&& sub.get(i).getGrade() == sub.get(i + 2).getGrade()) {
					cardList.add(sub.get(i));
					cardList.add(sub.get(i + 1));
					cardList.add(sub.get(i + 2));
					i = i + 2;
				}
			}
		}

		if (cardType == CardType.SI_DAI_ER) {
			// TODO
		}
		return cardList;
	}

	// public static List<Card> compareFeiJiOrSanDaiYiOrSiDaiErPaiAgain(CardType
	// cardType, List<Card> sub,
	// Map<CardType, List<List<Card>>> map) {
	//
	// List<Card> cardList = new ArrayList<Card>();
	// List<Card> pingList = new ArrayList<Card>();
	// if(cardType == CardType.SAN_BU_DAI || cardType == CardType.FEI_JI) {
	// for(int i = 0 ; i < sub.size()-2; i++) {
	// if(sub.get(i) == sub.get(i+1) && sub.get(i) == sub.get(i+2)) {
	// cardList.add(sub.get(i));
	// cardList.add(sub.get(i+1));
	// cardList.add(sub.get(i+2));
	// i = i + 2;
	// }
	// }
	// int needDanNum = cardList.size() / 3;
	// int fitNum = 0;
	// if(map.containsKey(CardType.DAN)) {
	// for(List<Card> list : map.get(CardType.DAN)) {
	// boolean isPaiLessThanEr = isPaiLessThanEr(list);
	// if(isPaiLessThanEr) {
	// cardList.addAll(list);
	// fitNum++;
	// }
	// if(fitNum == needDanNum) {
	// pingList.addAll(cardList);
	// return pingList;
	// }
	// }
	// }else if(map.containsKey(CardType.DUI_ZI)) {
	// for(List<Card> list : map.get(CardType.DUI_ZI)) {
	// boolean isPaiLessThanEr = isPaiLessThanEr(list);
	// if(isPaiLessThanEr) {
	// for(Card card : list) {
	// cardList.add(card);
	// fitNum++;
	// }
	// if(fitNum == needDanNum) {
	// pingList.addAll(cardList);
	// return pingList;
	// }
	// }
	// }
	// }
	// }
	// if(cardType == CardType.SI_DAI_ER) {
	// //tool
	// }
	// return null;
	// }

	// 得到飞机或三带一或四带二拼接后的牌
	private static List<Card> getPingJieFeiJiOrSanDaiYiOrSiDaiErPai(
			CardType cardType, List<Card> cardList,
			Map<CardType, List<List<Card>>> map) {
		List<Card> pingList = new ArrayList<Card>();
		if (cardType == CardType.SAN_BU_DAI || cardType == CardType.FEI_JI || cardType == CardType.SAN_DAI_YI) {
			int needDanNum = cardList.size() / 3;
			int fitNum = 0;
			if (map.containsKey(CardType.DAN)) { //带单牌
				for (List<Card> list : map.get(CardType.DAN)) {
					boolean isPaiLessThanEr = isPaiLessThanEr(list);
					if (isPaiLessThanEr) {
						cardList.addAll(list);
						fitNum++;
					}
					if (fitNum == needDanNum) {
						pingList.addAll(cardList);
						return pingList;
					}
				}
			}
			if (map.containsKey(CardType.DUI_ZI)) { //拆对子
				for (List<Card> list : map.get(CardType.DUI_ZI)) {
					boolean isPaiLessThanEr = isPaiLessThanEr(list);
					if (isPaiLessThanEr) {
						for (Card card : list) {
							cardList.add(card);
							fitNum++;
							if (fitNum == needDanNum) {
								pingList.addAll(cardList);
								return pingList;
							}
						}
					}
				}
			}
			if (map.containsKey(CardType.SHUN_ZI)) { //拆顺子
				for(List<Card> list : map.get(CardType.SHUN_ZI)) {
					for(Card card : list) {
						cardList.add(card);
						fitNum++;
						if (fitNum == needDanNum) {
							pingList.addAll(cardList);
							return pingList;
						}
					}
				}
			}
		}
		if (cardType == CardType.SI_DAI_ER) {
			// tool
		}
		return null;
	}

	private static List<List<Card>> analysisPai(
			Map<Integer, List<List<Card>>> map, List<Card> lastPai,
			List<Card> lastOutPaiAllPai) {

		if (map.size() == 0) {
			return null;
		}
		int danMinNum = getMinKey(map);
		List<List<Card>> canOutPaiList = new ArrayList<List<Card>>();
		int lastPaiSize = lastPai.size();
		Card card = new Card(13);
		boolean islessThanK = lastPai.get(lastPaiSize - 1).getGrade() < card
				.getGrade();
		boolean isShangJiaPaiSizeIsOverFive = lastOutPaiAllPai.size() > 5;
		boolean isTheMaxPai = isTheMaxPai(lastOutPaiAllPai,
				lastPai.get(lastPaiSize - 1));
		boolean flag = islessThanK && isShangJiaPaiSizeIsOverFive
				&& !isTheMaxPai;
		for (List<Card> cardList : map.get(danMinNum)) {
			int size = cardList.size();
			if (flag) {
				Card card2 = new Card(2);
				if (cardList.get(size - 1).getGrade() == card2.getGrade()) {
					continue;
				}
			}
			canOutPaiList.add(cardList);
		}

		if (canOutPaiList.size() > 0) {
			return canOutPaiList;
		} else {
			map.remove(danMinNum);
			return analysisPai(map, lastPai, lastOutPaiAllPai);
		}
	}

	private static boolean isTheMaxPai(List<Card> allPai, Card card) {
		for (Card c : allPai) {
			if (c.getGrade() > card.getGrade()) {
				return false;
			}
		}
		return true;
	}

	private static Integer getMinKey(Map<Integer, List<List<Card>>> map) {
		boolean flag = false;
		int danMinNum = 0;
		for (Map.Entry<Integer, List<List<Card>>> entry : map.entrySet()) {
			if (!flag) {
				danMinNum = entry.getKey();
				flag = true;
			} else {
				if (danMinNum > entry.getKey()) {
					danMinNum = entry.getKey();
				}
			}
		}
		return danMinNum;
	}

	// 包含大牌
	private static boolean isContainsBigCard(List<Card> cardList) {
		// tool
		Card card = new Card(1);
		int size = cardList.size();
		if(cardList.get(size - 1).getGrade() >= card.getGrade()) {
			return true;
		}
		return false;
	}


	/***
	 * 出牌后的散牌数量
	 * 
	 * @param cards
	 * @return
	 */
	private static int danPaiNumAfterOutPai(List<Card> myAllPai,
			List<Card> outPai) {
		List<Card> myCopyCard = new ArrayList<Card>();
		myCopyCard.addAll(myAllPai);
		removeMyPaiCard(myCopyCard, outPai);
		Map<CardType, List<List<Card>>> map = ZTDoudizhuRule
				.arrangePai(myCopyCard);
		if (map.containsKey(CardType.DAN)) {
			return map.get(CardType.DAN).size();
		}
		return 0;
	}

	/***
	 * 模拟跟牌是否能赢
	 * 
	 * @param cards
	 * @return
	 */
	private static boolean isAnalogyOutPaiCanWin(List<Card> myCardList,
			List<Card> shangJiaCardList, List<Card> xiaJiaCardList,
			List<Card> lastCard, boolean isOwner, boolean isEnemy) {

		List<Card> myCardCopyList = new ArrayList<Card>();
		myCardCopyList.addAll(myCardList);
		removeMyPaiCard(myCardCopyList, lastCard);
		Stack<List<Card>> stack = new Stack<List<Card>>();
		boolean isCanWin = getAnalogyOutPai(myCardCopyList,
				shangJiaCardList, xiaJiaCardList, stack, lastCard, isOwner, isEnemy);
		if (isCanWin) {
			return true;
		}
		return false;
	}
	
	/***
	 * 模拟跟牌
	 * 
	 * @param cards
	 * @return
	 */
	private static boolean getAnalogyOutPai(List<Card> myCardList,
			List<Card> shangJiaAllPaiList, List<Card> xiaJiaAllPaiList,
			Stack<List<Card>> stack, List<Card> lastPai, boolean isOwner, boolean isXiaJiaEnemy) {
		List<Card> myCardCopyList = new ArrayList<Card>();
		myCardCopyList.addAll(myCardList);
		if (myCardCopyList.size() == 0) {
			return true;
		}
		Map<CardType, List<List<Card>>> myPaiMap = ZTDoudizhuRule
				.arrangePai(myCardCopyList);
		int size = lastPai.size();
		CardType cardType = ZTDoudizhuRule.getCardType(lastPai);
		for (int i = 0; i <= myCardCopyList.size() - size; i++) {
			List<Card> sub = myCardCopyList.subList(i, i + size);
			CardType myType = ZTDoudizhuRule.getCardType(sub);
			boolean isOvercomePrev = ZTDoudizhuRule.isOvercomePrev(sub, myType,
					lastPai, cardType);
			if (isOvercomePrev) {
				if(cardType == CardType.SAN_DAI_YI || cardType == CardType.FEI_JI) {
					List<Card> list = getFeiJiOrSanDaiYIOrSiDaiEr(cardType, sub);
					sub = getPingJieFeiJiOrSanDaiYiOrSiDaiErPai(cardType, list, myPaiMap);
					if(sub == null) {
						continue;
					}
				}
				boolean isXiaJiaExitOverPai = isExitOverPai(sub,
						xiaJiaAllPaiList);
				boolean isShangJiaExitOverPai = isExitOverPai(sub,
						shangJiaAllPaiList);
				if(isOwner) { //地主
					if (!isShangJiaExitOverPai && !isXiaJiaExitOverPai) {
						List<Card> cardList = new ArrayList<Card>();
						cardList.addAll(sub);
						stack.push(cardList);
						removeMyPaiCard(myCardCopyList, sub);
						boolean flag = getAnalogySelfOutPai(myCardCopyList,
								shangJiaAllPaiList, xiaJiaAllPaiList, stack, isOwner, isXiaJiaEnemy);
						if (flag) {
							return true;
						} else {
							myCardCopyList.addAll(stack.pop());
							ZTDoudizhuRule.sortCards(myCardCopyList);
						}
					}
				}else { //不是地主
					if(isXiaJiaEnemy) { //下家是敌人
						if(!isXiaJiaExitOverPai) {
							List<Card> cardList = new ArrayList<Card>();
							cardList.addAll(sub);
							stack.push(cardList);
							removeMyPaiCard(myCardCopyList, sub);
							boolean flag = getAnalogySelfOutPai(myCardCopyList,
									shangJiaAllPaiList, xiaJiaAllPaiList, stack, isOwner, isXiaJiaEnemy);
							if (flag) {
								return true;
							} else {
								myCardCopyList.addAll(stack.pop());
								ZTDoudizhuRule.sortCards(myCardCopyList);
							}
						}
					}else {
						if(!isShangJiaExitOverPai) {
							List<Card> cardList = new ArrayList<Card>();
							cardList.addAll(sub);
							stack.push(cardList);
							removeMyPaiCard(myCardCopyList, sub);
							boolean flag = getAnalogySelfOutPai(myCardCopyList,
									shangJiaAllPaiList, xiaJiaAllPaiList, stack, isOwner, isXiaJiaEnemy);
							if (flag) {
								return true;
							} else {
								myCardCopyList.addAll(stack.pop());
								ZTDoudizhuRule.sortCards(myCardCopyList);
							}
						}
					}
				}
			}
		}
		
		if(myPaiMap.containsKey(CardType.ZHA_DAN) || myPaiMap.containsKey(CardType.WANG_ZHA)) {
			if(myPaiMap.containsKey(CardType.WANG_ZHA)) { //王炸
				List<Card> cardList = myPaiMap.get(CardType.WANG_ZHA).get(0);
				stack.push(cardList);
				removeMyPaiCard(myCardCopyList, cardList);
				boolean flag = getAnalogySelfOutPai(myCardCopyList,
						shangJiaAllPaiList, xiaJiaAllPaiList, stack, isOwner, isXiaJiaEnemy);
				if (flag) {
					return true;
				} else {
					myCardCopyList.addAll(stack.pop());
					ZTDoudizhuRule.sortCards(myCardCopyList);
				}
			}
			
			if(myPaiMap.containsKey(CardType.ZHA_DAN)) {
				for(List<Card> zhaList : myPaiMap.get(CardType.ZHA_DAN)) {
					boolean isOvercomePrev = ZTDoudizhuRule.isOvercomePrev(zhaList, CardType.ZHA_DAN, lastPai, cardType);
					if(isOvercomePrev) {
						boolean isXiaJiaExitOverPai = isExitOverPai(zhaList,
								xiaJiaAllPaiList);
						boolean isShangJiaExitOverPai = isExitOverPai(zhaList,
								shangJiaAllPaiList);
						if(isOwner) { //地主
							if (!isShangJiaExitOverPai && !isXiaJiaExitOverPai) {
								List<Card> cardList = new ArrayList<Card>();
								cardList.addAll(zhaList);
								stack.push(cardList);
								removeMyPaiCard(myCardCopyList, zhaList);
								boolean flag = getAnalogySelfOutPai(myCardCopyList,
										shangJiaAllPaiList, xiaJiaAllPaiList, stack, isOwner, isXiaJiaEnemy);
								if (flag) {
									return true;
								} else {
									myCardCopyList.addAll(stack.pop());
									ZTDoudizhuRule.sortCards(myCardCopyList);
								}
							}
						}else { //不是地主
							if(isXiaJiaEnemy) { //下家是敌人
								if(!isXiaJiaExitOverPai) {
									List<Card> cardList = new ArrayList<Card>();
									cardList.addAll(zhaList);
									stack.push(cardList);
									removeMyPaiCard(myCardCopyList, zhaList);
									boolean flag = getAnalogySelfOutPai(myCardCopyList,
											shangJiaAllPaiList, xiaJiaAllPaiList, stack, isOwner, isXiaJiaEnemy);
									if (flag) {
										return true;
									} else {
										myCardCopyList.addAll(stack.pop());
										ZTDoudizhuRule.sortCards(myCardCopyList);
									}
								}
							}else {
								if(!isShangJiaExitOverPai) {
									List<Card> cardList = new ArrayList<Card>();
									cardList.addAll(zhaList);
									stack.push(cardList);
									removeMyPaiCard(myCardCopyList, zhaList);
									boolean flag = getAnalogySelfOutPai(myCardCopyList,
											shangJiaAllPaiList, xiaJiaAllPaiList, stack, isOwner, isXiaJiaEnemy);
									if (flag) {
										return true;
									} else {
										myCardCopyList.addAll(stack.pop());
										ZTDoudizhuRule.sortCards(myCardCopyList);
									}
								}
							}
						}
					}
				}
			}
			
		}
		return false;
	}
	

	/***
	 * 模拟出牌
	 * 
	 * @param cards
	 * @return
	 */
	private static boolean getAnalogySelfOutPai(List<Card> myCardList,
			List<Card> shangJiaAllPaiList, List<Card> xiaJiaAllPaiList,
			Stack<List<Card>> stack, boolean isOwner, boolean isXiaJiaEnemy) {
		List<Card> myCardCopyList = new ArrayList<Card>();
		myCardCopyList.addAll(myCardList);
		if (myCardCopyList.size() == 0 || ZTDoudizhuRule.getCardType(myCardCopyList) != null) {
			stack.push(myCardCopyList);
			return true;
		}
		Map<CardType, List<List<Card>>> myPaiMap = ZTDoudizhuRule
				.arrangePai(myCardCopyList);
		for (Map.Entry<CardType, List<List<Card>>> entry : myPaiMap.entrySet()) {
			CardType cardType = entry.getKey();

			for (List<Card> cardList : entry.getValue()) {
				if (cardType == CardType.SAN_BU_DAI) { // 三带一
					List<Card> pingList = getPingJieFeiJiOrSanDaiYiOrSiDaiErPai(
							CardType.SAN_BU_DAI, cardList, myPaiMap);
					if (pingList != null) {
						boolean flag = getAnalogySelfOutPai(pingList,
								shangJiaAllPaiList, xiaJiaAllPaiList, stack,
								myCardCopyList, isOwner, isXiaJiaEnemy);
						if (flag) {
							return true;
						}
					} else {
						continue;
					}
				}
				if (cardType == CardType.FEI_JI) { // 飞机

					List<Card> pingList = getPingJieFeiJiOrSanDaiYiOrSiDaiErPai(
							cardType, cardList, myPaiMap);
					if (pingList != null) {
						boolean flag = getAnalogySelfOutPai(pingList,
								shangJiaAllPaiList, xiaJiaAllPaiList, stack,
								myCardCopyList, isOwner, isXiaJiaEnemy);
						if (flag) {
							return true;
						}
					} else {
						continue;
					}
				}

				if (cardType == CardType.SI_DAI_ER) {
					// tool
				}

				boolean flag = getAnalogySelfOutPai(cardList,
						shangJiaAllPaiList, xiaJiaAllPaiList, stack,
						myCardCopyList, isOwner, isXiaJiaEnemy);
				if (flag) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean getAnalogySelfOutPai(List<Card> cardList,
			List<Card> shangJiaAllPaiList, List<Card> xiaJiaAllPaiList,
			Stack<List<Card>> stack, List<Card> myCardCopyList, boolean isOwner, boolean isXiaJiaEnemy) {
		boolean isXiaJiaExitOverPai = isExitOverPai(cardList, xiaJiaAllPaiList);
		boolean isShangJiaExitOverPai = isExitOverPai(cardList,shangJiaAllPaiList);
		if(isOwner) {
			if (!isXiaJiaExitOverPai && !isShangJiaExitOverPai) {
				stack.push(cardList);
				removeMyPaiCard(myCardCopyList, cardList);
				boolean flag = getAnalogySelfOutPai(myCardCopyList,
						shangJiaAllPaiList, xiaJiaAllPaiList, stack, isOwner, isXiaJiaEnemy);
				if (flag) {
					return true;
				} else {
					myCardCopyList.addAll(stack.pop());
					ZTDoudizhuRule.sortCards(myCardCopyList);
				}
			}
		}else {
			if(isXiaJiaEnemy) {
				if (!isXiaJiaExitOverPai) {
					stack.push(cardList);
					removeMyPaiCard(myCardCopyList, cardList);
					boolean flag = getAnalogySelfOutPai(myCardCopyList,
							shangJiaAllPaiList, xiaJiaAllPaiList, stack, isOwner, isXiaJiaEnemy);
					if (flag) {
						return true;
					} else {
						myCardCopyList.addAll(stack.pop());
						ZTDoudizhuRule.sortCards(myCardCopyList);
					}
				}
			}else {
				if (!isShangJiaExitOverPai) {
					stack.push(cardList);
					removeMyPaiCard(myCardCopyList, cardList);
					boolean flag = getAnalogySelfOutPai(myCardCopyList,
							shangJiaAllPaiList, xiaJiaAllPaiList, stack, isOwner, isXiaJiaEnemy);
					if (flag) {
						return true;
					} else {
						myCardCopyList.addAll(stack.pop());
						ZTDoudizhuRule.sortCards(myCardCopyList);
					}
				}
			}
		}
		
		return false;
	}

	private static void removeMyPaiCard(List<Card> myCardList,
			List<Card> needRemoveCardList) {
		Map<Integer, Card> removeCardMap = new HashMap<Integer, Card>();
		for (Card card : needRemoveCardList) {
			removeCardMap.put(card.getId(), card);
		}

		Iterator<Card> it = myCardList.iterator();
		while (it.hasNext()) {
			Card card = it.next();
			if (removeCardMap.containsKey(card.getId())) {
				it.remove();
			}
		}
		ZTDoudizhuRule.sortCards(myCardList);
	}

	private static boolean isExitOverPai(List<Card> outPaiList, List<Card> allPai) {
		CardType cardType = ZTDoudizhuRule.getCardType(outPaiList);
		int size = outPaiList.size();
		for (int i = 0; i <= allPai.size() - size; i++) {
			List<Card> sub = allPai.subList(i, i + size);
			CardType myType = ZTDoudizhuRule.getCardType(sub);
			boolean flag = ZTDoudizhuRule.isOvercomePrev(sub, myType,
					outPaiList, cardType);
			if (flag) {
				return true;
			}
		}
		
		Map<CardType, List<List<Card>>> map = ZTDoudizhuRule.arrangePai(allPai);
		if(map.containsKey(CardType.ZHA_DAN)) {
			for(List<Card> zhaList : map.get(CardType.ZHA_DAN)) {
				if(ZTDoudizhuRule.isOvercomePrev(zhaList, CardType.ZHA_DAN, outPaiList, cardType)) {
					return true;
				}
			}
		}
		if(map.containsKey(CardType.WANG_ZHA)) {
			return true;
		}
		return false;
	}

}
