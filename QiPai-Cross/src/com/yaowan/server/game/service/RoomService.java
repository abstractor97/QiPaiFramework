/**
 * 
 */
package com.yaowan.server.game.service;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.ServerConfig;
import com.yaowan.constant.GameStatus;
import com.yaowan.constant.GameType;
import com.yaowan.csv.cache.NiuniuRoomCache;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.DispatchEvent;
import com.yaowan.framework.core.thread.SingleThreadManager;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.GameRole;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.center.CGame.CGameRole;
import com.yaowan.protobuf.center.CGame.CMsg_21100001;
import com.yaowan.protobuf.game.GGame.GMsg_12006001;
import com.yaowan.protobuf.game.GGame.GMsg_12006003;
import com.yaowan.protobuf.game.GGame.GMsg_12006004;
import com.yaowan.protobuf.game.GGame.GMsg_12006005;
import com.yaowan.protobuf.game.GGame.GMsg_12006009;
import com.yaowan.protobuf.game.GGame.GOnlineData;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.main.NettyClient;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.dezhou.function.DZCardFunction;
import com.yaowan.util.RandomPlayerCount;

/**
 * 大厅房间服务
 * 
 * @author zane 2016年10月12日 下午2:13:23
 *
 */
@Component
public class RoomService {

	@Autowired
	private SingleThreadManager manager;

	@Autowired
	private RoomFunction roomFunction;

	@Autowired
	private RoleFunction roleFunction;

//	@Autowired
//	private RoomLogFunction roomLogFunction;
//	
//	@Autowired
//	private DouniuFunction douniuFunction;
	
	@Autowired
	private NiuniuRoomCache niuniuRoomCache;
	@Autowired
	private DZCardFunction dzCardFunction;

	public void gameOnline(Player player) {
		// 太少则虚数
		GMsg_12006003.Builder builder = GMsg_12006003.newBuilder();
		for (Map.Entry<Integer, Integer> entry : roomFunction.getGameOnline()
				.entrySet()) {
			GOnlineData.Builder onlineData = GOnlineData.newBuilder();
			/*
			 * if(entry.getValue()<=5){
			 * onlineData.setNum(MathUtil.randomNumber(5, 20));
			 * onlineData.setType(entry.getKey()); }else{
			 * onlineData.setNum(entry.getValue());
			 * onlineData.setType(entry.getKey()); }
			 */
			int gameType = entry.getKey();
			onlineData.setType(gameType);
			if (GameType.MENJI == gameType) {
				onlineData.setNum(entry.getValue()
						+ RandomPlayerCount.roomCount(gameType));
			}
			if (GameType.DOUDIZHU == gameType) {
				onlineData.setNum(entry.getValue()
						+ RandomPlayerCount.roomCount(gameType));
			}
			if (GameType.MAJIANG == gameType || GameType.ZXMAJIANG == gameType || GameType.CDMAJIANG == gameType) {
				onlineData.setNum(entry.getValue()
						+ RandomPlayerCount.roomCount(gameType));
			}
			
			if(GameType.DEZHOU == gameType){//德州
				onlineData.setNum(entry.getValue()
						+ RandomPlayerCount.roomCount(gameType));
			}
			builder.addOnlineData(onlineData);
		}
		player.write(builder.build());
	}
	
	

	public void joinGame(Player player, int gameType, int roomType) {
		// 还没有达到等级要求
		Role role = player.getRole();

		int realType = roomFunction.getRealType(gameType, roomType);
		// 在游戏中
		if (roomFunction.isInGame(role.getRid())) {
			GMsg_12006001.Builder builder = GMsg_12006001.newBuilder();
			builder.setExpectWaitTime(1);
			player.write(builder.build());
			return;
		}
		// 已在排队队列中
		if (roomFunction.isReadyMatching(role)) {
			GMsg_12006001.Builder builder = GMsg_12006001.newBuilder();
			builder.setExpectWaitTime(1);
			player.write(builder.build());
			return;
		}

		// 预判报名费是否足够
		int entryFee = -1;
		// 钱不够
		if (role.getGold() < entryFee) {
			return;
		}
		
		
		
		// 无法准备游戏
		if (!roomFunction.getReadyToGame(player.getRole(), realType)) {
			return;
		}

		LogUtil.info(player.getRole().getNick() + " acept game:" + realType);
		GMsg_12006001.Builder builder = GMsg_12006001.newBuilder();
		builder.setExpectWaitTime(1);
		player.write(builder.build());
		
		
		Channel channel = NettyClient.getChannel();
		if (channel != null && channel.isRegistered()) {
			// 向中心服发送参加请求
			CMsg_21100001.Builder roomBuilder = CMsg_21100001.newBuilder();
			roomBuilder.setServerId(ServerConfig.serverId);
			roomBuilder.setGameType(gameType);
			roomBuilder.setRoomType(roomType);
			CGameRole.Builder cRole = CGameRole.newBuilder();
			cRole.setRid(role.getRid());
			cRole.setGold(role.getGold());
			cRole.setHead(role.getHead());
			cRole.setLevel(role.getLevel());
			cRole.setNick(role.getNick());
			roomBuilder.setGameRole(cRole);

			NettyClient.write(roomBuilder.build());
		}
		// G_T_C 进入房间日志处理
		// G_T_C 处理房间登录日志
//		if (role != null) {
//			roomLogFunction.dealRoomRoleActiveLog(role, gameType, roomType);
//			roomLogFunction.dealRoomRoleLoginLog(gameType, role,
//					player.getIp(), 0);
//		}
	}

	public void exitTable(Player player) {
		roomFunction.exitTable(player.getRole());
	}

	public void changeTable(final Player player) {
		
	}

	/**
	 * 取消准备
	 * 
	 * @param player
	 */
	public void cancelReady(Player player) {

		boolean flag = roomFunction.cancelReadyToGame(player.getRole());
		if (!flag) {
			Game game = roomFunction.getGameByRole(player.getId());
			if (game == null) {
				GMsg_12006009.Builder builder = GMsg_12006009.newBuilder();
				player.write(builder.build());
			}
		} else {
			GMsg_12006009.Builder builder = GMsg_12006009.newBuilder();
			player.write(builder.build());
		}
	}

	/**
	 * 进入桌子
	 * 
	 * @param player
	 */
	public void enterTable(Player player) {
		GameRole role = roomFunction.getGameRole(player.getRole().getRid());
		Game game = roomFunction.getGameByRole(player.getRole().getRid());
		if (game != null) {
			if (game.getStatus() == GameStatus.RUNNING) {
				if (role != null) {
					Event event = new Event(HandleType.GAME_ENTER, game, role);
					DispatchEvent.dispacthEvent(event);
				} else {
					GMsg_12006005.Builder builder = GMsg_12006005.newBuilder();
					builder.setCurrentSeat(0);
					roleFunction.sendMessageToPlayer(player.getRole().getRid(),
							builder.build());
				}

			} else if (game.getStatus() == GameStatus.WAIT_READY) {
				if (role == null) {
					Event event = new Event(HandleType.GAME_ENTER, game,
							player.getRole());
					DispatchEvent.dispacthEvent(event);
				} else {
					GMsg_12006005.Builder builder = GMsg_12006005.newBuilder();
					builder.setCurrentSeat(0);
					roleFunction.sendMessageToPlayer(player.getRole().getRid(),
							builder.build());
				}
			} else {
				GMsg_12006005.Builder builder = GMsg_12006005.newBuilder();
				builder.setCurrentSeat(0);
				roleFunction.sendMessageToPlayer(player.getRole().getRid(),
						builder.build());
			}

		} else {
			GMsg_12006005.Builder builder = GMsg_12006005.newBuilder();
			builder.setCurrentSeat(0);
			roleFunction.sendMessageToPlayer(player.getRole().getRid(),
					builder.build());
		}
	}
	
}
