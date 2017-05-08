/**
 * 
 */
package com.yaowan.server.game.main;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.yaowan.constant.ChannelConst;
import com.yaowan.core.base.GlobalConfig;
import com.yaowan.framework.core.handler.server.IServerExecutor;
import com.yaowan.framework.core.handler.server.ServerDispatcher;
import com.yaowan.framework.netty.Message;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.model.struct.Player;

/**
 * @author huangyuyuan
 *
 */
public class GameServerHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Player player = new Player(ctx.channel());
		ctx.channel().attr(ChannelConst.PLAYER).set(player);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Player player = ctx.channel().attr(ChannelConst.PLAYER).get();
		if(player != null) {
			IServerExecutor executor = ServerDispatcher.getExecutor(11001003);
			if(executor != null) {
				executor.execute(player, null);
			} else {
				LogUtil.error("Executor 11001003 not found");
			}
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (!(msg instanceof Message)) {
			return;
		}
		Player player = ctx.channel().attr(ChannelConst.PLAYER).get();
		if (player == null || player.getChannel() == null) {
			return;
		}
		Message message = (Message) msg;
		int protocol = message.getProtocol();
		// 是否为需要登录的请求
		if (!ServerDispatcher.isRequestValid(player, protocol)
				&& player.getRole() == null) {
			LogUtil.error("Protocol " + protocol + " request need login");
			player.getChannel().disconnect();
			return;
		}


		// 限制请求过多
		// if (ServerDispatcher.isTooMuchRequest(player.socket, protocol)) {
		// GError.send(player, GameError.SYSTEM_REQUEST_IS_TOO_FAST, false, "");
		// return;
		// }
		// TODO 记录执行时间
		// long startTime = System.currentTimeMillis();
		// TODO 需要考虑是否手动进行线程分配
		IServerExecutor executor = ServerDispatcher.getExecutor(protocol);
		if (executor == null) {
			LogUtil.error("Executor " + protocol + " not found");
			return;
		}
		if(GlobalConfig.isTest){
			LogUtil.info("ServerExecutor -----------" + executor.getClass().getName()+">>>>>"+protocol);
		}
		try {
			executor.execute(player, message.getMsgBody());
		} catch (Exception e) {
			LogUtil.error(ExceptionUtils.getStackTrace(e));
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
//		cause.printStackTrace();
	}
}
