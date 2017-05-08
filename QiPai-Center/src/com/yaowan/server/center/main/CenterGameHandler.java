/**
 * 
 */
package com.yaowan.server.center.main;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import com.yaowan.CenterErrorMsg;
import com.yaowan.constant.CenterError;
import com.yaowan.constant.ChannelConst;
import com.yaowan.framework.core.handler.server.IServerExecutor;
import com.yaowan.framework.core.handler.server.ServerDispatcher;
import com.yaowan.framework.netty.Message;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.model.struct.GameServer;

/**
 * @author huangyuyuan
 *
 */
public class CenterGameHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		GameServer gameServer = new GameServer(ctx.channel());
		ctx.channel().attr(ChannelConst.GAMESERVER).set(gameServer);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		GameServer gameServer = ctx.channel().attr(ChannelConst.GAMESERVER).get();
		if(gameServer != null) {
			IServerExecutor executor = ServerDispatcher.getExecutor(21001002);
			if(executor != null) {
				executor.execute(gameServer, null);
			} else {
				LogUtil.error("Executor 21001002 not found");
			}
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if(!(msg instanceof Message)) {
			return;
		}
		GameServer gameServer = ctx.channel().attr(ChannelConst.GAMESERVER).get();
		if (gameServer == null || gameServer.getChannel() == null) {
			return;
		}
		Message message = (Message) msg;
		int protocol = message.getProtocol();
		
		// 是否为需要登录的请求
		if (!ServerDispatcher.isRequestValid(gameServer, protocol) && gameServer.getServerId() == 0) {
			LogUtil.error("Protocol " + protocol + " request need login");
			gameServer.getChannel().disconnect();
			return;
		}
		//TODO 记录执行时间
//		long startTime = System.currentTimeMillis();
		//TODO 需要考虑是否手动进行线程分配
		IServerExecutor executor = ServerDispatcher.getExecutor(protocol);
		if(executor == null) {
			LogUtil.error("Executor " + protocol + " not found");
			return;
		}
		try {
			executor.execute(gameServer, message.getMsgBody());
		} catch(Exception e) {
			LogUtil.error(e);
			CenterErrorMsg.send(gameServer, CenterError.SYSTEM_EXCEPTION, protocol, e.getMessage());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
//		cause.printStackTrace();
	}
}
