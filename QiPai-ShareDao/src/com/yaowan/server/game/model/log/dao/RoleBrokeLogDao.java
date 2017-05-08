package com.yaowan.server.game.model.log.dao;

import org.springframework.stereotype.Repository;

import com.yaowan.database.asyn.AsynContainer;
import com.yaowan.framework.database.LogDao;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.log.entity.RoleBrokeLog;

/**
 * 
 * @author G_T_C
 */
@Repository
public class RoleBrokeLogDao extends LogDao<RoleBrokeLog>{

	/**
	 * 
	 * @author G_T_C
	 * @param rid
	 * @param gameType
	 * @param roomType
	 * @param time
	 */
	public void insertLog(long rid, int gameType, int roomType) {
		RoleBrokeLog log = new RoleBrokeLog();
		log.setRid(rid);
		log.setGameType(gameType);
		log.setRoomType(roomType);
		log.setTime(TimeUtil.time());
		AsynContainer.add(getInsertSql(log));
	}
	
}
