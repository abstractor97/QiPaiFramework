package com.yaowan.core.function;

import com.yaowan.server.game.model.data.entity.Role;



/**
 * 公用逻辑处理
 *
 */
public class FunctionAdapter implements IFunction {

	public FunctionAdapter() {
		FunctionManager.register(this);
	}
	
	@Override
	public void handleOnServerStart() {
		//default handle nothing
	}
	
	@Override
	public void handleOnServerStartLog() {
		//default handle nothing
	}

	@Override
	public void handleOnRoleLogin(Role role) {
		//default handle nothing
	}

	@Override
	public void handleOnRoleLogout(Role role) {
		//default handle nothing
	}
	
	@Override
	public void handleOnNextDay() {
		//default handle nothing
		
	}

	@Override
	public void handleOnServerShutdown() {
		// TODO Auto-generated method stub
		
	}
}
