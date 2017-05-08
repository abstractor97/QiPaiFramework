/**
 * 
 */
package com.yaowan.server.game.model.log.dao;

import org.springframework.stereotype.Component;

import com.yaowan.database.asyn.AsynContainer;
import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.server.game.model.log.entity.RoleLogin;

/**
 * 登录日志
 * @author zane
 *
 */
@Component
public class RoleLoginDao extends SingleKeyLogDao<RoleLogin,Long> {
	
	public void addRoleLogin(long rid, int time, String ip, String device, byte loginType) {
		RoleLogin roleLogin = new RoleLogin();
		roleLogin.setRid(rid);
		roleLogin.setLoginTime(time);
		roleLogin.setIp(ip);
		roleLogin.setDevice(device);
		roleLogin.setLoginType(loginType);
		AsynContainer.add(getInsertSql(roleLogin));
	}

	
	public void updateOnlineTime(long rid, int time, int onlineTime) {
		executeSql("update role_login set online_time=" + onlineTime
				+ " where rid=" + rid + " and login_time=" + time);
	}

}
