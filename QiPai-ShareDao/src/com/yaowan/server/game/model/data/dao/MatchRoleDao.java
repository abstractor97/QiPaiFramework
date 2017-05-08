package com.yaowan.server.game.model.data.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.DataDao;
import com.yaowan.server.game.model.data.entity.MatchRoleData;

@Component
public class MatchRoleDao extends DataDao<MatchRoleData> {
	public List<MatchRoleData> getAllRoleDataByMatchId(int matchId) {
		StringBuilder sb = new StringBuilder();
		sb.append("select * from game_match_role where match_id = ").append(matchId);
		return findList(sb.toString());
	}
	
	public void delete(MatchRoleData matchRoleData) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from game_match_role where match_id = ")
			.append(matchRoleData.getMatchId())
			.append(" and rid = ")
			.append(matchRoleData.getRid());
		executeSql(sb.toString());
		return;
	}
	
	public void deleteAllRole(int matchId) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from game_match_role where match_id = ")
			.append(matchId);
		executeSql(sb.toString());
		return;
	}
}
