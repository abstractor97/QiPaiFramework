package com.yaowan.server.game.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GMatch.ReqApplyType;
import com.yaowan.server.game.function.MatchFunction;


@Component
public class MatchService {
	
	@Autowired
	private MatchFunction matchFunction;
	
	public void handleGetMatchList(Player player) {
		matchFunction.getMatchList(player);
	}
	
	public void handleApplyMatch(Player player, ReqApplyType reqType, int matchId) {
		matchFunction.applyMatch(player, reqType, matchId);
	}
	
	public void handleGetMatchRecord(Player player) {
		matchFunction.getMatchRecord(player);
	}
}
