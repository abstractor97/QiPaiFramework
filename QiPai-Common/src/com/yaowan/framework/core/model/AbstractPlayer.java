/**
 * 
 */
package com.yaowan.framework.core.model;

import io.netty.channel.Channel;

import com.yaowan.framework.core.handler.AbstractLink;

/**
 * @author huangyuyuan
 * 考虑到战斗服跟游戏服是分布式的设计
 */
public abstract class AbstractPlayer extends AbstractLink {

	public AbstractPlayer(Channel channel) {
		super(channel);
	}
}
