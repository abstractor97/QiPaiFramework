/**
 * 
 */
package com.yaowan.core.function;

import java.util.HashMap;
import java.util.Map;

import com.yaowan.framework.util.LogUtil;
import com.yaowan.server.game.model.data.entity.Role;

/**
 * @author huangyuyuan
 *
 */
public class FunctionManager {

	private static Map<Class<? extends IFunction>, IFunction> functionMap = new HashMap<Class<? extends IFunction>, IFunction>();

	public static void register(IFunction function) {
		if (functionMap.containsKey(function.getClass())) {

			throw new RuntimeException("Duplicate function register "
					+ function.getClass().getSimpleName());
		}
		functionMap.put(function.getClass(), function);
	}

	/**
	 * 处理服务器启动
	 */
	public static void handleOnServerStart() {
		doHandleOnServerStart();
		doHandleOnServerStartLog();
	}

	/**
	 * 处理服务器启动时对游戏类的处理
	 */
	private static void doHandleOnServerStart() {
		try {
			for (IFunction handler : functionMap.values()) {
				handler.handleOnServerStart();
				System.out.println("doHandleOnServerStart "
						+ handler.getClass().getSimpleName());
			}
		} catch (Exception e) {
			LogUtil.error(e);
		}
	}

	/**
	 * 处理服务器启动时对日志类的处理
	 */
	private static void doHandleOnServerStartLog() {
		try {
			for (IFunction handler : functionMap.values()) {
				handler.handleOnServerStartLog();
			}
		} catch (Exception e) {
			LogUtil.error(e);
		}
	}

	public static void doHandleOnRoleLogin(Role role) {
		try {
			for (IFunction handler : functionMap.values()) {
				handler.handleOnRoleLogin(role);
			}
		} catch (Exception e) {
			LogUtil.error(e);
		}
	}

	public static void doHandleOnRoleLogout(Role role) {
		if(role == null ){
			return;
		}
		for (IFunction handler : functionMap.values()) {
			try {

				handler.handleOnRoleLogout(role);
			} catch (Exception e) {
				LogUtil.error(e);
			}
		}
	}

	public static void doHandleOnNextDay() {
		try {
			for (IFunction handler : functionMap.values()) {
				handler.handleOnNextDay();
			}
		} catch (Exception e) {
			LogUtil.error(e);
		}
	}
}
