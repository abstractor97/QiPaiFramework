/**
 * 
 */
package com.yaowan.server.game.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameStatus;
import com.yaowan.constant.GameType;
import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.csv.cache.AppGameCache;
import com.yaowan.csv.cache.AvatarCache;
import com.yaowan.csv.cache.DoudizhuAIBasicCache;
import com.yaowan.csv.cache.DoudizhuRoomCache;
import com.yaowan.csv.cache.HeadPortraitCache;
import com.yaowan.csv.cache.MajiangAIBasicCache;
import com.yaowan.csv.cache.MajiangChengDuAIBasicCache;
import com.yaowan.csv.cache.MajiangChengDuRoomCache;
import com.yaowan.csv.cache.MajiangRoomCache;
import com.yaowan.csv.cache.MajiangZhenXiongAIBasicCache;
import com.yaowan.csv.cache.MajiangZhenXiongRoomCache;
import com.yaowan.csv.cache.MenjiAIBasicCache;
import com.yaowan.csv.cache.MenjiRoomCache;
import com.yaowan.csv.cache.NiuniuAiBasicCache;
import com.yaowan.csv.cache.NiuniuRoomCache;
import com.yaowan.csv.entity.DoudizhuRoomCsv;
import com.yaowan.csv.entity.MajiangChengDuRoomCsv;
import com.yaowan.csv.entity.MajiangRoomCsv;
import com.yaowan.csv.entity.MajiangZhenXiongRoomCsv;
import com.yaowan.csv.entity.MenjiRoomCsv;
import com.yaowan.csv.entity.NiuniuRoomCsv;
import com.yaowan.framework.core.GlobalVar;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.DispatchEvent;
import com.yaowan.framework.core.thread.ISingleData;
import com.yaowan.framework.core.thread.SingleThreadManager;
import com.yaowan.framework.core.thread.SingleThreadTask;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.MathUtil;
import com.yaowan.framework.util.Probability;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.GameRole;
import com.yaowan.protobuf.game.GDouDiZhu.GMsg_12012001;
import com.yaowan.protobuf.game.GGame.GGameInfo;
import com.yaowan.protobuf.game.GGame.GGameRole;
import com.yaowan.protobuf.game.GGame.GMsg_12006002;
import com.yaowan.protobuf.game.GGame.GMsg_12006005;
import com.yaowan.protobuf.game.GGame.PlayerState;
import com.yaowan.protobuf.game.GMahJong.GMsg_12011001;
import com.yaowan.protobuf.game.GZXMahJong.GMsg_12041001;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.dezhou.function.DZCardFunction;
import com.yaowan.server.game.service.PushMessageService;

/**
 * 大厅数据服务
 * 
 * @author zane
 *
 */
@Component
public class RoomFunction extends FunctionAdapter {

	@Autowired
	private RoleFunction roleFunction;

	@Autowired
	private SingleThreadManager singleThreadManager;

	@Autowired
	private AppGameCache appGameCache;

	@Autowired
	private AvatarCache avatarCache;

	@Autowired
	private HeadPortraitCache headPortraitCache;

	@Autowired
	private MenjiRoomCache menjiRoomCache;

	@Autowired
	private MajiangRoomCache majiangRoomCache;

	@Autowired
	private DoudizhuRoomCache doudizhuRoomCache;
	
	@Autowired
	private MenjiAIBasicCache menjiAIBasicCache;
	
	@Autowired
	private MajiangAIBasicCache majiangAIBasicCache;
	
	@Autowired
	private DoudizhuAIBasicCache doudizhuAIBasicCache;
	
	@Autowired
	private NiuniuAiBasicCache niuniuAiBasicCache;
	
	
	
	@Autowired
	private MajiangZhenXiongAIBasicCache majiangZhenXiongAIBasicCache;
	
	@Autowired
	private MajiangZhenXiongRoomCache majiangZhenXiongRoomCache;
	
	@Autowired
	private MajiangChengDuAIBasicCache majiangChengDuAIBasicCache;
	
	@Autowired
	private MajiangChengDuRoomCache majiangChengDuRoomCache;

	@Autowired
	private NiuniuRoomCache niuniuRoomCache;
	
//	@Autowired
//	private NPCFunction npcFunction;
//	@Autowired
//	private RoomLogFunction roomLogFunction;
//	
//	@Autowired
//	private ZTDoudizhuFunction ztDoudizhuFunction;
//	
//	@Autowired
//	private ZTMenjiFunction ztMenjiFunction;
//	
//	@Autowired
//	private ZTMajiangFunction ztMajiangFunction;
//	
//	@Autowired 
//	private DoudizhuDataFunction doudizhuDataFunction;
//	
//	@Autowired
//	private MenjiDataFunction menjiDataFunction;
//	
//	@Autowired
//	private MajiangDataFunction majiangDataFunction;
//	@Autowired
//	private FriendRoomFunction friendRoomFunction;
//	@Autowired
//	private RoleBrokeLogDao roleBrokeLogDao;
	
	@Autowired
	private DZCardFunction dzCardFunction;
	
	@Autowired
	private PushMessageService pushMessageToAreaService;
	/**
	 * 游戏缓存<roomId, Game>
	 */
	private ConcurrentHashMap<Long, Game> runningGameMap = new ConcurrentHashMap<>();

	/**
	 * 角色正在准备的游戏类型<rid, realType>
	 */
	private Map<Long, Integer> gameTypeMap = new HashMap<>();

	/**
	 * 准备参与游戏的玩家
	 */
	private ConcurrentHashMap<Long, Role> readyRoleMap = new ConcurrentHashMap<>();

	/**
	 * 游戏排队队列<realType, roles>
	 */
	private Map<Integer, ConcurrentLinkedDeque<Role>> gameDequeMap = new HashMap<>();

	/**
	 * 玩家游戏关系缓存<rid, matchId>
	 */
	private ConcurrentHashMap<Long, Long> roleGameMap = new ConcurrentHashMap<>();

	private ConcurrentHashMap<Integer, Map<Integer, AtomicInteger>> onlineCache = new ConcurrentHashMap<>();

	private Map<Integer, Integer> gameOnlineCache = new ConcurrentHashMap<Integer, Integer>();
	
	private Map<Integer,ConcurrentHashMap<Integer,ConcurrentLinkedDeque<Long>>> readyDeque = new ConcurrentHashMap<Integer,ConcurrentHashMap<Integer,ConcurrentLinkedDeque<Long>>>();

	/**
	 * AI对应离开或准备Map<rid,<HandleType,Game>>
	 */
	private static Map<Long,ConcurrentHashMap<Integer, Game>> AIExitOrReadyMap = new ConcurrentHashMap<Long, ConcurrentHashMap<Integer,Game>>();
	
	/**
	 * AI对应的离开或准备时间 map<rid,time>
	 */
	private static Map<Long,Long> timeReadyMap = new ConcurrentHashMap<Long, Long>();

	
	/**
	 * 类型房间数量
	 */
	private Map<Integer, AtomicInteger> gameCountCache = new ConcurrentHashMap<Integer, AtomicInteger>();

	/**
	 * 准备参与游戏的玩家
	 */
	private ConcurrentHashMap<Integer, Integer> waitTimeMap = new ConcurrentHashMap<>();
	
	/**
	 * 斗牛的房间列表需要爆发式增长
	 */
	private Map<Integer,List<List<Long>>> douniuRoomMap = new ConcurrentHashMap<>(); 

	int MENJI_NUM = 2;

	@Autowired
	private GlobalVar globalVar;

	public AtomicLong robotId = new AtomicLong(10000000);

	long nextRefreshTime = 0l;

	private static AtomicBoolean IS_MATCHING = new AtomicBoolean(false);
	private static AtomicBoolean IS_DEAL_END = new AtomicBoolean(false);
	private static AtomicBoolean IS_DEAL_RUN = new AtomicBoolean(false);
//	boolean IS_DEAL_END = false;
//	boolean IS_DEAL_RUN = false;

	/**
	 * 服务器启动时用来初始化一些缓存变量
	 * @param gameType
	 * @param cids
	 */
	public void initGameCacheVal(int gameType,Collection<Integer> cids){
		for (Integer cid : cids) {
			int realType = getRealType(gameType, cid);
			gameDequeMap.put(realType,
					new ConcurrentLinkedDeque<Role>());
			gameCountCache.put(realType, new AtomicInteger(0));
			// 初始化在线人数
			Map<Integer, AtomicInteger> data = getRoomOnline(gameType);
			data.put(cid, new AtomicInteger());
			waitTimeMap.put(realType, 0);
		}
	}
	@Override
	public void handleOnServerStart() {		
		for (Integer id : appGameCache.allGame) {
			DispatchEvent.dispacthEvent(new Event(HandleType.GAME_SERVER_START, id));
		}
	}

	@Override
	public void handleOnRoleLogout(final Role role) {
		cancelReadyToGame(role);
		Game game = getGameByRole(role.getRid());
		if (game != null) {
			singleThreadManager.executeTask(new SingleThreadTask(game) {
				@Override
				public void doTask(ISingleData singleData) {
					Game game = (Game) singleData;
					if (game.getGameType() == GameType.MENJI) {
						quitRole(game, role);
					} else if (game.getGameType() == GameType.DOUDIZHU
							&& game.getStatus() == GameStatus.WAIT_READY) {
						quitRole(game, role);
					}else if((game.getGameType() == GameType.MAJIANG || game.getGameType() == GameType.ZXMAJIANG || game.getGameType() == GameType.CDMAJIANG)
							&& game.getStatus() == GameStatus.WAIT_READY){
						quitRole(game, role);
					} else if ((game.getGameType() == GameType.MAJIANG || game.getGameType() == GameType.ZXMAJIANG || game.getGameType() == GameType.CDMAJIANG)
							&& game.getStatus() == GameStatus.RUNNING) {
						GameRole gameRole = game.getSpriteMap().get(
								role.getRid());
						if (gameRole != null
								&& gameRole.getStatus() == PlayerState.PS_WATCH_VALUE) {
							quitRole(game, role);
						}else if(gameRole != null){
							gameRole.setAuto(true);
						}

					} else if (game.getGameType() == GameType.DOUNIU) {//牛牛
						GameRole gameRole = game.getSpriteMap().get(
								role.getRid());
						if (gameRole != null) {
							quitRole(game, role);
						}

					}else if (game.getStatus() == GameStatus.WAIT_READY
							|| game.getStatus() == GameStatus.END_REWARD) {
						GMsg_12006005.Builder builder = GMsg_12006005
								.newBuilder();
						GameRole gameRole = game.getSpriteMap().get(
								role.getRid());
						builder.setCurrentSeat(gameRole.getSeat());
						roleFunction.sendMessageToPlayers(game.getRoles(),
								builder.build());

						endGame(game);
						LogUtil.error("玩家掉线" + role.getNick());

					} else if (game.getStatus() == GameStatus.NO_READY) {
						GMsg_12006005.Builder builder = GMsg_12006005
								.newBuilder();
						GameRole gameRole = game.getSpriteMap().get(
								role.getRid());
						builder.setCurrentSeat(gameRole.getSeat());
						roleFunction.sendMessageToPlayers(game.getRoles(),
								builder.build());

						endGame(game);
						LogUtil.error("玩家掉线" + role.getNick());

					}else if (game.getStatus() == GameStatus.RUNNING) {
						GameRole gameRole = game.getSpriteMap().get(
								role.getRid());
						gameRole.setAuto(true);
						// gameRole.setStatus(PlayerState.PS_EXIT_VALUE);

					}
				}
			});

		}
	}

	public void decrementOnline(int gameType, int roomType) {
		// 在线人数
		Map<Integer, AtomicInteger> data = onlineCache.get(gameType);
		AtomicInteger count = data.get(roomType);
		if (count.get() > 0)
			count.decrementAndGet();
		data.put(roomType, count);
	}

	public Map<Integer, Integer> getGameOnline() {
		// 需要定时刷新统计
		long time = System.currentTimeMillis();
		if (time > nextRefreshTime) {
			nextRefreshTime = time + 5000;
			for (Map.Entry<Integer, Map<Integer, AtomicInteger>> entry : onlineCache
					.entrySet()) {
				Map<Integer, AtomicInteger> map = getRoomOnline(entry.getKey());
				int count = 0;
				for (Map.Entry<Integer, AtomicInteger> subEntry : map
						.entrySet()) {
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
	public int getRoomType(int realType){
		int roomType = realType % 10000000;
		return roomType;
	}
	
	public ConcurrentHashMap<Integer,ConcurrentLinkedDeque<Long>> getReadyQueue(int realType){
		return readyDeque.get(realType);
	}
	public int getGameMinRole(int realType) {
		int gameType = getGameType(realType);
		int num = 4;
		if (gameType == GameType.MENJI) {
			// 每次随机都要固定下来不要浮动
			int count = waitTimeMap.get(realType);
			if (MENJI_NUM == 0) {
				Map<Integer, Double> map = new HashMap<>();
				map.put(2, 0.1);
				map.put(3, 0.5);
				map.put(4, 0.3);
				map.put(5, 0.1);
				MENJI_NUM = Probability.getRand(map, 100);

			}
			num = MENJI_NUM;
		} else if (gameType == GameType.DOUDIZHU) {
			num = 3;
		} else if (gameType == GameType.MAJIANG) {
			num = 4;
		} else if (gameType == GameType.ZXMAJIANG) {
			num = 4;
		} else if (gameType == GameType.CDMAJIANG) {
			num = 4;
		} else if(gameType == GameType.DEZHOU){
			num = 1;
		}
		return num;
	}

	public Game newGame(int realType) {

		Game game = new Game(globalVar.actionId.getAndIncrement(), realType);
		if (game.getGameType() == GameType.MENJI) {
			MENJI_NUM = 0;
		}
		game.setNeedCount(getGameMinRole(game.getGameType()));
		runningGameMap.put(game.getRoomId(), game);
		AtomicInteger data = gameCountCache.get(realType);
		data.incrementAndGet();
		gameCountCache.put(realType, data);
		return game;
	}

	public Game getGameByRole(long rid) {
		Long id = getRoleGameMap().get(rid);
		if (id == null) {
			return null;
		}
		return getGame(id);
	}

	public GameRole getGameRole(long rid) {
		Game game = getGameByRole(rid);
		if (game != null) {
			return game.getSpriteMap().get(rid);
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
	 * 检测准备开始游戏
	 */
	public void dealRuningGames() {
		if (IS_DEAL_RUN.get()) {
			return;
		}
		IS_DEAL_RUN.set(true);
		try {
			for (Map.Entry<Long, Game> entry : getRunningGames().entrySet()) {
				final Game game = entry.getValue();
				singleThreadManager.executeTask(new SingleThreadTask(game) {
					@Override
					public void doTask(ISingleData singleData) {
						// LogUtil.debug("ROBOT_START" + game.getRoomId());

						if (game.getStatus() == GameStatus.WAIT_READY) {
							Event event = new Event(HandleType.ROBOT_START, game);
							DispatchEvent.dispacthEvent(event);

						} else if (game.getStatus() == GameStatus.END_REWARD) {
							Event event = new Event(HandleType.GAME_RESET, game);
							DispatchEvent.dispacthEvent(event);
						} else if (game.getStatus() == GameStatus.RUNNING) {
							Event event = new Event(HandleType.GAME_RUNNING, game);
							DispatchEvent.dispacthEvent(event);
						}
					}
				});

			}
		} finally {
			// TODO: handle exception
			IS_DEAL_RUN.set(false);
		}
		
	}

	/**
	 * 玩家是否在游戏中
	 * 
	 * @param role
	 * @return
	 */
	public boolean isInGame(long rid) {
		if (!getRoleGameMap().containsKey(rid)) {
			return false;
		}
		long roomId = getRoleGameMap().get(rid);
		Game game = runningGameMap.get(roomId);
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
		LogUtil.info(role.getNick() + " realType " + realType);
		if (readyRoleMap.containsKey(role.getRid())) {
			LogUtil.info(role.getNick() + " false ");
			return false;
		} else {
			if (!gameDequeMap.containsKey(realType)) {
				return false;
			}
			gameTypeMap.put(role.getRid(), realType);
			gameDequeMap.get(realType).add(role);
			readyRoleMap.put(role.getRid(), role);

			// 在线人数
			int roomType = realType % 10000000;
			int gameType = (realType - roomType) / 10000000;
			role.setLastGameType(gameType);
			role.setLastRoomType(roomType);

			Map<Integer, AtomicInteger> data = onlineCache.get(gameType);
			AtomicInteger count = data.get(roomType);
			count.incrementAndGet();
			data.put(roomType, count);
			
			//发送消息加入进入跨服游戏
			pushMessageToAreaService.pushJoinCrossGame(role.getRid(),getGameType(realType));
			
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
		if (readyRoleMap.containsKey(role.getRid())) {
			return true;
		}
		return false;
	}

	public ConcurrentLinkedDeque<Role> getGameDeque(int realType) {
		if(!gameDequeMap.containsKey(realType)){
			gameDequeMap.put(realType,
					new ConcurrentLinkedDeque<Role>());
		}
		return gameDequeMap.get(realType);
	}

	/**
	 * 取消游戏准备（比如掉线）
	 * 
	 * @param role
	 */
	public boolean cancelReadyToGame(Role role) {
		if (gameTypeMap.containsKey(role.getRid())
				|| readyRoleMap.containsKey(role.getRid())) {
			int realType = gameTypeMap.get(role.getRid());
			gameTypeMap.remove(role.getRid());
			getGameDeque(realType).remove(role);
			readyRoleMap.remove(role.getRid());

			int roomType = realType % 10000000;
			int gameType = (realType - roomType) / 10000000;
			decrementOnline(gameType, roomType);
			// TODO
			LogUtil.info(role.getNick() + " realType " + realType);
			return true;
		} else {
			return false;
		}

	}

	

	

	

	/**
	 * 
	 *  新玩家进入房间
	 */
	public void enterGame(final Game game, final Role role) {
		// 把玩家添加到比赛映射表中
		getRoleGameMap().put(role.getRid(), game.getRoomId());
		// 把玩家从准备队列中删除
		readyRoleMap.remove(role.getRid());
		gameTypeMap.remove(role.getRid());

		LogUtil.debug("Add match " + game.getRoomId() + " for "
				+ role.getNick());
		singleThreadManager.executeTask(new SingleThreadTask(game) {
			@Override
			public void doTask(ISingleData singleData) {
				LogUtil.debug(game.getRealType() + "GAME_ENTER"
						+ game.getRoomId() + " for " + role.getNick());
				Event event = new Event(HandleType.GAME_ENTER, game, role);
				DispatchEvent.dispacthEvent(event);
			}
		});
		//更新匹配时间
//		roomLogFunction.updateRoleMatchTime(role.getRid());
	}
	
	
	
	/**
	 * 匹配游戏 调优思路，假设在线人数3000，算50%人参与比赛，即1500，每场比赛4人，即375场比赛，每场比赛3分钟
	 * 每一个3分钟将进行100场比赛，即180/100=1.8秒需要匹配出一场比赛，相近与20秒匹配出10场比赛
	 * 
	 * @param matchType
	 * @return
	 */
	/**
	 * @param realType
	 * @return
	 */
	public List<Set<Role>> doMatching(int realType) {
		ConcurrentLinkedDeque<Role> deque = getGameDeque(realType);
		
		int num = getGameMinRole(realType);
		int nowTime = TimeUtil.time();
		// LogUtil.info(realType + " doMatching " + deque.size());
		if (deque == null || deque.size() == 0) {
			return null;
		}
		LogUtil.info("deque.size()" + deque.size());
		int size = waitTimeMap.get(realType);
		waitTimeMap.put(realType, size + 1);
		LogUtil.info(realType + "waitTimeMap" + (size + 1));
		if (deque.size() < 2) {

			int rest = num - deque.size();
			int count = waitTimeMap.get(realType);
			// 等待久了才创建机器人

			int gameType = getGameType(realType);
			int roomType = getRoomType(realType);
			final Role role = deque.pollFirst();
			boolean flag = true;
			int fix = 0;
			
			DispatchEvent.dispacthEvent(new Event(HandleType.GAME_MATCHING, role,realType));
			
			
/*			if (gameType == GameType.MAJIANG || gameType == GameType.ZXMAJIANG 
					|| gameType == GameType.CDMAJIANG) {
				if (count >= 1) {
					createRobotReady(rest < 1 ? 1 : rest, realType);
					return null;
				}

			}else
			{*/
				if(gameType == GameType.DOUDIZHU){
					for (DoudizhuRoomCsv csv : doudizhuRoomCache
							.getConfigList()) {
						if (csv.getEnterUpperLimit() == -1) {
							fix = csv.getRoomID();
							break;
						} else {
							if (role.getGold() > csv
									.getEnterUpperLimit()) {
								continue;
							}
							if (role.getGold() < csv
									.getEnterLowerLimit()) {
								continue;
							}
							fix = csv.getRoomID();
							break;
						}

					}
				}else if (gameType == GameType.MAJIANG){
					for (MajiangRoomCsv csv : majiangRoomCache
							.getConfigList()) {
						if (csv.getEnterUpperLimit() == -1) {
							fix = csv.getRoomID();
							break;
						} else {
							if (role.getGold() > csv
									.getEnterUpperLimit()) {
								continue;
							}
							if (role.getGold() < csv
									.getEnterLowerLimit()) {
								continue;
							}
							fix = csv.getRoomID();
							break;
						}

					}
				}else if (gameType == GameType.MENJI){
					for (MenjiRoomCsv csv : menjiRoomCache
							.getConfigList()) {
						if (csv.getEnterUpperLimit() == -1) {
							fix = csv.getRoomID();
							break;
						} else {
							if (role.getGold() > csv
									.getEnterUpperLimit()) {
								continue;
							}
							if (role.getGold() < csv
									.getEnterLowerLimit()) {
								continue;
							}
							fix = csv.getRoomID();
							break;
						}
					}
				} else if (gameType == GameType.ZXMAJIANG){
					for (MajiangZhenXiongRoomCsv csv : majiangZhenXiongRoomCache
							.getConfigList()) {
						if (csv.getEnterUpperLimit() == -1) {
							fix = csv.getRoomID();
							break;
						} else {
							if (role.getGold() > csv
									.getEnterUpperLimit()) {
								continue;
							}
							if (role.getGold() < csv
									.getEnterLowerLimit()) {
								continue;
							}
							fix = csv.getRoomID();
							break;
						}
					}
				} else if (gameType == GameType.CDMAJIANG){
					for (MajiangChengDuRoomCsv csv : majiangChengDuRoomCache
							.getConfigList()) {
						if (csv.getEnterUpperLimit() == -1) {
							fix = csv.getRoomID();
							break;
						} else {
							if (role.getGold() > csv
									.getEnterUpperLimit()) {
								continue;
							}
							if (role.getGold() < csv
									.getEnterLowerLimit()) {
								continue;
							}
							fix = csv.getRoomID();
							break;
						}
					}
				}
				
				if(runningGameMap != null){
					for (Map.Entry<Long, Game> entry : runningGameMap
							.entrySet()) {

						final Game game = entry.getValue();
						if(game != null){
							LogUtil.debug(realType +"search match " + game.getRoomId()
									+ " for " + role.getNick());
							LogUtil.info("getGameMinRole(realType):"+getGameMinRole(realType)+",game.getSpriteMap().size()"+game.getSpriteMap().size());
							if (game.getRoomType() == roomType && game.getGameType() == gameType && game.getStatus() == GameStatus.WAIT_READY
									&& game.getSpriteMap().size() < getGameMinRole(realType)) {
								
								LogUtil.debug(game.getRoomId() + "可以匹配" + fix
										+ "级房" + role.getNick());
								if (fix == 0 || game.getRoomType() != fix) {
									continue;
								}
								enterGame(game, role);
								if(readyDeque.get(realType) != null){
									ConcurrentHashMap<Integer, ConcurrentLinkedDeque<Long>> map = readyDeque.get(realType);
									for(ConcurrentLinkedDeque<Long> deque1 : map.values()){
										if(deque1.contains(game.getRoomId())){
											if(game.getSpriteMap().size() >= getGameMinRole(realType)){
												 deque1.remove(game.getRoomId());
											}
										}
									}
								}
								
								flag = false;
								break;
							}else if(game.getRoomType() == roomType && game.getGameType() == gameType && gameType == GameType.MENJI
									&& (game.getStatus() == GameStatus.RUNNING || game.getStatus() != GameStatus.WAIT_READY)
									&& game.getSpriteMap().size() < 5){
								LogUtil.debug(game.getRoomId() + "可以匹配" + fix
										+ "级房" + role.getNick());
								if (fix == 0 || game.getRoomType() != fix) {
									continue;
								}
								enterGame(game, role);
								if(readyDeque.get(realType) != null){
									ConcurrentHashMap<Integer, ConcurrentLinkedDeque<Long>> map = readyDeque.get(realType);
									for(ConcurrentLinkedDeque<Long> deque1 : map.values()){
										if(deque1.contains(game.getRoomId())){
											if(game.getSpriteMap().size() >= getGameMinRole(realType)){
												 deque1.remove(game.getRoomId());
											}
										}
									}
								}
								
								flag = false;
								break;
							}
						}else{
							LogUtil.info("game is null");
						}
						
					}
				}
				if (flag) {
					if(runningGameMap != null){
						for (Map.Entry<Long, Game> entry : runningGameMap
								.entrySet()) {
							final Game game = entry.getValue();
							if(game != null){
								if (game.getRoomType() == roomType && game.getGameType() == gameType && 
										(game.getStatus() == GameStatus.WAIT_READY || game.getStatus() == GameStatus.END_REWARD)) {
									if (fix == 0 || game.getRoomType() != fix) {
										continue;
									}
									for(Long rid : game.getSpriteMap().keySet()){
										GameRole gameRole = game.getSpriteMap().get(rid);
										if(gameRole != null && gameRole.isRobot()){
											Role role1 = gameRole.getRole();
											LogUtil.info("gameId:"+game.getRoomId());
											LogUtil.error("踢掉了"+role1.getNick()+"一个AI，换了一个用户");
											quitRole(game, role1);
											//exitTable(role1);
											deque.addFirst(role);
											flag = false;
											break;
										}else if(gameRole == null){
											LogUtil.info("没有删除干净");
											game.getSpriteMap().keySet().remove(rid);
										}
									}
								}
							}else{
								LogUtil.info("game is null");
							}
							
						}
					}
				}
				if(flag){
					deque.addFirst(role);
					int min = 2;
					if(deque.size() >= min){
						
						List<Set<Role>> resultList = new ArrayList<>();
						// 对队列加锁
						synchronized (deque) {
							LogUtil.info(deque.size()+","+getGameMinRole(realType));
							for (int i = 0; i < 10 && deque.size() >= min; i++) {
								Set<Role> rolesSet = getOneMatchRoles(deque, deque.size());
								if (rolesSet != null) {
									resultList.add(rolesSet);
								} else {
									LogUtil.info("没有人可以匹配");
								}
							}
						}
						flag = false;
						return resultList;
					}
					
				}
//				if(flag){
//					//AI更改为游戏状态
//					LogUtil.info("创建机器人");
//					createRobotReady(1,realType,-1);
//					LogUtil.info("deque"+deque.size());
//					return null;
//				}
			return null;
		}
		
		
		List<Set<Role>> resultList = new ArrayList<>();
		// 对队列加锁
		synchronized (deque) {
			for (int i = 0; i < 10 && deque.size() >= 2; i++) {
				Set<Role> rolesSet = getOneMatchRoles(deque, deque.size() > num ? num : deque.size());
				if (rolesSet != null) {
					resultList.add(rolesSet);
				} else {
					LogUtil.info("没有人可以匹配");
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
	public Set<Role> getOneMatchRoles(ConcurrentLinkedDeque<Role> deque,
			int total) {
		// 1 5000 0 5
		// 2 3000 5 15
		// 3 1500 15 30
		// 4 1000 30 50
		// 5 450 50 100
		// 6 50 100 9999999

		int[][] scoreRate = new int[][] { { 5000, 0, 5 }, { 3000, 5, 15 },
				{ 1500, 15, 30 }, { 1000, 30, 50 }, { 450, 50, 100 },
				{ 50, 100, 199999999 } };
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

				matchRole = getSimilarScoreRole(deque, matchRoleSet,
						standardScore, sr[1], sr[2], lowestRoles,
						candidateRoleSet);

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
	public Role getSimilarScoreRole(ConcurrentLinkedDeque<Role> deque,
			Collection<Role> currRoles, int standardScore, int min, int max,
			Collection<Long> lowestRoles, Collection<Role> candidateRoles) {
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
		int randomRate = MathUtil.randomNumber(0, totalRate);
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
	 * 成功匹配
	 * @param rolesList
	 */
	public void gameMatchSuccess(int realType,List<Set<Role>> rolesList){
		if (rolesList == null || rolesList.isEmpty()) {
			
			return ;
		}
		LogUtil.info(rolesList.size()+"realType" + realType);
		// 监控匹配流畅度
		waitTimeMap.put(realType, 0);
		for (Set<Role> roles : rolesList) {
			Game game = newGame(realType);
			
			//添加到牌桌队列，若牌桌人数已满不添加，焖鸡不添加
			insertReadyDeque(game);
			
			// 开始的记录
			// game.getRecordList().add(MatchInitRecord.snap(match));
			int i = 1;

			GGameInfo.Builder builder = GGameInfo.newBuilder();
			builder.setGameType(game.getGameType());
			builder.setRoomId(game.getRoomId());
			builder.setRoomType(game.getRoomType());
			for (Role role : roles) {
				// 扣入场费
				/*
				 * int entryFee =
				 * matchFormulaLogic.entryFee(match.getMatchType(),
				 * role.getItemList()); roleFunction.subGoldByMatch(role,
				 * entryFee, ResourceEvent.MATCH_ENTRYFEE);
				 */

				GameRole gameRole = new GameRole(role, game.getRoomId());
				gameRole.setSeat(i);

				game.getRoles().add(role.getRid());
				game.getSpriteMap().put(role.getRid(), gameRole);

				LogUtil.info("nick:"+role.getNick());
//				if (role.getPlatform() == null) {
//					gameRole.setRobot(true);
//					gameRole.setAuto(true);
//					gameRole.setAICount(0);
//					npcFunction.updateRoomId(game.getGameType(), game.getRoomType(), role.getRid(), game.getRoomId());
//				}
//				
//				if(gameRole.isRobot()){
//					Npc npc = npcFunction.getNpcById(game.getGameType(), game.getRoomType(), gameRole.getRole().getRid());
//					gameRole.setWinTotal(npc.getWinTotal());
//					gameRole.setWinWeek(npc.getWinWeek());
//					gameRole.setCountTotal(npc.getCountTotal());
//					gameRole.setCountWeek(npc.getCountWeek());
//
//				}

				GGameRole.Builder gameRoleBuilder = GGameRole.newBuilder();
				gameRoleBuilder.setRid(role.getRid());
				gameRoleBuilder.setNick(role.getNick());
				gameRoleBuilder.setGold(role.getGold());
				gameRoleBuilder.setHead(role.getHead());
				gameRoleBuilder.setLevel(role.getLevel());
				gameRoleBuilder.setSeat(gameRole.getSeat());
				gameRoleBuilder.setAvatarId(gameRole.getAvatarId());
				gameRoleBuilder.setSex(role.getSex());

				builder.addSprites(gameRoleBuilder);

				// 把玩家添加到比赛映射表中
				getRoleGameMap().put(role.getRid(), game.getRoomId());
				// 把玩家从准备队列中删除
				readyRoleMap.remove(role.getRid());
				gameTypeMap.remove(role.getRid());

				// 添加比赛参赛者记录
				// match.getRecordList().add(MatchSpriteInitRecord.snap(match.getStartTime(),
				// sprite));

				LogUtil.debug("Open match " + game.getRoomId() + " for "
						+ role.getNick());
				i++;

			}
			// 各游戏初始化
			runningGameMap.put(game.getRoomId(), game);
			Event event = new Event(HandleType.GAME_INIT, game);
			DispatchEvent.dispacthEvent(event);

			roleFunction.sendMessageToPlayers(game.getSpriteMap().keySet(),
					GMsg_12006002.newBuilder().setGame(builder).build());
		}
	}
	/**
	 * 处理游戏桌匹配
	 */
	public void doGameMatching() {
		if (IS_MATCHING.get()) {
			return;
		}
		IS_MATCHING.set(true);
		try {
			for (Integer realType : gameDequeMap.keySet()) {
				List<Set<Role>> rolesList = doMatching(realType);
				gameMatchSuccess(realType, rolesList);
			}
		} finally {
			// TODO: handle exception
			IS_MATCHING.set(false);
		}
		
	}

	/**
	 * 结束时销毁数据
	 * 
	 * @param game
	 */
	public void quitRole(final Game game, final Role role) {
		game.setStartTime(System.currentTimeMillis());
		Long gameId = getRoleGameMap().remove(role.getRid());
		role.setLastGameType(game.getGameType());
		role.setLastRoomType(game.getRoomType());
		// 将玩家从玩家游戏桌对应表中删除
		List<Long> all = new ArrayList<Long>();
		all.addAll(game.getRoles());
		LogUtil.info(" erere"+role.getRid());
		GameRole gameRole = game.getSpriteMap().get(role.getRid());
		if(gameRole.isRobot()){
//			npcFunction.updateStatus(role.getRid(), game.getGameType(), game.getRoomType(), 2);
//			npcFunction.updateRoomId(game.getGameType(), game.getRoomType(),role.getRid(),game.getRoomId());
		}
		Event event = new Event(HandleType.GAME_EXIT, game, role.getRid());
		DispatchEvent.dispacthEvent(event);

		LogUtil.info(" role.getRid()"+role.getRid());
		if (gameId != null) {
			decrementOnline(game.getGameType(), game.getRoomType());
		}

		if (game.getGameType() == GameType.DOUDIZHU) {
			//endGame(game);
		}else if (game.getGameType() == GameType.DOUNIU) {
			//endGame(game);
		} else {
			if (game.getRoles().size() <= 1) {
				// game.getRoles().clear();
				if (gameRole != null) {
					GMsg_12006005.Builder builder = GMsg_12006005.newBuilder();
					builder.setCurrentSeat(gameRole.getSeat());
					roleFunction.sendMessageToPlayers(all, builder.build());
				}
				endGame(game);
			}
		}

	}

	/**
	 * 结束时销毁数据
	 * 
	 * @param game
	 */
	public void endGame(Game game) {
		if (game != null) {

			// 将游戏桌从进行队列中删除
			if (runningGameMap.containsKey(game.getRoomId())) {
				runningGameMap.remove(game.getRoomId());
				AtomicInteger data = gameCountCache.get(game.getRealType());
				if (data.get() > 0) {
					data.decrementAndGet();
				}
				gameCountCache.put(game.getRealType(), data);

				// 添加已结束的游戏桌到游戏后缓存 TODO
				// endedGameMap.put(game.getRoomId(), game);
				Map<Long, GameRole> map = new HashMap<Long, GameRole>();
				map.putAll(game.getSpriteMap());
				for (GameRole sprite : map.values()) {
					Role role = sprite.getRole();
					if (!sprite.isRobot()) {
						GMsg_12006005.Builder builder = GMsg_12006005
								.newBuilder();
						builder.setCurrentSeat(sprite.getSeat());
						roleFunction.sendMessageToPlayers(game.getRoles(),
								builder.build());

					}else{
//						npcFunction.updateStatus(role.getRid(), game.getGameType(), game.getRoomType(), 2);
					}

					sprite.setStatus(PlayerState.PS_EXIT_VALUE);

					// 添加玩家最近游戏桌的记录
					role.setLatelyGames(game.getRoomId());
					// 先清理上一次游戏桌的低匹配优先级玩家，将自身以外的玩家添加到低匹配优先级集合
					role.getLastGameRids().clear();
					role.getLastGameRids().addAll(game.getSpriteMap().keySet());
					role.getLastGameRids().remove(role.getRid());

					role.setLastGameType(game.getGameType());
					role.setLastRoomType(game.getRoomType());

					// 将玩家从玩家游戏桌对应表中删除
					Long gameId=getRoleGameMap().remove(role.getRid());
					if(gameId!=null){
						decrementOnline(game.getGameType(), game.getRoomType());
					}				
					//通知地区服，退出游戏
					pushMessageToAreaService.pushQuitCrossGame(role.getRid());
				}
				Event event = new Event(HandleType.GAME_END, game);
				DispatchEvent.dispacthEvent(event);
				game.setStatus(GameStatus.CLEAR);
				game.getSpriteMap().clear();
				game.getRoles().clear();
			} else {
				game.getSpriteMap().clear();
				game.getRoles().clear();
				game.setStatus(GameStatus.CLEAR);
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
		if (IS_DEAL_END.get()) {
			return;
		}
		IS_DEAL_END.set(true);
		try {
			final long serverTime = System.currentTimeMillis();
			for (Game game : runningGameMap.values()) {
				//斗牛的房间暂时不会自动消失
				if (game.getGameType() == GameType.DOUNIU) {
					continue;
				}
				
				if(game.isFriendRoom()){
					//好友房不会消失
					continue;
				}
				
				// 30秒不准备 自动清场
				singleThreadManager.executeTask(new SingleThreadTask(game) {
					@Override
					public void doTask(ISingleData singleData) {
						Game game = (Game) singleData;
						int endInterval = 30;
						if (game.getGameType() == GameType.MENJI) {
							endInterval = 15;
						}
						if(game.getGameType() != GameType.MENJI && game.getSpriteMap().size() >= 2){
							boolean allRobot = true;
							for(GameRole gameRole : game.getSpriteMap().values()){
								if(gameRole != null && !gameRole.isRobot()){
									allRobot = false;
									break;
								}
									
							}
							if(allRobot){
								LogUtil.info("定时器 剩下多个人并且全是AI " + game.getRoomId() + " END!");
								endGame(game);
								return;
							}
						}
						
						if(game.getStatus() == GameStatus.WAIT_READY
								&& serverTime > game.getStartTime() + 10 * 1000){
							List<GameRole> gameRoles = new ArrayList<GameRole>();
							for(GameRole gameRole : game.getSpriteMap().values()){
								if(gameRole.getStatus() != PlayerState.PS_PREPARE_VALUE){
									gameRoles.add(gameRole);
								}
							}
							if(gameRoles.size() + 1 >= game.getSpriteMap().size()){
								game.setStatus(GameStatus.NO_READY);
								endGame(game);
							}else{
								for(GameRole gameRole : gameRoles){
									quitRole(game, gameRole.getRole());
								}
								insertReadyDeque(game);
							}
						}
						
						if ((game.getStatus() == GameStatus.WAIT_READY)
								&& serverTime > game.getStartTime() + 15 * 1000) {
							// 游戏结算，计算结果

							if (game.getGameType() == GameType.MENJI) {
								Map<Long, GameRole> map = new HashMap<Long, GameRole>();
								map.putAll(game.getSpriteMap());
								for (Map.Entry<Long, GameRole> entry : map
										.entrySet()) {
									GameRole gameRole = entry.getValue();
									if (gameRole.getStatus() != PlayerState.PS_PREPARE_VALUE) {

										LogUtil.info("menji NO_READY"
												+ gameRole.getRole().getNick());
										quitRole(game, gameRole.getRole());
									}
								}
								// 清除或者开始
								if (game.getSpriteMap().size() < 2) {
									endGame(game);
								} /*
								 * else { if (game.getStatus() ==
								 * GameStatus.WAIT_READY) { Event event = new
								 * Event(HandleType.ROBOT_START, game);
								 * DispatchEvent.dispacthEvent(event); }
								 * 
								 * }
								 */
							} else {
								// 标记游戏已超时
								
								LogUtil.info("定时器 15秒 不准备 " + game.getRoomId()
										+ " end!");
								game.setStatus(GameStatus.NO_READY);
								endGame(game);
							}

						} else if (game.getStatus() == GameStatus.END_REWARD
								&& game.getEndTime() > 0
								&& serverTime > game.getEndTime() + endInterval
										* 1000) {// status等于2{
							LogUtil.info("定时器 30秒 Game " + game.getRoomId()
									+ " END_REWARD!");

							endGame(game);
						} else if (serverTime > game.getStartTime() + 360 * 1000) {// status等于2{

							/*
							 * // 已经结算 if (game.getStatus() ==
							 * GameStatus.END_REWARD) { return; }
							 */
							boolean flag = true;
							for (Map.Entry<Long, GameRole> entry : game
									.getSpriteMap().entrySet()) {
								GameRole gameRole = entry.getValue();
								if (!gameRole.isRobot()) {
									flag = false;
								}
							}
							// 全部人不在线
							int n = 0;
							for (Map.Entry<Long, GameRole> entry : game
									.getSpriteMap().entrySet()) {
								boolean on = roleFunction.isOnline(entry.getKey());
								if (on) {
									n++;
								}
							}
							if (n == 0) {
								flag = true;
							}
							if (flag) {
								game.setStatus(GameStatus.NO_READY);
								for (Map.Entry<Long, GameRole> entry : game
										.getSpriteMap().entrySet()) {
									GameRole gameRole = entry.getValue();
									if (!gameRole.isRobot()) {
										GMsg_12006005.Builder builder = GMsg_12006005
												.newBuilder();
										builder.setCurrentSeat(gameRole.getSeat());
										roleFunction.sendMessageToPlayers(
												game.getRoles(), builder.build());
										break;
									}
								}
								// 游戏结算，计算结果

								// 标记游戏超时
								endGame(game);
								LogUtil.info("Game " + game.getRoomId()
										+ " 360s out!");
							}

						} else if (game.getSpriteMap().size() <= 1) {// status等于2{

							LogUtil.info("定时器 只剩下一个人 " + game.getRoomId() + " END!");
							endGame(game);
						} 

					}
				});

			}
		} finally{
			// TODO: handle exception
			IS_DEAL_END.set(false);
		}
		
	}

	/**
	 * 玩家主动退出
	 * 
	 * @param role
	 */
	public void exitTable(final Role role) {		
		DispatchEvent.dispacthEvent(new Event(HandleType.GAME_EXIT_TABLE,role));
	}

	public ConcurrentHashMap<Long, Long> getRoleGameMap() {
		return roleGameMap;
	}

	public int checkRoomType(int gameType, int roomType, int gold) {
		if (gameType == GameType.MENJI) {
			MenjiRoomCsv menjiRoomCsv = menjiRoomCache.getConfig(roomType);
			boolean flag = false;
			if (menjiRoomCsv.getEnterUpperLimit() == -1) {
				if (gold > menjiRoomCsv.getEnterLowerLimit()) {
					flag = true;
				}
			} else {
				if (gold > menjiRoomCsv.getEnterLowerLimit()
						&& gold < menjiRoomCsv.getEnterUpperLimit()) {
					flag = true;
				}
			}
			if (flag) {
				return roomType;
			}
			for (MenjiRoomCsv csv : menjiRoomCache.getConfigList()) {
				if (csv.getEnterUpperLimit() == -1) {
					roomType = csv.getRoomID();
					break;
				} else {
					if (gold > csv.getEnterUpperLimit()) {
						continue;
					}
					if (gold < csv.getEnterLowerLimit()) {
						continue;
					}
					roomType = csv.getRoomID();
					break;
				}
			}
		} else if (gameType == GameType.DOUDIZHU) {
			DoudizhuRoomCsv doudizhuRoomCsv = doudizhuRoomCache
					.getConfig(roomType);
			boolean flag = false;
			if (doudizhuRoomCsv.getEnterUpperLimit() == -1) {
				if (gold > doudizhuRoomCsv.getEnterLowerLimit()) {
					flag = true;
				}
			} else {
				if (gold > doudizhuRoomCsv.getEnterLowerLimit()
						&& gold < doudizhuRoomCsv.getEnterUpperLimit()) {
					flag = true;
				}
			}
			if (flag) {
				return roomType;
			}
			for (DoudizhuRoomCsv csv : doudizhuRoomCache.getConfigList()) {
				if (csv.getEnterUpperLimit() == -1) {
					roomType = csv.getRoomID();
					break;
				} else {
					if (gold > csv.getEnterUpperLimit()) {
						continue;
					}
					if (gold < csv.getEnterLowerLimit()) {
						continue;
					}
					roomType = csv.getRoomID();
					break;
				}
			}
		} else if (gameType == GameType.MAJIANG) {
			MajiangRoomCsv majiangRoomCsv = majiangRoomCache
					.getConfig(roomType);
			boolean flag = false;
			if (majiangRoomCsv.getEnterUpperLimit() == -1) {
				if (gold > majiangRoomCsv.getEnterLowerLimit()) {
					flag = true;
				}
			} else {
				if (gold > majiangRoomCsv.getEnterLowerLimit()
						&& gold < majiangRoomCsv.getEnterUpperLimit()) {
					flag = true;
				}
			}
			if (flag) {
				return roomType;
			}
			for (MajiangRoomCsv csv : majiangRoomCache.getConfigList()) {
				if (csv.getEnterUpperLimit() == -1) {
					roomType = csv.getRoomID();
					break;
				} else {
					if (gold > csv.getEnterUpperLimit()) {
						continue;
					}
					if (gold < csv.getEnterLowerLimit()) {
						continue;
					}
					roomType = csv.getRoomID();
					break;
				}
			}
		} else if (gameType == GameType.ZXMAJIANG) {
			MajiangZhenXiongRoomCsv majiangRoomCsv = majiangZhenXiongRoomCache
					.getConfig(roomType);
			boolean flag = false;
			if (majiangRoomCsv.getEnterUpperLimit() == -1) {
				if (gold > majiangRoomCsv.getEnterLowerLimit()) {
					flag = true;
				}
			} else {
				if (gold > majiangRoomCsv.getEnterLowerLimit()
						&& gold < majiangRoomCsv.getEnterUpperLimit()) {
					flag = true;
				}
			}
			if (flag) {
				return roomType;
			}
			for (MajiangZhenXiongRoomCsv csv : majiangZhenXiongRoomCache.getConfigList()) {
				if (csv.getEnterUpperLimit() == -1) {
					roomType = csv.getRoomID();
					break;
				} else {
					if (gold > csv.getEnterUpperLimit()) {
						continue;
					}
					if (gold < csv.getEnterLowerLimit()) {
						continue;
					}
					roomType = csv.getRoomID();
					break;
				}
			}
		} else if (gameType == GameType.CDMAJIANG) {
			MajiangChengDuRoomCsv majiangRoomCsv = majiangChengDuRoomCache
					.getConfig(roomType);
			boolean flag = false;
			if (majiangRoomCsv.getEnterUpperLimit() == -1) {
				if (gold > majiangRoomCsv.getEnterLowerLimit()) {
					flag = true;
				}
			} else {
				if (gold > majiangRoomCsv.getEnterLowerLimit()
						&& gold < majiangRoomCsv.getEnterUpperLimit()) {
					flag = true;
				}
			}
			if (flag) {
				return roomType;
			}
			for (MajiangChengDuRoomCsv csv : majiangChengDuRoomCache.getConfigList()) {
				if (csv.getEnterUpperLimit() == -1) {
					roomType = csv.getRoomID();
					break;
				} else {
					if (gold > csv.getEnterUpperLimit()) {
						continue;
					}
					if (gold < csv.getEnterLowerLimit()) {
						continue;
					}
					roomType = csv.getRoomID();
					break;
				}
			}
		}

		return roomType;
	}

	/**
	 * 牌桌缺乏人数队列，已满不添加，焖鸡不添加
	 * @param game
	 */
	public void insertReadyDeque(Game game){
		int realType = game.getRealType();
		int random =  MathUtil.randomNumber(2, 5);
		LogUtil.info(""+random+"秒后匹配机器人");
		int nowTime = TimeUtil.time() + random;
		if(getGameMinRole(realType) > game.getSpriteMap().size() && game.getGameType() != GameType.MENJI){
			ConcurrentHashMap<Integer, ConcurrentLinkedDeque<Long>> map;
			ConcurrentLinkedDeque<Long> tableIdDeque;
			if(readyDeque.get(realType) == null){
				map = new ConcurrentHashMap<Integer, ConcurrentLinkedDeque<Long>>();
				tableIdDeque = new ConcurrentLinkedDeque<Long>();
				tableIdDeque.addFirst(game.getRoomId());
				map.put(nowTime, tableIdDeque);
				readyDeque.put(realType, map);
			}else{
				map = readyDeque.get(realType);
				boolean flag = false;
				for(ConcurrentLinkedDeque<Long> deque : map.values()){
					if(deque.contains(game.getRoomId())){
						flag = true;
						break;
					}
				}
				if(!flag){
					if(map.get(nowTime) != null){
						tableIdDeque = map.get(nowTime);
					}else{
						tableIdDeque = new ConcurrentLinkedDeque<Long>();
					}
					tableIdDeque.addLast(game.getRoomId());
					map.put(nowTime, tableIdDeque);
					readyDeque.put(realType, map);
				}
			}
			
			
		}
	}
	
	/**
	 * 定时帮没有达到人数的牌桌添加AI
	 */
	public void removeReadyDeque(){
		int nowTime = TimeUtil.time();
		for(Integer realType : readyDeque.keySet()){
			ConcurrentHashMap<Integer, ConcurrentLinkedDeque<Long>> map = readyDeque.get(realType);
			if(map != null){
				for(Integer time : map.keySet()){
					if(time <= nowTime){
						ConcurrentLinkedDeque<Long> deque = map.get(time);
						if(deque.size() <= 0){
							map.keySet().remove(time);
						}else{
							for(int i = 0; i < deque.size(); i++){
								long roomId = deque.pollFirst();
								Game game = getGame(roomId);
								if(game != null && game.getSpriteMap().size() < getGameMinRole(game.getRealType())){
									int num = getGameMinRole(game.getRealType())-game.getSpriteMap().size();
									boolean flag = true;
									if(game.getStatus() != GameStatus.RUNNING && game.getStatus() != GameStatus.END){
										LogUtil.info("创建一个AI");
										LogUtil.info("游戏状态"+game.getStatus());
//										createRobotEnter(1, game);
										flag = false;
									}
									
									if(flag || num == 2){
										insertReadyDeque(game);
									}
								}
							}
							if(deque.size() <= 0){
								map.keySet().remove(time);
							}
						}
					}else{
						if(map.get(time).size() <= 0){
							map.keySet().remove(time);
						}
					}
				}
				if(map.keySet().size()== 0){
					readyDeque.remove(realType);
				}
			}
		}
	}
	
	/**
	 * 获取斗牛房间列表
	 * 
	 * @param role
	 */
	public List<List<Long>> listDouniuRooms(int level) {
		return douniuRoomMap.get(level);
	}

	public Map<Long, ConcurrentHashMap<Integer, Game>> getAIExitOrReadyMap(){
		return AIExitOrReadyMap;
	}
	
	public Map<Long, Long> gettimeReadyMap(){
		return timeReadyMap;
	}
	
	/**
	 * AI准备或者离开
	 * @param game
	 * @param rid
	 * @param readyOrexit
	 */
	public void AIWantByeOrReady(Game game,long rid,int readyOrexit){
		if(readyOrexit == HandleType.AI_END_EXIT){
			LogUtil.info("AI要拜拜了");
		}else{
			LogUtil.info("AI要准备了");
		}
		timeReadyMap.put(rid,System.currentTimeMillis() + MathUtil.randomNumber(1, 5) * 1000);
		ConcurrentHashMap<Integer, Game> readyGameMap = new ConcurrentHashMap<Integer, Game>();
		if(!AIExitOrReadyMap.containsKey(rid)){
			readyGameMap.put(readyOrexit, game);
			AIExitOrReadyMap.put(rid, readyGameMap);
		}else{
			readyGameMap = AIExitOrReadyMap.get(rid);
			readyGameMap.put(readyOrexit, game);
			AIExitOrReadyMap.put(rid, readyGameMap);
		}
	}
	
	/**
	 * AI离开
	 */
	public void AIBye(){
		long nowTime = System.currentTimeMillis();
		for (Long rid : timeReadyMap.keySet()) {
			Long time = timeReadyMap.get(rid);
			if(nowTime >= time){
				ConcurrentHashMap<Integer, Game> gameMap = AIExitOrReadyMap.get(rid);
				if(gameMap != null){
					for(Integer readyOrExit : gameMap.keySet()){
						if(gameMap.get(readyOrExit) != null){
							Event event = new Event(readyOrExit,gameMap.get(readyOrExit),rid);
							DispatchEvent.dispacthEvent(event);
						}
						gameMap.keySet().remove(readyOrExit);
					}
				}
				AIExitOrReadyMap.remove(rid);
				timeReadyMap.keySet().remove(rid);
			}
		}
	}
	
	/**
	 * AI准备
	 */
	public void AIready(Game game,long rid){
		GameRole gameRole = game.getSpriteMap().get(rid);
		if(gameRole != null){
			LogUtil.info(gameRole.getRole().getNick() + "准备");
			gameRole.setStatus(PlayerState.PS_PREPARE_VALUE);
			if(game.getGameType() == GameType.MAJIANG){
				GMsg_12011001.Builder builder = GMsg_12011001.newBuilder();
				builder.setSeat(gameRole.getSeat());
				roleFunction.sendMessageToPlayers(game.getRoles(),
						builder.build());
			}else if(game.getGameType() == GameType.DOUDIZHU){
				GMsg_12012001.Builder builder = GMsg_12012001.newBuilder();
				builder.setSeat(gameRole.getSeat());
				roleFunction.sendMessageToPlayers(game.getRoles(),
						builder.build());
			}else if(game.getGameType() == GameType.ZXMAJIANG){
				GMsg_12041001.Builder builder = GMsg_12041001.newBuilder();
				builder.setSeat(gameRole.getSeat());
				roleFunction.sendMessageToPlayers(game.getRoles(),
						builder.build());
			}
			
		}
	}
}
