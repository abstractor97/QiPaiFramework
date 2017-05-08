/**
 * 
 */
package com.yaowan.server.game.model.data.dao;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.server.game.model.data.entity.NCMenjiData;

/**
 * @author zane
 *
 */
@Component
public class NCMenjiDataDao extends SingleKeyDataDao<NCMenjiData,Long> {
	
	public void resetWeek() {
		executeSql("update ncmenji_data set baozi_week=0,tonghuashun_week=0,tonghua_week=0,shunzi_week=0,win_week=0,max_win_week=0,count_week=0,maxcontinue_week=0");
	}
	
	
}
