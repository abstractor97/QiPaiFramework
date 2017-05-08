package com.yaowan.server.game.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameModule;
import com.yaowan.framework.core.handler.server.ServerExecutor;
import com.yaowan.framework.server.base.handler.GameHandler;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GShop.GMsg_11007006;
import com.yaowan.server.game.service.ShopService;

@Component
public class ShopHandler extends GameHandler {

	@Autowired
	ShopService shopService;

	@Override
	public int moduleId() {
		// TODO Auto-generated method stub
		return GameModule.SHOP;
	}

	@Override
	public void register() {
		// TODO Auto-generated method stub
		// shop表结合后进行统一访问
		addExecutor(6, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11007006 msg = GMsg_11007006.parseFrom(data);
				shopService.buyGoods(player, msg.getGoodsId(), msg.getType(),
						msg.getPayType(), msg.getNum(), msg.getChannel());
			}
		});
	}

}
