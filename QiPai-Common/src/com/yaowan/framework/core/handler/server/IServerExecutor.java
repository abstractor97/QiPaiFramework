/**
 * 
 */
package com.yaowan.framework.core.handler.server;

import com.yaowan.framework.core.handler.AbstractLink;


/**
 * @author huangyuyuan
 *
 */
public interface IServerExecutor {
	
	public <T extends AbstractLink> void execute(T link, byte[] data) throws Exception;
}
