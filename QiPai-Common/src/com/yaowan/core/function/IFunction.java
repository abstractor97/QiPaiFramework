/**
 * 
 */
package com.yaowan.core.function;

import com.yaowan.server.game.model.data.entity.Role;



/**
 * @author huangyuyuan
 *
 */
public interface IFunction {
	/**
	 * 服务器启动时对游戏库的处理
	 */
	public void handleOnServerStart();
	/**
	 * 服务器启动时对日志库的处理
	 */
	public void handleOnServerStartLog();
	/**
	 * 在角色登陆的时候处理
	 * @param role
	 */
	public void handleOnRoleLogin(Role role);
	/**
	 * 在角色下线的时候处理
	 * @param role
	 */
	public void handleOnRoleLogout(Role role);
	

	/**
	 * 凌晨时候的处理
	 */
	public void handleOnNextDay() ;
	
	/**
	 * 服务器准备关闭前调用
	 */
	public void handleOnServerShutdown();
}
