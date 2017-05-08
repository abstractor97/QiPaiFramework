/**
 * 
 */
package com.yaowan.server.game.model.data.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.RankListBean;
import com.yaowan.server.game.model.data.entity.PushBean;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.entity.RoleRemainMoneyLog;

/**
 * @author zane
 *
 */
@Component
public class RoleDao extends SingleKeyDataDao<Role, Long> {

	/**
	 * 根据openId查询角色信息
	 * 
	 * @param openId
	 * @return
	 */
	public long getByOpenId(String openId) {
		StringBuilder sql = new StringBuilder();
		sql.append("select rid from role where open_id = '" + openId + "'");
		Long rid = this.findNumber(sql.toString(), Long.class);
		if (rid == null) {
			return 0;
		}
		return rid;
	}

	/**
	 * 启动时设置所有玩家为离线
	 */
	public void offlineAll() {
		this.executeSql("UPDATE role SET online = 0,last_offline_time="
				+ TimeUtil.time() + " where online=1");
	}

	/**
	 * 昵称是否存在
	 * 
	 * @param name
	 * @return
	 */
	public boolean isExistNick(String nick) {
		StringBuilder sql = new StringBuilder();
		sql.append("select rid from role where nick = '" + nick + "'");
		Long rid = this.findNumber(sql.toString(), Long.class);
		if (rid == null) {
			return false;
		}
		return true;
	}
	
	/**
	 * 通过昵称找rid
	 * 
	 */
	public Long getRidByNick(String nick) {
		StringBuilder sql = new StringBuilder();
		sql.append("select rid from role where nick = '" + nick + "'");
		Long rid = this.findNumber(sql.toString(), Long.class);
		return rid;
	}
	
	/**
	 * 通过id找昵称
	 * 
	 */
	public String getNickdById(long rid) {
		StringBuilder sql = new StringBuilder();
		sql.append("select nick from role where rid = '" + rid + "'");
		List<Object[]> list = findColumn(sql.toString());
		String nick = null;
		for(Object[] object : list) {
			nick = object[0] + "";
		}
		return nick;
	}
	
	/**
	 * 通过推荐码找登录方式
	 */
	public int getLoginTypeByRid(long rid) {
		StringBuilder sql = new StringBuilder();
		sql.append("select login_type from role where rid = " + rid);
		List<Object[]> list = findColumn(sql.toString());
		int type = 0;
		for(Object[] object : list) {
			type = Integer.parseInt(object[0] + "");
		}
		return type;
	}

	/**
	 * 第二天置空所有用户日登录次数信息
	 */
	public void deleteGoldResuceLoginCountInfo() {
		this.executeSql("UPDATE role SET daily_login=0");
	}

	/**
	 * 月初更新用户的签到信息
	 */
	public void deleteSignInfo() {
		this.executeSql("UPDATE role SET max_continue_sign = 0,sign_record='',sign_reward_get=''");
	}

	/*********************************
	 * 排行榜************************************** 一个星期更新重置排行榜信息
	 */
	public void resetRanking() {
		String sql = "update role set bureau_count=0,last_week_money=gold+gold_pot";
		executeSql(sql);
	}

	/*
	 * public void updateVitalityAnd(){ String sql =
	 * "update role set bureau_count=0, change_money=0,last_week_money=total_money"
	 * ; executeSql(sql); }
	 */

	/**
	 * 按类型获取排行榜信息
	 * 
	 * @author G_T_C
	 * @return
	 */
	public List<RankListBean> getThisRanking(int type, int rankLimit) {
		String condition = null;
		switch (type) {
		case 1: {// 赚钱榜
			condition = "change_money";
			break;
		}
		case 2: {// 活跃榜
			condition = "bureau_count";
			break;
		}
		case 3: {// 富豪榜
			condition = "total_money";
			break;
		}
		}
		List<Object[]> list = findColumn("select rid,nick,head,vip_time,(gold+gold_pot) as total_money,(gold+gold_pot-last_week_money) as change_money,bureau_count from role  order by "
				+ condition + " desc limit 0," + rankLimit);
		List<RankListBean> result = new ArrayList<>();
		if (list != null) {
			for (Object[] object : list) {
				RankListBean bean = new RankListBean();
				bean.setName(object[1] + "");
				bean.setRid(Long.parseLong(object[0] + ""));
				bean.setHead(Byte.parseByte(object[2] + ""));
				bean.setIsVip(Integer.parseInt(object[3] + "") > TimeUtil
						.time() ?  1 :  0);
				bean.setTotalMoney(Integer.parseInt(object[4]+""));
				bean.setChangeMoney(Integer.parseInt(object[5]+""));
				bean.setBureauCount(Integer.parseInt(object[6]+""));
				result.add(bean);
			}
		}
		return result;
	}

	public RoleRemainMoneyLog sumRemain() {
		String sql = "SELECT SUM(gold) as gold,SUM(diamond) as diamond, SUM(crystal) as lottery FROM `role`";
		List<Object[]> list = findColumn(sql);
		RoleRemainMoneyLog log = new RoleRemainMoneyLog();
		if(list != null){
			for(Object[] objects :list){
				log.setGold(Long.parseLong(objects[0]+""));
				log.setDiamond(Long.parseLong(objects[1]+""));
				log.setLottery(Long.parseLong(objects[2]+""));
			}
		}
		return log;
	}
	
	/**
	 *  查询连续一定天数没登录的用户
	 * 
	 * 
	 * @return
	 */
	
	public List<PushBean> getUnOnlineRole(int start, int end, int limitstart) {
		List<Object[]> list = findColumn("select device_token, device_type from role where last_login_time >= " + start + " and "
				+ "last_login_time < " + end + " limit " + limitstart + ",500"); 
		List<PushBean> result = new ArrayList<PushBean>();
		if(list != null) {
			for(Object[] objects : list) {
				PushBean push = new PushBean();
				push.setDevice_token(objects[0] + "");
				push.setDevice_type(Integer.parseInt(objects[1] + ""));
				result.add(push);
			}
		}
		return result;
	}
	
	
	
	/**
	 * 
	 * 查询连续一定天数没登录的用户数量
	 *
	 * @param start
	 * @param end
	 * @return
	 */
	
	public int countUnOnlineRole(int start, int end) {
		return findNumber("select count(*) from role where last_login_time > " + start + " and "
				+ "last_login_time < " + end , int.class);
	}
	
	/**
	 * 更新推荐码
	 * @param code
	 * @param rid
	 */
	public void updateRecommendNum(String code, long rid) {
		String sql = "update role set code = '" + code + "' where rid = " + rid ;
		executeSql(sql);
	}
	
	/**
	 * 通过推荐码找rid
	 * @param code
	 * @return
	 */
	public long findRidByCode(String code) {
		String sql = "select rid from role where code = '" + code + "'";
		Long rid = this.findNumber(sql.toString(), Long.class);
		return rid;
	}
	
	/**
	 * 是否被推荐
	 * @param code
	 * @return
	 */
	public int checkIsBeRecommend(long rid) {
		String sql = "select is_recommend from role where rid = " + rid ;
		List<Object[]> list = findColumn(sql);
		int is_Recommend = 0;
		for(Object[] object : list) {
			is_Recommend = Integer.parseInt(object[0] + "");
		}
		return is_Recommend;
	}
	
	/**
	 * 推荐码是否存在
	 * 
	 * @param name
	 * @return
	 */
	public boolean isExistRecommend(String code) {
		StringBuilder sql = new StringBuilder();
		sql.append("select rid from role where code = '" + code + "'");
		Long rid = this.findNumber(sql.toString(), Long.class);
		if (rid == null) {
			return false;
		}
		return true;
	}
}
