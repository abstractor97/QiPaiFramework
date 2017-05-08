/**
 * 
 */
package com.yaowan.server.game.main;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import com.yaowan.framework.core.handler.client.ClientDispatcher;
import com.yaowan.framework.core.handler.client.IClientExecutor;
import com.yaowan.framework.netty.Message;
import com.yaowan.framework.util.LogUtil;

/**
 * @author huangyuyuan
 *
 */
public class GameClientHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		NettyClient.setChannel(ctx.channel());
		
		//连接成功后，将本游戏服注册到中心服上
		NettyClient.registeServer();
		//TODO 怎么处理重连后的未完成任务
		//NettyClient.sendMessageWhenReconnected();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		LogUtil.info("channelInactive to Center server...");
		NettyClient.doConnect();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if(!(msg instanceof Message)) {
			return;
		}
		Message message = (Message) msg;
		int protocol = message.getProtocol();
		
		IClientExecutor executor = ClientDispatcher.getExecutor(protocol);
		if(executor == null) {
			LogUtil.error("Executor " + protocol + " not found");
			return;
		}
		try {
			executor.execute(message.getMsgBody());
		} catch(Exception e) {
			LogUtil.error(e);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//		cause.printStackTrace();
	}
}
