package com.yaowan.server.game.model.log.dao;

import org.springframework.stereotype.Component;

import com.yaowan.database.asyn.AsynContainer;
import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.log.entity.RoomRoleOnline;

/**
 * 
 * @author G_T_C
 */
@Component
public class RoomRoleOnlineDao extends SingleKeyLogDao<RoomRoleOnline, Long> {

	/**
	 * 添加 游戏房间在线人数 日志
	 * 
	 * @author G_T_C
	 * @param gameType
	 * @param num
	 * @param roomType
	 */
	public void addRoomRoleLineLog(int gameType, int num) {
		RoomRoleOnline log = new RoomRoleOnline();
		log.setDateTime(TimeUtil.time());
		log.setGameType(gameType);
		log.setNum(num);
		AsynContainer.add(getInsertSql(log));
	}
}
