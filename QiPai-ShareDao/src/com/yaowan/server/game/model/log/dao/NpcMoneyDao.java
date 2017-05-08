package com.yaowan.server.game.model.log.dao;

import org.springframework.stereotype.Repository;

import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.server.game.model.log.entity.NpcMoney;

@Repository
public class NpcMoneyDao extends SingleKeyLogDao<NpcMoney,Long>{
	
}
