/**
 * 
 */
package com.yaowan.server.game.handler;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameModule;
import com.yaowan.framework.core.handler.server.ServerExecutor;
import com.yaowan.framework.server.base.handler.GameHandler;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GGame.GMsg_11006001;
import com.yaowan.protobuf.game.GGame.GMsg_11006003;
import com.yaowan.protobuf.game.GGame.GMsg_11006004;
import com.yaowan.protobuf.game.GGame.GMsg_11006006;
import com.yaowan.protobuf.game.GGame.GMsg_11006010;
import com.yaowan.protobuf.game.GGame.GMsg_11006011;
import com.yaowan.server.game.cross.CrossFunction;
import com.yaowan.server.game.service.GameUpdateNoticeService;
import com.yaowan.server.game.service.RedBagService;
import com.yaowan.server.game.service.RoomService;

/**
 * @author zane
 *
 */
@Component
public class RoomHandler extends GameHandler {

	@Autowired
	private RoomService roomService;
	
	@Autowired
	private GameUpdateNoticeService gameUpdateNoticeService;
	
	@Autowired
	private RedBagService redBagService;
	@Autowired
	private CrossFunction crossFunction;
	
	@Override
	public int moduleId() {
		return GameModule.GAME;
	}

	@Override
	public void register() {
		addExecutor(1, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11006001 msg = GMsg_11006001.parseFrom(data);
				
				if(crossFunction.isCrossGame(msg.getGameType())){
					crossFunction.crossGMsg_11006001(player,msg.getGameType(), data);
					return;
				}
				roomService.joinGame(player, msg.getGameType(),msg.getRoomType());
			}
		});
		
		addExecutor(3, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11006003 msg = GMsg_11006003.parseFrom(data);
				if(crossFunction.isCrossGame(msg.getGameType())){
					crossFunction.crossGMsg_11006003(msg.getGameType(), player.getId(),data);
					return;
				}
				
				roomService.gameOnline(player);
			}
		});
		
		addExecutor(4, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11006004 msg = GMsg_11006004.parseFrom(data);
				
				if(crossFunction.isCrossGame(msg.getGameType())){
					crossFunction.crossGMsg_11006004(msg.getGameType(), player.getId(),data);
					return;
				}
				
				roomService.roomOnline(player,msg.getGameType());
			}
		});
		
		addExecutor(5, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				LogUtil.info("player"+player.getRole().getNick()+"主动请求退出焖鸡");
				roomService.exitTable(player);
			}
		});
		
		addExecutor(6, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				
				int gameType = crossFunction.getJoinedGameType(player.getId());
				if(gameType>0){
					crossFunction.crossGMsg_11006006(player,gameType,data);
					return; 
				}
				// 玩家操作
				roomService.changeTable(player);

			}
		});
		
		addExecutor(8, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				
				// 玩家操作
				roomService.enterTable(player);

			}
		});
		
		addExecutor(9, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				roomService.cancelReady(player);
			}
		});
		
		addExecutor(10, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11006010 msg = GMsg_11006010.parseFrom(data);
				roomService.listDouniuRooms(player, msg.getLevel());
			}
		});
		
		addExecutor(11, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11006011 msg = GMsg_11006011.parseFrom(data);
				gameUpdateNoticeService.getNotice(player, msg.getGameType());
			}
		});
		
		addExecutor(13, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				redBagService.reward(player);
			}
		});
	}
}
