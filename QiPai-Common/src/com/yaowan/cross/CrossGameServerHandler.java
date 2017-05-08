/**
 * 
 */
package com.yaowan.cross;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import com.yaowan.framework.util.LogUtil;

/**
 * 跨服游戏
 * @author YW0941
 *
 */
public class CrossGameServerHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("connect crossserver");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		
	}
	
	
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		try{
			if (!(msg instanceof BasePacket)) {
				return;
			}
			
			BasePacket packet = (BasePacket) msg;
			
			CrossPlayer crossPlayer = CrossPlayerContainer.get(packet.getRid());
			crossPlayer.setChannel(ctx.channel());
			packet.setCrossPlayer(crossPlayer);
			CrossGameNettyServer.receivePacket(packet);
//			DispatchController.get(packet.getCmd()).execute(crossPlayer,packet);
			
		}catch(Exception e){
			LogUtil.error( e);
		}
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
//		cause.printStackTrace();
	}
}
