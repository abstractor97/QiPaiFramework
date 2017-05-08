/**
 * 
 */
package com.yaowan.server.game.model.log.dao;

import org.springframework.stereotype.Component;

import com.yaowan.database.asyn.AsynContainer;
import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.server.game.model.log.entity.RegisterLog;

/**
 * 创号日志
 * @author zane
 *
 */
@Component
public class RegisterLogDao extends SingleKeyLogDao<RegisterLog,Long> {
	
	public void addRegisterLog(long rid, int time, String nick, String device,
			String ip, String platform, int deviceType) {
		RegisterLog registerLog = new RegisterLog();
		registerLog.setRid(rid);
		registerLog.setDevice(device);
		registerLog.setIp(ip);
		registerLog.setNick(nick);
		registerLog.setPlatform(platform);
		registerLog.setTime(time);
		AsynContainer.add(getInsertSql(registerLog));
	}

}
