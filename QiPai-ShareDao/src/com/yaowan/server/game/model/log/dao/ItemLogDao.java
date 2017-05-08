package com.yaowan.server.game.model.log.dao;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.server.game.model.log.entity.ItemLog;

@Component
public class ItemLogDao extends SingleKeyLogDao<ItemLog,Long> {
	
	public void addItemLog(ItemLog itemLog)
	{
		this.insert(itemLog);
	}
}
