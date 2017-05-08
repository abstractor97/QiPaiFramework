/**
 * 
 */
package com.yaowan.server.game.model.data.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.csv.cache.MissionCache;
import com.yaowan.framework.database.DataDao;
import com.yaowan.framework.database.orm.ResultCallBack;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.data.entity.TaskDaily;



/**
 * 
 * @author zane
 *
 */
@Component
public class TaskDailyDao extends DataDao<TaskDaily> {
	@Autowired
	private MissionCache missionCache;


	/**玩家当天的任务列表*/
	public Map<Integer, TaskDaily> getTodayTaskList(long rid, int type) {
		
		int ymd = TimeUtil.getTodayYmd();
		StringBuilder sql = new StringBuilder();
		sql.append("select * from task_daily where type=").append(type).append(" and rid =");//and ymd= "+ ymd
		sql.append(rid);
		final Map<Integer, TaskDaily> map = new HashMap<>();
		
		this.find(sql.toString(), new ResultCallBack() {
			@Override
			public void onResult(ResultSet rs) throws SQLException {
				while (rs.next()) {
					TaskDaily taskDaily = formObject(rs);
					map.put(taskDaily.getTaskId(), taskDaily);
				}
			}
		});
		return map;
	}
	
	public void updateTaskDaily(long rid, int taskId, int process, int reward) {

		StringBuilder sql = new StringBuilder();
		sql.append("update task_daily set process=").append(process)
				.append(",reward=").append(reward).append(" where rid = ")
				.append(rid).append(" and task_id = ").append(taskId);
		executeSql(sql.toString());
	}

	public void resetAllTask() {
		StringBuilder sb = new StringBuilder("delete from task_daily where type =2");
		executeSql(sb.toString());
	}

	public void resetRoleTask(long rid) {
		StringBuilder sql = new StringBuilder();
		sql.append("delete from task_daily where type=2 and rid = ").append(rid);
		executeSql(sql.toString());
	}

}
