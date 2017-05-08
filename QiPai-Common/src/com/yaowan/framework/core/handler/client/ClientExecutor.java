/**
 * 
 */
package com.yaowan.framework.core.handler.client;


/**
 * @author huangyuyuan
 *
 */
public abstract class ClientExecutor implements IClientExecutor {

	@Override
	public final void execute(byte[] data) {
		doExecute(data);
	}

	public abstract void doExecute(byte[] msgBody);
}
