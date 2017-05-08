package com.yaowan.server.game.model.data.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.data.entity.ActivityData;

/**
 * 活动中心dao
 * 
 * @author G_T_C
 */
@Component
public class ActivityDao extends SingleKeyDataDao<ActivityData, Long> {

	/**
	 * 按活动开始时间的降序查询领取还没有过期的活动
	 * 
	 * @author G_T_C 
	 * @return
	 */
	public List<ActivityData> findAllStartTimeDesc() {
		int time = TimeUtil.time();
		String sql = "select * from activity_data where reward_expire_time >"+time+" order by start_time desc";
		return findList(sql);
	}

	public void updateclickNum(long aid) {
		String sql = "update activity_data set click_num = click_num+1 where id ="+aid;
		executeSql(sql);
	}
}
