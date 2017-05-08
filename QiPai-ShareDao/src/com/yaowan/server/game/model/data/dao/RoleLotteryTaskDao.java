package com.yaowan.server.game.model.data.dao;

import org.springframework.stereotype.Repository;

import com.yaowan.framework.database.DataDao;
import com.yaowan.server.game.model.data.entity.RoleLotteryTask;

@Repository
public class RoleLotteryTaskDao extends DataDao<RoleLotteryTask>{

	public RoleLotteryTask findByRidGameType(long rid, int gameType) {
		String sql = "select * from role_lottery_task where rid="+rid+" and game_type=" +gameType ;
		return find(sql);
	}

	public void updateCompletmetTimes(long rid, int gameType) {
		String sql = "update role_lottery_task set complement_times = complement_times+1 where rid="+rid+" and game_type=" +gameType ;
		executeSql(sql);
	}

	public void resetAllTask() {
		truncate();
	}

}
