package com.yaowan.server.game.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameModule;
import com.yaowan.framework.core.handler.server.ServerExecutor;
import com.yaowan.framework.server.base.handler.GameHandler;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GExchange.GMsg_11017002;
import com.yaowan.protobuf.game.GExchange.GMsg_11017004;
import com.yaowan.server.game.service.ExchangeService;

@Component
public class ExchangeHandler extends GameHandler{

	@Autowired
	ExchangeService exchangeService;
	
	@Override
	public int moduleId() {
		return GameModule.EXCHANGE;
	}

	@Override
	public void register() {
		addExecutor(1, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				exchangeService.readExchangeItem(player);
			}
			
		});
		
		addExecutor(2, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11017002 msg = GMsg_11017002.parseFrom(data);
				exchangeService.exchangeItem(player,msg.getGoodsId(),msg.getQuantity());
			}
			
		});
		
	addExecutor(3, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				exchangeService.readExchangeLog(player);
			}
		});
		
		addExecutor(4, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11017004 msg = GMsg_11017004.parseFrom(data);
				exchangeService.useRechargeCard(player, msg.getPackId(), msg.getPhone());
			}
		});
	}

}
