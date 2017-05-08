package com.yaowan.server.game.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameModule;
import com.yaowan.framework.core.handler.server.ServerExecutor;
import com.yaowan.framework.server.base.handler.GameHandler;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GItem.GMsg_11005002;
import com.yaowan.protobuf.game.GItem.GMsg_11005003;
import com.yaowan.server.game.service.ItemService;

@Component
public class ItemHandler extends GameHandler {
	
	@Autowired
	private ItemService itemService;
	
	@Override
	public int moduleId() {
		return GameModule.ITEM;
	}

	@Override
	public void register() {
		// TODO Auto-generated method stub
		addExecutor(1, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				itemService.listPackItem(player);
			}
		});
		
		addExecutor(2, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11005002 msg = GMsg_11005002.parseFrom(data);
				itemService.useItem(player, msg.getId());
			}
		});
		
		addExecutor(3, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11005003 msg = GMsg_11005003.parseFrom(data);
				itemService.giveUpItem(player, msg.getId(), msg.getNum());
			}
		});
				
	}
}
