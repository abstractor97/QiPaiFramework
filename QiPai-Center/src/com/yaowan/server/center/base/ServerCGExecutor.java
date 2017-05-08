package com.yaowan.server.center.base;

import com.yaowan.framework.core.handler.AbstractLink;
import com.yaowan.framework.core.handler.server.IServerExecutor;
import com.yaowan.model.struct.GameServer;

public abstract class ServerCGExecutor implements IServerExecutor {

	@Override
	public <T extends AbstractLink> void execute(T gameServer, byte[] data) throws Exception {
		// TODO Auto-generated method stub
		doExecute((GameServer) gameServer, data);
	}

	public abstract void doExecute(GameServer gameServer, byte[] data) throws Exception;

}
