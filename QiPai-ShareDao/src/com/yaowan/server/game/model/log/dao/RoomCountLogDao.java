package com.yaowan.server.game.model.log.dao;

import org.springframework.stereotype.Component;

import com.yaowan.database.asyn.AsynContainer;
import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.log.entity.RoomCountLog;

/**
 * 游戏数据统计
 * @author G_T_C
 */
@Component
public class RoomCountLogDao extends SingleKeyLogDao<RoomCountLog, Long>{
	
	public void addRoomCountLog(RoomCountLog log){
		AsynContainer.add(getInsertSql(log));
	}

	public RoomCountLog findTodayRoomCountLog(byte gameType, byte roomType) {
		int todayStartTime = TimeUtil.dayBreak();
		RoomCountLog log = find("select * from room_count_log where game_type="+ gameType
				+ " and room_type="+roomType+" and create_time between "
				+ todayStartTime + " and " + TimeUtil.time());
		return log;
	}

	public void updateCountLog(RoomCountLog countLog) {
		int todayStartTime = TimeUtil.dayBreak();
		
		executeSql("update  room_count_log set create_time="+countLog.getCreateTime()+",draw_count="+countLog.getDrawCount()+""
				+ ",broken_count="+countLog.getBrokenCount()+",tax="+countLog.getTax()+",difficulty_banker_count="+countLog.getDifficultyBankerCount()
				+",general_player_count="+countLog.getGeneralPlayerCount()+",general_banker_count="+countLog.getGeneralBankerCount()+",difficulty_player_count="+countLog.getDifficultyPlayerCount()
				+ ",choushui="+countLog.getChoushui()
				+" where game_type="+ countLog.getGameType()
				+ " and room_type="+countLog.getRoomType()+" and create_time between "
				+ todayStartTime + " and " + TimeUtil.time());
	}
}