package com.yaowan.server.game.model.log.dao;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.log.entity.Charge;

@Component
public class ChargeDao extends SingleKeyLogDao<Charge,String> {
	
	public void addChargeLog(Charge charge)
	{
		this.insert(charge);
	}
	
	public Charge getChargeLogByOrderId(long orderId)
	{
		return this.find("select * from charge where orderId="+orderId);
	}
	
	
	public Charge getTodayChargeByRid(long rid){
		int todayStartTime = TimeUtil.dayBreak();
		return find("select * from charge where rid="+rid+" and time between "
				+ todayStartTime + " and " + TimeUtil.time());
	}

}
