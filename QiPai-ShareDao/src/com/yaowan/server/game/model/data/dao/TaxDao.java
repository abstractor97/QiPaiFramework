package com.yaowan.server.game.model.data.dao;

import org.springframework.stereotype.Component;

import com.yaowan.database.asyn.AsynContainer;
import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.data.entity.Tax;
import com.yaowan.server.game.model.log.entity.RoomCountLog;

/**
 * 游戏数据统计
 * @author G_T_C
 */
@Component
public class TaxDao extends SingleKeyDataDao<Tax, Long>{
	
	public void addTax(Tax tax){
		insert(tax);
	}

	public Tax findTax(byte gameType, int roomType) {
		Tax tax = find("select * from tax where game_type="+ gameType
				+ " and room_type="+roomType);
		return tax;
	}

	public void updateTax(Tax tax) {
		
		executeSql("update  tax set tax_count="+tax.getTaxCount()
				+" where game_type="+ tax.getGameType()
				+ " and room_type="+tax.getRoomType());
	}
}