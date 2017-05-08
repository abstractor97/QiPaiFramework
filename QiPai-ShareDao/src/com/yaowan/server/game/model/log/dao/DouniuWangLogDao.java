package com.yaowan.server.game.model.log.dao;

import org.springframework.stereotype.Component;

import com.yaowan.database.asyn.AsynContainer;
import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.server.game.model.log.entity.DouniuWangLog;

/**
 * 斗牛坐庄日志处理
 * 
 * @author zane
 */
@Component
public class DouniuWangLogDao extends SingleKeyLogDao<DouniuWangLog, Long> {

	/**
	 * 异步插入斗牛日志
	 * 
	 * @author G_T_C
	 * @param menjiLog
	 * @throws Exception
	 */
	public void addLog(DouniuWangLog log) {
		AsynContainer.add(getInsertSql(log));
	}

	/**
	 * 构造日志的bean
	 * 
	 * @author G_T_C
	 * @param game
	 * @param roomLv
	 * @param roomType
	 * @param rid
	 * @param isAi
	 * @return
	 */
	public DouniuWangLog getDouniuWangLog(Game game, int roomLv, int roomType, long rid, int isAi) {
		DouniuWangLog log = new DouniuWangLog();
		log.setRoomId(game.getRoomId());
		log.setRoomLv(roomLv);
		log.setRoomType(roomType);
		log.setRid(rid);
		log.setIsAi(isAi);
		log.setStartTime(TimeUtil.getTime(game.getStartTime()));
		log.setEndTime(TimeUtil.getTime(game.getEndTime()));
		return log;
	}
}
