package com.yaowan.server.game.model.dezhou;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.protobuf.GeneratedMessageLite;
import com.yaowan.core.base.Spring;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.DispatchEvent;
import com.yaowan.model.struct.Game;
import com.yaowan.protobuf.game.DZCardProto;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.model.dezhou.DZHandCard.CardResult;
import com.yaowan.server.game.model.dezhou.DZPlayer.DZPlayerOP;
import com.yaowan.server.game.model.dezhou.DZPlayer.DZPlayerStatus;

/**
 * 德州规则
 *
 * 发牌与下注规则 1.发牌每次从小盲开始。 首次发牌每人底牌两张，自动为小盲下最低值的一半， 大盲下最低值，其他人根据情况选择跟注、加注或弃牌
 * 2.二次发牌， 2.1大盲弃牌，此时立即发牌 2.2任何参与者加注，须等到所有参与者跟注或弃牌。中途若有加注者，须所有参与者跟注至此注为止
 * 
 * 3.三次发牌 所有参与者跟注至最后位加注者相等或弃牌为止 4.四次发牌 规则同三
 * 
 * 分池规则 在所有跟注者与最后加注者相等或弃牌后处理分池
 * 
 * 输赢分配规则 1.每位参与者，只能分配其参与的下注池筹码的分配 2.按【皇家同花顺>同花顺>四条>葫芦>同花>顺子>三条>两对>一对>高牌】来判断等级
 * ，同等级者比大小,赢者获得所有筹码，若多人赢则平分参与池筹码
 * 
 * 德州房
 * 
 * @author YW0941
 *
 */
public class DZRoom {
	// 最大参与人数
	public int MaxPlayerCount = 7;

	private Game game;

	// 自动生成的编号
	private long gid;
	// 房间配置编号
	private int cid;
	// 最低下注至
	private int minBet = 10;
	// 状态
	private DZRoomStatus status = DZRoomStatus.NotPlay;

	// 所有进入此房间的列表
	private DZPlayer[] players;
	// 参加游戏者
	private List<DZPlayer> joinedDzPlayers;

	private List<DZPlayer> wins = new ArrayList<DZPlayer>();
	// 庄家
	private DZPlayer banker;
	// 小盲
	private DZPlayer xiaoMang;
	// 大盲
	private DZPlayer daMang;

	// 下注峰值，让所有玩家下注值跟峰值相等时，进行发牌，直至4轮发牌完毕
	private int betMax;

	// 所发的公共牌
	private List<DZCard> commonCards = new ArrayList<DZCard>();

	// 下一个下注者
	private DZPlayer nextBetPlayer;

	// 筹码池
	private List<Integer> jetonPools = new ArrayList<Integer>();

	// 分割筹码池的分割值
	private List<Integer> splitValues = new ArrayList<Integer>();

	// 当前发牌回合,处理此次发的是什么牌，多少张(底牌， 翻牌，转牌，河牌)
	private int currTimes = 0;

	// 一副牌
	private DZPairCard pairCard;

	// 人数
	private AtomicInteger playerCount = new AtomicInteger(0);

	public DZRoom(Game game, int cid, int maxPlayerCount, int minBet) {
		this.game = game;
		this.gid = game.getRoomId();
		this.cid = cid;
		this.minBet = minBet;
		this.MaxPlayerCount = maxPlayerCount;
		players = new DZPlayer[maxPlayerCount];
	}
	
	public Game getGame() {
		return game;
	}
	
	public DZPlayer getBanker() {
		return banker;
	}

	public long getGid() {
		return gid;
	}

	public int getPlayerCount() {
		return playerCount.get();
	}

	public int getCid() {
		return cid;
	}

	public int getBetMax() {
		return betMax;
	}

	public void setBetMax(int betMax) {
		this.betMax = betMax;
	}

	public int getMinBet() {
		return minBet;
	}

	public void addSplitValue(int splitValue) {
		splitValues.add(splitValue);
	}

	private List<Integer> getSplitValues() {

		Collections.sort(splitValues, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return o1 - o2;
			}
		});
		return splitValues;
	}

	/**
	 * 每次进行分割后，需要清理此分割变量
	 */
	private void cleanSplitValues() {
		splitValues.clear();
	}

	/**
	 * 获取下注玩家
	 * 
	 * @return
	 */
	public DZPlayer getBetPlayer() {
		return nextBetPlayer;
	}

	/**
	 * 通过当前操作者，确定下位操作者
	 * 
	 */
	public void setNextPlayer() {
		DZPlayer currPlayer = nextBetPlayer;
		nextBetPlayer = getDzPlayerByOffset(currPlayer, 1);
	}

	/**
	 * 根据相对于庄家的位置偏移量获取参与者，排除未参加者
	 * 
	 * @param dzPlayer
	 *            相对此人来查找
	 * @param offset
	 * @return
	 */
	private DZPlayer getDzPlayerByOffset(DZPlayer dzPlayer, int offset) {
		int pos = dzPlayer.getPosition();
		if (offset == 0) {
			return banker;
		}
		for (;;) {
			if (pos == MaxPlayerCount) {
				pos = 1;
			} else {
				pos++;
			}
			if (players[pos - 1] == null) {
				continue;
			}
			if (players[pos - 1].getOp() == DZPlayerOP.GiveUp) {
				continue;
			}
			if (players[pos - 1].getStatus() == DZPlayerStatus.Joined) {
				offset--;
				if (offset <= 0) {
					return players[pos - 1];
				}
			}
		}
	}

	public DZPlayer getDaMang() {
		return daMang;
	}

	public void changeBetMax(int maxValue) {
		if (betMax < maxValue) {
			betMax = maxValue;
		}
	}
	
	/**
	 * 4轮牌没发，则进行发牌， 都发完了则进行比牌操作
	 */
	public void fapaiOrCompareCard() {
		currTimes++;
		boolean loop = true;
		while (loop) {
			if (currTimes == 1) {// 第1轮，每人发两张底牌
				for (int i = 0; i < 2; i++) {
					for (DZPlayer player : joinedDzPlayers) {
						player.getHandCard().put(pairCard.sendCard());
					}
				}
				loop = false;
			} else if (currTimes == 2) {// 第2轮，发3张公共牌
				commonCards.add(pairCard.sendCard());
				commonCards.add(pairCard.sendCard());
				commonCards.add(pairCard.sendCard());
			} else if (currTimes == 3 || currTimes == 4) {// 第3,4轮,发1张公共牌
				commonCards.add(pairCard.sendCard());
			} else {// 比牌
				compareCard();
				loop = false;
			}
			if (currTimes > 1) {
				loop = false; //正常情况下每次只发1轮牌
				int giveUpNum = 0; //弃牌的个数
				int allInNum = 0; //allIn的个数
				
				// 发完牌后，清理玩家的操作状态
				for (DZPlayer player : joinedDzPlayers) {
					
					if(player.getOp() == DZPlayerOP.GiveUp){
						giveUpNum++;
					}else if(player.getJeton() == 0){//AllIn
						allInNum++;
					}
					
					if (player.getOp() != DZPlayerOP.GiveUp
							&& player.getOp() != DZPlayerOP.Common) {
						player.setOp(DZPlayerOP.Common);
					}
				}
				if(allInNum == 0 && giveUpNum +1 == joinedDzPlayers.size()){//只剩下最后一个，其他人棋牌
					//TODO 立即判断输赢					
				}
				if(allInNum>0 && allInNum+giveUpNum+1 == joinedDzPlayers.size()){//只剩下一个人能下注，此时把后面的牌都发完
					loop = true; 
				}
			}

			// 重新确定下注者
			nextBetPlayer = getDzPlayerByOffset(banker, 1);
			if (currTimes <= 4) {

				final DZCardProto.GMsg_12051101.Builder builder = DZCardProto.GMsg_12051101
						.newBuilder();
				final DZCardProto.MsgDZCard.Builder cardBuilder = DZCardProto.MsgDZCard
						.newBuilder();
				switch (currTimes) {
				case 1:// 发底牌
					for (DZPlayer dzPlayer : joinedDzPlayers) {
						builder.setRid(dzPlayer.getRid());
						for (DZCard dzCard : dzPlayer.getHandCard().getCards()) {
							cardBuilder.setColor(dzCard.getColor().get());
							cardBuilder.setValue(dzCard.getValue());
							builder.addCards(cardBuilder.build());
							cardBuilder.clear();
						}
						sendMessage(dzPlayer, builder.build());
					}
					break;
				case 2:// 发三张公共牌，需要广播给所有人
					for (DZCard dzCard : commonCards) {
						cardBuilder.setColor(dzCard.getColor().get());
						cardBuilder.setValue(dzCard.getValue());
						builder.addCards(cardBuilder.build());
						cardBuilder.clear();
					}
					sendMessageToAll(builder.build(), null);
					break;
				case 3:
				case 4:
					DZCard dzCard = commonCards.get(commonCards.size() - 1);
					cardBuilder.setColor(dzCard.getColor().get());
					cardBuilder.setValue(dzCard.getValue());
					builder.addCards(cardBuilder.build());
					sendMessageToAll(builder.build(), null);
					break;
				default:
					break;
				}

			}
		}
	}

	/**
	 * 迭代房间所有的人
	 * 
	 * @param processPer
	 */
	public void iteratorAllDzPlayer(ProcessPer processPer) {

		for (int i = 0; i < MaxPlayerCount; i++) {
			if (players[i] != null) {
				processPer.process(players[i]);
			}
		}
	}

	public interface ProcessPer {
		public void process(DZPlayer dzPlayer);
	}

	public void print(ProcessPer print) {
		for (DZPlayer player : joinedDzPlayers) {
			print.process(player);
		}
	}

	/**
	 * 分割筹码池
	 */
	public void splitPool() {

		int count = 0;
		int poolIndex = 0;
		int usedSplit = 0;

		int poolId = jetonPools.size(); // 此次分池开始下标

		DZCardProto.GMsg_12051102.Builder builder = DZCardProto.GMsg_12051102
				.newBuilder();
		DZCardProto.MsgDZSplitPool.Builder poolBuilder = DZCardProto.MsgDZSplitPool
				.newBuilder();

		List<Integer> splits = getSplitValues();
		for (Integer split : splits) {
			count = 0;

			poolIndex = jetonPools.size();
			for (DZPlayer player : joinedDzPlayers) {
				if (player.getUseSplitPoolBetValue() >= split) {
					count++;
					if (split == player.getUseSplitPoolBetValue()) {
						player.cleanUseSplitPoolBetValue(poolIndex);
					}
				}
			}
			int jeton = (split - usedSplit) * count;
			jetonPools.add(jeton);
			usedSplit = split;

			// 广播处理
			poolBuilder.setPoolId(poolId);
			poolBuilder.setJetion(jeton);
			builder.addJetonPools(poolBuilder.build());
			builder.clear();
			poolId++;
		}
		cleanSplitValues();

		// 分池广播
		sendMessageToAll(builder.build(), null);
	}

	public DZPlayer getDzPlayer(long rid) {
		for (DZPlayer dzPlayer : joinedDzPlayers) {
			if (dzPlayer.getRid() == rid) {
				return dzPlayer;
			}
		}
		throw new RuntimeException("获取DZPlayer错误，  rid=" + rid);
	}

	/**
	 * 此参与者是否可以进行下注操作 指此轮之前的玩家没有进行过下的行为，此时该玩家可以按照自己的意愿率先下一定数量的德州豆到游戏中。
	 * 
	 * @param player
	 * @return
	 */
	public boolean canChipIn(DZPlayer player) {
		return canYield(player);
	}

	/**
	 * 是否可以让牌
	 * 
	 * @param player
	 * @return
	 */
	public boolean canYield(DZPlayer player) {
		DZPlayer xiaoMang = getXiaoMang();
		boolean checkXiaoMang = false;
		boolean checkDaMang = false;

		if (xiaoMang == player) {
			checkXiaoMang = true;
		}

		if (!checkXiaoMang && currTimes == 1) {
			// 第一轮发牌后，还没发第二轮牌
			DZPlayer daMang = getDaMang();
			if (daMang == player) {
				checkDaMang = true;
			}
		}
		if (checkXiaoMang || checkDaMang) {// 当前操作者为小盲位，则为新一轮操作开始，需要检测桌面上目前的下注筹码是否相等，
			for (DZPlayer player2 : joinedDzPlayers) {
				if (player2.getStatus() != DZPlayerStatus.Joined) {
					continue;
				}
				if (player2.getBetValue() != betMax) {
					return false;
				}
			}
		}
		if (checkXiaoMang) {
			return true;
		}

		int pos = xiaoMang.getPosition();
		for (;;) {
			if (pos == MaxPlayerCount) {
				pos = 1;
			} else {
				pos++;
			}
			if (players[pos - 1] == null) {
				continue;
			}
			if (players[pos - 1].getStatus() != DZPlayerStatus.Joined) {
				continue;
			}
			if (players[pos - 1] == player) {
				break;
			}
			if (!((players[pos - 1].getOp() == DZPlayerOP.Yield) || (players[pos - 1]
					.getOp() == DZPlayerOP.GiveUp))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 获取小盲
	 * 
	 * @return
	 */
	public DZPlayer getXiaoMang() {
		return xiaoMang;
	}

	/**
	 * 执行下注操作时进行设置
	 * 
	 * @param betMax
	 */
	public void chipInset(int betMax) {
		this.betMax = betMax;
		for (DZPlayer player : joinedDzPlayers) {
			if (player.getStatus() != DZPlayerStatus.Joined) {
				continue;
			}
			if (player.getOp() == DZPlayerOP.GiveUp) {
				continue;
			}
			player.setBetValue(0);
		}
	}

	/**
	 * 检验参与者让牌或弃牌时是否触发发牌
	 * 
	 * @return
	 */
	public boolean canFapaiByYield(DZPlayer dzPlayer) {

		// 大盲点让牌
		if (currTimes == 1) {// 发了第一轮牌
			DZPlayer daMang = getDaMang();
			if (daMang == dzPlayer) {// 当前大盲让牌
				if (currTimes == 1 && dzPlayer.getBetValue() == minBet) {// 而且下注值跟最小下注相等，此时需要发第二轮牌
					return true;
				}
			}
		}
		for (DZPlayer player : joinedDzPlayers) {
			if (player.getStatus() != DZPlayerStatus.Joined) {
				continue;
			}
			if (!((player.getOp() == DZPlayerOP.Yield) || (player.getOp() == DZPlayerOP.GiveUp))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 检验参与者有下注或弃牌是是否触发发牌
	 * 
	 * @return
	 */
	public boolean canFapaiByPutBetOrGiveup(DZPlayer dzPlayer) {

		if (currTimes == 1 && dzPlayer == xiaoMang
				&& dzPlayer.getBetValue() == minBet) {
			// 首轮小盲位不能发牌
			return false;
		}

		for (DZPlayer player : joinedDzPlayers) {
			if (player.getStatus() != DZPlayerStatus.Joined) {
				continue;
			}
			if (player.getOp() == DZPlayerOP.GiveUp) {
				continue;
			}
			if (player.getJeton() == 0) {// 全下
				continue;
			}
			if (player.getBetValue() != betMax) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 比牌，判输赢
	 * 
	 * @return 返回赢了的参与者
	 */
	public List<DZPlayer> compareCard() {
		// 将公共牌放入参与者牌中,进行等级确定
		List<DZPlayer> players = new ArrayList<DZPlayer>();
		for (DZPlayer player : joinedDzPlayers) {
			if (player.getOp() == DZPlayer.DZPlayerOP.GiveUp
					|| player.getStatus() != DZPlayerStatus.Joined) {
				continue;
			}
			players.add(player);
			player.getHandCard().put(commonCards);
			player.getHandCard().compareCard();
		}

		mainPool(players);
		for (int i = 1; i < jetonPools.size(); i++) {
			partPool(i, players);
		}
		
		
		
		Event event = new Event(HandleType.DEZHOU_END, this);
		DispatchEvent.dispacthEvent(event);
		return wins;
	}

	// 获取获胜的参与者
	private List<DZPlayer> getWin(List<DZPlayer> players) {

		final Map<CardResult, List<DZPlayer>> groupMap = new HashMap<CardResult, List<DZPlayer>>();
		// 按等级排序
		Collections.sort(players, new Comparator<DZPlayer>() {
			public int compare(DZPlayer o1, DZPlayer o2) {
				int r = o1.getHandCard().getResult().get()
						- o2.getHandCard().getResult().get();
				List<DZCard> cards1 = o1.getHandCard().getHandCard();
				List<DZCard> cards2 = o2.getHandCard().getHandCard();

				if (r == 0) {// 等级相同，比大小
					DZHandCard.CardResult result = o1.getHandCard().getResult();
					switch (result) {

					case RoyalFlush:// 皇家同花顺>
						break;
					case Flush:// 同花顺>
						r = cards1.get(0).getValue() - cards2.get(0).getValue();
						break;
					case Four:// 四条>
						r = cards1.get(0).getValue() - cards2.get(0).getValue();
						if (r == 0) {
							r = cards1.get(4).getValue()
									- cards2.get(4).getValue();
						}
						break;
					case gourd:// 葫芦>
						r = cards1.get(0).getValue() - cards2.get(0).getValue();
						if (r == 0) {
							r = cards1.get(2).getValue()
									- cards2.get(2).getValue();
							if (r == 0) {
								r = cards1.get(4).getValue()
										- cards2.get(4).getValue();
							}
						}
						break;
					case SameColor:// 同花>
						// 先排序
						Collections.sort(cards1, new Comparator<DZCard>() {
							public int compare(DZCard o1, DZCard o2) {
								return o2.getValue() - o1.getValue();
							}
						});
						Collections.sort(cards2, new Comparator<DZCard>() {
							public int compare(DZCard o1, DZCard o2) {
								return o2.getValue() - o1.getValue();
							}
						});
						r = cards1.get(0).getValue() - cards2.get(0).getValue();
						if (r == 0) {
							r = cards1.get(1).getValue()
									- cards2.get(1).getValue();
							if (r == 0) {
								r = cards1.get(2).getValue()
										- cards2.get(2).getValue();
								if (r == 0) {
									r = cards1.get(3).getValue()
											- cards2.get(3).getValue();
									if (r == 0) {
										r = cards1.get(4).getValue()
												- cards2.get(4).getValue();
									}
								}
							}
						}

						break;
					case ShunZi:// 顺子>
						r = cards1.get(0).getValue() - cards2.get(0).getValue();
						if (r == 0) {
							r = cards1.get(1).getValue()
									- cards2.get(1).getValue();
						}
						break;
					case Three:// 三条>
						r = cards1.get(0).getValue() - cards2.get(0).getValue();
						if (r == 0) {
							r = cards1.get(3).getValue()
									- cards2.get(3).getValue();
							if (r == 0) {
								r = cards1.get(4).getValue()
										- cards2.get(4).getValue();
							}
						}
						break;
					case Two:// 两对>
						r = cards1.get(0).getValue() - cards2.get(0).getValue();
						if (r == 0) {
							r = cards1.get(2).getValue()
									- cards2.get(2).getValue();
							if (r == 0) {
								r = cards1.get(4).getValue()
										- cards2.get(4).getValue();
							}
						}
						break;
					case One:// 一对>
						r = cards1.get(0).getValue() - cards2.get(0).getValue();
						if (r == 0) {
							r = cards1.get(2).getValue()
									- cards2.get(2).getValue();
							if (r == 0) {
								r = cards1.get(3).getValue()
										- cards2.get(3).getValue();
								if (r == 0) {
									r = cards1.get(4).getValue()
											- cards2.get(4).getValue();
								}
							}
						}
						break;
					case Single:// 高牌
						r = cards1.get(0).getValue() - cards2.get(0).getValue();
						if (r == 0) {
							r = cards1.get(1).getValue()
									- cards2.get(1).getValue();
							if (r == 0) {
								r = cards1.get(2).getValue()
										- cards2.get(2).getValue();
								if (r == 0) {
									r = cards1.get(3).getValue()
											- cards2.get(3).getValue();
									if (r == 0) {
										r = cards1.get(4).getValue()
												- cards2.get(4).getValue();
									}
								}
							}
						}
						break;

					default:
						break;
					}
					if (r == 0) {// 此时可能存在多赢玩家
						if (!groupMap.containsKey(result)) {
							groupMap.put(result, new ArrayList<DZPlayer>());
						}
						if (!groupMap.get(result).contains(o1)) {
							groupMap.get(result).add(o1);
						}
						if (!groupMap.get(result).contains(o2)) {
							groupMap.get(result).add(o2);
						}
					}
				}
				return r * (-1);
			}
		});

		List<DZPlayer> wins = null;

		// 先检查是否有多个赢者
		DZPlayer win = players.get(0);
		if (groupMap.containsKey(win.getHandCard().getResult())) {// 可能存在多个赢者
			if (groupMap.get(win.getHandCard().getResult()).contains(win)) {// 一定存在多个赢者了
				wins = groupMap.get(win.getHandCard().getResult());
			}
		}

		if (wins == null) {
			wins = new ArrayList<DZPlayer>();
			wins.add(players.get(0));
		}

		this.wins.removeAll(wins);
		this.wins.addAll(wins);
		return wins;
	}

	// 主池
	private void mainPool(List<DZPlayer> players) {
		List<DZPlayer> wins = getWin(players);
		allocateJeton(0, wins);
	}

	// 分池
	private void partPool(int poolIndex, List<DZPlayer> players) {
		Iterator<DZPlayer> iterator = players.iterator();
		while (iterator.hasNext()) {
			DZPlayer dzPlayer = (DZPlayer) iterator.next();
			if (dzPlayer.getJetonPoolMaxIndex() < poolIndex) {
				iterator.remove();
			}
		}
		List<DZPlayer> wins = getWin(players);
		allocateJeton(poolIndex, wins);
	}

	// 分配赢的筹码
	private void allocateJeton(int poolIndex, List<DZPlayer> wins) {
		try {

			// 广播赢的筹码
			DZCardProto.GMsg_12051103.Builder builder = DZCardProto.GMsg_12051103
					.newBuilder();
			DZCardProto.MsgDZSplitPool.Builder poolBuilder = DZCardProto.MsgDZSplitPool
					.newBuilder();
			DZCardProto.MsgDZHandCard.Builder handCardBuilder = DZCardProto.MsgDZHandCard
					.newBuilder();
			DZCardProto.MsgDZCard.Builder cardBuilder = DZCardProto.MsgDZCard
					.newBuilder();

			int jeton = jetonPools.get(poolIndex) / wins.size();

			for (DZPlayer dzPlayer : wins) {
				dzPlayer.incrementWinJeton(jeton);

				handCardBuilder.setRid(dzPlayer.getRid());
				List<DZCard> cards = dzPlayer.getHandCard().getHandCard();
				handCardBuilder.setType(dzPlayer.getHandCard().getResult()
						.get());
				for (DZCard dzCard : cards) {
					cardBuilder.setColor(dzCard.getColor().get());
					cardBuilder.setValue(dzCard.getValue());
					handCardBuilder.addCards(cardBuilder.build());
					cardBuilder.clear();
				}
				builder.addHandCards(handCardBuilder.build());
				handCardBuilder.clear();
			}

			poolBuilder.setJetion(jeton);
			poolBuilder.setPoolId(poolIndex);

			builder.setPool(poolBuilder.build());
			sendMessageToAll(builder.build(), null);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void setStatus(DZRoomStatus status) {
		this.status = status;
	}

	// 确定庄家,小盲，大盲
	private void fixBankerAndXiaoMangAndDaMang() {
		if (banker != null) {// 此房间已经开局过游戏，则顺时针方向上取下一位
			banker = getDzPlayerByOffset(banker, 1);
			return;
		}
		// 新开始的房间，需要随机取一位为庄家
		Random random = new Random();
		banker = joinedDzPlayers.get(random.nextInt(joinedDzPlayers.size()));
		xiaoMang = getDzPlayerByOffset(banker, 1);
		daMang = getDzPlayerByOffset(banker, 2);
	}

	/**
	 * 开始游戏
	 */
	public void start() {
		if (status == DZRoomStatus.Playing) {
			return;
		}
		if (joinedDzPlayers == null) {
			joinedDzPlayers = new ArrayList<DZPlayer>();
		} else {
			joinedDzPlayers.clear();
		}
		for (DZPlayer player : players) {
			if (player != null) {
				player.reset();
				player.setJoined(true);
				joinedDzPlayers.add(player);
			}
		}
		// 设置房间为游戏中状态
		status = DZRoomStatus.Playing;
		// 确定庄家
		fixBankerAndXiaoMangAndDaMang();
		// 设置下个下注者
		nextBetPlayer = getXiaoMang();
		// 进行重置
		commonCards.clear();
		betMax = 0;
		splitValues.clear();
		jetonPools.clear();
		currTimes = 0;
		wins.clear();

		// 生成一副牌
		pairCard = new DZPairCard();
		
		Event event = new Event(HandleType.DEZHOU_START, this);
		DispatchEvent.dispacthEvent(event);
	}

	/**
	 * 退出房间
	 * 
	 * @param player
	 * @return
	 */
	public boolean quitRoom(DZPlayer player) {
		if (player == null) {
			throw new RuntimeException("对象错误 -- quitRoom");
		}

		if (player.isJoined() && player.getOp() != DZPlayerOP.GiveUp) {
			return false;
		}
		players[player.getPosition() - 1] = null;
		player.setPosition(0);
		// 设置退出房间
		player.setJoined(false);
		playerCount.decrementAndGet();// 人数减1
		return true;
	}

	@Override
	public String toString() {
		return "DZRoom [minBet=" + minBet + ", status=" + status
				+ ",\n players=" + Arrays.toString(players)
				+ ", \njoineDzPlayers=" + joinedDzPlayers + ",\n banker="
				+ banker + ", betMax=" + betMax + ", commonCards="
				+ commonCards + ", nextBetPlayer=" + nextBetPlayer
				+ ", jetonPools=" + jetonPools + ", splitValues=" + splitValues
				+ ", currTimes=" + currTimes + ", \npairCard=" + pairCard + "]";
	}

	// 是否可以安排座位
	public boolean canSeat() {
		return (playerCount.get() < MaxPlayerCount);
	}

	/**
	 * 找个位置坐下
	 * 
	 * @param player
	 */
	public boolean seat(DZPlayer player) {
		if (player == null) {
			throw new RuntimeException("加入的对象错误");
		}
		if (player.getPosition() > 0) {
			if (players[player.getPosition() - 1] != null) {
				throw new RuntimeException("位置安排错误 !!");
			}
			players[player.getPosition() - 1] = player;
			return true;
		}
		int count = playerCount.incrementAndGet();
		if (count <= MaxPlayerCount) {
			for (int i = 0; i < MaxPlayerCount; i++) {
				if (players[i] == null) {
					synchronized (players) {
						if (players[i] == null) {
							players[i] = player;
							player.setPosition(i + 1);
							player.setRoom(this);
							break;
						}
					}
				}
			}
			return true;
		} else {
			playerCount.decrementAndGet();
			return false;
		}
	}

	public DZPlayer[] getPlayers() {
		return players;
	}

	/************************************** <<<<<<发送消息用>>> *************************************/
	private RoleFunction roleFunction;

	private RoleFunction getRoleFunction() {
		if (roleFunction == null) {
			roleFunction = Spring.getBean(RoleFunction.class);
			if (roleFunction == null) {
				throw new RuntimeException("RoleFunction注入操作");
			}
		}
		return roleFunction;
	}

	// 单发
	public void sendMessage(DZPlayer dzPlayer, GeneratedMessageLite msg) {
		getRoleFunction().sendMessageToPlayer(dzPlayer.getRid(), msg);
	}

	// 群发给所有
	public void sendMessageToAll(final GeneratedMessageLite msg,
			final DZPlayer exceptPlayer) {
		iteratorAllDzPlayer(new ProcessPer() {
			@Override
			public void process(DZPlayer dzPlayer) {
				if (dzPlayer == exceptPlayer) {
					return;
				}
				sendMessage(dzPlayer, msg);
			}
		});
	}

	// 群发给所有
	public void sendMessageToAll(final GeneratedMessageLite msg, final long rid) {
		iteratorAllDzPlayer(new ProcessPer() {
			@Override
			public void process(DZPlayer dzPlayer) {
				if (dzPlayer.getRid() == rid) {
					return;
				}
				sendMessage(dzPlayer, msg);
			}
		});
	}

	/**************************************** <<<<<<发送消息用>>> *************************************/

	/**
	 * 房间状态
	 * 
	 * @author YW0941
	 *
	 */
	enum DZRoomStatus {
		// 游戏中
		Playing(1),
		// 非游戏中
		NotPlay(2), ;

		private int status;

		private DZRoomStatus(int status) {
			this.status = status;
		}

		public int get() {
			return status;
		}
	}

}
