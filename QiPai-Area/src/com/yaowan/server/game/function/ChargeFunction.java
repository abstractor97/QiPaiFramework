package com.yaowan.server.game.function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.log.dao.ChargeDao;
import com.yaowan.server.game.model.log.entity.Charge;

@Component
public class ChargeFunction {
	
	@Autowired
	ChargeDao chargeDao;
	
	/**
	 * 添加一个订单记录(sdk)，状态为0
	 * @param rid
	 * @param type
	 * @param rmb
	 * @param status
	 * @param value
	 * @param orderId
	 */
	public void addChargeLog(String id,long rid,int type,int rmb,byte status,int value,int orderId,int channel,String platform,int serverId,String u8OrderId)
	{
		Charge charge=new Charge();
		charge.setId(id);
		charge.setRid(rid);
		charge.setType(type);
		charge.setRmb(rmb);
		charge.setStatus((byte) 0);
		charge.setValue(value);
		charge.setOrderId(orderId);
		charge.setChannel(channel);
		charge.setPlatform(platform);
		charge.setServerId(serverId);
		charge.setTime(TimeUtil.time());
		charge.setU8OrderId(u8OrderId);
		chargeDao.addChargeLog(charge);
	}
	
	public void updateCharge(Charge charge)
	{
		charge.setStatus((byte) 1);
		charge.markToUpdate("status");
		chargeDao.updateProperty(charge);
	}
		
	
}
