package com.yaowan.server.game.service;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.FriendRoomPayType;
import com.yaowan.constant.GameError;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.DispatchEvent;
import com.yaowan.framework.util.ObjectUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.GameRole;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.center.CGame.CGameInfo;
import com.yaowan.protobuf.center.CGame.CGameRole;
import com.yaowan.protobuf.game.GGame.GGameInfo;
import com.yaowan.protobuf.game.GGame.GGameRole;
import com.yaowan.protobuf.game.GGame.GMsg_12006002;
import com.yaowan.protobuf.game.GLogin.GMsg_12001001;
import com.yaowan.protobuf.game.GRole.GDoudizhuData;
import com.yaowan.protobuf.game.GRole.GMajiangData;
import com.yaowan.protobuf.game.GRole.GMenjiData;
import com.yaowan.protobuf.game.GRole.GRoleAvatar;
import com.yaowan.protobuf.game.GRole.GRoleInfo;
import com.yaowan.protobuf.game.GRole.GZXMajiangData;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.DoudizhuDataFunction;
import com.yaowan.server.game.function.FriendRoomFunction;
import com.yaowan.server.game.function.MajiangDataFunction;
import com.yaowan.server.game.function.MenjiDataFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.function.ZXMajiangDataFunction;
import com.yaowan.server.game.main.NettyClient;
import com.yaowan.server.game.model.data.entity.DoudizhuData;
import com.yaowan.server.game.model.data.entity.FriendRoom;
import com.yaowan.server.game.model.data.entity.MajiangData;
import com.yaowan.server.game.model.data.entity.MenjiData;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.data.entity.ZXMajiangData;
import com.yaowan.util.MD5Util;

/**
 * 游戏服对中心服业务层
 * 
 * @author KKON
 *
 */
@Component
public class CenterService {

	@Autowired
	private RoomFunction roomFunction;
	@Autowired
	private RoleFunction roleFunction;
	@Autowired
	private LoginService loginService;
	@Autowired
	private DoudizhuDataFunction doudizhuDataFunction;
	@Autowired
	private MajiangDataFunction majiangDataFunction;
	@Autowired
	private ZXMajiangDataFunction zxmajiangDataFunction;
	@Autowired
	private MenjiDataFunction menjiDataFunction;
	@Autowired
	private FriendRoomFunction friendRoomFunction;
	/**
	 * 处理中心服过来的game对象
	 * 
	 * @param player
	 * @param gameInfo
	 */
	public void putGameInServer(CGameInfo gameInfo) {
		Game game = new Game(gameInfo.getRoomId(), roomFunction.getRealType(gameInfo.getGameType(), gameInfo.getRoomType()));
		game.setGameType(gameInfo.getGameType());
		game.setRoomType(gameInfo.getRoomType());
		game.setStartTime(gameInfo.getStartTime());
		game.setEndTime(gameInfo.getEndTime());
		for (CGameRole cGameRole : gameInfo.getSpritesList()) {
			Role role = new Role();
			role.setRid(cGameRole.getRid());
			role.setNick(cGameRole.getNick());
			role.setLevel((short) cGameRole.getLevel());
			role.setHead((byte) cGameRole.getHead());
			role.setGold((int) cGameRole.getGold());
			role.setSex((byte) cGameRole.getSex());

			GameRole gameRole = new GameRole(role, gameInfo.getRoomId());
			gameRole.setAvatarId(cGameRole.getAvatarId());
			gameRole.setSeat(cGameRole.getSeat());
			game.getSpriteMap().put(role.getRid(), gameRole);
			game.getRoles().add(role.getRid());
			// 把玩家添加到比赛映射表中
			roomFunction.getRoleGameMap().put(role.getRid(), game.getRoomId());
		}

		// 各游戏初始化
		roomFunction.getRunningGames().put(game.getRoomId(), game);
		Event event = new Event(HandleType.GAME_INIT, game);
		DispatchEvent.dispacthEvent(event);

		GGameInfo.Builder builder = GGameInfo.newBuilder();
		builder.setGameType(game.getGameType());
		builder.setRoomId(game.getRoomId());
		builder.setRoomType(game.getRoomType());

		for (Map.Entry<Long, GameRole> entry : game.getSpriteMap().entrySet()) {
			GGameRole.Builder gameRole = GGameRole.newBuilder();
			Role target = entry.getValue().getRole();
			gameRole.setRid(entry.getKey());
			gameRole.setNick(target.getNick());
			gameRole.setGold(target.getGold());
			gameRole.setHead(target.getHead());
			gameRole.setLevel(target.getLevel());
			gameRole.setSeat(entry.getValue().getSeat());
			gameRole.setAvatarId(entry.getValue().getAvatarId());

			builder.addSprites(gameRole);

		}

		roleFunction.sendMessageToPlayers(game.getSpriteMap().keySet(), GMsg_12006002.newBuilder().setGame(builder).build());
	}

	public void centerLogin(Player player, String openId, int time, String sign, String imei) {
		Channel channel = NettyClient.getChannel();
		if (channel == null || !channel.isRegistered()) {
			GMsg_12001001.Builder builder = GMsg_12001001.newBuilder();
			builder.setFlag(GameError.WRONG_PARAMETER);
			player.write(builder.build());
			return;
		}
		if (StringUtils.isBlank(openId)) {
			GMsg_12001001.Builder builder = GMsg_12001001.newBuilder();
			builder.setFlag(GameError.WRONG_PARAMETER);
			player.write(builder.build());
			return;
		}
		// 校验前后端MD5
		HashMap<String, Object> signmap = new HashMap<String, Object>();
		signmap.put("openId", openId);
		signmap.put("time", time);
		signmap.put("imei", imei);
		String signString = MD5Util.makeSign(signmap);
		String md5 = sign.trim();
		/*
		 * if (!signString.equals(md5)) { GMsg_12001001.Builder builder =
		 * GMsg_12001001.newBuilder();
		 * builder.setFlag(GameError.LOGIN_WRONG_SIGN);
		 * player.write(builder.build()); return; }
		 */
		GameRole gameRole = roomFunction.getGameRole(player.getId());
		if(gameRole ==null){
			GMsg_12001001.Builder builder = GMsg_12001001.newBuilder();
			builder.setFlag(GameError.LOGIN_NOT_LOG);
			player.write(builder.build());
		}
		GRoleInfo.Builder roleBuilder = dealLoginRole(gameRole.getRole(), player);

		if (roleBuilder == null) {
			return;
		}

		GMsg_12001001.Builder builder = GMsg_12001001.newBuilder();
		builder.setFlag(0);

		builder.setRoleInfo(roleBuilder);
		builder.setServerTime(System.currentTimeMillis());
		player.write(builder.build());
	}
	
	private GRoleInfo.Builder dealLoginRole(Role role,Player player){

		GRoleInfo.Builder roleBuilder = GRoleInfo.newBuilder();
		
		roleBuilder.setRid(role.getRid());
		roleBuilder.setNick(role.getNick());
		roleBuilder.setSex(role.getSex());
		roleBuilder.setHead(role.getHead());
		roleBuilder.setLevel(role.getLevel());
		roleBuilder.setGold(role.getGold());
		roleBuilder.setExp(role.getExp());
		roleBuilder.setDiamond(role.getDiamond());
		
		roleBuilder.setCity(role.getCity());
		roleBuilder.setProvince(role.getProvince());
		roleBuilder.setFavorGames(role.getFavorGames());
		
		for(Map.Entry<Integer, List<Integer>> entry:role.getAvatarMap().entrySet()){
			GRoleAvatar.Builder avatarBuilder = GRoleAvatar.newBuilder();
			avatarBuilder.setId(entry.getKey());
			avatarBuilder.setIslock(entry.getValue().get(0));
			avatarBuilder.setIsfight(entry.getValue().get(1));
			roleBuilder.addRoleAvatar(avatarBuilder);
		}
		GDoudizhuData.Builder doudizhuBuilder = GDoudizhuData.newBuilder();
		DoudizhuData doudizhuData = doudizhuDataFunction.getDoudizhuData(role
				.getRid());
		doudizhuData.setBombTotal(33333);
		ObjectUtil.copyProperties(doudizhuData, doudizhuBuilder);
		roleBuilder.setDoudizhuData(doudizhuBuilder);

		GMenjiData.Builder menjiDataBuilder = GMenjiData.newBuilder();
		MenjiData menjiData = menjiDataFunction.getMenjiData(role.getRid());
		menjiData.setBaoziTotal(2222);
		ObjectUtil.copyProperties(menjiData, menjiDataBuilder);
		//roleBuilder.setMenjiData(menjiDataBuilder);

		GMajiangData.Builder majiangDataBuilder = GMajiangData.newBuilder();
		MajiangData majiangData = majiangDataFunction.getMajiangData(role
				.getRid());
		majiangData.setBigPairTotal(2222);
		ObjectUtil.copyProperties(majiangData, majiangDataBuilder);
		roleBuilder.setMajiangData(majiangDataBuilder);
		
		// 镇雄麻将
		GZXMajiangData.Builder zxmajiangDataBuilder = GZXMajiangData.newBuilder();
		ZXMajiangData zxmajiangData = zxmajiangDataFunction.getZXMajiangData(role
				.getRid());
		zxmajiangData.setBigPairTotal(2222);
		ObjectUtil.copyProperties(zxmajiangData, zxmajiangDataBuilder);
		roleBuilder.setZxmajiangData(zxmajiangDataBuilder);
		
		Game game = roomFunction.getGameByRole(role.getRid());
		if(game != null){
			roleBuilder.setIsMatching(game.getGameType());
		}else{
			long roomId = friendRoomFunction.getRoomIdByRid(role.getRid());
			FriendRoom friendRoom = friendRoomFunction.getFriendRoom(roomId);
			if(friendRoom != null){
				//在好友房内
				if(friendRoom.getOwner() == role.getRid()){
					if(friendRoom.isOwnerExit() == FriendRoomPayType.EXIT){
						//房主退出时回到原来的地方
						roleBuilder.setIsFriendroom(2);
					}else{
						//房主未退出的时候回到游戏内
						roleBuilder.setIsFriendroom(1);
					}
				}else{
					//普通玩家回到游戏内
					roleBuilder.setIsFriendroom(1);
				}
				
			}
		}
		return roleBuilder;
	}
}
