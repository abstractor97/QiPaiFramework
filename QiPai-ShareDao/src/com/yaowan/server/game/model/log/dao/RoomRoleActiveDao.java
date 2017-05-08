package com.yaowan.server.game.model.log.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.database.asyn.AsynContainer;
import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.log.entity.RoomRoleActive;

/**
 * 
 * @author G_T_C
 */
@Component
public class RoomRoleActiveDao extends SingleKeyLogDao<RoomRoleActive, Long> {
	
	Map<Long,RoomRoleActive> cacheMap = new HashMap<Long, RoomRoleActive>();

	public void addRoomRoleActiveLog(byte gameType, long rid, int roomType) {
		RoomRoleActive log = new RoomRoleActive();
		log.setGameType(gameType);
		log.setJoinTime(TimeUtil.time());
		log.setRid(rid);
		log.setRoomType(roomType);
		cacheMap.put(rid, log);
	}

	/**
	 * 按游戏类型和房间类型和角色id查询
	 * 
	 * @author G_T_C
	 * @param gameType
	 * @param rid
	 * @param roomType
	 * @return
	 */
	public RoomRoleActive getRoomRoleLog(long rid) {
		return cacheMap.get(rid);
	}

	/**
	 * 更新
	 * 
	 * @author zane
	 * @param gameType
	 * @param rid
	 * @param roomType
	 * @param onlineTime
	 */
	public void updateMatchTime(long rid) {
		RoomRoleActive roomRoleActive = cacheMap.get(rid);
		if (roomRoleActive != null) {
			roomRoleActive.setMatchTime(TimeUtil.time()
					- roomRoleActive.getJoinTime());
			AsynContainer.add(getInsertSql(roomRoleActive));
		}

	}

	/**
	 * 获取玩家一周的局数
	 * 
	 * @author G_T_C
	 * @param rid
	 * @return
	 */
	public int getCountByTime(long rid, int startTime, int endTime) {
		String sql = "select count(*) from room_role_active where rid=" + rid +" and join_time between "+ startTime +" and "+ endTime;
		return findNumber(sql, Integer.class);
	}
}
