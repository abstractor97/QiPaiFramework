/**
 * 
 */
package com.yaowan.server.game.model.data.dao;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.server.game.model.data.entity.CDMajiangData;

/**
 * @author yangbin
 *
 */
@Component
public class CDMajiangDataDao extends SingleKeyDataDao<CDMajiangData,Long> {
	
	public void resetWeek() {
		executeSql("update chengdu_majiang_data set count_week=0,win_week=0,maxcontinue_week=0,first_week=0,same_cards_week=0,seven_week=0,gang_and_hu_week=0,big_pair_week=0,max_power_week=0,max_win_week=0");
	}
	
	
}
