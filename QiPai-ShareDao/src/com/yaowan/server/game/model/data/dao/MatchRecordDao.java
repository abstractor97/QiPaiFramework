package com.yaowan.server.game.model.data.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.server.game.model.data.entity.MatchRecord;

@Component
public class MatchRecordDao extends SingleKeyDataDao<MatchRecord, Long> {
	public List<MatchRecord> getAllRecored(long rid) {
		StringBuilder sb = new StringBuilder();
		sb.append("select * from game_match_record where rid = ").append(rid);
		return findList(sb.toString());
	}
}
