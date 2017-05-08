package com.yaowan.server.game.model.log.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.yaowan.database.asyn.AsynContainer;
import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.framework.util.JSONObject;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.server.game.model.log.entity.MenjiLog;
import com.yaowan.server.game.rule.ZTMenji;

/**
 * 焖鸡日志处理
 * 
 * @author G_T_C
 */
@Component
public class MenjiLogDao extends SingleKeyLogDao<MenjiLog, Long> {

	/**
	 * 异步插入焖鸡日志
	 * 
	 * @author G_T_C
	 * @param menjiLog
	 * @throws Exception
	 */
	public void addLog(MenjiLog menjiLog) {
		AsynContainer.add(getInsertSql(menjiLog));
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
	public MenjiLog getMenjiLog(Game game, String roleInfo, String winRolesId, int aiGold, int totalMemberCount, int realPlayerCount) {
		MenjiLog log = new MenjiLog();
		log.setRoomId(game.getRoomId());
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
	 * @param ztMenjiRole
	 * @param allBetsNum
	 * @return
	 */
	public Object[] getRoleInfo(long rid, String nick, int seat,
			Boolean isRobot, int chip, int gold, List<Integer> pai, int owner) {
		List<Object> list = new ArrayList<>();
		list.add(rid);
		list.add(nick);
		list.add(seat);
		list.add(isRobot ? 1 : 0);// 1是机器人。0是玩家
		list.add(seat == owner ? 1 : 0);// 身份，1为庄家，0为参与玩家
		list.add(gold);// 资金变化多少 赢了对方多少注,资金变化多少，减少了多少注
		list.add(chip);// 投注
		list.add(ZTMenji.getCardType(pai));// 牌型
		list.add(JSONObject.encode(pai));// 当局牌型
		return list.toArray();
	}
}
