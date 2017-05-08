package com.yaowan.server.game.model.log.dao;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.server.game.model.log.entity.GiftLog;

/**
 * @author lijintao
 * 2017年2月24日
 */
@Component
public class GiftLogDao extends SingleKeyLogDao<GiftLog,Long> {
	
	public void addGiftLog(GiftLog giftLog)
	{
		this.insert(giftLog);
	}
}
