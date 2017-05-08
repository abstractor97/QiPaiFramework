package com.yaowan.server.game.function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.server.game.model.data.dao.SystemSettingDao;
import com.yaowan.server.game.model.data.entity.SystemSetting;

/**
 * 公共处理函数
 * 
 * @author zane 2016年12月9日 下午2:37:14
 */
@Component
public class CommonFunction extends FunctionAdapter {

	private SystemSetting systemSetting = null;

	@Autowired
	private SystemSettingDao systemSettingDao;

	@Override
	public void handleOnServerStart() {
		systemSetting = systemSettingDao.getSystemSetting();
	}

	public SystemSetting getSystemSetting(){
		return systemSetting;
	}
	public void updateResetInfo(){
		systemSettingDao.updateAllColumn(systemSetting);
	}
}
