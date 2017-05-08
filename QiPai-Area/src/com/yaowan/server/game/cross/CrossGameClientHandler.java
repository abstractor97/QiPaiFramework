/**
 * 
 */
package com.yaowan.server.game.cross;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import com.yaowan.core.base.Spring;
import com.yaowan.cross.BasePacket;
import com.yaowan.cross.DispatchController;
import com.yaowan.framework.netty.Message;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.protobuf.cmd.CMD.CrossCMD;
import com.yaowan.server.game.function.RoleFunction;

/**
 * @author huangyuyuan
 *
 */
public class CrossGameClientHandler extends ChannelInboundHandlerAdapter {
	
	private RoleFunction roleFunction;
	private CrossGameClient crossGameClient;
	public CrossGameClientHandler(CrossGameClient crossGameClient) {
		this.crossGameClient = crossGameClient;
		roleFunction = Spring.getBean(RoleFunction.class);
	}
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		crossGameClient.setChannel(ctx.channel());
		
		//连接成功后，将本游戏服注册到跨服游戏服上
		crossGameClient.registeServer();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		LogUtil.info("channelInactive to Center server...");
		crossGameClient.doConnect();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	
		try {
			if(!(msg instanceof BasePacket)){
				return ;
			}
			BasePacket basePacket = (BasePacket)msg;			
			if(basePacket.getGmsgID() == CrossCMD.Retransmission_VALUE){ //是转发消息，直接转发给玩家
				Message message = Message.build(basePacket.getGmsgID(), basePacket.getData());
				roleFunction.getPlayer(basePacket.getRid()).write(message);
				return;
			}
			//处理非转发消息
			DispatchController.get(basePacket.getCmd()).execute(roleFunction.getPlayer(basePacket.getRid()), basePacket);
		} catch(Exception e) {
			LogUtil.error(e);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//		cause.printStackTrace();
	}
}
