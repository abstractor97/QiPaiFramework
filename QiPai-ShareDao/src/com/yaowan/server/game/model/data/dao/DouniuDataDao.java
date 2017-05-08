/**
 * 
 */
package com.yaowan.server.game.model.data.dao;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.server.game.model.data.entity.DouniuData;

/**
 * 
 * @author zane
 *
 */
@Component
public class DouniuDataDao extends SingleKeyDataDao<DouniuData,Long> {
	
	public void resetWeek() {
		executeSql("update douniu_data set win_week=0,max_win_week=0,count_week=0,maxcontinue_week=0");
	}
	
	
}
