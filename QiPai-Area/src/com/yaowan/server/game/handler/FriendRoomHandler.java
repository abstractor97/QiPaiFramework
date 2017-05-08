package com.yaowan.server.game.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameModule;
import com.yaowan.framework.core.handler.server.ServerExecutor;
import com.yaowan.framework.server.base.handler.GameHandler;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_11020001;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_11020002;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_11020003;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_11020004;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_11020005;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_11020009;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_11020011;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_11020013;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_11020016;
import com.yaowan.protobuf.game.GFriendRoom.GMsg_11020017;
import com.yaowan.server.game.service.FriendRoomService;

@Component
public class FriendRoomHandler extends GameHandler {
	
	@Autowired
	private FriendRoomService friendRoomService;
	
	@Override
	public int moduleId() {
		return GameModule.FRIEDNROOM;
	}

	@Override
	public void register() {
		// TODO Auto-generated method stub
		addExecutor(1, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11020001 msg =GMsg_11020001.parseFrom(data);
				friendRoomService.createFriendRoom(player, msg.getGameType(),msg.getNum(),msg.getHighestPowerNum(),msg.getPayType());
			}
		});
		
		// TODO Auto-generated method stub
		addExecutor(2, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11020002 msg =GMsg_11020002.parseFrom(data);
			friendRoomService.joinInFriendRoom(player, msg.getRoomId());
			}
		});		
		
		// TODO Auto-generated method stub
		addExecutor(3, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11020003 msg =GMsg_11020003.parseFrom(data);
			friendRoomService.exitFriendRoom(player, msg.getRoomId());
			}
		});	
		
		// TODO Auto-generated method stub
		addExecutor(4, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
			GMsg_11020004 msg =GMsg_11020004.parseFrom(data);
			friendRoomService.launchVote(player, msg.getRoomId());
			}
		});	
		
		// TODO Auto-generated method stub
		addExecutor(5, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11020005 msg =GMsg_11020005.parseFrom(data);
			friendRoomService.agreeClearRoom(player, msg.getRoomId(), msg.getAgree());
			}
		});	
		
		// TODO Auto-generated method stub
		addExecutor(7, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
			friendRoomService.getRoleIsInfriendRoom(player);
			}
		});	
		
		// TODO Auto-generated method stub
		addExecutor(9, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11020009 msg =GMsg_11020009.parseFrom(data);
			friendRoomService.ownerClearBeforeStart(player, msg.getRoomId());
			}
		});	
		
		// TODO Auto-generated method stub
		addExecutor(11, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11020011 msg =GMsg_11020011.parseFrom(data);
			friendRoomService.playerPrepare(player,msg.getRoomId());
			}
		});	
		
		// TODO Auto-generated method stub
		addExecutor(12, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
			friendRoomService.enterFriendRoom(player);
			}
		});	
		
		// TODO Auto-generated method stub
		addExecutor(13, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11020013 msg =GMsg_11020013.parseFrom(data);
			friendRoomService.getPayType(player,msg.getRoomId());
			}
		});	
		
		// TODO Auto-generated method stub
		addExecutor(16, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11020016 msg =GMsg_11020016.parseFrom(data);
			friendRoomService.ownerAgree(player, msg.getRoomId(), msg.getAgree());
			}
		});	
		
		// TODO Auto-generated method stub
		addExecutor(17, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
			friendRoomService.getParameter(player);
			}
		});		
		
	}
}
