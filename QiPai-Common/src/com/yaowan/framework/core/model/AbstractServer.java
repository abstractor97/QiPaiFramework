/**
 * 
 */
package com.yaowan.framework.core.model;

import io.netty.channel.Channel;

import com.yaowan.framework.core.handler.AbstractLink;

/**
 * @author huangyuyuan
 *
 */
public abstract class AbstractServer extends AbstractLink {

	protected int serverId;
	
	public AbstractServer(Channel channel) {
		super(channel);
	}

	public int getServerId() {
		return serverId;
	}
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
}
