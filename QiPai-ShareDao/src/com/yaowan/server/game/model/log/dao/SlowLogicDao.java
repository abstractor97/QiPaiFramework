/**
 * 
 */
package com.yaowan.server.game.model.log.dao;

import org.springframework.stereotype.Component;

import com.yaowan.database.asyn.AsynContainer;
import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.log.entity.SlowLogic;

/**
 * @author zane
 *
 */
@Component
public class SlowLogicDao extends SingleKeyLogDao<SlowLogic,Long> {
	
	private void addSlowLogic(SlowLogic slowLogic) {
		AsynContainer.add(getInsertSql(slowLogic));
	}

	/**
	 * 添加慢逻辑日志
	 * @param action
	 * @param startTime
	 * @param exeTime
	 * @param rid
	 */
	public void addSlowLogic(String action, long startTime, int exeTime, long rid) {
		SlowLogic slowLogic = new SlowLogic();
		slowLogic.setAction(action);
		slowLogic.setStartTime(startTime);
		slowLogic.setRid(rid);
		slowLogic.setExeTime(exeTime);
		addSlowLogic(slowLogic);
	}
	
	/**
	 * 添加定时器执行日志
	 * 
	 * @param object
	 */
	public void addTimerLog(String className, long times) {
		if (times > 10) {
			addSlowLogic(className, TimeUtil.time(), (int) times, 0);
		}
	}
}
