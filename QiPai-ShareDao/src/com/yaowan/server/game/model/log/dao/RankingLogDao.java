package com.yaowan.server.game.model.log.dao;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.server.game.model.log.entity.RankingLog;

@Component
public class RankingLogDao extends SingleKeyLogDao<RankingLog, Long>{

}
