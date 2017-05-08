package com.yaowan.server.game.model.dezhou.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameStatus;
import com.yaowan.constant.GameType;
import com.yaowan.constant.MoneyEvent;
import com.yaowan.core.base.Spring;
import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.cross.CrossPlayer;
import com.yaowan.csv.cache.DZConfigCache;
import com.yaowan.csv.entity.DZConfigCsv;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.DispatchEvent;
import com.yaowan.framework.core.events.listener.EventListener;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.GameRole;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.DZCardProto;
import com.yaowan.protobuf.game.GGame.GGameInfo;
import com.yaowan.protobuf.game.GGame.GGameRole;
import com.yaowan.protobuf.game.GGame.GMsg_12006002;
import com.yaowan.protobuf.game.GGame.GMsg_12006004;
import com.yaowan.protobuf.game.GGame.GMsg_12006008;
import com.yaowan.protobuf.game.GGame.GOnlineData;
import com.yaowan.protobuf.game.GGame.PlayerState;
import com.yaowan.scheduler.SchedulerBean;
import com.yaowan.server.game.event.GameChangeTableEvent;
import com.yaowan.server.game.event.GameExitTableEvent;
import com.yaowan.server.game.event.GameMatchingEvent;
import com.yaowan.server.game.event.GameServerStartEvent;
import com.yaowan.server.game.event.RoomOnlineEvent;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.dezhou.DZPlayer;
import com.yaowan.server.game.model.dezhou.DZRoom;
import com.yaowan.server.game.model.dezhou.service.DZCardService;
import com.yaowan.server.game.service.RoomService;
import com.yaowan.util.RandomPlayerCount;

@Component
public class DZCardFunction extends FunctionAdapter implements EventListener {
	@Autowired
	private DZConfigCache dzConfigCache;
	@Autowired
	private DZCardService dzCardService;
	@Autowired
	private RoleFunction roleFunction;
	@Autowired
	private RoomFunction roomFunction;
	@Autowired
	private SchedulerBean schedulerBean;
	@Autowired
	private RoomService roomService;

	private ConcurrentMap<Long, DZRoom> dzRoomMap = new ConcurrentHashMap<Long, DZRoom>();

	public Collection<DZRoom> getDzRooms() {
		return dzRoomMap.values();
	}

	public final DZRoom getDzRoom(final long roomId) {
		if (!dzRoomMap.containsKey(roomId)) {
			throw new RuntimeException("DZRoom  not exists!!");
		}
		return dzRoomMap.get(roomId);
	}

	public List<DZConfigCsv> getDzConfigCsvs(int roomType) {
		return dzConfigCache.getDzConfigCsvs(roomType);
	}

	public Collection<Integer> getRoomCids() {
		return dzConfigCache.getRoomCids();
	}

	public List<Integer> getRoomTypes() {
		return dzConfigCache.getRoomTypes();
	}

	/**
	 * 获取放假所属的分类
	 * 
	 * @param cid
	 *            配置编号
	 * @return
	 */
	public int getDZRoomType(int cid) {
		return getDzConfigCsv(cid).getRoomType();
	}

	/**
	 * 获取单条配置项
	 * 
	 * @param id
	 * @return
	 */
	public DZConfigCsv getDzConfigCsv(int id) {
		return dzConfigCache.getDzConfigCsv(id);
	}

	/***
	 * 初始化游戏进入
	 * 
	 * @param game
	 */
	public void initTable(Game game) {
		int cid = game.getRoomType(); // 配置项中的编号
		final long roomId = game.getRoomId();

		DZConfigCsv dzConfigCsv = dzConfigCache.getDzConfigCsv(cid);
		DZRoom room = new DZRoom(game, dzConfigCsv.getID(),
				dzConfigCsv.getPeople(), dzConfigCsv.getBigBlind());
		dzRoomMap.put(roomId, room);
		// 筹码推送
		DZCardProto.GMsg_12051104.Builder builder = DZCardProto.GMsg_12051104
				.newBuilder();
		DZCardProto.MsgDZCardJeton.Builder jetonBuilder = DZCardProto.MsgDZCardJeton
				.newBuilder();
		// 生成DZPlayer对象
		Map<Long, GameRole> spriteMap = game.getSpriteMap();
		for (Long rid : spriteMap.keySet()) {

			Player player = roleFunction.getPlayer(rid);

			// 筹码兑换
			int jeton = dzConfigCsv.getInitialJetton();
			if (player.getRole().getGold() < jeton) {
				jeton = player.getRole().getGold();
			}
			roleFunction.goldSub(player.getRole(), jeton, MoneyEvent.DEZHOU,
					true);

			GameRole gameRole = spriteMap.get(rid);
			DZPlayer dzPlayer = new DZPlayer(rid, jeton);
			dzPlayer.setPosition(gameRole.getSeat());
			room.seat(dzPlayer);

			jetonBuilder.setRid(rid);
			jetonBuilder.setJeton(jeton);
			builder.addJetions(jetonBuilder.build());
			jetonBuilder.clear();
		}
		room.sendMessageToAll(builder.build(), null);

		// 倒计时启动游戏
		startScheduler(roomId, dzConfigCsv.getOperationTime() * 1000);
	}

	/**
	 * 利用定时器启动游戏
	 * 
	 * @param roomId
	 * @param interval
	 */
	public void startScheduler(final long roomId, int interval) {
		// 启动定时器，到时间开始游戏
		schedulerBean.submit("DEZHOU_START", interval, new Runnable() {
			@Override
			public void run() {
				DZRoom dzRoom = getDzRoom(roomId);
				if (dzRoom == null)
					return; // 此时说明玩家都已经离开
				dzRoom.start();
				// 设置为游戏中状态
				Game game = dzRoom.getGame();
				game.setStatus(GameStatus.RUNNING);
				Iterator<GameRole> iterator = game.getSpriteMap().values()
						.iterator();
				while (iterator.hasNext()) {
					GameRole gameRole = (GameRole) iterator.next();
					gameRole.setStatus(PlayerState.PS_PLAY_VALUE);
					// 广播一个状态，大家已经在游戏中 ......
					DZCardProto.GMsg_12051105.Builder builder = DZCardProto.GMsg_12051105
							.newBuilder();
					builder.setZrid(dzRoom.getBanker().getRid());
					builder.setDmrid(dzRoom.getDaMang().getRid());
					builder.setXmrid(dzRoom.getXiaoMang().getRid());
					dzRoom.sendMessageToAll(builder.build(), 0);
				}
			}
		});
	}

	/**
	 * 
	 * 游戏开始后进入游戏
	 * 
	 * @param owner
	 * @return
	 */
	public void enterTable(Game game, Role role) {

		int cid = game.getRoomType(); // 配置项中的编号
		long roomId = game.getRoomId();

		DZConfigCsv dzConfigCsv = dzConfigCache.getDzConfigCsv(cid);

		if (!dzRoomMap.containsKey(roomId)) {
			throw new RuntimeException("enterTable 错误， DZRoom not exists！！！");
		}
		DZRoom room = dzRoomMap.get(roomId);

		DZPlayer dzPlayer = new DZPlayer(role.getRid(),
				dzConfigCsv.getInitialJetton());
		room.seat(dzPlayer);

		GameRole gameRole = new GameRole(role, game.getRoomId());
		gameRole.setSeat(dzPlayer.getPosition());
		game.getRoles().add(role.getRid());
		game.getSpriteMap().put(role.getRid(), gameRole);

		GMsg_12006002.Builder builder = GMsg_12006002.newBuilder();
		gameRole.setStatus(PlayerState.PS_SEAT_VALUE);

		GGameInfo.Builder info = GGameInfo.newBuilder();
		info.setGameType(game.getGameType());
		info.setRoomId(game.getRoomId());
		info.setRoomType(game.getRoomType());

		GGameRole.Builder temp = null;

		for (Map.Entry<Long, GameRole> entry : game.getSpriteMap().entrySet()) {
			GGameRole.Builder builder3 = GGameRole.newBuilder();
			Role target = entry.getValue().getRole();
			builder3.setRid(entry.getKey());
			builder3.setNick(target.getNick());
			builder3.setGold(target.getGold());
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

			if (entry.getKey() == gameRole.getRole().getRid()) {
				temp = builder3;
			}
		}
		builder.setGame(info);
		// 发送
		room.sendMessage(dzPlayer, builder.build());

		// 其他人收到进入房间
		GMsg_12006008.Builder msg = GMsg_12006008.newBuilder();
		GGameRole.Builder builder3 = GGameRole.newBuilder();

		builder3.setRid(gameRole.getRole().getRid());
		builder3.setNick(gameRole.getRole().getNick());
		builder3.setGold(gameRole.getRole().getGold());
		builder3.setHead(gameRole.getRole().getHead());
		builder3.setLevel(gameRole.getRole().getLevel());
		builder3.setSeat(gameRole.getSeat());
		builder3.setAvatarId(gameRole.getAvatarId());
		builder3.setSex(gameRole.getRole().getSex());
		msg.setRoleInfo(temp);
		List<Long> otherList = new ArrayList<Long>();
		otherList.addAll(game.getRoles());
		otherList.remove(gameRole.getRole().getRid());
		// 发送
		room.sendMessageToAll(msg.build(), dzPlayer);

		// 推送筹码
		DZCardProto.GMsg_12051104.Builder builder2 = DZCardProto.GMsg_12051104
				.newBuilder();
		DZCardProto.MsgDZCardJeton.Builder jetonBuilder = DZCardProto.MsgDZCardJeton
				.newBuilder();

		// 筹码兑换
		int jeton = dzConfigCsv.getInitialJetton();
		if (role.getGold() < jeton) {
			jeton = role.getGold();
		}
		roleFunction.goldSub(role, jeton, MoneyEvent.DEZHOU, true);

		jetonBuilder.setRid(role.getRid());
		jetonBuilder.setJeton(jeton);
		builder2.addJetions(jetonBuilder.build());
		jetonBuilder.clear();
		room.sendMessageToAll(builder2.build(), null);
	}

	private void roomOnline(Event event, int handleType) {
		Player player = (Player) event.getParam()[0];
		int gameType = (Integer) event.getParam()[1];
		roomOnlineOfDZ(player, gameType);
	}
	private void roomOnlineOfDZ(Player player, int gameType) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (Map.Entry<Integer, AtomicInteger> entry : roomFunction
				.getRoomOnline(gameType).entrySet()) {
			int roomId = entry.getKey();
			int roomType = getDZRoomType(roomId);
			if (!map.containsKey(roomType)) {
				map.put(roomType, 0);
			}
			map.put(roomType, map.get(roomType) + entry.getValue().get());
		}
		GMsg_12006004.Builder builder = GMsg_12006004.newBuilder();
		GOnlineData.Builder onlineData = GOnlineData.newBuilder();
		List<Integer> types = getRoomTypes();
		int realCount = 0;
		for (Integer roomType : types) {

			onlineData.setType(roomType);
			int randomCount = RandomPlayerCount.roomCount(gameType);
			realCount = 0;
			if (map.containsKey(roomType)) {
				realCount = map.get(roomType);
			}
			int count = (int) Math.round(randomCount * 0.73);
			onlineData.setNum(realCount + count);
			builder.addOnlineData(onlineData.build());
			onlineData.clear();
		}
		player.write(builder.build());
	}

	@Override
	public void listenIn(Event event, int handleType) {
		switch (handleType) {
		case HandleType.GAME_MATCHING:// 游戏匹配时条用
			matchGame(event, handleType);
			break;
		case HandleType.GAME_EXIT_TABLE: // 退出桌子
			exitTable(event, handleType);
			break;
		case HandleType.GAME_CHANGE_TABLE:// 换桌子
			changeTable(event, handleType);
		case HandleType.ROOM_ONLINE:// 房间在线
			roomOnline(event, handleType);
		case HandleType.GAME_SERVER_START:	
			serverStart(event,handleType);
		default:
			break;
		}
	}

	@Override
	public void addToEventHandlerAddListenerAdapter() {
		Spring.getBean(GameMatchingEvent.class).addListener(this);
		Spring.getBean(GameExitTableEvent.class).addListener(this);
		Spring.getBean(GameChangeTableEvent.class).addListener(this);
		Spring.getBean(RoomOnlineEvent.class).addListener(this);
		Spring.getBean(GameServerStartEvent.class).addListener(this);
	}
	
	private void serverStart(Event event,int handleType){
		int gameType = (Integer)event.getParam()[0];
		if(gameType != GameType.DEZHOU){
			return;
		}
		roomFunction.initGameCacheVal(gameType, getRoomCids());
	}
	/**
	 * 换桌子
	 * 
	 * @param event
	 * @param eventHandle
	 */
	private void changeTable(Event event, int eventHandle) {
		Game game = null;
		DZPlayer dzPlayer = null;
		DZRoom dzRoom = null;
		if (event.getParam()[0] instanceof Player) {
			Player player = (Player) event.getParam()[0];
			game = roomFunction.getGameByRole(player.getRole().getRid());
			if (game == null
					|| (game != null && game.getGameType() != GameType.DEZHOU)) {
				return;
			}
			dzRoom = getDzRoom(game.getRoomId());
			dzPlayer = dzRoom.getDzPlayer(player.getRole().getRid());
		} else if (event.getParam()[0] instanceof DZPlayer) {
			dzPlayer = (DZPlayer) event.getParam()[0];
			dzRoom = dzPlayer.getRoom();
			game = dzRoom.getGame();
		}

		Role role = game.getSpriteMap().get(dzPlayer.getRid()).getRole();
		DispatchEvent
				.dispacthEvent(new Event(HandleType.GAME_EXIT_TABLE, role));

		Player player = roleFunction.getPlayer(dzPlayer.getRid());
		roomService.joinGame(player, GameType.DEZHOU, dzRoom.getCid());
	}

	// 离开桌子
	private void exitTable(Event event, int eventHandle) {

		if (event.getParam() == null || !(event.getParam()[0] instanceof Role)) {
			throw new RuntimeException(
					"GameExitTableEvent ，传参错误 ,param [Role] : "
							+ event.getParam()[0].getClass());
		}
		Role role = (Role) event.getParam()[0];
		Game game = roomFunction.getGameByRole(role.getRid());
		if (game == null
				|| (game != null && game.getGameType() != GameType.DEZHOU)) {
			return;
		}

		DZRoom dzRoom = getDzRoom(game.getRoomId());
		DZPlayer dzPlayer = dzRoom.getDzPlayer(role.getRid());

		// 把剩下的筹码转换成金币
		roleFunction
				.goldAdd(role, dzPlayer.getJeton(), MoneyEvent.DEZHOU, true);
		dzPlayer.setJeton(0);
		dzRoom.quitRoom(dzPlayer);
		roomFunction.quitRole(game, role);

		Long gameId = roomFunction.getRoleGameMap().remove(role.getRid());
		if (gameId != null) {
			roomFunction
					.decrementOnline(game.getGameType(), game.getRoomType());
		}

		// 如果最后一个人离开后，那么就应该解散
		if (dzRoom.getPlayerCount() <= 0) {
			dzRoomMap.remove(dzRoom.getGid());
		}
	}

	/**
	 * 游戏匹配处理
	 * 
	 * @param event
	 * @param eventHandle
	 */
	private void matchGame(Event event, int eventHandle) {
		int realType = (Integer) event.getParam()[1];
		int gameType = roomFunction.getGameType(realType);
		if (gameType != GameType.DEZHOU) {
			return;
		}
		Role role = (Role) event.getParam()[0];
		int fix = 0;

		DZConfigCsv csv = getDzConfigCsv(roomFunction.getRoomType(realType));

		if (csv != null) {
			if ((csv.getHighestLimit() == 0 || (role.getGold() <= csv
					.getHighestLimit()))
					&& (csv.getLowestLimit() == 0 || (role.getGold() >= csv
							.getLowestLimit()))) {
				fix = csv.getID();
			}
		}
		if (fix == 0) { // 不满足条件
			return;
		}
		boolean flag = true;
		int num = roomFunction.getGameMinRole(realType);
		for (Map.Entry<Long, Game> entry : roomFunction.getRunningGames()
				.entrySet()) {
			Game game = entry.getValue();

			if (game.getGameType() == gameType && gameType == GameType.DEZHOU) {
				// 德州
				if (game.getSpriteMap().size() < num) {// 匹配成功
					roomFunction.enterGame(game, role);
					if (roomFunction.getReadyQueue(realType) != null) {
						ConcurrentHashMap<Integer, ConcurrentLinkedDeque<Long>> map = roomFunction
								.getReadyQueue(realType);
						for (ConcurrentLinkedDeque<Long> deque1 : map.values()) {
							if (deque1.contains(game.getRoomId())) {
								if (game.getSpriteMap().size() >= num) {
									deque1.remove(game.getRoomId());
								}
							}
						}
					}
					flag = false;
					break;
				}
			}
		}

		if (flag) {
			ConcurrentLinkedDeque<Role> deque = roomFunction
					.getGameDeque(realType);
			deque.addFirst(role);
			int min = 1;
			if (deque.size() >= min) {

				List<Set<Role>> resultList = new ArrayList<>();
				// 对队列加锁
				synchronized (deque) {
					LogUtil.info(deque.size() + "," + num);
					for (int i = 0; i < 10 && deque.size() >= min; i++) {
						Set<Role> rolesSet = roomFunction.getOneMatchRoles(
								deque, deque.size());
						if (rolesSet != null) {
							resultList.add(rolesSet);
						} else {
							LogUtil.info("没有人可以匹配");
						}
					}
				}
				flag = false;
				roomFunction.gameMatchSuccess(realType, resultList);
			}
		}
	}

}
