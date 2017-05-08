package com.yaowan.server.game.model.data.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.DataDao;
import com.yaowan.server.game.model.data.entity.ActivityRole;
import com.yaowan.server.game.model.struct.Activity;

/**
 * 游戏活动玩家参与的活动
 * 
 * @author G_T_C
 */
@Component
public class ActivityRoleDao extends DataDao<ActivityRole> {

	public List<ActivityRole> findByRidAid(long rid,
			Map<Integer, Activity> activityCacheMap) {
		if (null != activityCacheMap && !activityCacheMap.isEmpty()) {
			StringBuilder builder = new StringBuilder("(");
			Set<Integer> ids = activityCacheMap.keySet();
			int size = ids.size();
			int i = 0;
			for (Integer id : ids) {
				long aid = activityCacheMap.get(id).getActivityData().getId();
				builder.append(aid);
				if (i == size - 1) {
					builder.append(")");
				} else {
					builder.append(",");
				}
				i++;
			}
			String sql = "select * from activity_role where rid=" + rid
					+ " and aid in " + builder.toString();
			return findList(sql);
		} else {
			return null;
		}

	}

	public void update(ActivityRole activityRole) {
		update(activityRole);
	}

	public void resetEveryDayActivity(long aid,String giftInfo) {
			String sql = "update activity_role set gift_info="+giftInfo+",process=1 where aid ="+aid;
			executeSql(sql);
	}

	private String getInString(Collection<Long> list) {
		if (null != list && !list.isEmpty()) {
			StringBuilder builder = new StringBuilder("(");
			int size = list.size();
			int i = 0;
			for (Long value : list) {
				builder.append(value);
				if (i == size - 1) {
					builder.append(")");
				} else {
					builder.append(",");
				}
				i++;
			}
			return builder.toString();
		} else {
			return null;
		}
	}
	

	public void insertByList(List<ActivityRole> activityRoles) {
		insertAll(activityRoles);
	}

	public void updateProcessAndGiftInfo(ActivityRole activityRole) {
		String sql = "update activity_role set gift_info="+activityRole.getGiftInfo()+",process="+activityRole.getProcess()+" where aid ="+activityRole.getAid();
		executeSql(sql);
	}

}
