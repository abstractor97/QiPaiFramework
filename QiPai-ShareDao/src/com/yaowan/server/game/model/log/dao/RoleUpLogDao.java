package com.yaowan.server.game.model.log.dao;

import org.springframework.stereotype.Component;

import com.yaowan.database.asyn.AsynContainer;
import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.entity.RoleUpLog;

/**
 * 角色升级日志操作
 * 
 * @author G_T_C
 */
@Component
public class RoleUpLogDao extends SingleKeyLogDao<RoleUpLog, Long> {

	public void addRoleUpLog(Role role) {
		RoleUpLog log = new RoleUpLog();
		log.setNick(role.getNick());
		log.setRid(role.getRid());
		log.setTime(TimeUtil.time());
		log.setUpToLevel(role.getLevel());
		AsynContainer.add(getInsertSql(log));
	}
}
