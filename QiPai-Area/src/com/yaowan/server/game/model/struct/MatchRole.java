package com.yaowan.server.game.model.struct;

import com.yaowan.server.game.model.data.entity.MatchRoleData;

public class MatchRole {
	private MatchRoleData matchRoleData;
	
	public MatchRole(MatchRoleData matchRoleData) {
		this.matchRoleData = matchRoleData;
	}
	
	public MatchRoleData getMatchRoleData() {
		return matchRoleData;
	}
	
	public void setMatchRoleData(MatchRoleData matchRoleData) {
		this.matchRoleData = matchRoleData;
	}
}
