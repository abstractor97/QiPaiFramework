package com.yaowan.server.game.model.log.dao;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.server.game.model.log.entity.ShopBuyLog;

@Component
public class ShopBuyDao extends SingleKeyLogDao<ShopBuyLog,Long> {
	
	public void addShopBuyLogy(ShopBuyLog shopBuyLog)
	{
		this.insert(shopBuyLog);
	}
	
	/**
	 * @author G_T_C
	 * @param rid 玩家rid
	 *        time  本周的时间戳
	 * @return 返回玩家一周内购买道具消耗的金币数量
	 */
	 public int getChangeGoldByTime(Long rid , int startTime, int endTime ){
			return findNumber("select sum(value) from shop_buy_log "
					+" where rid="+ rid+ 
					" and time between "+ startTime+" and "+ endTime,Integer.class
					);
	 }
}
