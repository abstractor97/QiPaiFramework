/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.FriendRoomPayType;
import com.yaowan.constant.GameRoom;
import com.yaowan.constant.GameStatus;
import com.yaowan.constant.GameType;
import com.yaowan.constant.MoneyEvent;
import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.csv.cache.DealProbabilityCache;
import com.yaowan.csv.cache.ExpCache;
import com.yaowan.csv.cache.MaJiangZhenXiongValueCache;
import com.yaowan.csv.cache.MajiangZhenXiongRoomCache;
import com.yaowan.csv.entity.DealProbabilityCsv;
import com.yaowan.csv.entity.ExpCsv;
import com.yaowan.csv.entity.MaJiangZhenXiongValueCsv;
import com.yaowan.csv.entity.MajiangZhenXiongRoomCsv;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.DispatchEvent;
import com.yaowan.framework.core.thread.ISingleData;
import com.yaowan.framework.core.thread.SingleThreadManager;
import com.yaowan.framework.core.thread.SingleThreadTask;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.MathUtil;
import com.yaowan.framework.util.Probability;
import com.yaowan.framework.util.Slf4jLogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.GameRole;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GBaseMahJong;
import com.yaowan.protobuf.game.GBaseMahJong.GBillsInfo;
import com.yaowan.protobuf.game.GBaseMahJong.GDetailBillsInfo;
import com.yaowan.protobuf.game.GBaseMahJong.GHuSeat;
import com.yaowan.protobuf.game.GBaseMahJong.GMajiangGold;
import com.yaowan.protobuf.game.GBaseMahJong.GMajiangPlayer;
import com.yaowan.protobuf.game.GBaseMahJong.GOpenInfo;
import com.yaowan.protobuf.game.GBaseMahJong.GPaiInfo;
import com.yaowan.protobuf.game.GBaseMahJong.GPaiQiang;
import com.yaowan.protobuf.game.GBaseMahJong.GTingPaiInfo;
import com.yaowan.protobuf.game.GBaseMahJong.OptionsType;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_12020015;
import com.yaowan.protobuf.game.GGame.GGameInfo;
import com.yaowan.protobuf.game.GGame.GGameRole;
import com.yaowan.protobuf.game.GGame.GMsg_12006002;
import com.yaowan.protobuf.game.GGame.GMsg_12006005;
import com.yaowan.protobuf.game.GGame.GMsg_12006008;
import com.yaowan.protobuf.game.GGame.GMsg_12006014;
import com.yaowan.protobuf.game.GGame.PlayerState;
import com.yaowan.protobuf.game.GMahJong.GMsg_12011006;
import com.yaowan.protobuf.game.GMahJong.GMsg_12011007;
import com.yaowan.protobuf.game.GMahJong.GMsg_12011016;
import com.yaowan.protobuf.game.GZXMahJong.GMajiangDingQue;
import com.yaowan.protobuf.game.GZXMahJong.GMsg_12041001;
import com.yaowan.protobuf.game.GZXMahJong.GMsg_12041002;
import com.yaowan.protobuf.game.GZXMahJong.GMsg_12041006;
import com.yaowan.protobuf.game.GZXMahJong.GMsg_12041007;
import com.yaowan.protobuf.game.GZXMahJong.GMsg_12041008;
import com.yaowan.protobuf.game.GZXMahJong.GMsg_12041009;
import com.yaowan.protobuf.game.GZXMahJong.GMsg_12041010;
import com.yaowan.protobuf.game.GZXMahJong.GMsg_12041012;
import com.yaowan.protobuf.game.GZXMahJong.GMsg_12041013;
import com.yaowan.protobuf.game.GZXMahJong.GMsg_12041014;
import com.yaowan.protobuf.game.GZXMahJong.GMsg_12041020;
import com.yaowan.protobuf.game.GZXMahJong.GMsg_12041021;
import com.yaowan.protobuf.game.GZXMahJong.GMsg_12041022;
import com.yaowan.protobuf.game.GZXMahJong.ZhenXiongWinType;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.model.data.entity.FriendRoom;
import com.yaowan.server.game.model.data.entity.Npc;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.dao.RoleBrokeLogDao;
import com.yaowan.server.game.model.log.dao.ZXMajiangLogDao;
import com.yaowan.server.game.model.log.entity.ZXMajiangLog;
import com.yaowan.server.game.model.struct.LotterTaskCreator;
import com.yaowan.server.game.model.struct.LotteryTaskType;
import com.yaowan.server.game.model.struct.MissionType;
import com.yaowan.server.game.model.struct.TaskType;
import com.yaowan.server.game.model.struct.ZTMaJongTable;
import com.yaowan.server.game.model.struct.ZTMajiangRole;
import com.yaowan.server.game.rule.ZTMahJongRule;
import com.yaowan.server.game.rule.ZXMahJongRule;

/**
 * 镇雄麻将
 *
 * @author yangbin
 */
@Component
public class ZXMajiangFunction extends FunctionAdapter {

	@Autowired
	private SingleThreadManager manager;

	@Autowired
	private RoleFunction roleFunction;

	@Autowired
	private RoomFunction roomFunction;

	@Autowired
	private ZXMajiangDataFunction majiangDataFunction;

	@Autowired
	private MajiangZhenXiongRoomCache majiangRoomCache;

	@Autowired
	private MaJiangZhenXiongValueCache maJiangValueCache;

	@Autowired
	private ExpCache expCache;

	@Autowired
	MissionFunction missionFunction;

	@Autowired
	private ZXMajiangLogDao majiangLogDao;

	@Autowired
	private RoomLogFunction roomLogFunction;

	@Autowired
	private DealProbabilityCache maJiangProbabilityCache;
	@Autowired
	private LotterTaskCreator taskCreator;

	@Autowired
	private NPCFunction npcFunction;

	@Autowired
	private FriendRoomFunction friendRoomFunction;

	@Autowired
	private RoleBrokeLogDao roleBrokeLogDao;

	private Map<Long, ZTMaJongTable> tableMap = new ConcurrentHashMap<>();

	private Map<Long, ZTMajiangRole> roleMap = new ConcurrentHashMap<>();

	// 统计房间当日对战数，破产数，抽水数
	private static Map<Integer, AtomicIntegerArray> roomCountMap = new ConcurrentHashMap<>();

	public Map<Long, ZTMaJongTable> getAllTable() {
		return tableMap;
	}

	public void clear(long roomId) {
		ZTMaJongTable table = tableMap.remove(roomId);
		if (table != null) {
			LogUtil.info("table.getmember.size：" + table.getMembers().size());
			// for (ZTMajiangRole role : table.getMembers()) {
			// LogUtil.info("role.getRole().getStatus():"+role.getRole().getStatus());
			// if (role.getRole() != null
			// && role.getRole().getStatus() != PlayerState.PS_EXIT_VALUE) {
			// LogUtil.info("clear把麻将role从缓存里面移除....==============================="+role.getRole().getRole().getRid());
			// removeRoleCache(role.getRole().getRole().getRid());
			// if (role.getRole().getRole().getRid() < 100000) {
			// int i = 0;
			// int j = 0;
			// }
			// }
			//
			// }
			LogUtil.info("clear时的玩家剩余座位:" + table.getRemainMembers());
			for (Integer seat : table.getRemainMembers()) {
				ZTMajiangRole role = table.getMembers().get(seat - 1);
				if (role.getRole() != null
						&& role.getRole().getStatus() == PlayerState.PS_EXIT_VALUE) {
					// LogUtil.info("clear把麻将role从缓存里面移除....==============================="+role.getRole().getRole().getRid());
					removeRoleCache(role.getRole().getRole().getRid());
					if (role.getRole().getRole().getRid() < 100000) {
						int i = 0;
						int j = 0;
					}
				}

			}
		}

	}

	public ZTMaJongTable getTable(Long id) {
		ZTMaJongTable ZTMaJongTable = tableMap.get(id);
		return ZTMaJongTable;
	}

	public ZTMaJongTable getTableByRole(Long id) {
		ZTMajiangRole majiangRole = getRole(id);
		if (majiangRole == null) {
			return null;
		}
		return getTable(majiangRole.getRole().getRoomId());
	}

	public ZTMajiangRole getRole(Long id) {
		ZTMajiangRole majiangRole = roleMap.get(id);
		// ZTMaJongTable jongTable
		// =tableMap.entrySet().iterator().next().getValue();

		return majiangRole;
	}

	public void addRoleCache(ZTMajiangRole role) {
		roleMap.put(role.getRole().getRole().getRid(), role);
	}

	public void removeRoleCache(Long id) {
		roleMap.remove(id);
	}

	public void resetOverTable(Game game) {
		if (game.getStatus() == GameStatus.END_REWARD) {
			game.setStatus(GameStatus.WAIT_READY);
			ZTMaJongTable table = getTable(game.getRoomId());
			table.reset();
		}
	}

	/**
	 * 定时检测所有人准备游戏开始
	 */
	public void checkStart(Game game) {
		boolean isRoleOK = true;
		long serviceTime = System.currentTimeMillis();
		boolean isFriendRoomOk = false;
		FriendRoom friendRoom = friendRoomFunction.getFriendRoom(game
				.getRoomId());
		if (game.isFriendRoom() && friendRoom != null
				&& game.getStatus() == GameStatus.WAIT_READY) {
			if (friendRoom.getStartTime() + TimeUtil.ONE_SECOND * 10 * 1000 <= serviceTime
					&& friendRoom.isStart() == FriendRoomPayType.START) {
				LogUtil.info("好友房开局啦，" + serviceTime);
				isFriendRoomOk = true;
				friendRoom.setStart((byte) 1);
				friendRoom.getPrepareList().clear();
				friendRoomFunction.getFriendRoomMap().put(game.getRoomId(),
						friendRoom);
			} else if (friendRoom.isStart() == FriendRoomPayType.NO_EXIT
					&& friendRoom.getPrepareList().size() == 4) {
				LogUtil.info("好友房开局啦，" + serviceTime);
				isFriendRoomOk = true;
				friendRoom.setStart((byte) 1);
				friendRoom.getPrepareList().clear();
				friendRoomFunction.getFriendRoomMap().put(game.getRoomId(),
						friendRoom);
			}
		} else {

			for (GameRole role : game.getSpriteMap().values()) {
				if (!role.isRobot()) {
					if ("".equals(role.getRole().getPlatform())) {
						role.setStatus(PlayerState.PS_PREPARE_VALUE);
					} else if (role.getStatus() != PlayerState.PS_PREPARE_VALUE) {
						isRoleOK = false;
					}
				} else {
					// 准备时检测
					MajiangZhenXiongRoomCsv majiangRoomCsv = majiangRoomCache
							.getConfig(game.getRoomType());
					boolean flag = false;
					if (majiangRoomCsv.getEnterUpperLimit() == -1) {
						if (role.getRole().getGold() < majiangRoomCsv
								.getEnterLowerLimit()) {
							flag = true;
						}
					} else {
						if (role.getRole().getGold() < majiangRoomCsv
								.getEnterLowerLimit()
								|| role.getRole().getGold() > majiangRoomCsv
										.getEnterUpperLimit()) {
							flag = true;
						}
					}

					if (!flag && role.isRobot()) {
						if (npcFunction.robotByebye(role.getRole().getRid(),
								game.getGameType(), game.getRoomType())) {
							// AI不在时间范围内就要走了
							LogUtil.error("麻将AI时间到，走了");
							flag = true;
						}
					}

					if (flag) {
						LogUtil.info("maj");
						GMsg_12006005.Builder builder = GMsg_12006005
								.newBuilder();
						builder.setCurrentSeat(role.getSeat());
						builder.setRoomId(game.getRoomId());
						roleFunction.sendMessageToPlayers(game.getRoles(),
								builder.build());
						// roomFunction.endGame(game);
						roomFunction.quitRole(game, role.getRole());
						return;
					}
					long time = game.getStartTime();
					if (game.getEndTime() > game.getStartTime()) {
						time = game.getEndTime();
					}
					int dif = (int) (System.currentTimeMillis() - time) / 1000;
					Map<Long, ConcurrentHashMap<Integer, Game>> AIExitOrReadyMap = roomFunction
							.getAIExitOrReadyMap();
					if (!AIExitOrReadyMap.containsKey(role.getRole().getRid())) {
						if (game.getCount() == 0
								&& role.getStatus() != PlayerState.PS_PREPARE_VALUE) {
							if (dif > MathUtil.randomNumber(1, 3)
									&& role.getStatus() != PlayerState.PS_PREPARE_VALUE) {

								role.setStatus(PlayerState.PS_PREPARE_VALUE);

								GMsg_12041001.Builder builder = GMsg_12041001
										.newBuilder();
								builder.setSeat(role.getSeat());
								roleFunction.sendMessageToPlayers(
										game.getRoles(), builder.build());
							}
						}
						if (game.getCount() > 0
								&& role.getStatus() != PlayerState.PS_PREPARE_VALUE) {
							if (dif > MathUtil.randomNumber(3, 5)
									&& role.getStatus() != PlayerState.PS_PREPARE_VALUE) {

								role.setStatus(PlayerState.PS_PREPARE_VALUE);

								GMsg_12041001.Builder builder = GMsg_12041001
										.newBuilder();
								builder.setSeat(role.getSeat());
								roleFunction.sendMessageToPlayers(
										game.getRoles(), builder.build());
							}
						}
					}

					// }

					if (role.getStatus() != PlayerState.PS_PREPARE_VALUE) {
						isRoleOK = false;
					}
				}

			}
			// for (GameRole role : game.getSpriteMap().values()) {}
		}

		if (game.isFriendRoom()) {
			if (isFriendRoomOk) {
				LogUtil.info("好友房准备完毕，开始");
				GMsg_12020015.Builder builder = GMsg_12020015.newBuilder();
				builder.setAllRound(friendRoom.getAllRound());
				builder.setNowRound(friendRoom.getNowRound());
				for (long rid : friendRoom.getSpriteMap().keySet()) {
					roleFunction.sendMessageToPlayer(rid, builder.build());
				}
				isRoleOK = true;
			} else {
				isRoleOK = false;
			}
		}
		if (game.getSpriteMap().size() < 4) {
			isRoleOK = false;
		}
		if (isRoleOK) {
			startTable(game);
		}
	}

	/**
	 * 开始游戏
	 */
	public void startTable(Game game) {
		if (game.getStatus() != GameStatus.WAIT_READY) {
			return;
		}

		ZTMaJongTable table = getTable(game.getRoomId());
		if (table == null || !table.isInited()) {
			return;
		}
		table.getWaiter().clear();
		AtomicIntegerArray roomCountLog = getRoomCountCacheByRoomType(game
				.getRoomType());
		for (Map.Entry<Long, GameRole> entry : game.getSpriteMap().entrySet()) {
			GameRole role = entry.getValue();
			role.setStatus(PlayerState.PS_PREPARE_VALUE);

			GBillsInfo.Builder billsInfo = GBillsInfo.newBuilder();
			billsInfo.setRid(entry.getValue().getRole().getRid());
			billsInfo.setNick(entry.getValue().getRole().getNick());
			billsInfo.setSeat(entry.getValue().getSeat());
			billsInfo.setGold(0);
			billsInfo.setExp(0);
			table.getBills().put(entry.getValue().getSeat(), billsInfo);
//			System.out.println("initTable数量" + table.getMembers().size());

			if (!game.isFriendRoom()) {
				// 扣税
				int tax = majiangRoomCache.getConfig(game.getRoomType())
						.getTaxPerGame();
				int gold = role.getRole().getGold();
				if (gold < tax) {
					tax = gold;
				}

				if (role.isRobot()) {
					role.getRole().setGold(role.getRole().getGold() - tax);
				} else {
					roleFunction.goldSub(role.getRole(), tax,
							MoneyEvent.ZXMAJIANG_TAX, true);
					roomCountLog.addAndGet(2, tax);// 抽水
				}

			}

		}

		if (!game.isFriendRoom()) {
			roomCountLog.incrementAndGet(0);// 对战数
		}

		List<Integer> initAllPai = initAllPai();
		// 洗牌 打乱顺序
		Collections.shuffle(initAllPai);
		table.setPais(initAllPai);

		// 发牌 目前根据概率配 保留随机发牌
		faPai(table);
		// faPaiTest(table);

		// 随机庄家
		int host = MathUtil.randomNumber(1, table.getMembers().size());
		table.setOwner(host);

		// 转骰子
		int point1 = MathUtil.randomNumber(1, 6);
		int point2 = MathUtil.randomNumber(1, 6);
		table.setPoint1(point1);
		table.setPoint2(point2);

		// 镇雄麻将没有坨牌(癞子)
		table.setLaiZiNum(0);

		if (game.isQuiting() || game.getSpriteMap().size() < 4) {
			return;
		}

		game.setStatus(GameStatus.RUNNING);
		game.setCount(game.getCount() + 1);

		// 给Members排序
		Collections.sort(table.getMembers(), new Comparator<ZTMajiangRole>() {

			@Override
			public int compare(ZTMajiangRole o1, ZTMajiangRole o2) {

				return o1.getRole().getSeat() - o2.getRole().getSeat();
			}
		});

		List<Integer> pais = table.getPais();
		for (ZTMajiangRole maJongMember : table.getMembers()) {
			LogUtil.info(maJongMember.getRole().getRole().getNick()
					+ "maJongMember:" + maJongMember.getPai());

			GPaiInfo.Builder info = GPaiInfo.newBuilder();
			info.addAllPai(maJongMember.getPai());

			GOpenInfo.Builder builder2 = GOpenInfo.newBuilder();
			builder2.setDeskRest(pais.size());
			builder2.setDicePoint1(point1);
			builder2.setDicePoint2(point2);
			builder2.setTuoPai(0);
			builder2.setHostSeat(table.getOwner());
			builder2.setHandPai(info);

			GMsg_12041002.Builder builder = GMsg_12041002.newBuilder();
			builder.setOpenInfo(builder2);
			roleFunction.sendMessageToPlayer(maJongMember.getRole().getRole()
					.getRid(), builder.build());

			// 增加活跃度
			GameRole gameRole = maJongMember.getRole();
			if (gameRole != null) {
				Role role = gameRole.getRole();
				if (null != role) {
					role.setBureauCount(role.getBureauCount() + 1);
					role.markToUpdate("bureauCount");
				}
			}
		}
		// table.getCanOptions().put(table.getOwner(),
		// OptionsType.ANNOUNCE_WIN);
		LogUtil.info("startTable数量" + table.getMembers().size());

		LogUtil.info(table.getGame().getRoomId() + " 等待前端播放抓牌动画后显示定缺  等待8秒");
		tableToWait(table, 0, 0, HandleType.MAJIANG_WAIT_SHOW_DING_QUE,
				System.currentTimeMillis() + 8000);
	}

	// 测试时 发想要的手牌
	private void faPaiTest(ZTMaJongTable table) {
		List<Integer> initAllPai = table.getPais();
		Integer remove = null;
		for (int j = 0; j < table.getMembers().size(); j++) {
			ZTMajiangRole maJongMember = table.getMembers().get(j);
			if (table.getMembers().get(j).getRole().isRobot()) {
				continue;
			}

			for (int i = 0; i < 13; i++) {
				remove = getCeShiPai(i);
				initAllPai.remove(remove);
				maJongMember.getPai().add(remove);
			}

//			System.out.println();
//			System.out.println("手牌" + maJongMember.getPai());
//			System.out.println("剩余牌" + initAllPai);
//			System.out.println("剩余牌数量" + initAllPai.size());
//			System.out.println();
		}

		for (int j = 0; j < table.getMembers().size(); j++) {
			ZTMajiangRole maJongMember = table.getMembers().get(j);
			if (!table.getMembers().get(j).getRole().isRobot()) {
				continue;
			}

			for (int i = 0; i < 13; i++) {
				remove = Probability.getRand(initAllPai);
				initAllPai.remove(remove);
				maJongMember.getPai().add(remove);
			}

//			System.out.println();
//			System.out.println("手牌" + maJongMember.getPai());
//			System.out.println("剩余牌" + initAllPai);
//			System.out.println("剩余牌数量" + initAllPai.size());
//			System.out.println();
		}
	}

	/**
	 * 发牌 特定发牌
	 * 
	 * @param table
	 */
	public void faPai(ZTMaJongTable table) {
		int type = 1;
		List<Integer> initAllPai = table.getPais();
		if (type == 0) {
			for (int i = 0; i < 13; i++) {
				for (int j = 0; j < table.getMembers().size(); j++) {
					ZTMajiangRole maJongMember = table.getMembers().get(j);
					Integer remove = initAllPai.remove(0);
					maJongMember.getPai().add(remove);
				}
			}
			/*
			 * for (int j = 0; j < table.getMembers().size(); j++) {
			 * ZTMajiangRole maJongMember = table.getMembers().get(j);
			 * System.out.println(j+"的牌"+maJongMember.getPai()); }
			 */
		} else {
			// 特定发牌
			List<Integer> wangPai = new ArrayList<Integer>();
			List<Integer> tongPai = new ArrayList<Integer>();
			List<Integer> tiaoPai = new ArrayList<Integer>();
			for (Integer integer : initAllPai) {
				if (integer > 10 && integer < 20) {
					wangPai.add(integer);
				} else if (integer > 20 && integer < 30) {
					tongPai.add(integer);
				} else if (integer > 30 && integer < 40) {
					tiaoPai.add(integer);
				}
			}
			// 读出所有牌
//			System.out.println("手牌" + initAllPai);
//			System.out.println(initAllPai);
//			System.out.println("***********");
			// 1代表万 2代表筒 3代表筒
			Integer wang = 1;
			Integer tong = 2;
			Integer tiao = 3;

			int[][] majiang = ZXMahJongRule.conversionType(initAllPai);
			int wangNum = majiang[0][0];// 万数量
			int tongNum = majiang[1][0];// 筒数量
			int tiaoNum = majiang[2][0];// 索数量
			// 一开始的发牌
			HashMap<Integer, Double> hm = new HashMap<Integer, Double>();
			for (int j = 0; j < table.getMembers().size(); j++) {
				ZTMajiangRole maJongMember = table.getMembers().get(j);
				int cardId = 1;
				if (maJongMember.getRole().isRobot()) {
					long rid = maJongMember.getRole().getRole().getRid();
					cardId = npcFunction.getDrawCardId(GameType.MAJIANG, table
							.getGame().getRoomType(), rid);
					LogUtil.info("麻将机器人npcid = " + rid + " 的发牌Id=" + cardId);
				}
				// 万，筒，索概率
				DealProbabilityCsv dealProbabilityCsv = maJiangProbabilityCache
						.getConfig(cardId);
				double wangProbability = dealProbabilityCsv.getProbabilityA();
				double tongProbability = dealProbabilityCsv.getProbabilityB();
				double tiaoProbability = dealProbabilityCsv.getProbabilityC();
				// 分配概率问题
				if (j == 0) {
//					System.out.println("key=" + wang + " value"
//							+ wangProbability);
//					System.out.println("key=" + tong + " value"
//							+ tongProbability);
//					System.out.println("key=" + tiao + " value"
//							+ tiaoProbability);
				} else {
					HashMap<Integer, Integer> hmSort = new HashMap<Integer, Integer>();
					hmSort.put(wang, wangNum);
					hmSort.put(tong, tongNum);
					hmSort.put(tiao, tiaoNum);
					// 使用 Map按value进行排序
					Map<Integer, Integer> bb = ZXMahJongRule
							.sortMapByValue(hmSort);
					int i = 0;
					for (Map.Entry<Integer, Integer> entry : bb.entrySet()) {
						Integer key = entry.getKey();
						// 判断位置
						int gailu = 0;
						switch (i) {
						case 0:
							gailu = dealProbabilityCsv.getProbabilityA();
							break;
						case 1:
							gailu = dealProbabilityCsv.getProbabilityB();
							break;
						case 2:
							gailu = dealProbabilityCsv.getProbabilityC();
							break;
						default:
							break;
						}
						// 判断概率
						switch (key) {
						case 1:
							wangProbability = gailu;
							break;
						case 2:
							tongProbability = gailu;
							break;
						case 3:
							tiaoProbability = gailu;
							break;
						default:
							break;
						}
						i++;
//						System.out.println("key=" + entry.getKey() + " value="
//								+ entry.getValue());
					}
				}
//				System.out.println("wangProbability" + wangProbability
//						+ ",tongProbability" + tongProbability
//						+ ",tiaoProbability" + tiaoProbability + "");
				// 分配概率问题
				for (int i = 0; i < 13; i++) {
					if (wangNum > 0) {
						hm.put(wang, wangProbability);
					}
					if (tongNum > 0) {
						hm.put(tong, tongProbability);
					}
					if (tiaoNum > 0) {
						hm.put(tiao, tiaoProbability);
					}
					Integer t = Probability.getRand(hm, 1);
					System.out.print("随机到" + t + "类型  ");
					Integer remove = null;
					switch (t) {
					case 1:// 随机到万
						remove = Probability.getRand(wangPai);
						wangPai.remove(remove);
						wangNum--;
						break;
					case 2:// 随机到筒
						remove = Probability.getRand(tongPai);
						tongPai.remove(remove);
						tongNum--;
						break;
					case 3:// 随机到条
						remove = Probability.getRand(tiaoPai);
						tiaoPai.remove(remove);
						tiaoNum--;
						break;
					default:
						break;
					}
					initAllPai.remove(remove);
					maJongMember.getPai().add(remove);
				}
//				System.out.println();
//				System.out.println("万的数量剩余" + wangNum + "筒的数量剩余" + tongNum
//						+ "条的数量剩余" + tiaoNum);
//				System.out.println("手牌" + maJongMember.getPai());
//				System.out.println("剩余牌" + initAllPai);
//				System.out.println();
			}
		}

	}

	public Integer moPai(ZTMaJongTable table, ZTMajiangRole ZTMajiangRole) {
		// 测听牌，根据听牌走不同的道路
		List<Integer> tingPai = new ArrayList<Integer>();
		List<Integer> shouPai = ZTMajiangRole.getPai();

		int[][] b = ZXMahJongRule.conversionType(shouPai);
		tingPai = ZXMahJongRule.tingPai(b, ZTMajiangRole.getShowPai());

		List<Integer> pais = table.getPais();
		Integer remove = null;
		DealProbabilityCsv dealProbabilityCsv = maJiangProbabilityCache
				.getConfig(1);
		if (tingPai.size() > 0) {// 听牌
			List<Integer> huPais = new ArrayList<Integer>();
			List<Integer> noHuPais = new ArrayList<Integer>();
			int totalNum = 0;
			// 牌桌剩余牌
			int[][] leftPais = ZXMahJongRule.conversionType(pais);
			// 打印牌牌组的牌
			for (int i = 0; i <= 3; i++) {
				for (int j = 0; j <= 9; j++) {
					System.out.print(leftPais[i][j] + " ");
				}
//				System.out.println();
			}
			// 打印牌
			// 能胡的牌
			for (Integer integer : tingPai) {
				int front = integer / 10 - 1;
				int next = integer % 10;
				int num = leftPais[front][next];
				totalNum += num;
				for (int i = 0; i < num; i++) {
					huPais.add(integer);
				}
			}
			// 能胡的牌
			// 不能胡的牌
			for (Integer integer : pais) {
				if (!huPais.contains(integer)) {
					noHuPais.add(integer);
				}
			}

			// 不能胡的牌
//			System.out.println("数量为" + totalNum + "可胡的有" + huPais);
			// X随机因子
			int x = dealProbabilityCsv.getEathuProbability();
			if (totalNum > 0) {
				double p1 = (x + totalNum) / (double) pais.size();
				if (p1 >= 1) {// 必能胡牌，从胡牌中摸出
					remove = Probability.getRand(huPais);
				} else {// 根据概率得出是否能胡牌
					double p2 = MathUtil.randomDouble(0f, 1f);
					if (p2 < p1) {// 能够胡牌
						remove = Probability.getRand(huPais);
					} else {// 不能胡牌
						remove = Probability.getRand(noHuPais);
					}
				}
			}
			// 没有胡牌，则随机给一个牌
			if (remove == null) {
				remove = Probability.getRand(pais);
			}
		} else {// 没有听（方式一：随机在剩余的牌库中抽一张牌。方式二：根据既定的套路给玩家发牌。）
			double p = MathUtil.randomDouble(0f, 10000f);
			// p<dealProbabilityCsv.getSuijiProbability()
			if (p < dealProbabilityCsv.getSuijiProbability()) {// 方式一
				remove = Probability.getRand(pais);
			} else {// 方式二，有5种摸牌方式
				remove = ZXMahJongRule.moPai(shouPai, pais);
				if (remove == 0) {
					remove = ZXMahJongRule.randomMoPai(shouPai, pais);
					if (remove == 0) {
						remove = Probability.getRand(pais);
					}
				}
			}
		}
		// 防止出现null空指针
		if (remove == null) {
			remove = Probability.getRand(pais);
		}
		table.getPais().remove(remove);
		return remove;
	}

	// 李培光测试
	public static void main(String[] args) {

	}

	// 李培光测试
	public void dealTest2() {
		ZTMaJongTable table = new ZTMaJongTable();
		table.setLaiZiNum(38);
		Role r = new Role();
		r.setNick("12");
		ZTMajiangRole ZTMajiangRole = new ZTMajiangRole(new GameRole(r, 1));
		List<Integer> a = new ArrayList<Integer>();
		a.add(11);
		a.add(11);
		a.add(13);
		a.add(13);
		a.add(14);
		a.add(14);
		a.add(31);
		a.add(31);
		a.add(32);
		a.add(32);
		a.add(33);
		a.add(33);
		a.add(36);
		ZTMajiangRole.setPai(a);

		Map<Integer, List<Integer>> showPai = new HashMap<Integer, List<Integer>>();
		/*
		 * List<Integer> show1=new ArrayList<Integer>(); show1.add(11);
		 * show1.add(11); show1.add(11); showPai.put(11, show1); List<Integer>
		 * show2=new ArrayList<Integer>(); show1.add(15); show1.add(15);
		 * show1.add(15); showPai.put(15, show2);
		 */
		ZTMajiangRole.setShowPai(showPai);

		boolean b = canHu(table, ZTMajiangRole, 37);
//		System.out.println(b);
	}

	/**
	 * 
	 *
	 * @param owner
	 * @return
	 */
	public void enterTable(Game game, GameRole role) {
		ZTMaJongTable table = getTable(game.getRoomId());
		FriendRoom friendRoom = new FriendRoom();
		if (game.isFriendRoom()) {
			friendRoom = friendRoomFunction.getFriendRoom(game.getRoomId());
		}
		if (table != null) {

			if (game.getStatus() == GameStatus.WAIT_READY) {

				GMsg_12006002.Builder builder = GMsg_12006002.newBuilder();

				// 新的桌面对象
				ZTMajiangRole ztMajiangRole = new ZTMajiangRole(role);
				addRoleCache(ztMajiangRole);
				role.setStatus(PlayerState.PS_SEAT_VALUE);

				if (table.getMembers().size() >= role.getSeat()) {
					table.getMembers().set(role.getSeat() - 1, ztMajiangRole);
					table.getRemainMembers().add(role.getSeat());
				} else {
					table.getMembers().add(role.getSeat() - 1, ztMajiangRole);
					table.getRemainMembers().add(role.getSeat());
				}
				LogUtil.info(role.getRole().getNick() + "gameRole.getSeat()"
						+ role.getSeat());

				GGameInfo.Builder info = GGameInfo.newBuilder();
				info.setGameType(game.getGameType());
				info.setRoomId(game.getRoomId());
				info.setRoomType(game.getRoomType());

				GGameRole.Builder temp = null;
				for (Map.Entry<Long, GameRole> entry : game.getSpriteMap()
						.entrySet()) {
					GGameRole.Builder builder3 = GGameRole.newBuilder();
					Role target = entry.getValue().getRole();
					builder3.setRid(entry.getKey());
					builder3.setNick(target.getNick());
					if (game.isFriendRoom()
							&& friendRoom != null
							&& friendRoom.getSpriteMap().containsKey(
									target.getRid())) {

						builder3.setGold(friendRoom.getSpriteMap().get(
								target.getRid()));
					} else {
						builder3.setGold(target.getGold());
					}

					builder3.setHead(target.getHead());
					builder3.setLevel(target.getLevel());
					builder3.setSeat(entry.getValue().getSeat());
					builder3.setAvatarId(entry.getValue().getAvatarId());
					builder3.setSex(target.getSex());
					info.addSprites(builder3);

					if (entry.getKey() == role.getRole().getRid()) {
						temp = builder3;
					}
				}
				builder.setGame(info);

				roleFunction.sendMessageToPlayer(role.getRole().getRid(),
						builder.build());

				// 其他人收到进入房间
				GMsg_12006008.Builder msg = GMsg_12006008.newBuilder();
				GGameRole.Builder builder3 = GGameRole.newBuilder();

				builder3.setRid(role.getRole().getRid());
				builder3.setNick(role.getRole().getNick());
				if (game.isFriendRoom()
						&& friendRoom != null
						&& friendRoom.getSpriteMap().containsKey(
								role.getRole().getRid())) {
					builder3.setGold(friendRoom.getSpriteMap().get(
							role.getRole().getRid()));
				} else {
					builder3.setGold(role.getRole().getGold());
				}

				builder3.setHead(role.getRole().getHead());
				builder3.setLevel(role.getRole().getLevel());
				builder3.setSeat(role.getSeat());
				builder3.setAvatarId(role.getAvatarId());
				builder3.setSex(role.getRole().getSex());
				builder3.setIsOnline(1);
				msg.setRoleInfo(temp);
				List<Long> otherList = new ArrayList<Long>();
				otherList.addAll(game.getRoles());
				otherList.remove(role.getRole().getRid());
				roleFunction.sendMessageToPlayers(otherList, msg.build());
				LogUtil.info("enterTable getGameType" + otherList);

			} else {
				role.setStatus(PlayerState.PS_PLAY_VALUE);
				role.setAuto(false);

				GMsg_12041010.Builder builder = GMsg_12041010.newBuilder();
				if (table.getWaitAction() == null) {
					if (table.getQueueWaitType() == HandleType.MAJIANG_WAIT_SHOW_DING_QUE
							|| table.getQueueWaitType() == HandleType.MAJIANG_WAIT_DING_QUE_TYPE) {
						table.setWaitAction(OptionsType.SET_QUE_TYPE);
					} else {
						table.setWaitAction(OptionsType.DISCARD_TILE);
					}
				}

				// 把已收到的指令座位排除 不再发送给客户端
				List<Integer> YetOperateSeat = new ArrayList<Integer>();
				for (Integer seat : table.getReceiveQueue().keySet()) {
					YetOperateSeat.add(seat);
				}

				// 添加玩家可操作列表
				for (Integer seat : table.getCanOptions().keySet()) {
					if (seat == role.getSeat()) {
						List<OptionsType> optionsTypes = table.getCanOptions()
								.get(seat);
						if (optionsTypes != null && optionsTypes.size() > 0
								&& !YetOperateSeat.contains(seat)) {
							for (OptionsType optionsType : optionsTypes) {
								builder.addOption(optionsType);
							}
						}

					}
				}
				LogUtil.info("掉线重连 给前端发送的 可操作列表:" + builder.getOptionList());
				if (table.getLastPlaySeat() != role.getSeat()) {
					builder.setLastOutpai(table.getLastPai());
				}
				LogUtil.info("table.getMoPai()：" + table.getMoPai()
						+ "table.getLastOutPai():" + table.getLastOutPai());
				if (table.getMoPai() == role.getSeat()
						&& table.getLastOutPai() != role.getSeat()) {
					builder.setMoPai(table.getLastMoPai());
				} else {
					builder.setMoPai(0);
				}
				// builder.setAction(table.getWaitAction());
				LogUtil.info("当前操作玩家：" + table.getLastPlaySeat());
				builder.setCurrentSeat(table.getLastPlaySeat());
				builder.setNextSeat(table.getNextPlaySeat());
				builder.setWaitTime((int) (table.getCoolDownTime() / 1000));

				// 房间信息
				GGameInfo.Builder info = GGameInfo.newBuilder();
				info.setGameType(game.getGameType());
				info.setRoomId(game.getRoomId());
				info.setRoomType(game.getRoomType());

				ZTMajiangRole majiangRole = table.getMembers().get(
						role.getSeat() - 1);

				for (ZTMajiangRole other : table.getMembers()) {
					GMajiangDingQue.Builder dingQueBuilder = GMajiangDingQue
							.newBuilder();
					dingQueBuilder.setSeat(other.getRole().getSeat());
					dingQueBuilder.setQuePaiType(other.getQueType());
					builder.addDingQue(dingQueBuilder.build());

					GMajiangPlayer.Builder player = GMajiangPlayer.newBuilder();

					if (table.getLastOutPai() == other.getRole().getSeat()) {
						player.setPaiOut(2);
					} else if (table.getLastOutPai() != other.getRole()
							.getSeat()
							&& table.getLastMoPai() == other.getRole()
									.getSeat()) {
						player.setPaiOut(1);
					} else if (table.getLastOutPai() == 0
							&& table.getOwner() == other.getRole().getSeat()) {
						player.setPaiOut(1);
					} else {
						player.setPaiOut(0);
					}

					player.setSeat(other.getRole().getSeat());
					// player.setTargetHu(0); // 这个字段没用到
					player.setSeat(other.getRole().getSeat());

					GPaiInfo.Builder destPaiBuilder = GPaiInfo.newBuilder();
					destPaiBuilder.addAllPai(other.getRecyclePai());
					player.setDestPai(destPaiBuilder);

					if (other.getHuType() != 0) {
						// 只发胡的那张牌-第一张牌
						GPaiInfo.Builder huPaiBuilder = GPaiInfo.newBuilder();
						int huPai = other.getPai().get(0).intValue();
						huPaiBuilder.addPai(huPai);
						player.setHuPai(huPaiBuilder);
						player.setPaiNum(other.getPai().size() - 1); // 去掉胡的那张

						if (other.getHuType() == -1) {
							// 自摸
							player.setTargetHu(0);
						} else {
							player.setTargetHu(1);
						}
					} else {
						player.setPaiNum(other.getPai().size());
					}
					// 牌墙
					for (Map.Entry<Integer, List<Integer>> entry : other
							.getShowPai().entrySet()) {
						GPaiQiang.Builder restPaiBuilder = GPaiQiang
								.newBuilder();
						GPaiInfo.Builder paiBuilder = GPaiInfo.newBuilder();
						paiBuilder.addAllPai(entry.getValue());
						restPaiBuilder.setPaiType(entry.getKey());
						restPaiBuilder.setPai(paiBuilder);
						player.addOperatePai(restPaiBuilder);
					}

					builder.addPlayer(player);

					GGameRole.Builder gameRole = GGameRole.newBuilder();
					Role target = other.getRole().getRole();
					gameRole.setRid(target.getRid());
					gameRole.setNick(target.getNick());
					gameRole.setIsOnline(1);
					if (game.isFriendRoom()
							&& friendRoom != null
							&& friendRoom.getSpriteMap().containsKey(
									target.getRid())) {
						if (target.getRid() == friendRoom.getOwner()) {
							if (friendRoom.isOwnerExit() == FriendRoomPayType.NO_EXIT) {
								gameRole.setIsOnline(1);
							} else {
								gameRole.setIsOnline(0);
							}
						}
						if (friendRoom.getLoginOutList() != null
								&& friendRoom.getLoginOutList().contains(
										target.getRid())
								&& target.getRid() != role.getRole().getRid()) {
							gameRole.setIsOnline(0);
						}
						gameRole.setGold(friendRoom.getSpriteMap().get(
								target.getRid()));
						gameRole.setGold(friendRoom.getSpriteMap().get(
								target.getRid()));
					} else {
						gameRole.setGold(target.getGold());
					}

					gameRole.setHead(target.getHead());
					gameRole.setLevel(target.getLevel());
					gameRole.setSeat(other.getRole().getSeat());
					gameRole.setAvatarId(other.getRole().getAvatarId());
					gameRole.setSex(target.getSex());
					info.addSprites(gameRole);
				}
				// 角色信息
				builder.setGame(info);

				// 开局信息
				GOpenInfo.Builder openBuilder = GOpenInfo.newBuilder();

				GPaiInfo.Builder paiBuilder = GPaiInfo.newBuilder();
				paiBuilder.addAllPai(majiangRole.getPai());
				openBuilder.setDeskRest(table.getPais().size());
				openBuilder.setDicePoint1(table.getPoint1());
				openBuilder.setDicePoint2(table.getPoint2());
				openBuilder.setHostSeat(table.getOwner());
				openBuilder.setTuoPai(0);
				openBuilder.setHandPai(paiBuilder);
				builder.setOpenInfo(openBuilder);

				LogUtil.info("majiangRole.getPai()" + majiangRole.getPai());

				roleFunction.sendMessageToPlayer(role.getRole().getRid(),
						builder.build());
				LogUtil.info("getGameType" + game.getGameType() + " ："
						+ table.getWaitAction());
			}

		} else {
			GMsg_12006005.Builder builder = GMsg_12006005.newBuilder();
			builder.setCurrentSeat(0);
			builder.setRoomId(0);
			roleFunction.sendMessageToPlayer(role.getRole().getRid(),
					builder.build());
		}

	}

	/**
	 * 初始化麻将牌局
	 *
	 * @param owner
	 * @return
	 */
	public ZTMaJongTable initTable(Game game) {
		if (game.getStatus() > 0) {
			return null;
		}
		ZTMaJongTable mahJiangTable = new ZTMaJongTable(game);

		MajiangZhenXiongRoomCsv majiangRoomCsv = majiangRoomCache
				.getConfig(game.getRoomType());
		if (game.isFriendRoom()) {
			// 计算番数并且排除重复的番数
			// 好友房设置最大番数
			FriendRoom friendRoom = friendRoomFunction.getFriendRoom(game
					.getRoomId());
			mahJiangTable.setMaxFanShu(friendRoom.getHighestPower());
			LogUtil.info("friendRoom.getHighestPower()："
					+ friendRoom.getHighestPower());
		} else {
			mahJiangTable.setTurnDuration(majiangRoomCsv.getTurnDuration());// 第一轮时间
			mahJiangTable.setTurn2Duration(majiangRoomCsv.getTurn2Duration());// 第二轮时间
			mahJiangTable.setActionDuration(majiangRoomCsv.getActionDuration());// 碰杠胡操作时间
			mahJiangTable.setOtpPunishment(majiangRoomCsv.getOTPunishment());// 最多超时次数
			mahJiangTable.setMaxFanShu(majiangRoomCsv.getMaxFanShu());// 最大番数
		}

		// mahJiangTable.setOwner(owner.getId());

		for (Map.Entry<Long, GameRole> entry : game.getSpriteMap().entrySet()) {
			ZTMajiangRole member = new ZTMajiangRole(entry.getValue());
			mahJiangTable.getMembers().add(member);
			mahJiangTable.getRemainMembers().add(member.getRole().getSeat());
			addRoleCache(member);

			LogUtil.info("=====================game.getSpriteMap().size()===================:"
					+ game.getSpriteMap().size());
			LogUtil.info("inittable的member==========================================:"
					+ member.getRole().getRole().getRid());

			// 修改前的
			/*
			 * MajiangBill value = new MajiangBill();
			 * value.setRid(entry.getValue().getRole().getRid());
			 * value.setSeat(entry.getValue().getSeat()); value.setWinTimes(1);
			 * mahJiangTable.getBills().put(entry.getValue().getSeat(), value);
			 */
			// 修改前的
		}

		// 给Members排序
		Collections.sort(mahJiangTable.getMembers(),
				new Comparator<ZTMajiangRole>() {

					@Override
					public int compare(ZTMajiangRole o1, ZTMajiangRole o2) {

						return o1.getRole().getSeat() - o2.getRole().getSeat();
					}
				});
		tableMap.put(game.getRoomId(), mahJiangTable);
		//
		game.setStatus(GameStatus.WAIT_READY);

		// tableToWait(mahJiangTable, 0, 0, HandleType.MAJIANG_INIT,
		// TimeUtil.time()+15000);
		return mahJiangTable;
	}

	/**
	 * 初始化麻将
	 *
	 * @return
	 */
	public List<Integer> initAllPai() {

		List<Integer> allPai = new LinkedList<>();
		for (int i = 11; i < 40; i++) {
			if (i % 10 == 0) {
				continue;
			}
			for (int j = 0; j < 4; j++) {
				allPai.add(i);
			}
		}
		return allPai;
	}

	public void exitTable(Game game, Long rid) {
		exitTable(game, rid, true);
	}

	public void exitTable(Game game, Long rid, boolean sendMsg) {
		ZTMaJongTable table = getTable(game.getRoomId());
		if (table != null) {
			GameRole gameRole = game.getSpriteMap().get(rid);
			if (gameRole != null) {
				Set<Long> rids = new HashSet<Long>();
				rids.addAll(game.getSpriteMap().keySet());
				game.getSpriteMap().remove(rid);
				LogUtil.info("移除前的座位号:" + table.getRemainMembers());
				table.getRemainMembers().remove((Integer) gameRole.getSeat());
				LogUtil.info("移除后的座位号:" + table.getRemainMembers());

				gameRole.setStatus(PlayerState.PS_EXIT_VALUE);
				game.getRoles().set(gameRole.getSeat() - 1, 0l);
				LogUtil.info("exittable把麻将role从缓存里面移除...." + rid);
				removeRoleCache(rid);
				LogUtil.info(gameRole.getRole().getNick() + "rid" + rid);

				GMsg_12006005.Builder builder = GMsg_12006005.newBuilder();
				builder.setCurrentSeat(gameRole.getSeat());
				roleFunction.sendMessageToPlayers(rids, builder.build());

				if (gameRole.isRobot()) {
					Npc npc = npcFunction.getNpcById(game.getGameType(),
							game.getRoomType(), rid);
					if (npc != null && npc.getStatus() == 1) {
						npcFunction.updateStatus(rid, game.getGameType(),
								game.getRoomType(), 2);
					}
				}
				if (game.getSpriteMap().size() <= 0) {
					tableMap.remove(game.getRoomId());
				} else if (game.getSpriteMap().size() >= 2) {
					roomFunction.insertReadyDeque(game);
				}
			} else {
				Npc npc = npcFunction.getNpcById(game.getGameType(),
						game.getRoomType(), rid);
				if (npc != null && npc.getStatus() == 1) {
					npcFunction.updateStatus(rid, game.getGameType(),
							game.getRoomType(), 2);
				}
			}
		} else {
			Npc npc = npcFunction.getNpcById(game.getGameType(),
					game.getRoomType(), rid);
			if (npc != null && npc.getStatus() == 1) {
				npcFunction.updateStatus(rid, game.getGameType(),
						game.getRoomType(), 2);
			}
		}

	}

	/**
	 * 牌局结算
	 */
	/**
	 * @param table
	 */
	public void endTable(ZTMaJongTable table) {
		table.setQueueWaitType(0);
		table.getGame().setStatus(GameStatus.END);
		LogUtil.info(table.getGame().getRoomId() + " end");
		LogUtil.info("endTable数量" + table.getMembers().size());
		LogUtil.info("endTable数量" + table.getBills().size());

		// 全部玩家推牌（全部牌都变成可见）
		for (ZTMajiangRole role : table.getMembers()) {
			int seat = role.getRole().getSeat();
			GBillsInfo.Builder billsInfo = null;
			if (table.getBills().get(seat) != null) {
				billsInfo = table.getBills().get(seat);
			} else {
				billsInfo = GBillsInfo.newBuilder();
			}

			GPaiInfo.Builder paiinfo = GPaiInfo.newBuilder();
			paiinfo.addAllPai(role.getPai());
			billsInfo.setPai(paiinfo);
			billsInfo.setRid(role.getRole().getRole().getRid());
			billsInfo.setSeat(seat);
			table.getBills().put(seat, billsInfo);
		}

		int aiGold = 0;
		int realPlayerCount = 0;
		GMsg_12041009.Builder endBuilder = GMsg_12041009.newBuilder();

		for (ZTMajiangRole role : table.getMembers()) {
			if (!table.getWinners().contains(role.getRole().getSeat())) {
				ExpCsv csv = expCache.getConfig(role.getRole().getRole()
						.getLevel() + 1);
				if (!table.getGame().isFriendRoom()) {
					roleFunction.expAdd(role.getRole().getRole(),
							csv.getZhenxiongmajiangLoseExp(), true);
				}

			}
			// 任务检测
			missionFunction.checkTaskFinish(role.getRole().getRole().getRid(),
					TaskType.daily_task, MissionType.TIMES, GameType.ZXMAJIANG);
		}
		// 结算增加花猪和无听（判断所有的牌）
		List<Integer> huaZhuMemberList = new ArrayList<Integer>();
		List<Integer> noTingMemberList = new ArrayList<Integer>();
		List<Integer> tingMemberList = new ArrayList<Integer>();
		List<Integer> allPai = new ArrayList<Integer>();
		int que_pai_type = 0;
		for (ZTMajiangRole role : table.getMembers()) {
			if (!table.getWinners().contains(role.getRole().getSeat())) {// 赢家之外
				allPai.clear();
				allPai.addAll(role.getPai());
				que_pai_type = role.getQueType();

				// 优先花猪
				if (ZXMahJongRule.huaZhu(allPai, que_pai_type) == false) {
					huaZhuMemberList.add(role.getRole().getSeat());// 判断花猪
					continue;
				}

				List<Integer> pai = role.getPai();
				int[][] shouPai = ZXMahJongRule.conversionType(pai);
				if (ZXMahJongRule.tingPai(shouPai, role.getShowPai()).size() > 0) {
					tingMemberList.add(role.getRole().getSeat());// 听牌
				} else {
					noTingMemberList.add(role.getRole().getSeat());// 不听
				}

				if (!role.getRole().isRobot()) {
					realPlayerCount++;
				}

				// 连胜中断
				missionFunction.checkTaskFinish(role.getRole().getRole()
						.getRid(), TaskType.daily_task, MissionType.TIMES,
						GameType.ZXMAJIANG);
			}
		}

		if (table.getRemainMembers().size() >= 2) {

		}
		// 赔偿列表
		// key:赔偿玩家 ，value：赔给玩家列表, value(0)压的是赔偿番数
		HashMap<Integer, List<Integer>> peiMemberMap = new HashMap<Integer, List<Integer>>();

		// 花猪赔给不花猪 列表
		List<Integer> noHuaZhuList = new ArrayList<Integer>();
		noHuaZhuList.add(0, ZhenXiongWinType.ZX_HUA_ZHU_VALUE);
		noHuaZhuList.addAll(noTingMemberList);
		noHuaZhuList.addAll(tingMemberList);
		for (Integer huaZhuSeat : huaZhuMemberList) {
			peiMemberMap.put(huaZhuSeat, noHuaZhuList);
		}

		// 不听的赔给听 列表
		tingMemberList.add(0, ZhenXiongWinType.ZX_WU_JIAO_VALUE);
		for (Integer noTingSeat : noTingMemberList) {
			peiMemberMap.put(noTingSeat, tingMemberList);
		}

		// 执行赔偿
		int roomType = table.getGame().getRoomType();
		for (Map.Entry<Integer, List<Integer>> peiEntry : peiMemberMap
				.entrySet()) {
			int srcSeat = peiEntry.getKey();
			List<Integer> dstSeatList = new ArrayList<Integer>();
			dstSeatList.addAll(peiEntry.getValue());
			int huTypeId = dstSeatList.get(0);
			dstSeatList.remove(0);

			if (dstSeatList.isEmpty()) {
				continue;
			}

			ZTMajiangRole srcMJRole = table.getMembers().get(srcSeat - 1);
			GameRole srcGameRole = srcMJRole.getRole();
			Role srcRole = srcGameRole.getRole();

			// 花猪需要赔的番数和金币
			MaJiangZhenXiongValueCsv maJiangValueCsv = maJiangValueCache
					.getConfig(huTypeId);
			int winTimes = maJiangValueCsv.getHuTimes();
			int diZhu;
			if (table.getGame().isFriendRoom()
					&& friendRoomFunction.getFriendRoom(table.getGame()
							.getRoomId()) != null) {
				diZhu = friendRoomFunction.getFriendRoom(
						table.getGame().getRoomId()).getBaseChip();
			} else {
				diZhu = majiangRoomCache.getConfig(
						table.getGame().getRoomType()).getPotOdds();
			}

			// 单个赔付的金币
			int singleLoseGold = (int) Math.pow(2, winTimes) * diZhu;

			// 需要赔付的人数
			int winNum = dstSeatList.size();
			int totalLoseGold = winNum * singleLoseGold;
			if (!table.getGame().isFriendRoom()) {
				if (!srcGameRole.isRobot()
						&& roleFunction.isPoChan(srcRole.getGold(),
								srcRole.getGoldPot(), totalLoseGold)) {
					AtomicIntegerArray log = getRoomCountCacheByRoomType(table
							.getGame().getRoomType());
					log.incrementAndGet(1);// 破产数
					roleBrokeLogDao.insertLog(srcRole.getRid(), table.getGame()
							.getGameType(), table.getGame().getRoomType());
				}
			}
			// 每个赢家可以分到的钱
			int actualLoseGold = 0;
			// 判断钱是否做够 赔给所有的人
			if (!table.getGame().isFriendRoom()) {
				if (totalLoseGold <= srcRole.getGold()) {// 钱够赔给所有的人
					actualLoseGold = singleLoseGold;
				} else {// 钱不够赔给所有的人 则平分
					actualLoseGold = srcRole.getGold() / winNum;
				}
			}
			for (Integer dstSeat : dstSeatList) {
				ZTMajiangRole dstMjRole = table.getMembers().get(dstSeat - 1);
				GameRole dstGameRole = dstMjRole.getRole();
				Role dstRole = dstGameRole.getRole();

				// 赔的人减钱
				if (table.getGame().isFriendRoom()) {
					FriendRoom friendRoom = friendRoomFunction
							.getFriendRoom(table.getGame().getRoomId());
					if (friendRoom != null
							&& friendRoom.getSpriteMap().containsKey(
									srcGameRole.getRole().getRid())) {
						friendRoom.getSpriteMap().put(
								srcGameRole.getRole().getRid(),
								friendRoom.getSpriteMap().get(
										srcGameRole.getRole().getRid())
										- actualLoseGold);
					}
				} else {
					if (srcGameRole.isRobot()) {
						srcRole.setGold(srcRole.getGold() - actualLoseGold);
						// 记录ai和玩家之间的盈亏情况
						if (!dstGameRole.isRobot()) {
							npcFunction.updateGainOrLoss(srcRole.getRid(),
									-actualLoseGold, table.getGame()
											.getGameType(), table.getGame()
											.getRoomType(),
									MoneyEvent.ZXMAJIANG);
						}
					} else {
						roleFunction.goldSub(srcRole, actualLoseGold,
								MoneyEvent.ZXMAJIANG, true);
						// 记录ai和玩家之间的盈亏情况
						if (dstGameRole.isRobot()) {
							npcFunction.updateGainOrLoss(dstRole.getRid(),
									actualLoseGold, table.getGame()
											.getGameType(), table.getGame()
											.getRoomType(),
									MoneyEvent.ZXMAJIANG);
						}
					}
				}

				// billInfo 上面处理推牌已经创建了，保证不会为null
				GBillsInfo.Builder billsInfo = table.getBills().get(srcSeat);
				billsInfo.setGold(billsInfo.getGold() - actualLoseGold);
				GDetailBillsInfo.Builder detailBillsInfo = GDetailBillsInfo
						.newBuilder();
				detailBillsInfo.setNick(dstRole.getNick());// 输显示别人的名称
				detailBillsInfo.setWinTimes(winTimes);
				detailBillsInfo.addWinTypes(huTypeId);
				detailBillsInfo.setGoldDetail(-actualLoseGold);
				billsInfo.addDetailBillsInfo(detailBillsInfo);
				table.getBills().put(srcSeat, billsInfo);

				// 获赔的人加钱
				int actualLottery = 0;
				int actualGold = actualLoseGold;
				if (table.getGame().isFriendRoom()) {
					FriendRoom friendRoom = friendRoomFunction
							.getFriendRoom(table.getGame().getRoomId());
					if (friendRoom != null
							&& friendRoom.getSpriteMap().containsKey(
									dstGameRole.getRole().getRid())) {
						friendRoom.getSpriteMap().put(
								dstGameRole.getRole().getRid(),
								friendRoom.getSpriteMap().get(
										dstGameRole.getRole().getRid())
										+ actualGold);
					}
				} else {
					if (roomType == 1 || roomType == 2) { // 金币房
						if (dstGameRole.isRobot()) {
							dstRole.setGold(dstRole.getGold() + actualGold);
						} else {
							roleFunction.goldAdd(dstRole, actualGold,
									MoneyEvent.ZXMAJIANG, true);
						}
					} else { // 奖券房
						if (!dstGameRole.isRobot()) { // 机器人不用加
							int exchangeLottery = majiangRoomCache.getConfig(
									roomType).getExchangeLottery();
							actualLottery = actualLoseGold * exchangeLottery
									/ 10000;
							if (actualLottery == 0) {
								actualLottery = 1;
							}

							roleFunction.crystalAdd(dstRole, actualLottery,
									MoneyEvent.ZXMAJIANG);
						}

						actualGold = 0; // 置为0，方便后面发消息
					}
				}

				// billsInfo1 上面处理推牌已经创建了，保证不会为null
				GBillsInfo.Builder billsInfo1 = table.getBills().get(dstSeat);
				billsInfo1.setGold(billsInfo1.getGold() + actualGold);
				billsInfo1.setLottery(billsInfo1.getLottery() + actualLottery);
				GDetailBillsInfo.Builder detailBillsInfo1 = GDetailBillsInfo
						.newBuilder();
				detailBillsInfo1.setNick(srcRole.getNick());// 赢显示别人的名称
				detailBillsInfo1.setWinTimes(winTimes);
				detailBillsInfo1.addWinTypes(huTypeId);
				detailBillsInfo1.setGoldDetail(actualGold);
				detailBillsInfo1.setLottery(actualLottery);
				billsInfo1.addDetailBillsInfo(detailBillsInfo1);
				table.getBills().put(dstSeat, billsInfo1);
			}
		}

		// 检测流局
		for (Map.Entry<Integer, GBillsInfo.Builder> entry : table.getBills()
				.entrySet()) {
			GBillsInfo.Builder billsInfo = entry.getValue();
			if (billsInfo.getDetailBillsInfoCount() == 0) {
				GDetailBillsInfo.Builder value = GDetailBillsInfo.newBuilder();
				value.setGoldDetail(0);
				value.setNick("");
				value.setWinTimes(0);
				value.addWinTypes(ZhenXiongWinType.ZX_NO_WINNER_VALUE);
				billsInfo.addDetailBillsInfo(value);
				billsInfo.setGold(0);
			}
			if (billsInfo.getNick() == null) {
				billsInfo.setNick("");
			}
			LogUtil.info("endTable1 胡的黄金" + billsInfo.getNick()
					+ billsInfo.getGold() + "座位" + billsInfo.getSeat());
			endBuilder.addBills(billsInfo);
		}
		// 广播房间所有
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				endBuilder.build());

		table.getGame().setStatus(GameStatus.END_REWARD);
		table.getGame().setEndTime(System.currentTimeMillis());

		// G_T_C 插入日志
		List<Object[]> roleInfoList = new ArrayList<>();
		// 拼接：角色信息流水明细；格式：角色id:番数_金币变化_另外玩家,|
		List<String> roleInfoDetailList = new ArrayList<>();
		majiangLogDao.crateRoleInfoAndDetail(table, roleInfoList,
				roleInfoDetailList);// 处理role info 和 detail 组装到list，为了拼接字符串
		aiGold = majiangLogDao.countAiGold(table);
		String roleInfoDetails = StringUtil.listToString(roleInfoDetailList,
				StringUtil.DELIMITER_BETWEEN_ITEMS);
		LogUtil.debug("role_info_detail=" + roleInfoDetails);
		String roleInfo = StringUtil.listArrayToString(roleInfoList,
				StringUtil.DELIMITER_BETWEEN_ITEMS,
				StringUtil.DELIMITER_INNER_ITEM);
		List<Long> winRoleIdList = majiangLogDao.getWinRoleList(table);
		String winners = StringUtil.listToString(winRoleIdList,
				StringUtil.DELIMITER_BETWEEN_ITEMS);
		LogUtil.debug("role_info=" + roleInfo);
		ZXMajiangLog majiangLog = majiangLogDao.getMajiangLog(table.getGame(),
				roleInfo, roleInfoDetails, winners, aiGold, table.getMembers()
						.size(), realPlayerCount);
		majiangLogDao.addLog(majiangLog);

		for (ZTMajiangRole role : table.getMembers()) {
			if (role.getRole() != null) {
				// G_T_C 处理房间登录日志
				Role role2 = role.getRole().getRole();
				Game game = table.getGame();
				if (role2 != null && game != null && !role.getRole().isRobot()) {
					roomLogFunction.dealRoomRoleLoginLog(game.getGameType(),
							role2, "", 1);
				}
			}
		}
		if (table.getGame().isFriendRoom()) {
			friendRoomFunction.roundEnd(table.getGame().getRoomId());
		}
	}

	/**
	 * 处理超时
	 */
	public void tableToWait(ZTMaJongTable table, int targetSeat, int nextSeat,
			int handleType, long coolDownTime, Long... targetTime) {
		if (targetTime != null && targetTime.length > 0) {
			table.setTargetTime(targetTime[0]);
		}
		table.setLastPlaySeat(targetSeat);
		// LogUtil.info(table+" tabletowait把lastplayseat设置为:"+targetSeat+" 该座位的玩家昵称:"+table.getMembers().get(targetSeat
		// - 1).getRole().getRole().getNick());
		table.setNextSeat(nextSeat);
		table.setQueueWaitType(handleType);
		table.setCoolDownTime(coolDownTime);
	}

	/**
	 * 流程处理
	 */
	public void autoAction() {
		for (Map.Entry<Long, ZTMaJongTable> entry : tableMap.entrySet()) {
			ZTMaJongTable table = entry.getValue();
			if (table.getGame().getStatus() > GameStatus.RUNNING) {
				// table.setQueueWaitType(HandleType.MAJIANG_HU);
				// table.setCoolDownTime(System.currentTimeMillis()+5000);
				continue;
			}
			if (table.getQueueWaitType() == 0) {
				// continue;
				// table.setQueueWaitType(HandleType.MAJIANG_HU);
				// table.setCoolDownTime(System.currentTimeMillis() + 1000);
				/*
				 * for (GameRole gameRole : table.getGame().getSpriteMap()
				 * .values()) { if (!gameRole.isRobot()) { int i = 0; //
				 * table.setQueueWaitType(HandleType.MAJIANG_GET_PAI); // 1000);
				 * } }
				 */
				continue;
			}
			if (table.getGame().isFriendRoom()
					&& (table.getQueueWaitType() != HandleType.MAJIANG_GET_PAI
							&& table.getQueueWaitType() != HandleType.MAJIANG_WAIT && table
							.getQueueWaitType() != HandleType.MAJIANG_WAIT_SHOW_DING_QUE)) {
				if (table.getQueueWaitType() == HandleType.MAJIANG_GET_PAI
						&& table.getCanOptions() != null) {
					continue;
				}
				LogUtil.info("table.getQueueWaitType():"
						+ table.getQueueWaitType());
				continue;
			}
			manager.executeTask(new SingleThreadTask(table) {
				@Override
				public void doTask(ISingleData singleData) {
					ZTMaJongTable table = (ZTMaJongTable) singleData;

					// 还在倒计时中
					long currentTimeMillis = System.currentTimeMillis();
					long coolDownTime = table.getCoolDownTime();
					if (coolDownTime > currentTimeMillis) {
						return;
					}
					int tableState = table.getQueueWaitType();
					try {
						// 处理完变成没事件
						table.setQueueWaitType(0);
						ZTMajiangRole role = null;
						if (table.getLastPlaySeat() != 0) {
							role = table.getMembers().get(
									table.getLastPlaySeat() - 1);
						}
						Event event = new Event(tableState, table, role);
						DispatchEvent.dispacthEvent(event);
					} catch (Exception e) {
						table.setQueueWaitType(tableState);
						table.setCoolDownTime(currentTimeMillis + 5000);
						LogUtil.error("麻将流程处理异常:"
								+ ExceptionUtils.getStackTrace(e));
					}
				}
			});
		}
	}

	/**
	 * 处理出牌
	 *
	 * @param player
	 * @param maJongTable
	 * @param member
	 * @param pai
	 */
	public void dealDisCard(ZTMaJongTable table, Integer pai) {
		table.setQueueWaitType(0);
		int seat = table.getLastPlaySeat();
		ZTMajiangRole role = table.getMembers().get(seat - 1);
		if (!role.getRole().isRobot()) {
			LogUtil.info("！！！麻将玩家" + role.getRole().getRole().getNick()
					+ " 正在出牌！！！");
		}
		if (!role.getPai().contains(pai)) {
			pai = role.getPai().get(role.getPai().size() - 1);
			LogUtil.info("！！！麻将玩家" + role.getRole().getRole().getNick()
					+ " 没有这张牌！！！" + pai);
			pai = ZTMahJongRule.chosePai(role.getPai(), pai,
					table.getLaiZiNum());
		}

		LogUtil.info(table.getGame().getRoomId() + ",seat-" + seat + "出牌 pai:"
				+ pai);

		table.setLastOutPai(seat);
		table.setLastRealSeat(seat);

		role.getPai().remove(pai);
		table.setLastPai(pai);
		role.getRecyclePai().add(pai);

		GPaiInfo.Builder info = GPaiInfo.newBuilder();
		info.addPai(pai);
		GMsg_12041008.Builder builder = GMsg_12041008.newBuilder();
		builder.setNextSeat(table.getNextPlaySeat());
		builder.setOption(OptionsType.DISCARD_TILE);
		builder.setTargetSeat(seat);
		builder.setOperatePai(info);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());
		boolean flag = true;

		boolean onlyRobot = true;

		table.getCanOptions().clear();
		table.getWaiter().clear();
		table.getYetOptions().remove(role.getRole().getSeat());

		for (ZTMajiangRole other : table.getMembers()) {
			if (other != role && other.getHuType() == 0
					&& !table.getWinners().contains(other.getRole().getSeat())) {

				GMsg_12041007.Builder waitBuilder = GMsg_12041007.newBuilder();

				if (table.getPais().size() > 0) {
					if (canGang(table, other)) {
						waitBuilder.addOption(OptionsType.EXPOSED_GANG);
						LogUtil.info(table.getGame().getRoomId()
								+ " EXPOSED_GANG "
								+ other.getRole().getRole().getNick() + "明杠 "
								+ pai);
					}
					if (canPeng(table, other)) {
						int lastQiPai = other.getLastQiPai();
						if (lastQiPai != pai) { // 曾经弃过这张牌，不能再碰
							waitBuilder.addOption(OptionsType.PENG);
							LogUtil.info(table.getGame().getRoomId() + " PENG "
									+ other.getRole().getRole().getNick()
									+ "碰 " + pai);
						}
					}
				}

				if (canHu(table, other, table.getLastPai())) {
					// 弃牌的番数肯定一样的，直接检查番数就行了
					List<Integer> huFanType2 = dealFanType2(other, table);
					int FanNum = dealFanNum(huFanType2, table);
					if (other.getHuFan() < FanNum) {
						waitBuilder.addOption(OptionsType.ANNOUNCE_WIN);
						LogUtil.info(table.getGame().getRoomId()
								+ " ANNOUNCE_WIN "
								+ other.getRole().getRole().getNick() + "胡 "
								+ pai);
						other.setHuFan(FanNum);
					} else {
//						System.out.println("玩家：" + other.getRole().getSeat()
//								+ "上一轮能胡没胡  所以不能胡");
//						System.out.println("上次没胡的番数:" + other.getHuFan());
//						System.out.println("这次胡的番数:" + FanNum);
					}
				}
				if (waitBuilder.getOptionCount() > 0) {
					if (!other.getRole().isRobot()) {
						onlyRobot = false;
					}
					table.getCanOptions().put(other.getRole().getSeat(),
							waitBuilder.getOptionList());

					// 可进行操作
					roleFunction.sendMessageToPlayer(other.getRole().getRole()
							.getRid(), waitBuilder.build());
					LogUtil.info("dealdiscard wanjiacaozuo:"
							+ waitBuilder.getOptionList());
					flag = false;
				}

			}

		}
		// 把table的状态设置为多人操作状态
		if (table.getCanOptions().size() > 1) {
			table.setManyOperate(true);
		}

		LogUtil.info("table是否为多人操作:" + table.isManyOperate());
		LogUtil.info("flag:" + flag);

		if (flag) {
			tableToWait(table, table.getLastPlaySeat(),
					table.getNextPlaySeat(), HandleType.MAJIANG_WAIT,
					System.currentTimeMillis() + 0);
		} else {

			// 优先级胡(AI手上有两张或以上癞子时 优先自摸)
			int huNum = 0;
			for (ZTMajiangRole other : table.getMembers()) {
				if (other != role && other.getHuType() == 0) {
					List<OptionsType> data = table.getCanOptions().get(
							other.getRole().getSeat());

					if (data != null && data.contains(OptionsType.ANNOUNCE_WIN)) {
						huNum++;
					}
				}
			}
			if (huNum >= 2) {
				LogUtil.debug("一炮多响触发");
				tableToWait(table, role.getRole().getSeat(),
						table.getNextPlaySeat(), HandleType.MAJIANG_MANY_HU,
						System.currentTimeMillis()
								+ (table.getActionDuration() + 1) * 1000,
						System.currentTimeMillis()
								+ (table.getActionDuration() + 1) * 1000);
				return;
			}
			for (ZTMajiangRole other : table.getMembers()) {
				if (other != role && other.getHuType() == 0) {
					List<OptionsType> data = table.getCanOptions().get(
							other.getRole().getSeat());

					if (data != null && data.contains(OptionsType.ANNOUNCE_WIN)) {

						// 有别人操作
						GMsg_12041006.Builder builder3 = GMsg_12041006
								.newBuilder();
						/*
						 * builder3.setAction(OptionsType.PENG);
						 * builder3.setCurrentSeat(seat);
						 * builder3.setWaitTime(TimeUtil.time() +
						 * table.getActionDuration());
						 * builder3.setOverTime(TimeUtil.time() +
						 * table.getActionDuration());
						 * roleFunction.sendMessageToPlayers(table.getGame()
						 * .getRoles(), builder3.build());
						 */

						if (other.getRole().isRobot()) {
							int interval = MathUtil.randomNumber(1000, 2000);
							if (table.isManyOperate()) {
								table.getReceiveQueue().put(
										other.getRole().getSeat(),
										OptionsType.ANNOUNCE_WIN);
								doManyHand(table);
							} else {
								tableToWait(table, other.getRole().getSeat(),
										table.getNextPlaySeat(),
										HandleType.MAJIANG_HU,
										System.currentTimeMillis() + interval);
							}

						} else {
							if (other.getRole().isAuto()) {
								int interval = MathUtil
										.randomNumber(1500, 2500);
								if (table.isManyOperate()) {
									table.getReceiveQueue().put(
											other.getRole().getSeat(),
											OptionsType.ANNOUNCE_WIN);
									LogUtil.info("机器人发送指令...hu");
									doManyHand(table);
								} else {
									tableToWait(table, other.getRole()
											.getSeat(),
											table.getNextPlaySeat(),
											HandleType.MAJIANG_HU,
											System.currentTimeMillis()
													+ interval);
								}

							} else {
								tableToWait(
										table,
										other.getRole().getSeat(),
										table.getNextPlaySeat(),
										HandleType.MAJIANG_HU,
										System.currentTimeMillis()
												+ (table.getActionDuration() + 1)
												* 1000);
							}
						}
						return;
					}
				}
			}
			// 其他根据响应排队
			for (ZTMajiangRole other : table.getMembers()) {
				if (other != role
						&& !table.getWinners().contains(
								other.getRole().getSeat())) {
					List<OptionsType> data = table.getCanOptions().get(
							other.getRole().getSeat());
					if (data != null) {
						if (other.getRole().isAuto()) {
							if (onlyRobot) {
								int interval = MathUtil
										.randomNumber(1000, 2000);
								// 机器人默认就第一个就操作了
								int pai_type = pai / 10;
								if (data.contains(OptionsType.EXPOSED_GANG)) {
									table.setWaitSeat(other.getRole().getSeat());
									if (pai_type == other.getQueType()) {

										tableToWait(table,
												table.getLastPlaySeat(),
												table.getNextPlaySeat(),
												HandleType.MAJIANG_WAIT,
												System.currentTimeMillis()
														+ interval);
									} else {
										if (table.isManyOperate()) {
											table.getReceiveQueue().put(
													other.getRole().getSeat(),
													OptionsType.EXPOSED_GANG);
											LogUtil.info("机器人发送指令...gang");
											doManyHand(table);
										} else {
											tableToWait(table, other.getRole()
													.getSeat(),
													table.getNextPlaySeat(),
													HandleType.MAJIANG_GANG,
													System.currentTimeMillis()
															+ interval);
										}

									}
								} else if (data.contains(OptionsType.PENG)) {
									table.setWaitSeat(other.getRole().getSeat());
									if (pai_type == other.getQueType()) {
										if (table.isManyOperate()) {
											table.getReceiveQueue().put(
													other.getRole().getSeat(),
													OptionsType.PASS);
											LogUtil.info("机器人发送指令...pass");
											doManyHand(table);
										} else {
											tableToWait(table,
													table.getLastPlaySeat(),
													table.getNextPlaySeat(),
													HandleType.MAJIANG_WAIT,
													System.currentTimeMillis()
															+ interval);
										}

									} else {
										if (table.isManyOperate()) {
											table.getReceiveQueue().put(
													other.getRole().getSeat(),
													OptionsType.PENG);
											LogUtil.info("机器人发送指令...peng");
											doManyHand(table);
										} else {
											LogUtil.info("机器人tabletowait...peng");
											tableToWait(table, other.getRole()
													.getSeat(),
													table.getNextPlaySeat(),
													HandleType.MAJIANG_PENG,
													System.currentTimeMillis()
															+ interval);
										}

									}
								} else {
									tableToWait(table, table.getLastPlaySeat(),
											table.getNextPlaySeat(),
											HandleType.MAJIANG_WAIT,
											System.currentTimeMillis() + 500);
								}

								break;
							} else {
								tableToWait(
										table,
										table.getLastPlaySeat(),
										table.getNextPlaySeat(),
										HandleType.MAJIANG_WAIT,
										System.currentTimeMillis()
												+ (table.getActionDuration())
												* 1000 - 500);
								if (other.getRole().isAuto()) {
									dealPass(table, other);
								}

							}

						} else {
							tableToWait(
									table,
									table.getLastPlaySeat(),
									table.getNextPlaySeat(),
									HandleType.MAJIANG_WAIT,
									System.currentTimeMillis()
											+ (table.getActionDuration() + 1)
											* 1000);
						}
					}

				}
			}
			// 通知等待别人操作
			GMsg_12041006.Builder builderWait = GMsg_12041006.newBuilder();
			/*
			 * builderWait.setAction(OptionsType.PENG);
			 * builderWait.setCurrentSeat(seat); builderWait
			 * .setWaitTime(TimeUtil.time() + table.getActionDuration());
			 * builderWait.setOverTime(TimeUtil.time() +
			 * table.getActionDuration());
			 * roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
			 * builderWait.build());
			 */
		}

		/*
		 * if (table.getPais().size() == 0) { //TODO
		 * 
		 * GMsg_12011009.Builder endBuilder = GMsg_12011009.newBuilder(); for
		 * (ZTMajiangRole majiangRole : table.getMembers()) { GBillsInfo.Builder
		 * end = GBillsInfo.newBuilder();
		 * end.setWinTimes(majiangRole.getCurrentPower()); end.setGold(333);
		 * end.setSeat(majiangRole.getRole().getSeat());
		 * endBuilder.addBills(end);
		 * 
		 * } roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
		 * endBuilder.build());
		 * 
		 * table.getGame().setStatus(GameStatus.END_REWARD);
		 * 
		 * } else{
		 * 
		 * 
		 * }
		 */

		getOutPutAllPai(table);
	}

	/**
	 * 通知操作结果并提交下一操作
	 */
	public void processAction(ZTMaJongTable table, OptionsType action,
			int nextSeat, int handleType, long time) {
		GMsg_12041008.Builder builder = GMsg_12041008.newBuilder();

		GPaiInfo.Builder value = GPaiInfo.newBuilder();
		builder.setOption(action);
		builder.setNextSeat(nextSeat);
		builder.setTargetSeat(table.getLastPlaySeat());
		builder.setOperatePai(value);

		if (action == OptionsType.DISCARD_TILE) {
			value.addPai(table.getLastPai());
		}

		builder.setOperatePai(value);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());
		/*
		 * LogUtil.info(table.getGame().getRoomId() + " seat " +
		 * table.getLastPlaySeat() + " DoudizhuEvent " + handleType);
		 */

		table.setLastPlaySeat(nextSeat);
		// LogUtil.info(table+" !havehu把lastplayseat设置为:"+table.getLastOutPai()+" 该座位的玩家昵称:"+table.getMembers().get(table.getLastOutPai()
		// - 1).getRole().getRole().getNick());
		table.setNextSeat(table.getNextPlaySeat());

		tableToWait(table, table.getLastPlaySeat(), table.getNextPlaySeat(),
				handleType, time);
	}

	/**
	 * 能否胡
	 *
	 * @param maJongTable
	 * @param member
	 */
	public boolean canHu(ZTMaJongTable table, ZTMajiangRole role,
			Integer destPai) {
		// [36, 14, 39, 35, 34, 26, 37, 26, 13, 38, 35, 12, 37, 34]
		List<Integer> memberShouPai = new ArrayList<Integer>();
		memberShouPai.addAll(role.getPai());
		if (destPai != null) {
			memberShouPai.add(destPai);
		}
		int[][] memberPai = ZXMahJongRule.conversionType(memberShouPai);
		if (!ZXMahJongRule.isQueYiMen(memberPai, role.getQueType())) {
			return false;
		}

		boolean huPai = ZXMahJongRule.fitHu(memberPai, role.getShowPai());

		LogUtil.info(memberShouPai + "data:"
				+ role.getRole().getRole().getNick() + "canHu" + memberShouPai);
		return huPai;
	}

	/**
	 * 能否碰
	 *
	 * @param maJongTable
	 * @param member
	 */
	public boolean canPeng(ZTMaJongTable table, ZTMajiangRole member) {
		int lastPai = table.getLastPai();
		int paiType = lastPai / 10;
		if (member.getQueType() == paiType) {
			return false;
		}

		List<Integer> memberShouPai = member.getPai();
		int[][] memberPai = ZXMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int yetNum = memberPai[paiType - 1][lastPai % 10];
		if (yetNum >= 2) {
			return true;
		}
		return false;
	}

	/**
	 * 能否暗杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public int canDarkGang(ZTMaJongTable table, ZTMajiangRole member) {
		List<Integer> memberShouPai = member.getPai();
		int[][] memberPai = ZXMahJongRule.conversionType(memberShouPai);
		for (int i = 0; i < 3; i++) {
			for (int j = 1; j < 10; j++) {
				if (member.getQueType() == (i + 1)) {
					continue;
				}

				int count = memberPai[i][j];
				if (count > 3) {
					return (i + 1) * 10 + j;
				}
			}
		}
		return 0;
	}

	/**
	 * 能否杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public boolean canGang(ZTMaJongTable table, ZTMajiangRole member) {
		int lastPai = table.getLastPai();
		int paiType = lastPai / 10;
		if (member.getQueType() == paiType) {
			return false;
		}

		List<Integer> memberShouPai = member.getPai();
		int[][] memberPai = ZXMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int yetNum = memberPai[paiType - 1][lastPai % 10];
		if (yetNum >= 3) {
			return true;
		}
		return false;
	}

	/**
	 * 能否飞碰
	 *
	 * @param maJongTable
	 * @param member
	 */
	public boolean canFreePeng(ZTMaJongTable table, ZTMajiangRole member) {
		return false;
	}

	/**
	 * 能否飞杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public boolean canFreeGang(ZTMaJongTable table, ZTMajiangRole member) {
		return false;
	}

	/**
	 * 能否飞补杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public boolean canExtraFreeGang(ZTMaJongTable table, ZTMajiangRole member) {
		return false;
	}

	/**
	 * 能否补杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public Integer canExtraGang(ZTMaJongTable table, ZTMajiangRole member) {
		List<Integer> memberShouPai = member.getPai();
		int[][] memberPai = ZXMahJongRule.conversionType(memberShouPai);
		for (Map.Entry<Integer, List<Integer>> entry : member.getShowPai()
				.entrySet()) {
			List<Integer> data = entry.getValue();
			if (data.size() == 3) {
				int yetNum = memberPai[entry.getKey() / 10 - 1][entry.getKey() % 10];
				if (yetNum >= 1) {
					return entry.getKey();
				}
			}
		}
		return 0;
	}

	/**
	 * 处理碰
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealPeng(ZTMaJongTable table, ZTMajiangRole role) {
		if (checkOrder(table, role, OptionsType.PENG)) {

		}
		table.setQueueWaitType(0);
		int seat = role.getRole().getSeat();
		table.setLastRealSeat(seat);

		Integer lastPai = table.getLastPai();
		List<Integer> memberShouPai = role.getPai();
		int[][] memberPai = ZXMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int yetNum = memberPai[lastPai / 10 - 1][lastPai % 10];
		if (yetNum >= 2) {
			table.getYetOptions().put(role.getRole().getSeat(),
					OptionsType.PENG);
		} else {
			LogUtil.error("处理碰 请求错误 条件检查不通过");
		}

		ZTMajiangRole lastRole = table.getMembers().get(
				table.getLastOutPai() - 1);
		lastRole.getRecyclePai().remove(lastPai);

		List<Integer> rest = new ArrayList<Integer>();
		rest.add(lastPai);
		rest.add(lastPai);
		rest.add(lastPai);

		role.getShowPai().put(lastPai, rest);
		role.getPai().remove(lastPai);
		role.getPai().remove(lastPai);

		GPaiInfo.Builder info = GPaiInfo.newBuilder();
		info.addAllPai(rest);
		GMsg_12041008.Builder builder = GMsg_12041008.newBuilder();
		builder.setNextSeat(seat);
		builder.setOption(OptionsType.PENG);
		builder.setTargetSeat(table.getLastOutPai());
		builder.setOperatePai(info);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		List<OptionsType> list = new ArrayList<OptionsType>();
		list.add(OptionsType.DISCARD_TILE);

		table.getCanOptions().put(seat, list);

		// 重置操作座位
		table.setLastPlaySeat(seat);
		LogUtil.info(table
				+ " dealpeng把lastplayseat设置为:"
				+ seat
				+ " 该座位的玩家昵称:"
				+ table.getMembers().get(seat - 1).getRole().getRole()
						.getNick());
		// 直接出牌的流程
		GMsg_12041006.Builder chuBuilder = GMsg_12041006.newBuilder();

		// 读取麻将配置的操作时间
		int waitTime = table.getTurnDuration();// 等待第1轮时间
		int overTime = table.getTurnDuration();// 等待第2轮时间
		if (role.getTimeOutNum() < table.getOtpPunishment()) {
			overTime += table.getTurn2Duration();
		}
		chuBuilder.setOverTime(TimeUtil.time() + overTime);
		// 读取麻将配置的操作时间

		chuBuilder.setAction(OptionsType.DISCARD_TILE);
		chuBuilder.setCurrentSeat(seat);
		chuBuilder.setWaitTime(TimeUtil.time() + waitTime);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				chuBuilder.build());

		GMsg_12041007.Builder builder4 = GMsg_12041007.newBuilder();
		builder4.addAllOption(list);
		roleFunction.sendMessageToPlayer(role.getRole().getRole().getRid(),
				builder4.build());
		LogUtil.info(table.getGame().getRoomId() + " seat " + seat + "碰 "
				+ lastPai);

		if (role.getRole().isAuto()) {
			tableToWait(table, seat, table.getNextPlaySeat(),
					HandleType.MAJIANG_OUT_PAI,
					System.currentTimeMillis() + 1000,
					System.currentTimeMillis() + (overTime + 1) * 1000);
		} else {
			tableToWait(table, seat, table.getNextPlaySeat(),
					HandleType.MAJIANG_OUT_PAI, System.currentTimeMillis()
							+ (overTime + 1) * 1000);
		}

		// 取消其他人的弃牌/弃胡
		for (ZTMajiangRole role2 : table.getMembers()) {
			role2.setLastQiPai(0);
			role2.setHuFan(-1);
		}
		table.resetHu();
		table.getReceiveQueue().clear();
		table.setManyOperate(false);
	}

	public boolean checkOrder(ZTMaJongTable table, ZTMajiangRole role,
			OptionsType type) {
		int seat = role.getRole().getSeat();

		role.setOptionsType(type);
		table.getWaiter().add(seat);

		if (table.getCanOptions().size() >= 2) {
			List<Integer> huSeat = new ArrayList<Integer>();
			List<Integer> otherSeat = new ArrayList<Integer>();
			for (Map.Entry<Integer, List<GBaseMahJong.OptionsType>> entry : table
					.getCanOptions().entrySet()) {
				if (entry.getValue().contains(OptionsType.ANNOUNCE_WIN)) {
					huSeat.add(entry.getKey());
				} else {
					otherSeat.add(entry.getKey());
				}
			}
			if (huSeat.size() == 0) {
				for (Integer otherSeatIndex : otherSeat) {
					ZTMajiangRole target = table.getMembers().get(
							otherSeatIndex - 1);
					target.setLastQiPai(table.getLastPai()); // 设置弃牌
				}
				ZXMahJongRule.getNextPlaySeat(
						table.getGame().getRoles().size(), table.getWinners(),
						table.getLastOutPai());
			} else if (huSeat.size() == 1) {

			} else {

			}
		}
		if (type == OptionsType.ANNOUNCE_WIN) {

			return true;
		}
		if (table.getWaiter().size() >= table.getCanOptions().size()) {
			return true;
		}
		return false;
	}

	public boolean manyHu(ZTMaJongTable table, int seat) {
		List<Integer> orderList = new ArrayList<Integer>();
		boolean flag = false;
		int huNum = 0;
		for (Map.Entry<Integer, List<GBaseMahJong.OptionsType>> entry : table
				.getCanOptions().entrySet()) {
			if (entry.getValue().contains(OptionsType.ANNOUNCE_WIN)) {
				huNum++;
				orderList.add(entry.getKey());
			}
		}
		if (orderList.size() > 0 && orderList.contains(seat)) {
			if (table.getWaiter().containsAll(orderList)) {

			}
			for (Integer order : orderList) {
				dealHu(table, table.getMembers().get(order - 1));
			}
			table.setLastPlaySeat(table.getLastOutPai());
			LogUtil.info(table
					+ " manyhu把lastplayseat设置为:"
					+ table.getLastOutPai()
					+ " 该座位的玩家昵称:"
					+ table.getMembers().get(table.getLastOutPai() - 1)
							.getRole().getRole().getNick());
			// 一炮多响是
			ZTMajiangRole target = table.getMembers().get(
					table.getLastOutPai() - 1);
			if (target.getRole().isAuto()) {
				tableToWait(table, table.getLastPlaySeat(),
						table.getNextPlaySeat(), HandleType.MAJIANG_GET_PAI,
						System.currentTimeMillis() + 4000);
			} else {
				tableToWait(table, table.getLastPlaySeat(),
						table.getNextPlaySeat(), HandleType.MAJIANG_GET_PAI,
						System.currentTimeMillis() + 10000);
			}
		}
		return false;
	}

	/**
	 * 处理杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealGang(ZTMaJongTable table, ZTMajiangRole role) {
		refreshPai(table, role);// 向客户端发送玩家的牌信息同步手牌 摸牌之前
		table.setQueueWaitType(0);
		int seat = role.getRole().getSeat();
		Integer lastPai = table.getLastPai();
		List<Integer> memberShouPai = role.getPai();
		int[][] memberPai = ZXMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int yetNum = memberPai[lastPai / 10 - 1][lastPai % 10];
		if (yetNum >= 3) {
			table.getYetOptions().put(role.getRole().getSeat(),
					OptionsType.EXPOSED_GANG);
		} else {
			LogUtil.error("处理杠 请求错误 条件检查不通过");
		}

		ZTMajiangRole lastRole = table.getMembers().get(
				table.getLastOutPai() - 1);
		lastRole.getRecyclePai().remove(lastPai);

		List<Integer> rest = new ArrayList<Integer>();
		rest.add(lastPai);
		rest.add(lastPai);
		rest.add(lastPai);
		rest.add(lastPai);

		role.getShowPai().put(lastPai, rest);
		role.getPai().remove(lastPai);
		role.getPai().remove(lastPai);
		role.getPai().remove(lastPai);

		// 取消其他人的弃牌/弃胡
		for (ZTMajiangRole role2 : table.getMembers()) {
			role2.setLastQiPai(0);
			role2.setHuFan(-1);
		}

		// Integer pai = table.getPais().remove(table.getPais().size() - 1);

		// 概率摸牌
		Integer pai = moPai(table, lastRole);
		// pai = 12;
		// 概率摸牌

		table.setMoPai(seat);
		table.setLastMoPai(pai);
		role.getPai().add(pai);

		table.setLastRealSeat(seat);

		GPaiInfo.Builder info = GPaiInfo.newBuilder();
		info.addAllPai(rest);
		info.addPai(pai);

		GMsg_12041008.Builder builder = GMsg_12041008.newBuilder();
		builder.setNextSeat(seat);
		builder.setOption(OptionsType.EXPOSED_GANG);
		builder.setTargetSeat(table.getLastOutPai());
		builder.setOperatePai(info);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		// 重置操作座位
		table.setLastPlaySeat(seat);
		LogUtil.info(table
				+ " dealgang把lastplayseat设置为:"
				+ seat
				+ " 该座位的玩家昵称:"
				+ table.getMembers().get(seat - 1).getRole().getRole()
						.getNick());
		LogUtil.info(table.getGame().getRoomId() + " seat " + seat + "杠 "
				+ lastPai);

		checkSelfOption(table, role);
		table.resetHu();
		table.setManyOperate(false);
		table.getReceiveQueue().clear();

		/*** Mission-16* 麻将牌型任务上报:杠 ***********************************/
		if (!role.getRole().isRobot()) {
			long rid = role.getRole().getRole().getRid();

			// 任务检测
			missionFunction.checkTaskFinish(rid, TaskType.daily_task,
					MissionType.GANG);
			LogUtil.debug(rid + " > gang .........");
		}
		/* 玩家牌型任务上报 *************************************************** */

		// 直接出牌的流程
		/*
		 * List<OptionsType> list = new ArrayList<OptionsType>();
		 * list.add(OptionsType.DISCARD_TILE); table.getCanOptions().put(seat,
		 * list);
		 * 
		 * GMsg_12011006.Builder chuBuilder = GMsg_12011006.newBuilder();
		 * chuBuilder.setAction(OptionsType.DISCARD_TILE);
		 * chuBuilder.setCurrentSeat(seat);
		 * chuBuilder.setWaitTime(TimeUtil.time() + 12);
		 * roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
		 * chuBuilder.build());
		 * 
		 * GMsg_12011007.Builder builder4 = GMsg_12011007.newBuilder();
		 * builder4.addAllOption(list);
		 * roleFunction.sendMessageToPlayer(role.getRole().getRole().getRid(),
		 * builder4.build());
		 * 
		 * 
		 * if (role.getRole().isAuto()) { tableToWait(table, seat,
		 * table.getNextPlaySeat(), HandleType.MAJIANG_OUT_PAI,
		 * System.currentTimeMillis() + 1000); } else { tableToWait(table, seat,
		 * table.getNextPlaySeat(), HandleType.MAJIANG_OUT_PAI,
		 * System.currentTimeMillis() + 12000); }
		 */

	}

	/**
	 * 处理飞碰
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealFreePeng(ZTMaJongTable table, ZTMajiangRole role) {
		table.setQueueWaitType(0);
	}

	/**
	 * 处理飞杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealFreeGang(ZTMaJongTable table, ZTMajiangRole role) {
		table.setQueueWaitType(0);
	}

	/**
	 * 处理胡
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealHu(ZTMaJongTable table, ZTMajiangRole role) {
		table.setQueueWaitType(0);
		int seat = role.getRole().getSeat();

		Integer destPai = null;
		if (table.getLastRealSeat() == seat) {
			destPai = table.getLastMoPai();
			role.getPai().remove(destPai); // 先移除，后面会把胡的这张牌放到第一个位置
			role.setHuType(-1);// 自摸
		} else {
			LogUtil.info("是否是抢杠状态并且进入处理抢杠胡逻辑:" + table.isQiangGangHu());
			if (table.isQiangGangHu()) {// 抢杠胡的时候
				destPai = table.getBuGangPai();
				LogUtil.info("抢杠胡的牌:" + destPai);
				role.setHuType(destPai);
				LogUtil.info("玩家手牌抢杠:" + role.getPai());
				// 从放炮玩家已出牌列表中移除这张牌
				ZTMajiangRole lastRole = table.getMembers().get(
						table.getBeiQiangGangHuSeat() - 1);
				lastRole.getPai().remove(destPai);
				GMsg_12041021.Builder builder = GMsg_12041021.newBuilder();
				builder.setPai(destPai);
				builder.setSeat(table.getBeiQiangGangHuSeat());
				LogUtil.info("发送 抢杠协议被抢杠胡的座位:" + builder.getSeat());
				roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
						builder.build());
			} else {
				destPai = table.getLastPai();
				role.setHuType(destPai);

				// 从放炮玩家已出牌列表中移除这张牌
				ZTMajiangRole lastRole = table.getMembers().get(
						table.getLastOutPai() - 1);
				lastRole.getRecyclePai().remove(destPai);
			}

		}

		// 把胡的这张牌放到第一张-用以重连的时候告诉重连玩家
		role.getPai().add(0, destPai);

		table.getWinners().add(role.getRole().getSeat());
		LogUtil.info("winners：" + table.getWinners());

		// 手牌
		List<Integer> memberShouPai = new ArrayList<Integer>();
		memberShouPai.addAll(role.getPai());

		// 摆牌
		List<Integer> memberShowPai = new ArrayList<Integer>();
		Map<Integer, List<Integer>> showPai = role.getShowPai();
		for (Map.Entry<Integer, List<Integer>> entry : showPai.entrySet()) {
			memberShowPai.addAll(entry.getValue());
		}

		// 全牌
		List<Integer> allPai = new ArrayList<Integer>();
		allPai.addAll(memberShouPai);// 手牌
		allPai.addAll(memberShowPai);// 摆牌
		LogUtil.info(seat + "------胡牌-----" + memberShouPai);

		List<Integer> listTypeId = dealFanType(role, table, memberShouPai,
				memberShowPai, allPai, showPai);// 番的类型
//		System.out.println("listTypeId：" + listTypeId);
		List<Integer> sendListTypeList = new ArrayList<Integer>();
		sendListTypeList.addAll(listTypeId);
		dealWin(role, table, listTypeId);// 处理赢,流水id设置
		// dealZeroGold(table);

		role.getRole().setStatus(PlayerState.PS_WATCH_VALUE);

		// 胡牌后不亮出所有牌（牌桌上的玩家都不可以看），剩下的继续血战。亮出来的只是玩家胡的那一张牌
		GPaiInfo.Builder info = GPaiInfo.newBuilder();
		info.addPai(destPai);

		GMsg_12041008.Builder builder = GMsg_12041008.newBuilder();
		builder.setOption(OptionsType.ANNOUNCE_WIN);
		builder.setOperatePai(info);

		int target_seat = seat;
		if (role.getHuType() != -1) {
			if (!table.isQiangGangHu()) {
				ZTMajiangRole lastRole = table.getMembers().get(
						table.getLastOutPai() - 1);
				target_seat = lastRole.getRole().getSeat();
			} else {
				ZTMajiangRole lastRole = table.getMembers().get(
						table.getBeiQiangGangHuSeat() - 1);
				target_seat = lastRole.getRole().getSeat();
			}

		}
		builder.setTargetSeat(target_seat);
		builder.setNextSeat(seat);

		// 发送飘字
		GMsg_12041012.Builder goldBuilder = GMsg_12041012.newBuilder();
		for (Entry<Integer, GMajiangGold.Builder> entry : table.getPiaoZi()
				.entrySet()) {
			goldBuilder.addChangeGold(entry.getValue());
		}
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				goldBuilder.build());
		// 胡的时候返回胡的类型
		GDetailBillsInfo.Builder detailBillsInfo = GDetailBillsInfo
				.newBuilder();
		detailBillsInfo.setNick(role.getRole().getRole().getNick());
		detailBillsInfo.addAllWinTypes(sendListTypeList);
		LogUtil.info("发给客户端的胡牌类型：" + detailBillsInfo.getWinTypesList());
		detailBillsInfo.setGoldDetail(0);
		detailBillsInfo.setWinTimes(0);
		builder.setHuStyle(detailBillsInfo);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		// 检查游戏中是否存在玩家金币为零的情况
		checkGoldZeroMember(table);
		// 重置操作座位 为胡家
		table.setLastPlaySeat(seat);
		if (!table.isGamePause()) {
			LogUtil.info("table.getlastplayseat0：" + table.getLastPlaySeat());
			LogUtil.info(table
					+ " dealhu把lastplayseat设置为:"
					+ seat
					+ " 该座位的玩家昵称:"
					+ table.getMembers().get(seat - 1).getRole().getRole()
							.getNick());
			table.setLastRealSeat(seat);
			LogUtil.info("table.getlastplayseat1：" + table.getLastPlaySeat());
			table.setLastPlaySeat(table.getNextPlaySeat());
			LogUtil.info(table
					+ " dealhu把lastplayseat设置为:"
					+ table.getNextPlaySeat()
					+ " 该座位的玩家昵称:"
					+ table.getMembers().get(table.getNextPlaySeat() - 1)
							.getRole().getRole().getNick());
			LogUtil.info("table.getlastplayseat2：" + table.getLastPlaySeat());
			table.setNextSeat(table.getNextPlaySeat());
			if (table.getPais().size() > 0) {
				if (table.getWinners().size() >= table.getMembers().size() - 1) {
					endTable(table);
				} else {
					tableToWait(table, table.getLastPlaySeat(),
							table.getNextPlaySeat(),
							HandleType.MAJIANG_GET_PAI,
							System.currentTimeMillis() + 1500);
				}
			} else {
				endTable(table);
			}
		} else {
			// 调用踢人处理事件
			tableToWait(table, table.getLastPlaySeat(),
					table.getNextPlaySeat(), HandleType.MAJIANG_KICK_PLAYER,
					System.currentTimeMillis() + 15000);
		}

		table.getYetOptions().put(role.getRole().getSeat(),
				OptionsType.ANNOUNCE_WIN);
		table.getCanOptions().clear();
		table.getWaiter().clear();
		table.resetHu();
		table.getReceiveQueue().clear();
		table.setQiangGangHu(false);
		table.getPiaoZi().clear();
		table.setManyOperate(false);
		table.setGamePause(false);
	}

	/**
	 * 处理多人胡
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealManyHu(ZTMaJongTable table, ZTMajiangRole... roleList) {
		table.setQueueWaitType(0);
		LogUtil.debug("胡的玩家列表大小:" + roleList.length);
		// LogUtil.debug("胡的玩家座位号:"+roleList[0].getRole().getSeat()+"-======"+roleList[1].getRole().getSeat());
		GMsg_12041014.Builder builder = GMsg_12041014.newBuilder();
		GHuSeat.Builder huSeatInfo = GHuSeat.newBuilder();
		// 按照座位进行逆时针排序 得到逆时针排序的rolelist数组
		List<Integer> seatList = new ArrayList<Integer>();
		for (ZTMajiangRole role : roleList) {
			if (role != null) {
				seatList.add(role.getRole().getSeat());
			}

		}
		List<Integer> youXuSeat = table.getYouXianJiSeat(seatList,
				table.getLastOutPai());
		ZTMajiangRole[] youXuRoleList = new ZTMajiangRole[3];
		for (int i = 0; i < youXuSeat.size(); i++) {
			youXuRoleList[i] = table.getMembers().get(youXuSeat.get(i) - 1);
		}
		int targetSeat = table.getLastOutPai();
		if (table.isQiangGangHu()) {
			targetSeat = table.getBeiQiangGangHuSeat();
		}

		// 放炮玩家已出牌列表中移除一次牌
		Integer destPai = table.getLastPai();
		if (table.isQiangGangHu()) {
			destPai = table.getBuGangPai();
			// role.setHuType(destPai);
			// LogUtil.info("玩家手牌:" + role.getPai());
			// LogUtil.info("抢杠胡的牌:" + destPai);
			// 从被抢杠玩家手牌中移除这张牌
			ZTMajiangRole lastRole = table.getMembers().get(
					table.getBeiQiangGangHuSeat() - 1);
			lastRole.getPai().remove(destPai);
			GMsg_12041021.Builder gangBuilder = GMsg_12041021.newBuilder();
			gangBuilder.setPai(destPai);
			gangBuilder.setSeat(table.getBeiQiangGangHuSeat());
			LogUtil.info("被抢杠胡的座位:" + gangBuilder.getSeat());
			roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
					gangBuilder.build());
		} else {
			// role.setHuType(destPai);
			ZTMajiangRole lastRole = table.getMembers().get(
					table.getLastOutPai() - 1);
			lastRole.getRecyclePai().remove(destPai);
		}

		for (ZTMajiangRole role : youXuRoleList) {
			if (role != null) {

				table.setQueueWaitType(0);
				int seat = role.getRole().getSeat();

				GPaiInfo.Builder info = GPaiInfo.newBuilder();

				LogUtil.info("玩家手牌:" + role.getPai());
				LogUtil.info("玩家位置:" + role.getRole().getSeat());

				// info.addPai(destPai);
				role.getPai().add(0, destPai);
				role.setHuType(destPai);

				table.getYetOptions().put(role.getRole().getSeat(),
						OptionsType.ANNOUNCE_WIN);

				// 手牌
				List<Integer> memberShouPai = new ArrayList<Integer>();
				memberShouPai.addAll(role.getPai());
				// info.addAllPai(memberShouPai);
				info.addPai(destPai);

				// 摆牌
				List<Integer> memberShowPai = new ArrayList<Integer>();
				Map<Integer, List<Integer>> showPai = role.getShowPai();
				for (Map.Entry<Integer, List<Integer>> entry : showPai
						.entrySet()) {
					memberShowPai.addAll(entry.getValue());
				}

				// 全牌
				List<Integer> allPai = new ArrayList<Integer>();
				allPai.addAll(memberShouPai);// 手牌
				allPai.addAll(memberShowPai);// 摆牌
				LogUtil.info(seat + "------胡牌-----" + memberShouPai);
				List<Integer> listTypeId = dealFanType(role, table,
						memberShouPai, memberShowPai, allPai, showPai);// 番的类型
				table.getWinners().add(role.getRole().getSeat());
				LogUtil.info("winners：" + table.getWinners());
				dealWin(role, table, listTypeId);// 处理赢,流水id设置
				// 重置操作座位 为胡家
				LogUtil.info("最后操作的玩家座位号:" + seat);
				table.setLastPlaySeat(seat);
				LogUtil.info(table
						+ " dealmanyhu把lastplayseat设置为:"
						+ seat
						+ " 该座位的玩家昵称:"
						+ table.getMembers().get(seat - 1).getRole().getRole()
								.getNick());
				table.setLastRealSeat(seat);
				LogUtil.info(table
						+ " dealmanyhu把lastplayseat设置为:"
						+ table.getNextPlaySeat()
						+ " 该座位的玩家昵称:"
						+ table.getMembers().get(table.getNextPlaySeat() - 1)
								.getRole().getRole().getNick());
				LogUtil.info("table.getlastseat:" + table.getLastPlaySeat());
				table.setNextSeat(table.getNextPlaySeat());

				role.getRole().setStatus(PlayerState.PS_WATCH_VALUE);
				int target_seat = seat;
				if (role.getHuType() != -1) {
					ZTMajiangRole lastRole = table.getMembers().get(
							table.getLastOutPai() - 1);
					target_seat = lastRole.getRole().getSeat();
				}

				// 胡的时候返回胡的类型
				GDetailBillsInfo.Builder detailBillsInfo = GDetailBillsInfo
						.newBuilder();
				detailBillsInfo.setNick(role.getRole().getRole().getNick());
				detailBillsInfo.addAllWinTypes(listTypeId);
				detailBillsInfo.setGoldDetail(0);
				detailBillsInfo.setWinTimes(0);

				// huSeatInfo.setOption(OptionsType.ANNOUNCE_WIN);
				huSeatInfo.setTargetSeat(targetSeat);// 被胡的玩家
				huSeatInfo.setHuSeat(seat);// 胡的玩家
				huSeatInfo.setHuPai(info);// 胡的手牌
				huSeatInfo.setHuStyle(detailBillsInfo);// 胡的类型
				LogUtil.debug("一炮多响的时候发给客户端的 被胡的玩家:" + targetSeat + "====胡的玩家:"
						+ seat + "====" + info.getPaiList() + "====胡的类型"
						+ detailBillsInfo.getWinTypesList());
				builder.addHuInfo(huSeatInfo);
			}

		}
		// 发送飘字
		GMsg_12041012.Builder goldBuilder = GMsg_12041012.newBuilder();
		for (Entry<Integer, GMajiangGold.Builder> entry : table.getPiaoZi()
				.entrySet()) {
			goldBuilder.addChangeGold(entry.getValue());
		}
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				goldBuilder.build());
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		checkGoldZeroMember(table);
		if (!table.isGamePause()) {
			table.setLastPlaySeat(table.getNextPlaySeat());
			if (table.getPais().size() > 0) {
				if (table.getWinners().size() >= table.getMembers().size() - 1) {
					endTable(table);
				} else {
					LogUtil.debug("轮到谁摸牌:" + table.getLastPlaySeat());
					tableToWait(table, table.getLastPlaySeat(),
							table.getNextPlaySeat(),
							HandleType.MAJIANG_GET_PAI,
							System.currentTimeMillis() + 1500);
				}
			} else {
				endTable(table);
			}
		}

		table.getCanOptions().clear();
		table.getWaiter().clear();
		table.getReceiveQueue().clear();
		table.setQiangGangHu(false);
		table.getPiaoZi().clear();
		table.setManyOperate(false);
		table.setGamePause(false);

	}

	public List<Integer> dealFanType2(ZTMajiangRole role, ZTMaJongTable table) {

		int seat = role.getRole().getSeat();
		Integer destPai = null;

		if (table.getLastRealSeat() == seat) {
			role.setHuType(-1);// 自摸
		} else {
			destPai = table.getLastPai();
		}

		// 手牌
		List<Integer> memberShouPai = new ArrayList<Integer>();
		memberShouPai.addAll(role.getPai());
		if (destPai != null) {
			memberShouPai.add(destPai);
		}

		// 摆牌
		List<Integer> memberShowPai = new ArrayList<Integer>();
		Map<Integer, List<Integer>> showPai = role.getShowPai();
		for (Map.Entry<Integer, List<Integer>> entry : showPai.entrySet()) {
			List<Integer> listPai = entry.getValue();
			for (int j = 0; j < listPai.size(); j++) {
				memberShowPai.add(listPai.get(j));
			}
		}
		// 全牌
		List<Integer> allPai = new ArrayList<Integer>();
		allPai.addAll(memberShouPai);// 手牌
		allPai.addAll(memberShowPai);// 摆牌

		role.setHuType(0);
		return dealFanType(role, table, memberShouPai, memberShowPai, allPai,
				showPai);
	}

	/**
	 * 处理胡的类型
	 * 
	 * @param role
	 * @param table
	 * @param memberShouPai手牌
	 * @param memberShowPai摆牌癞子作为普通牌设进去
	 *            ,经过转换)
	 * @param allPai所有牌
	 * @param showPai摆拍
	 *            （用户摆出的牌直接set进去）
	 * @return
	 */
	public List<Integer> dealFanType(ZTMajiangRole role, ZTMaJongTable table,
			List<Integer> memberShouPai, List<Integer> memberShowPai,
			List<Integer> allPai, Map<Integer, List<Integer>> showPai) {
		// 打印
		// for (Integer integer : allPai) {
		// System.out.print(" all" + integer);
		// }
		// System.out.println("****");
		// for (Integer integer : memberShouPai) {
		// System.out.print(" ShouPai" + integer);
		// }
		// System.out.println("****");
		// for (Integer integer : memberShowPai) {
		// System.out.print(" ShowPai" + integer);
		// }
		// System.out.println("****");
		// 一副手牌
		List<Integer> shouPai = new ArrayList<Integer>();
		shouPai.addAll(memberShouPai);

		// 胡的类型（大类）
		List<Integer> listHuTypeId = new ArrayList<Integer>();// 胡大类的集合

		if (memberShouPai.size() == 14) {

			listHuTypeId.add(ZhenXiongWinType.ZX_MEN_QING_VALUE);

			if (ZXMahJongRule.judegeSevenPairs(shouPai)) {// 7对(手牌一定要14张)
				listHuTypeId.add(ZhenXiongWinType.ZX_QIAO_QI_DUI_VALUE);
			}
			if (ZXMahJongRule.judegeLongSevenPairs(shouPai)) {// 龙7对(手牌一定要14张)
				listHuTypeId.add(ZhenXiongWinType.ZX_LONG_QI_DUI_VALUE);
			}
		}
		if (ZXMahJongRule.judgeFourTriple(memberShouPai)) {// 大对子
			listHuTypeId.add(ZhenXiongWinType.ZX_DA_DUI_ZI_VALUE);
		}

		if (ZXMahJongRule.judgeFlush(allPai)) {// 清一色
			listHuTypeId.add(ZhenXiongWinType.ZX_QING_YI_SE_VALUE);
		}
		if (table.getPais().size() == 54 && role.getHuType() == -1)// 天胡
		{
			listHuTypeId.add(ZhenXiongWinType.ZX_TIAN_HU_VALUE);
		}
		if (table.getPais().size() == 54 && role.getHuType() != -1
				&& table.getOwner() == table.getLastOutPai())// 地胡
		{
			listHuTypeId.add(ZhenXiongWinType.ZX_DI_HU_VALUE);
		}

		int show_gang_num = ZXMahJongRule.judegeWithGang(memberShouPai, showPai);
		if (show_gang_num == 1) // 一杠
		{
			listHuTypeId.add(ZhenXiongWinType.ZX_1_GNAG_VALUE);
		} else if (show_gang_num == 2) {
			listHuTypeId.add(ZhenXiongWinType.ZX_2_GANG_VALUE);
		} else if (show_gang_num == 3) {
			listHuTypeId.add(ZhenXiongWinType.ZX_3_GANG_VALUE);
		}

		if (listHuTypeId.contains(ZhenXiongWinType.ZX_QIAO_QI_DUI_VALUE)
				&& listHuTypeId.contains(ZhenXiongWinType.ZX_QING_YI_SE_VALUE))// 清小队
		{
			listHuTypeId.add(ZhenXiongWinType.ZX_QING_XIAO_DUI_VALUE);
		}
		if (listHuTypeId.contains(ZhenXiongWinType.ZX_DA_DUI_ZI_VALUE)
				&& listHuTypeId.contains(ZhenXiongWinType.ZX_QING_YI_SE_VALUE))// 清大对子
		{
			listHuTypeId.add(ZhenXiongWinType.ZX_QING_DA_DUI_ZI_VALUE);
		}
		// 胡小类（判断全部牌）
		listHuTypeId.add(ZhenXiongWinType.ZX_PING_HU_VALUE);

		// 杠上花
		// LogUtil.info("table.getYetOptions().get(role.getRole().getSeat()："+table.getYetOptions().get(role.getRole().getSeat());
		if ((table.getYetOptions().get(role.getRole().getSeat()) == OptionsType.EXPOSED_GANG)) {// 明杠
			listHuTypeId.add(ZhenXiongWinType.ZX_GANG_SHANG_HUA_VALUE);
		}
		if ((table.getYetOptions().get(role.getRole().getSeat()) == OptionsType.EXTRA_GANG)) {
			listHuTypeId.add(ZhenXiongWinType.ZX_GANG_SHANG_HUA_VALUE);
		}
		if ((table.getYetOptions().get(role.getRole().getSeat()) == OptionsType.DARK_GANG)) {
			listHuTypeId.add(ZhenXiongWinType.ZX_GANG_SHANG_HUA_VALUE);
		}

		// 杠上炮
		if ((table.getYetOptions().get(table.getLastOutPai()) == OptionsType.EXPOSED_GANG)
				&& role.getHuType() != -1) {
			listHuTypeId.add(ZhenXiongWinType.ZX_GANG_SHANG_PAO_VALUE);
		}
		if ((table.getYetOptions().get(table.getLastOutPai()) == OptionsType.EXTRA_GANG)
				&& role.getHuType() != -1) {
			listHuTypeId.add(ZhenXiongWinType.ZX_GANG_SHANG_PAO_VALUE);
		}
		if ((table.getYetOptions().get(table.getLastOutPai()) == OptionsType.DARK_GANG)
				&& role.getHuType() != -1) {
			listHuTypeId.add(ZhenXiongWinType.ZX_GANG_SHANG_PAO_VALUE);
		}

		// 抢杠
		// if (table.getYetOptions().get(table.getLastPlaySeat()) ==
		// OptionsType.EXTRA_GANG) {// 当前角色明杠
		// ZTMajiangRole ZTMajiangRole = table.getMembers().get(
		// table.getMoPai() - 1);
		// Map<Integer, List<Integer>> pai = ZTMajiangRole.getShowPai();
		// for (Map.Entry<Integer, List<Integer>> entry : pai.entrySet()) {
		// if (entry.getValue().size() == 4) {// 四个值为杠
		// if (role.getHuType() == entry.getKey()) {// 胡的牌是杠的牌
		// listHuTypeId.add(ZhenXiongWinType.ZX_QIANG_GANG_VALUE);
		// break;
		// }
		// }
		// }
		// }
		if (table.isQiangGangHu()) {
			LogUtil.info("抢杠...");
			listHuTypeId.add(ZhenXiongWinType.ZX_QIANG_GANG_VALUE);
		}
		// 只剩下最后一张牌, 别人放炮胡-全求大对
		if (role.getPai().size() == 2 && role.getHuType() != -1) {
			listHuTypeId.add(ZhenXiongWinType.ZX_QUAN_QIU_DA_DUI_VALUE);
		}
		// 自摸
		if (role.getHuType() == -1) {
			listHuTypeId.add(ZhenXiongWinType.ZX_ZI_MO_VALUE);

			if (listHuTypeId.contains(ZhenXiongWinType.ZX_QIAO_QI_DUI_VALUE)) {
				listHuTypeId.add(ZhenXiongWinType.ZX_QIAO_QI_DUI_ZIMO_VALUE);
			}
		}
//		System.out.println("listHuTypeId" + listHuTypeId);
		return listHuTypeId;
	}

	/**
	 * 处理番的数目
	 * 
	 * @param listHuTypeId
	 *            （存的是胡的类型）
	 * @return
	 */
	public int dealFanNum(List<Integer> listHuTypeId, ZTMaJongTable table) {
		HashMap<Integer, Integer> fanMap = new HashMap<Integer, Integer>();// 大番map
		List<Integer> add = new ArrayList<Integer>();// 小番list
		int addfan = 0;// 小番的番数
		// 去除大番的记录
		for (Integer integer : listHuTypeId) {
			MaJiangZhenXiongValueCsv maJiangValueCsv = maJiangValueCache
					.getConfig(integer);
			if (maJiangValueCsv.getHuAdd() == 0) {
				fanMap.put(integer, maJiangValueCsv.getHuTimes());
			} else {
				add.add(integer);
				addfan += maJiangValueCsv.getHuTimes();
			}
		}
		int huBigTypeId = 0;
		int maxfan = -1;
		for (Map.Entry<Integer, Integer> entry : fanMap.entrySet()) {
			if (entry.getValue() > maxfan) {
				huBigTypeId = entry.getKey();
				maxfan = entry.getValue();
			}
		}
		listHuTypeId.clear();// 清除数据
		listHuTypeId.add(huBigTypeId);// 设置大番（只能显示一个）
		listHuTypeId.addAll(add);// 设置小番（显示多个）

//		System.out.println(listHuTypeId);
//		System.out.println("实际番数 ：" + (maxfan + addfan));
		int maxFanShu = table.getMaxFanShu();
		return (maxfan + addfan) > maxFanShu ? maxFanShu : maxfan + addfan;
	}

	/**
	 * 摸牌后检测自摸杠
	 * 
	 * @param role
	 * @param table
	 */
	public void checkSelfOption(ZTMaJongTable table, ZTMajiangRole role) {
		int seat = role.getRole().getSeat();

		table.getCanOptions().clear();
		table.getWaiter().clear();

		List<OptionsType> list = new ArrayList<OptionsType>();
		list.add(OptionsType.DISCARD_TILE);
		if (table.getPais().size() > 0) {
			if (canDarkGang(table, role) > 0) {
				list.add(OptionsType.DARK_GANG);
				LogUtil.info(table.getGame().getRoomId() + " DARK_GANG "
						+ role.getRole().getRole().getNick() + "按杠 "
						+ table.getLastMoPai());
			}
			if (canExtraGang(table, role) > 0) {
				list.add(OptionsType.EXTRA_GANG);
				LogUtil.info(table.getGame().getRoomId() + " EXTRA_GANG "
						+ role.getRole().getRole().getNick() + "补杠 "
						+ table.getLastMoPai());
			}
			if (canExtraFreeGang(table, role)) {
				list.add(OptionsType.EXTRA_FREE_GANG);
				LogUtil.info(table.getGame().getRoomId() + " EXTRA_FREE_GANG "
						+ role.getRole().getRole().getNick() + "补飞杠 "
						+ table.getLastMoPai());
			}
		}

		if (canHu(table, role, null)) {
			List<Integer> huFanType2 = dealFanType2(role, table);
			int FanNum = dealFanNum(huFanType2, table);
			list.add(OptionsType.ANNOUNCE_WIN);
			LogUtil.info(table.getGame().getRoomId() + " ANNOUNCE_WIN "
					+ role.getRole().getRole().getNick() + "摸胡 "
					+ table.getLastMoPai());

			role.setHuFan(FanNum);
		}

		table.getCanOptions().put(seat, list);

		GMsg_12041006.Builder builder3 = GMsg_12041006.newBuilder();
		// 读取麻将配置的操作时间
		int waitTime = table.getTurnDuration();// 等待第1轮时间
		int overTime = table.getTurnDuration();// 等待第2轮时间
		if (role.getTimeOutNum() < table.getOtpPunishment()) {
			overTime += table.getTurn2Duration();
		}

		// 读取麻将配置的操作时间

		if (list.contains(OptionsType.ANNOUNCE_WIN)) {
			if (role.getRole().isRobot()) {
				int interval = MathUtil.randomNumber(1000, 2000);
				tableToWait(table, seat, table.getNextPlaySeat(),
						HandleType.MAJIANG_HU, System.currentTimeMillis()
								+ interval);
			} else {
				if (role.getRole().isAuto()) {
					tableToWait(table, seat, table.getNextPlaySeat(),
							HandleType.MAJIANG_HU,
							System.currentTimeMillis() + 1 * 1000);
				} else {
					tableToWait(table, seat, table.getNextPlaySeat(),
							HandleType.MAJIANG_HU, System.currentTimeMillis()
									+ (overTime + 1) * 1000);
				}
			}

		} else {
			if (role.getRole().isAuto()) {
				if (role.getRole().isRobot()) {
					int interval = MathUtil.randomNumber(1000, 2000);
					if (list.contains(OptionsType.DARK_GANG)) {
						table.setWaitSeat(seat);
						tableToWait(table, seat, table.getNextPlaySeat(),
								HandleType.MAJIANG_AN_GANG,
								System.currentTimeMillis() + interval);
					} else if (list.contains(OptionsType.EXTRA_GANG)) {
						table.setWaitSeat(seat);
						tableToWait(table, seat, table.getNextPlaySeat(),
								HandleType.MAJIANG_EXTRA_GANG,
								System.currentTimeMillis() + interval);
					} else {

						tableToWait(table, seat, table.getNextPlaySeat(),
								HandleType.MAJIANG_OUT_PAI,
								System.currentTimeMillis() + interval);
					}
				} else {
					if (list.contains(OptionsType.DARK_GANG)) {
						tableToWait(table, seat, table.getNextPlaySeat(),
								HandleType.MAJIANG_AN_GANG,
								System.currentTimeMillis() + 2000,
								System.currentTimeMillis() + (overTime + 1)
										* 1000);
					} else {
						tableToWait(table, seat, table.getNextPlaySeat(),
								HandleType.MAJIANG_OUT_PAI,
								System.currentTimeMillis() + 1 * 1000);
					}
				}

			} else {
				tableToWait(table, seat, table.getNextPlaySeat(),
						HandleType.MAJIANG_OUT_PAI, System.currentTimeMillis()
								+ (overTime + 1) * 1000);
			}
			// wait = 7;
		}

		builder3.setAction(OptionsType.DISCARD_TILE);
		builder3.setCurrentSeat(seat);
		builder3.setWaitTime(TimeUtil.time() + waitTime);
		builder3.setOverTime(TimeUtil.time() + overTime);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder3.build());

		long rid = role.getRole().getRole().getRid();
		GMsg_12041007.Builder builder4 = GMsg_12041007.newBuilder();
		builder4.addAllOption(list);
		roleFunction.sendMessageToPlayer(rid, builder4.build());

	}

	/**
	 * 处理赢得返回结果 如果输的人黄金不够，直接扣除到0
	 * 
	 * @param role
	 * @param table
	 */
	public void dealWin(ZTMajiangRole role, ZTMaJongTable table,
			List<Integer> listHuTypeId) {
		// 第一个胡的标志
		boolean FirstWeek = false;
		Game game = table.getGame();
		FriendRoom friendRoom = new FriendRoom();
		if (game.isFriendRoom()) {
			friendRoom = friendRoomFunction.getFriendRoom(game.getRoomId());
		}
		if (table.getWinners().size() == 1) {
			FirstWeek = true;
		}

		GameRole winGameRole = role.getRole();
		Role winRole = winGameRole.getRole();

		int winTimes = dealFanNum(listHuTypeId, table);

		// 任务检测
		if (!winGameRole.isRobot()) {

			missionFunction.checkTaskFinish(winRole.getRid(),
					TaskType.daily_task, MissionType.WIN, GameType.ZXMAJIANG);
			missionFunction.checkTaskFinish(winRole.getRid(),
					TaskType.daily_task, MissionType.CONTINUE_WIN,
					GameType.ZXMAJIANG, true);
			missionFunction.checkTaskFinish(winRole.getRid(),
					TaskType.daily_task, MissionType.CARD_TYPE,
					GameType.ZXMAJIANG, listHuTypeId);

			// 奖券任务检测
			doLotteryTaskMission(winRole, listHuTypeId, winTimes);
		}

		// 自摸类型仅仅用于奖券任务
		Integer ziMoHuType = ZhenXiongWinType.ZX_ZI_MO_VALUE;
		listHuTypeId.remove(ziMoHuType);

		int diZhu;
		int gold;
		if (game.isFriendRoom() && friendRoom != null) {
			diZhu = friendRoom.getBaseChip();
		} else {
			diZhu = majiangRoomCache.getConfig(table.getGame().getRoomType())
					.getPotOdds();
		}
		gold = (int) Math.pow(2, winTimes) * diZhu; // 失去黄金 = Math.pow(2, 番数) *
													// 底注
		LogUtil.info("gold:" + gold + ",winTimes:" + winTimes + ",diZhu:"
				+ diZhu);

		GMsg_12041012.Builder goldBuilder = GMsg_12041012.newBuilder();
		int roomType = table.getGame().getRoomType();

		// 输家
		List<GameRole> lossGameRoleList = new ArrayList<GameRole>();
		if (role.getHuType() == -1) { // 自摸(自己加金币,其他人扣金币, 已经胡的不扣)
			List<ZTMajiangRole> ZTMajiangRoles = table.getMembers();
			for (ZTMajiangRole ztMajiangRole : ZTMajiangRoles) {
				GameRole gameRole2 = ztMajiangRole.getRole();
				int seat = gameRole2.getSeat();
				if (!table.getWinners().contains(seat)) { // 自己也在Winners列表里面
					lossGameRoleList.add(gameRole2);
				}
			}
		} else {
			List<ZTMajiangRole> members = table.getMembers();
			ZTMajiangRole ztRole = members.get(table.getLastOutPai() - 1);
			if (table.isQiangGangHu()) {
				ztRole = members.get(table.getBeiQiangGangHuSeat() - 1);
			}
			GameRole gameRole2 = ztRole.getRole();
			lossGameRoleList.add(gameRole2);
		}

		int goldActualTotal = 0;
		// 输家扣金币
		for (GameRole lossGameRole : lossGameRoleList) {
			Role lossRole = lossGameRole.getRole();
			int seat = lossGameRole.getSeat();
			int goldActualOne = gold;
			if (!table.getGame().isFriendRoom()) {
				if (lossRole.getGold() < gold) {
					goldActualOne = lossRole.getGold();
				}
			}
			goldActualTotal += goldActualOne;
			if (game.isFriendRoom() && friendRoom != null
					&& friendRoom.getSpriteMap().containsKey(lossRole.getRid())) {
				friendRoom.getSpriteMap().put(
						lossRole.getRid(),
						friendRoom.getSpriteMap().get(lossRole.getRid())
								- goldActualOne);
			} else {
				if (lossGameRole.isRobot()) {
					lossRole.setGold(lossRole.getGold() - goldActualOne);
					lossGameRole.setRobotLost();
					// 记录ai和玩家之间的盈亏情况
					if (!winGameRole.isRobot()) {
						npcFunction.updateGainOrLoss(lossRole.getRid(),
								-goldActualOne, table.getGame().getGameType(),
								table.getGame().getRoomType(),
								MoneyEvent.ZXMAJIANG);
					}
				} else {
					// 扣除金币
					roleFunction.goldSub(lossRole, goldActualOne,
							MoneyEvent.ZXMAJIANG, true);
					// 记录ai和玩家之间的盈亏情况
					if (winGameRole.isRobot()) {
						npcFunction.updateGainOrLoss(winRole.getRid(),
								goldActualOne, table.getGame().getGameType(),
								table.getGame().getRoomType(),
								MoneyEvent.ZXMAJIANG);
					}
				}
			}

			// 通知客户端飘字
			// GMajiangGold.Builder mgold = GMajiangGold.newBuilder();
			// mgold.setSeat(seat);
			// mgold.setGold(-goldActualOne);
			// goldBuilder.addChangeGold(mgold);
			// LogUtil.debug("飘字 seat:"+seat+"===金币:"+-goldActualOne);
			// 因为一炮多响的需求 先把飘字存到table里面统一发送
			GMajiangGold.Builder mgold2 = table.getPiaoZi().get(seat);
			if (mgold2 == null) {
				GMajiangGold.Builder mgold3 = GMajiangGold.newBuilder();
				mgold3.setSeat(seat);
				mgold3.setGold(-goldActualOne);
				table.getPiaoZi().put(seat, mgold3);
			} else {
				mgold2.setSeat(seat);
				mgold2.setGold(mgold2.getGold() - goldActualOne);
				table.getPiaoZi().put(seat, mgold2);
			}

			// 输家的流水清单列表
			GBillsInfo.Builder billsInfo = null;
			if (table.getBills().get(seat) != null) {
				billsInfo = table.getBills().get(seat);
			} else {
				billsInfo = GBillsInfo.newBuilder();
			}
			billsInfo.setRid(lossRole.getRid());
			billsInfo.setSeat(seat);
			billsInfo.setGold(billsInfo.getGold() - goldActualOne);
			// 输家具体的流水统计
			GDetailBillsInfo.Builder detailBillsInfo = GDetailBillsInfo
					.newBuilder();
			detailBillsInfo.setNick(winRole.getNick());// 输显示别人的名称
			detailBillsInfo.setWinTimes(winTimes);
			detailBillsInfo.addAllWinTypes(listHuTypeId);
			detailBillsInfo.setGoldDetail(-goldActualOne);
			billsInfo.addDetailBillsInfo(detailBillsInfo);
			table.getBills().put(seat, billsInfo);

			// 输家的麻将数据数据库记录
			majiangDataFunction.updateMajiangData(lossRole.getRid(), winTimes,
					goldActualOne, listHuTypeId, false, false);
		}

		// 经验
		ExpCsv csv = expCache.getConfig(winRole.getLevel() + 1);
		if (!table.getGame().isFriendRoom()) {
			roleFunction.expAdd(winRole, csv.getZhenxiongmajiangWinExp(), true);
		}
		int lotteryTotal = 0;
		if (game.isFriendRoom() && friendRoom != null
				&& friendRoom.getSpriteMap().containsKey(winRole.getRid())) {
			friendRoom.getSpriteMap().put(
					winRole.getRid(),
					friendRoom.getSpriteMap().get(winRole.getRid())
							+ goldActualTotal);
		} else {
			if (roomType == 1 || roomType == 2) { // 金币房规则：输家扣金币，赢家加金币
				if (winGameRole.isRobot()) {
					winRole.setGold(winRole.getGold() + goldActualTotal);
				} else {
					roleFunction.goldAdd(winRole, goldActualTotal,
							MoneyEvent.ZXMAJIANG, true);
				}
			} else { // 奖券房规则：输家扣金币，赢家加奖券
				int exchangeLottery = majiangRoomCache.getConfig(roomType)
						.getExchangeLottery();
				lotteryTotal = goldActualTotal * exchangeLottery / 10000;
				if (lotteryTotal == 0) {
					lotteryTotal = 1;
				}

				if (!winGameRole.isRobot()) { // 机器人不用加
					roleFunction.crystalAdd(winRole, lotteryTotal,
							MoneyEvent.ZXMAJIANG);
				}

				goldActualTotal = 0; // 重置为0，方便后面发消息飘字
			}
		}

		// 赢家的麻将数据数据库记录
		majiangDataFunction.updateMajiangData(winRole.getRid(), winTimes,
				goldActualTotal, listHuTypeId, FirstWeek, true);
		// 飘字
		// GMajiangGold.Builder mgold = GMajiangGold.newBuilder();
		// mgold.setSeat(winGameRole.getSeat());
		// mgold.setGold(goldActualTotal);
		// mgold.setLottery(lotteryTotal);
		// goldBuilder.addChangeGold(mgold);
		// LogUtil.debug("飘字 seat:"+winGameRole.getSeat()+"===金币:"+goldActualTotal+"===奖券:"+lotteryTotal);
		GMajiangGold.Builder mgold2 = table.getPiaoZi().get(
				winGameRole.getSeat());
		if (mgold2 == null) {
			GMajiangGold.Builder mgold3 = GMajiangGold.newBuilder();
			mgold3.setSeat(winGameRole.getSeat());
			mgold3.setGold(goldActualTotal);
			mgold3.setLottery(lotteryTotal);
			table.getPiaoZi().put(winGameRole.getSeat(), mgold3);
		} else {
			mgold2.setSeat(winGameRole.getSeat());
			mgold2.setGold(mgold2.getGold() + goldActualTotal);
			mgold2.setLottery(mgold2.getLottery() + lotteryTotal);
			table.getPiaoZi().put(winGameRole.getSeat(), mgold2);
		}

		// 发送出去
		// roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
		// goldBuilder.build());

		// 赢家清单
		GBillsInfo.Builder billsInfo = null;
		if (table.getBills().get(winGameRole.getSeat()) != null) {
			billsInfo = table.getBills().get(winGameRole.getSeat());
		} else {
			billsInfo = GBillsInfo.newBuilder();
		}
		billsInfo.setSeat(winGameRole.getSeat());
		billsInfo.setNick(winRole.getNick());
		billsInfo.setGold(billsInfo.getGold() + goldActualTotal);
		billsInfo.setLottery(billsInfo.getLottery() + lotteryTotal);
		billsInfo.setRid(winRole.getRid());
		table.getBills().put(winGameRole.getSeat(), billsInfo);
		// 赢家细节清单
		Role loseRole = null;
		if (lossGameRoleList.size() == 1) {// 胡一家的时候显示被胡人的昵称
			loseRole = lossGameRoleList.get(0).getRole();
		} else {
			loseRole = winRole;
		}
		GDetailBillsInfo.Builder detailBillsInfo = GDetailBillsInfo
				.newBuilder();
		detailBillsInfo.setNick(loseRole.getNick());
		detailBillsInfo.addAllWinTypes(listHuTypeId);
		detailBillsInfo.setWinTimes(winTimes);
		detailBillsInfo.setGoldDetail(goldActualTotal);
		detailBillsInfo.setLottery(lotteryTotal);
		billsInfo.addDetailBillsInfo(detailBillsInfo);
		table.getBills().put(winGameRole.getSeat(), billsInfo);

		if (winGameRole.isRobot()) { // 机器人概率离开
			winGameRole.setRobotWin();
			if (Math.random() < 0.7) {
				roomFunction.AIWantByeOrReady(table.getGame(),
						winRole.getRid(), HandleType.AI_END_READY);
			} else {
				roomFunction.AIWantByeOrReady(table.getGame(),
						winRole.getRid(), HandleType.AI_END_EXIT);
			}
		}
		for (Entry<Integer, GMajiangGold.Builder> entry : table.getPiaoZi()
				.entrySet()) {
			LogUtil.debug("table取出来的飘字:座位:" + entry.getValue().getSeat()
					+ "飘字:金币:" + entry.getValue().getGold() + "飘字:奖券:"
					+ entry.getValue().getLottery());
		}
	}

	/**
	 * 处理显示定缺
	 * 
	 * @param table
	 */
	public void dealShowDingQue(ZTMaJongTable table) {
		table.setQueueWaitType(0);

		for (int i = 0; i < table.getMembers().size(); i++) {
			ZTMajiangRole maJongMember = table.getMembers().get(i);
			if (maJongMember.getQueType() == 0) {
				GMsg_12041007.Builder waitBuilder = GMsg_12041007.newBuilder();
				waitBuilder.addOption(OptionsType.SET_QUE_TYPE);
				roleFunction.sendMessageToPlayer(maJongMember.getRole()
						.getRole().getRid(), waitBuilder.build());
			}
		}

		GMsg_12041006.Builder chuBuilder = GMsg_12041006.newBuilder();
		int waitTime = table.getTurnDuration();// 等待第1轮时间
		chuBuilder.setAction(OptionsType.SET_QUE_TYPE);
		chuBuilder.setCurrentSeat(0); // 所有人
		chuBuilder.setWaitTime(TimeUtil.time() + waitTime);
		chuBuilder.setOverTime(TimeUtil.time() + waitTime);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				chuBuilder.build());
		// 针对所有人的, 座位传0
		tableToWait(table, 0, 0, HandleType.MAJIANG_WAIT_DING_QUE_TYPE,
				System.currentTimeMillis() + (waitTime + 1) * 1000);

		// 机器人直接定缺
		for (int i = 0; i < table.getMembers().size(); i++) {
			ZTMajiangRole maJongMember = table.getMembers().get(i);
			if (maJongMember.getRole().isRobot()
					&& maJongMember.getQueType() == 0) {
				// 机器人直接定缺
				int que_type_index = ZXMahJongRule.choseQueType(ZXMahJongRule
						.conversionType(maJongMember.getPai()));
				maJongMember.setQueType(que_type_index + 1);

				GMsg_12041020.Builder builder = GMsg_12041020.newBuilder();
				builder.setSeat(maJongMember.getRole().getSeat());
				builder.setQuePaiType(que_type_index + 1);
				roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
						builder.build());
			}
		}
	}

	/**
	 * 设置 成员定缺
	 * 
	 * @param table
	 */
	public void setMemberQueType(ZTMaJongTable table) {
		table.setQueueWaitType(0);

		for (int i = 0; i < table.getMembers().size(); i++) {
			ZTMajiangRole maJongMember = table.getMembers().get(i);
			if (maJongMember.getQueType() == 0) {
				int que_type_index = ZXMahJongRule.choseQueType(ZXMahJongRule
						.conversionType(maJongMember.getPai()));
				maJongMember.setQueType(que_type_index + 1);

				GMsg_12041020.Builder builder = GMsg_12041020.newBuilder();
				builder.setSeat(maJongMember.getRole().getSeat());
				builder.setQuePaiType(que_type_index + 1);
				roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
						builder.build());
			}
		}

		LogUtil.info(table.getGame().getRoomId() + " 开始打麻将 等待4秒");
		tableToWait(table, table.getOwner(), table.getNextPlaySeat(),
				HandleType.MAJIANG_GET_PAI, System.currentTimeMillis() + 4000);
	}

	/**
	 * 处理 设置缺一门类型
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void setQueType(ZTMaJongTable table, ZTMajiangRole member,
			Integer que_type) {
		for (int i = 0; i < table.getMembers().size(); i++) {
			ZTMajiangRole maJongMember = table.getMembers().get(i);
			LogUtil.info(maJongMember.getRole().getRole().getNick() + "缺的牌型:"
					+ maJongMember.getQueType());
		}
		member.setQueType(que_type);

		GMsg_12041020.Builder builder = GMsg_12041020.newBuilder();
		builder.setSeat(member.getRole().getSeat());
		builder.setQuePaiType(que_type);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		// 检测是否所有人都设置了类型
		for (int i = 0; i < table.getMembers().size(); i++) {
			ZTMajiangRole maJongMember = table.getMembers().get(i);
			if (maJongMember.getQueType() == 0) {
				return;
			}
		}

		// 所有人都设置了，准备开始游戏
		table.setQueueWaitType(0);
		LogUtil.info(table.getGame().getRoomId() + " 开始打麻将 等待4秒");
		tableToWait(table, table.getOwner(), table.getNextPlaySeat(),
				HandleType.MAJIANG_GET_PAI, System.currentTimeMillis() + 4000);
	}

	/**
	 * 处理过
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealPass(ZTMaJongTable table, ZTMajiangRole member) {
		LogUtil.info("进入dealpass流程....");
		LogUtil.info("当前的邓赛队列为:" + table.getQueueWaitType());
		LogUtil.info("可操作列表：:" + table.getCanOptions());
		for (Entry<Integer, List<OptionsType>> entry : table.getCanOptions()
				.entrySet()) {
			LogUtil.info("座位:" + entry.getKey() + "==操作:" + entry.getValue());
		}
		if (table.getQueueWaitType() == HandleType.MAJIANG_OUT_PAI) {

		} else if (table.getQueueWaitType() == HandleType.MAJIANG_WAIT) {
			// if (table.getCanOptions().size() == 1) {
			table.setQueueWaitType(0);
			if (table.getGame().isFriendRoom()) {
				table.getCanOptions().clear();
				table.setLastPlaySeat(table.getLastOutPai());
				LogUtil.info(table
						+ " isfrendroom把lastplayseat设置为:"
						+ table.getLastOutPai()
						+ " 该座位的玩家昵称:"
						+ table.getMembers().get(table.getLastOutPai() - 1)
								.getRole().getRole().getNick());

			}
			tableToWait(table, table.getLastPlaySeat(),
					table.getNextPlaySeat(), HandleType.MAJIANG_WAIT,
					System.currentTimeMillis() + 10);

		} else if (table.getQueueWaitType() == HandleType.MAJIANG_HU
				|| table.getQueueWaitType() == HandleType.MAJIANG_MANY_HU) {
			LogUtil.info("等待队列是胡 或多人胡");
			List<GBaseMahJong.OptionsType> list = table.getCanOptions().get(
					member.getRole().getSeat());
			LogUtil.info("操作list的玩家座位:" + member.getRole().getSeat());
			LogUtil.info("操作list:" + list);
			if (list != null && list.contains(OptionsType.ANNOUNCE_WIN)) {
				table.setQueueWaitType(0);
				if (table.getLastRealSeat() == member.getRole().getSeat()) {
					// 自摸取消
					LogUtil.info("进入自摸取消流程...");
					tableToWait(table, table.getMoPai(),
							table.getNextPlaySeat(),
							HandleType.MAJIANG_OUT_PAI,
							System.currentTimeMillis() + 6000);
				} else {
					LogUtil.info("进入胡取消流程...");
					table.setLastPlaySeat(table.getLastOutPai());
					LogUtil.info(table
							+ " dealpass把lastplayseat设置为:"
							+ table.getLastOutPai()
							+ " 该座位的玩家昵称:"
							+ table.getMembers().get(table.getLastOutPai() - 1)
									.getRole().getRole().getNick());

					// table.setLastPlaySeat(table.getNextPlaySeat());
					tableToWait(table, table.getLastPlaySeat(),
							table.getNextPlaySeat(), HandleType.MAJIANG_WAIT,
							System.currentTimeMillis() + 50);
				}
			}
		}
		table.getCanOptions().clear();
		table.getReceiveQueue().clear();
		table.setManyOperate(false);
	}

	/**
	 * 处理暗杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealAnGang(ZTMaJongTable table, ZTMajiangRole role,
			Integer lastPai) {
		refreshPai(table, role);// 向客户端发送玩家的牌信息同步手牌
		table.setQueueWaitType(0);
		int seat = role.getRole().getSeat();

		List<Integer> memberShouPai = role.getPai();
		int[][] memberPai = ZXMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int yetNum = memberPai[lastPai / 10 - 1][lastPai % 10];
		if (yetNum == 4) {
			table.getYetOptions().put(role.getRole().getSeat(),
					OptionsType.EXPOSED_GANG);
		} else {
			LogUtil.error("处理杠 请求错误 条件检查不通过");
		}

		// 四张一样牌的暗杠
		List<Integer> rest = new ArrayList<Integer>();
		rest.add(lastPai);
		rest.add(lastPai);
		rest.add(lastPai);
		rest.add(lastPai);

		role.getShowPai().put(lastPai, rest);
		role.getPai().removeAll(rest);

		// Integer pai = table.getPais().remove(table.getPais().size() - 1);

		// 概率摸牌
		Integer pai = moPai(table, role);
		// pai = 12;
		// 概率摸牌

		role.getPai().add(pai);
		table.setMoPai(seat);
		table.setLastMoPai(pai);

		table.setLastRealSeat(seat);

		GPaiInfo.Builder info = GPaiInfo.newBuilder();
		info.addAllPai(rest);
		info.addPai(pai);

		GMsg_12041008.Builder builder = GMsg_12041008.newBuilder();
		builder.setNextSeat(seat);
		builder.setOption(OptionsType.DARK_GANG);
		builder.setTargetSeat(seat);
		builder.setOperatePai(info);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		LogUtil.info(table.getGame().getRoomId() + " seat " + seat + "杠 "
				+ lastPai);

		// 重置操作座位
		table.setLastPlaySeat(seat);
		LogUtil.info(table
				+ " dealangang把lastplayseat设置为:"
				+ seat
				+ " 该座位的玩家昵称:"
				+ table.getMembers().get(seat - 1).getRole().getRole()
						.getNick());

		table.getReceiveQueue().clear();
		// 取消所有人的弃牌/弃胡
		for (ZTMajiangRole role2 : table.getMembers()) {
			role2.setLastQiPai(0);
			role2.setHuFan(-1);
		}
		checkSelfOption(table, role);

		/*** Mission-16* 麻将牌型任务上报:杠 ***********************************/
		if (!role.getRole().isRobot()) {
			long rid = role.getRole().getRole().getRid();

			// 任务检测
			missionFunction.checkTaskFinish(rid, TaskType.daily_task,
					MissionType.GANG);
			LogUtil.debug(rid + " > gang .........");
		}

		/*
		 * List<OptionsType> list = new ArrayList<OptionsType>();
		 * list.add(OptionsType.DISCARD_TILE); table.getCanOptions().put(seat,
		 * list); // 直接出牌的流程 GMsg_12011006.Builder chuBuilder =
		 * GMsg_12011006.newBuilder();
		 * chuBuilder.setAction(OptionsType.DISCARD_TILE);
		 * chuBuilder.setCurrentSeat(seat);
		 * chuBuilder.setWaitTime(TimeUtil.time() + 12);
		 * roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
		 * chuBuilder.build());
		 * 
		 * GMsg_12011007.Builder builder4 = GMsg_12011007.newBuilder();
		 * builder4.addAllOption(list);
		 * roleFunction.sendMessageToPlayer(role.getRole().getRole().getRid(),
		 * builder4.build());
		 * 
		 * 
		 * if (role.getRole().isAuto()) { tableToWait(table, seat,
		 * table.getNextPlaySeat(), HandleType.MAJIANG_OUT_PAI,
		 * System.currentTimeMillis() + 3000); } else { tableToWait(table, seat,
		 * table.getNextPlaySeat(), HandleType.MAJIANG_OUT_PAI,
		 * System.currentTimeMillis() + 10000); }
		 */
	}

	/**
	 * 处理补杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealExtraGang(ZTMaJongTable table, ZTMajiangRole role,
			Integer lastPai) {
		refreshPai(table, role);// 向客户端发送玩家的牌信息同步手牌
		table.setQueueWaitType(0);
		int seat = role.getRole().getSeat();
		table.setLastRealSeat(seat);
		List<Integer> memberShouPai = role.getPai();
		int[][] memberPai = ZXMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int yetNum = memberPai[lastPai / 10 - 1][lastPai % 10];
		if (yetNum >= 1) {
			table.getYetOptions().put(role.getRole().getSeat(),
					OptionsType.EXTRA_GANG);
		} else {
			LogUtil.error("处理杠 请求错误 条件检查不通过");
		}
		List<Integer> data = role.getShowPai().get(lastPai);

		List<Integer> rest = new ArrayList<Integer>();
		rest.addAll(data);
		rest.add(lastPai);

		role.getShowPai().put(lastPai, rest);
		role.getPai().remove(lastPai);

		// 取消其他人的弃牌/弃胡
		for (ZTMajiangRole role2 : table.getMembers()) {
			role2.setLastQiPai(0);
			role2.setHuFan(-1);
		}

		// Integer pai = table.getPais().remove(table.getPais().size() - 1);
		refreshPai(table, role);// 向客户端发送玩家的牌信息同步手牌
		// 概率摸牌
		Integer pai = moPai(table, role);
		// 概率摸牌

		role.getPai().add(pai);
		table.setLastMoPai(pai);

		GPaiInfo.Builder info = GPaiInfo.newBuilder();
		info.addAllPai(rest);
		info.addPai(pai);

		GMsg_12041008.Builder builder = GMsg_12041008.newBuilder();
		builder.setNextSeat(seat);
		builder.setOption(OptionsType.EXTRA_GANG);
		builder.setTargetSeat(seat);
		builder.setOperatePai(info);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		LogUtil.info(table.getGame().getRoomId() + " seat " + seat + "补杠 "
				+ lastPai);

		// 重置操作座位
		table.setLastPlaySeat(seat);
		LogUtil.info(table
				+ " dealextragang把lastplayseat设置为:"
				+ seat
				+ " 该座位的玩家昵称:"
				+ table.getMembers().get(seat - 1).getRole().getRole()
						.getNick());

		table.resetHu();
		checkSelfOption(table, role);
		table.setQiangGangHu(false);
		table.setManyOperate(false);
		table.getReceiveQueue().clear();

		/*** Mission-16* 麻将牌型任务上报:杠 ***********************************/
		if (!role.getRole().isRobot()) {
			long rid = role.getRole().getRole().getRid();

			// 任务检测
			missionFunction.checkTaskFinish(rid, TaskType.daily_task,
					MissionType.GANG);
			LogUtil.debug(rid + " > gang .........");
		}

		/*
		 * List<OptionsType> list = new ArrayList<OptionsType>();
		 * list.add(OptionsType.DISCARD_TILE); table.getCanOptions().put(seat,
		 * list);
		 * 
		 * // 重置操作座位 table.setLastPlaySeat(seat); // 直接出牌的流程
		 * GMsg_12011006.Builder chuBuilder = GMsg_12011006.newBuilder();
		 * 
		 * // 读取麻将配置的操作时间 int waitTime = table.getTurnDuration();//等待第1轮时间 int
		 * overTime = table.getTurnDuration();//等待第2轮时间 if (role.getTimeOutNum()
		 * < table.getOtpPunishment()) { overTime += table.getTurn2Duration(); }
		 * 
		 * chuBuilder.setOverTime(TimeUtil.time()+overTime); // 读取麻将配置的操作时间
		 * 
		 * chuBuilder.setAction(OptionsType.DISCARD_TILE);
		 * chuBuilder.setCurrentSeat(seat);
		 * chuBuilder.setWaitTime(TimeUtil.time() + waitTime);
		 * roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
		 * chuBuilder.build());
		 * 
		 * GMsg_12011007.Builder builder4 = GMsg_12011007.newBuilder();
		 * builder4.addAllOption(list);
		 * roleFunction.sendMessageToPlayer(role.getRole().getRole().getRid(),
		 * builder4.build());
		 * 
		 * 
		 * if (role.getRole().isAuto()) { tableToWait(table, seat,
		 * table.getNextPlaySeat(), HandleType.MAJIANG_OUT_PAI,
		 * System.currentTimeMillis() + 3000,System.currentTimeMillis() +
		 * (overTime+1) * 1000); } else { tableToWait(table, seat,
		 * table.getNextPlaySeat(), HandleType.MAJIANG_OUT_PAI,
		 * System.currentTimeMillis() + (overTime+1)*1000); }
		 */
	}

	/**
	 * 处理飞补杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealExtraFreeGang(ZTMaJongTable table, ZTMajiangRole role,
			Integer lastPai) {
		table.setQueueWaitType(0);
	}

	static {
		// 低级房统计初始化,中级房统计初始化,高级房统计初始化
		for (int i = 1; i <= GameRoom.ZXMAJIANGROOMNUM; i++) {
			AtomicIntegerArray log = new AtomicIntegerArray(8);
			log.set(0, 0);// 对战数
			log.set(1, 0);// 破产数
			log.set(2, 0);// 房费数
			log.set(3, 0);// 庄家破产数
			log.set(4, 0);// 闲家破产数
			log.set(5, 0);// 普通破产数
			log.set(6, 0);// 困难破产数
			log.set(7, 0);// 抽水数
			roomCountMap.put(i, log);
		}
	}

	/**
	 * 根据房间号码拿到统计实体。由于出现并发获取，需要同步
	 * 
	 * @author G_T_C
	 * @param roomType
	 * @return
	 */
	private AtomicIntegerArray getRoomCountCacheByRoomType(int roomType) {
		// synchronized (this) {
		AtomicIntegerArray log = roomCountMap.get(roomType);
		/*
		 * if (log == null) { log = new AtomicIntegerArray(3); log.set(0, 0);//
		 * 对战数 log.set(1, 0);// 破产数 log.set(2, 0);// 抽水数
		 * roomCountMap.put(roomType, log); }
		 */
		return log;
		// }

	}

	public Map<Integer, AtomicIntegerArray> getRoomCountCache() {
		return roomCountMap;
	}

	/**
	 * 摸完牌之后 检查出哪张牌能听牌 以及听的牌的剩余数量 和胡的番数
	 * 
	 * @author xueshangyu
	 * @param table
	 */
	public void checkTingPai(ZTMaJongTable table, ZTMajiangRole role) {
		// 手牌
		List<Integer> memberShouPai = new ArrayList<Integer>();
		memberShouPai.addAll(role.getPai());
		LogUtil.info("玩家手牌: " + role.getPai());

		// 摆牌
		List<Integer> memberShowPai = new ArrayList<Integer>();
		Map<Integer, List<Integer>> showPai = role.getShowPai();
		for (Map.Entry<Integer, List<Integer>> entry : showPai.entrySet()) {
			memberShowPai.addAll(entry.getValue());
		}

		// 全牌
		List<Integer> allPai = new ArrayList<Integer>();
		allPai.addAll(memberShowPai);// 摆牌
		// 拿到玩家摸完牌后的手牌
		List<Integer> pais = new ArrayList<Integer>();
		pais.addAll(role.getPai());
		List<Integer> paistemp = new ArrayList<Integer>();// 遍历 打每一张手牌
		// 去掉手牌重复的数
		for (Integer pai : role.getPai()) {
			if (!paistemp.contains(pai)) {
				paistemp.add(pai);
			}
		}
		List<Integer> ting_pai = new ArrayList<Integer>();
		if (!role.getRole().isRobot()) {
			GMsg_12041013.Builder builder = GMsg_12041013.newBuilder();
			if (null != pais && pais.size() > 0) {
				// 遍历 出手牌上的每一张 判断能不能听牌 能听牌继续
				for (int i = 0; i < paistemp.size(); i++) {
					Integer temp = paistemp.get(i);
					pais.remove(temp);
					int[][] shuZuallPai = ZXMahJongRule.conversionType(pais);// 玩家手牌

					if (ZXMahJongRule
							.isQueYiMen(shuZuallPai, role.getQueType())) {
						ting_pai = ZXMahJongRule.tingPai(shuZuallPai,
								role.getShowPai());
						if (null != ting_pai && ting_pai.size() > 0) {
							for (int j = 0; j < ting_pai.size(); j++) {
								Integer pai_num = 0;
								Integer temp2 = ting_pai.get(j);

								pai_num = getCardNum(table, role, temp2);

								pais.add(temp2);
								allPai.addAll(pais);
								LogUtil.info("听牌 胡牌手牌:" + allPai);
								List<Integer> huFanTypeId = dealFanType(role,
										table, pais, memberShouPai, allPai,
										showPai);
								LogUtil.info("听牌 胡牌类型:" + huFanTypeId);
								int fan_shu = dealFanNum(huFanTypeId, table);
								LogUtil.info("听牌 胡牌番数:" + fan_shu);
								allPai.removeAll(pais);
								GTingPaiInfo.Builder tingPaiInfo = GTingPaiInfo
										.newBuilder();
								tingPaiInfo.setPai(temp);// 将要出的牌
								tingPaiInfo.setPaiNum(pai_num); // 听的牌剩余数
								tingPaiInfo.setFanShu(fan_shu); // 胡的番数
								tingPaiInfo.setTingPai(temp2);// 听的牌
								builder.addTingPaiInfo(tingPaiInfo);
								pais.remove(temp2);
							}
						}

					}
					pais.add(temp);
				}
				roleFunction.sendMessageToPlayer(role.getRole().getRole()
						.getRid(), builder.build());
			}
		}

	}

	/**
	 * 听牌的时候 返回听牌以及其剩余数量 和 胡的番数
	 * 
	 * @author xueshangyu
	 * @param table
	 * @param role
	 */
	public void tingPai(ZTMaJongTable table, ZTMajiangRole role) {

		// 手牌
		List<Integer> memberShouPai = new ArrayList<Integer>();
		memberShouPai.addAll(role.getPai());

		// 摆牌(要判断坨子)
		List<Integer> memberShowPai = new ArrayList<Integer>();
		Map<Integer, List<Integer>> showPai = role.getShowPai();
		for (Map.Entry<Integer, List<Integer>> entry : showPai.entrySet()) {
			List<Integer> listPai = entry.getValue();
			for (int j = 0; j < listPai.size(); j++) {
				memberShowPai.add(listPai.get(j));
			}
		}
		// 全牌
		List<Integer> allPai = new ArrayList<Integer>();
		allPai.addAll(memberShouPai);// 手牌
		allPai.addAll(memberShowPai);// 摆牌
		// 拿到玩家的手牌
		List<Integer> pais = new ArrayList<Integer>();
		pais.addAll(role.getPai());
		List<Integer> ting_pai = new ArrayList<Integer>();
		int[][] shuZuallPai = ZXMahJongRule.conversionType(pais);

		if (ZXMahJongRule.isQueYiMen(shuZuallPai, role.getQueType())) {
			ting_pai = ZXMahJongRule.tingPai(shuZuallPai, showPai);
			if (null != ting_pai && ting_pai.size() > 0) {
				GMsg_12041013.Builder builder = GMsg_12041013.newBuilder();
				for (int j = 0; j < ting_pai.size(); j++) {
					Integer pai_num = 0;
					Integer temp2 = ting_pai.get(j);
					pai_num = getCardNum(table, role, temp2);
					pais.add(temp2);
					List<Integer> huFanTypeId = dealFanType(role, table, pais,
							memberShouPai, allPai, showPai);
					int fan_shu = dealFanNum(huFanTypeId, table);
					GTingPaiInfo.Builder tingPaiInfo = GTingPaiInfo
							.newBuilder();
					tingPaiInfo.setPaiNum(pai_num); // 听的牌剩余数
					tingPaiInfo.setFanShu(fan_shu); // 胡的番数
					tingPaiInfo.setTingPai(temp2);// 听的牌
					builder.addTingPaiInfo(tingPaiInfo);
					pais.remove(temp2);
				}

				roleFunction.sendMessageToPlayer(role.getRole().getRole()
						.getRid(), builder.build());
			}
		}
	}

	// 玩家可见牌的剩余数量
	private int getCardNum(ZTMaJongTable table, ZTMajiangRole role, Integer pai) {
		int card_num = 0;
		List<Integer> allCard = new ArrayList<Integer>();
		// allCard.addAll(table.get);
		for (ZTMajiangRole member : table.getMembers()) {
			if (role.getRole().getSeat() != member.getRole().getSeat()) {
				if (table.getWinners().contains(member.getRole().getSeat())) {
					allCard.addAll(member.getPai());
					LogUtil.info(pai + " 0 allCard" + allCard);
					allCard.addAll(member.getRecyclePai());
					// LogUtil.info("薛 弃牌堆 ："+member.getRecyclePai());
					for (Map.Entry<Integer, List<Integer>> entry : member
							.getShowPai().entrySet()) {
						allCard.addAll(entry.getValue());
					}
				} else {
					allCard.addAll(member.getRecyclePai());
					for (Map.Entry<Integer, List<Integer>> entry : member
							.getShowPai().entrySet()) {
						allCard.addAll(entry.getValue());
					}
				}

				LogUtil.info("1 allCard" + allCard);
			} else {
				allCard.addAll(member.getPai());
				allCard.addAll(member.getRecyclePai());
				LogUtil.info("2 allCard" + allCard);
				for (Map.Entry<Integer, List<Integer>> entry : member
						.getShowPai().entrySet()) {
					allCard.addAll(entry.getValue());
					LogUtil.info("3 allCard" + allCard);
				}
			}
		}
		int[][] allPai = ZXMahJongRule.conversionType(allCard);
		// LogUtil.info("牌的二维数组");
		// for (int i = 0; i < allPai.length; i++) {
		// for (int j = 0; j < allPai[i].length; j++) {
		// System.out.print(allPai[i][j]);
		// }
		// System.out.println();
		// }
		card_num = 4 - allPai[pai / 10 - 1][pai % 10];
		// LogUtil.info("薛 "+pai +"的数量："+allPai[pai / 10 - 1][pai % 10]);
		LogUtil.info("card_num" + card_num);
		// if(card_num<0){
		// card_num = 0;
		// }
		// LogUtil.info("薛 剩余牌"+card_num);
		return card_num;

	}

	// 奖券任务检测
	private void doLotteryTaskMission(Role role, List<Integer> listTypeId,
			int dealFanNum) {
		if (dealFanNum >= 5) {
			missionFunction.checkLotteryTaskCompleted(role,
					LotteryTaskType.HU_GTE_5S, dealFanNum);
		}
		if (null != listTypeId && listTypeId.size() > 0) {
			for (Integer winType : listTypeId) {
				switch (winType) {
				case ZhenXiongWinType.ZX_ZI_MO_VALUE:// 自摸
					missionFunction.checkLotteryTaskCompleted(role,
							LotteryTaskType.ZIMO_1S, 1);
					break;
				case ZhenXiongWinType.ZX_PING_HU_VALUE:// 胡别人
					missionFunction.checkLotteryTaskCompleted(role,
							LotteryTaskType.HU_1S, 1);
					break;
				case ZhenXiongWinType.ZX_QUAN_QIU_DA_DUI_VALUE:// 全球大对
				case ZhenXiongWinType.ZX_DA_DUI_ZI_VALUE:// 大对子
					missionFunction.checkLotteryTaskCompleted(role,
							LotteryTaskType.DA_DUI_ZI, 1);
					break;
				case ZhenXiongWinType.ZX_QING_YI_SE_VALUE:// 清一色
					missionFunction.checkLotteryTaskCompleted(role,
							LotteryTaskType.QING_YI_SE, 1);
					break;
				case ZhenXiongWinType.ZX_QING_DA_DUI_ZI_VALUE:// 清大对子
					missionFunction.checkLotteryTaskCompleted(role,
							LotteryTaskType.QING_YI_SE, 1);
					missionFunction.checkLotteryTaskCompleted(role,
							LotteryTaskType.DA_DUI_ZI, 1);
					break;
				case ZhenXiongWinType.ZX_QIAO_QI_DUI_VALUE:// 巧七对
					missionFunction.checkLotteryTaskCompleted(role,
							LotteryTaskType.QIAO_QI_DUI, 1);
					break;
				case ZhenXiongWinType.ZX_QING_XIAO_DUI_VALUE:// 清小对子
					missionFunction.checkLotteryTaskCompleted(role,
							LotteryTaskType.QIAO_QI_DUI, 1);
					missionFunction.checkLotteryTaskCompleted(role,
							LotteryTaskType.QING_YI_SE, 1);
					break;
				case ZhenXiongWinType.ZX_QIAO_QI_DUI_ZIMO_VALUE:// 巧七对(自摸)
					missionFunction.checkLotteryTaskCompleted(role,
							LotteryTaskType.ZIMO_1S, 1);
					missionFunction.checkLotteryTaskCompleted(role,
							LotteryTaskType.QIAO_QI_DUI, 1);
					break;
				}
			}
		}
	}

	// 检测多人胡的时候 胡发送信息完了没有 或者 在一人胡或者没人胡的时候 判断一点玩家的操作是否存在比他优先级更高的操作
	public boolean checkWaitOrDeal(ZTMaJongTable table,
			Map<Integer, List<OptionsType>> canOperateYet) {
		LogUtil.info("进入检查流程....");
		LogUtil.info("table.getlastplayseat99：" + table.getLastPlaySeat());
		boolean isDeal = false;

		// for(Entry<Integer, List<OptionsType>> entry :
		// canOperateYet.entrySet()){
		// if (entry.getValue().contains(OptionsType.ANNOUNCE_WIN)) {
		// return isDeal;
		// }
		// }
		int canHuNum = 0;// 能胡的玩家数量
		int receviceHuNum = 0;// 收到胡的玩家数量
		Map<Integer, OptionsType> receviceOperate = table.getReceiveQueue();
		Map<Integer, List<OptionsType>> canOperate = table.getCanOptions();
		for (Entry<Integer, List<OptionsType>> entry : canOperate.entrySet()) {
			LogUtil.info("可操作列表:" + entry.getKey() + "===" + entry.getValue());
			if (entry.getValue().contains(OptionsType.ANNOUNCE_WIN)) {
				canHuNum++;
			}
		}
		for (Entry<Integer, OptionsType> entry : receviceOperate.entrySet()) {
			LogUtil.info("玩家操作列表:" + entry.getKey() + "===" + entry.getValue());
			if (entry.getValue() == OptionsType.ANNOUNCE_WIN) {
				receviceHuNum++;
			}
		}
		LogUtil.debug("能胡的数量:" + canHuNum);
		LogUtil.debug("收到胡的玩家数量:" + receviceHuNum);
		// 先判断指令到齐了没有
		if (receviceOperate.size() >= canOperate.size()) {
			isDeal = true;
			if (table.isQiangGangHu()) {
				if (receviceOperate.size() != canOperate.size() + 1) {
					isDeal = false;
				}
			}
		} else {
			// 判断是否多人胡
			if (canHuNum > 1) {
				// 再判断多人胡的情况胡到齐了没有
				if (canHuNum == receviceHuNum) {
					LogUtil.info("数据canhunum:" + canHuNum + "===收到的能胡数量:"
							+ table.getReceiveQueue().size());
					LogUtil.info("胡的数量到齐了:" + receviceHuNum);
					isDeal = true;
				}
			} else {// 一人胡
				// 最后判断一个人胡或者没人胡的时候的优先级顺序 如果已到达指令之前没有优先级更高的则返回true 否则返回false
				if (canOperate.size() == receviceOperate.size()) {
					isDeal = true;
				}
				Map<Integer, OptionsType> lastOptionsMap = new HashMap<Integer, GBaseMahJong.OptionsType>();// 最后一个操作
				for (Entry<Integer, OptionsType> entry : receviceOperate
						.entrySet()) {
					lastOptionsMap.clear();
					lastOptionsMap.put(entry.getKey(), entry.getValue());
					LogUtil.info("收到的最后一个操作:" + entry.getKey() + "==="
							+ entry.getValue());
				}
				// 先把可以飞碰的座位存到一个list 方便操作
				List<Integer> freePengSeat = new ArrayList<Integer>();
				for (Entry<Integer, List<OptionsType>> entry : canOperate
						.entrySet()) {
					if (entry.getValue().contains(OptionsType.FREE_PENG)) {
						freePengSeat.add(entry.getKey());
					}
				}
				LogUtil.debug("飞碰list:" + freePengSeat);
				// 对飞碰的操作按逆时针进行座位排序
				List<Integer> paiXuFreePengSeat = table.getYouXianJiSeat(
						freePengSeat, table.getLastOutPai());
				LogUtil.debug("有序飞碰list:" + paiXuFreePengSeat);
				// 遍历canOperate 对几种情况进行处理
				for (Entry<Integer, OptionsType> doneEntry : lastOptionsMap
						.entrySet()) {
					// 拿出receviceOperate的最后一个操作 和 canOperate里面的操作相比较
					for (Entry<Integer, List<OptionsType>> canEntry : canOperateYet
							.entrySet()) {

						if (doneEntry.getValue() == OptionsType.PASS) {
							continue;
						}
						// 把自己剔除
						if (doneEntry.getValue() == OptionsType.ANNOUNCE_WIN) {
							// 假如最后传过来的是胡 要
							isDeal = true;
						} else if (doneEntry.getValue() == OptionsType.EXPOSED_GANG) {
							if (!canEntry.getValue().contains(
									OptionsType.ANNOUNCE_WIN)) {
								LogUtil.debug("杠现在的优先级最高...");
								isDeal = true;
							}
						} else if (doneEntry.getValue() == OptionsType.FREE_EXPOSED_GANG) {
							if (!canEntry.getValue().contains(
									OptionsType.ANNOUNCE_WIN)) {
								LogUtil.debug("飞杠现在的优先级最高...");
								isDeal = true;
							}
						} else if (doneEntry.getValue() == OptionsType.PENG) {
							if (!canEntry.getValue().contains(
									OptionsType.ANNOUNCE_WIN)) {
								LogUtil.debug("碰现在的优先级最高...");
								isDeal = true;
							}
						} else if (doneEntry.getValue() == OptionsType.FREE_PENG) {
							// 加个分支判断都是飞碰时的座位优先队列
							if (!canEntry.getValue().contains(
									OptionsType.ANNOUNCE_WIN)
									&& !canEntry.getValue().contains(
											OptionsType.FREE_EXPOSED_GANG)
									&& !canEntry.getValue().contains(
											OptionsType.PENG)) {
								LogUtil.info("进入多个飞碰操作...");
								if (!canEntry.getValue().contains(
										OptionsType.FREE_PENG)) {
									LogUtil.debug("只有一个飞碰...飞碰现在的优先级最高...");
									isDeal = true;
								} else {// 在有多个飞碰的情况下 进行座位优先级判断
										// 如果已操作的飞碰是在 list的第一个
									if (doneEntry.getKey() == paiXuFreePengSeat
											.get(0)) {
										LogUtil.info("有多个飞碰...最高优先级的飞碰点了飞碰...");
										isDeal = true;
										break;
									} else {
										LogUtil.info("有多个飞碰...最高优先级的飞碰还没有操作...");
									}
								}
							}
						}
					}
				}
			}
		}
		return isDeal;
	}

	// 多人操作情况选择的思路: 把玩家发送过来的指令存到table.getrecevice的最后面
	// 然后把最后操作传到checkwaitordeal里
	// 判断是否可以立即执行 可以的时候把最后的玩家座位拿出来进行相应的操作 不可以的时候根据情况进行tabletowait或者不作处理
	public void doManyHand(ZTMaJongTable table) {
		// 存玩家能操作但是还没有操作的
		Map<Integer, List<OptionsType>> canOperateYet = new HashMap<Integer, List<OptionsType>>();
		canOperateYet.putAll(table.getCanOptions());
		for (Entry<Integer, List<OptionsType>> entry : table.getCanOptions()
				.entrySet()) {
			for (Entry<Integer, OptionsType> entry1 : table.getReceiveQueue()
					.entrySet()) {
				if (entry.getKey() == entry1.getKey()) {
					canOperateYet.remove(entry.getKey());
				}

			}
		}
		boolean haveHu = false;
		for (Entry<Integer, List<OptionsType>> entry : canOperateYet.entrySet()) {
			LogUtil.info("能操作但是还没有操作的:" + entry.getKey() + "=="
					+ entry.getValue());
			if (entry.getValue().contains(OptionsType.ANNOUNCE_WIN)) {
				haveHu = true;
			}
		}
		for (Entry<Integer, OptionsType> entry : table.getReceiveQueue()
				.entrySet()) {
			if (entry.getValue() == (OptionsType.ANNOUNCE_WIN)) {
				haveHu = true;
			}
		}
		if (!haveHu) {
			table.setLastPlaySeat(table.getLastOutPai());
			LogUtil.info(table
					+ " !havehu把lastplayseat设置为:"
					+ table.getLastOutPai()
					+ " 该座位的玩家昵称:"
					+ table.getMembers().get(table.getLastOutPai() - 1)
							.getRole().getRole().getNick());
			tableToWait(table, table.getLastPlaySeat(),
					table.getNextPlaySeat(), HandleType.MAJIANG_WAIT,
					table.getTargetTime());
			LogUtil.info("尚可操作列表和收到列表都没有胡的指令  进入等待摸牌状态...");
		}
		List<Integer> freePengSeat = new ArrayList<Integer>();
		for (Entry<Integer, OptionsType> entry : table.getReceiveQueue()
				.entrySet()) {
			if (entry.getValue() == OptionsType.FREE_PENG) {
				freePengSeat.add(entry.getKey());
			}
		}
		LogUtil.info("收到的飞碰list:" + freePengSeat);
		// 对飞碰的操作按逆时针进行座位排序
		List<Integer> paiXuFreePengSeat = table.getYouXianJiSeat(freePengSeat,
				table.getLastOutPai());
		LogUtil.info("收到的有序飞碰list:" + paiXuFreePengSeat);
		LogUtil.info("进入多人操作/.....");
		Map<Integer, OptionsType> receviceOperatetemp = table.getReceiveQueue();
		Map<Integer, OptionsType> receviceOperate = getPaiXuMap((LinkedHashMap<Integer, OptionsType>) receviceOperatetemp);
		Map<Integer, List<OptionsType>> canOptions = table.getCanOptions();
		// 收到的最后一个的操作
		Map<Integer, OptionsType> lastOptionsMap = new HashMap<Integer, GBaseMahJong.OptionsType>();// 最后一个操作
		int lastSeat = 0;// 还要处理收到的队列最后是pass的情况
		for (Entry<Integer, OptionsType> entry : receviceOperate.entrySet()) {
			if (entry.getValue() != OptionsType.PASS) {
				lastOptionsMap.clear();
				lastOptionsMap.put(entry.getKey(), entry.getValue());
				lastSeat = entry.getKey();
			}
		}
		LogUtil.info("最后一个玩家的操作:" + lastSeat + "===："
				+ lastOptionsMap.get(lastSeat));
		if (checkWaitOrDeal(table, canOperateYet)) {
			LogUtil.info("进入真正处理流程...");
			// 先考虑全过的
			int passNum = 0;
			for (Entry<Integer, OptionsType> entry : table.getReceiveQueue()
					.entrySet()) {
				if (entry.getValue() == OptionsType.PASS) {
					passNum++;
				}
			}
			OptionsType optionsType = receviceOperate.get(lastSeat);
			LogUtil.info("收到的优先级最高的操作:" + optionsType);
			LogUtil.info("最后操作的座位:" + lastSeat);
			LogUtil.info("是否为抢杠状态:" + table.isQiangGangHu());
			// 抢杠的时候 处理其他全过
			if (table.isQiangGangHu()) {
				// 取出补杠的类型
				if (passNum == table.getReceiveQueue().size() - 1) {
					OptionsType gangOptionsType = table.getReceiveQueue().get(
							table.getMoPai());
					LogUtil.info("抢杠进入处理杠流程...最后摸牌的座位:这里是被抢杠的座位:"
							+ table.getMoPai());
					ZTMajiangRole role = table.getMembers().get(
							table.getMoPai() - 1);
					int lastPai = table.getLastPai();
					LogUtil.info("被抢杠的牌的key:" + lastPai);
					if (gangOptionsType == OptionsType.EXTRA_GANG) {
						dealExtraGang(table, role, lastPai);
					} else if (gangOptionsType == OptionsType.EXTRA_FREE_GANG) {
						dealExtraFreeGang(table, role, lastPai);
					}
					table.setQiangGangHu(false);
				}
			}
			if (passNum == table.getReceiveQueue().size()) {
				LogUtil.info("table.getCanOptions().size() == 0 dealpass....");
				int seat = 0;
				for (Entry<Integer, List<OptionsType>> entry : table
						.getCanOptions().entrySet()) {
					if (entry.getValue().contains(optionsType.ANNOUNCE_WIN)) {
						seat = entry.getKey();
					}
					if (seat == 0) {
						seat = entry.getKey();
					}
				}

				LogUtil.info("dealpass的座位:" + seat);
				dealPass(table, table.getMembers().get(seat - 1));
			} else {
				LogUtil.info("table.getCanOptions().size() != 0 deal....");
				switch (optionsType.getNumber()) {
				case OptionsType.ANNOUNCE_WIN_VALUE:
					int huNum = 0;
					ZTMajiangRole[] ztMajiangRoles = new ZTMajiangRole[3];
					for (Entry<Integer, OptionsType> entry : receviceOperate
							.entrySet()) {
						if (entry.getValue() == OptionsType.ANNOUNCE_WIN) {
							ztMajiangRoles[huNum] = table.getMembers().get(
									entry.getKey() - 1);
							huNum++;
						}
					}
					LogUtil.info("胡的数量:" + huNum);
					if (huNum == 1) {
						LogUtil.info("table.getCanOptions().size() != 0 dealhu....");
						dealHu(table, table.getMembers().get(lastSeat - 1));
					} else {
						LogUtil.info("table.getCanOptions().size() != 0 dealmanyhu....");
						dealManyHu(table, ztMajiangRoles);
					}
					break;
				case OptionsType.EXPOSED_GANG_VALUE:
					LogUtil.info("table.getCanOptions().size() != 0 dealGang....");
					dealGang(table, table.getMembers().get(lastSeat - 1));
					break;
				case OptionsType.FREE_EXPOSED_GANG_VALUE:
					LogUtil.info("table.getCanOptions().size() != 0 dealFreeGang....");
					dealFreeGang(table, table.getMembers().get(lastSeat - 1));
					break;
				case OptionsType.PENG_VALUE:
					LogUtil.info("table.getCanOptions().size() != 0 dealPeng....");
					dealPeng(table, table.getMembers().get(lastSeat - 1));
					break;
				case OptionsType.FREE_PENG_VALUE:
					LogUtil.info("table.getCanOptions().size() != 0 dealFreePeng....");
					dealFreePeng(table,
							table.getMembers()
									.get(paiXuFreePengSeat.get(0) - 1));
					break;
				}
			}
		} else {
			LogUtil.info("检查没过....进入tabletowait流程...");
			boolean canWait = false;
			int canHuNoHuNum = 0;
			int canHuNum = 0;
			int canHuSeat = 0;
			for (Entry<Integer, List<OptionsType>> entry : canOptions
					.entrySet()) {
				// 没有胡才能tabletowait
				if (!entry.getValue().contains(OptionsType.ANNOUNCE_WIN)) {
					canWait = true;
					LogUtil.info("可操作对列里面有胡 不用进行tabletowait...");
				}
			}
			for (Entry<Integer, List<OptionsType>> entry : canOptions
					.entrySet()) {
				if (entry.getValue().contains(OptionsType.ANNOUNCE_WIN)) {
					canHuNum++;
					for (Entry<Integer, OptionsType> entry1 : table
							.getReceiveQueue().entrySet()) {
						if (entry.getKey() == entry1.getKey()
								&& entry1.getValue() != OptionsType.ANNOUNCE_WIN) {
							canHuNoHuNum++;
							LogUtil.info("canHuNoHuNum：" + canHuNoHuNum);
						} else {
							canHuSeat = entry.getKey();
							LogUtil.info("canhuseat：" + canHuSeat);
						}
					}
				}
			}
			if (canHuNum - canHuNoHuNum == 1) {
				LogUtil.info("等待一个人胡...座位:" + canHuSeat);
				tableToWait(table, canHuSeat, table.getNextPlaySeat(),
						HandleType.MAJIANG_HU, table.getTargetTime());
			}
			if (canWait) {// 最后的操作之前没有胡的操作要等待了 所以可以tabletowait相应的
				LogUtil.info("tabletowait：" + lastOptionsMap.get(lastSeat));
				int handleType = 0;
				if (lastOptionsMap.size() > 0) {
					switch (lastOptionsMap.get(lastSeat).getNumber()) {
					case OptionsType.ANNOUNCE_WIN_VALUE:
						handleType = HandleType.MAJIANG_HU;
						break;
					case OptionsType.EXPOSED_GANG_VALUE:
						handleType = HandleType.MAJIANG_GANG;
						break;
					case OptionsType.FREE_EXPOSED_GANG_VALUE:
						handleType = HandleType.MAJIANG_FREE_GANG;
						break;
					case OptionsType.PENG_VALUE:
						handleType = HandleType.MAJIANG_PENG;
						break;
					case OptionsType.FREE_PENG_VALUE:
						handleType = HandleType.MAJIANG_FREE_PENG;
						break;
					}
				}
				if (lastOptionsMap.get(lastSeat) != null
						&& lastOptionsMap.get(lastSeat) != OptionsType.PASS) {
					tableToWait(table, lastSeat, table.getNextPlaySeat(),
							handleType, table.getTargetTime());
					table.setWaitSeat(lastSeat);
					LogUtil.info("table.getWaitSeat()：" + table.getWaitSeat());
					LogUtil.info("table的等待转态:" + table.getQueueWaitType());
				}
			} else {// 最后的操作之前有胡的操作要等待 因为胡是默认tabletowait 所以不可以tabletowait相应的操作

			}
		}
	}

	public void dealQiangGangHu(ZTMaJongTable table, ZTMajiangRole role) {
		LogUtil.info("dealQiangGangHu里面最前...tabletowait的状态："
				+ table.getQueueWaitType() + "座位:" + table.getLastPlaySeat());
		boolean flag = true;
		boolean onlyRobot = true;
		table.getCanOptions().clear();
		table.getWaiter().clear();
		// table.getYetOptions().remove(role.getRole().getSeat());
		// List<OptionsType> beiQiangGang = new
		// ArrayList<GBaseMahJong.OptionsType>();
		// beiQiangGang.addAll(table.getCanOptions().get(role.getRole().getSeat()));
		// beiQiangGang.remove(OptionsType.DISCARD_TILE);
		// table.getCanOptions().put(role.getRole().getSeat(), beiQiangGang);
		Integer pai = table.getBuGangPai();
		for (ZTMajiangRole other : table.getMembers()) {
			if (other != role && other.getHuType() == 0) {
				table.getCanOptions().remove(other.getRole().getSeat());
				GMsg_12011007.Builder waitBuilder = GMsg_12011007.newBuilder();
				if (canHu(table, other, pai)) {
					waitBuilder.addOption(OptionsType.ANNOUNCE_WIN);
					LogUtil.info(table.getGame().getRoomId() + " ANNOUNCE_WIN "
							+ other.getRole().getRole().getNick() + "抢杠胡 "
							+ pai);
					table.setQiangGangHu(true);
					table.setBeiQiangGangHuSeat(role.getRole().getSeat());
					LogUtil.info("被抢杠胡的座位:" + role.getRole().getSeat()
							+ "抢杠胡的座位:" + other.getRole().getSeat());
				}
				if (waitBuilder.getOptionCount() > 0) {
					if (!other.getRole().isRobot()) {
						onlyRobot = false;
					}
					table.getCanOptions().put(other.getRole().getSeat(),
							waitBuilder.getOptionList());

					// 可进行操作
					roleFunction.sendMessageToPlayer(other.getRole().getRole()
							.getRid(), waitBuilder.build());
					LogUtil.info("dealqiangganghu玩家可以进行的操作:"
							+ waitBuilder.getOptionList() + ".."
							+ other.getRole().getRole().getNick());
					flag = false;
				}
			}
		}
		// 把table的状态设置为多人操作状态
		if (table.getCanOptions().size() > 1) {
			table.setManyOperate(true);
		}

		LogUtil.info("table是否为多人操作:" + table.isManyOperate());
		LogUtil.info("table是否为抢杠操作:" + table.isQiangGangHu());
		if (flag) {
			LogUtil.info("玩家在补杠的时候没有触发抢杠胡...");
			return;
		} else {

			// 优先级胡(AI手上有两张或以上癞子时 优先自摸)
			int huNum = 0;
			for (ZTMajiangRole other : table.getMembers()) {
				if (other != role && other.getHuType() == 0) {
					List<OptionsType> data = table.getCanOptions().get(
							other.getRole().getSeat());

					if (data != null && data.contains(OptionsType.ANNOUNCE_WIN)) {
						huNum++;
					}
				}
			}
			if (huNum >= 2) {
				LogUtil.info("一炮多响触发");
				tableToWait(table, role.getRole().getSeat(),
						table.getNextPlaySeat(), HandleType.MAJIANG_MANY_HU,
						System.currentTimeMillis()
								+ (table.getActionDuration() + 1) * 1000,
						System.currentTimeMillis()
								+ (table.getActionDuration() + 1) * 1000);
				return;
			}
			for (ZTMajiangRole other : table.getMembers()) {
				if (other != role && other.getHuType() == 0) {
					List<OptionsType> data = table.getCanOptions().get(
							other.getRole().getSeat());

					if (data != null && data.contains(OptionsType.ANNOUNCE_WIN)) {
						// 有别人操作
						GMsg_12011006.Builder builder3 = GMsg_12011006
								.newBuilder();
						if (other.getRole().isRobot()) {

							int interval = MathUtil.randomNumber(1000, 4000);
							int laizi = table.getLaiZiNum();
							int[][] allPai = ZTMahJongRule.conversionType(other
									.getPai());
							int num = allPai[laizi / 10 - 1][laizi % 10];
							if (num >= 2) {
								if (table.isManyOperate()) {
									table.getReceiveQueue().put(
											other.getRole().getSeat(),
											OptionsType.PASS);
									doManyHand(table);
								} else {
									tableToWait(table, table.getLastPlaySeat(),
											table.getNextPlaySeat(),
											HandleType.MAJIANG_WAIT,
											System.currentTimeMillis()
													+ interval);
									dealPass(table, other);
								}

							} else {
								if (table.isManyOperate()) {
									table.getReceiveQueue().put(
											other.getRole().getSeat(),
											OptionsType.ANNOUNCE_WIN);
									doManyHand(table);
								} else {
									tableToWait(table, other.getRole()
											.getSeat(),
											table.getNextPlaySeat(),
											HandleType.MAJIANG_HU,
											System.currentTimeMillis()
													+ interval);
								}

							}
						} else {
							if (other.getRole().isAuto()) {
								if (table.isManyOperate()) {
									table.getReceiveQueue().put(
											other.getRole().getSeat(),
											OptionsType.ANNOUNCE_WIN);
									LogUtil.info("机器人发送指令...hu");
									doManyHand(table);
								} else {
									int interval = MathUtil.randomNumber(1500,
											2500);
									tableToWait(table, other.getRole()
											.getSeat(),
											table.getNextPlaySeat(),
											HandleType.MAJIANG_HU,
											System.currentTimeMillis()
													+ interval);
								}

							} else {
								LogUtil.info("触发一个人胡");
								tableToWait(
										table,
										other.getRole().getSeat(),
										table.getNextPlaySeat(),
										HandleType.MAJIANG_HU,
										System.currentTimeMillis()
												+ (table.getActionDuration() + 1)
												* 1000,
										System.currentTimeMillis()
												+ (table.getActionDuration() + 1)
												* 1000);
							}
						}
						return;
					}
				}
			}
			// 通知等待别人操作
			GMsg_12011006.Builder builderWait = GMsg_12011006.newBuilder();
			LogUtil.info("dealQiangGangHu里面最后...tabletowait的状态："
					+ table.getQueueWaitType() + "座位:"
					+ table.getLastPlaySeat());
		}
	}

	// 对收到的指令进行优先级排序
	public LinkedHashMap<Integer, OptionsType> getPaiXuMap(
			LinkedHashMap<Integer, OptionsType> map) {
		LinkedHashMap<Integer, OptionsType> linkedHashMap = new LinkedHashMap<Integer, GBaseMahJong.OptionsType>();
		// boolean haveHu = false;
		for (Entry<Integer, OptionsType> entry : map.entrySet()) {
			if (entry.getValue() == OptionsType.FREE_PENG) {
				linkedHashMap.put(entry.getKey(), entry.getValue());
			}
		}
		for (Entry<Integer, OptionsType> entry : map.entrySet()) {
			if (entry.getValue() == OptionsType.PENG) {
				linkedHashMap.put(entry.getKey(), entry.getValue());
			}
		}
		for (Entry<Integer, OptionsType> entry : map.entrySet()) {
			if (entry.getValue() == OptionsType.FREE_EXPOSED_GANG) {
				linkedHashMap.put(entry.getKey(), entry.getValue());
			}
		}
		for (Entry<Integer, OptionsType> entry : map.entrySet()) {
			if (entry.getValue() == OptionsType.EXPOSED_GANG) {
				linkedHashMap.put(entry.getKey(), entry.getValue());
			}
		}
		for (Entry<Integer, OptionsType> entry : map.entrySet()) {
			if (entry.getValue() == OptionsType.ANNOUNCE_WIN) {
				linkedHashMap.put(entry.getKey(), entry.getValue());
			}
		}
		return linkedHashMap;
		// if (haveHu) {
		// return linkedHashMap;
		// } else {
		// return map;
		// }

	}

	// 得到玩家想要的测试牌
	public static Integer getCeShiPai(int t) {
		List<Integer> list = new ArrayList<Integer>();
		list.add(11);
		list.add(11);
		list.add(11);
		list.add(12);
		list.add(12);
		list.add(12);
		list.add(13);
		list.add(13);
		list.add(13);
		list.add(14);
		list.add(14);
		list.add(14);
		list.add(14);
		return list.get(t);
	}

	// 客户端刷新验证手牌
	public void refreshPai(ZTMaJongTable table, ZTMajiangRole role) {
		GMsg_12041022.Builder paiInfoList = GMsg_12041022.newBuilder();

		GPaiInfo.Builder shouPaiBuilder = GPaiInfo.newBuilder();
		shouPaiBuilder.addAllPai(role.getPai());// 玩家手牌
		GPaiInfo.Builder recyclePaiBuilder = GPaiInfo.newBuilder();
		recyclePaiBuilder.addAllPai(role.getRecyclePai());// 玩家出的牌

		paiInfoList.addPai(shouPaiBuilder);
		paiInfoList.addPai(recyclePaiBuilder);

		LogUtil.info("GMsg_12041022服务器向客户端发送玩家的牌信息.... 玩家昵称:"
				+ role.getRole().getRole().getNick() + " 玩家手牌:" + role.getPai()
				+ " 玩家打出的牌:" + role.getRecyclePai());
		for (Integer key : role.getShowPai().keySet()) {// 玩家摆牌
			GPaiInfo.Builder showPaiBuilder = GPaiInfo.newBuilder();
			shouPaiBuilder.addAllPai(role.getShowPai().get(key));
			paiInfoList.addPai(showPaiBuilder);
			LogUtil.info("玩家摆牌:" + role.getShowPai().get(key));
		}

		roleFunction.sendMessageToPlayer(role.getRole().getRole().getRid(),
				paiInfoList.build());
	}

	// 处理玩家在游戏中金币为零时的领取救济金和充值问题
	public void dealZeroGold(ZTMaJongTable table) {
		if (table.getGame().isFriendRoom()) {
			return;
		}
		List<Integer> members = new ArrayList<Integer>();
		members.addAll(table.getRemainMembers());
		for (Integer seat : members) {
			ZTMajiangRole ztMajiangRole = table.getMembers().get(seat - 1);
			if (ztMajiangRole.getRole().getRole().getGold() <= 0
					&& table.getRemainMembers().size() > 2) {
				table.getWinners().add(seat);
				roomFunction.quitRole(table.getGame(), ztMajiangRole.getRole()
						.getRole());
				table.getRemainMembers().remove(seat);
				LogUtil.info("剩余的玩家..座位.." + table.getRemainMembers());
				LogUtil.info("玩家被剔出牌桌..."
						+ ztMajiangRole.getRole().getRole().getNick());
				GMsg_12006014.Builder goldInfo = GMsg_12006014.newBuilder();
				goldInfo.setSeat(ztMajiangRole.getRole().getSeat());
				goldInfo.setStatus(2);
				roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
						goldInfo.build());
			}
		}
	}

	/*
	 * 检查玩家的金币是否为零 返回金币为零的玩家
	 */
	public void checkGoldZeroMember(ZTMaJongTable table) {
		if (table.getGame().isFriendRoom()) {
			return;
		}
		table.getPoChangRoles().clear();
		List<ZTMajiangRole> haveCharityList = new ArrayList<ZTMajiangRole>();
		List<ZTMajiangRole> noHaveCharityList = new ArrayList<ZTMajiangRole>();
		for (Integer seat : table.getRemainMembers()) {
			ZTMajiangRole ztMajiangRole = table.getMembers().get(seat - 1);
			if (ztMajiangRole.getRole().getRole().getGold() <= 0) {
				table.getPoChangRoles().add(ztMajiangRole);
				if (roleFunction.isHaveGoldResuceTimes(ztMajiangRole.getRole()
						.getRole().getGoldResuceTimes())) {
					LogUtil.info("玩家可以领取救济金.."
							+ ztMajiangRole.getRole().getRole().getNick());

					GMsg_12006014.Builder goldInfo = GMsg_12006014.newBuilder();
					goldInfo.setSeat(ztMajiangRole.getRole().getSeat());
					goldInfo.setStatus(1);
					roleFunction.sendMessageToPlayers(table.getGame()
							.getRoles(), goldInfo.build());
					haveCharityList.add(ztMajiangRole);
					table.getRechargePlayer().add(ztMajiangRole);
				} else {
					noHaveCharityList.add(ztMajiangRole);
				}
			}
		}
		LogUtil.info("破产list..." + table.getPoChangRoles());
		for (ZTMajiangRole role : table.getPoChangRoles()) {
			LogUtil.info("玩家昵称.." + role.getRole().getRole().getNick());
		}
		if (table.getPoChangRoles().size() <= 1) {
			if (noHaveCharityList.size() == 1) {
				LogUtil.info("破产数为一个人 没人能领取救济金...");
				kickPlayer(table, noHaveCharityList);
			} else if (haveCharityList.size() == 1) {
				tableToWait(table, table.getLastPlaySeat(),
						table.getNextPlaySeat(),
						HandleType.MAJIANG_KICK_PLAYER,
						System.currentTimeMillis() + 15000);
				LogUtil.info("一人领取救济金...");
			}
		} else {
			if (table.getPoChangRoles().size() == noHaveCharityList.size()) {
				kickPlayer(table, noHaveCharityList);
				LogUtil.info("多人破产并且没人可以领取救济金...");
			} else {
				tableToWait(table, table.getLastPlaySeat(),
						table.getNextPlaySeat(),
						HandleType.MAJIANG_KICK_PLAYER,
						System.currentTimeMillis() + 15000);
				LogUtil.info("多人领取救济金...");
			}

		}
	}

	//
	public void dealPay(Game game, Player player, int type) {
		if (game == null) {
			return;
		}
		int gameType = game.getGameType();
		if (gameType != GameType.ZXMAJIANG) {
			return;
		}
		ZTMaJongTable table = getTable(game.getRoomId());
		if (table == null) {
			return;
		}
		ZTMajiangRole role = getRole(player.getRole().getRid());
		//
		if (table.getRechargePlayer().contains(role)) {
			table.getRechargePlayer().remove(role);
			GMsg_12006014.Builder goldInfo = GMsg_12006014.newBuilder();
			goldInfo.setSeat(role.getRole().getSeat());
			goldInfo.setStatus(0);
			roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
					goldInfo.build());
		}
		LogUtil.info("table.getRechargePlayer().size()..."
				+ table.getRechargePlayer().size());
		if (table.getRechargePlayer().size() == 0) {// 可充值玩家为零时执行
			if (table.getQueueWaitType() == HandleType.MAJIANG_KICK_PLAYER) {
				kickPlayer(table, table.getPoChangRoles());
			}
		}
	}

	/*
	 * 向前端发送踢出玩家
	 */
	public void kickPlayer(ZTMaJongTable table, List<ZTMajiangRole> list) {
		if (list == null) {
			Slf4jLogUtil.warn("KickPlayer", "List<ZTMajiangRole> list is null");
			return;
		}
		for (ZTMajiangRole ztMajiangRole : list) {
			if (ztMajiangRole.getRole().getRole().getGold() <= 0) {
				GMsg_12006014.Builder goldInfo = GMsg_12006014.newBuilder();
				goldInfo.setSeat(ztMajiangRole.getRole().getSeat());
				goldInfo.setStatus(2);
				roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
						goldInfo.build());
				Integer seat = ztMajiangRole.getRole().getSeat();
				table.getWinners().add(seat);
				table.getRemainMembers().remove(seat);
//				exitTable(table.getGame(), ztMajiangRole.getRole().getRole()
//						.getRid(), false);
				LogUtil.info("玩家被剔出牌桌..."
						+ ztMajiangRole.getRole().getRole().getNick());
				LogUtil.info("剩余的玩家..座位.." + table.getRemainMembers());
				LogUtil.info("赢的或者离开的玩家..座位.." + table.getWinners());
			}
		}
		table.setLastPlaySeat(table.getNextPlaySeat());
		if (table.getPais().size() > 0) {
			if (table.getWinners().size() >= table.getMembers().size() - 1) {
				endTable(table);
			} else {
				LogUtil.info("轮到谁摸牌:" + table.getLastPlaySeat());
				tableToWait(table, table.getLastPlaySeat(),
						table.getNextPlaySeat(), HandleType.MAJIANG_GET_PAI,
						System.currentTimeMillis() + 1500);
			}
		} else {
			endTable(table);
		}
		// table.setGamePause(false);
	}

	// 输出所有玩家当前的手牌 出的牌和摆牌(输出日志查找bug专用)
	private void getOutPutAllPai(ZTMaJongTable table) {
		for (ZTMajiangRole zTMajiangRole : table.getMembers()) {
			Role role = zTMajiangRole.getRole().getRole();
			List<Integer> shouPai = new ArrayList<Integer>();
			shouPai.addAll(zTMajiangRole.getPai());
			Collections.sort(shouPai);
			LogUtil.info("当前牌桌信息....玩家昵称:" + role.getNick() + " 玩家手牌:"
					+ shouPai + " 玩家打出去的牌:" + zTMajiangRole.getRecyclePai());
			for (Integer key : zTMajiangRole.getShowPai().keySet()) {
				LogUtil.info("玩家摆牌:" + zTMajiangRole.getShowPai().get(key));
			}
		}

	}
}
