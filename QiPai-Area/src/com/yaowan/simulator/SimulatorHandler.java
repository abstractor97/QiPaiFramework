package com.yaowan.simulator;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;

import com.yaowan.framework.core.handler.ProtobufCenter;
import com.yaowan.framework.netty.Message;
import com.yaowan.protobuf.game.GLogin.GMsg_12001001;


public class SimulatorHandler extends ChannelInboundHandlerAdapter {
	
	private SimulatorNetty netty = SimulatorNetty.getInstance();

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (netty.getChannel() != null) {
			netty.getChannel().disconnect();
		}
		netty.setChannel(ctx.channel());
		SimulatorClient.getInstance().addResultText("连接  "
				+ ctx.channel().remoteAddress().toString()
						.replace("/", "") + "  成功");
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		SimulatorClient.getInstance().addResultText("连接  "
				+ ctx.channel().remoteAddress().toString()
						.replace("/", "") + "  断开");
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		Message message = (Message) msg;
		Class<?> clz = ProtobufCenter.getProtobufClass(message.getProtocol());
		System.out.println("================" + clz.getSimpleName() + "================");
		SimulatorClient.getInstance().addResultText("\n================" + clz.getSimpleName() + "================");
		try {
			Method method = clz.getMethod("parseFrom", byte[].class);
			Object object = method.invoke(clz, message.getMsgBody());
			
			if(object instanceof GMsg_12001001) {
				GMsg_12001001 login = (GMsg_12001001) object;
				if(login.getRoleInfo() != null) {
					netty.startHeartBeat();
				}
				System.out.println(login.getRoleInfo());
			}
//			else if(object instanceof GResInit) {
//				GResInit init = (GResInit) object;
//				if(init.getRid() != 0) {
//					netty.startHeartBeat();
//				}
//			}
			SimulatorPrinter.print(object);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		ctx.channel().disconnect();
	}
}