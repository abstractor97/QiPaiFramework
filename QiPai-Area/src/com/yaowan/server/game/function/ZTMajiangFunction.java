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

import javax.sound.midi.MidiDevice.Info;

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
import com.yaowan.csv.cache.MaJiangValueCache;
import com.yaowan.csv.cache.MajiangRoomCache;
import com.yaowan.csv.entity.DealProbabilityCsv;
import com.yaowan.csv.entity.ExpCsv;
import com.yaowan.csv.entity.MaJiangValueCsv;
import com.yaowan.csv.entity.MajiangRoomCsv;
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
import com.yaowan.protobuf.game.GBaseMahJong.GBillsInfo.Builder;
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
import com.yaowan.protobuf.game.GMahJong.GMsg_12011001;
import com.yaowan.protobuf.game.GMahJong.GMsg_12011002;
import com.yaowan.protobuf.game.GMahJong.GMsg_12011006;
import com.yaowan.protobuf.game.GMahJong.GMsg_12011007;
import com.yaowan.protobuf.game.GMahJong.GMsg_12011008;
import com.yaowan.protobuf.game.GMahJong.GMsg_12011009;
import com.yaowan.protobuf.game.GMahJong.GMsg_12011010;
import com.yaowan.protobuf.game.GMahJong.GMsg_12011012;
import com.yaowan.protobuf.game.GMahJong.GMsg_12011013;
import com.yaowan.protobuf.game.GMahJong.GMsg_12011014;
import com.yaowan.protobuf.game.GMahJong.GMsg_12011015;
import com.yaowan.protobuf.game.GMahJong.GMsg_12011016;
import com.yaowan.protobuf.game.GMahJong.WinType;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.model.data.entity.FriendRoom;
import com.yaowan.server.game.model.data.entity.Npc;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.dao.MajiangLogDao;
import com.yaowan.server.game.model.log.dao.RoleBrokeLogDao;
import com.yaowan.server.game.model.log.entity.MajiangLog;
import com.yaowan.server.game.model.struct.LotterTaskCreator;
import com.yaowan.server.game.model.struct.LotteryTaskType;
import com.yaowan.server.game.model.struct.MissionType;
import com.yaowan.server.game.model.struct.TaskType;
import com.yaowan.server.game.model.struct.ZTMaJongTable;
import com.yaowan.server.game.model.struct.ZTMajiangRole;
import com.yaowan.server.game.rule.ZTMahJongRule;

/**
 * 昭通麻将
 *
 * @author zane
 */
@Component
public class ZTMajiangFunction extends FunctionAdapter {

	@Autowired
	private NPCFunction npcFunction;

	@Autowired
	private SingleThreadManager manager;

	@Autowired
	private RoleFunction roleFunction;

	@Autowired
	private RoomFunction roomFunction;

	@Autowired
	private MajiangDataFunction majiangDataFunction;

	@Autowired
	private MajiangRoomCache majiangRoomCache;

	@Autowired
	private MaJiangValueCache maJiangValueCache;

	@Autowired
	private ExpCache expCache;

	@Autowired
	MissionFunction missionFunction;

	@Autowired
	private MajiangLogDao majiangLogDao;

	@Autowired
	private RoomLogFunction roomLogFunction;

	@Autowired
	private DealProbabilityCache maJiangProbabilityCache;

	@Autowired
	private LotterTaskCreator taskCreator;

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
		LogUtil.info("clear前 roleMap的大小:" + roleMap.size());
		ZTMaJongTable table = tableMap.remove(roomId);
		if (table != null) {
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
				LogUtil.info("role的rid:" + role.getRole().getRole().getRid()
						+ "role的状态5:" + role.getRole().getStatus());
				if (role.getRole() != null
						&& role.getRole().getStatus() == PlayerState.PS_EXIT_VALUE) {
					LogUtil.info("clear把麻将role从缓存里面移除....==============================="
							+ role.getRole().getRole().getRid());
					removeRoleCache(role.getRole().getRole().getRid());
					if (role.getRole().getRole().getRid() < 100000) {
						int i = 0;
						int j = 0;
					}
				}

			}
			LogUtil.info("clear后 roleMap的大小:" + roleMap.size());
			// for(Entry<Long, ZTMajiangRole> entry : roleMap.entrySet()){
			// LogUtil.info("endtable clear完后  rolemap的缓存:"+entry.getValue().getRole().getRole().getRid());
			// }
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
		// for(Entry<Long, ZTMajiangRole> entry : roleMap.entrySet()){
		// LogUtil.info(" rid："+entry.getKey()+"role："+entry.getValue());
		// }
		ZTMajiangRole majiangRole = roleMap.get(id);
		// ZTMaJongTable jongTable
		// =tableMap.entrySet().iterator().next().getValue();

		return majiangRole;
	}

	public void addRoleCache(ZTMajiangRole role) {
		LogUtil.info("把麻将role加入 缓存.." + role.getRole().getRole().getRid());
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
			} else if (friendRoom.isStart() == FriendRoomPayType.NO_START
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
						role.getRole().setGold(222222);
//						role.getRole().get
					} else if (role.getStatus() != PlayerState.PS_PREPARE_VALUE) {
						isRoleOK = false;
					}
				} else {
					// 准备时检测
					MajiangRoomCsv majiangRoomCsv = majiangRoomCache
							.getConfig(game.getRoomType());
					boolean flag = false;
					if (majiangRoomCsv.getEnterUpperLimit() == -1) {
						if (role.getRole().getGold() < majiangRoomCsv
								.getEnterLowerLimit()) {
							LogUtil.info("麻将的ai金币少于能进入房间的金币...");
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
							LogUtil.info("麻将AI时间到，走了");
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

								GMsg_12011001.Builder builder = GMsg_12011001
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

								GMsg_12011001.Builder builder = GMsg_12011001
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
		LogUtil.info("-------------------game num is "
				+ game.getSpriteMap().size());
		if (game.getSpriteMap().size() < 4) {
			isRoleOK = false;
		}

		for (GameRole role : game.getSpriteMap().values()) {
			LogUtil.info(role.getRole().getNick() + "=====" + role.getSeat());
		}
		LogUtil.info("game.getSpriteMap().size():" + game.getSpriteMap().size());
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
		if (table.getMembers().size() > 4) {
			roomFunction.endGame(game);
		}
		for(Entry<Long, ZTMaJongTable> entry: tableMap.entrySet()){
			LogUtil.info("table..."+ entry.getValue());
			for(ZTMajiangRole role : entry.getValue().getMembers()){
				LogUtil.info("全部 table.getMembers():"
						+ role.getRole().getRole().getNick() + "座位.."
						+ role.getRole().getSeat()+"身上的金币.."+role.getRole().getRole().getGold());
			}
		}
		for (ZTMajiangRole role : table.getMembers()) {
			LogUtil.info("table.getMembers():"
					+ role.getRole().getRole().getNick() + "座位.."
					+ role.getRole().getSeat());
		}

		if (table == null || !table.isInited()) {
			return;
		}
		table.getWaiter().clear();
		AtomicIntegerArray roomCountLog = getRoomCountCacheByRoomType(game
				.getRoomType());
		for (Map.Entry<Long, GameRole> entry : game.getSpriteMap().entrySet()) {
			GameRole role = entry.getValue();
			role.setStatus(PlayerState.PS_PREPARE_VALUE);

			// 李培光改
			GBillsInfo.Builder billsInfo = GBillsInfo.newBuilder();
			billsInfo.setRid(entry.getValue().getRole().getRid());
			billsInfo.setNick(entry.getValue().getRole().getNick());
			billsInfo.setSeat(entry.getValue().getSeat());
			billsInfo.setGold(0);
			billsInfo.setExp(0);
			table.getBills().put(entry.getValue().getSeat(), billsInfo);
			// 李培光改

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
							MoneyEvent.MAJIANG_TAX, true);
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

//		 Integer tuopai = null;
//		 for (Integer pai : initAllPai) {
//		
//		 if (pai == 24) {
//		 tuopai = pai;
//		 }
//		 }
		// 发牌 目前根据概率配 保留随机发牌
		faPai(table);
//		 faPaiTest(table);
		List<Integer> pais = table.getPais();
		// 发牌 目前根据概率配 保留随机发牌

		/*
		 * List<Integer> pais = table.getPais(); for (int i = 0; i < 13; i++) {
		 * for (int j = 0; j < table.getMembers().size(); j++) { ZTMajiangRole
		 * maJongMember = table.getMembers().get(j); Integer remove =
		 * pais.remove(0); maJongMember.getPai().add(remove); } }
		 */

		/*
		 * for (ZTMajiangRole maJongMember : table.getMembers()) {
		 * maJongMember.getPai().clear(); maJongMember.getPai().add(21);
		 * maJongMember.getPai().add(22); maJongMember.getPai().add(23);
		 * maJongMember.getPai().add(31); maJongMember.getPai().add(32);
		 * maJongMember.getPai().add(33); maJongMember.getPai().add(16);
		 * maJongMember.getPai().add(17); maJongMember.getPai().add(18);
		 * maJongMember.getPai().add(19); maJongMember.getPai().add(26);
		 * maJongMember.getPai().add(26); maJongMember.getPai().add(26); }
		 */

		int point1 = MathUtil.randomNumber(1, 6);
		int point2 = MathUtil.randomNumber(1, 6);

		int host = MathUtil.randomNumber(1, table.getMembers().size());
		table.setOwner(host);

		Integer tuopai = pais.remove(0);
		// pais.add(tuopai); 坨牌不需要了
//		 pais.remove(tuopai);
		table.setPoint1(point1);
		table.setPoint2(point2);

		table.setLaiZiNum((tuopai % 10) % 9 == 0 ? tuopai / 10 * 10 + 1
				: tuopai + 1);

		// 把坨子加入弃牌堆
		Integer seat = (host - 1) == 0 ? 4 : host - 1;
		table.getMembers().get(seat - 1).getRecyclePai().add(tuopai);

		if (game.isQuiting() || game.getSpriteMap().size() < 4) {// 有玩家操作退出游戏，则不允许运行游戏
			return;
		}

		// 开始游戏
		game.setStatus(GameStatus.RUNNING);

		game.setCount(game.getCount() + 1);
		LogUtil.info("这把牌局的癞子:" + table.getLaiZiNum());

		// 给Members排序
		Collections.sort(table.getMembers(), new Comparator<ZTMajiangRole>() {

			@Override
			public int compare(ZTMajiangRole o1, ZTMajiangRole o2) {

				return o1.getRole().getSeat() - o2.getRole().getSeat();
			}
		});

		for (ZTMajiangRole maJongMember : table.getMembers()) {
			// 设置缺牌
			maJongMember.setQueType(ZTMahJongRule.choseQueType(
					ZTMahJongRule.conversionType(maJongMember.getPai()),
					(table.getLaiZiNum())) + 1);

			GMsg_12011002.Builder builder = GMsg_12011002.newBuilder();
			GOpenInfo.Builder builder2 = GOpenInfo.newBuilder();
			GPaiInfo.Builder info = GPaiInfo.newBuilder();
			// 输出日志排序手牌专用
			info.addAllPai(maJongMember.getPai());
			List<Integer> paiii = new ArrayList<Integer>();
			paiii.addAll(maJongMember.getPai());
			Collections.sort(paiii);
			LogUtil.info(maJongMember.getRole().getRole().getNick()
					+ "maJongMember:" + paiii);

			builder2.setDeskRest(pais.size());
			builder2.setDicePoint1(point1);
			builder2.setDicePoint2(point2);
			builder2.setHostSeat(table.getOwner());
			builder2.setTuoPai(table.getLaiZiNum());
			builder2.setHandPai(info);
			builder.setOpenInfo(builder2);
			roleFunction.sendMessageToPlayer(maJongMember.getRole().getRole()
					.getRid(), builder.build());
			// 增加活跃度
			GameRole gameRole = maJongMember.getRole();
			if (gameRole != null) {
				Role role = gameRole.getRole();
				if (null != role && !gameRole.isRobot()) {
					role.setBureauCount(role.getBureauCount() + 1);
					role.markToUpdate("bureauCount");
				}
				/*
				 * if (role.getLotteryTaskId() != -1 && !gameRole.isRobot()) {
				 * int taskId = taskCreator.randomTaskId(GameType.MAJIANG);
				 * maJongMember.getRole().getRole().setLotteryTaskId(taskId);
				 * GMsg_12016003.Builder taskbuilder = GMsg_12016003
				 * .newBuilder(); taskbuilder.setTaskId(taskId);
				 * roleFunction.sendMessageToPlayer(maJongMember.getRole()
				 * .getRole().getRid(), taskbuilder.build()); }
				 */
			}
		}
		// table.getCanOptions().put(table.getOwner(),
		// OptionsType.ANNOUNCE_WIN);
		LogUtil.info("startTable数量" + table.getMembers().size());
		LogUtil.info(table.getGame().getRoomId() + " 开始打麻将 等待15秒");
		tableToWait(table, table.getOwner(), table.getNextPlaySeat(),
				HandleType.MAJIANG_GET_PAI, System.currentTimeMillis() + 15000);
		for (Integer seatTemp : table.getRemainMembers()) {
			LogUtil.info(table
					+ " 开始游戏时 当前桌子状态:座位:"
					+ seatTemp
					+ "玩家昵称:"
					+ table.getMembers().get(seatTemp - 1).getRole().getRole()
							.getNick()
					+ "玩家rid"
					+ table.getMembers().get(seatTemp - 1).getRole().getRole()
							.getRid());
		}
		for (ZTMajiangRole role : table.getMembers()) {
			LogUtil.info("table.getmember的位置顺序:" + role.getRole().getSeat()
					+ "昵称..." + role.getRole().getRole().getNick());
		}
	}

	// 测试时 发想要的手牌
	private void faPaiTest(ZTMaJongTable table) {
		List<Integer> initAllPai = table.getPais();
		// TODO Auto-generated method stub
		// List<ZTMajiangRole> paiXuMembers = new ArrayList<ZTMajiangRole>();
		// for(int i = 0 ; i < table.getMembers().size() ; i++){
		// if (!table.getMembers().get(i).getRole().isRobot()) {
		// paiXuMembers.add(table.getMembers().get(i));
		// }
		// }
		// for(int i = 0 ; i < table.getMembers().size() ; i++){
		// if (table.getMembers().get(i).getRole().isRobot()) {
		// paiXuMembers.add(table.getMembers().get(i));
		// }
		// }
		// table.getMembers().clear();
		// table.getMembers().addAll(paiXuMembers);
		Integer remove = null;
		for (int j = 0; j < table.getMembers().size(); j++) {
			ZTMajiangRole maJongMember = table.getMembers().get(j);
			if (table.getMembers().get(j).getRole().isRobot()) {
				continue;
			}
			if (!table.getMembers().get(j).getRole().isRobot()) {
				for (int i = 0; i < 13; i++) {
					remove = getCeShiPai(i);
					initAllPai.remove(remove);
					maJongMember.getPai().add(remove);
				}
			}
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

				// remove = getCeShiPai(i);
				// initAllPai.remove(remove);
				// maJongMember.getPai().add(remove);
			}
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
			// 1代表万 2代表筒 3代表筒
			Integer wang = 1;
			Integer tong = 2;
			Integer tiao = 3;

			int[][] majiang = ZTMahJongRule.conversionType(initAllPai);
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
					Map<Integer, Integer> bb = ZTMahJongRule
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
					}
				}
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
				LogUtil.info("万的数量剩余" + wangNum + "筒的数量剩余" + tongNum + "条的数量剩余"
						+ tiaoNum);
				LogUtil.info("手牌" + maJongMember.getPai());
				LogUtil.info("剩余牌" + initAllPai);
				LogUtil.info("剩余牌数量" + initAllPai.size());
			}
		}

	}

	public Integer moPai(ZTMaJongTable table, ZTMajiangRole ZTMajiangRole) {
		// 测听牌，根据听牌走不同的道路
		List<Integer> tingPai = new ArrayList<Integer>();
		int laiZiNum = table.getLaiZiNum();
		List<Integer> shouPai = ZTMajiangRole.getPai();
		if (shouPai.contains(laiZiNum)) {
			tingPai = ZTMahJongRule.tingPai2(shouPai, laiZiNum,
					ZTMajiangRole.getShowPai());
		} else {
			int[][] b = ZTMahJongRule.conversionType(shouPai);
			tingPai = ZTMahJongRule.tingPai(b, ZTMajiangRole.getShowPai());
		}
		List<Integer> pais = table.getPais();
		Integer remove = null;
		DealProbabilityCsv dealProbabilityCsv = maJiangProbabilityCache
				.getConfig(1);
		if (tingPai.size() > 0) {// 听牌
			List<Integer> huPais = new ArrayList<Integer>();
			List<Integer> noHuPais = new ArrayList<Integer>();
			int totalNum = 0;
			// 牌桌剩余牌
			int[][] leftPais = ZTMahJongRule.conversionType(pais);
			// 打印牌牌组的牌
//			for (int i = 0; i <= 3; i++) {
//				for (int j = 0; j <= 9; j++) {
//					System.out.print(leftPais[i][j] + " ");
//				}
//			}
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
			/*
			 * int pai=0; int num=0; for (int i = 1; i <= 3; i++) { for (int j =
			 * 1; j <= 9; j++) { pai = 10 * i + j; if(!huPais.contains(pai)){
			 * num=leftPais[i-1][j]; for (int k = 0; k < num; k++) {
			 * noHuPais.add(pai); } } } }
			 */
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
				remove = ZTMahJongRule.moPai(shouPai, pais);
				if (remove == 0) {
					remove = ZTMahJongRule.randomMoPai(shouPai, pais);
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
		ZTMajiangRole ztMajiangRole = new ZTMajiangRole(new GameRole(r, 1));
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
		ztMajiangRole.setPai(a);

		Map<Integer, List<Integer>> showPai = new HashMap<Integer, List<Integer>>();
		/*
		 * List<Integer> show1=new ArrayList<Integer>(); show1.add(11);
		 * show1.add(11); show1.add(11); showPai.put(11, show1); List<Integer>
		 * show2=new ArrayList<Integer>(); show1.add(15); show1.add(15);
		 * show1.add(15); showPai.put(15, show2);
		 */
		ztMajiangRole.setShowPai(showPai);

		boolean b = canHu(table, ztMajiangRole, 37);
//		System.out.println(b);
	}

	/**
	 * 
	 * 重新进入房间
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
				LogUtil.info("entertable里面的吧role加进缓存...============================="
						+ ztMajiangRole.getRole().getSeat());

				addRoleCache(ztMajiangRole);
				role.setStatus(PlayerState.PS_SEAT_VALUE);

				LogUtil.info("entertable 当前牌桌有几个人.."
						+ table.getMembers().size());
				for (ZTMajiangRole role2 : table.getMembers()) {
					LogUtil.info("昵称.." + role2.getRole().getRole().getNick()
							+ "座位.." + role2.getRole().getSeat());
				}

		
				for (ZTMajiangRole role2 : table.getMembers()) {
					LogUtil.info("昵称.." + role2.getRole().getRole().getNick()
							+ "座位.." + role2.getRole().getSeat());
				}
				if (table.getMembers().size() >= role.getSeat()) {
					table.getMembers().set(role.getSeat() - 1, ztMajiangRole);
					table.getRemainMembers().add(role.getSeat());
				} else {
					table.getMembers().add(role.getSeat() - 1, ztMajiangRole);
					table.getRemainMembers().add(role.getSeat());
				}
//				table.getMembers().set(role.getSeat() - 1, ztMajiangRole);
//				table.getRemainMembers().add(role.getSeat());
				for (ZTMajiangRole role2 : table.getMembers()) {
					LogUtil.info("昵称.." + role2.getRole().getRole().getNick()
							+ "座位.." + role2.getRole().getSeat());
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
				builder3.setIsOnline(1);
				builder3.setHead(role.getRole().getHead());
				builder3.setLevel(role.getRole().getLevel());
				builder3.setSeat(role.getSeat());
				builder3.setAvatarId(role.getAvatarId());
				builder3.setSex(role.getRole().getSex());
				msg.setRoleInfo(temp);
				List<Long> otherList = new ArrayList<Long>();
				otherList.addAll(game.getRoles());
				otherList.remove(role.getRole().getRid());
				roleFunction.sendMessageToPlayers(otherList, msg.build());
				LogUtil.info("enterTable getGameType" + otherList);

			} else {
				role.setStatus(PlayerState.PS_PLAY_VALUE);
				role.setAuto(false);
				ZTMajiangRole member = table.getMembers().get(
						role.getSeat() - 1);

				GMsg_12011010.Builder builder = GMsg_12011010.newBuilder();
				if (table.getWaitAction() == null) {
					table.setWaitAction(OptionsType.DISCARD_TILE);
				}
				List<Integer> YetOperateSeat = new ArrayList<Integer>();
				for (Integer seat : table.getReceiveQueue().keySet()) {
					YetOperateSeat.add(seat);
				}
				// 添加玩家可操作列表
				for (Integer seat : table.getCanOptions().keySet()) {
					LogUtil.info("座位 :" + seat + "操作列表:"
							+ table.getCanOptions().get(seat));
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
					GPaiInfo.Builder destPaiBuilder = GPaiInfo.newBuilder();
					destPaiBuilder.addAllPai(other.getRecyclePai());
					player.setDestPai(destPaiBuilder);
					player.setPaiNum(other.getPai().size());
					player.setSeat(other.getRole().getSeat());
					player.setTargetHu(0);
					if (other.getHuType() != 0) {
						GPaiInfo.Builder huPaiBuilder = GPaiInfo.newBuilder();
						huPaiBuilder.addAllPai(other.getPai());
						player.setHuPai(huPaiBuilder);

						if (other.getHuType() == -1) {
							// 自摸
							player.setTargetHu(0);
						} else {
							player.setTargetHu(1);
						}
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
				openBuilder.setTuoPai(table.getLaiZiNum());
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
		LogUtil.info("初始化麻将table...game的状态:" + game.getStatus());
		if (game.getStatus() > 0) {
			return null;
		}
		ZTMaJongTable mahJiangTable = new ZTMaJongTable(game);

		MajiangRoomCsv majiangRoomCsv = majiangRoomCache.getConfig(game
				.getRoomType());
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
		LogUtil.info("game.getSpriteMap().size()===================:"
				+ game.getSpriteMap().size());
		for (Entry<Long, GameRole> entry : game.getSpriteMap().entrySet()) {
			LogUtil.info("玩家顺序:" + entry.getValue().getRole().getNick() + "座位:"
					+ entry.getValue().getSeat());
		}
//		for (int i = 0; i < 4; i++) {
//			mahJiangTable.getMembers().add(null);
//		}
		for (Map.Entry<Long, GameRole> entry : game.getSpriteMap().entrySet()) {
			LogUtil.info("玩家座位:" + entry.getValue().getSeat() + " 昵称:"
					+ entry.getValue().getRole().getNick());
			ZTMajiangRole member = new ZTMajiangRole(entry.getValue());

			mahJiangTable.getMembers().add(member);
//			mahJiangTable.getMembers().set(entry.getValue().getSeat() - 1, member);
			mahJiangTable.getRemainMembers().add(member.getRole().getSeat());
			addRoleCache(member);

			LogUtil.info("inittable的member===================:"
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
		Collections.sort(mahJiangTable.getMembers(), new Comparator<ZTMajiangRole>() {

			@Override
			public int compare(ZTMajiangRole o1, ZTMajiangRole o2) {

				return o1.getRole().getSeat() - o2.getRole().getSeat();
			}
		});
		LogUtil.info("牌局剩余的玩家座位:" + mahJiangTable.getRemainMembers());
		for (ZTMajiangRole role : mahJiangTable.getMembers()) {
			if (role != null)
				LogUtil.info("table.getmember：role："
						+ role.getRole().getRole().getRid());
		}

		tableMap.put(game.getRoomId(), mahJiangTable);
		//
		game.setStatus(GameStatus.WAIT_READY);

		// tableToWait(mahJiangTable, 0, 0, HandleType.MAJIANG_INIT,
		// TimeUtil.time()+15000);
		return mahJiangTable;
	}

	/**
	 * 新的endtable 修改了旧的花猪和无听的结算
	 * 
	 * @param table
	 */
	public void endTable(ZTMaJongTable table) {
		// LogUtil.info("进入endtable流程...");
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
		GMsg_12011009.Builder endBuilder = GMsg_12011009.newBuilder();

		for (ZTMajiangRole role : table.getMembers()) {
			if (!table.getWinners().contains(role.getRole().getSeat())) {
				ExpCsv csv = expCache.getConfig(role.getRole().getRole()
						.getLevel() + 1);
				if (!table.getGame().isFriendRoom()) {
					roleFunction.expAdd(role.getRole().getRole(),
							csv.getMajiangLoseExp(), true);
				}
			}
			// 任务检测
			missionFunction.checkTaskFinish(role.getRole().getRole().getRid(),
					TaskType.daily_task, MissionType.TIMES, GameType.MAJIANG);
		}
		// 结算增加花猪和无听（判断所有的牌）
		List<Integer> huaZhuMemberList = new ArrayList<Integer>();
		List<Integer> noTingMemberList = new ArrayList<Integer>();
		List<Integer> tingMemberList = new ArrayList<Integer>();
		List<Integer> allPai = new ArrayList<Integer>();
		int laiZi = table.getLaiZiNum();
		for (ZTMajiangRole role : table.getMembers()) {
			if (!table.getWinners().contains(role.getRole().getSeat())) {// 赢家之外
				allPai.clear();
				allPai.addAll(role.getPai());

				// 优先花猪
				if (ZTMahJongRule.huaZhu(allPai, laiZi) == false) {
					huaZhuMemberList.add(role.getRole().getSeat());// 判断花猪
					continue;
				}

				List<Integer> pai = role.getPai();
				int[][] shouPai = ZTMahJongRule.conversionType(pai);
				if (role.getPai().contains(table.getLaiZiNum())) {// 判断有癞子的听牌
					if (ZTMahJongRule.tingPai2(role.getPai(),
							table.getLaiZiNum(), role.getShowPai()).size() > 0) {
						tingMemberList.add(role.getRole().getSeat());// 听牌
					} else {
						noTingMemberList.add(role.getRole().getSeat());// 不听
					}
				} else {// 判断没有癞子的听牌
					if (ZTMahJongRule.tingPai(shouPai, role.getShowPai())
							.size() > 0) {
						tingMemberList.add(role.getRole().getSeat());// 听牌
					} else {
						noTingMemberList.add(role.getRole().getSeat());// 不听
					}
				}
				if (!role.getRole().isRobot()) {
					realPlayerCount++;
				}

				// 连胜中断
				missionFunction.checkTaskFinish(role.getRole().getRole()
						.getRid(), TaskType.daily_task, MissionType.TIMES,
						GameType.MAJIANG);
			}
		}

		// 赔偿列表
		// key:赔偿玩家 ，value：赔给玩家列表, value(0)压的是赔偿番数
		HashMap<Integer, List<Integer>> peiMemberMap = new HashMap<Integer, List<Integer>>();

		// 花猪赔给不花猪 列表
		List<Integer> noHuaZhuList = new ArrayList<Integer>();
		noHuaZhuList.add(0, WinType.HU_AZHU_VALUE);
		noHuaZhuList.addAll(noTingMemberList);
		noHuaZhuList.addAll(tingMemberList);
		for (Integer huaZhuSeat : huaZhuMemberList) {
			peiMemberMap.put(huaZhuSeat, noHuaZhuList);
		}

		// 不听的赔给听 列表
		tingMemberList.add(0, WinType.NO_TING_VALUE);
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
			MaJiangValueCsv maJiangValueCsv = maJiangValueCache
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
			if (!srcGameRole.isRobot()
					&& roleFunction.isPoChan(srcRole.getGold(),
							srcRole.getGoldPot(), totalLoseGold)) {
				AtomicIntegerArray log = getRoomCountCacheByRoomType(table
						.getGame().getRoomType());
				log.incrementAndGet(1);// 破产数
				roleBrokeLogDao.insertLog(srcRole.getRid(), table.getGame()
						.getGameType(), table.getGame().getRoomType());
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
											.getRoomType(), MoneyEvent.MAJIANG);
						}
					} else {
						roleFunction.goldSub(srcRole, actualLoseGold,
								MoneyEvent.MAJIANG, true);
						// 记录ai和玩家之间的盈亏情况
						if (dstGameRole.isRobot()) {
							npcFunction.updateGainOrLoss(dstRole.getRid(),
									actualLoseGold, table.getGame()
											.getGameType(), table.getGame()
											.getRoomType(), MoneyEvent.MAJIANG);
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
									MoneyEvent.MAJIANG, true);
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
									MoneyEvent.MAJIANG);
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
				value.addWinTypes(WinType.NO_WINNER_VALUE);
				billsInfo.addDetailBillsInfo(value);
				billsInfo.setGold(0);
			}
			if (billsInfo.getNick() == null) {
				billsInfo.setNick("");
			}
			LogUtil.info("endTable1 胡的黄金" + billsInfo.getNick()
					+ billsInfo.getGold() + "座位：" + billsInfo.getSeat()
					+ "详细列表:");
			for (GDetailBillsInfo detail : billsInfo.getDetailBillsInfoList()) {
				LogUtil.info("玩家昵称:" + detail.getNick() + "输赢的金币:"
						+ detail.getGoldDetail());
			}

			endBuilder.addBills(billsInfo);
			// for(endBuilder.getBillsList());
		}
		// 广播房间所有
		LogUtil.info("table.getGame().getRoles() ："
				+ table.getGame().getRoles());
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
		MajiangLog majiangLog = majiangLogDao.getMajiangLog(table.getGame(),
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
		for (Entry<Integer, GBillsInfo.Builder> entry : table.getBills()
				.entrySet()) {
			LogUtil.info("getBills:" + entry.getKey());
			LogUtil.info("getBills:" + entry.getValue().getGold());
			LogUtil.info("getBills:" + entry.getValue().getNick());
		}
		if (table.getGame().isFriendRoom()) {
			friendRoomFunction.roundEnd(table.getGame().getRoomId());
		}
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
				game.getSpriteMap().remove(rid);
				LogUtil.info(gameRole.getRole().getNick()
						+ " exittable把麻将role从缓存里面移除...." + rid);
				removeRoleCache(rid);
				if (sendMsg) {
					GMsg_12006005.Builder builder = GMsg_12006005.newBuilder();
					builder.setCurrentSeat(gameRole.getSeat());
					roleFunction.sendMessageToPlayers(rids, builder.build());
				}
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
			}
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
		// LogUtil.info(table+":tabletowait把lastplayseat设置为:"+targetSeat+" 该座位的玩家昵称:"+table.getMembers().get(targetSeat
		// - 1).getRole().getRole().getNick()+"等待转态:"+handleType);
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
				 * table.setQueueWaitType(HandleType.MAJIANG_GET_PAI); //
				 * table.setCoolDownTime(System.currentTimeMillis() + // 1000);
				 * } }
				 */
				continue;
			}
			// if (table.getQueueWaitType() != HandleType.MAJIANG_WAIT) {
			// continue;
			// }
			if (table.getGame().isFriendRoom()
					&& (table.getQueueWaitType() != HandleType.MAJIANG_GET_PAI && table
							.getQueueWaitType() != HandleType.MAJIANG_WAIT)) {
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
							try {
								role = table.getMembers().get(
										table.getLastPlaySeat() - 1);
							} catch (Exception e) {
								StringBuilder builder = new StringBuilder();
								builder.append(
										"getLastPlaySeat:"
												+ table.getLastPlaySeat())
										.append("\r\n");
								builder.append(
										"membersize:"
												+ table.getMembers().size())
										.append("\r\n");
								for (ZTMajiangRole role2 : table.getMembers()) {
									builder.append("role : ")
											.append(role2.getRole().getRole()
													.getNick()).append("\r\n");
								}
								LogUtil.error(builder.toString());
							}

						}

						Event event = new Event(tableState, table, role);
						DispatchEvent.dispacthEvent(event);
					} catch (Exception e) {
						table.setQueueWaitType(tableState);
						table.setCoolDownTime(currentTimeMillis + 5000);
						LogUtil.error("麻将流程处理异常ZTMajiang:"
								+ ExceptionUtils.getStackTrace(e));
						LogUtil.error(ExceptionUtils.getStackFrames(e));
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
		LogUtil.info("dealdiscard...当前出牌玩家 table.getLastPlaySeat()："
				+ table.getLastPlaySeat());
		for (Integer seatTemp : table.getRemainMembers()) {
			LogUtil.info("当前桌子状态:座位:"
					+ seatTemp
					+ "玩家昵称:"
					+ table.getMembers().get(seatTemp - 1).getRole().getRole()
							.getNick());
		}
		ZTMajiangRole role = table.getMembers().get(seat - 1);
		if (!role.getRole().isRobot()) {
			LogUtil.info("！！！麻将玩家" + role.getRole().getRole().getNick()
					+ " 正在出牌！！！");
			LogUtil.debug("出牌之前 玩家手牌:" + role.getPai());
			for (Entry<Integer, List<Integer>> entry : role.getShowPai()
					.entrySet()) {
				LogUtil.debug("出牌之前 玩家摆牌:" + entry.getValue());
			}
		}
		if (!role.getPai().contains(pai)) {
			LogUtil.info("！！！麻将玩家" + role.getRole().getRole().getNick()
					+ " 没有这张牌！！！" + pai);
			pai = role.getPai().get(role.getPai().size() - 1);
			pai = ZTMahJongRule.chosePai(role.getPai(), pai,
					table.getLaiZiNum());
			LogUtil.info("前端出现牌的变化 客户端要打一张没有的牌  服务器根据出牌规则帮玩家出一张....出的牌:" + pai);
		}

		// 判断是否延时出牌（出牌倒计时）
		/*
		 * long a=table.getCoolDownTime();//倒计时结束的时间戳 long
		 * b=TimeUtil.millisTime();//当前时间 if (a-b<table.getTurnDuration() *
		 * 1000) { role.setTimeOutNum(role.getTimeOutNum() + 1); }
		 */
		// （李培光）

		// pai = 18;
		LogUtil.info(table.getGame().getRoomId() + " seat-" + seat + " pai"
				+ pai);

		table.setLastOutPai(seat);

		table.setLastRealSeat(seat);

		role.getPai().remove(pai);
		table.setLastPai(pai);
		role.getRecyclePai().add(pai);

		GPaiInfo.Builder info = GPaiInfo.newBuilder();
		info.addPai(pai);
		GMsg_12011008.Builder builder = GMsg_12011008.newBuilder();
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

				GMsg_12011007.Builder waitBuilder = GMsg_12011007.newBuilder();

				if (table.getPais().size() > 0) {
					if (canGang(table, other)) {

						waitBuilder.addOption(OptionsType.EXPOSED_GANG);
						LogUtil.info(table.getGame().getRoomId()
								+ " EXPOSED_GANG "
								+ other.getRole().getRole().getNick() + "明杠 "
								+ pai);
					}
					if (canFreeGang(table, other)) {
						waitBuilder.addOption(OptionsType.FREE_EXPOSED_GANG);
						LogUtil.info(table.getGame().getRoomId()
								+ " FREE_EXPOSED_GANG "
								+ other.getRole().getRole().getNick() + "飞杠 "
								+ pai);
					}
					if (canPeng(table, other)) {
						waitBuilder.addOption(OptionsType.PENG);
						LogUtil.info(table.getGame().getRoomId() + " PENG "
								+ other.getRole().getRole().getNick() + "碰 "
								+ pai);
					}

					if (canFreePeng(table, other)) {
						waitBuilder.addOption(OptionsType.FREE_PENG);
						LogUtil.info(table.getGame().getRoomId()
								+ " FREE_PENG "
								+ other.getRole().getRole().getNick() + "飞碰 "
								+ pai);
					}
				}

				if (canHu(table, other, table.getLastPai())) {
					List<Integer> huFanType2 = dealFanType2(other, table);
					int FanNum = dealFanNum(huFanType2, table);
					if (other.getHuFan() < FanNum) {
						waitBuilder.addOption(OptionsType.ANNOUNCE_WIN);
						LogUtil.info(table.getGame().getRoomId()
								+ other.getRole().getSeat() + "=="
								+ " ANNOUNCE_WIN "
								+ other.getRole().getRole().getNick() + "胡 "
								+ pai);
						other.setHuFan(FanNum);
					} else {

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
					flag = false;
					LogUtil.info("出牌时 玩家可以进行的操作:" + waitBuilder.getOptionList()
							+ ".." + other.getRole().getRole().getNick());
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
			LogUtil.info("当前出牌的玩家:" + table.getLastPlaySeat());
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
						GMsg_12011006.Builder builder3 = GMsg_12011006
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
								}
								// else if
								// (data.contains(OptionsType.FREE_PENG)) {
								// if (table.isManyOperate()) {
								// table.getReceiveQueue().put(
								// other.getRole().getSeat(),
								// OptionsType.PASS);
								// LogUtil.info("飞碰时机器人发送指令...pass");
								// doManyHand(table);
								// }
								// }else if
								// (data.contains(OptionsType.FREE_EXPOSED_GANG))
								// {
								// if (table.isManyOperate()) {
								// table.getReceiveQueue().put(
								// other.getRole().getSeat(),
								// OptionsType.PASS);
								// LogUtil.info("飞杠时机器人发送指令...pass");
								// doManyHand(table);
								// }
								// }
								else {
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
									if (table.isManyOperate()) {
										table.getReceiveQueue().put(
												other.getRole().getSeat(),
												OptionsType.PASS);
										doManyHand(table);
									} else {
										dealPass(table, other);
									}

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
											* 1000, System.currentTimeMillis()
											+ (table.getActionDuration() + 1)
											* 1000);
						}
					}
				}
			}
			// 通知等待别人操作
			GMsg_12011006.Builder builderWait = GMsg_12011006.newBuilder();
			table.getReceiveQueue().clear();
		}
		LogUtil.info("当前table的状态  table.getQueueWaitType():"
				+ table.getQueueWaitType());
		getOutPutAllPai(table);
	}

	/**
	 * 通知操作结果并提交下一操作
	 */
	public void processAction(ZTMaJongTable table, OptionsType action,
			int nextSeat, int handleType, long time) {
		GMsg_12011008.Builder builder = GMsg_12011008.newBuilder();
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
		LogUtil.info(table
				+ " processAction把lastplayseat设置为:"
				+ nextSeat
				+ " 该座位的玩家昵称:"
				+ table.getMembers().get(nextSeat - 1).getRole().getRole()
						.getNick());
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
		int[][] memberPai = ZTMahJongRule.conversionType(memberShouPai);
		if (!ZTMahJongRule.isQueYiMen(memberPai, role.getShowPai(),
				table.getLaiZiNum())) {
			return false;
		}
		int num = memberPai[table.getLaiZiNum() / 10 - 1][table.getLaiZiNum() % 10];
		boolean huPai = false;
		if (num > 0) {
			huPai = ZTMahJongRule.huLaizi(memberShouPai, table.getLaiZiNum(),
					role.getShowPai()).size() > 0;
		} else {
			huPai = ZTMahJongRule.fitHu(memberPai, role.getShowPai());
		}
		// LogUtil.info(memberShouPai + "data:"
		// + role.getRole().getRole().getNick() + "canHu" + memberShouPai);
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
		List<Integer> memberShouPai = member.getPai();
		int[][] memberPai = ZTMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int yetNum = memberPai[lastPai / 10 - 1][lastPai % 10];
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
		int[][] memberPai = ZTMahJongRule.conversionType(memberShouPai);
		for (int i = 0; i < 3; i++) {
			for (int j = 1; j < 10; j++) {
				int pai = 10 * (i + 1) + j;
				if (pai == table.getLaiZiNum()) {
					continue;
				}
				int count = memberPai[i][j];
				if (count > 3) {
					return (i + 1) * 10 + j;
				}/*
				 * else if (count == 3 && memberPai[table.getLaiZiNum() / 10 -
				 * 1][table.getLaiZiNum() % 10] >= 1){ //有癞子牌的暗杠 return (i + 1)
				 * * 10 + j; }
				 */
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
		if (lastPai == table.getLaiZiNum()) {
			return false;
		}
		List<Integer> memberShouPai = member.getPai();
		int[][] memberPai = ZTMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int yetNum = memberPai[lastPai / 10 - 1][lastPai % 10];
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
		int free = table.getLaiZiNum();
		int lastPai = table.getLastPai();
		if (free == lastPai) {
			return false;
		}
		List<Integer> memberShouPai = member.getPai();
		int[][] memberPai = ZTMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量

		int yetNum = memberPai[lastPai / 10 - 1][lastPai % 10];
		int freeNum = memberPai[free / 10 - 1][free % 10];
		if (freeNum > 0 && yetNum >= 1) {
			return true;
		}
		return false;
	}

	/**
	 * 能否飞杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public boolean canFreeGang(ZTMaJongTable table, ZTMajiangRole member) {
		int free = table.getLaiZiNum();
		int lastPai = table.getLastPai();
		if (free == lastPai) {
			return false;
		}
		List<Integer> memberShouPai = member.getPai();
		int[][] memberPai = ZTMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int yetNum = memberPai[lastPai / 10 - 1][lastPai % 10];
		int freeNum = memberPai[free / 10 - 1][free % 10];
		if (freeNum > 0 && yetNum >= 2) {
			return true;
		}
		return false;
	}

	/**
	 * 能否飞暗杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public int canFreeAnGang(ZTMaJongTable table, ZTMajiangRole member) {
		List<Integer> memberShouPai = member.getPai();
		int[][] memberPai = ZTMahJongRule.conversionType(memberShouPai);
		for (int i = 0; i < 3; i++) {
			for (int j = 1; j < 10; j++) {
				int pai = 10 * (i + 1) + j;
				if (pai == table.getLaiZiNum()) {
					continue;
				}
				int count = memberPai[i][j];
				if (count >= 3
						&& memberPai[table.getLaiZiNum() / 10 - 1][table
								.getLaiZiNum() % 10] >= 1) {
					return (i + 1) * 10 + j;
				}/*
				 * else if (count == 3 && memberPai[table.getLaiZiNum() / 10 -
				 * 1][table.getLaiZiNum() % 10] >= 1){ //有癞子牌的暗杠 return (i + 1)
				 * * 10 + j; }
				 */
			}
		}
		return 0;
	}

	/**
	 * 能否飞补杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public boolean canExtraFreeGang(ZTMaJongTable table, ZTMajiangRole member) {

		Integer free = table.getLaiZiNum();

		List<Integer> memberShouPai = member.getPai();
		int[][] memberPai = ZTMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int freeNum = memberPai[free / 10 - 1][free % 10];
		if (freeNum > 0) {
			freeNum = 1;
		}
		for (Map.Entry<Integer, List<Integer>> entry : member.getShowPai()
				.entrySet()) {
			List<Integer> data = entry.getValue();
			if (data.size() == 3) {
				if (!data.contains(free)) {
					if (freeNum >= 1) {
						return true;
					}
				}
				if (data.contains(free)) {
					int yetNum = memberPai[entry.getKey() / 10 - 1][entry
							.getKey() % 10];
					if (yetNum >= 1) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * 能否补杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public Integer canExtraGang(ZTMaJongTable table, ZTMajiangRole member) {
		Integer free = table.getLaiZiNum();
		List<Integer> memberShouPai = member.getPai();
		int[][] memberPai = ZTMahJongRule.conversionType(memberShouPai);
		for (Map.Entry<Integer, List<Integer>> entry : member.getShowPai()
				.entrySet()) {
			List<Integer> data = entry.getValue();
			if (data.size() == 3 && !data.contains(free)) {
				int yetNum = memberPai[entry.getKey() / 10 - 1][entry.getKey() % 10];
				if (yetNum >= 1) {
					table.setBuGangPai(entry.getKey());
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
		// if (checkOrder(table, role, OptionsType.PENG)) {
		//
		// }
		table.setQueueWaitType(0);
		int seat = role.getRole().getSeat();
		table.setLastRealSeat(seat);

		Integer lastPai = table.getLastPai();
		List<Integer> memberShouPai = role.getPai();
		int[][] memberPai = ZTMahJongRule.conversionType(memberShouPai);
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
		GMsg_12011008.Builder builder = GMsg_12011008.newBuilder();
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
				+ " dealpang把lastplayseat设置为:"
				+ seat
				+ " 该座位的玩家昵称:"
				+ table.getMembers().get(seat - 1).getRole().getRole()
						.getNick());
		// 直接出牌的流程
		GMsg_12011006.Builder chuBuilder = GMsg_12011006.newBuilder();

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

		GMsg_12011007.Builder builder4 = GMsg_12011007.newBuilder();
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
		table.resetHu();
		table.getReceiveQueue().clear();
		table.setManyOperate(false);
	}

	public boolean checkOrder(ZTMaJongTable table, ZTMajiangRole role,
			OptionsType type) {
		int seat = role.getRole().getSeat();
		table.getReceiveQueue().put(seat, type);// 保存玩家发过来的命令

		for (Map.Entry<Integer, OptionsType> entry : table.getReceiveQueue()
				.entrySet()) {
			LogUtil.debug("玩家座位：" + entry.getKey() + " 发过来的命令:"
					+ entry.getValue());
		}

		role.setOptionsType(type);
		table.getWaiter().add(seat);
		List<Integer> buGangSeat = new ArrayList<Integer>();
		List<Integer> huBuGangSeat = new ArrayList<Integer>();
		boolean manyOperate = false;// 判断是否出现同时的操作 或者 是否出现枪杠胡
		if (type == OptionsType.EXTRA_FREE_GANG
				|| type == OptionsType.EXTRA_GANG) {
			LogUtil.debug("玩家飞补杠或补杠....");
			int buGangPai = table.getBuGangPai();
			for (Map.Entry<Integer, List<GBaseMahJong.OptionsType>> entry : table
					.getCanOptions().entrySet()) {
				if (entry.getValue().contains(OptionsType.EXTRA_FREE_GANG)
						|| entry.getValue().contains(OptionsType.EXTRA_GANG)) {
					buGangSeat.add(entry.getKey());
				}
				if (entry.getValue().contains(OptionsType.ANNOUNCE_WIN)) {
					huBuGangSeat.add(entry.getKey());
				}
			}
			// 判断 补杠的牌是否能被其他人胡
			for (ZTMajiangRole ztMajiangRole : table.getMembers()) {
				if (role != ztMajiangRole
						&& !table.getWinners().contains(ztMajiangRole)) {
					if (canHu(table, ztMajiangRole, buGangPai)) {
						manyOperate = true;
						table.setQiangGangHu(true);
						GMsg_12011007.Builder waitBuilder = GMsg_12011007
								.newBuilder();
						waitBuilder.addOption(OptionsType.ANNOUNCE_WIN);
						LogUtil.info(table.getGame().getRoomId()
								+ " ANNOUNCE_WIN "
								+ ztMajiangRole.getRole().getRole().getNick()
								+ "胡 " + buGangPai);
						table.getCanOptions().put(
								ztMajiangRole.getRole().getSeat(),
								waitBuilder.getOptionList());
						// 可进行操作
						roleFunction.sendMessageToPlayer(ztMajiangRole
								.getRole().getRole().getRid(),
								waitBuilder.build());
					}
				}
			}

			// 执行调用默认胡操作
			int huNum = 0;
			// 统计有多少人抢杠胡
			for (ZTMajiangRole ztMajiangRole : table.getMembers()) {
				if (ztMajiangRole != role && ztMajiangRole.getHuType() == 0) {
					List<OptionsType> data = table.getCanOptions().get(
							ztMajiangRole.getRole().getSeat());
					if (data != null && data.contains(OptionsType.ANNOUNCE_WIN)) {
						huNum++;
					}
				}
			}
			if (huNum == 1) {
				tableToWait(table, huBuGangSeat.get(0),
						table.getNextPlaySeat(), HandleType.MAJIANG_HU,
						System.currentTimeMillis()
								+ (table.getActionDuration() + 1) * 1000,
						System.currentTimeMillis()
								+ (table.getActionDuration() + 1) * 1000);
			} else if (huNum > 1) {
				tableToWait(table, huBuGangSeat.get(0),
						table.getNextPlaySeat(), HandleType.MAJIANG_MANY_HU,
						System.currentTimeMillis()
								+ (table.getActionDuration() + 1) * 1000,
						System.currentTimeMillis()
								+ (table.getActionDuration() + 1) * 1000);
			}

		}
		for (Map.Entry<Integer, List<GBaseMahJong.OptionsType>> entry : table
				.getCanOptions().entrySet()) {
			if (entry.getValue().contains(OptionsType.ANNOUNCE_WIN)) {
				huBuGangSeat.add(entry.getKey());
			}
		}

		if (table.isQiangGangHu()) {// 处理补杠时 有其他玩家能胡 但都不胡的情况
			int receviceSize = 0;
			for (Entry<Integer, OptionsType> entry : table.getReceiveQueue()
					.entrySet()) {
				if (entry.getValue() == OptionsType.PASS) {
					receviceSize++;
				}
			}
			if (receviceSize == huBuGangSeat.size()) {
				LogUtil.debug("receviceSize:" + receviceSize
						+ "===huBuGangSeat.size():" + huBuGangSeat.size());
				if (table.getReceiveQueue().get(buGangSeat.get(0)) == (OptionsType.EXTRA_FREE_GANG)) {
					dealExtraFreeGang(table, role, table.getBuGangPai());
				}
				if (table.getReceiveQueue().get(buGangSeat.get(0)) == (OptionsType.EXTRA_GANG)) {
					dealExtraGang(table, role, table.getBuGangPai());
				}
			}
		}
		if (buGangSeat.size() > 0) {
			table.getReceiveQueue().remove(buGangSeat.get(0));
		}
		if (table.getCanOptions().size() >= 2) {// 2个或以上玩家同时操作 同个操作时
			LogUtil.debug("两个人出现同时操作");
			for (Entry<Integer, List<OptionsType>> map : table.getCanOptions()
					.entrySet()) {
				LogUtil.debug("玩家座位:" + map.getKey() + "===" + "玩家可以的操作:"
						+ map.getValue());
			}
			manyOperate = true;
			List<Integer> huSeat = new ArrayList<Integer>();
			List<Integer> feiGangSeat = new ArrayList<Integer>();
			List<Integer> pengSeat = new ArrayList<Integer>();
			List<Integer> feiPengSeat = new ArrayList<Integer>();
			List<Integer> gangSeat = new ArrayList<Integer>();
			for (Map.Entry<Integer, List<GBaseMahJong.OptionsType>> entry : table
					.getCanOptions().entrySet()) {
				if (entry.getValue().contains(OptionsType.ANNOUNCE_WIN)) {
					huSeat.add(entry.getKey());
				}
				if (entry.getValue().contains(OptionsType.FREE_EXPOSED_GANG)) {
					feiGangSeat.add(entry.getKey());
				}
				if (entry.getValue().contains(OptionsType.PENG)) {
					pengSeat.add(entry.getKey());
				}
				if (entry.getValue().contains(OptionsType.FREE_PENG)) {
					feiPengSeat.add(entry.getKey());
					LogUtil.debug("里面的:");
					LogUtil.debug("玩家:" + entry.getKey() + "可以进行的操作:"
							+ entry.getValue());
				}
				if (entry.getValue().contains(OptionsType.EXPOSED_GANG)) {
					gangSeat.add(entry.getKey());
				}
			}
			if (huSeat.size() == 0) {// 没人胡
				dealOperation(table, feiGangSeat, pengSeat, feiPengSeat);
			} else if (huSeat.size() == 1) {// 正常一个人胡
				OptionsType receiveOperate = table.getReceiveQueue().get(
						huSeat.get(0));
				if (receiveOperate == OptionsType.ANNOUNCE_WIN) {
					dealHu(table, table.getMembers().get(huSeat.get(0) - 1));
				} else if (receiveOperate == OptionsType.PASS) {
					if (gangSeat != null
							&& gangSeat.size() > 0
							&& table.getReceiveQueue().get(gangSeat.get(0)) == OptionsType.EXPOSED_GANG) {
						dealGang(table,
								table.getMembers().get(gangSeat.get(0) - 1));
					} else {
						dealOperation(table, feiGangSeat, pengSeat, feiPengSeat);
					}
				} else if (receiveOperate == null) {
					tableToWait(table, huSeat.get(0), table.getNextPlaySeat(),
							HandleType.MAJIANG_HU, table.getTargetTime());
				} else {
					dealOperation(table, feiGangSeat, pengSeat, feiPengSeat);
				}
			} else {// 2或3个人胡 （点胡,点胡以外的和啥也不点三种情况）
				LogUtil.debug("指令大小:" + table.getReceiveQueue().size());
				switch (table.getReceiveQueue().size()) {
				// 没收到指令 默认胡 一炮多响
				case 0:
					LogUtil.debug("0指令:");
					tableToWait(table, 0, 0, HandleType.MAJIANG_MANY_HU,
							table.getTargetTime());
					break;
				// 收到一个指令
				case 1:
					LogUtil.debug("1指令:");
					Integer operateSeat = 0;
					OptionsType operateSeatType = null;
					for (Entry<Integer, OptionsType> key : table
							.getReceiveQueue().entrySet()) {
						operateSeat = key.getKey();
						operateSeatType = key.getValue();
						LogUtil.debug("operate:" + operateSeat);
						LogUtil.debug("operateSeatType:" + operateSeatType);
					}

					if (operateSeatType == OptionsType.ANNOUNCE_WIN) {
						tableToWait(table, 0, 0, HandleType.MAJIANG_MANY_HU,
								table.getTargetTime());
					} else if (operateSeatType != OptionsType.ANNOUNCE_WIN) {
						LogUtil.debug("huSeat:" + huSeat);
						LogUtil.debug("operateSeat:" + operateSeat);
						huSeat.remove(operateSeat);
						LogUtil.debug("移除前 huSeat:" + huSeat);
						if (huSeat.size() == 1) {
							int canHuSeat = huSeat.get(0);
							tableToWait(table, canHuSeat,
									table.getNextPlaySeat(),
									HandleType.MAJIANG_HU,
									table.getTargetTime());
						}
						if (huSeat.size() == 2) {
							tableToWait(table, 0, 0,
									HandleType.MAJIANG_MANY_HU,
									table.getTargetTime());
						}
					}

					break;
				// 收到两个指令
				case 2:
					LogUtil.debug("2指令:");
					Map<Integer, List<OptionsType>> canOptions = new HashMap<Integer, List<OptionsType>>();
					canOptions.putAll(table.getCanOptions());
					List<Integer> operateSeatList = new ArrayList<Integer>();
					List<OptionsType> operateSeatTypeList = new ArrayList<GBaseMahJong.OptionsType>();
					for (Entry<Integer, OptionsType> key : table
							.getReceiveQueue().entrySet()) {
						operateSeatList.add(key.getKey());
						operateSeatTypeList.add(key.getValue());
						// table.getCanOptions().remove(key.getKey());
						canOptions.remove(key.getKey());
					}
					if (canOptions.size() == 0) {
						LogUtil.debug("2指令:canOptions.size() == 0");
						if (operateSeatTypeList
								.contains(OptionsType.ANNOUNCE_WIN)) {
							List<Integer> huSeatList = new ArrayList<Integer>();
							for (Entry<Integer, OptionsType> key : table
									.getReceiveQueue().entrySet()) {
								if (key.getValue() == OptionsType.ANNOUNCE_WIN) {
									huSeatList.add(key.getKey());
								}
							}
							if (huSeatList.size() == 1) {
								LogUtil.debug("huSeatList.size() == 1: huseat"
										+ huSeatList.get(0));
								dealHu(table,
										table.getMembers().get(
												huSeatList.get(0) - 1));
							} else if (huSeatList.size() == 2) {
								dealManyHu(
										table,
										table.getMembers().get(
												huSeatList.get(0) - 1),
										table.getMembers().get(
												huSeatList.get(1) - 1));
							}
						} else {
							LogUtil.debug("2指令:进入没有胡的优先级处理");
							dealOperation(table, feiGangSeat, pengSeat,
									feiPengSeat);
						}
					} else if (canOptions.size() == 1) {
						LogUtil.debug("2指令:canOptions.size() == 1");
						if (operateSeatTypeList
								.contains(OptionsType.ANNOUNCE_WIN)) {
							List<Integer> huSeatList = new ArrayList<Integer>();
							for (Entry<Integer, OptionsType> key : table
									.getReceiveQueue().entrySet()) {
								if (key.getValue() == OptionsType.ANNOUNCE_WIN) {
									huSeatList.add(key.getKey());
								}
							}
							if (huSeatList.size() == 1) {
								dealManyHu(
										table,
										table.getMembers().get(
												huSeatList.get(0) - 1),
										table.getMembers().get(
												huSeatList.get(1) - 1));
							} else if (huSeatList.size() == 2) {
								dealManyHu(
										table,
										table.getMembers().get(
												huSeatList.get(0) - 1),
										table.getMembers().get(
												huSeatList.get(1) - 1),
										table.getMembers().get(
												huSeatList.get(2) - 1));
							}
						} else {
							for (Entry<Integer, List<OptionsType>> map : canOptions
									.entrySet()) {
								if (map.getValue().contains(
										OptionsType.ANNOUNCE_WIN)) {
									tableToWait(table, map.getKey() - 1,
											table.getNextPlaySeat(),
											HandleType.MAJIANG_HU,
											table.getTargetTime());
								} else {
									dealOperation(table, feiGangSeat, pengSeat,
											feiPengSeat);
								}
							}
						}
					}

					break;
				// 收到三个指令
				case 3:
					LogUtil.debug("3指令:");
					List<Integer> operateSeatList_three = new ArrayList<Integer>();
					List<Integer> huOperateSeatList_three = new ArrayList<Integer>();
					List<OptionsType> operateSeatTypeList_three = new ArrayList<GBaseMahJong.OptionsType>();
					int receviceHuNum = 0;
					for (Entry<Integer, OptionsType> key : table
							.getReceiveQueue().entrySet()) {
						operateSeatList_three.add(key.getKey());
						operateSeatTypeList_three.add(key.getValue());
						if (key.getValue() == OptionsType.ANNOUNCE_WIN) {
							receviceHuNum++;
							huOperateSeatList_three.add(key.getKey());
						}
					}
					if (receviceHuNum == 0) {
						dealOperation(table, feiGangSeat, pengSeat, feiPengSeat);
					} else if (receviceHuNum == 1) {
						dealHu(table,
								table.getMembers().get(
										huOperateSeatList_three.get(0) - 1));
					} else if (receviceHuNum == 2) {
						dealManyHu(
								table,
								table.getMembers().get(
										huOperateSeatList_three.get(0) - 1),
								table.getMembers().get(
										huOperateSeatList_three.get(1) - 1));
					} else if (receviceHuNum == 3) {
						dealManyHu(
								table,
								table.getMembers().get(
										huOperateSeatList_three.get(0) - 1),
								table.getMembers().get(
										huOperateSeatList_three.get(1) - 1),
								table.getMembers().get(
										huOperateSeatList_three.get(2) - 1));
					}
					break;
				}
			}
		}

		return manyOperate;
	}

	private void dealOperation(ZTMaJongTable table, List<Integer> feiGangSeat,
			List<Integer> pengSeat, List<Integer> feiPengSeat) {
		int totalSize = feiGangSeat.size() + feiPengSeat.size()
				+ pengSeat.size();
		if (totalSize == 0) {
			LogUtil.debug("tatalsize == 0:");
			List<Integer> seatList = new ArrayList<Integer>();
			for (Entry<Integer, OptionsType> map : table.getReceiveQueue()
					.entrySet()) {
				seatList.add(map.getKey());
			}
			List<Integer> youXianJiSeat = table.getYouXianJiSeat(seatList,
					table.getLastOutPai());
			LogUtil.debug("逆时针最后的座位"
					+ table.getMembers().get(
							youXianJiSeat.get(youXianJiSeat.size() - 1) - 1));
			dealPass(
					table,
					table.getMembers().get(
							youXianJiSeat.get(youXianJiSeat.size() - 1) - 1));
		}
		// 单个操作
		if (totalSize == 1) {
			LogUtil.debug("tatalsize == 1:");
			if (feiGangSeat != null && feiGangSeat.size() > 0) {
				if (table.getReceiveQueue().get(feiGangSeat.get(0)) == OptionsType.FREE_EXPOSED_GANG) {
					LogUtil.debug("单个操作飞杠:seat:dealfreegang:"
							+ table.getMembers().get(feiGangSeat.get(0) - 1));
					dealFreeGang(table,
							table.getMembers().get(feiGangSeat.get(0) - 1));
				} else {
					LogUtil.debug("单个操作飞杠:seat:dealpass:"
							+ table.getMembers().get(feiGangSeat.get(0) - 1));
					dealPass(table,
							table.getMembers().get(feiGangSeat.get(0) - 1));
				}
			}
			if (feiPengSeat != null && feiPengSeat.size() > 0) {
				if (table.getReceiveQueue().get(feiPengSeat.get(0)) == OptionsType.FREE_PENG) {
					LogUtil.debug("单个操作飞碰:seat:dealfreepeng:"
							+ table.getMembers().get(feiPengSeat.get(0) - 1));
					dealFreePeng(table,
							table.getMembers().get(feiPengSeat.get(0) - 1));
				} else {
					LogUtil.debug("单个操作飞碰:seat:dealfreepeng:"
							+ table.getMembers().get(feiPengSeat.get(0) - 1));
					dealPass(table,
							table.getMembers().get(feiPengSeat.get(0) - 1));
				}
			}

			if (pengSeat != null && pengSeat.size() > 0) {
				if (table.getReceiveQueue().get(pengSeat.get(0)) == OptionsType.PENG) {
					LogUtil.debug("单个操作碰:seat:dealpass:"
							+ table.getMembers().get(pengSeat.get(0) - 1));
					dealPeng(table, table.getMembers().get(pengSeat.get(0) - 1));
				} else {
					LogUtil.debug("单个操作碰:seat:dealpass:"
							+ table.getMembers().get(pengSeat.get(0) - 1));
					dealPass(table, table.getMembers().get(pengSeat.get(0) - 1));
				}
			}

		}
		if (totalSize >= 2) {
			LogUtil.debug("tatalsize == 2:");
			// 处理飞杠 碰 和 飞碰 除了胡之外
			// 能出现同时操作的只有 飞杠和飞碰(最多两人) 碰和飞碰(最多两人) 飞碰和飞碰(最多三人)这三种情况
			// 1.飞杠和飞碰
			if (feiGangSeat.size() == 1 && feiPengSeat.size() == 1) {
				OptionsType feiPengSeatOperate = table.getReceiveQueue().get(
						feiPengSeat.get(0) - 1);
				OptionsType feiGangSeatOperate = table.getReceiveQueue().get(
						feiGangSeat.get(0) - 1);
				if (feiGangSeatOperate == OptionsType.FREE_EXPOSED_GANG) {
					// 飞杠点飞杠
					dealFreeGang(table,
							table.getMembers().get(feiGangSeat.get(0) - 1));
				} else if (feiGangSeatOperate == OptionsType.PASS) {
					// 飞杠点pass
					if (feiPengSeatOperate == OptionsType.FREE_PENG) {
						dealFreePeng(table,
								table.getMembers().get(feiGangSeat.get(0) - 1));
					} else if (feiPengSeatOperate == OptionsType.PASS) {
						dealPass(table,
								table.getMembers().get(feiPengSeat.get(0) - 1));
					}
				} else if (feiGangSeatOperate == null) {
					// 飞杠没有点pass
					if (feiPengSeatOperate == OptionsType.FREE_PENG) {
						tableToWait(table, feiPengSeat.get(0),
								table.getNextPlaySeat(),
								HandleType.MAJIANG_FREE_PENG,
								table.getTargetTime());
					}
				}
			}

			// 2.碰和飞碰
			if (pengSeat.size() == 1 && feiPengSeat.size() == 1) {
				int i = pengSeat.get(0);
				int j = feiPengSeat.get(0);
				if (table.getReceiveQueue().get(i) == OptionsType.PENG) {
					// 碰点碰
					dealPeng(table, table.getMembers().get(i - 1));
				} else if (table.getReceiveQueue().get(i) == OptionsType.PASS) {
					// 碰点pass
					if (table.getReceiveQueue().get(j) == OptionsType.FREE_PENG) {
						dealFreePeng(table, table.getMembers().get(j - 1));
					} else if (table.getReceiveQueue().get(j) == OptionsType.PASS) {
						dealPass(table, table.getMembers().get(j - 1));
					}

				}
				if (table.getReceiveQueue().get(i) == null) {
					// 碰啥也没点
					if (table.getReceiveQueue().get(j) == OptionsType.FREE_PENG) {
						tableToWait(table, j, table.getNextPlaySeat(),
								HandleType.MAJIANG_FREE_PENG,
								table.getTargetTime());
					}

				}
			}

			// 3.飞碰和飞碰
			if (feiPengSeat.size() >= 2) {
				// 最后打牌玩家座位
				int lastOutPaiSeat = table.getLastOutPai();
				List<Integer> youXianJiSeat = table.getYouXianJiSeat(
						feiPengSeat, lastOutPaiSeat);
				// 遍历一轮飞碰玩家
				// for (int i = 0; i < feiPengSeat.size(); i++) {
				if (feiPengSeat.size() == 3) {
					int firstSeat = youXianJiSeat.get(0);// 第一优先级
					int secondSeat = youXianJiSeat.get(1);// 第二优先级
					int thirdSeat = youXianJiSeat.get(2);// 第三优先级
					OptionsType firstOperate = table.getReceiveQueue().get(
							firstSeat);
					OptionsType secondOperate = table.getReceiveQueue().get(
							secondSeat);
					OptionsType thirdOperate = table.getReceiveQueue().get(
							thirdSeat);
					if (firstOperate == OptionsType.PENG) {
						// 1. 第一优先级的飞碰点飞碰
						dealFreePeng(table,
								table.getMembers().get(firstSeat - 1));
					} else if (firstOperate == OptionsType.PASS) {
						// 2. 第一优先级的飞碰点pass
						if (secondOperate == OptionsType.FREE_PENG) {
							// 2.1第二优先级的飞碰点碰
							dealFreePeng(table,
									table.getMembers().get(secondSeat - 1));
						} else if (secondOperate == OptionsType.PASS) {
							// 2.2第二优先级的飞碰点过
							if (thirdOperate == OptionsType.FREE_PENG) {
								// 2.2.1第三优先级的飞碰点飞碰
								dealFreePeng(table,
										table.getMembers().get(thirdSeat - 1));
							} else if (thirdOperate == OptionsType.PASS) {
								dealPass(table,
										table.getMembers().get(thirdSeat - 1));
							}
						}

					} else if (firstOperate == null) {
						// 3.第一优先级的飞碰啥也没点
						if (secondOperate == OptionsType.FREE_PENG) {
							// 3.1 第二优先级的飞碰点碰
							dealFreePeng(table,
									table.getMembers().get(secondSeat - 1));
						} else if (secondOperate == OptionsType.PASS) {
							// 3.2 第二优先级的飞碰点pass
							if (thirdOperate == OptionsType.FREE_PENG) {
								// 3.2.1第三优先级的飞碰点飞碰
								dealFreePeng(table,
										table.getMembers().get(thirdSeat - 1));
							}

						} else if (secondOperate != null) {
							// 3.3 第二优先级的飞碰啥也没点
							// if (thirdOperate == OptionsType.FREE_PENG) {
							// 3.3.1第三优先级的飞碰点飞碰
							if (thirdOperate == OptionsType.FREE_PENG) {
								// table.setWaitSeat(waitSeat);
								tableToWait(table, thirdSeat,
										table.getNextPlaySeat(),
										HandleType.MAJIANG_FREE_PENG,
										table.getTargetTime());
								// }
							}
						}

					}
				}
				if (feiPengSeat.size() == 2) {// 2个飞碰
					int firstSeat = youXianJiSeat.get(0);// 第一优先级
					int secondSeat = youXianJiSeat.get(1);// 第二优先级
					OptionsType firstOperate = table.getReceiveQueue().get(
							firstSeat);
					OptionsType secondOperate = table.getReceiveQueue().get(
							secondSeat);
					if (firstOperate == OptionsType.FREE_PENG) {
						// 优先级高的飞碰点飞碰
						dealFreePeng(table,
								table.getMembers().get(firstSeat - 1));
					} else if (firstOperate == OptionsType.PASS) {
						// 优先级高的飞碰点pass
						if (secondOperate == OptionsType.FREE_PENG) {
							dealFreePeng(table,
									table.getMembers().get(secondSeat - 1));
						} else if (secondOperate == OptionsType.PASS) {
							dealPass(table,
									table.getMembers().get(secondSeat - 1));
						}
					} else if (firstOperate == null) {
						// 优先级高的飞碰没有点pass
						if (secondOperate == OptionsType.FREE_PENG) {
							table.setWaitSeat(secondSeat);
							tableToWait(table, secondSeat,
									table.getNextPlaySeat(),
									HandleType.MAJIANG_FREE_PENG,
									table.getTargetTime());
						}
					}
				}
				// }
			}
		}
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
		refreshPai(table, role);// 向客户端发送玩家的牌信息同步手牌
		table.setQueueWaitType(0);
		int seat = role.getRole().getSeat();
		Integer lastPai = table.getLastPai();
		List<Integer> memberShouPai = role.getPai();
		int[][] memberPai = ZTMahJongRule.conversionType(memberShouPai);
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

		// Integer pai = table.getPais().remove(table.getPais().size() - 1);
		// 概率摸牌

		Integer pai = moPai(table, lastRole);
		// 概率摸牌

		table.setMoPai(seat);
		table.setLastMoPai(pai);
		role.getPai().add(pai);

		table.setLastRealSeat(seat);

		GPaiInfo.Builder info = GPaiInfo.newBuilder();
		info.addAllPai(rest);
		info.addPai(pai);

		GMsg_12011008.Builder builder = GMsg_12011008.newBuilder();
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
		LogUtil.info(table.getGame().getRoomId() + " 昵称  "
				+ role.getRole().getRole().getNick() + " seat " + seat + "杠 "
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
	 * 处理提坨
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealTiTuo(ZTMaJongTable table, ZTMajiangRole role, Integer pai) {
//		System.out.println("进入提坨操作:");
		table.setQueueWaitType(0);
		Integer seat = role.getRole().getSeat();
		table.setLastRealSeat(seat);
		LogUtil.info("提坨之后.牌桌最后操作玩家：" + table.getLastRealSeat());
		Integer free = table.getLaiZiNum();
		List<Integer> shouPai = role.getPai();
		Map<Integer, List<Integer>> showPai = role.getShowPai();
		List<Integer> tempPai = new ArrayList<Integer>();
		for (Entry<Integer, List<Integer>> entry : showPai.entrySet()) {
			Integer key = entry.getKey();
			if (null != pai && key == pai) {
				LogUtil.info("提坨的牌:" + key);
				int size = showPai.get(key).size();
				for (int i = 0; i < size; i++) {
					tempPai.add(pai);
				}
			}
		}
		showPai.put(pai, tempPai);
		shouPai.remove(pai);
		shouPai.add(free);
		table.setLastMoPai(free);
		GPaiInfo.Builder info = GPaiInfo.newBuilder();
		info.addPai(pai);
		GMsg_12011008.Builder builder = GMsg_12011008.newBuilder();
		builder.setOption(OptionsType.TI_TUO);
		builder.setOperatePai(info);
		builder.setNextSeat(seat);
		LogUtil.info("座位:" + seat);
		LogUtil.info("提坨1:" + builder.getOption() + "pai："
				+ builder.getOperatePai().getPaiList());
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());
		// LogUtil.info("提坨2:" + builder.getOption() + "pai："
		// + builder.getOperatePai().getPaiList());
		LogUtil.info("提完坨之后玩家的手牌:" + role.getPai());
		for (Entry<Integer, List<Integer>> entry : role.getShowPai().entrySet()) {
			LogUtil.info("提坨完之后 玩家摆牌:" + entry.getValue());
		}
		checkSelfOption(table, role);

	}

	/**
	 * 处理飞碰
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealFreePeng(ZTMaJongTable table, ZTMajiangRole role) {
		table.setQueueWaitType(0);
		int seat = role.getRole().getSeat();
		table.setLastRealSeat(seat);
		Integer free = table.getLaiZiNum();
		Integer lastPai = table.getLastPai();
		List<Integer> memberShouPai = role.getPai();
		int[][] memberPai = ZTMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int yetNum = memberPai[lastPai / 10 - 1][lastPai % 10];
		int freeNum = memberPai[free / 10 - 1][free % 10];
		if (freeNum > 0 && yetNum >= 1) {
			table.getYetOptions().put(role.getRole().getSeat(),
					OptionsType.FREE_PENG);
//			System.out.println("gsm008 : 飞碰  操作的牌 " + lastPai + "玩家的手牌 ："
//					+ role.getPai());
		} else {
			LogUtil.error("处理飞碰 请求错误 条件检查不通过");
		}
		ZTMajiangRole lastRole = table.getMembers().get(
				table.getLastOutPai() - 1);
		lastRole.getRecyclePai().remove(lastPai);

		List<Integer> rest = new ArrayList<Integer>();
		rest.add(free);
		rest.add(lastPai);
		rest.add(lastPai);

		role.getShowPai().put(lastPai, rest);
		role.getPai().remove(lastPai);
		role.getPai().remove(free);

		GPaiInfo.Builder info = GPaiInfo.newBuilder();
		info.addAllPai(rest);

		GMsg_12011008.Builder builder = GMsg_12011008.newBuilder();
		builder.setNextSeat(seat);
		builder.setOption(OptionsType.FREE_PENG);
		builder.setTargetSeat(table.getLastOutPai());
		builder.setOperatePai(info);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

//		System.out.println("gsm008 服务端向客户端发送: 飞碰  操作的牌 "
//				+ builder.getOperatePai().getPaiList() + "玩家的手牌 ："
//				+ role.getPai());
		List<OptionsType> list = new ArrayList<OptionsType>();
		list.add(OptionsType.DISCARD_TILE);
		table.getCanOptions().put(seat, list);

		// 重置操作座位
		table.setLastPlaySeat(seat);
		LogUtil.info(table
				+ " dealfreepeng把lastplayseat设置为:"
				+ seat
				+ " 该座位的玩家昵称:"
				+ table.getMembers().get(seat - 1).getRole().getRole()
						.getNick());
		// 直接出牌的流程
		GMsg_12011006.Builder chuBuilder = GMsg_12011006.newBuilder();

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

		GMsg_12011007.Builder builder4 = GMsg_12011007.newBuilder();
		builder4.addAllOption(list);
		roleFunction.sendMessageToPlayer(role.getRole().getRole().getRid(),
				builder4.build());
		LogUtil.info(table.getGame().getRoomId() + " seat " + seat + "飞碰 "
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
		table.resetHu();
		table.getReceiveQueue().clear();
		table.setManyOperate(false);
	}

	/**
	 * 处理飞杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealFreeGang(ZTMaJongTable table, ZTMajiangRole role) {
		refreshPai(table, role);// 向客户端发送玩家的牌信息同步手牌
		table.setQueueWaitType(0);
		int seat = role.getRole().getSeat();
		table.setLastRealSeat(seat);

		Integer free = table.getLaiZiNum();
		Integer lastPai = table.getLastPai();
		List<Integer> memberShouPai = role.getPai();
		int[][] memberPai = ZTMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int yetNum = memberPai[lastPai / 10 - 1][lastPai % 10];
		int freeNum = memberPai[free / 10 - 1][free % 10];
		if (freeNum > 0) {
			freeNum = 1;
		}
		if (freeNum > 0 && yetNum >= 2) {
			table.getYetOptions().put(role.getRole().getSeat(),
					OptionsType.FREE_EXPOSED_GANG);
		} else {
			LogUtil.error("处理飞杠 请求错误 条件检查不通过");
		}
		ZTMajiangRole lastRole = table.getMembers().get(
				table.getLastOutPai() - 1);
		lastRole.getRecyclePai().remove(lastPai);
		List<Integer> rest = new ArrayList<Integer>();
		rest.add(free);
		rest.add(lastPai);
		rest.add(lastPai);
		rest.add(lastPai);

		role.getShowPai().put(lastPai, rest);
		role.getPai().remove(lastPai);
		role.getPai().remove(lastPai);
		role.getPai().remove(free);

		// Integer pai = table.getPais().remove(table.getPais().size() - 1);

		// 概率摸牌
		Integer pai = moPai(table, lastRole);
		// 概率摸牌

		role.getPai().add(pai);
		table.setMoPai(seat);
		table.setLastMoPai(pai);

		table.setLastRealSeat(seat);

		GPaiInfo.Builder info = GPaiInfo.newBuilder();
		info.addAllPai(rest);
		info.addPai(pai);

		GMsg_12011008.Builder builder = GMsg_12011008.newBuilder();
		builder.setNextSeat(seat);
		builder.setOption(OptionsType.FREE_EXPOSED_GANG);
		builder.setTargetSeat(table.getLastOutPai());
		builder.setOperatePai(info);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		LogUtil.info(table.getGame().getRoomId() + " seat " + seat + "飞杠 "
				+ lastPai);

		// 重置操作座位
		table.setLastPlaySeat(seat);
		LogUtil.info(table
				+ " dealfregang把lastplayseat设置为:"
				+ seat
				+ " 该座位的玩家昵称:"
				+ table.getMembers().get(seat - 1).getRole().getRole()
						.getNick());
		table.resetHu();

		checkSelfOption(table, role);

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
		 * chuBuilder.setWaitTime(TimeUtil.time() + 10);
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
		 * System.currentTimeMillis() + 10000); }
		 */
		table.getReceiveQueue().clear();
		table.setManyOperate(false);
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

		LogUtil.info("最后操作的玩家...：" + table.getLastRealSeat() + "当前玩家位置:" + seat);
		Integer destPai = null;
		if (table.getLastRealSeat() == seat) {
			destPai = table.getLastMoPai();
			role.getPai().remove(destPai); // 先移除，后面会把胡的这张牌放到第一个位置
			LogUtil.info("玩家手牌自摸:" + role.getPai());
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
				GMsg_12011015.Builder builder = GMsg_12011015.newBuilder();
				builder.setPai(destPai);
				builder.setSeat(table.getBeiQiangGangHuSeat());
				LogUtil.info("被抢杠胡的座位:" + builder.getSeat());
				roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
						builder.build());
			} else {
				destPai = table.getLastPai();
				role.setHuType(destPai);
				LogUtil.info("玩家手牌正常胡:" + role.getPai());

				// 从放炮玩家已出牌列表中移除这张牌
				LogUtil.info("table.getLastOutPai() :" + table.getLastOutPai());
				ZTMajiangRole lastRole = table.getMembers().get(
						table.getLastOutPai() - 1);
				lastRole.getRecyclePai().remove(destPai);
			}

		}

		// 把胡的这张牌放到第一张-用以重连的时候告诉重连玩家
		role.getPai().add(0, destPai);

		// 手牌
		List<Integer> memberShouPai = new ArrayList<Integer>();
		memberShouPai.addAll(role.getPai());

		// 摆牌
		List<Integer> memberShowPai = new ArrayList<Integer>();
		Map<Integer, List<Integer>> showPai = role.getShowPai();
		for (Map.Entry<Integer, List<Integer>> entry : showPai.entrySet()) {
			memberShowPai.addAll(entry.getValue());
		}

		table.getWinners().add(role.getRole().getSeat());
		LogUtil.info("winners：" + table.getWinners());
		// 全牌
		List<Integer> allPai = new ArrayList<Integer>();
		allPai.addAll(memberShouPai);// 手牌
		allPai.addAll(memberShowPai);// 摆牌
		LogUtil.info(seat + "------胡牌-----" + memberShouPai);

		List<Integer> listTypeId = dealFanType(role, table, memberShouPai,
				memberShowPai, allPai, showPai);// 番的类型
		List<Integer> sendListTypeList = new ArrayList<Integer>();
		sendListTypeList.addAll(listTypeId);
		LogUtil.debug("dealwin前胡的类型:" + listTypeId);
		dealWin(role, table, listTypeId);// 处理赢,流水id设置
		LogUtil.debug("dealwin后胡的类型:" + listTypeId);

		role.getRole().setStatus(PlayerState.PS_WATCH_VALUE);

		GPaiInfo.Builder info = GPaiInfo.newBuilder();
		List<Integer> shouPai = role.getPai();
		info.addAllPai(shouPai);

		GMsg_12011008.Builder builder = GMsg_12011008.newBuilder();
		builder.setOption(OptionsType.ANNOUNCE_WIN);
		builder.setOperatePai(info);
		LogUtil.debug("pai:" + info.getPaiList());

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
		GMsg_12011012.Builder goldBuilder = GMsg_12011012.newBuilder();
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
		detailBillsInfo.setGoldDetail(0);
		detailBillsInfo.setWinTimes(0);
		builder.setHuStyle(detailBillsInfo);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		// 检查游戏中是否存在玩家金币为零的情况
		checkGoldZeroMember(table);
		// 重置操作座位 为胡家
		table.setLastPlaySeat(seat);
		table.setLastRealSeat(seat);
		if (!table.isGamePause()) {
			LogUtil.info("table.getlastplayseat0：" + table.getLastPlaySeat());
			LogUtil.info(table
					+ " dealhu把lastplayseat设置为:"
					+ seat
					+ " 该座位的玩家昵称:"
					+ table.getMembers().get(seat - 1).getRole().getRole()
							.getNick());
			LogUtil.info("table.getlastplayseat1：" + table.getLastPlaySeat());
			table.setLastPlaySeat(table.getNextPlaySeat());
			LogUtil.info("table.getlastplayseat2：" + table.getLastPlaySeat());
			table.setNextSeat(table.getNextPlaySeat());
			if (table.getPais().size() > 0) {
				if (table.getWinners().size() >= table.getMembers().size() - 1) {
					endTable(table);
				} else {
					LogUtil.info("轮到谁摸牌:" + table.getLastPlaySeat());
					tableToWait(table, table.getLastPlaySeat(),
							table.getNextPlaySeat(),
							HandleType.MAJIANG_GET_PAI,
							System.currentTimeMillis() + 1500);
				}
			} else {
				endTable(table);
			}
		} else {
//			// 调用踢人处理事件
//			tableToWait(table, table.getLastPlaySeat(),
//					table.getNextPlaySeat(), HandleType.MAJIANG_KICK_PLAYER,
//					System.currentTimeMillis() + 15000);
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
		GMsg_12011014.Builder builder = GMsg_12011014.newBuilder();
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

		Integer destPai = table.getLastPai();
		// 放炮玩家已出牌列表中移除一次牌
		if (table.isQiangGangHu()) {
			destPai = table.getBuGangPai();
			// 从被抢杠玩家手牌中移除这张牌
			ZTMajiangRole lastRole = table.getMembers().get(
					table.getBeiQiangGangHuSeat() - 1);
			lastRole.getPai().remove(destPai);
			GMsg_12011015.Builder gangBuilder = GMsg_12011015.newBuilder();
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
				info.addAllPai(memberShouPai);

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

				// dealZeroGold(table);
				// 重置操作座位 为胡家

				LogUtil.info("最后操作的玩家座位号:" + seat);
				table.setLastPlaySeat(seat);
				table.setLastRealSeat(seat);
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
		GMsg_12011012.Builder goldBuilder = GMsg_12011012.newBuilder();
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
							table.getNextPlaySeat(), HandleType.MAJIANG_GET_PAI,
							System.currentTimeMillis() + 1500);
				}
			} else {
				endTable(table);
			}
		}else {
			
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

		role.setHuType(0);
		return dealFanType(role, table, memberShouPai, memberShowPai, allPai,
				showPai);

	}

	/**
	 * 处理胡的类型（注意：要加入癞子）
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
		// 逻辑
		int laiZi = table.getLaiZiNum();// 癞子
		// 一副手牌
		List<Integer> shouPai = new ArrayList<Integer>();
		// LogUtil.debug("龙七对debug 玩家完整的手牌："+memberShouPai);
		shouPai.addAll(memberShouPai);
		// 没有癞子的手牌
		List<Integer> shouPaiNoLaiZi = new ArrayList<Integer>();// 手牌
		for (Integer integer : shouPai) {
			if (integer != laiZi) {
				shouPaiNoLaiZi.add(integer);
			}
		}
		// 胡的类型（大类）
		List<Integer> listHuTypeId = new ArrayList<Integer>();// 胡大类的集合
		listHuTypeId.add(8);
		// 加入癞子后的转变手牌
		List<Integer> shouPaiYouLaiZi = new ArrayList<Integer>();
		// 如果有癞子,癞子替换的牌
		List<List<Integer>> huLaiZiList = ZTMahJongRule.huLaizi(shouPai, laiZi,
				showPai);
		LogUtil.info("huLaiZiList" + huLaiZiList);
		if (huLaiZiList.size() > 0) {// 有癞子的胡
			for (List<Integer> list : huLaiZiList) {
				shouPaiYouLaiZi.clear();
				shouPaiYouLaiZi.addAll(shouPaiNoLaiZi);// 获取手牌
				shouPaiYouLaiZi.addAll(list);// 手牌
				if (memberShouPai.size() == 14) {
					if (!listHuTypeId.contains(15)) {// 7对(手牌一定要14张)
						if (ZTMahJongRule.judegeSevenPairs(shouPaiYouLaiZi)) {
							listHuTypeId.add(15);
						}
					}
					if (!listHuTypeId.contains(17)) {// 龙7对(手牌一定要14张)
						if (ZTMahJongRule.judegeLongSevenPairs(shouPaiYouLaiZi)) {
							listHuTypeId.add(17);
						}
					}
				}
				if (!listHuTypeId.contains(10)) {// 大对子
					if (ZTMahJongRule.judgeFourTriple(shouPaiYouLaiZi)) {
						listHuTypeId.add(10);
					}
				}
			}
		} else// 没有癞子的胡
		{
			if (memberShouPai.size() == 14) {
				if (ZTMahJongRule.judegeSevenPairs(shouPai)) {// 7对(手牌一定要14张)
					listHuTypeId.add(15);
				}
				if (ZTMahJongRule.judegeLongSevenPairs(shouPai)) {// 龙7对(手牌一定要14张)
					listHuTypeId.add(17);
				}
			}
			if (ZTMahJongRule.judgeFourTriple(memberShouPai)) {// 大对子
				listHuTypeId.add(10);
			}
		}
		// 只需判断手牌
		if (ZTMahJongRule.judgeFlush(allPai, laiZi)) {// 清一色
			listHuTypeId.add(13);
		}
		if (table.getPais().size() == 54 && role.getHuType() == -1)// 天胡
		{
			listHuTypeId.add(11);
		}
		// System.out.println("能地胡吗: table牌的剩余数量:" + table.getPais().size()
		// + "hutype：" + role.getHuType());
		if (table.getPais().size() == 54 && role.getHuType() != -1
				&& table.getOwner() == table.getLastOutPai())// 地胡
		{
			listHuTypeId.add(12);
		}
		if (ZTMahJongRule.judegeWithGang(showPai))// 勾胡(只要有一个杠)
		{
			listHuTypeId.add(9);
		}
		if (listHuTypeId.contains(13) && listHuTypeId.contains(15))// 清巧7对
		{
			listHuTypeId.add(16);
		}
		if (listHuTypeId.contains(13) && listHuTypeId.contains(10))// 清大对子
		{
			listHuTypeId.add(14);
		}
		// 胡小类（判断全部牌）
		boolean flag = true;// 无坨子 allpai里面封装有问题
		for (Integer integer : allPai) {
			if (integer == table.getLaiZiNum()) {
				flag = false;
				break;
			}
		}
		for (Integer integer : memberShowPai) {
			if (integer == table.getLaiZiNum()) {
				flag = false;
				break;
			}
		}
		if (flag == true) {
			if (!listHuTypeId.contains(17)) {
				listHuTypeId.add(1);
			}

		}
		// 杠
		// LogUtil.info("已操作:"+table.getYetOptions().get(role.getRole().getSeat()));
		if ((table.getYetOptions().get(role.getRole().getSeat()) == OptionsType.EXPOSED_GANG)) {// 明杠
			listHuTypeId.add(2);
		}
		if ((table.getYetOptions().get(role.getRole().getSeat()) == OptionsType.EXTRA_FREE_GANG)) {// 飞杠
			listHuTypeId.add(2);
		}
		if ((table.getYetOptions().get(role.getRole().getSeat()) == OptionsType.EXTRA_GANG)) {
			listHuTypeId.add(2);
		}
		if ((table.getYetOptions().get(role.getRole().getSeat()) == OptionsType.FREE_EXPOSED_GANG)) {
			listHuTypeId.add(2);
		}
		if ((table.getYetOptions().get(role.getRole().getSeat()) == OptionsType.DARK_GANG)) {
			listHuTypeId.add(2);
		}
		// 杠上炮
		if ((table.getYetOptions().get(table.getLastOutPai()) == OptionsType.EXPOSED_GANG)
				&& role.getHuType() != -1) {
			listHuTypeId.add(3);
		}
		if ((table.getYetOptions().get(table.getLastOutPai()) == OptionsType.EXTRA_FREE_GANG)
				&& role.getHuType() != -1) {
			listHuTypeId.add(3);
		}
		if ((table.getYetOptions().get(table.getLastOutPai()) == OptionsType.EXTRA_GANG)
				&& role.getHuType() != -1) {
			listHuTypeId.add(3);
		}
		if ((table.getYetOptions().get(table.getLastOutPai()) == OptionsType.FREE_EXPOSED_GANG)
				&& role.getHuType() != -1) {
			listHuTypeId.add(3);
		}
		if ((table.getYetOptions().get(table.getLastOutPai()) == OptionsType.DARK_GANG)
				&& role.getHuType() != -1) {
			listHuTypeId.add(3);
		}
		// 三墙子(摸完牌后，翻出牌墙第一张牌，定义为墙子，墙子的后一张叫坨（听用），一共4张坨（癞子）)
		int num = 0;
		int qiangZi = (laiZi % 10) - 1 == 0 ? laiZi / 10 * 10 + 9 : laiZi - 1;
		for (Integer integer : allPai) {
			if (integer == qiangZi) {
				num++;
				if (num == 3) {
					listHuTypeId.add(7);
					break;
				}
			}
		}
		// 抢杠
		// if (table.getYetOptions().get(table.getLastPlaySeat()) ==
		// OptionsType.EXTRA_GANG) {// 当前角色明杠
		// ZTMajiangRole ztMajiangRole = table.getMembers().get(
		// table.getMoPai() - 1);
		// Map<Integer, List<Integer>> pai = ztMajiangRole.getShowPai();
		// for (Map.Entry<Integer, List<Integer>> entry : pai.entrySet()) {
		// if (entry.getValue().size() == 4) {// 四个值为杠
		// if (role.getHuType() == entry.getKey()) {// 胡的牌是杠的牌
		// listHuTypeId.add(4);
		// break;
		// }
		// }
		// }
		// }
		// 抢杠
		if (table.isQiangGangHu()) {
			listHuTypeId.add(4);
		}
		// 金钩钩（只剩下最后一张牌）
		if (role.getPai().size() == 2) {
			listHuTypeId.add(5);
		}
		// 自摸
		if (role.getHuType() == -1) {
			listHuTypeId.add(6);
		}
		LogUtil.info("方法里面的listHuTypeId" + listHuTypeId);
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
			MaJiangValueCsv maJiangValueCsv = maJiangValueCache
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

		/*
		 * int maxfan = 0; if (fan.size() > 0) { for (Integer integer : fan) {
		 * if (integer > maxfan) { maxfan = integer; } } }
		 */
		// 计算格外的番
		/*
		 * int addfan = 0; for (Integer integer : add) { addfan += integer; }
		 */
		// System.out.println(listHuTypeId);
		int maxFanShu = table.getMaxFanShu();
		// System.out.println("实际番数 ：" + (maxfan + addfan));
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
			if (canFreeAnGang(table, role) > 0) {
				LogUtil.info("能飞暗杠..." + canFreeAnGang(table, role));
				list.add(OptionsType.DARK_FREE_GANG);
				LogUtil.info(table.getGame().getRoomId() + " DARK_FREE_GANG "
						+ role.getRole().getRole().getNick() + "飞暗杠 "
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

		GMsg_12011006.Builder builder3 = GMsg_12011006.newBuilder();
		// 读取麻将配置的操作时间
		int waitTime = table.getTurnDuration();// 等待第1轮时间
		int overTime = table.getTurnDuration();// 等待第2轮时间
		if (role.getTimeOutNum() < table.getOtpPunishment()) {
			overTime += table.getTurn2Duration();
		}

		// 读取麻将配置的操作时间

		if (list.contains(OptionsType.ANNOUNCE_WIN)) {
			if (role.getRole().isRobot()) {
				// int interval = MathUtil.randomNumber(1000, 3000);
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
		GMsg_12011007.Builder builder4 = GMsg_12011007.newBuilder();
		builder4.addAllOption(list);
		LogUtil.info("摸牌时 玩家可以进行的操作:" + list + ".."
				+ role.getRole().getRole().getNick());
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

		// if (game.isFriendRoom() && friendRoom != null) {
		// // 好友房设置最大番数
		// table.setMaxFanShu(friendRoom.getHighestPower());
		// }
		// 计算番数并且排除重复的番数
		int winTimes = dealFanNum(listHuTypeId, table);
		LogUtil.info("dealwin里面的番数:" + winTimes);

		// 任务检测
		if (!role.getRole().isRobot()) {
			missionFunction.checkTaskFinish(role.getRole().getRole().getRid(),
					TaskType.daily_task, MissionType.WIN, GameType.MAJIANG);
			missionFunction.checkTaskFinish(role.getRole().getRole().getRid(),
					TaskType.daily_task, MissionType.CONTINUE_WIN,
					GameType.MAJIANG, true);

			missionFunction.checkTaskFinish(role.getRole().getRole().getRid(),
					TaskType.daily_task, MissionType.CARD_TYPE,
					GameType.MAJIANG, listHuTypeId);
			// 奖券任务检测
			doLotteryTaskMission(role.getRole().getRole(), listHuTypeId,
					winTimes);
		}
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

		GMsg_12011012.Builder goldBuilder = GMsg_12011012.newBuilder();
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
			if (lossRole.getGold() < gold && !game.isFriendRoom()) {
				goldActualOne = lossRole.getGold();
			}
			goldActualTotal += goldActualOne;
			if (game.isFriendRoom() && friendRoom != null
					&& friendRoom.getSpriteMap().containsKey(lossRole.getRid())) {
				LogUtil.info(lossRole.getNick() + "输了" + goldActualOne + "积分");
				friendRoom.getSpriteMap().put(
						lossRole.getRid(),
						friendRoom.getSpriteMap().get(lossRole.getRid())
								- goldActualOne);
				// lossRole.setGold(lossRole.getGold() - goldActualOne);
			} else {
				if (lossGameRole.isRobot()) {
					lossRole.setGold(lossRole.getGold() - goldActualOne);
					lossGameRole.setRobotLost();
					// 记录ai和玩家之间的盈亏情况
					if (!winGameRole.isRobot()) {
						npcFunction.updateGainOrLoss(lossRole.getRid(),
								-goldActualOne, table.getGame().getGameType(),
								table.getGame().getRoomType(),
								MoneyEvent.MAJIANG);
					}
				} else {
					// 扣除金币
					roleFunction.goldSub(lossRole, goldActualOne,
							MoneyEvent.MAJIANG, true);
					// 记录ai和玩家之间的盈亏情况
					if (winGameRole.isRobot()) {
						npcFunction.updateGainOrLoss(winRole.getRid(),
								goldActualOne, table.getGame().getGameType(),
								table.getGame().getRoomType(),
								MoneyEvent.MAJIANG);
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
			if (role.getRole().isRobot()) {
				// 设置机器人场数信息
				role.getRole().setRobotLost();
			}
		}

		ExpCsv csv = expCache.getConfig(winRole.getLevel() + 1);
		if (!table.getGame().isFriendRoom()) {
			roleFunction.expAdd(winRole, csv.getMajiangWinExp(), true);
		}
		int lotteryTotal = 0;
		if (game.isFriendRoom() && friendRoom != null
				&& friendRoom.getSpriteMap().containsKey(winRole.getRid())) {
			friendRoom.getSpriteMap().put(
					winRole.getRid(),
					friendRoom.getSpriteMap().get(winRole.getRid())
							+ goldActualTotal);
			LogUtil.info(winRole.getNick() + "赢了" + goldActualTotal + "积分");
			// winGameRole.getRole().setGold(winGameRole.getRole().getGold()+goldActualTotal);
		} else {
			if (roomType == 1 || roomType == 2) { // 金币房规则：输家扣金币，赢家加金币
				if (!winGameRole.isRobot()) { // 机器人不用加
					roleFunction.goldAdd(winRole, goldActualTotal,
							MoneyEvent.MAJIANG, true);
				} else {
					winGameRole.getRole().setCrystal(
							winGameRole.getRole().getGold() + goldActualTotal);
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
							MoneyEvent.MAJIANG);
				} else {
					winGameRole.getRole().setCrystal(
							winGameRole.getRole().getCrystal() + lotteryTotal);
				}

				goldActualTotal = 0; // 重置为0，方便后面发消息飘字
			}
		}
		// 赢家的麻将数据数据库记录
		majiangDataFunction.updateMajiangData(winRole.getRid(), winTimes,
				goldActualTotal, listHuTypeId, FirstWeek, true);
		if (role.getRole().isRobot()) {
			// 设置机器人场数信息
			role.getRole().setRobotWin();
		}

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

		// 发送出去 改为在dealwin的外面dealhu方法里发送
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
		detailBillsInfo.setWinTimes(winTimes);
		detailBillsInfo.addAllWinTypes(listHuTypeId);
		detailBillsInfo.setGoldDetail(goldActualTotal);
		detailBillsInfo.setLottery(lotteryTotal);
		billsInfo.addDetailBillsInfo(detailBillsInfo);
		table.getBills().put(winGameRole.getSeat(), billsInfo);

		if (role.getRole().isRobot()) { // 机器人概率离开
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
			LogUtil.info("table取出来的飘字:座位:" + entry.getValue().getSeat()
					+ "飘字:金币:" + entry.getValue().getGold() + "飘字:奖券:"
					+ entry.getValue().getLottery());
		}
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
						+ " table.getGame().isFriendRoom()把lastplayseat设置为:"
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

	// 处理飞暗杠
	public void dealFreeAnGang(ZTMaJongTable table, ZTMajiangRole role,
			Integer lastPai) {
		refreshPai(table, role);// 向客户端发送玩家的牌信息同步手牌
		LogUtil.debug("飞暗杠完之前0 玩家手牌:" + role.getPai());
		table.setQueueWaitType(0);
		int seat = role.getRole().getSeat();

		List<Integer> memberShouPai = role.getPai();
		int[][] memberPai = ZTMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int yetNum = memberPai[lastPai / 10 - 1][lastPai % 10];
		if (yetNum >= 3) {
			table.getYetOptions().put(role.getRole().getSeat(),
					OptionsType.DARK_FREE_GANG);
		} else {
			LogUtil.error("处理杠 请求错误 条件检查不通过");
		}
		List<Integer> rest = new ArrayList<Integer>();
		LogUtil.debug("飞暗杠完之前1 玩家手牌:" + role.getPai());
		if (yetNum >= 3) {
			// 有癞子牌的暗杠
			rest.add(lastPai);
			rest.add(lastPai);
			rest.add(lastPai);
			rest.add(table.getLaiZiNum());
		}

		LogUtil.info("飞暗杠完之前 玩家手牌2:" + role.getPai());
		role.getShowPai().put(lastPai, rest);
		// role.getPai().removeAll(rest);
		for (Integer i : rest) {
			role.getPai().remove(i);
		}
		LogUtil.info("飞暗杠完之前 玩家手牌3:" + role.getPai() + "rest:" + rest);
		// Integer pai = table.getPais().remove(table.getPais().size() - 1);

		// 概率摸牌
		Integer pai = moPai(table, role);
		// 概率摸牌

		role.getPai().add(pai);
		table.setMoPai(seat);
		table.setLastMoPai(pai);

		table.setLastRealSeat(seat);

		GPaiInfo.Builder info = GPaiInfo.newBuilder();
		info.addAllPai(rest);
		info.addPai(pai);

//		System.out.println("飞暗杠的牌和摸到的牌:" + info.getPaiList());
		GMsg_12011008.Builder builder = GMsg_12011008.newBuilder();
		builder.setNextSeat(seat);
		builder.setOption(OptionsType.DARK_FREE_GANG);
		builder.setTargetSeat(seat);
		builder.setOperatePai(info);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		LogUtil.info(table.getGame().getRoomId() + " seat " + seat + "飞暗杠 "
				+ lastPai);

		LogUtil.debug("飞暗杠完之后 玩家手牌:" + role.getPai());
		for (Entry<Integer, List<Integer>> entry : role.getShowPai().entrySet()) {
			LogUtil.debug("飞暗杠完之后 玩家摆牌:" + entry.getValue());
		}
		// 重置操作座位
		table.setLastPlaySeat(seat);
		LogUtil.info(table
				+ " dealfreegang把lastplayseat设置为:"
				+ seat
				+ " 该座位的玩家昵称:"
				+ table.getMembers().get(seat - 1).getRole().getRole()
						.getNick());
		checkSelfOption(table, role);

		/*** Mission-16* 麻将牌型任务上报:杠 ***********************************/
		if (!role.getRole().isRobot()) {
			long rid = role.getRole().getRole().getRid();

			// 任务检测
			missionFunction.checkTaskFinish(rid, TaskType.daily_task,
					MissionType.GANG);
			LogUtil.debug(rid + " > gang .........");
		}
		table.resetHu();
		table.getReceiveQueue().clear();

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
		int[][] memberPai = ZTMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int yetNum = memberPai[lastPai / 10 - 1][lastPai % 10];
		if (yetNum == 4) {
			table.getYetOptions().put(role.getRole().getSeat(),
					OptionsType.EXPOSED_GANG);
		} else {
			LogUtil.error("处理杠 请求错误 条件检查不通过");
		}
		List<Integer> rest = new ArrayList<Integer>();
		if (yetNum == 4) {
			// 有四张一样牌的暗杠
			rest.add(lastPai);
			rest.add(lastPai);
			rest.add(lastPai);
			rest.add(lastPai);
		}
		// else {
		// // 有癞子牌的暗杠
		// rest.add(lastPai);
		// rest.add(lastPai);
		// rest.add(lastPai);
		// rest.add(table.getLaiZiNum());
		// }

		role.getShowPai().put(lastPai, rest);
		role.getPai().removeAll(rest);

		// Integer pai = table.getPais().remove(table.getPais().size() - 1);

		// 概率摸牌
		Integer pai = moPai(table, role);
		// 概率摸牌

		role.getPai().add(pai);
		table.setMoPai(seat);
		table.setLastMoPai(pai);

		table.setLastRealSeat(seat);

		GPaiInfo.Builder info = GPaiInfo.newBuilder();
		info.addAllPai(rest);
		info.addPai(pai);

		GMsg_12011008.Builder builder = GMsg_12011008.newBuilder();
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
		table.resetHu();
		checkSelfOption(table, role);
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
		int[][] memberPai = ZTMahJongRule.conversionType(memberShouPai);
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

		// 判断其他玩家呢能否抢杠胡
		// boolean canQiangGangHu = false;
		// for (ZTMajiangRole other : table.getMembers()) {
		// if (other != role && !role.getRole().isRobot()
		// && other.getHuType() == 0) {
		//
		// GMsg_12011007.Builder waitBuilder = GMsg_12011007.newBuilder();
		// if (canHu(table, other, lastPai)) {
		// table.setLastPai(lastPai);
		// table.setBeiQiangGangHuSeat(seat);
		// canQiangGangHu = true;
		// waitBuilder.addOption(OptionsType.ANNOUNCE_WIN);
		// LogUtil.info(table.getGame().getRoomId() + " ANNOUNCE_WIN "
		// + other.getRole().getRole().getNick() + "枪杠胡 "
		// + lastPai);
		// }
		// if (waitBuilder.getOptionCount() > 0) {
		// table.getCanOptions().put(other.getRole().getSeat(),
		// waitBuilder.getOptionList());
		//
		// LogUtil.info("抢杠时 发给客户端的操作:"+waitBuilder.getOptionList()+"==="+other.getRole().getRole()
		// .getRid());
		// // 可进行操作
		// roleFunction.sendMessageToPlayer(other.getRole().getRole()
		// .getRid(), waitBuilder.build());
		// }
		//
		// }
		//
		// }
		GPaiInfo.Builder info = GPaiInfo.newBuilder();

		Integer pai = moPai(table, role);
		// 概率摸牌
		role.getPai().add(pai);
		table.setLastMoPai(pai);

		info.addAllPai(rest);
		info.addPai(pai);
		GMsg_12011008.Builder builder = GMsg_12011008.newBuilder();
		builder.setNextSeat(seat);
		builder.setOption(OptionsType.EXTRA_GANG);
		builder.setTargetSeat(seat);
		builder.setOperatePai(info);
		LogUtil.info("补杠的牌和摸的牌:" + info.getPaiList() + "座位:" + seat);
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
		refreshPai(table, role);// 向客户端发送玩家的牌信息同步手牌
		table.setQueueWaitType(0);
		int seat = role.getRole().getSeat();
		table.setLastRealSeat(seat);
		List<Integer> memberShouPai = role.getPai();
		int[][] memberPai = ZTMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		Integer free = table.getLaiZiNum();
		int yetNum = memberPai[lastPai / 10 - 1][lastPai % 10];
		int freeNum = memberPai[free / 10 - 1][free % 10];

		if (yetNum >= 1 || freeNum >= 1) {
			table.getYetOptions().put(role.getRole().getSeat(),
					OptionsType.EXTRA_FREE_GANG);
		} else {
			LogUtil.error("处理杠 请求错误 条件检查不通过");
		}
		List<Integer> data = role.getShowPai().get(lastPai);
		LogUtil.info("飞补杠的牌:" + lastPai);

		List<Integer> rest = new ArrayList<Integer>();
		rest.addAll(data);
		if (data.contains(free)) {
			rest.add(lastPai);
			role.getPai().remove(lastPai);
		} else {
			rest.add(free);
			role.getPai().remove(free);
		}

		role.getShowPai().put(lastPai, rest);
		// 判断其他玩家呢能否抢杠胡
		// boolean canQiangGangHu = false;
		// for (ZTMajiangRole other : table.getMembers()) {
		// if (other != role && !role.getRole().isRobot()
		// && other.getHuType() == 0) {
		//
		// GMsg_12011007.Builder waitBuilder = GMsg_12011007.newBuilder();
		// if (canHu(table, other, lastPai)) {
		// table.setLastPai(lastPai);
		// table.setBeiQiangGangHuSeat(seat);
		// canQiangGangHu = true;
		// waitBuilder.addOption(OptionsType.ANNOUNCE_WIN);
		// LogUtil.info(table.getGame().getRoomId() + " ANNOUNCE_WIN "
		// + other.getRole().getRole().getNick() + "枪杠胡 "
		// + lastPai);
		// }
		// if (waitBuilder.getOptionCount() > 0) {
		// table.getCanOptions().put(other.getRole().getSeat(),
		// waitBuilder.getOptionList());
		//
		// // 可进行操作
		// roleFunction.sendMessageToPlayer(other.getRole().getRole()
		// .getRid(), waitBuilder.build());
		// }
		//
		// }
		//
		// }
		GPaiInfo.Builder info = GPaiInfo.newBuilder();

		Integer pai = moPai(table, role);
		// 概率摸牌
		role.getPai().add(pai);
		table.setLastMoPai(pai);

		info.addAllPai(rest);
		info.addPai(pai);
		// System.out.println("飞补杠所操作的牌:"+rest+"==="+pai);
		GMsg_12011008.Builder builder = GMsg_12011008.newBuilder();
		builder.setNextSeat(seat);
		builder.setOption(OptionsType.EXTRA_FREE_GANG);
		builder.setTargetSeat(seat);
		builder.setOperatePai(info);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		LogUtil.info(table.getGame().getRoomId() + " seat " + seat + "飞补杠 "
				+ lastPai);

		// 重置操作座位
		table.setLastPlaySeat(seat);
		LogUtil.info(table
				+ " dealexxtragang把lastplayseat设置为:"
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
		 * 
		 * GMsg_12011006.Builder chuBuilder = GMsg_12011006.newBuilder(); //
		 * 读取麻将配置的操作时间 int waitTime = table.getTurnDuration(); int overTime =
		 * table.getTurnDuration();//等待第2轮时间 if (role.getTimeOutNum() <
		 * table.getOtpPunishment()) { overTime += table.getTurn2Duration(); }
		 * chuBuilder.setOverTime(TimeUtil.time()+overTime); // 读取麻将配置的操作时间
		 * chuBuilder.setAction(OptionsType.DISCARD_TILE);
		 * chuBuilder.setCurrentSeat(seat);
		 * chuBuilder.setWaitTime(TimeUtil.time() + waitTime);
		 * roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
		 * chuBuilder.build());
		 * 
		 * GMsg_12011007.Builder builder4 = GMsg_12011007.newBuilder();
		 * builder4.addAllOption(list);
		 * roleFunction.sendMessageToPlayer(role.getRole().getRole().getRid(),
		 * builder4.build()); LogUtil.info(table.getGame().getRoomId() +
		 * " seat " + seat + "补杠 " + lastPai);
		 * 
		 * if (role.getRole().isAuto()) { tableToWait(table, seat,
		 * table.getNextPlaySeat(), HandleType.MAJIANG_OUT_PAI,
		 * System.currentTimeMillis() + 2000,System.currentTimeMillis() +
		 * (overTime+1) * 1000); } else { tableToWait(table, seat,
		 * table.getNextPlaySeat(), HandleType.MAJIANG_OUT_PAI,
		 * System.currentTimeMillis() + (overTime+1) * 1000); }
		 */
	}

	static {
		// 低级房统计初始化,中级房统计初始化,高级房统计初始化
		for (int i = 1; i <= GameRoom.ZTMAJIANGROOMNUM; i++) {
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
		allPai.addAll(memberShowPai);// 摆牌
		List<Integer> paistemp = new ArrayList<Integer>();// 遍历 打每一张手牌
		// 去掉手牌重复的数
		for (Integer pai : role.getPai()) {
			if (!paistemp.contains(pai)) {
				paistemp.add(pai);
			}
		}
		List<Integer> ting_pai = new ArrayList<Integer>();
		if (!role.getRole().isRobot()) {
			GMsg_12011013.Builder builder = GMsg_12011013.newBuilder();
			if (null != role.getPai() && role.getPai().size() > 0) {
				int laiZi = table.getLaiZiNum();
				// 遍历 出手牌上的每一张 判断能不能听牌 能听牌继续
				for (int i = 0; i < paistemp.size(); i++) {
					// List<Integer> aaa = pais;//调试
					// Collections.sort(aaa);//调试
					// //Collections.
					// System.out.println("玩家手牌:"+ aaa);
					// System.out.println("玩家手牌大小:"+aaa.size());
					Integer temp = paistemp.get(i);
					role.getPai().remove(temp);
					int[][] shuZuallPai = ZTMahJongRule.conversionType(role
							.getPai());// 玩家手牌
					int num = shuZuallPai[laiZi / 10 - 1][laiZi % 10];
					if (ZTMahJongRule.isQueYiMen(shuZuallPai,
							role.getShowPai(), table.getLaiZiNum())) {
						if (num > 0) {
							ting_pai = ZTMahJongRule.tingPai2(role.getPai(),
									table.getLaiZiNum(), showPai);
						} else {
							ting_pai = ZTMahJongRule.tingPai(shuZuallPai,
									role.getShowPai());
						}
						// 把不缺一门和误判七小对的听牌移除
						List<Integer> ting_pai_temp = new ArrayList<Integer>();
						ting_pai_temp.addAll(ting_pai);
						for (Integer tingPai : ting_pai_temp) {
							role.getPai().add(tingPai);
							if (!canHu(table, role, null)) {
								ting_pai.remove(tingPai);
							}
							role.getPai().remove(tingPai);
						}
						if (null != ting_pai && ting_pai.size() > 0) {
							// LogUtil.debug("听牌list:" + ting_pai);
							for (int j = 0; j < ting_pai.size(); j++) {
								// LogUtil.debug("听牌list:" + ting_pai);
								Integer pai_num = 0;
								Integer temp2 = ting_pai.get(j);

								role.getPai().add(temp);
								pai_num = getCardNum(table, role, temp2);
								role.getPai().remove(temp);

								role.getPai().add(temp2);
								allPai.addAll(role.getPai());
								List<Integer> huFanTypeId = dealFanType(role,
										table, role.getPai(), memberShouPai,
										allPai, showPai);
								// LogUtil.info("听牌的类型:" + huFanTypeId);
								int fan_shu = dealFanNum(huFanTypeId, table);
								// LogUtil.info("听牌的番数:" + fan_shu);
								allPai.removeAll(role.getPai());
								GTingPaiInfo.Builder tingPaiInfo = GTingPaiInfo
										.newBuilder();
								tingPaiInfo.setPai(temp);// 将要出的牌
								tingPaiInfo.setPaiNum(pai_num); // 听的牌剩余数
								tingPaiInfo.setFanShu(fan_shu); // 胡的番数
								tingPaiInfo.setTingPai(temp2);// 听的牌
								builder.addTingPaiInfo(tingPaiInfo);
								role.getPai().remove(temp2);
							}
						}

					}
					role.getPai().add(temp);
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
		int[][] shuZuallPai = ZTMahJongRule.conversionType(pais);
		int laiZi = table.getLaiZiNum();
		int num = shuZuallPai[laiZi / 10 - 1][laiZi % 10];
		if (num > 0) {
			ting_pai = ZTMahJongRule.tingPai2(pais, table.getLaiZiNum(),
					showPai);
		} else {
			ting_pai = ZTMahJongRule.tingPai(shuZuallPai, showPai);
		}
		GMsg_12011013.Builder builder = GMsg_12011013.newBuilder();
		if (null != ting_pai && ting_pai.size() > 0) {
			if (null != ting_pai
					&& ting_pai.size() > 0
					&& ZTMahJongRule.isQueYiMen(shuZuallPai, role.getShowPai(),
							table.getLaiZiNum())) {
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
			}

			roleFunction.sendMessageToPlayer(role.getRole().getRole().getRid(),
					builder.build());
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
					// LogUtil.info(pai + " 0 allCard" + allCard);
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

				// LogUtil.info("1 allCard" + allCard);
			} else {
				allCard.addAll(member.getPai());
				allCard.addAll(member.getRecyclePai());
				// LogUtil.info("2 allCard" + allCard);
				for (Map.Entry<Integer, List<Integer>> entry : member
						.getShowPai().entrySet()) {
					allCard.addAll(entry.getValue());
					// LogUtil.info("3 allCard" + allCard);
				}
			}
		}
		int[][] allPai = ZTMahJongRule.conversionType(allCard);
		// LogUtil.info("牌的二维数组");
		// for (int i = 0; i < allPai.length; i++) {
		// for (int j = 0; j < allPai[i].length; j++) {
		// System.out.print(allPai[i][j]);
		// }
		// System.out.println();
		// }
		card_num = 4 - allPai[pai / 10 - 1][pai % 10];
		// LogUtil.info("薛 "+pai +"的数量："+allPai[pai / 10 - 1][pai % 10]);
		// LogUtil.info("card_num" + card_num);
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
			for (Integer taskType : listTypeId) {
				switch (taskType) {
				case WinType.OWN_DRAW_VALUE:// 自摸
				case WinType.HEAVENLY_HAND_VALUE:
					missionFunction.checkLotteryTaskCompleted(role,
							LotteryTaskType.ZIMO_1S, 1);
					break;
				case WinType.COMMON_HAND_VALUE:// 胡别人
					missionFunction.checkLotteryTaskCompleted(role,
							LotteryTaskType.HU_1S, 1);
					break;
				case WinType.FOUR_TRIPLE_VALUE:// 大对子
					missionFunction.checkLotteryTaskCompleted(role,
							LotteryTaskType.DA_DUI_ZI, 1);
					break;
				case WinType.FLUSH_VALUE:// 清一色
					missionFunction.checkLotteryTaskCompleted(role,
							LotteryTaskType.QING_YI_SE, 1);
					break;
				case WinType.FLUSH_FOUR_TRIPLE_VALUE:// 清大对子
					missionFunction.checkLotteryTaskCompleted(role,
							LotteryTaskType.QING_YI_SE, 1);
					missionFunction.checkLotteryTaskCompleted(role,
							LotteryTaskType.DA_DUI_ZI, 1);
					break;
				case WinType.SEVEN_PAIRS_VALUE:// 巧七对
					missionFunction.checkLotteryTaskCompleted(role,
							LotteryTaskType.QIAO_QI_DUI, 1);
					break;
				case WinType.FLUSH_SEVEN_PAIRS_VALUE:// 清巧七对
					missionFunction.checkLotteryTaskCompleted(role,
							LotteryTaskType.QIAO_QI_DUI, 1);
					missionFunction.checkLotteryTaskCompleted(role,
							LotteryTaskType.QING_YI_SE, 1);
					break;
				}
			}
		}

	}

	// 得到玩家想要的测试牌
	public static Integer getCeShiPai(int t) {
		List<Integer> list = new ArrayList<Integer>();
		list.add(21);
		list.add(21);
		list.add(33);
		list.add(22);
		list.add(22);
		list.add(34);
		list.add(35);
		list.add(36);
		list.add(37);
		list.add(18);
		list.add(28);
		list.add(25);
		list.add(25);
		return list.get(t);
	}

	// 抢杠胡的时候做的处理
	// public void dealQiangGangHu(ZTMaJongTable table) {
	// Map<Integer, OptionsType> receviceOperate = table.getReceiveQueue();
	// if (table.isManyOperate()) {// 多人可以胡
	// if (checkWaitOrDeal(table)) {
	// int huNum = 0;
	// int buGangSeat = 0;
	// ZTMajiangRole[] ztMajiangRoles = new ZTMajiangRole[3];
	// for (Entry<Integer, OptionsType> entry : receviceOperate
	// .entrySet()) {
	// if (entry.getValue() == OptionsType.ANNOUNCE_WIN) {
	// ztMajiangRoles[huNum] = table.getMembers().get(
	// entry.getKey() - 1);
	// huNum++;
	// }
	// }
	// if (huNum == 0) {
	// // 处理补杠
	// if (receviceOperate.get(buGangSeat) == OptionsType.EXPOSED_GANG) {
	// dealExtraGang(table,
	// table.getMembers().get(buGangSeat - 1),
	// table.getBuGangPai());
	// } else {
	// dealExtraFreeGang(table,
	// table.getMembers().get(buGangSeat - 1),
	// table.getBuGangPai());
	// }
	// } else if (huNum == 1) {
	// ZTMajiangRole role = ztMajiangRoles[0];
	// dealHu(table, role);
	// } else if (huNum > 1) {
	// dealManyHu(table, ztMajiangRoles);
	// }
	// } else {
	// tableToWait(table, 0, 0, HandleType.MAJIANG_MANY_HU,
	// table.getTargetTime());
	// }
	// } else {// 单人可以胡
	// // 最后一个的操作
	// Map<Integer, OptionsType> lastOptionsMap = new HashMap<Integer,
	// GBaseMahJong.OptionsType>();// 最后一个操作
	// for (Entry<Integer, OptionsType> entry : table.getReceiveQueue()
	// .entrySet()) {
	// lastOptionsMap.clear();
	// lastOptionsMap.put(entry.getKey(), entry.getValue());
	// }
	// if (receviceOperate.size() == 2) {
	// for (Entry<Integer, OptionsType> entry : lastOptionsMap
	// .entrySet()) {
	// if (entry.getValue() == OptionsType.ANNOUNCE_WIN) {
	// // 处理胡
	// dealHu(table, table.getMembers()
	// .get(entry.getKey() - 1));
	// } else {
	// // 处理补杠
	// if (entry.getValue() == OptionsType.EXPOSED_GANG) {
	// dealExtraGang(table,
	// table.getMembers().get(entry.getKey() - 1),
	// table.getBuGangPai());
	// } else {
	// dealExtraFreeGang(table,
	// table.getMembers().get(entry.getKey() - 1),
	// table.getBuGangPai());
	// }
	// }
	// }
	// }
	// }
	// }

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

		// table.getCanOptions().clear();
		table.getWaiter().clear();
		table.getYetOptions().remove(role.getRole().getSeat());

		// List<OptionsType> beiQiangGang = new
		// ArrayList<GBaseMahJong.OptionsType>();
		// beiQiangGang
		// .addAll(table.getCanOptions().get(role.getRole().getSeat()));
		// beiQiangGang.remove(OptionsType.DISCARD_TILE);
		// table.getCanOptions().put(role.getRole().getSeat(), beiQiangGang);
		// table.getCanOptions().remove(role.getRole().getSeat());
		table.getCanOptions().clear();
		Integer pai = table.getBuGangPai();
		for (ZTMajiangRole other : table.getMembers()) {
			table.getCanOptions().remove(other.getRole().getSeat());
			if (other != role && other.getHuType() == 0) {
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

					LogUtil.info("dealqiangganghu玩家可以进行的操作:"
							+ waitBuilder.getOptionList() + ".."
							+ other.getRole().getRole().getNick());
					// 可进行操作
					roleFunction.sendMessageToPlayer(other.getRole().getRole()
							.getRid(), waitBuilder.build());
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

	// 客户端刷新验证手牌
	public void refreshPai(ZTMaJongTable table, ZTMajiangRole role) {
		GMsg_12011016.Builder paiInfoList = GMsg_12011016.newBuilder();

		GPaiInfo.Builder shouPaiBuilder = GPaiInfo.newBuilder();
		shouPaiBuilder.addAllPai(role.getPai());// 玩家手牌
		GPaiInfo.Builder recyclePaiBuilder = GPaiInfo.newBuilder();
		recyclePaiBuilder.addAllPai(role.getRecyclePai());// 玩家出的牌

		paiInfoList.addPai(shouPaiBuilder);
		paiInfoList.addPai(recyclePaiBuilder);

		LogUtil.info("GMsg_12011016 服务器向客户端发送玩家的牌信息.... 玩家昵称:"
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

	// 输出所有玩家当前的手牌 出的牌和摆牌(输出日志查找bug)
	private void getOutPutAllPai(ZTMaJongTable table) {
		LogUtil.info("当前牌局大小" + table.getMembers().size());
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
				boolean isHaveGold = roleFunction.isHaveGoldResuceTimes(ztMajiangRole.getRole()
						.getRole().getGoldResuceTimes());
				if (isHaveGold) {
					LogUtil.info("玩家可以领取救济金.."
							+ ztMajiangRole.getRole().getRole().getNick());
					GMsg_12006014.Builder goldInfo = GMsg_12006014.newBuilder();
					goldInfo.setSeat(ztMajiangRole.getRole().getSeat());
					goldInfo.setStatus(1);
					roleFunction.sendMessageToPlayers(table.getGame()
							.getRoles(), goldInfo.build());
					haveCharityList.add(ztMajiangRole);
					table.getRechargePlayer().add(ztMajiangRole);
				} else {LogUtil.info("玩家不可以领取救济金.."
						+ ztMajiangRole.getRole().getRole().getNick());
					noHaveCharityList.add(ztMajiangRole);
				}
			}
		}
		if (table.getPoChangRoles().size() >= 1) {
			table.setGamePause(true);
		}
		LogUtil.info("破产list..." + table.getPoChangRoles());
		LogUtil.info("破产list大小..." + table.getPoChangRoles().size());
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
	public void dealPay(Game game, Player player,int type, int gold) {
		if (game == null) {
			return;
		}
		int gameType = game.getGameType();
		if (gameType != GameType.MAJIANG ) {
			return;
		}
		ZTMaJongTable table = getTable(game.getRoomId());
		if (table == null) {
			return;
		}
		ZTMajiangRole role = getRole(player.getRole().getRid());
		//
		LogUtil.info("table.getRechargePlayer().size()0..."+table.getRechargePlayer().size());
		for(ZTMajiangRole ztMajiangRole :table.getRechargePlayer()){
			LogUtil.info("昵称.."+ztMajiangRole.getRole().getRole().getNick());
		}
		LogUtil.info("自己.."+role.getRole().getRole().getNick());
		if (table.getRechargePlayer().contains(role)) {
			table.getRechargePlayer().remove(role);
			if (type == 2) {
				LogUtil.info("玩家充值状态更换为游戏状态...");
				GMsg_12006014.Builder goldInfo = GMsg_12006014.newBuilder();
				goldInfo.setSeat(role.getRole().getSeat());
				goldInfo.setStatus(0);
				goldInfo.setGold(gold);
				roleFunction.sendMessageToPlayers(table.getGame()
						.getRoles(), goldInfo.build());
			}
			
		}
		LogUtil.info("table.getRechargePlayer().size()1..."+table.getRechargePlayer().size());
		if (table.getRechargePlayer().size() == 0) {// 可充值玩家为零时执行
			if(table.getQueueWaitType() == HandleType.MAJIANG_KICK_PLAYER){
				LogUtil.info("执行kickplayer...");
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
//				exitTable(table.getGame(), ztMajiangRole.getRole().getRole().getRid(), false);
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
}
