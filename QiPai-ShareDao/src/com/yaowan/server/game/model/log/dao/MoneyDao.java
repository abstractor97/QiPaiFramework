/**
 * 
 */
package com.yaowan.server.game.model.log.dao;

import org.springframework.stereotype.Component;

import com.yaowan.database.asyn.AsynContainer;
import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.server.game.model.log.entity.Money;

/**
 * @author zane
 *
 */
@Component
public class MoneyDao extends SingleKeyLogDao<Money,Long> {
	
	public void addMoney(Money money) {
		AsynContainer.add(getInsertSql(money));
	}

}
