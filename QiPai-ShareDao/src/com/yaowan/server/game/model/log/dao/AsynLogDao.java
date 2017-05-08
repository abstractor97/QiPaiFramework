/**
 * 
 */
package com.yaowan.server.game.model.log.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yaowan.database.asyn.AsynContainer;
import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.server.game.model.log.entity.Money;

/**
 * @author zane
 *
 */
@Component
public class AsynLogDao extends SingleKeyLogDao<Money,Long> {
	
	public AsynLogDao(){
		super();
		AsynContainer.setAsynLogDao(this);
	}
	
	public void executeSql(List<String> list) {
		if (list.size() == 1) {
			executeSql(list.get(0));
		} else {
			executeBatch(list);
		}

	}
		
	
}
