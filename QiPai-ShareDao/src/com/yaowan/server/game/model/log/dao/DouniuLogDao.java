package com.yaowan.server.game.model.log.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.yaowan.database.asyn.AsynContainer;
import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.framework.util.JSONObject;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.server.game.model.log.entity.DouniuLog;

/**
 * 斗牛日志处理
 * 
 * @author zane
 */
@Component
public class DouniuLogDao extends SingleKeyLogDao<DouniuLog, Long> {

	/**
	 * 异步插入斗牛日志
	 * 
	 * @author G_T_C
	 * @param menjiLog
	 * @throws Exception
	 */
	public void addLog(DouniuLog log) {
		AsynContainer.add(getInsertSql(log));
	}

	/**
	 * 构造日志的bean
	 * 
	 * @author G_T_C
	 * @param game
	 * @param roleInfo
	 * @param winRolesId
	 * @return
	 */
	public DouniuLog getDouniuLog(Game game, int roomLv, int roomType, String roleInfo, String winRolesId, int aiGold, int totalMemberCount, int realPlayerCount) {
		DouniuLog log = new DouniuLog();
		log.setRoomId(game.getRoomId());
		log.setRoomLv(roomLv);
		log.setRoomType(roomType);
		log.setRoleInfo(roleInfo);
		log.setStartTime(TimeUtil.getTime(game.getStartTime()));
		log.setEndTime(TimeUtil.getTime(game.getEndTime()));
		log.setWinRoles(winRolesId);
		log.setAiGold(aiGold);
		log.setTotalPlayerCount(totalMemberCount);
		log.setRealPlayerCount(realPlayerCount);
		return log;
	}

	/**
	 * 得到单个角色的role info
	 * 
	 * @author G_T_C
	 * @param 
	 * @param 
	 * @return
	 */
	public Object[] getRoleInfo(long rid, String nick, int seat,
			Boolean isRobot, int chip, int gold, List<Integer> pai, boolean isWang) {
		List<Object> list = new ArrayList<>();
		list.add(rid);
		list.add(nick);
		list.add(seat);
		list.add(isRobot ? 1 : 0);// 1是机器人。0是玩家
		list.add(isWang ? 1 : 0);// 身份，1为庄家，0为参与玩家
		list.add(gold);// 资金变化多少 赢了对方多少注,资金变化多少，减少了多少注
		list.add(chip);// 投注
		list.add(JSONObject.encode(pai));// 当局牌型
		return list.toArray();
	}
}
