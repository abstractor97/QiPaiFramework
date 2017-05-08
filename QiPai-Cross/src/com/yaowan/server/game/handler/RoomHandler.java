/**
 * 
 */
package com.yaowan.server.game.handler;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameModule;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.DispatchEvent;
import com.yaowan.framework.core.handler.server.ServerExecutor;
import com.yaowan.framework.server.base.handler.GameHandler;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GGame.GMsg_11006001;
import com.yaowan.protobuf.game.GGame.GMsg_11006004;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.service.RoomService;

/**
 * @author zane
 *
 */
@Component
public class RoomHandler extends GameHandler {

	@Autowired
	private RoomService roomService;

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
				roomService.joinGame(player, msg.getGameType(),msg.getRoomType());
			}
		});
		
		addExecutor(3, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				roomService.gameOnline(player);
			}
		});
		
		addExecutor(4, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11006004 msg = GMsg_11006004.parseFrom(data);
//				roomService.roomOnline(player,msg.getGameType());
				DispatchEvent.dispacthEvent(new Event(HandleType.ROOM_ONLINE, player,msg.getGameType()));
			}
		});

		
		addExecutor(6, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				
				
				DispatchEvent.dispacthEvent(new Event(HandleType.GAME_CHANGE_TABLE,player));
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
		
		
		
	}
}
