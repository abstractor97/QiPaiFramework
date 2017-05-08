package com.yaowan.server.game.model.data.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.DataDao;
import com.yaowan.server.game.model.data.entity.SystemSetting;

@Component
public class SystemSettingDao extends DataDao<SystemSetting> {

	/**
	 * 
	 * @return
	 */
	public SystemSetting getSystemSetting() {
		List<SystemSetting> list = this.findAll();
		if (list.isEmpty()) {
			SystemSetting t = new SystemSetting();
			this.insert(t);
			return t;
		}
		return list.get(0);
	}
	
}
