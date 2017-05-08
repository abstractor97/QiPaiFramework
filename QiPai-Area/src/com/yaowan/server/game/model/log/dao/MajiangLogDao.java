package com.yaowan.server.game.model.log.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.database.asyn.AsynContainer;
import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.framework.util.JSONObject;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.protobuf.game.GBaseMahJong.GBillsInfo;
import com.yaowan.protobuf.game.GBaseMahJong.GDetailBillsInfo;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.entity.MajiangLog;
import com.yaowan.server.game.model.struct.ZTMaJongTable;
import com.yaowan.server.game.model.struct.ZTMajiangRole;

/**
 * 麻将日志处理dao
 * 
 * @author G_T_C
 */
@Component
public class MajiangLogDao extends SingleKeyLogDao<MajiangLog, Long> {

	/**
	 * 插入一条日志
	 * 
	 * @author G_T_C
	 * @param majiangLog
	 * @throws Exception
	 */
	public void addLog(MajiangLog majiangLog) {
		AsynContainer.add(getInsertSql(majiangLog));
	}

	/**
	 * 得到log bean
	 * 
	 * @author G_T_C
	 * @param game
	 * @param roleInfos
	 *            玩家流水
	 * @param roleInfoDetail
	 *            玩家流水明细
	 * @param winners
	 *            赢的玩家
	 * @return
	 */
	public MajiangLog getMajiangLog(Game game, String roleInfos,
			String roleInfoDetail, String winners, int aiGold,
			int totalMemberCount, int realPlayerCount) {
		MajiangLog log = new MajiangLog();
		log.setEndTime(TimeUtil.getTime(game.getEndTime()));
		log.setRoomId(game.getRoomId());
		log.setRoleInfo(roleInfos);
		log.setRoleInfoDetail(roleInfoDetail);
		log.setStartTime(TimeUtil.getTime(game.getStartTime()));
		if (winners == null) {
			winners = "流局";
		}
		log.setWinRoles(winners);
		log.setAiGold(aiGold);
		log.setTotalPlayerCount(totalMemberCount);
		log.setRealPlayerCount(realPlayerCount);
		return log;
	}

	/**
	 * 得到role info
	 * 
	 * @author G_T_C
	 * @param ztMajiangRole
	 * @param gold
	 * @param owner
	 * @return
	 */
	private Object[] getRoleInfo(ZTMajiangRole ztMajiangRole, int gold,
			int owner) {
		Role role = ztMajiangRole.getRole().getRole();
		List<Object> list = new ArrayList<>();
		list.add(role.getRid());// 玩家id
		list.add(role.getNick());// 玩家昵称
		int seat = ztMajiangRole.getRole().getSeat();
		list.add(seat);// 座位号
		list.add(ztMajiangRole.getRole().isRobot() ? 1 : 0);// 是否为机器人0为玩家，1为机器人
		list.add(seat == owner ? 1 : 0);// 身份 1为庄家。0为参与玩家
		list.add(gold);// 金币变化
		list.add(ztMajiangRole.getHuType());// 胡的类型
		list.add(JSONObject.encode(ztMajiangRole.getPai()));// 牌
		return list.toArray();
	}

	/**
	 * 得到赢的rid
	 * 
	 * @author G_T_C
	 * @param table
	 * @return
	 */
	public List<Long> getWinRoleList(ZTMaJongTable table) {
		List<Long> list = new ArrayList<>();
		List<Integer> winnerList = table.getWinners();
		if (winnerList != null && !winnerList.isEmpty()) {
			for (Integer i : winnerList) {
				Role role = table.getMembers().get(i - 1).getRole().getRole();
				list.add(role.getRid());
			}
		}
		return list;
	}

	/**
	 * 处理role info 和 detail 组装到list，为了拼接字符串
	 * 
	 * @author G_T_C
	 * @param table
	 * @param roleInfoList
	 * @param roleInfoDetailList
	 */
	public void crateRoleInfoAndDetail(ZTMaJongTable table,
			List<Object[]> roleInfoList, List<String> roleInfoDetailList) {
		for (Map.Entry<Integer, GBillsInfo.Builder> entry : table.getBills()
				.entrySet()) {
			Map<Long, List<Object[]>> detailMap = new HashMap<Long, List<Object[]>>();
			GBillsInfo.Builder billsInfo = entry.getValue();
			ZTMajiangRole zTMajiangRole = table.getMembers().get(
					billsInfo.getSeat() - 1);
			Role role = zTMajiangRole.getRole().getRole();
			List<Object[]> detailList = new ArrayList<>();
			for (int i = 0; i < billsInfo.getDetailBillsInfoList().size(); i++) {
				Object[] details = new Object[3];
				GDetailBillsInfo info = billsInfo.getDetailBillsInfo(i);
				details[0] = info.getWinTimes();// 番数
				details[1] = info.getGoldDetail();// 金币明细
				details[2] = info.getNick();// 玩家昵称
				detailList.add(details);
			}
			detailMap.put(role.getRid(), detailList);
			roleInfoDetailList.add(JSONObject.encode(detailMap));
			roleInfoList.add(getRoleInfo(zTMajiangRole, billsInfo.getGold(),
					table.getOwner()));
		}

	}

	// 统计一把游戏中 全部ai对玩家的盈亏
	public int countAiGold(ZTMaJongTable table) {
		int aiGold = 0;
		for (Map.Entry<Integer, GBillsInfo.Builder> entry : table.getBills()
				.entrySet()) {
			GBillsInfo.Builder billsInfo = entry.getValue();
			ZTMajiangRole zTMajiangRole = table.getMembers().get(
					billsInfo.getSeat() - 1);
			if (zTMajiangRole.getRole().isRobot()) {
				aiGold += billsInfo.getGold();
			}
		
		}
		return aiGold;
	}
}
