package com.yaowan.server.game.model.data.dao;

import org.springframework.stereotype.Repository;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.server.game.model.data.entity.RoleRedBag;

@Repository
public class RoleRedBagDao extends SingleKeyDataDao<RoleRedBag, Long>{

	/**
	 * 本轮重置
	 * @author G_T_C
	 */
	public void resetEpicycle() {
		String sql = "update role_red_bag set times_num = 0";
		executeSql(sql);
	}
	
	public void updateTimesNum(long rid){
		String sql = "update role_red_bag set times_num = times_num+1,daily_num = daily_num+1 where rid ="+rid;
		executeSql(sql);
	}

}
