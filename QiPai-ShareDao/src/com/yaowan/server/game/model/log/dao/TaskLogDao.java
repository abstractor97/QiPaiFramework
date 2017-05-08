package com.yaowan.server.game.model.log.dao;

import org.springframework.stereotype.Repository;

import com.yaowan.framework.database.LogDao;
import com.yaowan.server.game.model.log.entity.TaskLog;

/**
 * 任务日志
 * @author G_T_C
 */
@Repository
public class TaskLogDao extends LogDao<TaskLog>{

	public int findCount(long rid, int taskId, int i) {
		String sql = "select count(*) from task_log where rid="+rid+" and task_id="+taskId+" and status="+i;
		return findNumber(sql, int.class);
	}

}
