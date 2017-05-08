package com.yaowan.server.game.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameModule;
import com.yaowan.framework.core.handler.server.ServerExecutor;
import com.yaowan.framework.server.base.handler.GameHandler;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GChat.GMsg_11008001;
import com.yaowan.protobuf.game.GChat.GMsg_11008002;
import com.yaowan.protobuf.game.GChat.GMsg_11008003;
import com.yaowan.protobuf.game.GChat.GMsg_11008005;
import com.yaowan.server.game.service.ChatService;

@Component
public class ChatHandler extends GameHandler{

	@Autowired
	ChatService chatService;
	
	@Override
	public int moduleId() {
		return GameModule.CHAT;
	}

	@Override
	public void register() {
		// TODO Auto-generated method stub
		addExecutor(1, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11008001 msg =GMsg_11008001.parseFrom(data);
				chatService.sendMessageInRoom(player, msg.getType(), msg.getMessage(),msg.getGame(),msg.getSound(),msg.getSoundLen());
			}
		});
		
		addExecutor(2, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11008002 msg =GMsg_11008002.parseFrom(data);
				chatService.sendMessageInHall(player, msg.getType(), msg.getMessage());;
			}
		});
		
		addExecutor(3, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11008003 msg =GMsg_11008003.parseFrom(data);
				chatService.sendPropMessage(player, msg.getGoodsId(), msg.getSenderSeat(), msg.getTargetSeat(),msg.getItemIndex());
			}
		});
		
		addExecutor(4, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				chatService.sendMessage(player);
			}
		});
		
		addExecutor(5, 1000, true, new ServerExecutor() {

			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11008005 msg = GMsg_11008005.parseFrom(data);
				chatService.addClickNum(player, msg.getAid());
			}
		});
		
	}
	
}
