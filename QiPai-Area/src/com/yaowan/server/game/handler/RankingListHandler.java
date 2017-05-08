package com.yaowan.server.game.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameModule;
import com.yaowan.framework.core.handler.server.ServerExecutor;
import com.yaowan.framework.server.base.handler.GameHandler;
import com.yaowan.model.struct.Player;
import com.yaowan.server.game.service.RankingListService;

/**
 * 排行榜handler
 * 
 * @author G_T_C
 */
@Component
public class RankingListHandler extends GameHandler {

	@Autowired
	private RankingListService rankingListService;

	@Override
	public int moduleId() {
		return GameModule.RANKING_LIST;
	}

	@Override
	public void register() {
		addExecutor(1, 1000, true, new ServerExecutor() {

			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				//GMsg_11018001 msg = GMsg_11018001.parseFrom(data);
				rankingListService.senderMessages(player);
			}
		});
	}
}
