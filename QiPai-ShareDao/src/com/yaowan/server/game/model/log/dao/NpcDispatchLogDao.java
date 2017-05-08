package com.yaowan.server.game.model.log.dao;

import org.springframework.stereotype.Repository;

import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.server.game.model.log.entity.NpcDispatchLog;

@Repository
public class NpcDispatchLogDao extends SingleKeyLogDao<NpcDispatchLog, Long>{

	public void updateIsOpen(String logId, int isOpen) {
		StringBuilder sql  = new StringBuilder("update npc_dispatch_log set is_open = ").append(isOpen).append(" where id = ").append(logId);
		executeSql(sql.toString());
	}

	public int findById(long id) {
		StringBuilder sql  = new StringBuilder("select create_time from npc_dispatch_log ").append(" where id = ").append(id);
		return findNumber(sql.toString(), int.class);
	}
	
}
