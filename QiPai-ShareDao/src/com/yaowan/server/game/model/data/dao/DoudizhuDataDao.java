/**
 * 
 */
package com.yaowan.server.game.model.data.dao;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.server.game.model.data.entity.DoudizhuData;

/**
 * @author zane
 *
 */
@Component
public class DoudizhuDataDao extends SingleKeyDataDao<DoudizhuData,Long> {
	
	public void resetWeek() {
		executeSql("update doudizhu_data set count_week=0,win_week=0,maxcontinue_week=0,host_week=0,host_win_week=0,farmer_week=0,farmer_win_week=0,bomb_week=0,king_bomb_week=0,high_power_week=0,max_win_week=0");
	}
	
	
}
