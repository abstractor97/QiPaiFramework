package com.yaowan.server.game.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameModule;
import com.yaowan.framework.core.handler.server.ServerExecutor;
import com.yaowan.framework.server.base.handler.GameHandler;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GMatch.GMsg_11030002;
import com.yaowan.server.game.service.MatchService;

@Component
public class MatchHandler extends GameHandler {
	
	@Autowired
	private MatchService matchService;
	
	@Override
	public int moduleId() {
		return GameModule.MATCH;
	}

	@Override
	public void register() {
		addExecutor(1, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				matchService.handleGetMatchList(player);
			}
		});
		
		addExecutor(2, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11030002 msg = GMsg_11030002.parseFrom(data);
				matchService.handleApplyMatch(player, msg.getReqType(), msg.getMatchId());
			}
		});
		
		addExecutor(9, 500, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				matchService.handleGetMatchRecord(player);
			}
		});
	}
}
