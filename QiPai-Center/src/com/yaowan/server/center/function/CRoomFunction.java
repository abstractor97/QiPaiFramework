package com.yaowan.server.center.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameStatus;
import com.yaowan.constant.GameType;
import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.csv.cache.AppGameCache;
import com.yaowan.framework.core.GlobalVar;
import com.yaowan.framework.core.thread.ISingleData;
import com.yaowan.framework.core.thread.SingleThreadManager;
import com.yaowan.framework.core.thread.SingleThreadTask;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.MathUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.GameRole;
import com.yaowan.model.struct.GameServer;
import com.yaowan.protobuf.center.CGame.CGameInfo;
import com.yaowan.protobuf.center.CGame.CGameRole;
import com.yaowan.protobuf.center.CGame.CMsg_22100003;
import com.yaowan.protobuf.game.GGame.GMsg_12006005;
import com.yaowan.server.center.model.RoleModel;
import com.yaowan.server.game.model.data.entity.Role;

/**
 * 跨服大厅数据服务
 * 
 * @author KKON
 *
 */
@Component
public class CRoomFunction extends FunctionAdapter {

	@Autowired
	private AppGameCache appGameCache;
	@Autowired
	private CRegisterFunction registerFunction;
	@Autowired
	private SingleThreadManager singleThreadManager;

	/** 游戏缓存<roomId, Game> */
	private ConcurrentHashMap<Long, Game> runningGameMap = new ConcurrentHashMap<>();

	/** 已经结束的游戏缓存<roomId, Game> */
	private Map<Long, Game> endedGameMap = new HashMap<>();

	/** 大厅玩家信息缓存 */
	private Map<Long, RoleModel> roleModelMap = new HashMap<>();

	/** 游戏排队队列<realType, roles> */
	private Map<Integer, ConcurrentLinkedDeque<Role>> gameDequeMap = new HashMap<>();

	/** 游戏在线人数 < gameType, < roomType, 人数>> */
	private ConcurrentHashMap<Integer, Map<Integer, AtomicInteger>> onlineCache = new ConcurrentHashMap<>();

	/** 游戏类型在线人数 < gameType,人数> */
	private Map<Integer, Integer> gameOnlineCache = new ConcurrentHashMap<Integer, Integer>();

	/**
	 * 准备参与游戏的玩家
	 */
	private ConcurrentHashMap<Integer, Integer> waitTimeMap = new ConcurrentHashMap<>();

	/**
	 * 获取大厅玩家信息
	 * 
	 * @param role
	 * @return
	 */
	private RoleModel getRoleModel(Role role) {
		RoleModel roleModel = roleModelMap.get(role.getRid());
		if (roleModel == null) {
			roleModel = new RoleModel();
			roleModel.setRid(role.getRid());
			roleModel.setRole(role);
		}
		return roleModel;
	}

	/**
	 * 重置大厅玩家信息
	 * 
	 * @param rid
	 */
	private void resetRoleModel(long rid) {
		RoleModel roleModel = roleModelMap.get(rid);
		if (roleModel == null) {
			return;
		}
		roleModel.setRealType(0);
		roleModel.setRoomId(0);
	}

	@Autowired
	private GlobalVar globalVar;

	long nextRefreshTime = 0l;

	@Override
	public void handleOnServerStart() {

		for (Integer id : appGameCache.allGame) {
			for (int j = 1; j <= 3; j++) {
				int realType = getRealType(id, j);
				gameDequeMap.put(realType, new ConcurrentLinkedDeque<Role>());

				// 初始化在线人数
				Map<Integer, AtomicInteger> data = getRoomOnline(id);
				data.put(j, new AtomicInteger());

				waitTimeMap.put(realType, 0);
			}
		}

	}


	public void handleOnRoleLogout(Role role) {
		cancelReadyToGame(role);
		Game game = getGameByRole(role);
		if (game != null) {
			if (game.getStatus() == GameStatus.WAIT_READY || game.getStatus() == GameStatus.END_REWARD || game.getStatus() == GameStatus.NO_READY) {
				GMsg_12006005.Builder builder = GMsg_12006005.newBuilder();
				GameRole gameRole = game.getSpriteMap().get(role.getRid());
				builder.setCurrentSeat(gameRole.getSeat());
				/*
				 * roleFunction.sendMessageToPlayers(game.getRoles(),
				 * builder.build());
				 */

				clear(game);

			} else if (game.getStatus() == GameStatus.RUNNING) {
				GameRole gameRole = game.getSpriteMap().get(role.getRid());
				gameRole.setAuto(true);
			}
		}
	}

	private void decrementOnline(int gameType, int roomType) {
		// 在线人数
		Map<Integer, AtomicInteger> data = onlineCache.get(gameType);
		AtomicInteger count = data.get(roomType);
		count.decrementAndGet();
		data.put(roomType, count);
	}

	public Map<Integer, Integer> getGameOnline() {
		// 需要定时刷新统计
		long time = System.currentTimeMillis();
		if (time > nextRefreshTime) {
			nextRefreshTime = time + 5000;
			for (Map.Entry<Integer, Map<Integer, AtomicInteger>> entry : onlineCache.entrySet()) {
				Map<Integer, AtomicInteger> map = getRoomOnline(entry.getKey());
				int count = 0;
				for (Map.Entry<Integer, AtomicInteger> subEntry : map.entrySet()) {
					count += subEntry.getValue().get();
				}
				gameOnlineCache.put(entry.getKey(), count);
			}
		}
		return gameOnlineCache;
	}

	public Map<Integer, AtomicInteger> getRoomOnline(Integer gameType) {
		Map<Integer, AtomicInteger> map = onlineCache.get(gameType);
		if (map == null) {
			map = new HashMap<Integer, AtomicInteger>();
			onlineCache.put(gameType, map);
		}
		return map;
	}

	public int getWaitTime(int gameType, int roomType) {
		return waitTimeMap.get(getRealType(gameType, roomType));
	}

	public int getRealType(int gameType, int roomType) {
		return gameType * 10000000 + roomType;
	}

	public int getGameType(int realType) {
		int roomType = realType % 10000000;
		int gameType = (realType - roomType) / 10000000;
		return gameType;
	}

	public int getGameMinRole(int realType) {
		int gameType = getGameType(realType);
		int num = 4;
		if (gameType == GameType.MENJI) {
			num = 4;
		} else if (gameType == GameType.DOUDIZHU) {
			num = 3;
		} else if (gameType == GameType.MAJIANG) {
			num = 4;
		} else if (gameType == GameType.ZXMAJIANG) {
			num = 4;
		} else if (gameType == GameType.CDMAJIANG) {
			num = 4;
		}
		return num;
	}

	public Game newGame(int realType) {
		Game game = new Game(globalVar.actionId.getAndIncrement(), realType);
		game.setNeedCount(getGameMinRole(game.getGameType()));
		runningGameMap.put(game.getRoomId(), game);
		return game;
	}

	public Game getGameByRole(Role role) {
		if (roleModelMap.containsKey(role.getRid())) {
			RoleModel roleModel = getRoleModel(role);
			long roomId = roleModel.getRoomId();
			return getGame(roomId);
		}

		return null;
	}

	public GameRole getGameRole(Role role) {
		Game game = getGameByRole(role);
		if (game != null) {
			return game.getSpriteMap().get(role.getRid());
		}
		return null;
	}

	public Game getGame(long id) {
		return runningGameMap.get(id);
	}

	public Map<Long, Game> getRunningGames() {
		return runningGameMap;
	}



	/**
	 * 玩家是否在游戏中
	 * 
	 * @param role
	 * @return
	 */
	public boolean isInGame(Role role) {
		if (roleModelMap.containsKey(role.getRid())) {
			return false;
		}
		Game game = getGameByRole(role);
		if (game == null) {
			return false;
		}
		if (game.getStatus() > 2) {
			return false;
		}
		return true;
	}

	/**
	 * 准备游戏
	 * 
	 * @param role
	 * @return
	 */
	public boolean getReadyToGame(Role role, int realType) {
		// LogUtil.info(role.getNick() + " realType " + realType);

		if (roleModelMap.containsKey(role.getRid())) {
			return false;
		} else {
			if (!gameDequeMap.containsKey(realType)) {
				return false;
			}
			RoleModel roleModel = getRoleModel(role);
			roleModel.setRealType(realType);
			gameDequeMap.get(realType).add(role);

			// 在线人数
			int roomType = realType % 10000000;
			int gameType = (realType - roomType) / 10000000;
			Map<Integer, AtomicInteger> data = onlineCache.get(gameType);
			AtomicInteger count = data.get(roomType);
			count.incrementAndGet();
			data.put(roomType, count);
			return true;
		}
	}

	/**
	 * 是否已经准备中
	 * 
	 * @param role
	 * @return
	 */
	public boolean isReadyMatching(Role role) {
		if (roleModelMap.containsKey(role.getRid())) {
			RoleModel roleModel = getRoleModel(role);
			return roleModel.getRealType() > 0;
		}
		return false;
	}

	public ConcurrentLinkedDeque<Role> getGameDeque(int realType) {
		return gameDequeMap.get(realType);
	}

	/**
	 * 取消游戏准备（比如掉线）
	 * 
	 * @param role
	 */
	public void cancelReadyToGame(Role role) {
		if (roleModelMap.containsKey(role.getRid())) {

			RoleModel roleModel = getRoleModel(role);

			int realType = roleModel.getRealType();
			getGameDeque(roleModel.getRealType()).remove(role);
			// 0代表没有准备
			roleModel.setRealType(0);
			int roomType = realType % 10000000;
			int gameType = (realType - roomType) / 10000000;
			decrementOnline(gameType, roomType);

			// TODO
			LogUtil.info(role.getNick() + " realType " + realType);
		}
	}

	/**
	 * 匹配游戏 调优思路，假设在线人数3000，算50%人参与比赛，即1500，每场比赛4人，即375场比赛，每场比赛3分钟
	 * 每一个3分钟将进行100场比赛，即180/100=1.8秒需要匹配出一场比赛，相近与20秒匹配出10场比赛
	 * 
	 * @param matchType
	 * @return
	 */
	public List<Set<Role>> doMatching(int realType) {
		ConcurrentLinkedDeque<Role> deque = getGameDeque(realType);
		int num = getGameMinRole(realType);
		// LogUtil.info(realType + " doMatching " + deque.size());
		if (deque == null || deque.size() < num) {
			return null;
		}

		List<Set<Role>> resultList = new ArrayList<>();
		// 对队列加锁
		synchronized (deque) {
			for (int i = 0; i < 10 && deque.size() >= getGameMinRole(realType); i++) {
				Set<Role> rolesSet = getOneMatchRoles(deque, num);
				if (rolesSet != null) {
					resultList.add(rolesSet);
				}
			}
		}
		return resultList;
	}

	/**
	 * 匹配出一场游戏的参赛玩家
	 * 
	 * @param deque
	 * @return
	 */
	public Set<Role> getOneMatchRoles(ConcurrentLinkedDeque<Role> deque, int total) {
		// 1 5000 0 5
		// 2 3000 5 15
		// 3 1500 15 30
		// 4 1000 30 50
		// 5 450 50 100
		// 6 50 100 9999999

		int[][] scoreRate = new int[][] { { 5000, 0, 5 }, { 3000, 5, 15 }, { 1500, 15, 30 }, { 1000, 30, 50 }, { 450, 50, 100 }, { 50, 100, 199999999 } };
		// 只取出了第一个玩家
		Role firstRole = deque.pollFirst();
		// 匹配到的对手
		Set<Role> matchRoleSet = new HashSet<>(total);
		matchRoleSet.add(firstRole);
		// 最低优先级队列
		Set<Long> lowestRoles = new HashSet<>();
		lowestRoles.addAll(firstRole.getLastGameRids());
		/*
		 * 候选对手集合 ABCD进行了比赛，即A.LastMatchRids=BCD
		 * 比赛后ABC继续进行匹配，此时EF也进行比赛匹配，即deque=ABCEF 这时候选集合中应包括BC，但不包含D，因为D并没有继续进行匹配
		 */
		Set<Role> candidateRoleSet = new HashSet<>();

		int standardScore = firstRole.getLevel();

		// 选出n个对手，n=本场参赛人数-1
		for (int i = 0; i < total - 1; i++) {
			int randomIndex = randomIndex(scoreRate, 0);

			int originIndex = randomIndex;
			Role matchRole = null;
			while (matchRole == null) {
				int[] sr = scoreRate[randomIndex];

				matchRole = getSimilarScoreRole(deque, matchRoleSet, standardScore, sr[1], sr[2], lowestRoles, candidateRoleSet);

				randomIndex = (randomIndex + 1) % scoreRate.length;
				// 防止死循环
				if (originIndex == randomIndex) {
					break;
				}
			}
			// 找到对手
			if (matchRole != null) {
				matchRoleSet.add(matchRole);
				lowestRoles.addAll(matchRole.getLastGameRids());
			}
		}
		// 在首次匹配对手中找不到足够对手，就得从候选队列中随机
		if (matchRoleSet.size() < total) {
			List<Role> candidateRoles = new ArrayList<>(candidateRoleSet);
			while (matchRoleSet.size() < total && candidateRoles.size() > 0) {
				int index = MathUtil.randomNumber(0, candidateRoles.size() - 1);
				Role candidateRole = candidateRoles.get(index);
				matchRoleSet.add(candidateRole);
				candidateRoles.remove(index);
			}
		}
		// 匹配到符合桌子人数的玩家
		if (matchRoleSet.size() == total) {
			// 将匹配到的玩家从排队队列中删除
			for (Role role : matchRoleSet) {
				deque.remove(role);
			}
			return matchRoleSet;
		} else {
			// 将从队列中移除的第一个玩家添加回队列中
			deque.addFirst(firstRole);
			return null;
		}
	}

	/**
	 * 随机某积分范围内的对手
	 * 
	 * @param deque
	 * @param currRoles
	 *            当前已经被分配的对手
	 * @param standardScore
	 *            标准分
	 * @param min
	 *            区间下限
	 * @param max
	 *            区间上限
	 * @param lowestRoles
	 *            低匹配优先级对手
	 * @param candidateRoles
	 *            候选对手
	 * @return
	 */
	public Role getSimilarScoreRole(ConcurrentLinkedDeque<Role> deque, Collection<Role> currRoles, int standardScore, int min, int max, Collection<Long> lowestRoles, Collection<Role> candidateRoles) {
		List<Role> randomList = new ArrayList<>();
		for (Role role : deque) {
			// 如果玩家已经加入到匹配队列中，或者在最低优先级队列中，则先跳过
			if (currRoles.contains(role)) {
				continue;
			}
			if (lowestRoles.contains(role.getRid())) {
				candidateRoles.add(role);
				continue;
			}
			// 符合积分范围要求的加入到随机队列中
			int delta = Math.abs(standardScore - role.getLevel());
			if (min <= delta && delta <= max) {
				randomList.add(role);
			}
		}
		if (randomList.size() <= 0) {
			return null;
		}
		int randomIndex = MathUtil.randomNumber(0, randomList.size() - 1);
		return randomList.get(randomIndex);
	}

	/**
	 * 随机概率
	 * 
	 * @param randomRates
	 * @param rateIndex
	 *            概率所在的列下标
	 * @return
	 */
	public int randomIndex(int[][] randomRates, int rateIndex) {
		// 总概率
		int totalRate = 0;
		for (int[] rate : randomRates) {
			totalRate += rate[rateIndex];
		}
		// 随机概率
		int randomRate = com.yaowan.framework.util.MathUtil.randomNumber(0, totalRate);
		// 当前概率
		int currRate = 0;
		// 随机下标
		int randomIndex = 0;
		for (int i = 0; i < randomRates.length; i++) {
			currRate += randomRates[i][0];
			if (randomRate <= currRate) {
				randomIndex = i;
				break;
			}
		}
		return randomIndex;
	}

	/**
	 * 处理游戏桌匹配
	 */
	public void doGameMatching() {
		for (Integer realType : gameDequeMap.keySet()) {
			List<Set<Role>> rolesList = doMatching(realType);
			if (rolesList == null) {
				int count = waitTimeMap.get(realType);
				waitTimeMap.put(realType, count + 1);
				continue;
			}
			// 监控匹配流畅度
			waitTimeMap.put(realType, 0);
			for (Set<Role> roles : rolesList) {
				Game game = newGame(realType);
				// 开始的记录
				// game.getRecordList().add(MatchInitRecord.snap(match));
				int i = 1;
				for (Role role : roles) {
					// 扣入场费
					/*
					 * int entryFee =
					 * matchFormulaLogic.entryFee(match.getMatchType(),
					 * role.getItemList()); roleFunction.subGoldByMatch(role,
					 * entryFee, ResourceEvent.MATCH_ENTRYFEE);
					 */

					RoleModel roleModel = getRoleModel(role);

					GameRole gameRole = new GameRole(role, game.getRoomId());
					gameRole.setSeat(i);

					game.getRoles().add(role.getRid());
					game.getSpriteMap().put(role.getRid(), gameRole);

					if (role.getPlatform() == null) {
						gameRole.setRobot(true);
						gameRole.setAuto(true);
					}

					// 把玩家添加到比赛映射表中
					roleModel.setRoomId(game.getRoomId());
					roleModel.setRealType(0);

					// 添加比赛参赛者记录
					// match.getRecordList().add(MatchSpriteInitRecord.snap(match.getStartTime(),
					// sprite));

					// LogUtil.debug("Open match " + game.getRoomId() + " for "
					// + role.getNick());

					i++;
				}
				// 各游戏初始化
				runningGameMap.put(game.getRoomId(), game);

				CMsg_22100003.Builder builder = CMsg_22100003.newBuilder();
				CGameInfo.Builder cGameInfo = CGameInfo.newBuilder();
				cGameInfo.setGameType(game.getGameType());
				cGameInfo.setRoomId(game.getRoomId());
				cGameInfo.setRoomType(game.getRoomType());
				cGameInfo.setStartTime(game.getStartTime());
				cGameInfo.setEndTime(game.getEndTime());

				for (Map.Entry<Long, GameRole> entry : game.getSpriteMap().entrySet()) {
					CGameRole.Builder gameRole = CGameRole.newBuilder();
					Role target = entry.getValue().getRole();
					gameRole.setRid(entry.getKey());
					gameRole.setNick(target.getNick());
					gameRole.setGold(target.getGold());
					gameRole.setHead(target.getHead());
					gameRole.setLevel(target.getLevel());
					gameRole.setSeat(entry.getValue().getSeat());
					gameRole.setAvatarId(entry.getValue().getAvatarId());
					gameRole.setSex(target.getSex());
					cGameInfo.addSprites(gameRole);
				}
				builder.setGame(cGameInfo);
				// 运算出目标服
				for (GameServer gameServer : registerFunction.getGameServerMap().values()) {
					gameServer.write(builder.build());
					break;
				}
			}
		}
	}

	/**
	 * 结束时销毁数据
	 * 
	 * @param game
	 */
	private void clear(Game game) {
		if (game != null) {
			// 将游戏桌从进行队列中删除
			if(runningGameMap.containsKey(game.getRoomId())){
				runningGameMap.remove(game.getRoomId());
				// 添加已结束的游戏桌到游戏后缓存
				endedGameMap.put(game.getRoomId(), game);

				for (GameRole sprite : game.getSpriteMap().values()) {
					Role role = sprite.getRole();
					// 添加玩家最近游戏桌的记录
					role.setLatelyGames(game.getRoomId());
					// 先清理上一次游戏桌的低匹配优先级玩家，将自身以外的玩家添加到低匹配优先级集合
					role.getLastGameRids().clear();
					role.getLastGameRids().addAll(game.getSpriteMap().keySet());
					role.getLastGameRids().remove(role.getRid());
					// 将玩家从玩家游戏桌对应表中删除
					resetRoleModel(role.getRid());
				}
				//TODO  移动屏蔽
//				Event event = new Event(HandleType.GAME_END, game);
//				DispatchEvent.dispacthEvent(event);
				for (int i = 0; i < game.getRoles().size(); i++) {
					decrementOnline(game.getGameType(), game.getRoomType());
				}
			}
			
		}

	}

	/**
	 * 游戏结束
	 */
	public void doGameEnd() {
		if (runningGameMap.size() <= 0) {
			return;
		}
		final long serverTime = System.currentTimeMillis();
		for (Game game : runningGameMap.values()) {
			// 30秒不准备 自动清场
			singleThreadManager.executeTask(new SingleThreadTask(game) {
				@Override
				public void doTask(ISingleData singleData) {
					Game game = (Game) singleData;
					if ((game.getStatus() == GameStatus.WAIT_READY && serverTime > game.getStartTime() + 360 * 1000)) {
						// 游戏结算，计算结果

						// 标记游戏已超时
						game.setStatus(GameStatus.NO_READY);

						for (Map.Entry<Long, GameRole> entry : game.getSpriteMap().entrySet()) {
							GameRole gameRole = entry.getValue();
							if (!gameRole.isRobot() && gameRole.getStatus() == 1) {
								GMsg_12006005.Builder builder = GMsg_12006005.newBuilder();
								builder.setCurrentSeat(gameRole.getSeat());
								/*
								 * roleFunction.sendMessageToPlayers(
								 * game.getRoles(), builder.build());
								 */
								break;
							}
						}
						LogUtil.info("Game NO_READY " + game.getRoomId() + " end!");
						clear(game);
					} else if (game.getStatus() == GameStatus.END) {// status等于2{
						LogUtil.info("Game " + game.getRoomId() + " end!");
					} else if (serverTime > game.getStartTime() + 360 * 1000) {// status等于2{
						// 已经结算
						if (game.getStatus() == GameStatus.END_REWARD) {
							return;
						}
						for (Map.Entry<Long, GameRole> entry : game.getSpriteMap().entrySet()) {
							GameRole gameRole = entry.getValue();
							if (!gameRole.isRobot()) {
								GMsg_12006005.Builder builder = GMsg_12006005.newBuilder();
								builder.setCurrentSeat(gameRole.getSeat());
								/*
								 * roleFunction.sendMessageToPlayers(
								 * game.getRoles(), builder.build());
								 */
								break;
							}
						}
						// 游戏结算，计算结果

						// 标记游戏超时
						game.setStatus(GameStatus.NO_READY);
						clear(game);
						LogUtil.info("Game " + game.getRoomId() + " 360s out!");
					}

				}
			});

		}
	}

}
