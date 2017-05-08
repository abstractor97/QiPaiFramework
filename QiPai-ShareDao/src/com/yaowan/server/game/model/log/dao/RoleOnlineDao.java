/**
 * 
 */
package com.yaowan.server.game.model.log.dao;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.server.game.model.log.entity.RoleOnline;

/**
 * 在线日志
 * @author zane
 *
 */
@Component
public class RoleOnlineDao extends SingleKeyLogDao<RoleOnline,Long> {
	
	public void addRoleOnline(int dateTime, int num) {
		RoleOnline roleOnline = new RoleOnline();
		roleOnline.setDateTime(dateTime);
		roleOnline.setNum((short)num);
		insert(roleOnline);
	}

	


}
