/**
 * 
 */
package com.yaowan.framework.core.handler;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.net.InetSocketAddress;

import com.google.protobuf.GeneratedMessageLite;
import com.yaowan.framework.netty.Message;


/**
 * @author zane
 *
 */
public abstract class AbstractLink {
	
	private Channel channel;
	
	private int clientType;
	
	
	
	public AbstractLink(Channel channel) {
		this.channel = channel;
	}
	
	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public void write(GeneratedMessageLite msg) {
		if(channel != null && channel.isWritable() && msg != null) {
			Message message = Message.build(msg);
			write(message);
		}
	}
	
	public void write(Message message) {
		if(channel != null && channel.isWritable() && message != null) {
			if (clientType == 1) {
				channel.writeAndFlush(new BinaryWebSocketFrame(NetUtil.wrapBuffer(message)));
			}else{
				channel.writeAndFlush(message);		
			}
		}
	}

	public Channel getChannel() {
		return channel;
	}
	
	public abstract long getId();
	
	public String getIp() {
		return ((InetSocketAddress) channel.remoteAddress()).getAddress()
				.getHostAddress();
	}

	public int getClientType() {
		return clientType;
	}

	public void setClientType(int clientType) {
		this.clientType = clientType;
	}
}
