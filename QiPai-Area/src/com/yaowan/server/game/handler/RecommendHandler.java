package com.yaowan.server.game.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameModule;
import com.yaowan.framework.core.handler.server.ServerExecutor;
import com.yaowan.framework.server.base.handler.GameHandler;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GRecommend.GMsg_11023002;
import com.yaowan.protobuf.game.GRecommend.GMsg_11023003;
import com.yaowan.server.game.service.RecommendService;

@Component
public class RecommendHandler extends GameHandler{

	@Autowired
	private RecommendService recommendService;
	
	@Override
	public int moduleId() {
		// TODO Auto-generated method stub
		return GameModule.bind;
	}

	@Override
	public void register() {
		// TODO Auto-generated method stub
		addExecutor(1, 1000, true, new ServerExecutor() {
			
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				// TODO Auto-generated method stub
				recommendService.recommendList(player);
			}
		});
		
		addExecutor(2, 1000, true, new ServerExecutor() {
			
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				// TODO Auto-generated method stub
				GMsg_11023002 msg = GMsg_11023002.parseFrom(data);
				recommendService.doResult(player, msg.getRecommendNumber());
			}
		});
		
		addExecutor(3, 1000, true, new ServerExecutor() {
			
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				// TODO Auto-generated method stub
				recommendService.doGetMoney(player);
			}
		});
	}

	
}
