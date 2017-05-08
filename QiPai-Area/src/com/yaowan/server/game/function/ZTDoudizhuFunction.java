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
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.yaowan.csv.cache.DoudizhuDrawCardCache;
import com.yaowan.csv.cache.DoudizhuRoomCache;
import com.yaowan.csv.cache.ExpCache;
import com.yaowan.csv.entity.DoudizhuRoomCsv;
import com.yaowan.csv.entity.ExpCsv;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.DispatchEvent;
import com.yaowan.framework.core.thread.ISingleData;
import com.yaowan.framework.core.thread.SingleThreadManager;
import com.yaowan.framework.core.thread.SingleThreadTask;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.MathUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.GameRole;
import com.yaowan.protobuf.game.GDouDiZhu.DouDiZhuAction;
import com.yaowan.protobuf.game.GDouDiZhu.GDouDiZhuEnd;
import com.yaowan.protobuf.game.GDouDiZhu.GDouDiZhuPai;
import com.yaowan.protobuf.game.GDouDiZhu.GMsg_12012001;
import com.yaowan.protobuf.game.GDouDiZhu.GMsg_12012002;
import com.yaowan.protobuf.game.GDouDiZhu.GMsg_12012006;
import com.yaowan.protobuf.game.GDouDiZhu.GMsg_12012007;
import com.yaowan.protobuf.game.GDouDiZhu.GMsg_12012008;
import com.yaowan.protobuf.game.GDouDiZhu.GMsg_12012009;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_12020015;
import com.yaowan.protobuf.game.GGame.GGameInfo;
import com.yaowan.protobuf.game.GGame.GGameRole;
import com.yaowan.protobuf.game.GGame.GMsg_12006002;
import com.yaowan.protobuf.game.GGame.GMsg_12006005;
import com.yaowan.protobuf.game.GGame.GMsg_12006008;
import com.yaowan.protobuf.game.GGame.PlayerState;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.model.data.entity.FriendRoom;
import com.yaowan.server.game.model.data.entity.Npc;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.dao.DoudizhuLogDao;
import com.yaowan.server.game.model.log.entity.DoudizhuLog;
import com.yaowan.server.game.model.struct.Card;
import com.yaowan.server.game.model.struct.CardType;
import com.yaowan.server.game.model.struct.LotterTaskCreator;
import com.yaowan.server.game.model.struct.LotteryTaskType;
import com.yaowan.server.game.model.struct.MissionType;
import com.yaowan.server.game.model.struct.TaskType;
import com.yaowan.server.game.model.struct.ZTDoudizhuRole;
import com.yaowan.server.game.model.struct.ZTDoudizhuTable;
import com.yaowan.server.game.rule.ZTDoudizhuRule;

/**
 * 昭通斗地主
 *
 * @author zane
 */
@Component
public class ZTDoudizhuFunction extends FunctionAdapter {

	@Autowired
	private SingleThreadManager manager;

	@Autowired
	private RoleFunction roleFunction;

	@Autowired
	private DoudizhuRoomCache doudizhuRoomCache;

	@Autowired
	private ExpCache expCache;

	@Autowired
	private DoudizhuDataFunction doudizhuDataFunction;

	// G_T_C add
	@Autowired
	private DoudizhuLogDao doudizhuLogDao;

	@Autowired
	MissionFunction missionFunction;

	@Autowired
	RoomFunction roomFunction;

	@Autowired
	private RoomLogFunction roomLogFunction;

	@Autowired
	private NPCFunction NPCFunction;

	@Autowired
	private LotterTaskCreator taskCreator;

	@Autowired
	private NPCFunction npcFunction;

	private Map<Long, ZTDoudizhuTable> tableMap = new ConcurrentHashMap<>();

	private Map<Long, ZTDoudizhuRole> roleMap = new ConcurrentHashMap<>();

	private static Map<Integer, AtomicIntegerArray> roomCountMap = new ConcurrentHashMap<>();

	@Autowired
	private FriendRoomFunction friendRoomFunction;

	@Autowired
	private DoudizhuDrawCardCache doudizhuDrawCardCache;

	public ZTDoudizhuTable getTable(Long id) {
		ZTDoudizhuTable doudizhuTable = tableMap.get(id);
		return doudizhuTable;
	}

	public ZTDoudizhuTable getTableByRole(Long id) {
		ZTDoudizhuRole doudizhuRole = getRole(id);
		return getTable(doudizhuRole.getRole().getRoomId());
	}

	public ZTDoudizhuRole getRole(Long id) {
		ZTDoudizhuRole doudizhuRole = roleMap.get(id);
		return doudizhuRole;
	}

	public void clear(long roomId) {
		ZTDoudizhuTable table = tableMap.remove(roomId);
		if (table != null) {
			for (ZTDoudizhuRole role : table.getMembers()) {
				if (role != null && role.getRole() != null) {
					removeRoleCache(role.getRole().getRole().getRid());
				}
			}
		}

	}

	public void resetOverTable(Game game) {
		if (game.getStatus() == GameStatus.END_REWARD) {
			game.setStatus(GameStatus.WAIT_READY);
			ZTDoudizhuTable table = getTable(game.getRoomId());
			table.reset();
		}
	}

	/**
	 * 调用需是已同步方法
	 * 
	 * @param game
	 */
	public void robotOutIn(Game game) {

		int count = 0;
		for (Long id : game.getRoles()) {
			if (id > 0) {
				count++;
			}
		}
		int num = 3 - count;

		boolean sub = false;
		// if (count > 2 && Math.random() > (1 - count * 0.1)) {
		// LogUtil.info("sub");
		// for (GameRole role : game.getSpriteMap().values()) {
		// if (role.isRobot()) {
		// roomFunction.quitRole(game, role.getRole());
		//
		// break;
		// }
		// }
		// sub = true;
		// }
		// if (!sub) {
		// if (num >= 1) {
		// if (Math.random() < 0.7) {
		// num = 1;
		// } else {
		// num = 0;
		// }
		// }
		// if (num > 0) {
		// LogUtil.info("add" + num);
		// roomFunction.createRobotEnter(num, game);
		// }
		// }
		game.setLastRobotCreate(System.currentTimeMillis());
	}

	/**
	 * 定时检测所有人准备游戏开始
	 */
	public void checkStart(Game game) {
		long serviceTime = System.currentTimeMillis();
		boolean isRoleOK = true;
		boolean isFriendRoomOk = false;
		Map<Long, GameRole> map = new HashMap<Long, GameRole>();
		map.putAll(game.getSpriteMap());
		FriendRoom friendRoom = friendRoomFunction.getFriendRoom(game
				.getRoomId());
		if (game.isFriendRoom() && friendRoom != null
				&& game.getStatus() == GameStatus.WAIT_READY) {
			LogUtil.info("size:" + friendRoom.getPrepareList().size());
			LogUtil.info("game.getNeedCount():" + game.getNeedCount());
			if (friendRoom.getStartTime() + TimeUtil.ONE_SECOND * 10 * 1000 <= serviceTime) {
				LogUtil.info("好友房开局啦，" + serviceTime);
				isFriendRoomOk = true;
				friendRoom.setStart((byte) 1);
				if (friendRoom.getPrepareList() != null) {
					for (int i = 0; i < friendRoom.getPrepareList().size(); i++) {
						friendRoom.getPrepareList().remove(0);
					}
				}
				friendRoomFunction.getFriendRoomMap().put(game.getRoomId(),
						friendRoom);
			} else if (friendRoom.getPrepareList().size() == game
					.getNeedCount()) {
				LogUtil.info("好友房开局啦，" + serviceTime);
				isFriendRoomOk = true;
				friendRoom.setStart((byte) 1);
				if (friendRoom.getPrepareList() != null) {
					for (int i = 0; i < friendRoom.getPrepareList().size(); i++) {
						friendRoom.getPrepareList().remove(0);
					}
				}
				friendRoomFunction.getFriendRoomMap().put(game.getRoomId(),
						friendRoom);
			}
		} else {
			if(map.size() > 3 || getTable(game.getRoomId()).getMembers().size() > 3){
				LogUtil.error("斗地主人数超过三个, roomId :" + game.getRoomId() + "结束游戏");
				roomFunction.endGame(game);
				return;
			}
			for (Map.Entry<Long, GameRole> entry : map.entrySet()) {
				GameRole role = entry.getValue();
				if (!role.isRobot()) {
					if ("".equals(role.getRole().getPlatform())) {
						if (role.getStatus() != PlayerState.PS_PREPARE_VALUE) {
							role.setStatus(PlayerState.PS_PREPARE_VALUE);
						}
					} else if (role.getStatus() != PlayerState.PS_PREPARE_VALUE) {
						isRoleOK = false;
					}

				} else {
					// role.getRole().setGold(333);
					DoudizhuRoomCsv doudizhuRoomCsv = doudizhuRoomCache
							.getConfig(game.getRoomType());
					LogUtil.info(role.getRole().getNick() + "doud"
							+ role.getRole().getGold());
					boolean flag = false;
					if (doudizhuRoomCsv.getEnterUpperLimit() == -1) {
						if (role.getRole().getGold() < doudizhuRoomCsv
								.getEnterLowerLimit()) {
							flag = true;
						}
					} else {
						if (role.getRole().getGold() < doudizhuRoomCsv
								.getEnterLowerLimit()
								|| role.getRole().getGold() > doudizhuRoomCsv
										.getEnterUpperLimit()) {
							flag = true;
						}
					}
					if (!flag && role.isRobot()) {
						if (NPCFunction.robotByebye(role.getRole().getRid(),
								game.getGameType(), game.getRoomType())) {
							// AI不在时间范围内就要走了
							LogUtil.error("斗地主AI时间到，走了");
							flag = true;
						}
					}
					if (flag) {
						LogUtil.info("doud");
						GMsg_12006005.Builder builder = GMsg_12006005
								.newBuilder();
						builder.setCurrentSeat(role.getSeat());
						builder.setRoomId(game.getRoomId());
						roleFunction.sendMessageToPlayers(game.getRoles(),
								builder.build());
						roomFunction.quitRole(game, role.getRole());
						return;
					}
					long time = game.getStartTime();
					if (game.getEndTime() > game.getStartTime()) {
						time = game.getEndTime();
					}
					int dif = (int) (System.currentTimeMillis() - time) / 1000;

					/*
					 * if (game.getCount() == 0 && role.getStatus() !=
					 * PlayerState.PS_PREPARE_VALUE) {
					 * role.setStatus(PlayerState.PS_PREPARE_VALUE);
					 * 
					 * GMsg_12012001.Builder builder =
					 * GMsg_12012001.newBuilder();
					 * builder.setSeat(role.getSeat());
					 * roleFunction.sendMessageToPlayers(game.getRoles(),
					 * builder.build()); } else {
					 */

					Map<Long, ConcurrentHashMap<Integer, Game>> AIExitOrReadyMap = roomFunction
							.getAIExitOrReadyMap();
					if (!AIExitOrReadyMap.containsKey(role.getRole().getRid())) {
						if (dif > MathUtil.randomNumber(1, 3)
								&& role.getStatus() != PlayerState.PS_PREPARE_VALUE) {
							role.setStatus(PlayerState.PS_PREPARE_VALUE);

							GMsg_12012001.Builder builder = GMsg_12012001
									.newBuilder();
							builder.setSeat(role.getSeat());
							roleFunction.sendMessageToPlayers(game.getRoles(),
									builder.build());
						}
					}

					// }
					if (role.getStatus() != PlayerState.PS_PREPARE_VALUE) {
						isRoleOK = false;
					}
				}

			}
			if (game.getSpriteMap().size() < 3) {
				isRoleOK = false;
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
		if (game.getSpriteMap().size() < 3) {
			return;
		}

		ZTDoudizhuTable table = getTable(game.getRoomId());
		if (table == null || !table.isInited()) {
			return;
		}

		if (table.getMembers().size() > 3) {
			LogUtil.error("人数超过三个，tableNum : " + table.getMembers().size() + "结束游戏！");
			roomFunction.endGame(game);
			return;
			// for(int i = 0; i < table.getMembers().size(); i++) {
			// roomFunction.exitTable(table.getMembers().get(i).getRole().getRole());
			// }
		}
		if (game.isFriendRoom()) {
			FriendRoom friendRoom = friendRoomFunction.getFriendRoom(game
					.getRoomType());
			if (friendRoom != null) {
				friendRoom.setStart((byte) 1);
			}
		}
		
		if(game.isQuiting()){//有人正退出游戏，则不能开始游戏
			return;
		}
		
		if (game.getSpriteMap().size() < 3) {
			return;
		}
		
		//判断金币小于等于0
		for(ZTDoudizhuRole ztdoudizhuRole : table.getMembers()) {
			if(ztdoudizhuRole != null) {
				int lowerlimit = doudizhuRoomCache.getConfig(game.getRoomType()).getEnterLowerLimit();
				if(ztdoudizhuRole.getRole().getRole().getGold() < lowerlimit) {
					roomFunction.quitRole(game, ztdoudizhuRole.getRole().getRole());
				}
			}
		}
		
		
		
		// 开始游戏
		game.setStatus(GameStatus.RUNNING);
		Collections.sort(table.getMembers(), new Comparator<ZTDoudizhuRole>() {

			@Override
			public int compare(ZTDoudizhuRole o1, ZTDoudizhuRole o2) {
				// TODO Auto-generated method stub
				return o1.getRole().getSeat() - o2.getRole().getSeat();
			}
		});

		
		game.setCount(game.getCount() + 1);
		AtomicIntegerArray roomCountLog = getRoomCountCacheByRoomType(game
				.getRoomType());
		for (Map.Entry<Long, GameRole> entry : game.getSpriteMap().entrySet()) {
			GameRole role = entry.getValue();
			role.setStatus(PlayerState.PS_PREPARE_VALUE);

			if (!game.isFriendRoom()) {
				// 不是好友房才扣税
				// 扣税
				int tax = doudizhuRoomCache.getConfig(game.getRoomType())
						.getTaxPerGame();
				int gold = role.getRole().getGold();
				if (gold < tax) {
					tax = gold;
				}

				if (role.isRobot()) {
					role.getRole().setGold(role.getRole().getGold() - tax);
				} else {
					roleFunction.goldSub(role.getRole(), tax,
							MoneyEvent.DOUDIZHU_TAX, true);
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

		LogUtil.info("start pais" + table.getPais());

		for (int j = 0; j < table.getMembers().size(); j++) {
			ZTDoudizhuRole doudizhuMember = table.getMembers().get(j);
			if(doudizhuMember != null) {
				doudizhuMember.getPai().clear();
			}
		}
		List<Integer> pais = table.getPais();
		// 先随机抽出3张地主牌
		List<Integer> dzPais = new ArrayList<>();
		for (int dzc = 0; dzc < 3; dzc++)
			dzPais.add(pais.remove(0));
		// 剩余牌按权值进行分牌
		// for (int i = 0; i < 17; i++) {
		// for (int j = 0; j < table.getMembers().size(); j++) {
		// ZTDoudizhuRole doudizhuMember = table.getMembers().get(j);
		// Integer remove = pais.remove(0);
		// doudizhuMember.getPai().add(remove);
		// }
		// }
		ZTDoudizhuRule.fenPai(table, doudizhuDrawCardCache, npcFunction);
		// 分完牌后再把4张地主牌加进该桌牌
		table.getPais().addAll(dzPais);
		for (int j = 0; j < table.getMembers().size(); j++) {
			ZTDoudizhuRole doudizhuMember = table.getMembers().get(j);
			if(doudizhuMember != null) {
				LogUtil.info("start pais" + doudizhuMember.getPai());
			}
		}

		int seat = MathUtil.randomNumber(1, table.getMembers().size());
		/*
		 * Integer tuopai = pais.remove(0); pais.add(tuopai);
		 */
		table.setLastPlaySeat(seat);
		table.setStartSeat(seat);

		// 开局状态
		for (ZTDoudizhuRole doudizhuMember : table.getMembers()) {
			if(doudizhuMember != null) {
				GMsg_12012002.Builder builder = GMsg_12012002.newBuilder();
				roleFunction.sendMessageToPlayer(doudizhuMember.getRole().getRole()
						.getRid(), builder.build());
				// 增加活跃度
				GameRole gameRole = doudizhuMember.getRole();
				if (gameRole != null && !gameRole.isRobot()) {
					Role role = gameRole.getRole();
					if (null != role) {
						role.setBureauCount(role.getBureauCount() + 1);
						role.markToUpdate("bureauCount");
					}

					/*
					 * if (role.getLotteryTaskId() !=-1 && !gameRole.isRobot()) {
					 * int taskId = taskCreator.randomTaskId(GameType.DOUDIZHU);
					 * doudizhuMember.getRole().getRole().setLotteryTaskId(taskId);
					 * GMsg_12016003.Builder taskbuilder =
					 * GMsg_12016003.newBuilder(); taskbuilder.setTaskId(taskId);
					 * roleFunction.sendMessageToPlayer(doudizhuMember.getRole()
					 * .getRole().getRid(), taskbuilder.build()); }
					 */

				} else if (gameRole != null && gameRole.isRobot()) {
					gameRole.setAICount(gameRole.getAICount() + 1);
				}
			}
		}
		tableToWait(table, table.getLastPlaySeat(), table.getNextPlaySeat(),
				HandleType.DOUDIZHU_ZHUA, System.currentTimeMillis() + 4000);

	}

	/**
	 * 
	 *
	 * @param owner
	 * @return
	 */
	public void enterTable(Game game, GameRole role) {
		ZTDoudizhuTable table = getTable(game.getRoomId());
		FriendRoom friendRoom = new FriendRoom();
		if (game.isFriendRoom()) {
			friendRoom = friendRoomFunction.getFriendRoom(game.getRoomId());
		}
		if (table != null) {

			if (game.getStatus() == GameStatus.WAIT_READY) {
				GMsg_12006002.Builder builder = GMsg_12006002.newBuilder();

				// 新的桌面对象
				ZTDoudizhuRole doudizhuRole = new ZTDoudizhuRole(role);
				addRoleCache(doudizhuRole);
				role.setStatus(PlayerState.PS_SEAT_VALUE);
				
				if (table.getMembers().size() > 3 || game.getSpriteMap().size() > 3) {
					LogUtil.error("斗地主人数超过三个, roomId :" + game.getRoomId() + "结束游戏");
					roomFunction.endGame(game);
					return;
				}
				
				if(table.getMembers().get(role.getSeat() - 1) != null){
					GMsg_12006005.Builder builder1 = GMsg_12006005.newBuilder();
					builder1.setCurrentSeat(0);
					builder1.setRoomId(0);
					roleFunction.sendMessageToPlayer(role.getRole().getRid(),
							builder1.build());
				}


				table.getMembers().set(role.getSeat() - 1, doudizhuRole);
				
				for(ZTDoudizhuRole ztDoudizhuRole : table.getMembers()){
					if(ztDoudizhuRole != null) {
						LogUtil.info("Members座位号：" + ztDoudizhuRole.getRole().getSeat() + "," + ztDoudizhuRole.getRole().getRole().getNick());
					}
				}
				for(GameRole ztDoudizhuRole : game.getSpriteMap().values()){
					if(ztDoudizhuRole != null) {
						LogUtil.info("game座位号：" + ztDoudizhuRole.getSeat() + "," + ztDoudizhuRole.getRole().getNick());
					}
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
					if (game.isFriendRoom()) {
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
					if (entry.getValue().getStatus() == PlayerState.PS_PREPARE_VALUE) {
						builder3.setIsReady(1);
					} else {
						builder3.setIsReady(0);
					}
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
				if (game.isFriendRoom()) {
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
				ZTDoudizhuRole member = table.getMembers().get(
						role.getSeat() - 1);
				
				if(member != null) {
					GMsg_12012009.Builder builder = GMsg_12012009.newBuilder();
					if (table.getWaitAction() != null) {
						builder.setAction(table.getWaitAction());
					} else {
						builder.setAction(DouDiZhuAction.GA_PLAYING);
					}
					builder.setZhuaType(table.getZhuaType());
					builder.setCurrentSeat(table.getLastPlaySeat());

					GDouDiZhuPai.Builder paiBuilder = GDouDiZhuPai.newBuilder();
					paiBuilder.addAllPai(member.getPai());

					GDouDiZhuPai.Builder restPaiBuilder = GDouDiZhuPai.newBuilder();
					restPaiBuilder.addAllPai(table.getPais());

					GGameInfo.Builder info = GGameInfo.newBuilder();
					info.setGameType(game.getGameType());
					info.setRoomId(game.getRoomId());
					info.setRoomType(game.getRoomType());

					for (Map.Entry<Long, GameRole> entry : game.getSpriteMap()
							.entrySet()) {
						GGameRole.Builder gameRole = GGameRole.newBuilder();
						Role target = entry.getValue().getRole();
						gameRole.setRid(entry.getKey());
						gameRole.setNick(target.getNick());
						gameRole.setIsOnline(1);
						if (game.isFriendRoom()) {
							gameRole.setGold(friendRoom.getSpriteMap().get(
									target.getRid()));
							if (entry.getKey() == friendRoom.getOwner()) {
								if (friendRoom.isOwnerExit() == FriendRoomPayType.NO_EXIT) {
									gameRole.setIsOnline(1);
								} else {
									gameRole.setIsOnline(0);
								}
							}
							if (friendRoom.getLoginOutList() != null
									&& friendRoom.getLoginOutList().contains(
											entry.getKey())
									&& entry.getKey() != role.getRole().getRid()) {
								gameRole.setIsOnline(0);
							}
						} else {
							gameRole.setGold(target.getGold());
						}
						gameRole.setHead(target.getHead());
						gameRole.setLevel(target.getLevel());
						gameRole.setSeat(entry.getValue().getSeat());
						gameRole.setAvatarId(entry.getValue().getAvatarId());
						gameRole.setSex(target.getSex());
						info.addSprites(gameRole);
					}

					builder.setGame(info);
					if (table.getZhuaType() == 0 && member.getLookedPai() == 1) {
						builder.setHandPai(paiBuilder);
					}
					if (table.getWaitAction() == DouDiZhuAction.GA_PLAYING) {
						builder.setHandPai(paiBuilder);
					}
					if (table.getWaitAction() != DouDiZhuAction.GA_PLAYING
							&& table.getZhuaType() == 2) {
						builder.setHandPai(paiBuilder);
					}

					builder.setRestPai(restPaiBuilder);// 闷抓情况下只有地主可以显示三张牌
					builder.setTable(table.serialize());
					builder.setWaitTime((int) (table.getCoolDownTime() / 1000));
					builder.setLastOutSeat(table.getLastOutPai());

					roleFunction.sendMessageToPlayer(role.getRole().getRid(),
							builder.build());
					LogUtil.info("getGameType" + game.getGameType() + " ："
							+ table.getWaitAction());
				}

				
			}

		} else {
			GMsg_12006005.Builder builder = GMsg_12006005.newBuilder();
			builder.setCurrentSeat(0);
			builder.setRoomId(0);
			roleFunction.sendMessageToPlayer(role.getRole().getRid(),
					builder.build());
		}

	}

	public void addRoleCache(ZTDoudizhuRole role) {
		LogUtil.info("增加角色到缓存");
		roleMap.put(role.getRole().getRole().getRid(), role);
	}

	public void removeRoleCache(Long id) {
		roleMap.remove(id);
	}

	/**
	 * 处理退出牌桌
	 * 
	 * @param game
	 * @param rid
	 */
	public void exitTable(Game game, Long rid) {
		ZTDoudizhuTable table = getTable(game.getRoomId());
		if (table != null) {
			GameRole gameRole = game.getSpriteMap().get(rid);
			if (gameRole != null) {

				Set<Long> rids = new HashSet<Long>();
				rids.addAll(game.getSpriteMap().keySet());
				
				game.getSpriteMap().remove(rid);
//				table.getMembers().remove(gameRole.getSeat() - 1);
				table.getMembers().set(gameRole.getSeat() - 1,null);
				gameRole.setStatus(PlayerState.PS_EXIT_VALUE);
				game.getRoles().set(gameRole.getSeat() - 1, 0l);

				removeRoleCache(rid);
				LogUtil.error(gameRole.getRole().getNick() + "rid" + rid);

				GMsg_12006005.Builder builder = GMsg_12006005.newBuilder();
				builder.setCurrentSeat(gameRole.getSeat());
				builder.setRoomId(game.getRoomId());
				roleFunction.sendMessageToPlayers(rids, builder.build());
				LogUtil.info("发送退出成功："+ builder.getRoomId());
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
				} else if (game.getSpriteMap().size() >= 2
						&& !game.isFriendRoom()) {
					roomFunction.insertReadyDeque(game);
				}
			}
		}

	}

	/**
	 * 初始化麻将牌局
	 *
	 * @param owner
	 * @return
	 */
	public ZTDoudizhuTable initTable(Game game) {
		if (game.getStatus() > 0) {
			return null;
		}
		ZTDoudizhuTable doudizhuTable = new ZTDoudizhuTable(game);
		// mahJiangTable.setOwner(owner.getId());
		for(int i = 0; i < 3; i++){
			doudizhuTable.getMembers().add(null);
		}
		for (Map.Entry<Long, GameRole> entry : game.getSpriteMap().entrySet()) {
			Long rid = entry.getKey();
			GameRole gameRole = game.getSpriteMap().get(rid);
			if (gameRole != null) {
				ZTDoudizhuRole ownerMember = new ZTDoudizhuRole(
						entry.getValue());
				doudizhuTable.getMembers().set(gameRole.getSeat() - 1, ownerMember);
				addRoleCache(ownerMember);
			}
		}

		tableMap.put(game.getRoomId(), doudizhuTable);

		game.setStatus(GameStatus.WAIT_READY);

		return doudizhuTable;
	}

	/**
	 * 通知操作结果并提交下一操作
	 */
	public void processAction(ZTDoudizhuTable table, DouDiZhuAction action,
			int nextSeat, int handleType, long time) {

		GMsg_12012006.Builder builder = GMsg_12012006.newBuilder();

		builder.setAction(action);
		builder.setTable(table.serialize());
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		table.setLastPlaySeat(nextSeat);
		table.setNextSeat(table.getNextPlaySeat());

		tableToWait(table, table.getLastPlaySeat(), table.getNextPlaySeat(),
				handleType, time);
	}

	public int getFriendMinSize(ZTDoudizhuTable table, ZTDoudizhuRole role) {
		if (role.getRole().getSeat() == table.getOwner()) {
			return 0;
		} else {
			for (ZTDoudizhuRole other : table.getMembers()) {
				if(other != null) {
					if (other.getRole().getSeat() != table.getOwner()
							&& other.getRole().getSeat() != role.getRole()
									.getSeat()) {
						return other.getPai().size();
					}
				}
			}

		}
		return 17;
	}

	public int getEnemyMinSize(ZTDoudizhuTable table, ZTDoudizhuRole role) {
		int min = 17;
		if (role.getRole().getSeat() == table.getOwner()) {
			for (ZTDoudizhuRole other : table.getMembers()) {
				if(other != null) {
					if (other.getRole().getSeat() != table.getOwner()) {
						if (other.getPai().size() < min) {
							min = other.getPai().size();
						}
					}
				}
			}
		} else {
			ZTDoudizhuRole owner = table.getMembers().get(table.getOwner() - 1);
			if(owner != null) {
				min = owner.getPai().size();
			}
			
		}
		return min;
	}

	/**
	 * 处理出牌的逻辑
	 */
	public void dealPaiOut(ZTDoudizhuTable table, List<Integer> data) {
		table.setQueueWaitType(0);
		int seat = table.getLastPlaySeat();
		ZTDoudizhuRole role = table.getMembers().get(seat - 1);
		if (role.getRole().isRobot()) {
			LogUtil.info("Ai" + role.getRole().getRole().getNick() + "正在出牌！！"
					+ "roomId = " + table.getGame().getRoomId());
		}

		if (!role.getRole().isRobot()) {
			LogUtil.info("！！！玩家" + role.getRole().getRole().getNick()
					+ " 正在出牌！！！roomId = " + table.getGame().getRoomId());
		}

		LogUtil.info(table.getGame().getRoomId() + " seat-" + seat + " nick-" + role.getRole().getRole().getNick()
				+" rid-" + role.getRole().getRole().getRid() + " data" + data);

		DouDiZhuAction action = null;
		int handleType = 0;
		if (data == null || data.isEmpty()) {
			action = DouDiZhuAction.GA_PASS_PLAYING;
			handleType = HandleType.DOUDIZHU_OUT_PAI_AUTO;
			role.setPassCount(role.getPassCount() + 1);

			table.setPassCount(table.getPassCount() + 1);
		} else {
			table.setLastOutPai(seat);
			table.setPassCount(0);

			action = DouDiZhuAction.GA_PLAYING;
			if (role.getPai().containsAll(data)) {
				role.getPai().removeAll(data);
				table.getLastPai().clear();
				table.getLastPai().addAll(data);

				table.getRecyclePai().addAll(data);
			} else {
				data = new ArrayList<Integer>();
			}
			role.setOutCount(role.getOutCount() + 1);
			handleType = HandleType.DOUDIZHU_OUT_PAI_AUTO;
		}

		if (data != null && data.size() > 0) {
			// 王炸
			List<Card> list = new ArrayList<Card>();
			for (Integer id : data) {
				list.add(new Card(id));
			}
			ZTDoudizhuRule.sortCards(list);
			CardType cardType = ZTDoudizhuRule.getCardType(list);
			if (cardType != null
					&& (cardType == CardType.ZHA_DAN || cardType == CardType.WANG_ZHA)) {
				if (cardType == CardType.ZHA_DAN) {
					role.setZhadanCount(role.getZhadanCount() + 1);
				} else {
					role.setWangzhaCount(role.getWangzhaCount() + 1);
				}
				int power = 0;
				ZTDoudizhuRole owner = null;
				for (ZTDoudizhuRole farmer : table.getMembers()) {
					if (farmer.getRole().getSeat() != table.getOwner()) {
						farmer.setCurrentPower(farmer.getCurrentPower() * 2);
						power += farmer.getCurrentPower();
					} else {
						owner = farmer;
					}
				}
				owner.setCurrentPower(power);
				/*** Mission-16* 特殊任务类型* 玩家牌型任务上报 ** PAIOUT *********************************/
				if (!role.getRole().isRobot()) {
					long rid = role.getRole().getRole().getRid();
					missionFunction.checkTaskFinish(rid, TaskType.daily_task,
							MissionType.CARD_TYPE, GameType.DOUDIZHU, cardType);
				}
				/* 玩家牌型任务上报 ********************************** */

			}
			if (cardType != null && cardType == CardType.FEI_JI) {
				role.setFeijiCount(role.getFeijiCount() + 1);
			}
			if (cardType != null && cardType == CardType.LIAN_DUI) {
				role.setFeijiCount(role.getFeijiCount() + 1);
			}
			if (cardType != null && cardType == CardType.SHUN_ZI) {
				role.setShunziCount(role.getShunziCount() + 1);
			}
		}

		table.getYetOptions().put(role.getRole().getSeat(), action);
		if (role.getPai().size() == 0) {

			dealResult(table, role);

		} else {
			processAction(table, action, table.getNextPlaySeat(), handleType,
					System.currentTimeMillis() + 500);
		}

	}

	/**
	 * 处理结束的逻辑
	 */
	public void dealResult(ZTDoudizhuTable table, ZTDoudizhuRole winner) {
		table.setWaitAction(DouDiZhuAction.GA_PLAYING);
		Game game = table.getGame();
		FriendRoom friendRoom = new FriendRoom();
		if (game.isFriendRoom()) {
			friendRoom = friendRoomFunction.getFriendRoom(game.getRoomId());
		}
		// 是否春天
		int isSpring = 0;
		ZTDoudizhuRole dizhu = table.getMembers().get(table.getOwner() - 1);
		if (winner != dizhu) {
			if (dizhu.getOutCount() <= 1) {
				isSpring = 1;

			}
		} else {
			boolean flag = true;
			for (ZTDoudizhuRole role : table.getMembers()) {
				if (role != dizhu && role.getOutCount() > 0) {
					flag = false;
				}
			}
			if (flag) {
				isSpring = 1;
			}
		}

		// 地主番数
		int power = 0;
		// 农民番数
		double[] farmerPower = new double[2];
		int j = 0;
		for (ZTDoudizhuRole farmer : table.getMembers()) {
			if (farmer.getOutCount() != 0 || farmer.getPassCount() != 0) {
				if (farmer.getRole().getSeat() > 0
						&& farmer.getRole().getSeat() != table.getOwner()) {
					if (isSpring == 1) {
						farmer.setCurrentPower(farmer.getCurrentPower() * 2);
					}
					if (j > 1) {
						LogUtil.error("member size:"
								+ table.getMembers().size());
						j = 1;
					}
					farmerPower[j] = farmer.getCurrentPower();
					power += farmer.getCurrentPower();
					j++;

				}
			}
		}
		dizhu.setCurrentPower(power);
		int highestPower;
		DoudizhuRoomCsv doudizhuRoomCsv = doudizhuRoomCache.getConfig(table
				.getGame().getRoomType());
		if (game.isFriendRoom()) {
			highestPower = friendRoom.getHighestPower();
		} else {
			highestPower = doudizhuRoomCsv.getMaxbeishu();
		}

		if (power > highestPower && highestPower != -1) {
			// 重新计算地主和农民倍数，只适用于计算，不改变实际倍数
			LogUtil.error("斗地主倍出超过了");
			if (farmerPower[0] > farmerPower[1]) {
				farmerPower[0] = (int) Math
						.ceil((farmerPower[0] * highestPower / power));
				farmerPower[1] = highestPower - farmerPower[0];
			} else if (farmerPower[1] > farmerPower[0]) {
				farmerPower[1] = (int) Math
						.ceil((farmerPower[1] * highestPower / power));
				farmerPower[0] = highestPower - farmerPower[1];
			} else {
				farmerPower[0] = highestPower / 2.0;
				farmerPower[1] = highestPower / 2.0;
			}
			power = highestPower;
		}

		GMsg_12012006.Builder actionBuilder = GMsg_12012006.newBuilder();

		actionBuilder.setAction(DouDiZhuAction.GA_PLAYING);
		actionBuilder.setTable(table.serialize());
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				actionBuilder.build());

		int base;
		int roundMaxScore;
		int dizhuMaxScore;

		// 地主和农民是否超出输赢底线
		boolean dizhuOut = false;
		boolean[] farmerOut = { false, false };
		// 农民1最多可以获得或损失的积分
		int[] farmersMaxScore = new int[2];
		int i = 0;
		if (game.isFriendRoom()) {
			base = friendRoom.getBaseChip();
			dizhuMaxScore = base * power;
			for (ZTDoudizhuRole farmer : table.getMembers()) {
				if (farmer.getRole().getSeat() != table.getOwner()) {
					farmersMaxScore[i] = (int) (farmerPower[i] * base);
					i = i + 1;
				}
			}
		} else {
			base = doudizhuRoomCsv.getPotOdds();
			// 本局理论上可以获得的积分
			roundMaxScore = base * power;
			// 地主最多可以获得或损失的积分
			dizhuMaxScore = roundMaxScore > dizhu.getRole().getRole().getGold() ? dizhu
					.getRole().getRole().getGold()
					: roundMaxScore;
			for (ZTDoudizhuRole farmer : table.getMembers()) {
				if (farmer.getRole().getSeat() != table.getOwner()) {
					Role role = farmer.getRole().getRole();
					if (dizhuMaxScore * farmerPower[i] / power > role.getGold()) {
						farmersMaxScore[i] = role.getGold();
						farmerOut[i] = true;
					} else {
						farmersMaxScore[i] = (int) (dizhuMaxScore
								* farmerPower[i] / power);
					}
					// farmersMaxScore[i] = (int) (dizhuMaxScore *
					// farmerPower[i]
					// / power > role.getGold() ? role.getGold()
					// : dizhuMaxScore * farmerPower[i] / power);
					i = i + 1;
				}
			}
			dizhuMaxScore = farmersMaxScore[0] + farmersMaxScore[1];
			if (dizhuMaxScore == dizhu.getRole().getRole().getGold()) {
				dizhuOut = true;
			}

		}

		GMsg_12012008.Builder endBuilder = GMsg_12012008.newBuilder();
		// 农民下标
		int farmerIndex = 0;
		// G_T_C 斗地主日志角色信息集合
		List<Object[]> roleInfoList = new ArrayList<Object[]>();
		List<Long> winList = new ArrayList<Long>();
		int aiGold = 0;
		int realMemberCount = 0;
		j = 0;
		for (ZTDoudizhuRole doudizhuRole : table.getMembers()) {
			GDouDiZhuEnd.Builder end = GDouDiZhuEnd.newBuilder();
			boolean isDiZhu;
			boolean isSuccess;
			boolean isAI;
			if (winner != dizhu) {
				// 农民赢
				if (doudizhuRole == dizhu) {
					// end.setGold(-score);
					end.setGoldOut(dizhuOut ? 2 : 0);
					end.setGold(-dizhuMaxScore);
					end.setType(1);
					isDiZhu = true;
					isSuccess = false;
					if (!doudizhuRole.getRole().isRobot()) {
						isAI = false;
						Role role = doudizhuRole.getRole().getRole();
						ExpCsv expCsv = expCache.getConfig(role.getLevel());
						roomFunction.dealValue(table.getGame(), role, isAI,
								isSuccess, dizhuMaxScore,
								expCsv.getDoudizhuLoseExp(), 0, 0,
								MoneyEvent.DOUDIZHU);
						doudizhuDataFunction.updateDoudizhuData(role.getRid(),
								isDiZhu, doudizhuRole.getCurrentPower(),
								dizhuMaxScore, isSuccess,
								doudizhuRole.getWangzhaCount(),
								doudizhuRole.getZhadanCount());

						// 任务检测
						missionFunction.checkTaskFinish(role.getRid(),
								TaskType.daily_task, MissionType.CONTINUE_WIN,
								GameType.DOUDIZHU, false);

					} else {
						isAI = true;
						// AI实际盈亏，排除了AI对AI的计算
						Role role = doudizhuRole.getRole().getRole();
						ExpCsv expCsv = expCache.getConfig(role.getLevel());
						int lost = 0;
						int k = 0;
						for (ZTDoudizhuRole doudizhuRole1 : table.getMembers()) {
							if (doudizhuRole1 != dizhu) {

								if (!doudizhuRole1.getRole().isRobot()) {
									lost = lost + farmersMaxScore[k];
								}
								k = k + 1;
							}
						}
						roomFunction.dealValue(table.getGame(), role, isAI,
								isSuccess, dizhuMaxScore,
								expCsv.getDoudizhuLoseExp(), 0, lost,
								MoneyEvent.DOUDIZHU);
						// 设置机器人场数信息
						doudizhuRole.getRole().setRobotLost();
						NPCFunction.setRobotLost(doudizhuRole.getRole()
								.getRole().getRid(), table.getGame()
								.getGameType(), table.getGame().getRoomType());
						aiGold -= dizhuMaxScore;
					}
				} else {
					// end.setGold(score);
					end.setGold(farmersMaxScore[farmerIndex]);
					end.setGoldOut(farmerOut[farmerIndex] ? 1 : 0);
					isDiZhu = false;
					isSuccess = true;
					Role role = doudizhuRole.getRole().getRole();
					ExpCsv expCsv = expCache.getConfig(role.getLevel());
					if (!doudizhuRole.getRole().isRobot()) {
						isAI = false;
						int exchange = 0;
						if (table.getGame().getRoomType() == 3
								|| table.getGame().getRoomType() == 4) {
							// 兑换成奖劵，AI不需要存库
							exchange = (int) Math
									.floor(farmersMaxScore[farmerIndex]
											/ doudizhuRoomCsv
													.getExchangeLottery());
							end.setGold(exchange > 0 ? exchange : 1);
							farmersMaxScore[farmerIndex] = 0;
							end.setType(2);
						} else {
							end.setType(1);
						}
						roomFunction.dealValue(table.getGame(), role, isAI,
								isSuccess, farmersMaxScore[farmerIndex],
								expCsv.getDoudizhuWinExp(), exchange, 0,
								MoneyEvent.DOUDIZHU);
						doudizhuDataFunction.updateDoudizhuData(role.getRid(),
								isDiZhu, doudizhuRole.getCurrentPower(),
								farmersMaxScore[farmerIndex], isSuccess,
								doudizhuRole.getWangzhaCount(),
								doudizhuRole.getZhadanCount());

						// 任务检测
						missionFunction.checkTaskFinish(role.getRid(),
								TaskType.daily_task, MissionType.WIN,
								GameType.DOUDIZHU, true);
						missionFunction.checkTaskFinish(role.getRid(),
								TaskType.daily_task, MissionType.CONTINUE_WIN,
								GameType.DOUDIZHU, true);
						doLotteryTask(doudizhuRole);
					} else {
						isAI = true;
						int exchange = 0;
						int lost = 0;
						// ai实际盈亏
						if (!dizhu.getRole().isRobot()) {
							lost = farmersMaxScore[farmerIndex];
						}
						if (table.getGame().getRoomType() == 3
								|| table.getGame().getRoomType() == 4) {
							// 兑换成奖劵，AI不需要存库
							exchange = (int) Math
									.floor(farmersMaxScore[farmerIndex]
											/ doudizhuRoomCsv
													.getExchangeLottery());
							end.setGold(exchange > 0 ? exchange : 1);
							end.setType(2);
						} else {
							end.setType(1);
						}

						roomFunction.dealValue(table.getGame(), role, isAI,
								isSuccess, farmersMaxScore[farmerIndex],
								expCsv.getDoudizhuWinExp(), exchange, lost,
								MoneyEvent.DOUDIZHU);
						// 设置机器人场数信息
						doudizhuRole.getRole().setRobotWin();
						NPCFunction.setRobotWin(doudizhuRole.getRole()
								.getRole().getRid(), table.getGame()
								.getGameType(), table.getGame().getRoomType());
						aiGold += farmersMaxScore[farmerIndex];
					}
					farmerIndex = farmerIndex + 1;

					// G_T_C 添加一个农民赢了的角色id
					winList.add(doudizhuRole.getRole().getRole().getRid());
				}

			} else {
				// 地主赢
				if (doudizhuRole == dizhu) {
					// end.setGold(score);
					end.setGold(dizhuMaxScore);
					end.setGoldOut(dizhuOut ? 1 : 0);
					isDiZhu = true;
					isSuccess = true;
					Role role = doudizhuRole.getRole().getRole();
					ExpCsv expCsv = expCache.getConfig(role.getLevel());
					if (!doudizhuRole.getRole().isRobot()) {
						isAI = false;
						int exchange = 0;
						if (table.getGame().getRoomType() == 3
								|| table.getGame().getRoomType() == 4) {
							// 兑换成奖劵，AI不需要存库
							exchange = (int) Math.floor(dizhuMaxScore
									/ doudizhuRoomCsv.getExchangeLottery());
							end.setGold(exchange > 0 ? exchange : 1);
							end.setType(2);
							dizhuMaxScore = 0;
						} else {
							end.setType(1);
						}
						roomFunction.dealValue(table.getGame(), role, isAI,
								isSuccess, dizhuMaxScore,
								expCsv.getDoudizhuWinExp(), exchange, 0,
								MoneyEvent.DOUDIZHU);
						doudizhuDataFunction.updateDoudizhuData(role.getRid(),
								isDiZhu, doudizhuRole.getCurrentPower(),
								dizhuMaxScore, isSuccess,
								doudizhuRole.getWangzhaCount(),
								doudizhuRole.getZhadanCount());

						// 任务检测
						missionFunction.checkTaskFinish(role.getRid(),
								TaskType.daily_task, MissionType.WIN,
								GameType.DOUDIZHU, true);
						missionFunction.checkTaskFinish(role.getRid(),
								TaskType.daily_task, MissionType.CONTINUE_WIN,
								GameType.DOUDIZHU, true);
						doLotteryTask(doudizhuRole);
					} else {
						isAI = true;
						int exchange = 0;
						if (table.getGame().getRoomType() == 3
								|| table.getGame().getRoomType() == 4) {
							// 兑换成奖劵，AI不需要存库
							exchange = (int) Math.floor(dizhuMaxScore
									/ doudizhuRoomCsv.getExchangeLottery());
							end.setGold(exchange > 0 ? exchange : 1);
							end.setType(2);
						} else {
							end.setType(1);
						}
						// AI实际盈亏，排除了AI对AI的计算
						int lost = 0;
						int k = 0;
						for (ZTDoudizhuRole doudizhuRole1 : table.getMembers()) {
							if (doudizhuRole1 != dizhu) {

								if (!doudizhuRole1.getRole().isRobot()) {
									lost = lost + farmersMaxScore[k];
								}
								k = k + 1;
							}
						}
						// 设置机器人场数信息
						roomFunction.dealValue(table.getGame(), role, isAI,
								isSuccess, dizhuMaxScore,
								expCsv.getDoudizhuWinExp(), exchange, lost,
								MoneyEvent.DOUDIZHU);
						doudizhuRole.getRole().setRobotWin();
						NPCFunction.setRobotWin(doudizhuRole.getRole()
								.getRole().getRid(), table.getGame()
								.getGameType(), table.getGame().getRoomType());

						aiGold += dizhuMaxScore;
					}

					// G_T_C 添加一个地主赢了的角色id
					winList.add(doudizhuRole.getRole().getRole().getRid());
				} else {
					// end.setGold(-score);
					end.setGold(-farmersMaxScore[farmerIndex]);
					end.setGoldOut(farmerOut[farmerIndex] ? 2 : 0);
					end.setType(1);
					isDiZhu = false;
					isSuccess = false;
					Role role = doudizhuRole.getRole().getRole();
					ExpCsv expCsv = expCache.getConfig(role.getLevel());
					if (!doudizhuRole.getRole().isRobot()) {
						isAI = false;
						roomFunction.dealValue(table.getGame(), role, isAI,
								isSuccess, farmersMaxScore[farmerIndex],
								expCsv.getDoudizhuLoseExp(), 0, 0,
								MoneyEvent.DOUDIZHU);
						doudizhuDataFunction.updateDoudizhuData(role.getRid(),
								isDiZhu, doudizhuRole.getCurrentPower(),
								farmersMaxScore[farmerIndex], isSuccess,
								doudizhuRole.getWangzhaCount(),
								doudizhuRole.getZhadanCount());

						// 任务检测
						missionFunction.checkTaskFinish(role.getRid(),
								TaskType.daily_task, MissionType.CONTINUE_WIN,
								GameType.DOUDIZHU, false);
					} else {
						// AI实际盈亏，排除了AI对AI的计算
						isAI = true;
						int lost = 0;
						if (!dizhu.getRole().isRobot()) {
							lost = farmersMaxScore[farmerIndex];
						}
						if (expCsv != null) {
							roomFunction.dealValue(table.getGame(), role, isAI,
									isSuccess, farmersMaxScore[farmerIndex],
									expCsv.getDoudizhuLoseExp(), 0, lost,
									MoneyEvent.DOUDIZHU);
						}

						// 设置机器人场数信息
						doudizhuRole.getRole().setRobotLost();
						NPCFunction.setRobotLost(doudizhuRole.getRole()
								.getRole().getRid(), table.getGame()
								.getGameType(), table.getGame().getRoomType());
						aiGold -= farmersMaxScore[farmerIndex];
					}
					farmerIndex = farmerIndex + 1;
				}
			}
			GDouDiZhuPai.Builder value = GDouDiZhuPai.newBuilder();
			value.addAllPai(doudizhuRole.getPai());

			end.setCurrentPower(doudizhuRole.getCurrentPower());
			end.setSeat(doudizhuRole.getRole().getSeat());

			end.setPai(value);
			endBuilder.addEndInfo(end);

			// G_T_C组装单个角色信息日志
			Object[] objs = doudizhuLogDao.getRoleInfo(doudizhuRole, isDiZhu,
					end.getGold());
			roleInfoList.add(objs);
			// 任务检测
			missionFunction.checkTaskFinish(doudizhuRole.getRole().getRole()
					.getRid(), TaskType.daily_task, MissionType.TIMES,
					GameType.DOUDIZHU);

			if (!doudizhuRole.getRole().isRobot()) {
				realMemberCount++;
			}

		}
		endBuilder.setIsSpring(isSpring);

		LogUtil.info("通知结算结果");
		for (long rid : table.getGame().getRoles()) {
			LogUtil.info(".....rid:" + rid);
		}
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				endBuilder.build());

		table.getGame().setStatus(GameStatus.END_REWARD);
		table.getGame().setEndTime(System.currentTimeMillis());

		// G_T_C add 斗地主日志处理
		String roleInfo = StringUtil.listArrayToString(roleInfoList,
				StringUtil.DELIMITER_BETWEEN_ITEMS,
				StringUtil.DELIMITER_INNER_ITEM);
		LogUtil.debug(roleInfo);
		DoudizhuLog doudizhuLog = doudizhuLogDao.get(table, StringUtil
				.listToString(winList, StringUtil.DELIMITER_BETWEEN_ITEMS),
				roleInfo, aiGold, table.getMembers().size(), realMemberCount);
		doudizhuLogDao.addLog(doudizhuLog);

		for (ZTDoudizhuRole role : table.getMembers()) {
			if (role.getRole() != null) {
				// G_T_C 处理房间登录日志
				Role role2 = role.getRole().getRole();
				if (role2 != null && game != null && !role.getRole().isRobot()) {
					roomLogFunction.dealRoomRoleLoginLog(game.getGameType(),
							role2, role2.getLastLoginIp(), 1);
				}
			}
		}

		for (ZTDoudizhuRole role : table.getMembers()) {
			if (role.getRole() != null
					&& role.getRole().getStatus() == PlayerState.PS_EXIT_VALUE
					|| role.getRole().getRole().getOnline() == 0) {
				// TODO
				// table.getGame().setEndTime(System.currentTimeMillis()-60000);
				GMsg_12006005.Builder builder = GMsg_12006005.newBuilder();
				builder.setCurrentSeat(role.getRole().getSeat());
				builder.setRoomId(game.getRoomId());
				roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
						builder.build());
				roomFunction.endGame(table.getGame());
				return;
			}
		}
		for (ZTDoudizhuRole role : table.getMembers()) {
			if (role.getRole() != null && role.getRole().isRobot()) {
				if (Math.random() < (0.3 + role.getRole().getAICount() * 0.1)) {
					roomFunction.AIWantByeOrReady(table.getGame(), role
							.getRole().getRole().getRid(),
							HandleType.AI_END_EXIT);
				}
			}
		}
		if (game.isFriendRoom()) {
			// 是好友房，判断是否到达局数退出
			friendRoomFunction.roundEnd(game.getRoomId());
		}
	}

	private void doLotteryTask(ZTDoudizhuRole doudizhuRole) {
		GameRole gameRole = doudizhuRole.getRole();
		if (gameRole == null) {
			return;
		}
		Role role = gameRole.getRole();
		missionFunction.checkLotteryTaskCompleted(role, LotteryTaskType.WIN, 1);
		if (doudizhuRole.getFeijiCount() > 0) {
			missionFunction.checkLotteryTaskCompleted(role,
					LotteryTaskType.FEI_JI_1S, doudizhuRole.getFeijiCount());
		}
		if (doudizhuRole.getShunziCount() > 0) {
			missionFunction.checkLotteryTaskCompleted(role,
					LotteryTaskType.SHUN_ZI_1S, doudizhuRole.getShunziCount());
		}
		if (doudizhuRole.getLianduiCount() > 0) {
			missionFunction.checkLotteryTaskCompleted(role,
					LotteryTaskType.LIAN_DUI__1S,
					doudizhuRole.getLianduiCount());
		}
		if (doudizhuRole.getWangzhaCount() > 0) {
			missionFunction
					.checkLotteryTaskCompleted(role,
							LotteryTaskType.WANG_ZHA_1S,
							doudizhuRole.getWangzhaCount());
		}
		if (doudizhuRole.getZhadanCount() > 0) {
			missionFunction.checkLotteryTaskCompleted(role,
					LotteryTaskType.ZHA_DAN_1S, doudizhuRole.getZhadanCount());
		}

	}

	/**
	 * 处理抓的逻辑
	 */
	public void dealZhua(ZTDoudizhuTable table, int zhuaType) {
		table.setQueueWaitType(0);
		int seat = table.getLastPlaySeat();
		ZTDoudizhuRole role = table.getMembers().get(seat - 1);
		table.setZhuaType(zhuaType);

		DouDiZhuAction action = null;
		int handleType = 0;
		if (zhuaType == 1) {
			int power = 0;
			for (ZTDoudizhuRole farmer : table.getMembers()) {
				if (farmer.getRole().getSeat() != seat) {
					farmer.setCurrentPower(farmer.getCurrentPower() * 2);
					power += farmer.getCurrentPower();
				}
			}
			role.setCurrentPower(power);

			role.getPai().addAll(table.getPais());
			table.setOwner(seat);
			action = DouDiZhuAction.GA_DARK_GRAB;

			if (table.getNoZhuaCount() >= 2) {
				handleType = HandleType.DOUDIZHU_PAI;
			} else {
				handleType = HandleType.DOUDIZHU_DAO;
				if (table.getGame().isFriendRoom()) {
					table.setWaitAction(DouDiZhuAction.GA_UPSIDE);
				}
			}

		} else if (zhuaType == 2) {
			int power = 0;
			for (ZTDoudizhuRole farmer : table.getMembers()) {
				if (farmer.getRole().getSeat() != seat) {
					farmer.setCurrentPower(farmer.getCurrentPower());
					power += farmer.getCurrentPower();
				}
			}
			role.setCurrentPower(power);

			role.getPai().addAll(table.getPais());
			table.setOwner(seat);
			action = DouDiZhuAction.GA_MING_GRAB;
			handleType = HandleType.DOUDIZHU_DAO;
			if (table.getGame().isFriendRoom()) {
				if (table.getNoZhuaCount() >= 2) {

				} else {
					table.setWaitAction(DouDiZhuAction.GA_UPSIDE);
				}
			}

		} else {
			table.setNoZhuaCount(table.getNoZhuaCount() + 1);
			action = DouDiZhuAction.GA_PASS_GRAB;
			handleType = HandleType.DOUDIZHU_ZHUA;
			if (table.getGame().isFriendRoom()) {
				table.setWaitAction(DouDiZhuAction.GA_UPSIDE);
			}
		}

		if (handleType == HandleType.DOUDIZHU_PAI) {
			processAction(table, action, table.getOwner(), handleType,
					System.currentTimeMillis() + 500);
		} else {
			processAction(table, action, table.getNextPlaySeat(), handleType,
					System.currentTimeMillis() + 500);
		}
		// 抓时所有人明牌
		if (zhuaType == 2) {
			for (ZTDoudizhuRole member : table.getMembers()) {
				GMsg_12012007.Builder builder = GMsg_12012007.newBuilder();
				GDouDiZhuPai.Builder value = GDouDiZhuPai.newBuilder();
				value.addAllPai(member.getPai());
				builder.setPai(value);
				roleFunction.sendMessageToPlayer(member.getRole().getRole()
						.getRid(), builder.build());
			}
		}
	}

	/**
	 * 处理倒的逻辑
	 */
	public void dealDao(ZTDoudizhuTable table, int daoType) {
		table.setQueueWaitType(0);
		int seat = table.getLastPlaySeat();
		ZTDoudizhuRole role = table.getMembers().get(seat - 1);

		DouDiZhuAction action = null;
		int handleType = 0;
		if (daoType == 1) {
			role.setCurrentPower(role.getCurrentPower() * 2);
			// 地主番数
			ZTDoudizhuRole dizhu = table.getMembers().get(table.getOwner() - 1);
			int power = 0;
			for (ZTDoudizhuRole farmer : table.getMembers()) {
				if(farmer != null) {
					if (farmer.getRole().getSeat() != table.getOwner()) {
						power += farmer.getCurrentPower();
					}
				}
			}
			dizhu.setCurrentPower(power);
			table.setDaoCount(table.getDaoCount() + 1);
			table.setLastDao(seat);
			action = DouDiZhuAction.GA_UPSIDE;
		} else {

			action = DouDiZhuAction.GA_PASS_UPSIDE;

		}

		if (table.getNoZhuaCount() == 2) {
			handleType = HandleType.DOUDIZHU_PAI;
			processAction(table, action, table.getOwner(), handleType,
					System.currentTimeMillis() + 1000);
		} else {
			handleType = HandleType.DOUDIZHU_DAO;
			if (table.getNoZhuaCount() == 1) {
				table.setWaitAction(DouDiZhuAction.GA_PULL);
				processAction(table, action, table.getOwner(), handleType,
						System.currentTimeMillis() + 1000);
			} else {
				table.setWaitAction(DouDiZhuAction.GA_UPSIDE);
				processAction(table, action, table.getNextPlaySeat(),
						handleType, System.currentTimeMillis() + 1000);
			}
		}

	}

	/**
	 * 处理拉的逻辑
	 */
	public void dealLa(ZTDoudizhuTable table, int laType) {
		table.setQueueWaitType(0);
		int seat = table.getLastPlaySeat();
		ZTDoudizhuRole role = table.getMembers().get(seat - 1);

		table.setWaitAction(DouDiZhuAction.GA_PULL);
		DouDiZhuAction action = null;
		int handleType = 0;
		if (laType == 1) {

			// 只增加倒的倍数
			int power = 0;
			for (ZTDoudizhuRole farmer : table.getMembers()) {
				if (farmer.getRole().getSeat() != table.getOwner()) {
					if (table.getDaoCount() > 1) {
						farmer.setCurrentPower(farmer.getCurrentPower() * 2);
						power += farmer.getCurrentPower();
					} else if (table.getDaoCount() == 1
							&& farmer.getRole().getSeat() == table.getLastDao()) {
						farmer.setCurrentPower(farmer.getCurrentPower() * 2);
						power += farmer.getCurrentPower();
					} else {
						power += farmer.getCurrentPower();
					}
				}
			}
			role.setCurrentPower(power);

			action = DouDiZhuAction.GA_PULL;
			handleType = HandleType.DOUDIZHU_PAI;
		} else {
			action = DouDiZhuAction.GA_PASS_PULL;
			handleType = HandleType.DOUDIZHU_PAI;
		}

		// 地主牌
		processAction(table, action, seat, handleType,
				System.currentTimeMillis() + 1000);

	}

	/**
	 * 处理看牌的逻辑
	 */
	public void dealLook(ZTDoudizhuTable table) {
		table.setQueueWaitType(0);
		int seat = table.getLastPlaySeat();
		ZTDoudizhuRole me = table.getMembers().get(seat - 1);
		me.setLookedPai(1);

		int paiType = 0;
		GMsg_12012007.Builder builder = GMsg_12012007.newBuilder();
		GDouDiZhuPai.Builder value = GDouDiZhuPai.newBuilder();
		value.addAllPai(me.getPai());
		builder.setPai(value);
		roleFunction.sendMessageToPlayer(me.getRole().getRole().getRid(),
				builder.build());

		// 计算牌型
		List<Card> cardList = new ArrayList<Card>();
		List<Integer> pai = me.getPai();
		for (Integer integer : pai) {
			cardList.add(new Card(integer));
		}
		if (table.getZhuaType() != 1)// 闷抓不抓
		{
			if (ZTDoudizhuRule.zhua(cardList)) {
				paiType = 1;
			}
		}
		if (table.getGame().isFriendRoom()) {
			if (paiType == 1) {
				table.setWaitAction(DouDiZhuAction.GA_UPSIDE);
			} else {
				table.setWaitAction(DouDiZhuAction.GA_MING_GRAB);
			}
		}
		if (paiType == 1) {

			// 重新倒计时
			processAction(table, DouDiZhuAction.GA_LOOK_CARD, seat,
					HandleType.DOUDIZHU_BI_ZHUA,
					System.currentTimeMillis() + 300);

			// dealZhua(table, 2);
		} else {
			// 重新倒计时
			processAction(table, DouDiZhuAction.GA_LOOK_CARD, seat,
					HandleType.DOUDIZHU_ZHUA, System.currentTimeMillis() + 1000);
		}

	}

	/**
	 * 初始化斗地主牌
	 *
	 * @return
	 */
	public List<Integer> initAllPai() {

		List<Integer> allPai = MathUtil.generateDifNums(54, 1, 54);
		return allPai;
	}

	/**
	 * 处理超时
	 */
	public void tableToWait(ZTDoudizhuTable table, int targetSeat,
			int nextSeat, int hanlerType, long coolDownTime) {
		table.setLastPlaySeat(targetSeat);
		table.setNextSeat(nextSeat);
		table.setCoolDownTime(coolDownTime);
		table.setQueueWaitType(hanlerType);
	}

	/**
	 * 流程处理
	 */
	public void autoAction() {
		for (Map.Entry<Long, ZTDoudizhuTable> entry : tableMap.entrySet()) {
			ZTDoudizhuTable table = entry.getValue();
			if (table.getGame().getStatus() > GameStatus.RUNNING
					|| table.getQueueWaitType() == 0) {
				continue;
			}
			if (table.getGame().isFriendRoom()
					&& (table.getQueueWaitType() != HandleType.DOUDIZHU_DAO
							&& table.getQueueWaitType() != HandleType.DOUDIZHU_PAI
							&& table.getQueueWaitType() != HandleType.DOUDIZHU_OUT_PAI_AUTO
							&& table.getQueueWaitType() != HandleType.DOUDIZHU_ZHUA
							&& table.getQueueWaitType() != HandleType.DOUDIZHU_BI_DAO
							&& table.getQueueWaitType() != HandleType.DOUDIZHU_BI_ZHUA && table
							.getQueueWaitType() != HandleType.DOUDIZHU_LAST_MEN_ZHUA)) {
				continue;
			}
			manager.executeTask(new SingleThreadTask(table) {
				@Override
				public void doTask(ISingleData singleData) {
					ZTDoudizhuTable table = (ZTDoudizhuTable) singleData;

					// 还在倒计时中
					long currentTimeMillis = System.currentTimeMillis();
					long coolDownTime = table.getCoolDownTime();
					if (coolDownTime > currentTimeMillis) {
						return;
					}
					// 处理完变成没事件
					int tableState = table.getQueueWaitType();
					try {
						table.setQueueWaitType(0);
						Event event = new Event(tableState, table);
						DispatchEvent.dispacthEvent(event);
					} catch (Exception e) {
						table.setQueueWaitType(tableState);
						table.setCoolDownTime(currentTimeMillis + 5000);
						LogUtil.error("斗地主流程处理异常:"
								+ ExceptionUtils.getStackTrace(e));
					}
				}
			});
		}
	}

	static {
		// 低级房统计初始化,中级房统计初始化,高级房统计初始化
		for (int i = 1; i <= GameRoom.ZTDOUDIZHUROOMNUM; i++) {
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
	public AtomicIntegerArray getRoomCountCacheByRoomType(int roomType) {
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

}
