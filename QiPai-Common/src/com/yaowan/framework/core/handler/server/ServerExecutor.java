/**
 * 
 */
package com.yaowan.framework.core.handler.server;

import com.yaowan.framework.core.handler.AbstractLink;
import com.yaowan.model.struct.Player;

/**
 * @author huangyuyuan
 *
 */
public abstract class ServerExecutor implements IServerExecutor {
	
	@Override
	public final <T extends AbstractLink> void execute(T player, byte[] data) throws Exception {
		doExecute((Player) player, data);
	}
	
	public abstract void doExecute(Player player, byte[] data) throws Exception;
}
