package com.yaowan.server.game.model.data.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.server.game.model.data.entity.RoleRecommend;

@Component
public class RoleRecommendDao extends SingleKeyDataDao<RoleRecommend, Long>{

	/**
	 * 通过rid找记录
	 * @param rid
	 * @return
	 */
	public List<RoleRecommend> findByRid(long rid) {
		String sql = "select * from role_recommend where rid = " + rid;
		return findList(sql);
	}
	
	/**
	 * 通过推荐码找记录
	 * @param recommend
	 * @return
	 */
	public List<RoleRecommend> findByRecommend(String recommendNum) {
		String sql = "select * from role_recommend where code = '" + recommendNum +"'";
		return findList(sql);
	}
	
	/**
	 * 增加推荐好友和可领取的奖励
	 * @param friendNum
	 * @param canGetReward
	 * @param rid
	 */
	public void addFriendNumAndReward(int friendNum, int canGetReward, long rid) {
		String sql = "update role_recommend set recommendNum = " + friendNum + 
				"canGetReward = " + canGetReward + "where rid = " + rid;
		executeSql(sql);
	}
	
	/**
	 * 增加已经获得的奖励
	 * @param hadGetReward
	 * @param rid
	 */
	public void addHadGetReward(int hadGetReward, long rid) {
		String sql = "update role_recommend set hasGetReward = " + hadGetReward +
				"where rid = " + rid;
		executeSql(sql);
	}
	
	/**
	 * 第二天清空每天的推荐次数
	 */
	public void updateTimes() {
		this.executeSql("UPDATE role_recommend SET times=0");
	}
	
	/**
	 * 查询推荐码有没有使用过
	 */
	public boolean isExistCode(String code) {
		StringBuilder sql = new StringBuilder();
		sql.append("select rid from role_recommend where code = '" + code + "'");
		Long rid = this.findNumber(sql.toString(), Long.class);
		if (rid == null) {
			return false;
		}
		return true;
	}
	
	/**
	 * 查询今天推荐码的使用次数
	 * @param recommendNum
	 * @return
	 */
	public int getTodayTimes(String recommendNum) {
		String sql = "select times from role_recommend where code = '" + recommendNum + "'";
		List<Object[]> list = findColumn(sql);
		int times = 0;
		for(Object[] object : list) {
			times = Integer.parseInt(object[0] + "");
		}
		return times;
	}
	
	public int getUserIsOpen(String recommendNm) {
		String sql = "select is_open from role_recommend where code = '" + recommendNm + "'";
		Integer isOpen = this.findNumber(sql, Integer.class);
		return isOpen;
	}
}
