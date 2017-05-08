package com.yaowan.server.game.model.data.dao;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.server.game.model.data.entity.MatchData;

@Component
public class MatchDao extends SingleKeyDataDao<MatchData, Long> {
	
}
