package com.yaowan.server.game.model.log.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.yaowan.database.asyn.AsynContainer;
import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.entity.DoudizhuLog;
import com.yaowan.server.game.model.struct.ZTDoudizhuRole;
import com.yaowan.server.game.model.struct.ZTDoudizhuTable;

/**
 * 斗地主日志操作dao
 * 
 * @author G_T_C
 */
@Component
public class DoudizhuLogDao extends SingleKeyLogDao<DoudizhuLog, Long> {

	/**
	 * 插入日志
	 * 
	 * @author G_T_C
	 * @param doudizhuLog
	 * @throws Exception
	 */
	public void addLog(DoudizhuLog doudizhuLog) {
		AsynContainer.add(getInsertSql(doudizhuLog));
	}

	/**
	 * 组装斗地主的日志bean
	 * 
	 * @author G_T_C
	 * @param table
	 * @param winner
	 * @return
	 */
	public DoudizhuLog get(ZTDoudizhuTable table, String rolesId,
			String roleInfo, int aiGold, int totalMemberCount, int realPlayerCount) {
		DoudizhuLog log = new DoudizhuLog();
		Game game = table.getGame();
		log.setRoomId(game.getRoomId());
		log.setEndTime(TimeUtil.getTime(game.getEndTime()));
		log.setRoleInfo(roleInfo);
		log.setStartTime(TimeUtil.getTime(game.getStartTime()));
		log.setWinRoles(rolesId);
		log.setAiGold(aiGold);
		log.setTotalPlayerCount(totalMemberCount);
		log.setRealPlayerCount(realPlayerCount);
		return log;
	}

	/**
	 * 获取一个参赛的角色的信息
	 * 
	 * @author G_T_C
	 * @param doudizhuRole 
	 * @param identity 判断赢的是不是地主
	 * @param gold 结算金额
	 * @return
	 */
	public Object[] getRoleInfo(ZTDoudizhuRole doudizhuRole, boolean identity,
			int gold) {
		Role role = doudizhuRole.getRole().getRole();
		List<Object> list = new ArrayList<>();
		list.add(role.getRid());
		list.add(role.getNick());
		list.add(doudizhuRole.getRole().getSeat());
		list.add(doudizhuRole.getRole().isRobot() ? 1 : 0);// 1是机器人。0是玩家
		list.add(identity ? 1 : 0);// 1为地主，0为农民
		list.add(gold);
		list.add(doudizhuRole.getWangzhaCount());
		list.add(doudizhuRole.getZhadanCount());
		return list.toArray();
	}
}
