/**
 * 
 */
package com.yaowan.server.game.model.data.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.server.game.model.data.entity.Friend;

/**
 * @author zane
 *
 */
@Component
public class FriendDao extends SingleKeyDataDao<Friend,Long> {
	
	
	/**
	 * 获取好友关系
	 * 
	 * @param 
	 * @return
	 */
	public Friend getFriend(long rid1,long rid2) {
		StringBuilder sql = new StringBuilder();
		sql.append("select * from friend where rid1 = ");
		sql.append(rid1);
		sql.append(" and rid2=");
		sql.append(rid2);
		return this.find(sql.toString());
	}
	
	/**
	 * 获取好友关系
	 * 
	 * @param openId
	 * @return
	 */
	public List<Long> getFriends(long rid) {
		StringBuilder sql = new StringBuilder();
		sql.append("select rid2 from friend where rid1 = ");
		sql.append(rid);
		sql.append(" and status=2");
		return this.findIdList(sql.toString());
	}
	
	/**
	 * 获取好友申请关系
	 * 
	 * @param openId
	 * @return
	 */
	public List<Long> getAsks(long rid) {
		StringBuilder sql = new StringBuilder();
		sql.append("select rid2 from friend where rid1 = ");
		sql.append(rid);
		sql.append(" and status like '%2%'");
		return this.findIdList(sql.toString());
	}
	
	/**
	 * 获取被申请关系
	 * 
	 * @param openId
	 * @return
	 */
	public List<Long> getApplys(long rid) {
		StringBuilder sql = new StringBuilder();
		sql.append("select rid2 from friend where rid1 = ");
		sql.append(rid);
		sql.append(" and status like '%1%'");
		return this.findIdList(sql.toString());
	}
	
	
	/**
	 * 成为好友
	 * 
	 * @param 
	 * @return
	 */
	public boolean agreeFriend(long rid1,long rid2) {
		StringBuilder sql = new StringBuilder();
		sql.append("update friend set status='2' where rid1 = ");
		sql.append(rid1);
		sql.append(" and rid2=");
		sql.append(rid2);
		sql.append(" or (rid2=");
		sql.append(rid1);
		sql.append(" and rid1=");
		sql.append(rid2);
		sql.append(")");
		return executeSql(sql.toString());
	}
	
	
	/**
	 * 删除好友
	 * 
	 * @param 
	 * @return
	 */
	public boolean deleteFriend(long rid1,long rid2) {
		StringBuilder sql = new StringBuilder();
		sql.append("delete from friend where rid1 = ");
		sql.append(rid1);
		sql.append(" and rid2=");
		sql.append(rid2);
		sql.append(" or (rid2=");
		sql.append(rid1);
		sql.append(" and rid1=");
		sql.append(rid2);
		sql.append(")");
		return executeSql(sql.toString());
	}
	
}
