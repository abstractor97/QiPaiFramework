/**
 * Project Name:dfh3_server
 * File Name:PushByTime.java
 * Package Name:push
 * Date:2016年6月2日下午2:35:07
 * Copyright (c) 2016, luguanlin All Rights Reserved.
 *
*/

package com.yaowan.push;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.data.entity.Role;

/**
 * 
 * @author luguanlin 2016年6月2日
 */
@Component
public class PushByTime {


	/**
	 * 挽回提示 :老板，公司的发展离不开你的支持，请回来做出下一步的发展规划吧~
	 * 
	 * @author luguanlin 2016年6月2日
	 */
	public void pushThreeDayOffLine() {
		int now = TimeUtil.time();
		List<Role> roles = null;//ghRole.getValidRole(now - 4 * 24 * 60 * 60, now - 3 * 24 * 60 * 60);
		PushHelper.listcastPush(roles, PushConst.SYSTEM_REDEEM);
	}
}
