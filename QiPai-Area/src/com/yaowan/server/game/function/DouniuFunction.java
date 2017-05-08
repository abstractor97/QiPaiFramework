package com.yaowan.server.game.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameError;
import com.yaowan.constant.GameStatus;
import com.yaowan.constant.GameType;
import com.yaowan.constant.MoneyEvent;
import com.yaowan.constant.MoneyType;
import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.csv.cache.ExpCache;
import com.yaowan.csv.cache.NiuniuAiBasicCache;
import com.yaowan.csv.cache.NiuniuMultipleCache;
import com.yaowan.csv.cache.NiuniuRoomCache;
import com.yaowan.csv.entity.ExpCsv;
import com.yaowan.csv.entity.NiuniuAiBasicCsv;
import com.yaowan.csv.entity.NiuniuRoomCsv;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.DispatchEvent;
import com.yaowan.framework.core.thread.ISingleData;
import com.yaowan.framework.core.thread.SingleThreadManager;
import com.yaowan.framework.core.thread.SingleThreadTask;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.MathUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.GameRole;
import com.yaowan.protobuf.game.GDouniu.GDouniuEnd;
import com.yaowan.protobuf.game.GDouniu.GDouniuOwner;
import com.yaowan.protobuf.game.GDouniu.GDouniuRecord;
import com.yaowan.protobuf.game.GDouniu.GDouniuTable;
import com.yaowan.protobuf.game.GDouniu.GDouniuXian;
import com.yaowan.protobuf.game.GDouniu.GMsg_12025001;
import com.yaowan.protobuf.game.GDouniu.GMsg_12025002;
import com.yaowan.protobuf.game.GDouniu.GMsg_12025004;
import com.yaowan.protobuf.game.GDouniu.GMsg_12025005;
import com.yaowan.protobuf.game.GDouniu.GMsg_12025006;
import com.yaowan.protobuf.game.GDouniu.GMsg_12025007;
import com.yaowan.protobuf.game.GDouniu.GMsg_12025009;
import com.yaowan.protobuf.game.GDouniu.GMsg_12025010;
import com.yaowan.protobuf.game.GDouniu.GMsg_12025011;
import com.yaowan.protobuf.game.GGame.GGameInfo;
import com.yaowan.protobuf.game.GGame.GGameRole;
import com.yaowan.protobuf.game.GGame.GMsg_12006005;
import com.yaowan.protobuf.game.GGame.GMsg_12006008;
import com.yaowan.protobuf.game.GGame.PlayerState;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.model.data.entity.Npc;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.dao.DouniuLogDao;
import com.yaowan.server.game.model.log.dao.DouniuWangLogDao;
import com.yaowan.server.game.model.log.entity.DouniuLog;
import com.yaowan.server.game.model.log.entity.DouniuWangLog;
import com.yaowan.server.game.model.struct.DouniuRole;
import com.yaowan.server.game.model.struct.DouniuTable;
import com.yaowan.server.game.model.struct.DouniuXian;
import com.yaowan.server.game.model.struct.MissionType;
import com.yaowan.server.game.model.struct.TaskType;
import com.yaowan.server.game.rule.DouniuRule;

/**
 *  欢乐斗牛
 *
 * @author zane
 */
@Component
public class DouniuFunction extends FunctionAdapter {

	@Autowired
	private SingleThreadManager manager;

	@Autowired
	private DouniuDataFunction douniuDataFunction;

	@Autowired
	private RoleFunction roleFunction;

	@Autowired
	private RoomFunction roomFunction;


	private Map<Long, DouniuTable> tableMap = new ConcurrentHashMap<>();

	private Map<Long, DouniuRole> roleMap = new ConcurrentHashMap<>();


	private static Map<Integer, AtomicIntegerArray> roomCountMap = new ConcurrentHashMap<>();
	
	
	@Autowired
	private NiuniuRoomCache niuniuRoomCache;


	@Autowired
	private NiuniuAiBasicCache niuniuAiBasicCache;
	
	@Autowired
	private ExpCache expCache;

	@Autowired
	MissionFunction missionFunction;

	// G_T_C 日志流水处理dao
	@Autowired
	private DouniuLogDao douniuLogDao;

	// G_T_C 坐庄日志流水处理dao
	@Autowired
	private DouniuWangLogDao douniuWangLogDao;
	
	@Autowired
	private RoomLogFunction roomLogFunction;
	
	@Autowired
	private NiuniuMultipleCache niuniuMultipleCache;
	
	@Autowired
	private NPCFunction npcFunction;
	
	@Override
	public void handleOnServerStart() {
		// 低级房统计初始化,中级房统计初始化,高级房统计初始化
		for (NiuniuRoomCsv csv:niuniuRoomCache.getConfigList()) {
			AtomicIntegerArray log = new AtomicIntegerArray(8);
			log.set(0, 0);// 对战数
			log.set(1, 0);// 破产数
			log.set(2, 0);// 房费数
			log.set(3, 0);// 庄家破产数
			log.set(4, 0);// 闲家破产数
			log.set(5, 0);// 普通破产数
			log.set(6, 0);// 困难破产数
			log.set(7, 0);// 抽水数
			roomCountMap.put(csv.getId(), log);
		}	
	}

	public DouniuTable getTable(Long id) {
		DouniuTable table = tableMap.get(id);
		return table;
	}

	public DouniuTable getTableByRole(Long id) {
		DouniuRole douniuRole = getRole(id);
		if(douniuRole==null){
			return null;
		}
		return getTable(douniuRole.getRole().getRoomId());
	}

	public DouniuRole getRole(Long id) {
		DouniuRole role = roleMap.get(id);
		return role;
	}

	public void addRoleCache(DouniuRole role) {
		roleMap.put(role.getRid(), role);
	}

	public void removeRoleCache(Long id) {
		roleMap.remove(id);
	}
	
	/**
	 * 检测是否需要动态创建一组牛牛房间
	 * 增加条件：该roomLv已加入玩家人数/总容纳人数 >= 0.8f
	 * @param game
	 */
	public void checkAddNewDouniuRoom(Game douniuGame) {
		NiuniuRoomCsv csv = niuniuRoomCache.getConfig(douniuGame.getRoomType());	
		int realMemberCount = 0;
		List<Long> gameList = roomFunction.listDouniuRooms(csv.getRoomLv());
		for (Long roomId : gameList){
			DouniuTable douniuTable = getTable(roomId);
			if (douniuTable != null) {
				realMemberCount += douniuTable.getPlayers().size();
			}
		}
		
		int allowMemberCount = gameList.size() * csv.getPlayerMax();
		if (realMemberCount >= allowMemberCount * 0.8) {
			roomFunction.CreateNewGroupDouniuRoom(csv.getRoomLv(), true);
		}	
	}
	
	/**
	 * 检测是否需要删除一组牛牛房间
	 * 删除条件：5分钟内没有人玩
	 * @param douniuGame
	 * @param douniuTable
	 */
	public void checkDeleteNewDouniuRoom(Game douniuGame, DouniuTable douniuTable) {
		// 5分钟房间都没有人玩就移除这张桌子
		if (douniuGame.canDelete() == false 
				|| douniuTable.getPlayers().size() > 0 
				|| douniuGame.getRoomId() != douniuGame.getGroupId()) {
			return;
		}
				
		long curTime = System.currentTimeMillis();
		long lastNoMemberTime = douniuTable.getLastNoMemberTime();
		if (curTime - lastNoMemberTime > 600000) {
			roomFunction.deleteDouniuGroupRoom(douniuGame.getGroupId());
		}
	}
	
	/**
	 * 在游戏结束或者玩家旁观状态可以调用直接清除
	 * @param rid
	 */
	public void clearMember(DouniuTable table, long rid) {
		table.getFightOwner().remove(rid);
		table.getWaitOwner().remove(rid);
		table.getMembers().remove(rid);
		table.getPlayers().remove(rid);
		removeRoleCache(rid);
		
		if (table.getMembers().size() == 0) {
			table.setLastNoMemberTime(System.currentTimeMillis());
		}
	}
	
	public void clear(long roomId) {
		DouniuTable table = tableMap.remove(roomId);
		if (table != null) {
			for (Map.Entry<Long,DouniuRole> entry : table.getMembers().entrySet()) {
				DouniuRole douNiuRole = entry.getValue();
				if (douNiuRole.getRole() != null) {
					removeRoleCache(douNiuRole.getRole().getRole().getRid());
				}
			}
		}
	}
	
	public void resetOverTable(Game game) {
		if (game.getStatus() == GameStatus.END_REWARD) {
			game.setStatus(GameStatus.WAIT_READY);
			DouniuTable table = getTable(game.getRoomId());
			table.reset();
		}
	}

	public void removeMember(DouniuTable table, GameRole gameRole) {
		GMsg_12006005.Builder leaveBuilder = GMsg_12006005.newBuilder();
		leaveBuilder.setCurrentSeat(gameRole.getSeat());
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(), leaveBuilder.build());
		
		DouniuRole douniuRole = table.getMembers().get(gameRole.getRole().getRid());
		
		// 围观群众直接移除
		if (douniuRole.getPlayerType() == 0) {
			clearMember(table, gameRole.getRole().getRid());
		}								
		else { // 下注者/庄家，只要牌局未开始/已结束，就直接移除
			if ( table.getGame().getStatus() == GameStatus.WAIT_READY || table.getGame().getStatus() == GameStatus.END_REWARD) {
				clearMember(table, gameRole.getRole().getRid());
				
				if (douniuRole.getPlayerType() == 1) {
					// 庄家
					if(table.getFightOwner().size() == 0){
						table.setOwnerHp(0);
					}
			
					GMsg_12025011.Builder builder = GMsg_12025011.newBuilder();
					roleFunction.sendMessageToPlayer(douniuRole.getRid(), builder.build());
					 
					GDouniuOwner.Builder ownerBuilder = GDouniuOwner.newBuilder();
					ownerBuilder.setFightOwner(table.getFightOwner().size());
					ownerBuilder.setHandPai(table.getOwnerPai());
					ownerBuilder.setHp(table.getOwnerHp());
					ownerBuilder.setStatus(1);
					ownerBuilder.setWaitOwner(table.getWaitOwner().size());
					 
					for (Long id : table.getFightOwner()) {
						GGameRole.Builder builder3 = GGameRole.newBuilder();
						DouniuRole douniuRole2 = table.getMembers().get(id);
						if (douniuRole2 != null) {
							Role target = gameRole.getRole();
							builder3.setRid(target.getRid());
							builder3.setNick(target.getNick());
							builder3.setGold(target.getGold());
							builder3.setHead(target.getHead());
							builder3.setLevel(target.getLevel());
							builder3.setSeat(gameRole.getSeat());
							builder3.setAvatarId(gameRole.getAvatarId());
							builder3.setSex(target.getSex());
							ownerBuilder.addFightOwnerList(builder3);
						}
					}
					
					GMsg_12025010.Builder builder2 = GMsg_12025010.newBuilder();
					builder2.setOwner(ownerBuilder);
					roleFunction.sendMessageToPlayers(table.getGame().getRoles(), builder2.build());
				}
			}		
		}
	}
	
	public void exitTable(Game game, Long rid) {
		DouniuTable table = getTable(game.getRoomId());
		if (table != null) {
			GameRole gameRole = game.getSpriteMap().get(rid);
			if (gameRole != null) {
				
				removeMember(table, gameRole);
						
				gameRole.setStatus(PlayerState.PS_EXIT_VALUE);

				game.getRoles().set(gameRole.getSeat() - 1, 0l);
				if (game.getSpriteMap().remove(rid) != null) {
					removeRoleCache(rid);
				}
				
				LogUtil.info("exitTable nick" + gameRole.getRole().getNick());
				/*if (game.getSpriteMap().size() <= 0) {
					tableMap.remove(game.getRoomId());
				}*/
			}
		}
	}


	/**
	 * 定时检测所有人准备游戏开始 已被同步
	 */
	public void checkStart(Game game) {
		
		DouniuTable table = getTable(game.getRoomId());
		NiuniuRoomCsv niuniuRoomCsv = niuniuRoomCache.getConfig(game.getRoomType());
		
		NiuniuAiBasicCsv niuniuAiBasicCsv = niuniuAiBasicCache.getConfig(game.getRoomType());
		
		long time = game.getStartTime();
		if (game.getEndTime() > game.getStartTime()) {
			time = game.getEndTime();
		}
		int dif = (int) (System.currentTimeMillis() - time) / 1000;
		
		// 没人当王，就创建机器人进场，机器人会抢着当王
		if (table.getMembers().size() > 0
				&& table.getFightOwner().size() == 0
				&& dif >= niuniuAiBasicCsv.getAiEntertime()) {
			roomFunction.createRobotEnter(1, game);
		}
		
		if (game.getStatus() != GameStatus.WAIT_READY) {
			return;
		}
		boolean isRoleOK = true;
		
		// 5秒不准备
		if (table.getFightOwner().size() == 0 || table.getPlayers().size() == 0) {
			isRoleOK = false;
		}
		
		if (game.getSpriteMap().size() <= 1) {
			isRoleOK = false;
		}
		if (isRoleOK) {
			startTable(game);
		} else {
			checkDeleteNewDouniuRoom(game, table);
		}
	}

	/**
	 * 已被同步
	 * @param game
	 * @param role
	 */
	public void playerPrepare(Game game, GameRole role) {
		DouniuTable table = getTable(game.getRoomId());
		NiuniuRoomCsv niuniuRoomCsv = niuniuRoomCache.getConfig(game.getRoomType());
		if(table.getPlayers().size() >= niuniuRoomCsv.getPlayerMax()){
			GMsg_12025001.Builder builder = GMsg_12025001.newBuilder();
			builder.setFlag(GameError.GAME_FULL);
			roleFunction.sendMessageToPlayer(role.getRole().getRid(), builder.build());
			return;
		}
		if (table.getFightOwner().contains(role.getRole().getRid())
				|| table.getWaitOwner().contains(role.getRole().getRid())) {
			GMsg_12025001.Builder builder = GMsg_12025001.newBuilder();
			builder.setFlag(GameError.WRONG_PARAMETER);
			roleFunction.sendMessageToPlayer(role.getRole().getRid(), builder.build());
			return;
		}
		role.setStatus(PlayerState.PS_PREPARE_VALUE);
		DouniuRole member = getRole(role.getRole().getRid());
		member.setPlayerType(2);
		if(!table.getPlayers().contains(role.getRole().getRid())){
			table.getPlayers().add(role.getRole().getRid());
			
			// 检测是否需要增加牛牛房间
			checkAddNewDouniuRoom(game);
			
			// 日志记录
			roomLogFunction.dealRoomRoleActiveLog(role.getRole(), game.getGameType(), game.getRoomType());
		}
	
		GMsg_12025001.Builder builder = GMsg_12025001.newBuilder();
		builder.setFlag(0);
		roleFunction.sendMessageToPlayer(role.getRole().getRid(), builder.build());	
	}
	
	/**
	 * 已被同步
	 * @param game
	 * @param role
	 */
	public void cancelOwner(DouniuTable table, DouniuRole member) {		
		NiuniuRoomCsv niuniuRoomCsv = niuniuRoomCache.getConfig(table.getGame()
				.getRoomType());
		
		table.getFightOwner().remove(member.getRid());
		table.getWaitOwner().remove(member.getRid());
		for (int i = 0; i < niuniuRoomCsv.getOwnerMax()-table.getFightOwner().size()
				&& i < table.getWaitOwner().size(); i++) {
			Long id = table.getWaitOwner().remove(0);
			table.getFightOwner().add(id);
			DouniuRole role = table.getMembers().get(id);
			role.setPlayerType(1);
			table.getPlayers().remove(id);
		}

		member.getRole().setStatus(PlayerState.PS_PREPARE_VALUE);
		member.setPlayerType(2);
		
		if(table.getFightOwner().size()==0){
			table.setOwnerHp(0);
		}

		GMsg_12025011.Builder builder = GMsg_12025011.newBuilder();
		roleFunction.sendMessageToPlayer(member.getRid(), builder.build());
		
		GDouniuOwner.Builder ownerBuilder = GDouniuOwner.newBuilder();
		ownerBuilder.setFightOwner(table.getFightOwner().size());
		ownerBuilder.setHandPai(table.getOwnerPai());
		ownerBuilder.setHp(table.getOwnerHp());
		ownerBuilder.setStatus(1);
		ownerBuilder.setWaitOwner(table.getWaitOwner().size());
		
		
		for (Long id : table.getFightOwner()) {
			GGameRole.Builder builder3 = GGameRole.newBuilder();
			DouniuRole douniuRole = table.getMembers().get(id);
			if (douniuRole != null) {
				GameRole gameRole = douniuRole.getRole();
				Role role = gameRole.getRole();
				builder3.setRid(role.getRid());
				builder3.setNick(role.getNick());
				builder3.setGold(role.getGold());
				builder3.setHead(role.getHead());
				builder3.setLevel(role.getLevel());
				builder3.setSeat(gameRole.getSeat());
				builder3.setAvatarId(gameRole.getAvatarId());
				builder3.setSex(role.getSex());
				ownerBuilder.addFightOwnerList(builder3);
			}
		}
		
		GMsg_12025010.Builder builder2 = GMsg_12025010.newBuilder();
		builder2.setOwner(ownerBuilder);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(), builder2.build());
		
	}
	
	/**
	 * 已被同步
	 * @param game
	 * @param role
	 */
	public void applyOwner(DouniuTable table, DouniuRole member) {
		NiuniuRoomCsv niuniuRoomCsv = niuniuRoomCache.getConfig(table.getGame().getRoomType());
		
		if(member.getRole().getRole().getGold() < niuniuRoomCsv.getQwnerEnterLowerLimit()){
			GMsg_12025009.Builder builder = GMsg_12025009.newBuilder();
			builder.setFlag(GameError.ROLE_MONEY_LACK);
			roleFunction.sendMessageToPlayer(member.getRid(), builder.build());
			LogUtil.error("member.getRole().getRole().getGold()"+member.getRole().getRole().getGold());
			return;
		}
		if(table.getFightOwner().contains(member.getRid()) || table.getWaitOwner().contains(member.getRid())){
			return;
		}
		if (table.getFightOwner().size() >= niuniuRoomCsv.getOwnerMax()) {
			table.getWaitOwner().add(member.getRid());
		} else {
			if (table.getFightOwner().size() == 0) {
				table.setOwnerHp(niuniuRoomCsv.getHp());
			}
			table.getFightOwner().add(member.getRid());
		}
		table.getPlayers().remove(member.getRid());

		member.getRole().setStatus(PlayerState.PS_PLAY_VALUE);
		member.setPlayerType(1);

		GMsg_12025009.Builder builder = GMsg_12025009.newBuilder();
		builder.setFlag(0);
		roleFunction.sendMessageToPlayer(member.getRid(), builder.build());
		
		GDouniuOwner.Builder ownerBuilder = GDouniuOwner.newBuilder();
		ownerBuilder.setFightOwner(table.getFightOwner().size());
		ownerBuilder.setHandPai(table.getOwnerPai());
		ownerBuilder.setHp(table.getOwnerHp());
		ownerBuilder.setStatus(1);
		ownerBuilder.setWaitOwner(table.getWaitOwner().size());
		
		for (Long id : table.getFightOwner()) {
			GGameRole.Builder builder3 = GGameRole.newBuilder();
			DouniuRole douniuRole = table.getMembers().get(id);
			if (douniuRole != null) {
				GameRole gameRole = douniuRole.getRole();
				Role target = gameRole.getRole();
				builder3.setRid(target.getRid());
				builder3.setNick(target.getNick());
				builder3.setGold(target.getGold());
				builder3.setHead(target.getHead());
				builder3.setLevel(target.getLevel());
				builder3.setSeat(gameRole.getSeat());
				builder3.setAvatarId(gameRole.getAvatarId());
				builder3.setSex(target.getSex());
				ownerBuilder.addFightOwnerList(builder3);
			}
		}
		
		GMsg_12025010.Builder builder2 = GMsg_12025010.newBuilder();
		builder2.setOwner(ownerBuilder);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(), builder2.build());
		
		// 日志记录
		roomLogFunction.dealRoomRoleActiveLog(member.getRole().getRole(), table.getGame().getGameType(), table.getGame().getRoomType());
	}

	/**
	 * 开始游戏
	 */
	public void startTable(Game game) {
		LogUtil.info("游戏 roomType " + game.getRoomType());
		if (game.getStatus() == GameStatus.RUNNING) {
			return;
		}	
		
		DouniuTable table = getTable(game.getRoomId());
        if (table.getFightOwner().size() == 0 || table.getPlayers().size() == 0) {
        	return;
		}
		// 开始游戏
		game.setStatus(GameStatus.RUNNING);
		for (Map.Entry<Long, GameRole> entry : game.getSpriteMap().entrySet()) {
			GameRole role = entry.getValue();
			role.setStatus(PlayerState.PS_PREPARE_VALUE);
			
		}

		table.reset();

		NiuniuRoomCsv niuniuRoomCsv = niuniuRoomCache.getConfig(table.getGame().getRoomType());

		AtomicIntegerArray roomCountLog = getRoomCountCacheByRoomType(game
				.getRoomType());
		for (Map.Entry<Long,DouniuRole> entry : table.getMembers().entrySet()) {
			DouniuRole member = entry.getValue();

			Role role = member.getRole().getRole();
			if (role != null) {
				role.setBureauCount(role.getBureauCount() + 1);
				role.markToUpdate("bureauCount");
			}
		}
		roomCountLog.incrementAndGet(0);// 对战数

		game.setCount(game.getCount() + 1);

		
		tableToWait(table, HandleType.DOUNIU_RESULT, System.currentTimeMillis()
				+ niuniuRoomCsv.getBetCountDown() * 1000);
		GMsg_12025002.Builder builder = GMsg_12025002.newBuilder();
		builder.setWaitTime((int) (table.getCoolDownTime() / 1000));
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());
		
		// 日志 更新匹配时间
		for(Long rid:game.getRoles()){
			roomLogFunction.updateRoleMatchTime(rid);
		}
	}

	/**
	 * 寻找斗牛房间列表
	 * 
	 * @param role
	 */
	public Game findDouniuRooms(int level, Long oldId) {
		List<Long> curGamelist = roomFunction.listDouniuRooms(level);
		List<Long> list1 = new ArrayList<Long>();	// 下注人未满,有王
		List<Long> list2 = new ArrayList<Long>();	// 下注人未满,无王
		List<Long> list3 = new ArrayList<Long>();	// 下注人已满
		for (Long id : curGamelist) {
			if (oldId.equals(id)) {
				continue;
			}
			DouniuTable table = getTable(id);
			NiuniuRoomCsv csv = niuniuRoomCache.getConfig(table.getGame().getRoomType());

			if (table.getPlayers().size() < csv.getPlayerMax()) {
				if (table.getFightOwner().size() > 0) {
					list1.add(id);
				} else {
					list2.add(id);
				}
			} else {
				list3.add(id);
			}
		}
		if (list1.size() > 0) {
			return roomFunction.getGame(list1.get(MathUtil.randomNumber(0,
					list1.size() - 1)));
		}
		if (list2.size() > 0) {
			return roomFunction.getGame(list2.get(MathUtil.randomNumber(0,
					list2.size() - 1)));
		}
		if (list3.size() > 0) {
			return roomFunction.getGame(list3.get(MathUtil.randomNumber(0,
					list3.size() - 1)));
		}

		return roomFunction.getGame(oldId);
	}

	/**
	 * 参加桌子
	 *
	 */
	public void enterTable(final Game game, final Role role) {
		DouniuTable table = getTable(game.getRoomId());
		
		if (game.getStatus() == GameStatus.RUNNING && table == null) {
			game.setStatus(GameStatus.NO_INIT);
			game.getRoles().clear();
			game.getSpriteMap().clear();
			game.setStartTime(System.currentTimeMillis());
			table = initTable(game);
			LogUtil.error("table null" + game.getRoomId() + role.getNick());
		}
		if (table == null) {
			LogUtil.error(game.getRoles() + " table null" + game.getRoomId()
					+ role.getNick());
			game.setStatus(GameStatus.CLEAR);
			return;

		}
	
		//初次进入要建立角色
		if (getRole(role.getRid()) == null) {
			// 把玩家添加到映射表中
			roomFunction.getRoleGameMap().put(role.getRid(), table.getGame().getRoomId());
		
			// 初始化桌对象 查询空座位坐下
			GameRole gameRole = game.findEnterSeat(role);
			// 新的桌面对象
			DouniuRole member = new DouniuRole(gameRole);
			addRoleCache(member);
			gameRole.setStatus(PlayerState.PS_WATCH_VALUE);
			// 标记为旁观者 机器人进来就想当庄家
			if(member.getRole().isRobot()){
				// 设置机器人每周以及生涯的胜场数，总场数
				Npc npc = npcFunction.getNpcById(game.getGameType(), game.getRoomType(), gameRole.getRole().getRid());
				gameRole.setWinTotal(npc.getWinTotal());
				gameRole.setWinWeek(npc.getWinWeek());
				gameRole.setCountTotal(npc.getCountTotal());
				gameRole.setCountWeek(npc.getCountWeek());
				gameRole.setDiamond(role.getDiamond());
				
				applyOwner(table, member);
			}else{
				member.setPlayerType(0);
			}		

			table.getMembers().put(gameRole.getRole().getRid(), member);
			
			if(table.getMembers().size() == 1){
				game.setStartTime(System.currentTimeMillis());
			}
		
			// 其他人收到进入房间
			GMsg_12006008.Builder msg = GMsg_12006008.newBuilder();
			GGameRole.Builder builder3 = GGameRole.newBuilder();

			builder3.setRid(role.getRid());
			builder3.setNick(role.getNick());
			builder3.setGold(role.getGold());
			builder3.setHead(role.getHead());
			builder3.setLevel(role.getLevel());
			builder3.setSeat(gameRole.getSeat());
			builder3.setAvatarId(gameRole.getAvatarId());
			builder3.setSex(role.getSex());
			msg.setRoleInfo(builder3.build());
			List<Long> otherList = new ArrayList<Long>();
			otherList.addAll(game.getRoles());
			otherList.remove(role.getRid());
			roleFunction.sendMessageToPlayers(otherList, msg.build());
			LogUtil.info("enterTable getGameType" + otherList);
		}else{
			// 标记为旁观者
			DouniuRole member = getRole(role.getRid());
			member.setPlayerType(0);
		}
		
		role.setLastGameType(game.getGameType());
		role.setLastRoomType(game.getRoomType());

		GMsg_12025007.Builder builder = GMsg_12025007.newBuilder();
		GGameInfo.Builder gameBuilder = GGameInfo.newBuilder();
		gameBuilder.setGameType(table.getGame().getGameType());
		gameBuilder.setRoomId(table.getGame().getRoomId());
		gameBuilder.setRoomType(table.getGame().getRoomType());
		for (Map.Entry<Long, DouniuRole> entry : table.getMembers().entrySet()) {
			GGameRole.Builder builder3 = GGameRole.newBuilder();
			Role target = entry.getValue().getRole().getRole();
			builder3.setRid(entry.getKey());
			builder3.setNick(target.getNick());
			builder3.setGold(target.getGold());
			builder3.setHead(target.getHead());
			builder3.setLevel(target.getLevel());
			builder3.setSeat(entry.getValue().getRole().getSeat());
			builder3.setAvatarId(entry.getValue().getRole().getAvatarId());
			builder3.setSex(target.getSex());
			
			gameBuilder.addSprites(builder3.build());

			
		}
		builder.setGame(gameBuilder);
	
		
		GDouniuTable.Builder tableBuilder = GDouniuTable.newBuilder();
		if(table.getGame().getStatus()==GameStatus.WAIT_READY){
			tableBuilder.setStatus(0);
		}else if(table.getGame().getStatus()==GameStatus.RUNNING){
			tableBuilder.setStatus(1);
			
		}else if(table.getGame().getStatus()==GameStatus.END_REWARD){
			tableBuilder.setStatus(2);
			tableBuilder.setGold(0);		
		}
		GDouniuOwner.Builder ownerBuilder = GDouniuOwner.newBuilder();
		ownerBuilder.setFightOwner(table.getFightOwner().size());
		ownerBuilder.setHandPai(table.getOwnerPai());
		ownerBuilder.setHp(table.getOwnerHp());
		ownerBuilder.setStatus(1);
		ownerBuilder.setWaitOwner(table.getWaitOwner().size());
		
		for (Long id : table.getFightOwner()) {
			GGameRole.Builder builder3 = GGameRole.newBuilder();
			DouniuRole douniuRole = table.getMembers().get(id);
			if (douniuRole != null) {
				GameRole gameRole = douniuRole.getRole();
				Role target = gameRole.getRole();
				builder3.setRid(target.getRid());
				builder3.setNick(target.getNick());
				builder3.setGold(target.getGold());
				builder3.setHead(target.getHead());
				builder3.setLevel(target.getLevel());
				builder3.setSeat(gameRole.getSeat());
				builder3.setAvatarId(gameRole.getAvatarId());
				builder3.setSex(target.getSex());
				ownerBuilder.addFightOwnerList(builder3);
			}
		}
		
		tableBuilder.setOwner(ownerBuilder);
		
		DouniuRole member = getRole(role.getRid());
		tableBuilder.setPlayerType(member.getPlayerType());

		tableBuilder.addAllRecord(table.getRecords());
		
		for(DouniuXian douniuXian:table.getXians()){
			GDouniuXian.Builder xianBuilder = GDouniuXian.newBuilder();
			xianBuilder.addAllChips(douniuXian.getChips());
			xianBuilder.setHandPai(douniuXian.getPai());
			xianBuilder.setIndex(douniuXian.getIndex());
			
			xianBuilder.setTotalGold(douniuXian.getTotalGold());
			
			xianBuilder.setGold(member.getWinGold());
			xianBuilder.setPower(member.getWinPower());

			Integer bet = member.getChips().get(douniuXian.getIndex());
			if (bet == null) {
				xianBuilder.setSelfGold(0);
			} else {
				xianBuilder.setSelfGold(bet);
			}
			
			tableBuilder.addXian(xianBuilder);
		}
		tableBuilder.setWaitTime((int)(table.getCoolDownTime()/1000));

		builder.setOpenInfo(tableBuilder);
		
	    roleFunction.sendMessageToPlayer(role.getRid(), builder.build());
	}

	/**
	 * 初始化麻将牌局
	 *
	 * @param owner
	 * @return
	 */
	public DouniuTable initTable(Game game) {
		if (game.getStatus() > 0) {
			return null;
		}
		DouniuTable table = new DouniuTable(game);
		// mahJiangTable.setOwner(owner.getId());

		for (Map.Entry<Long, GameRole> entry : game.getSpriteMap().entrySet()) {
			DouniuRole member = new DouniuRole(entry.getValue());
			table.getMembers().put(entry.getKey(),member);
			addRoleCache(member);
		}

		tableMap.put(game.getRoomId(), table);
		
		table.setLastNoMemberTime(System.currentTimeMillis());

		//
		game.setStatus(GameStatus.WAIT_READY);

		return table;
	}

	/**
	 * 初始化翻金花牌
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
	public void tableToWait(DouniuTable table,int hanlerType, long coolDownTime) {
		table.setQueueWaitType(hanlerType);
		table.setCoolDownTime(coolDownTime);
	}



	/**
	 * 流程处理
	 */
	public void autoAction() {
		for (Map.Entry<Long, DouniuTable> entry : tableMap.entrySet()) {
			DouniuTable table = entry.getValue();
			if (table.getQueueWaitType() == 0) {
				continue;
			}
			manager.executeTask(new SingleThreadTask(table) {
				@Override
				public void doTask(ISingleData singleData) {
					DouniuTable table = (DouniuTable) singleData;

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
						Event event = new Event(tableState, table);
						DispatchEvent.dispacthEvent(event);
					} catch (Exception e) {
						//table.setQueueWaitType(tableState);
						e.printStackTrace();
						LogUtil.error("斗牛流程处理异常", e);
					}
				}
			});
		}
	}





	


	
	/**
	 * 结算
	 * 
	 * @param table
	 */
	public void endTable(DouniuTable table) {
		if (table.getFightOwner().size() == 0) {
			table.getGame().setStatus(GameStatus.WAIT_READY);
			return;
		}
		table.setQueueWaitType(0);
		table.getGame().setStatus(GameStatus.END);
		table.getGame().setEndTime(System.currentTimeMillis());
		
		LogUtil.info(table.getGame().getRoomId() + " end");
		
		
		NiuniuRoomCsv niuniuRoomCsv = niuniuRoomCache.getConfig(table.getGame().getRoomType());
		
		AtomicIntegerArray roomCountLog = getRoomCountCacheByRoomType(table.getGame()
				.getRoomType());	
		// G_T_C 得到该角色的对局信息
		List<Object[]> roleInfos = new ArrayList<>();
		int aiGold = 0;
		Long winRid = 0l;
		
		int totalPlayerCount = 0;
		int realPlayerCount = 0;

		//得到各家牌型
		List<Integer> pais = initAllPai();
		// 洗牌 打乱顺序
		Collections.shuffle(pais);
		
		
		// 庄家牌
		List<Integer> zhuangPai = new ArrayList<Integer>();
		for (int i = 0; i < 5; i++) {
			//zhuangPai.add(pais.remove(0));
			zhuangPai.add(13);
		}
		table.faPai(zhuangPai);
		
		if (table.getRecords().size() >= 20) {
			table.getRecords().remove(0);
		}
		GDouniuRecord.Builder rbuilder = GDouniuRecord.newBuilder();

		// 闲家牌
		for (DouniuXian douniuXian : table.getXians()) {
			List<Integer> pai = new ArrayList<Integer>();
			for (int i = 0; i < 5; i++) {
				pai.add(pais.remove(0));
			}
			douniuXian.faPai(pai);
			boolean flag = DouniuRule.getWhoBig(table.getOwnerPai(), douniuXian.getPai());
			int winPower = 0;
			if (flag) {
				winPower = niuniuMultipleCache.getConfig(
						table.getOwnerPai().getPaiType().getNumber())
						.getCardTypeMultiple();
			} else {
				winPower = niuniuMultipleCache.getConfig(
						douniuXian.getPai().getPaiType().getNumber())
						.getCardTypeMultiple();
			}
			if (flag) {
				douniuXian.setWinPower(-winPower);
				rbuilder.addArray(0);

			} else {
				douniuXian.setWinPower(winPower);
				rbuilder.addArray(1);
			}

		}
		table.getRecords().add(rbuilder.build());

		int zhuangGold = 0;
		
		totalPlayerCount = table.getPlayers().size() + table.getFightOwner().size();
		
		for (Map.Entry<Long,DouniuRole> entry : table.getMembers().entrySet()) {
			DouniuRole member = entry.getValue();			
			if (member.getPlayerType() != 2) {
				continue;
			}
			
			if(!member.getRole().isRobot()){
				++realPlayerCount;
			}
				
			for (DouniuXian douniuXian : table.getXians()) {
				Integer bet = member.getChips().get(douniuXian.getIndex());
				if (bet != null) {
					// 判断输赢
					int winGold = bet * douniuXian.getWinPower();
					member.setWinGold(member.getWinGold() + winGold);
					member.setWinPower(member.getWinPower() + douniuXian.getWinPower());
					zhuangGold -= winGold;
				}
			}

			if (member.getWinGold() == 0) {
				member.setNoGameCount(member.getNoGameCount() + 1);
				if (member.getNoGameCount() >= niuniuRoomCsv.getPleaseLeave()) {
					member.setPlayerType(0);
					table.getPlayers().remove(member.getRid());
					member.setNoGameCount(0);
					GMsg_12025006.Builder noGameBuilder = GMsg_12025006.newBuilder();
					roleFunction.sendMessageToPlayer(member.getRid(), noGameBuilder.build());
					LogUtil.info("getNoGameCount");
				}
				continue;
			} else {
				member.setNoGameCount(0);
			}
	
			int tax = member.getWinGold() > 0 ? niuniuRoomCsv.getFreeTaxPerGame() : niuniuRoomCsv.getTaxPerGame();
					
			Float gold = Math.abs(member.getWinGold() * (tax / 10000f));
			tax = gold.intValue();
			
			Role role = member.getRole().getRole();
			int beforeValue = role.getGold();

			member.setWinGold(member.getWinGold() - tax);
			
			if(member.getRole().isRobot()){
				roomCountLog.addAndGet(2, tax);
				// 机器人不管输赢，直接加
				role.setGold(role.getGold() + member.getWinGold());
			} else {
				
				ExpCsv expCsv = expCache.getConfig(role.getLevel());		
				
				if (member.getWinGold() > 0) {
					roomCountLog.addAndGet(2, tax);
					roleFunction.expAdd(role, expCsv.getNiuniuWinExp(), true);
					roleFunction.goldAdd(role, member.getWinGold(), MoneyEvent.NIUNIU, false);			
				
					// 赢家日志记录
					douniuDataFunction.updateDouniuData(role.getRid(), member.getWinGold(), true);
				
				} else if (member.getWinGold() < 0) {
					if (roleFunction.isPoChan(role.getGold(), role.getGoldPot(),-member.getWinGold())) {
						AtomicIntegerArray log = getRoomCountCacheByRoomType(table.getGame().getRoomType());
						log.incrementAndGet(1);// 破产数
						if(member.getPlayerType() == 1){
							if(niuniuRoomCsv.getRoomLv() == 1){
								log.incrementAndGet(3);//普通庄家破产数
							}else{
								log.incrementAndGet(4);//困难庄家破产数
							}
						}else{
							if(niuniuRoomCsv.getRoomLv() == 1){
								log.incrementAndGet(5);//普通闲家破产数
							}else{
								log.incrementAndGet(6);//困难闲家破产数
							}
						}
						
					}	
					
					roleFunction.expAdd(role, expCsv.getNiuniuLoseExp(), true);
					roleFunction.goldSub(role, -member.getWinGold(), MoneyEvent.NIUNIU, false);	
				
					// 输家日志记录
					douniuDataFunction.updateDouniuData(role.getRid(), 0, false);
				}
				
				// 抽税
				roleFunction.goldSub(role, tax, MoneyEvent.NIUNIU_TAX, false);
				roleFunction.addMoneyEvent(MoneyEvent.NIUNIU_TAX, role.getRid(), MoneyType.Gold, -tax, beforeValue, role.getGold());					
			}
		}
		
		int tax = 0;
		if (zhuangGold > 0) {
			tax = niuniuRoomCsv.getBankerTaxPerGame();
		} else {
			tax = niuniuRoomCsv.getTaxPerGame();
		}
		
		int perGold = zhuangGold / table.getFightOwner().size();
		Float gold = Math.abs(perGold * (tax / 10000f));
		tax = gold.intValue();

		perGold -= tax;
		for (Long id : table.getFightOwner()) {
			DouniuRole member = table.getMembers().get(id);
			member.setWinGold(perGold);
			Role role = member.getRole().getRole();
			int beforeValue = role.getGold();
			int isAi = 0;
			if (member.getRole().isRobot()) {
				role.setGold(role.getGold() + perGold);
				isAi = 1;
			} else {
				++realPlayerCount;
				
				ExpCsv expCsv = expCache.getConfig(role.getLevel());
				if (zhuangGold > 0) {
					roleFunction.goldAdd(role, perGold, MoneyEvent.NIUNIU, false);
					roleFunction.expAdd(role, expCsv.getNiuniuWinExp(), true);
					
					// 赢家日志记录
					douniuDataFunction.updateDouniuData(role.getRid(), zhuangGold, true);
				} else {
					roleFunction.goldSub(role, -perGold, MoneyEvent.NIUNIU, false);
					roleFunction.expAdd(role, expCsv.getNiuniuLoseExp(), true);
					
					// 输家日志记录
					douniuDataFunction.updateDouniuData(role.getRid(), 0, false);
				}
				
				// 抽税
				roleFunction.goldSub(role, tax, MoneyEvent.NIUNIU_TAX, false);
				roleFunction.addMoneyEvent(MoneyEvent.NIUNIU_TAX, role.getRid(), MoneyType.Gold, -tax, beforeValue, role.getGold());		
			}
			
			// G_T_C 日志插入
			DouniuWangLog wangLog = douniuWangLogDao.getDouniuWangLog(table.getGame(), niuniuRoomCsv.getRoomLv(), niuniuRoomCsv.getRoomType(),role.getRid(),isAi);
			douniuWangLogDao.addLog(wangLog);
		}
		
		int leftHp = table.getOwnerHp() + zhuangGold * 4;
		int curHp = leftHp > niuniuRoomCsv.getHp() ? niuniuRoomCsv.getHp() : leftHp;
		table.setOwnerHp(curHp);
		
		System.out.println(table.getOwnerHp()+"zhuangGold"+zhuangGold);
		if (table.getOwnerHp() <= 0) {
			table.setOwnerHp(0);
			for(Long id:table.getFightOwner()){
				DouniuRole member = table.getMembers().get(id);
				if (member != null) {
					member.setPlayerType(0);
				}	
			}
			table.getFightOwner().clear();
			if (table.getWaitOwner().size() > 0) {
				for (int i = 0; i < niuniuRoomCsv.getOwnerMax()
						&& i < table.getWaitOwner().size(); i++) {
					Long id = table.getWaitOwner().remove(0);
					DouniuRole member = table.getMembers().get(id);
					if (member != null) {
						table.getFightOwner().add(id);
						member.setPlayerType(1);
						table.getPlayers().remove(id);
					}	
				}
			}
		}
		
		//发送输赢消息
		for (Map.Entry<Long,DouniuRole> entry : table.getMembers().entrySet()) {
			DouniuRole member = entry.getValue();
			
			GDouniuEnd.Builder value = GDouniuEnd.newBuilder();
			value.setExp(0);
			value.setGold(member.getWinGold());
			int totalChip = 0;
			for(DouniuXian douniuXian:table.getXians()){
				GDouniuXian.Builder xianBuilder = GDouniuXian.newBuilder();
				xianBuilder.addAllChips(douniuXian.getChips());
				xianBuilder.setHandPai(douniuXian.getPai());
				xianBuilder.setIndex(douniuXian.getIndex());
				xianBuilder.setResult(douniuXian.getWinPower() > 0 ? 1 : 0);
				xianBuilder.setTotalGold(douniuXian.getTotalGold());
				
				if (member.getPlayerType() == 1) {
					// 庄家
					xianBuilder.setSelfGold(0);
					xianBuilder.setGold((int) (-douniuXian.getWinPower() * douniuXian.getTotalGold()));
					xianBuilder.setPower(-douniuXian.getWinPower());
				} else {
					Integer bet = member.getChips().get(douniuXian.getIndex());
					if (bet == null) {
						xianBuilder.setSelfGold(0);
						xianBuilder.setGold(0);
						xianBuilder.setPower(0);
					} else {
						xianBuilder.setSelfGold(bet);
						xianBuilder.setGold(douniuXian.getWinPower() * bet);
						xianBuilder.setPower(douniuXian.getWinPower());
						
						totalChip += bet;
					}
				}

				value.addXian(xianBuilder);
			}
			//公共部分
			GMsg_12025004.Builder builder = GMsg_12025004.newBuilder();
			GDouniuOwner.Builder ownerBuilder = GDouniuOwner.newBuilder();
			ownerBuilder.setFightOwner(table.getFightOwner().size());
			ownerBuilder.setHandPai(table.getOwnerPai());
			ownerBuilder.setHp(table.getOwnerHp());
			ownerBuilder.setStatus(1);
			ownerBuilder.setWaitOwner(table.getWaitOwner().size());
			
			for (Long id : table.getFightOwner()) {
				GGameRole.Builder builder3 = GGameRole.newBuilder();
				DouniuRole douniuRole = table.getMembers().get(id);
				if (douniuRole != null) {
					GameRole gameRole = douniuRole.getRole();
					Role target = gameRole.getRole();
					builder3.setRid(target.getRid());
					builder3.setNick(target.getNick());
					builder3.setGold(target.getGold());
					builder3.setHead(target.getHead());
					builder3.setLevel(target.getLevel());
					builder3.setSeat(gameRole.getSeat());
					builder3.setAvatarId(gameRole.getAvatarId());
					builder3.setSex(target.getSex());
					ownerBuilder.addFightOwnerList(builder3);
				}
			}

			value.setOwner(ownerBuilder);
			builder.setEndInfo(value);
			roleFunction.sendMessageToPlayer(entry.getKey(), builder.build());
			
			table.getGame().setStatus(GameStatus.END_REWARD);
			//value.clear();		
			
			// 牌型
			missionFunction.checkTaskFinish(member.getRid(),TaskType.daily_task,
					MissionType.CARD_TYPE, GameType.DOUNIU,member.getPlayerType());
			// 胜
			missionFunction.checkTaskFinish(member.getRid(),TaskType.daily_task, MissionType.WIN,
					GameType.DOUNIU);
			// 连胜
			missionFunction.checkTaskFinish(member.getRid(),TaskType.daily_task,
					MissionType.CONTINUE_WIN, GameType.DOUNIU, true);

			// 任务检测
			missionFunction.checkTaskFinish(member.getRid(),TaskType.daily_task, MissionType.TIMES,
					GameType.DOUNIU);

			// G_T_C add 日志处理
			GameRole gRole = member.getRole();
			Role role = gRole.getRole();
			Object[] objs = douniuLogDao.getRoleInfo(role.getRid(),
					role.getNick(), gRole.getSeat(),
					gRole.isRobot(), totalChip,
					member.getWinGold(),
					null, member.getPlayerType() == 1);
			roleInfos.add(objs);
			
			Game game = table.getGame();
			if (!member.getRole().isRobot()) {
				roomLogFunction.dealRoomRoleLoginLog(game.getGameType(), role,
						role.getLastLoginIp(), 1);
			}

		}

		// G_T_C 日志插入
		String roleInfo = StringUtil.listArrayToString(roleInfos,
				StringUtil.DELIMITER_BETWEEN_ITEMS,
				StringUtil.DELIMITER_INNER_ITEM);
		LogUtil.debug(roleInfo);
		DouniuLog log = douniuLogDao.getDouniuLog(table.getGame(), niuniuRoomCsv.getRoomLv(), niuniuRoomCsv.getRoomType(),roleInfo,
				winRid + "", aiGold,totalPlayerCount,realPlayerCount);
		douniuLogDao.addLog(log);
		
		// 处理离开/掉线的人
		for (Map.Entry<Long,DouniuRole> entry : table.getMembers().entrySet()) {
			DouniuRole douniuRole = entry.getValue();
			if (douniuRole.getRole().getStatus() == PlayerState.PS_EXIT_VALUE) {
				removeMember(table, douniuRole.getRole());
			}
		}
		
		tableToWait(table,HandleType.DOUNIU_START, System.currentTimeMillis() + niuniuRoomCsv.getOpeningCountDown()*1000);
		GMsg_12025005.Builder startBulid = GMsg_12025005.newBuilder();
		startBulid.setWaitTime((int)(table.getCoolDownTime()/1000));
		roleFunction.sendMessageToPlayers(table.getGame()
				.getRoles(), startBulid.build());
	}

	/**
	 * 根据房间号码拿到统计实体。由于出现并发获取，需要同步
	 * 
	 * @author G_T_C
	 * @param roomType
	 * @return
	 */
	private AtomicIntegerArray getRoomCountCacheByRoomType(int roomType) {
		AtomicIntegerArray log = roomCountMap.get(roomType);
		return log;
	}

	public Map<Integer, AtomicIntegerArray> getRoomCountCache() {
		return roomCountMap;
	}
}
