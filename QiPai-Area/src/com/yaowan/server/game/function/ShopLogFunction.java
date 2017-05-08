package com.yaowan.server.game.function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.server.game.model.log.dao.ShopBuyDao;
import com.yaowan.server.game.model.log.entity.ShopBuyLog;
import com.yaowan.server.game.model.struct.MissionType;
import com.yaowan.server.game.model.struct.TaskType;

@Component
public class ShopLogFunction extends FunctionAdapter {
	
	@Autowired
	ShopBuyDao shopBuyDao;
	
	@Autowired
	private MissionFunction missionFunction;
	
	public void addShopLog(long rid,int itemId, int itemNum, int type,int value,int time,int shopType)
	{
		ShopBuyLog shopBuyLog=new ShopBuyLog();
		shopBuyLog.setItemId(itemId);//物品ID
		shopBuyLog.setRid(rid);//玩家角色id
		shopBuyLog.setItemNum(itemNum);//物品数量
		shopBuyLog.setType(type);//消耗类型
		shopBuyLog.setShopType(shopType);//商店类型
		shopBuyLog.setTime(time);//时间
		shopBuyLog.setValue(value);//消耗类型
		shopBuyDao.addShopBuyLogy(shopBuyLog);
		//检测任务
		missionFunction.checkTaskFinish(rid, TaskType.main_task, MissionType.SHOP_BUY, 1);
	}
}
