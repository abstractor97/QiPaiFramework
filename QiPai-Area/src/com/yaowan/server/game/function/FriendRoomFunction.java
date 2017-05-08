package com.yaowan.server.game.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.FriendRoomPayType;
import com.yaowan.constant.GameStatus;
import com.yaowan.constant.GameType;
import com.yaowan.constant.ItemEvent;
import com.yaowan.constant.MoneyEvent;
import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.csv.cache.FriendRoomCache;
import com.yaowan.csv.entity.FriendRoomCsv;
import com.yaowan.framework.core.GlobalVar;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.DispatchEvent;
import com.yaowan.framework.core.thread.ISingleData;
import com.yaowan.framework.core.thread.SingleThreadManager;
import com.yaowan.framework.core.thread.SingleThreadTask;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.GameRole;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GFriendRoom.GFClearRoomInfo;
import com.yaowan.protobuf.game.GFriendRoom.GFEndInfo;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_12020001;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_12020002;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_12020003;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_12020004;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_12020006;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_12020007;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_12020008;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_12020009;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_12020010;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_12020013;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_12020014;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_12020017;
import com.yaowan.protobuf.game.GFriendRoom.Gcsv;
import com.yaowan.protobuf.game.GGame.GGameInfo;
import com.yaowan.protobuf.game.GGame.GGameRole;
import com.yaowan.protobuf.game.GGame.GMsg_12006008;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.model.data.dao.FriendRoomDao;
import com.yaowan.server.game.model.data.entity.FriendRoom;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.dao.FriendRoomLogDao;
import com.yaowan.server.game.model.log.entity.FriendRoomLog;
import com.yaowan.server.game.model.struct.ZTDoudizhuRole;
import com.yaowan.server.game.model.struct.ZTDoudizhuTable;
import com.yaowan.server.game.model.struct.ZTMaJongTable;
import com.yaowan.server.game.model.struct.ZTMajiangRole;


@Component
public class FriendRoomFunction extends FunctionAdapter {
	
	@Autowired
	private GlobalVar globalVar;
	
	@Autowired
	private RoleFunction roleFunction;
	
	@Autowired
	private RoomFunction roomFunction;
	
	@Autowired
	private SingleThreadManager singleThreadManager;
	
	@Autowired
	private ZTDoudizhuFunction ztDoudizhuFunction;
	
	@Autowired
	private ZTMajiangFunction ztMajiangFunction;
	
	@Autowired
	private SingleThreadManager manager;
	
	@Autowired
	private ZXMajiangFunction zxMajiangFunction;
	
	@Autowired
	private ItemFunction itemFunction;
	
	@Autowired
	private FriendRoomLogDao friendRoomLogDao;
	
	@Autowired
	private FriendRoomDao friendRoomDao;
	
	/**
	 * 玩家游戏关系缓存<rid, matchId>
	 */
	private ConcurrentHashMap<Long, Long> roleGameMap = new ConcurrentHashMap<>();
	
	/**
	 * 游戏缓存<roomId, Game>
	 */
	private ConcurrentHashMap<Long, Game> runningGameMap = new ConcurrentHashMap<>();
	
	/**
	 *好友房缓存
	 */
	private ConcurrentHashMap<Long, FriendRoom> friendRoomMap = new ConcurrentHashMap<>();
	
	@Autowired
	private FriendRoomCache friendRoomCache;
	
	private static AtomicBoolean IS_MATCHING = new AtomicBoolean(false);
	
	private static AtomicBoolean IS_EXIT = new AtomicBoolean(false);
	
	
	public ConcurrentHashMap<Long, Long> getRoleGameMap(){
		return roleGameMap;
		
	}
	
 	public int getRealType(int gameType, int roomType) {
		return gameType * 10000000 + roomType;
	}
	
	public int getGameType(int realType) {
		int roomType = realType % 10000000;
		int gameType = (realType - roomType) / 10000000;
		return gameType;
	}
	
	public int getGameMinRole(int gameType) {
		int num = 4;
		if (gameType == GameType.MENJI) {
			num = 2;
		} else if (gameType == GameType.DOUDIZHU) {
			num = 3;
		} else if (gameType == GameType.MAJIANG) {
			num = 4;
		}
		return num;
	}
	
	//新建游戏对象
	public Game newGame(int id,int realType,int friendRoomBaseChip) {
		Game game = new Game(id, realType);
		game.setFriendRoom(true);
		game.setNeedCount(getGameMinRole(game.getGameType()));
		return game;
	}
	
	//根据房间号获得游戏
	public Game getGame(long roomId){
		Game game = runningGameMap.get(roomId);
		return game;
	}
	
	//根据房间号获得好友房
	public FriendRoom getFriendRoom(long roomId){
		FriendRoom friendRoom = friendRoomMap.get(roomId);
		return friendRoom;
	}
	
	public ConcurrentHashMap<Long, FriendRoom> getFriendRoomMap(){
		return friendRoomMap;
	}
	
	/**
	 *根据房间号删除好友房
	 */
	public void removeFriendRoom(long roomId){
		friendRoomMap.remove(roomId);
		friendRoomDao.deleteFriendRoom(roomId);
	}
	
	public long getRoomIdByRid(long rid){
		if(roleGameMap == null){
			return -1;
		}else{
			if(roleGameMap.containsKey(rid)){
				return roleGameMap.get(rid);
			}else{
				return -1;
			}
		}
		
	}
	
	//判断用户有没有在游戏内
	public boolean inGameByrid(long rid){
		if(roleGameMap.containsKey(rid)){
			return true;
		}
		return false;
	}
	
	
	//新建好友房对象
	public FriendRoom newFriendRoom(int baseChip,int gameType,int id,long owner,int round,
			int highestPower,int initScore,int cardNum,int cardId,int payType,int roomType){
		FriendRoom friendRoom = new FriendRoom();
		friendRoom.setBaseChip(baseChip);
		friendRoom.setGameType(gameType);
		friendRoom.setId(id);
		friendRoom.setOwner(owner);
		friendRoom.setAllRound(1);
		friendRoom.setNowRound(1);
		friendRoom.setStartTime(System.currentTimeMillis());
		friendRoom.setHighestPower(highestPower);
		friendRoom.setInitScore(initScore);
		friendRoom.setCardNum(cardNum);
		friendRoom.setCardId(cardId);
		friendRoom.setPayType(payType);
		friendRoom.setRoomType(roomType);
		friendRoom.setCreateTime(TimeUtil.time());
		friendRoom.setOpenId(roleFunction.getRoleByRid(owner).getOpenId());
		return friendRoom;
	}
	
	public void putFriendRoomMap(FriendRoom friendRoom){
		friendRoomMap.put(friendRoom.getId(), friendRoom);
		friendRoomDao.addFriendRoom(friendRoom);
	}
	
	public void updateFriendRoomMap(long rid,FriendRoom friendRoom){
		friendRoomMap.put(friendRoom.getId(), friendRoom);
		friendRoomDao.updateSpriteToString(friendRoom);
	}
	
	/**
	 * 用户是否在好友房
	 * @param player
	 */
	public void getRoleIsInfriendRoom(Player player){
		GMsg_12020007.Builder builder = GMsg_12020007.newBuilder();
		if(inGameByrid(player.getRole().getRid())){
			builder.setInRoom(1);
		}else{
			builder.setInRoom(0);
		}
		roleFunction.sendMessageToPlayer(player.getRole().getRid(), builder.build());
	}
	
	/**
	 * 加入房间
	 * @param role
	 * @param roomId
	 */
	public void joinInFriendRoom(Player player,long roomId){
		
		Game game = getGame(roomId);
		Role role = player.getRole();
		FriendRoom friendRoom = getFriendRoom(roomId);
		if(game == null || friendRoom == null){
			//好友房不存在
			if(game != null){
				runningGameMap.remove(roomId);
			}
			if(friendRoom != null){
				removeFriendRoom(roomId);
			}
			return;
		}
		GameRole gameRole;
		if(friendRoom.getOwner() == role.getRid()){
			gameRole = game.getSpriteMap().get(game.getRoomId());
			friendRoom.setOwnerExit((byte)0);
		}else{
			List<Integer> seatlist = new ArrayList<Integer>();
			for(GameRole gamerole1 : game.getSpriteMap().values()){
				seatlist.add(gamerole1.getSeat());
			}
			Collections.sort(seatlist);
			int seat = 1;
			if(seatlist.size() == 1){
				seat = seat + 1;
			}else{
				for (int i = 0; i < seatlist.size(); i++) {
					if(i+1 < seatlist.size()){
						if(seatlist.get(i+1) - seatlist.get(i) == 1){
							continue;
						}else{
							seat = seatlist.get(i) + 1;
							break;
						}
					}
				}
				if(seat == 1){
					seat = seatlist.get(seatlist.size() - 1) + 1;
				}
			}
			LogUtil.info("seat:"+seat);
			gameRole = new GameRole(role, game.getRoomId());
			gameRole.setSeat(seat);
			game.getRoles().add(role.getRid());
			game.getSpriteMap().put(role.getRid(), gameRole);
			// 把玩家添加到比赛映射表中
			roleGameMap.put(role.getRid(), game.getRoomId());
		}
		GGameInfo.Builder builder = GGameInfo.newBuilder();
		builder.setGameType(game.getGameType());
		builder.setRoomId(game.getRoomId());
		builder.setRoomType(game.getRoomType());
		
		//是否人满
		int isFull = 0;
		GGameRole.Builder item = GGameRole.newBuilder();
		friendRoom.setStartTime(System.currentTimeMillis());
		friendRoom.getSpriteMap().put(role.getRid(), friendRoom.getInitScore());
		updateFriendRoomMap(role.getRid(), friendRoom);
		if(game.getNeedCount() <= game.getSpriteMap().size() && friendRoom.isOwnerExit() == FriendRoomPayType.NO_EXIT){
			friendRoom.setRoundCreateTime(TimeUtil.time());
			Event event = new Event(HandleType.GAME_INIT, game);
			DispatchEvent.dispacthEvent(event);
			isFull = 1;
			GMsg_12020010.Builder builder2 = GMsg_12020010.newBuilder();
			builder2.setIsFull(1);
			roleFunction.sendMessageToPlayers(game.getSpriteMap().keySet(), builder2.build());
		}
		List<Long> rids = new ArrayList<Long>();
		for (Map.Entry<Long, GameRole> entry : game.getSpriteMap()
				.entrySet()) {
			GGameRole.Builder gameRole1 = GGameRole.newBuilder();
			Role target = entry.getValue().getRole();
			gameRole1.setRid(entry.getKey());
			gameRole1.setNick(target.getNick());
			gameRole1.setGold(friendRoom.getSpriteMap().get(entry.getKey()));
			gameRole1.setHead(target.getHead());
			gameRole1.setLevel(target.getLevel());
			gameRole1.setSeat(entry.getValue().getSeat());
			gameRole1.setAvatarId(entry.getValue().getAvatarId());
			gameRole1.setSex(target.getSex());
			gameRole1.setIsOnline(1);
			if(entry.getKey() == friendRoom.getOwner()){
				if(friendRoom.isOwnerExit() == FriendRoomPayType.NO_EXIT){
					gameRole1.setIsOnline(1);
				}else{
					gameRole1.setIsOnline(0);
				}
				
			}
			if(friendRoom.getLoginOutList() != null
					&& friendRoom.getLoginOutList().contains(entry.getKey())
					&& entry.getKey() != role.getRid()){
				gameRole1.setIsOnline(0);
			}
			builder.addSprites(gameRole1);
			
			if(entry.getKey() == role.getRid()){
				item = gameRole1;
			}else{
				rids.add(entry.getKey());
			}
		}
		
		
		GMsg_12020002.Builder builder1 = GMsg_12020002.newBuilder();
		
		if(friendRoom.isOwnerExit() == FriendRoomPayType.NO_EXIT){
			builder1.setOwnerExit(0);
		}else{
			rids.remove(friendRoom.getOwner());
			builder1.setOwnerExit(1);
		}
		builder1.setPower(friendRoom.getHighestPower());
		builder1.setIsFull(isFull);
		builder1.setGame(builder);
		builder1.setOwner(friendRoom.getOwner());
		builder1.setSuccess(1);
		builder1.setAllRound(friendRoom.getAllRound());
		builder1.setNowRound(1);
		
		roleFunction.sendMessageToPlayer(role.getRid(),
				builder1.build());
		
		GMsg_12006008.Builder builder2 = GMsg_12006008.newBuilder();
		builder2.setRoleInfo(item);
		if(friendRoom.isOwnerExit() == FriendRoomPayType.EXIT){
			rids.remove(friendRoom.getOwner());
		}
		roleFunction.sendMessageToPlayers(rids,
				builder2.build());
		
		
		
	}

	/**
	 * 创建房间
	 * @param role
	 * @param gameType 游戏类型
	 * @param nick 房间名称
	 * @param id 房间号
	 * @param num 消耗的卡片数量
	 */
	public void createFriendRoom(Role role, int gameType,int id,int round,int baseChip,
			int highestPower,int initScore,int cardNum,int cardId,int payType,int roomType) {
		int realType = getRealType(gameType, 0);
		Game game = newGame(id,realType,baseChip);
		GameRole gameRole = new GameRole(role, game.getRoomId());
		gameRole.setSeat(1);
		game.setFriendRoom(true);
		game.getRoles().add(role.getRid());
		game.getSpriteMap().put(role.getRid(), gameRole);
		FriendRoom friendRoom = newFriendRoom(baseChip,gameType,id,role.getRid(),round,highestPower,initScore,cardNum,cardId,payType,roomType);		
		//添加玩家积分
		friendRoom.getSpriteMap().put(role.getRid(), initScore);
		// 把玩家添加到比赛映射表中
		roleGameMap.put(role.getRid(), game.getRoomId());
		//添加游戏映射表
		runningGameMap.put(game.getRoomId(), game);
		//添加到好友房映射表
		putFriendRoomMap(friendRoom);
		GGameInfo.Builder builder2 = GGameInfo.newBuilder();
		
		builder2.setGameType(game.getGameType());
		builder2.setRoomId(game.getRoomId());
		builder2.setRoomType(game.getRoomType());
			GGameRole.Builder gameRole2 = GGameRole.newBuilder();
			Role target = gameRole.getRole();
			gameRole2.setRid(target.getRid());
			gameRole2.setNick(target.getNick());
			gameRole2.setGold(initScore);
			gameRole2.setHead(target.getHead());
			gameRole2.setLevel(target.getLevel());
			gameRole2.setSeat(gameRole.getSeat());
			gameRole2.setAvatarId(gameRole.getAvatarId());
			gameRole2.setSex(target.getSex());
			gameRole2.setIsOnline(1);
			builder2.addSprites(gameRole2);
			
			GMsg_12020001.Builder builder3 = GMsg_12020001.newBuilder();
			builder3.setPower(friendRoom.getHighestPower());
			builder3.setGame(builder2);
			builder3.setOwner(role.getRid());
			builder3.setSuccess(1);
			builder3.setAllRound(friendRoom.getAllRound());
			builder3.setNowRound(friendRoom.getNowRound());
		roleFunction.sendMessageToPlayer(role.getRid(),
				builder3.build());
	}
	
	/**
	 * 退出房间
	 */
	public void exitFriendRoom(Player player,long roomId){
		if(IS_EXIT.get()){
			return;
		}
		IS_EXIT.set(true);
		LogUtil.info("roomId:" + roomId);
		try {
			Role role = player.getRole();
			if(friendRoomMap.containsKey(roomId)){
				FriendRoom friendRoom = getFriendRoom(roomId);
				if(friendRoom.isStart() == FriendRoomPayType.START){
					return;
				}
				friendRoom.getPrepareList().remove(role.getRid());
				//退出房间
				Game game = getGame(roomId);
				List<Long> rids = new ArrayList<Long>();
				rids.addAll(game.getSpriteMap().keySet());
				if(friendRoom.isOwnerExit() == FriendRoomPayType.EXIT){
					rids.remove(friendRoom.getOwner());
				}
				GameRole gameRole = game.getSpriteMap().get(role.getRid());
				GMsg_12020003.Builder builder = GMsg_12020003.newBuilder();
				if(friendRoom.getOwner() != role.getRid()){
					LogUtil.error("普通玩家退出房间");
					roleGameMap.remove(role.getRid());
					game.getSpriteMap().remove(role.getRid());
					game.getRoles().remove(role.getRid());
					friendRoom.getSpriteMap().remove(role.getRid());
					updateFriendRoomMap(role.getRid(), friendRoom);
					if(friendRoom.getPayType() == FriendRoomPayType.DIAMOND){
						roleFunction.diamondAdd(role, friendRoom.getCardNum(), MoneyEvent.FRIENDROOM);
					}
				}else{
					LogUtil.error("房主退出房间");
					friendRoom.setOwnerExit((byte)1);
				}
				if(friendRoom.isOwnerExit() == FriendRoomPayType.EXIT){
					builder.setOwnerExit(1);
				}else{
					builder.setOwnerExit(0);
				}
				builder.setCurrentSeat(gameRole.getSeat());
				roleFunction.sendMessageToPlayers(rids, builder.build());
			}
		} finally{
			// TODO: handle exception
			IS_EXIT.set(false);
		}
		
	}
		
	/**
	 * 解散房间
	 * @param friendRoom
	 */
	public void clear(FriendRoom friendRoom,boolean isVote,boolean isPHP){
		
		if(friendRoom.isStart() == FriendRoomPayType.START){
			// 游戏开始，统计日志
			FriendRoomLog friendRoomLog = new FriendRoomLog();
			friendRoomLog.setDayTime(friendRoom.getCreateTime());
			friendRoomLog.setGameType(friendRoom.getGameType());
			friendRoomLog.setPayType(friendRoom.getPayType());
			friendRoomLog.setRound(friendRoom.getNowRound());
			friendRoomLog.setRoomType(friendRoom.getRoomType());
			switch (friendRoom.getPayType()) {
			case FriendRoomPayType.ROOMCARD:
				friendRoomLog.setConsumeNum(friendRoom.getCardNum());
				break;
			case FriendRoomPayType.DIAMOND:
				friendRoomLog.setConsumeNum(friendRoom.getCardNum() * friendRoom.getSpriteMap().size());
				break;

			default:
				break;
			}
			friendRoomLogDao.addMoney(friendRoomLog);
		}
		
		//从房间列表里面删除房间
		removeFriendRoom(friendRoom.getId());
		//删除游戏
		Game game = runningGameMap.remove(friendRoom.getId());
		//从牌局中移除游戏
		switch (game.getGameType()) {
		case GameType.DOUDIZHU:
			ztDoudizhuFunction.clear(friendRoom.getId());
			break;
			
		case GameType.MAJIANG:
			ztMajiangFunction.clear(friendRoom.getId());
			break;
			
		case GameType.ZXMAJIANG:
			zxMajiangFunction.clear(friendRoom.getId());
			break;

		default:
			break;
		}
		List<Long> rids = new ArrayList<Long>();
		rids.addAll(game.getSpriteMap().keySet());
		GMsg_12020006.Builder builder = GMsg_12020006.newBuilder();
		//从游戏中移除角色
		for ( Map.Entry<Long, GameRole> entry : game.getSpriteMap().entrySet()) {
			GFEndInfo.Builder endInfo = GFEndInfo.newBuilder();
			roleGameMap.remove(entry.getKey());
			game.getSpriteMap().entrySet().remove(entry.getKey());
			if(entry.getKey() == friendRoom.getOwner()){
				endInfo.setOwner(1);
			}else{
				endInfo.setOwner(0);
			}
			if(friendRoom.getSpriteMap() != null && friendRoom.getSpriteMap().size() > 0){
				endInfo.setScore(friendRoom.getSpriteMap().get(entry.getKey()) - friendRoom.getInitScore());
			}
			endInfo.setSeat(entry.getValue().getSeat());
			LogUtil.info("玩家座位号："+endInfo.getSeat());
			if(friendRoom.isStart() == FriendRoomPayType.START){
				builder.addEndInfo(endInfo);
			}
			
			if(isVote){
				builder.setIsVote(1);
			}else{
				builder.setIsVote(0);
			}
		}
		if(friendRoom.isStart() == FriendRoomPayType.START || isPHP){
			roleFunction.sendMessageToPlayers(rids, builder.build());
		}
		
		
		
		
	}
	
	/**
	 * 发起投票
	 */
	public void launchVote(Player player,long roomId){
		Role role = player.getRole();
		Game game = getGame(roomId);
		FriendRoom friendRoom = friendRoomMap.get(roomId);
		if(friendRoom == null ){
			LogUtil.info("房间不存在了");
			return;
		}else if(friendRoom != null && !game.getSpriteMap().containsKey(player.getId())){
			LogUtil.info("不是本房人员，不能解散房间");
			return;
		}
		
		LogUtil.error("发起投票");
		if(friendRoom.getAgreeMap() != null && friendRoom.getAgreeMap().size() > 0){
			LogUtil.error("已经有人先发起投票，变成同意投票");
			agreeClear(role.getRid(), roomId, 1);
			return;
		}
		friendRoom.setVoteStartTime(System.currentTimeMillis());
		friendRoom.getAgreeMap().put(role.getRid(), 1);
		boolean isAllAgree = true;
		for (Entry<Long, GameRole> entry : game.getSpriteMap().entrySet()) {
			if(entry.getKey() != role.getRid()){
				if(friendRoom.getLoginOutList() != null && friendRoom.getLoginOutList().contains(entry.getKey())){
					friendRoom.getAgreeMap().put(entry.getKey(), 1);
				}else{
					friendRoom.getAgreeMap().put(entry.getKey(), 0);
					isAllAgree = false;
				}
				
			}
		}
		
		GMsg_12020004.Builder builder = GMsg_12020004.newBuilder();
		FriendRoomCsv friendRoomCsv = friendRoomCache.getConfig(game.getGameType());
		builder.setTime(friendRoom.getVoteStartTime() / 1000 + friendRoomCsv.getPromptGoOutTime() - System.currentTimeMillis() / 1000);
		int i = 0;
		for(Entry<Long,Integer> entry : friendRoom.getAgreeMap().entrySet()){
			if(i == 0){
				builder.setNick(game.getSpriteMap().get(entry.getKey()).getRole().getNick());
			}else{
				GFClearRoomInfo.Builder clearRoomInfo = GFClearRoomInfo.newBuilder();
				clearRoomInfo.setAgree(entry.getValue());
				clearRoomInfo.setNick(game.getSpriteMap().get(entry.getKey()).getRole().getNick());
				builder.addClearRoomInfo(clearRoomInfo);
			}
			i++;
		}
		roleFunction.sendMessageToPlayers(friendRoom.getAgreeMap().keySet(), builder.build());
		if(isAllAgree){
			clear(friendRoom, true, false);
		}
	}

	/**
	 * 同意解散房间
	 * @param player
	 * @param roomId
	 * @param agree 1同意，0不同意
	 */
	public void agreeClear(long rid ,long roomId,int agree){
		FriendRoom friendRoom = friendRoomMap.get(roomId);
		Game game = runningGameMap.get(roomId);
		if(friendRoom == null ){
			LogUtil.error("房间不存在了");
			return;
		}else if(friendRoom != null && !game.getSpriteMap().containsKey(rid)){
			LogUtil.error("不是本房人员，不能投票");
			return;
		}
		if((friendRoom.getAgreeMap().size() <= 0 || friendRoom.getAgreeMap() == null)){
			LogUtil.error("操作错误，没人发起投票");
			return;
		}
		if(friendRoom.getAgreeMap().get(rid) != 0){
			GMsg_12020004.Builder builder = GMsg_12020004.newBuilder();
			FriendRoomCsv friendRoomCsv = friendRoomCache.getConfig(game.getGameType());
			builder.setTime(friendRoom.getVoteStartTime() / 1000 + friendRoomCsv.getPromptGoOutTime() - System.currentTimeMillis() / 1000);
			int i = 0;
			for(Entry<Long,Integer> entry : friendRoom.getAgreeMap().entrySet()){
				if(i == 0){
					builder.setNick(game.getSpriteMap().get(entry.getKey()).getRole().getNick());
				}else{
					GFClearRoomInfo.Builder clearRoomInfo = GFClearRoomInfo.newBuilder();
					clearRoomInfo.setAgree(entry.getValue());
					clearRoomInfo.setNick(game.getSpriteMap().get(entry.getKey()).getRole().getNick());
					builder.addClearRoomInfo(clearRoomInfo);
				}
				i++;
			}
			roleFunction.sendMessageToPlayer(rid, builder.build());
			return;
		}
		boolean isVote = true;
		if(agree != 0){
//			if(friendRoom.isOwnerClear() && friendRoom.getOwner() == rid){
//				//房主同意解散
//				
//				clear(friendRoom,isVote);
//				return;
//			}
			//同意投票
			friendRoom.getAgreeMap().put(rid, 1);
		}else{
//			if(friendRoom.isOwnerClear() && friendRoom.getOwner() == rid){
//				//房主不同解散
//				friendRoom.setOwnerClear(false);
//				friendRoom.setStartTime(System.currentTimeMillis());
//				return;
//			}
			friendRoom.setStartTime(System.currentTimeMillis());
			friendRoom.getAgreeMap().put(rid, 2);
			friendRoom.setOwnerClear(false);
		}
		//是否有人没投票
		boolean flag = false;
		
		//是否有人拒绝
		boolean haveReject = false;
		
		for(Entry<Long,Integer> entry : friendRoom.getAgreeMap().entrySet()){
			if(entry.getValue() == 0){
				flag = true;
			}else if(entry.getValue() == 2){
				haveReject = true;
			}
		}
		if(!flag && !haveReject){
			//所有人都同意解散
			clear(friendRoom,isVote,false);
		}else{
			GMsg_12020004.Builder builder = GMsg_12020004.newBuilder();
			FriendRoomCsv friendRoomCsv = friendRoomCache.getConfig(game.getGameType());
			builder.setTime(friendRoom.getVoteStartTime() / 1000 + friendRoomCsv.getPromptGoOutTime() - System.currentTimeMillis() / 1000);
			int i = 0;
			for(Entry<Long,Integer> entry : friendRoom.getAgreeMap().entrySet()){
				if(i == 0){
					builder.setNick(game.getSpriteMap().get(entry.getKey()).getRole().getNick());
				}else{
					GFClearRoomInfo.Builder clearRoomInfo = GFClearRoomInfo.newBuilder();
					clearRoomInfo.setAgree(entry.getValue());
					clearRoomInfo.setNick(game.getSpriteMap().get(entry.getKey()).getRole().getNick());
					builder.addClearRoomInfo(clearRoomInfo);
				}
				i++;
			}
			if(!flag && haveReject){
				builder.setEndRound(1);
			}
			roleFunction.sendMessageToPlayers(friendRoom.getAgreeMap().keySet(), builder.build());
		}
		if(!flag && haveReject){
			friendRoom.getAgreeMap().clear();
			friendRoom.setVoteStartTime(0);
			friendRoomMap.put(roomId, friendRoom);
		}
	}

	/**
	 * 好友房达到次数
	 * @param roomId
	 */
	public void roundEnd(long roomId){
		FriendRoom friendRoom = getFriendRoom(roomId);
//		friendRoom.getRoundTime().add(friendRoom.getRoundCreateTime());
//		friendRoom.getPlayerScoreList().add(scoreMap);
//		friendRoom.setNowRound(friendRoom.getNowRound() + 1);
		if(friendRoom.getNowRound() > friendRoom.getAllRound()){
			boolean isVote = false;
			LogUtil.info("解散，总局数："+friendRoom.getAllRound() + "，当前局数：" + friendRoom.getNowRound());
			clear(friendRoom,isVote,false);
		}else{
			friendRoomDao.updateSpriteToString(friendRoom);
		}
	}
	
	public void dealRunningGames(){
		for (Map.Entry<Long, Game> entry : runningGameMap.entrySet()) {
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
					}
				}
			});

		}
	}

	public void clearRoomBeforeStart(Player player,long roomId){
		Role role = player.getRole();
		Game game = getGame(roomId);
		FriendRoom friendRoom = friendRoomMap.get(roomId);
		GMsg_12020009.Builder builder = GMsg_12020009.newBuilder();
		if(friendRoom == null ){
			LogUtil.info("房间不存在了");
			builder.setSuccess(0);
			return;
		}else if(friendRoom != null && !game.getSpriteMap().containsKey(player.getId())){
			LogUtil.info("不是本房人员，不能解散房间");
			builder.setSuccess(0);
			return;
		}else{
			boolean isVote = false;
			if(friendRoom.isStart() == FriendRoomPayType.NO_START){
				if(role.getRid() == friendRoom.getOwner()){
					//牌局还没创建成功，直接解散
					LogUtil.error("房主在牌局还没创建成功的时候解散了房间");
					clear(friendRoom,isVote,false);
					switch (friendRoom.getPayType()) {
					case FriendRoomPayType.ROOMCARD:
						itemFunction.addPackItem(role, friendRoom.getCardId(), friendRoom.getCardNum(), ItemEvent.FriendRoom, true);
						break;
						
					case FriendRoomPayType.DIAMOND:
						for(long rid : friendRoom.getSpriteMap().keySet()){
							Role role1 = roleFunction.getRoleByRid(rid);
							roleFunction.diamondAdd(role1, friendRoom.getCardNum(), MoneyEvent.FRIENDROOM);
						}
						
						break;

					default:
						break;
					}
					
					builder.setSuccess(1);
				}else{
					LogUtil.error("普通人在牌局还没创建成功时不能发起投票");
					builder.setSuccess(0);
				}
			}else{
				builder.setSuccess(0);
			}
		}
		
		
		roleFunction.sendMessageToPlayers(friendRoom.getSpriteMap().keySet(), builder.build());
	}
	
	/**
	 * 好友房定时扫描，房间长时间不动，返回给房主处理解散
	 */
	public void clearScheduler(){
		long serviceTime = System.currentTimeMillis();
		for(Entry<Long, FriendRoom> entey :friendRoomMap.entrySet()){
			FriendRoom friendRoom = entey.getValue();
			Game game = getGame(friendRoom.getId());
			 FriendRoomCsv friendRoomCsv = friendRoomCache.getConfig(game.getGameType());
			if(friendRoom.isStart() == FriendRoomPayType.NO_START){
				continue;
			}
			
			if(friendRoom.getAgreeMap().size() > 0 && friendRoom.getVoteStartTime() != 0
					&& serviceTime < friendRoom.getVoteStartTime() + friendRoomCsv.getPromptGoOutTime() * 1000){
				continue;
			}else if(friendRoom.getAgreeMap().size() == 0 && friendRoom.isOwnerClear()){
				continue;
			}else if(serviceTime < friendRoom.getStartTime() + friendRoomCsv.getPromptGoOutTime() * 1000){
				continue;
			}
			
			singleThreadManager.executeTask(new SingleThreadTask(friendRoom) {
				@Override
				public void doTask(ISingleData singleData) {
					FriendRoom friendRoom = (FriendRoom) singleData;
					if(friendRoom.getAgreeMap().size() > 0){
						for(Entry<Long, Integer> entry : friendRoom.getAgreeMap().entrySet()){
							if(entry.getValue() == 0){
								agreeClear(entry.getKey(), friendRoom.getId(), 1);
							}
						}
					}else{
						LogUtil.info("推送给房主解散房间");
						GMsg_12020008.Builder builder = GMsg_12020008.newBuilder();
						//房主座位号肯定是1
						String nick = "";
						switch (friendRoom.getGameType()) {
						case GameType.DOUDIZHU:
							ZTDoudizhuTable ztDoudizhuTable = ztDoudizhuFunction.getTable(friendRoom.getId());
							if(ztDoudizhuTable.getGame().getStatus() != GameStatus.RUNNING){
								ZTDoudizhuRole ztDoudizhuRole = ztDoudizhuTable.getMembers().get(ztDoudizhuTable.getLastPlaySeat() - 1);
								if(ztDoudizhuRole != null && ztDoudizhuRole.getRole() != null && ztDoudizhuRole.getRole().getRole() != null){
									nick = ztDoudizhuRole.getRole().getRole().getNick();
								}
							}
							break;
						case GameType.MAJIANG:
							ZTMaJongTable ztMaJongTable = ztMajiangFunction.getTable(friendRoom.getId());
							if(ztMaJongTable.getGame().getStatus() != GameStatus.RUNNING){
								ZTMajiangRole ztMajiangRole = ztMaJongTable.getMembers().get(ztMaJongTable.getLastPlaySeat() - 1);
								if(ztMajiangRole != null && ztMajiangRole.getRole() != null && ztMajiangRole.getRole().getRole() != null){
									nick = ztMajiangRole.getRole().getRole().getNick();
								}
							}
							
							break;
						case GameType.ZXMAJIANG:
							ZTMaJongTable zxMaJiangTable = zxMajiangFunction.getTable(friendRoom.getId());
							if(zxMaJiangTable.getGame().getStatus() != GameStatus.RUNNING){
								ZTMajiangRole zxMaJiangRole = zxMaJiangTable.getMembers().get(zxMaJiangTable.getLastPlaySeat() - 1);
								if(zxMaJiangRole != null && zxMaJiangRole.getRole() != null && zxMaJiangRole.getRole().getRole() != null){
									nick = zxMaJiangRole.getRole().getRole().getNick();
								}
							}
							break;
						}
						friendRoom.setOwnerClear(true);
						builder.setNick(nick);
						roleFunction.sendMessageToPlayer(friendRoom.getOwner(), builder.build());
					}
					
					
				}
			});
			
		}
	}

	/**
	 * 好友房准备，收到所有人准备后开始，或者10秒后自动开始
	 * @param role
	 * @param roomId
	 */
	public void playerPrepare(Role role,long roomId) {
		// TODO Auto-generated method stub
		LogUtil.info("发送准备");
		FriendRoom friendRoom = getFriendRoom(roomId);
		if(friendRoom != null){
			LogUtil.info("准备rid:" + role.getRid());
			LogUtil.info("friendroom start prepare size:" + friendRoom.getPrepareList().size());
			if(friendRoom.getPrepareList() == null || friendRoom.getPrepareList().size() == 0){
				LogUtil.info("好友房准备" + role.getNick());
				friendRoom.getPrepareList().add(role.getRid());
			}else if(!friendRoom.getPrepareList().contains(role.getRid())){
				LogUtil.info("好友房准备" + role.getNick());
				friendRoom.getPrepareList().add(role.getRid());
			}
			LogUtil.info("friendroom end prepare size:" + friendRoom.getPrepareList().size());
			//updateFriendRoomMap(friendRoom);
		}
		
		
		
	}

	/**
	 * 好友房断线重连
	 * @param role
	 */
	public void enterFriendRoom(Role role){
		LogUtil.info(role.getNick() + "重连好友房");
		long roomId = getRoomIdByRid(role.getRid());
		FriendRoom friendRoom = getFriendRoom(roomId);
		Game game = getGame(friendRoom.getId());
		if(friendRoom != null){
			GGameInfo.Builder builder = GGameInfo.newBuilder();
			builder.setGameType(game.getGameType());
			builder.setRoomId(game.getRoomId());
			builder.setRoomType(game.getRoomType());
			for (Map.Entry<Long, GameRole> entry : game.getSpriteMap()
					.entrySet()) {
				GGameRole.Builder gameRole1 = GGameRole.newBuilder();
				Role target = entry.getValue().getRole();
				gameRole1.setRid(entry.getKey());
				gameRole1.setNick(target.getNick());
				gameRole1.setGold(friendRoom.getSpriteMap().get(entry.getKey()));
				gameRole1.setHead(target.getHead());
				gameRole1.setLevel(target.getLevel());
				gameRole1.setSeat(entry.getValue().getSeat());
				gameRole1.setAvatarId(entry.getValue().getAvatarId());
				gameRole1.setSex(target.getSex());
				gameRole1.setIsOnline(1);
				if(entry.getKey() == friendRoom.getOwner()){
					if(friendRoom.isOwnerExit() == FriendRoomPayType.NO_EXIT){
						gameRole1.setIsOnline(1);
					}else{
						gameRole1.setIsOnline(0);
					}
				}
				if(friendRoom.getLoginOutList() != null
						&& friendRoom.getLoginOutList().contains(entry.getKey())
						&& entry.getKey() != role.getRid()){
					gameRole1.setIsOnline(0);
				}
				builder.addSprites(gameRole1);
			}
			GMsg_12020002.Builder builder1 = GMsg_12020002.newBuilder();
			int isFull = 0;
			int ownerExit = 0;
			if(friendRoom.isOwnerExit() == FriendRoomPayType.EXIT){
				ownerExit = 1;
			}
			if(friendRoom.isOwnerExit() == FriendRoomPayType.NO_EXIT && game.getNeedCount() == game.getSpriteMap().size()){
				isFull = 1;
				if(game.getNeedCount() <= game.getSpriteMap().size() && friendRoom.isOwnerExit() == FriendRoomPayType.NO_EXIT){
					Event event = new Event(HandleType.GAME_INIT, game);
					DispatchEvent.dispacthEvent(event);
				}
				GMsg_12020010.Builder builder2 = GMsg_12020010.newBuilder();
				builder2.setIsFull(1);
				roleFunction.sendMessageToPlayer(role.getRid(), builder2.build());
			}
			builder1.setPower(friendRoom.getHighestPower());
			builder1.setAllRound(friendRoom.getAllRound());
			builder1.setNowRound(friendRoom.getNowRound());
			builder1.setOwnerExit(ownerExit);
			builder1.setIsFull(isFull);
			builder1.setGame(builder);
			builder1.setOwner(friendRoom.getOwner());
			builder1.setSuccess(1);
			roleFunction.sendMessageToPlayer(role.getRid(),
					builder1.build());
			if(friendRoom.isStart() == FriendRoomPayType.START){
				if(role.getRid() == friendRoom.getOwner()){
					if(friendRoom.getAgreeMap().size() > 0
							&& friendRoom.getAgreeMap().containsKey(role.getRid())){
						if(friendRoom.getAgreeMap().get(role.getRid()) == 0){
							//断线重连有投票，默认同意
							agreeClear(role.getRid(), roomId, 1);
						}
					}else if(friendRoom.isOwnerClear()){
						//断线重连有推送给房主解散，默认不同意
						friendRoom.setOwnerClear(false);
					}
				}
				//好友房开始
				GameRole gameRole = game.getSpriteMap().get(role.getRid());
				switch (game.getGameType()) {
				case GameType.DOUDIZHU:
					LogUtil.info("重连昭通斗地主好友房");
					ztDoudizhuFunction.enterTable(game, gameRole);
					break;
					
				case GameType.MAJIANG:
					LogUtil.info("重连昭通麻将好友房");
					ztMajiangFunction.enterTable(game, gameRole);
					break;
					
				case GameType.ZXMAJIANG:
					LogUtil.info("重连镇雄麻将好友房");
					zxMajiangFunction.enterTable(game, gameRole);
					break;

				default:
					break;
				}
			}
		}
	}

	/**
	 * 查看房间付费类型
	 * @param role
	 * @param roomId
	 */
	public void getPayType(Role role, long roomId) {
		if(IS_MATCHING.get()){
			return;
		}
		IS_MATCHING.set(true);
		try {
			// TODO Auto-generated method stub
			FriendRoom friendRoom = getFriendRoom(roomId);
			Game game = getGame(roomId);
			GMsg_12020013.Builder builder = GMsg_12020013.newBuilder();
			if(friendRoom != null && game != null){
				LogUtil.info("friendRoom.getSpriteMap().size():"+friendRoom.getSpriteMap().size());
				LogUtil.info("game.getNeedCount():"+game.getNeedCount());
				if(game.getNeedCount() == friendRoom.getSpriteMap().size()){
					builder.setReason(2);
				}
				builder.setIsExist(1);
				builder.setRoomId(roomId);
				builder.setGameType(friendRoom.getGameType());
				if(friendRoom.getPayType() == FriendRoomPayType.DIAMOND){
					builder.setIsPay(1);
					builder.setPayNum(friendRoom.getCardNum());
					builder.setPayType(friendRoom.getPayType());
				}else{
					builder.setIsPay(0);
				}
			}else{
				builder.setIsExist(0);
				builder.setReason(1);
			}
			roleFunction.sendMessageToPlayer(role.getRid(), builder.build());
		} finally {
			// TODO: handle exception
			IS_MATCHING.set(false);
		}
		
	}

	/**
	 * 好友房好友离线通知
	 */
	@Override
	public void handleOnRoleLogout(final Role role) {
		long roomId = getRoomIdByRid(role.getRid());
		FriendRoom friendRoom = getFriendRoom(roomId);
		Game game = getGame(roomId);
		
		if(friendRoom != null && game != null){
			Map<Long, GameRole> spriteMap = game.getSpriteMap();
			LogUtil.info("好友房玩家掉线");
			GameRole gameRole = spriteMap.get(role.getRid());
			LogUtil.info("玩家掉线时的手牌:"+gameRole.getPai());
			friendRoom.getLoginOutList().add(role.getRid());
			if(friendRoom.getSpriteMap().size() > 1){
				GMsg_12020014.Builder builder = GMsg_12020014.newBuilder();
				builder.setSeat(game.getSpriteMap().get(role.getRid()).getSeat());
				builder.setType(0);
				for(long rid : friendRoom.getSpriteMap().keySet()){
					if(rid != role.getRid()){
						if (friendRoom.getLoginOutList() != null && !friendRoom.getLoginOutList().contains(rid)) {
							if(rid != friendRoom.getOwner() || friendRoom.isOwnerExit() == FriendRoomPayType.NO_EXIT){
								roleFunction.sendMessageToPlayer(rid, builder.build());
							}
						}
						
					}
					
				}
				
			}
			if(friendRoom.getAgreeMap() != null && friendRoom.getAgreeMap().size() > 0){
				if(friendRoom.getAgreeMap().containsKey(role.getRid()) && friendRoom.getAgreeMap().get(role.getRid()) == 0){
					//还没投票,掉线后默认选择同意
					agreeClear(role.getRid(), roomId, 1);
				}
			}
			
		}
	}
	
	/**
	 * 好友房好友上线通知
	 */
	@Override
	public void handleOnRoleLogin(final Role role){
		long roomId = getRoomIdByRid(role.getRid());
		FriendRoom friendRoom = getFriendRoom(roomId);
		Game game = getGame(roomId);
		if(friendRoom != null && game != null){
			List<Long> list = friendRoom.getLoginOutList();
			if(list != null && list.contains(role.getRid())){
				LogUtil.info("好友房玩家上线");
				//断线列表存在改用户
				list.remove(list.indexOf(role.getRid()));
				if(friendRoom.getSpriteMap().size() > 1){
					GMsg_12020014.Builder builder = GMsg_12020014.newBuilder();
					builder.setSeat(game.getSpriteMap().get(role.getRid()).getSeat());
					builder.setType(1);
					for(long rid : friendRoom.getSpriteMap().keySet()){
						if(rid != role.getRid()){
							if (friendRoom.getLoginOutList() != null && !friendRoom.getLoginOutList().contains(rid)) {
								if(rid != friendRoom.getOwner() || friendRoom.isOwnerExit() == FriendRoomPayType.NO_EXIT){
									roleFunction.sendMessageToPlayer(rid, builder.build());
								}
							}
							
						}
						
					}
				}
			}
			
		}
	}

	/**
	 * 响应推给房主解散房间
	 * @param rid
	 * @param roomId
	 * @param agree
	 */
	public void ownerAgree(long rid ,long roomId,int agree){
		FriendRoom friendRoom = friendRoomMap.get(roomId);
		Game game = runningGameMap.get(roomId);
		boolean isVote = true;
		if(friendRoom != null && game != null){
			if(friendRoom.isOwnerClear() && friendRoom.getOwner() == rid){
				if(agree == 1){
					//房主同意解散
					clear(friendRoom,isVote,false);
					return;
				}else{
					//房主不同解散
					friendRoom.setOwnerClear(false);
					friendRoom.setStartTime(System.currentTimeMillis());
					return;
				}
			}
		}
	}

	/**
	 * 超时解散房间
	 */
	public void clearRoomOvertime(){
		long serviceTime = System.currentTimeMillis() / 1000;
		boolean isVote = false;
		for(FriendRoom friendRoom : friendRoomMap.values()){
			if(friendRoom.isStart() == FriendRoomPayType.NO_START && friendRoom.getCreateTime() + TimeUtil.ONE_DAY < serviceTime){
				LogUtil.error("超时解散房间，并返回房卡或金币");
				clear(friendRoom,isVote,false);
				switch (friendRoom.getPayType()) {
				case FriendRoomPayType.ROOMCARD:
					if(friendRoom.getCardNum() != 0){
						itemFunction.addPackItem(roleFunction.getRoleByRid(friendRoom.getOwner()), friendRoom.getCardId(), friendRoom.getCardNum(), ItemEvent.FriendRoom, true);
					}
					break;
					
				case FriendRoomPayType.DIAMOND:
					if(friendRoom.getCardNum() != 0){
						for(long rid : friendRoom.getSpriteMap().keySet()){
							Role role1 = roleFunction.getRoleByRid(rid);
							roleFunction.diamondAdd(role1, friendRoom.getCardNum(), MoneyEvent.FRIENDROOM);
						}
					}
					break;

				default:
					break;
				}
			}
		}
	}

	/**
	 * 强制解散房间
	 */
	public boolean forceClear(long roomId){
		FriendRoom friendRoom = getFriendRoom(roomId);
		if(friendRoom == null){
			return false;
		}else{
			clear(friendRoom, true,true);
			return true;
		}
	}

	@Override
	public void handleOnServerStart() {
		LogUtil.info("重启服务器加载好友房信息");
		List<FriendRoom> friendRooms = friendRoomDao.findAll();
		if(friendRooms == null || friendRooms.size() == 0){
			return;
		}
		for(FriendRoom friendRoom : friendRooms){
			Map<Long, Integer> map = StringUtil.stringToMap(friendRoom.getSpriteToString(), StringUtil.DELIMITER_BETWEEN_ITEMS,
					StringUtil.DELIMITER_INNER_ITEM, Long.class, Integer.class);
			friendRoom.getSpriteMap().putAll(map);
			friendRoom.setStartTime(System.currentTimeMillis());
			friendRoom.getLoginOutList().addAll(friendRoom.getSpriteMap().keySet());
			int realType = getRealType(friendRoom.getGameType(), friendRoom.getRoomType());
			Game game = newGame((int)friendRoom.getId(),realType,friendRoom.getBaseChip());
			game.setFriendRoom(true);
			
			int i = 0;
			for(long rid : friendRoom.getSpriteMap().keySet()){
				i = i + 1;
				// 把玩家添加到比赛映射表中
				roleGameMap.put(rid, friendRoom.getId());
				
				GameRole gameRole = new GameRole(roleFunction.getRoleByRid(rid), game.getRoomId());
				gameRole.setSeat(i);
				game.getRoles().add(rid);
				game.getSpriteMap().put(rid, gameRole);
			}
			// 把游戏添加到比赛映射表中
			runningGameMap.put(friendRoom.getId(), game);
			
			friendRoomMap.put(friendRoom.getId(), friendRoom);
			

		}
	}
	
	public void getParameter(Role role){
		List<FriendRoomCsv> friendRoomCsvlist = friendRoomCache.getConfigList();
		if(friendRoomCsvlist.size() > 0){
			LogUtil.info("friendRoomCsvlist size is " + friendRoomCsvlist.size());
			GMsg_12020017.Builder builder = GMsg_12020017.newBuilder();
			for(FriendRoomCsv friendRoomCsv : friendRoomCsvlist){
				
				Gcsv.Builder builder2 = Gcsv.newBuilder();
				builder2.setBottomNote(friendRoomCsv.getBottomNote());
				builder2.setCustomMultiple(friendRoomCsv.getCustomMultiple());
				builder2.setDisplay(friendRoomCsv.getDisplay());
				builder2.setEstablishConsume(friendRoomCsv.getEstablishConsume());
				builder2.setEstablishIntegral(friendRoomCsv.getEstablishIntegral());
				builder2.setGameType(friendRoomCsv.getGameType());
				builder2.setGoOutTime(friendRoomCsv.getGoOutTime());
				builder2.setPayType(friendRoomCsv.getPayType());
				builder2.setPromptGoOutTime(friendRoomCsv.getPromptGoOutTime());
				builder2.setRoomId(friendRoomCsv.getRoomId());
				builder2.setRoomMultiple(friendRoomCsv.getRoomMultiple());
				builder.addCsv(builder2);
				LogUtil.info(friendRoomCsv.toString());
				
			}
			roleFunction.sendMessageToPlayer(role.getRid(), builder.build());
		}
	}
	
}
