package com.yaowan.server.game.model.log.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.database.asyn.AsynContainer;
import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.log.entity.RoomRoleLogin;

/**
 * 角色登录房间日志
 * 
 * @author G_T_C
 */
@Component
public class RoomRoleLoginDao extends SingleKeyLogDao<RoomRoleLogin, Long> {

	@Autowired
	private ChargeDao chargeDao;

	public void addRoomRoleLoginLog(byte gameType, String ip, int loginTime,
			int onlineTime, long rid, byte todayCharge, byte userVip) {
		RoomRoleLogin log = new RoomRoleLogin();
		log.setGameType(gameType);
		log.setIp(ip);
		log.setLoginTime(loginTime);
		log.setOnlineTime(onlineTime);
		log.setRid(rid);
		log.setTodayCharge(todayCharge);
		log.setUserVip(userVip);
		AsynContainer.add(getInsertSql(log));
	}

	/**
	 * 查询当天是否有登录过房间
	 * 
	 * @author G_T_C
	 * @param gameType
	 * @param rid
	 * @return
	 */
	public RoomRoleLogin findTodayRoomLoginLog(int gameType, long rid) {
		int todayStartTime = TimeUtil.dayBreak();
		RoomRoleLogin log = find("select * from room_role_login where game_type="
				+ gameType
				+ " and rid="
				+ rid
				+ " and login_time between "
				+ todayStartTime + " and " + TimeUtil.time());
		return log;
	}

	/**
	 * 更新当天的时长和登录时间，是否有充值和ip更新
	 * 
	 * @author G_T_C
	 * @param log
	 */
	public void updateRoomRoleLog(RoomRoleLogin log) {
		int todayStartTime = TimeUtil.dayBreak();
		executeSql("update room_role_login set login_time="
				+ log.getLoginTime() + ",online_time=" + log.getOnlineTime()
			    + ",today_charge=" + log.getTodayCharge() + ",ip='"
				+ log.getIp() + "' where game_type=" + log.getGameType()
				+ " and rid=" + log.getRid() + " and login_time between "
				+ todayStartTime + " and " + TimeUtil.time());
	}

	/**
	 * 更新登陆时间
	 * 
	 * @author G_T_C
	 * @param log
	 */
	public void updateRoomRoleLoginTime(RoomRoleLogin log) {
		int todayStartTime = TimeUtil.dayBreak();
		executeSql("update room_role_login set login_time="
				+ log.getLoginTime() + "" + ",ip='" + log.getIp()
				+ "' where game_type=" + log.getGameType() + " and rid="
				+ log.getRid() + " and login_time between " + todayStartTime
				+ " and " + TimeUtil.time());
	}
}
