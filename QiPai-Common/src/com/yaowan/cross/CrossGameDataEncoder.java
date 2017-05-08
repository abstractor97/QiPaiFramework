package com.yaowan.cross;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/***
 * 返回协议格式
 *   	协议头+协议数据长度+CMD+GMsg协议号+RoleId+协议数据
 * 长度  
 * 		1+2+2+4+8+协议数据字节
 * 最少长度： 18
 * @author YW0941
 *
 */
public class CrossGameDataEncoder extends ChannelOutboundHandlerAdapter {
	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise promise) throws Exception {
		if(msg instanceof BasePacket){
			BasePacket packet = (BasePacket) msg;
//			ByteBuf byteBuf = Unpooled.buffer(17+packet.getData().length);
		
			int dataLen = 0;
			if(packet.getData()!=null){
				dataLen =packet.getData().length;
			}
			ByteBuf byteBuf = ctx.alloc().buffer(17+dataLen);
			//写入协议头 +1  = 1
			byteBuf.writeByte(BasePacket.HEAD);
			//写入协议数据长度 +2 = 3
			byteBuf.writeShort(dataLen);
			//写入cmd +2 = 5
			byteBuf.writeShort(packet.getCmd());
			//协议Gmsg号 +4 = 9
			byteBuf.writeInt(packet.getGmsgID());
			//roleId +8 = 17
			byteBuf.writeLong(packet.getRid());
			//协议数据 
			if(dataLen>0){
				byteBuf.writeBytes(packet.getData());
			}
			ctx.write(byteBuf);
			
		}
	}
}
