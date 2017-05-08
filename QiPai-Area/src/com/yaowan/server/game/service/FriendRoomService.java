package com.yaowan.server.game.service;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.FriendRoomPayType;
import com.yaowan.constant.MoneyEvent;
import com.yaowan.csv.cache.FriendRoomCache;
import com.yaowan.csv.entity.FriendRoomCsv;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_12020001;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_12020002;
import com.yaowan.server.game.function.FriendRoomFunction;
import com.yaowan.server.game.function.ItemFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.model.data.dao.PackItemDao;
import com.yaowan.server.game.model.data.entity.FriendRoom;

@Component
public class FriendRoomService {
	
	@Autowired
	private RoomFunction roomFunction;
	
	@Autowired
	private FriendRoomFunction friendRoomFunction;
	
	@Autowired
	private FriendRoomCache friendRoomCache;
	
	@Autowired
	private PackItemDao packItemDao;
	
	@Autowired
	private RoleFunction roleFunction;
	
	@Autowired
	private ItemFunction itemFunction;
	
	
	
	/**
	 * 创建好友房
	 * @param player
	 * @param gameType 游戏类型
	 * @param roomNick 房间名称
	 * @param num 房间类型
	 * @param highestPowerNum 最高倍数
	 * @param payType 付费类型
	 */
	public void createFriendRoom(Player player,int gameType,int num,int highestPowerNum,int payType){
		// 在游戏中
		LogUtil.info("好友房创建。。。。");
		GMsg_12020001.Builder builder = GMsg_12020001.newBuilder();
		if (roomFunction.isInGame(player.getRole().getRid())) {
			builder.setSuccess(0);
			roleFunction.sendMessageToPlayer(player.getRole().getRid(), builder.build());
			return;
		}
		
		// 已在排队队列中
		if (roomFunction.isReadyMatching(player.getRole())) {
			builder.setSuccess(0);
			roleFunction.sendMessageToPlayer(player.getRole().getRid(), builder.build());
			return;
		}
		
		//已经在房间中
		if(friendRoomFunction.inGameByrid(player.getRole().getRid())){
			FriendRoom friendRoom2 = friendRoomFunction.getFriendRoom(friendRoomFunction.getRoomIdByRid(player.getRole().getRid()));
			Game game = friendRoomFunction.getGame(friendRoom2.getId());
			if(friendRoom2.getOwner() == player.getRole().getRid()){
				friendRoomFunction.joinInFriendRoom(player, friendRoomFunction.getRoomIdByRid(player.getRole().getRid()));
				LogUtil.error("已经是别的房间的房主，主动加入别的房间");
			}else if(game.getSpriteMap().containsKey(player.getRole().getRid())){
				LogUtil.error("已经在游戏中了");
				friendRoomFunction.enterFriendRoom(player.getRole());
//				builder.setSuccess(0);
//				roleFunction.sendMessageToPlayer(player.getRole().getRid(), builder.build());
			}
		}else{
			FriendRoomCsv friendRoomCsv = new FriendRoomCsv();
			List<FriendRoomCsv> friendRoomCsvlist = friendRoomCache.getConfigList();
			for(FriendRoomCsv friendRoomCsv1 : friendRoomCsvlist){
				if(friendRoomCsv1.getGameType() == gameType && friendRoomCsv1.getPayType() == payType){
					friendRoomCsv = friendRoomCsv1;
					break;
				}
			}
			String establishConsume = friendRoomCsv.getEstablishConsume();
			String RoomMultiple = friendRoomCsv.getRoomMultiple();
			List<Integer[]> list = StringUtil.stringToListArray(establishConsume, Integer.class, "|", "_");
			List<Integer> highestPowerList = StringUtil.stringToList(RoomMultiple, "|", Integer.class);
			
			int round = 0;//需要进行的局数
			int baseChip = friendRoomCsv.getBottomNote();
			int highestPower = 0;
			int initScore = 0;
			int cardNum = 0;
			int cardId = 0;
			if(list.size() < num || num <= 0){
				return;
			}else{
				Integer[] item = list.get(num - 1);
				round = item[0];
				cardId = item[1];
				cardNum = item[2];
				initScore = friendRoomCsv.getEstablishIntegral() * item[2];
				if(highestPowerList.size() < num || num <= 0){
					highestPower = highestPowerList.get(0);
				}else{
					highestPower = highestPowerList.get(highestPowerNum - 1);
				}
				
				

				switch (payType) {
				case FriendRoomPayType.ROOMCARD:
					//判断房卡是否满足需求
					if(cardNum > 0){
						if(!packItemDao.isExistNum(item[1], player.getRole().getRid(), item[2])){
							builder.setSuccess(2);
							roleFunction.sendMessageToPlayer(player.getRole().getRid(), builder.build());
								return;
						}else{
								itemFunction.useByItemId(player.getRole(), cardId, cardNum, true);
						}
					}else{
						payType = 0;
					}
					break;
				case FriendRoomPayType.DIAMOND:
					if(cardNum > 0){
						if(player.getRole().getDiamond() < item[2]){
							builder.setSuccess(3);
							roleFunction.sendMessageToPlayer(player.getRole().getRid(), builder.build());
								return;
						}else{
							roleFunction.diamondSub(player.getRole(), item[2], MoneyEvent.FRIENDROOM, true);
						}
					}else{
						payType = 0;
					}
					break;

				default:
					break;
				}
				
				
			}
			
			
			//随机生成6位数口令
			String id = "";
			while(id.length() != 6){
				 Random random = new Random();  
				 id  = random.nextInt(1000000) +"";
				 if(friendRoomFunction.getFriendRoom(Integer.parseInt(id)) != null){
					 id = "";
				 }
			} 
			//创建房间
			friendRoomFunction.createFriendRoom(player.getRole(), gameType, Integer.parseInt(id), round, baseChip, highestPower, initScore,cardNum,cardId,payType,num);
		}
		
		
		
	}
	
	/**
	 * 加入好友房
	 * @param player
	 * @param password
	 * @param roomId
	 */
	public void joinInFriendRoom(Player player,long roomId){
		LogUtil.error(player.getRole().getNick());
		FriendRoom friendRoom = friendRoomFunction.getFriendRoom(roomId);
		GMsg_12020002.Builder builder = GMsg_12020002.newBuilder();
		if(friendRoom == null){
			if(friendRoomFunction.getFriendRoom(friendRoomFunction.getRoomIdByRid(player.getRole().getRid())) == null){
				builder.setSuccess(0);
				roleFunction.sendMessageToPlayer(player.getRole().getRid(), builder.build());
				return;
			}
			
		}
		// 在游戏中
		if (roomFunction.isInGame(player.getRole().getRid())) {
			builder.setSuccess(0);
			roleFunction.sendMessageToPlayer(player.getRole().getRid(), builder.build());
			return;
		}
		
		// 已在排队队列中
		if (roomFunction.isReadyMatching(player.getRole())) {
			builder.setSuccess(0);
			roleFunction.sendMessageToPlayer(player.getRole().getRid(), builder.build());
			return;
		}
		
		//已经在房间中
		if(friendRoomFunction.inGameByrid(player.getRole().getRid())){
			FriendRoom friendRoom2 = friendRoomFunction.getFriendRoom(friendRoomFunction.getRoomIdByRid(player.getRole().getRid()));
			Game game = friendRoomFunction.getGame(friendRoom2.getId());
			if(friendRoom2.getOwner() == player.getRole().getRid()){
				friendRoomFunction.joinInFriendRoom(player, friendRoomFunction.getRoomIdByRid(player.getRole().getRid()));
				LogUtil.error("已经是别的房间的房主，主动加入别的房间");
			}else if(game.getSpriteMap().containsKey(player.getRole().getRid())){
				LogUtil.error("已经在游戏中了");
				friendRoomFunction.enterFriendRoom(player.getRole());
//				builder.setSuccess(0);
//				roleFunction.sendMessageToPlayer(player.getRole().getRid(), builder.build());
			}
			
		}else{
			//房间人数已满
			int owner = 0;
			if(!friendRoomFunction.getGame(roomId).getSpriteMap().containsKey(friendRoom.getOwner())){
				owner = owner + 1;
			}
			if(friendRoomFunction.getGame(roomId).getNeedCount() <= friendRoomFunction.getGame(roomId).getSpriteMap().size() + owner){
				builder.setSuccess(0);
				roleFunction.sendMessageToPlayer(player.getRole().getRid(), builder.build());
				return;
			}
			if(player.getRole().getRid() != friendRoom.getOwner() && friendRoom.getPayType() == FriendRoomPayType.DIAMOND){
				//多人付费房，需自己付砖石
				if(player.getRole().getDiamond() >= friendRoom.getCardNum()){
					friendRoomFunction.joinInFriendRoom(player, roomId);
					roleFunction.diamondSub(player.getRole(), friendRoom.getCardNum(), MoneyEvent.FRIENDROOM, true);
				}
			}else{
				friendRoomFunction.joinInFriendRoom(player, roomId);
			}
			
		}
		
		
		
		
	}

	/**
	 * 退出房间
	 */
	public void exitFriendRoom(Player player,long roomId){
		friendRoomFunction.exitFriendRoom(player, roomId);
	}
	
	/**
	 * 发起投票解散房间
	 */
	public void launchVote(Player player,long roomId){
		friendRoomFunction.launchVote(player, roomId);
	}
	
	/**
	 * 玩家操作是否同意解散房间
	 */
	public void agreeClearRoom(Player player,long roomId,int agree){
		friendRoomFunction.agreeClear(player.getRole().getRid(), roomId, agree);
	}
	
	/**
	 * 用户是否在好友房
	 * @param player
	 */
	public void getRoleIsInfriendRoom(Player player){
		friendRoomFunction.getRoleIsInfriendRoom(player);
	}
	
	/**
	 * 游戏还没开始时，房主解散房间
	 */
	public void ownerClearBeforeStart(Player player,long roomId){
		friendRoomFunction.clearRoomBeforeStart(player, roomId);
	}
	
	/**
	 * 玩家准备
	 * @param player
	 * @param roomId
	 */
	public void playerPrepare(Player player,long roomId){
		friendRoomFunction.playerPrepare(player.getRole(),roomId);
	}
	
	/**
	 * 重新进入牌桌
	 * @param player
	 */
	public void enterFriendRoom(Player player){
		friendRoomFunction.enterFriendRoom(player.getRole());
	}

	public void getPayType(Player player, long roomId) {
		// TODO Auto-generated method stub
		friendRoomFunction.getPayType(player.getRole(),roomId);
	}
	
	public void ownerAgree(Player player, long roomId,int agree){
		friendRoomFunction.ownerAgree(player.getRole().getRid(), roomId, agree);
	}
	
	public void getParameter(Player player){
		friendRoomFunction.getParameter(player.getRole());
	}
}
