package com.yaowan.server.game.model.data.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.MathUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.data.entity.Npc;
import com.yaowan.server.game.model.log.dao.NpcDispatchLogDao;
import com.yaowan.server.game.model.log.entity.NpcDispatchLog;

/**
 * 机器人的dao
 * 
 * @author G_T_C
 */
@Repository
public class NpcDao extends SingleKeyDataDao<Npc, Long> {
	
	@Autowired
	private NpcDispatchLogDao npcDispatchLogDao;

	/**
	 * 更新身上的金额，钻石，奖券
	 * 
	 * @author G_T_C
	 * @param npcId
	 * @param value
	 * @param i
	 */
	public void updateMoneryPackge(long npcId, int value, int i) {
		String updateName = null;
		switch (i) {
		case 1:// 金币
			updateName = "gold";
			break;

		case 2:// 砖石
			updateName = "diamond";
			break;

		case 3:// 奖券
			updateName = "crystal";
			break;

		}
		StringBuilder sql = new StringBuilder("update npc set ");
		sql.append(updateName).append("=").append(updateName).append("+")
				.append(value).append(" where rid=").append(npcId);
		executeSql(sql.toString());
	}

	/**
	 * 更新输赢
	 * 
	 * @author G_T_C
	 * @param npcId
	 * @param value
	 */
	public void updateGainOrLoss(long npcId, int value) {
		String sql = "update npc set gain_or_loss = gain_or_loss+" + value
				+ " where rid=" + npcId;
		executeSql(sql);
	}

	public Map<Long, Npc> findNpcForMap() {
		/*
		 * int currtime = TimeUtil.time(); int date = TimeUtil.dayBreak(); int
		 * time = currtime - date;
		 */
		String sql = "select * from npc where is_open = 1";
		/*
		 * StringBuilder sql = new
		 * StringBuilder("select * from npc where is_open = 1 and ");
		 * sql.append(
		 * "do_start_time <=").append(time).append(" and do_end_time >"
		 * ).append(time); sql.append(" and do_end_date>").append(date).append(
		 * " order by game_type,room_type");
		 */
		LogUtil.info(sql);
		return findForMap(sql);
	}

	public Map<Long, Npc> findAllNpcMap() {
		String sql = "select * from npc";
		return findForMap(sql);
	}

	public int aiCount() {
		return findNumber("select count(*) from npc", int.class);
	}

	/**
	 * 调度和禁用
	 * 
	 * @author G_T_C
	 * @param ids
	 * @param isOpen
	 * @param map
	 */
	public void updateDispatch(String[] ids, Map<String, Object> map) {
		String theme = (String) map.get("theme");
		String goldString = map.get("gold")+"";
		String[] golds = goldString.split(",");
		int gold = MathUtil.randomNumber(Integer.parseInt(golds[0]),
				Integer.parseInt(golds[1]));
		int gameType = Integer.parseInt(map.get("gameType")+"");
		int roomType = Integer.parseInt(map.get("roomType")+"");
		int sdate = Integer.parseInt( map.get("sdate")+"");
		int stime = Integer.parseInt( map.get("stime")+"");
		int edate = Integer.parseInt( map.get("edate")+"");
		int etime = Integer.parseInt( map.get("etime")+"");
		int serverId = Integer.parseInt(map.get("serverId")+"");
		String creator = map.get("creator")+"";
		int isAllDay = Integer.parseInt(map.get("isAllDay")+"");
		if (isAllDay == 1) {
			stime = 0;
			etime = 24*60*60;
		}
		StringBuilder sql = new StringBuilder();
		sql.append("update npc set is_open =1").append(", gold =").append(gold);
		sql.append(", game_type=").append(gameType);
		sql.append(",room_type=").append(roomType).append(",do_start_date=")
				.append(sdate).append(",do_start_time=").append(stime);
		sql.append(", do_end_date=").append(edate).append(", do_end_time=")
				.append(etime);
		if (ids.length > 1) {
			sql.append(" where rid in (")
					.append(StringUtil.arrayToString(ids, ",")).append(")");
		} else {
			sql.append(" where rid = ").append(ids[0]);
		}
		LogUtil.info(sql.toString());
		executeSql(sql.toString());
		//插入或更新日志
		String logIdString = (String) map.get("logId");
		if(logIdString != null && ! "".equals(logIdString)){
			NpcDispatchLog log = new NpcDispatchLog(StringUtil.arrayToString(ids, ","), goldString, ids.length, isAllDay, stime, etime, sdate, edate, gameType, roomType, theme, creator, serverId);
			log.setId(Long.parseLong(logIdString));
			int createTime = npcDispatchLogDao.findById(log.getId());
			if(createTime != 0){
				log.setCreateTime(createTime);
				log.setIsOpen(1);
				npcDispatchLogDao.update(log);
			}else{
				NpcDispatchLog log2 = new NpcDispatchLog(StringUtil.arrayToString(ids, ","), goldString, ids.length, isAllDay, stime, etime, sdate, edate, gameType, roomType, theme, creator,serverId);
				log.setCreateTime(TimeUtil.time());
				log.setIsOpen(1);
				npcDispatchLogDao.insert(log2);
			}
		}else{
			NpcDispatchLog log = new NpcDispatchLog(StringUtil.arrayToString(ids, ","), goldString, ids.length, isAllDay, stime, etime, sdate, edate, gameType, roomType, theme, creator,serverId);
			log.setCreateTime(TimeUtil.time());
			log.setIsOpen(1);
			npcDispatchLogDao.insert(log);
		}
	}

	/**
	 * 
	 * 
	 * @author G_T_C
	 * @param ids
	 * @return
	 */
	public List<Npc> findByIds(String[] ids) {
		StringBuilder sql = new StringBuilder();
		sql.append(
				"select * from npc ");
		if (ids.length > 1) {
			sql.append(" where rid in (")
					.append(StringUtil.arrayToString(ids, ",")).append(")");
		} else {
			sql.append(" where rid = ").append(ids[0]);
		}
		return findList(sql.toString());
	}

	public void updateIsOpen(String[] ids, int isOpen, String logId) {
		StringBuilder sql = new StringBuilder();
		sql.append("update npc set is_open =").append(isOpen);
		if (ids.length > 1) {
			sql.append(" where rid in (")
					.append(StringUtil.arrayToString(ids, ",")).append(")");
		} else {
			sql.append(" where rid = ").append(ids[0]);
		}
		executeSql(sql.toString());
		if(logId != null && !logId.equals("")){
			npcDispatchLogDao.updateIsOpen(logId, isOpen);
		}
	}

	public void updateLoginStatus( int status) {
		String sql = "update npc set status = "+status+" where is_open=1";
		executeSql(sql);
	}
}
