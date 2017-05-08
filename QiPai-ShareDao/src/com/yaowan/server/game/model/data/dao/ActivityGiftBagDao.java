package com.yaowan.server.game.model.data.dao;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.server.game.model.data.entity.ActivityGiftBag;

/**
 * 游戏活动礼包
 * @author G_T_C
 */
@Component
public class ActivityGiftBagDao extends SingleKeyDataDao<ActivityGiftBag, Long>{
	
	public Map<Long,ActivityGiftBag> findByAid(long aid) {
		String sql = "select * from activity_gift_bag where aid="+aid;
		return findForMap(sql);
	}

}
