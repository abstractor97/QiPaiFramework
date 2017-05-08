/**
 * 
 */
package com.yaowan.server.game.model.data.dao;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.server.game.model.data.entity.MenjiData;

/**
 * @author zane
 *
 */
@Component
public class MenjiDataDao extends SingleKeyDataDao<MenjiData,Long> {
	
	public void resetWeek() {
		executeSql("update menji_data set baozi_week=0,tonghuashun_week=0,tonghua_week=0,shunzi_week=0,win_week=0,max_win_week=0,count_week=0,maxcontinue_week=0");
	}
	
	
}
