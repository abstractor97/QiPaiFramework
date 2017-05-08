package com.yaowan.cross;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
/**
 * 请求协议格式
 *   	协议头+协议数据长度+CMD+GMsg协议号+RoleId+协议数据
 * 长度  
 * 		1+2+2+4+8+协议数据字节
 * 最少长度： 17
 * @author YW0941
 *
 */
public class CrossGameDataDecoder extends ByteToMessageDecoder {
	
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		
		if (in.readableBytes() < 17)
            return;
        in.markReaderIndex();
        if (in.readByte() != BasePacket.HEAD) {//读取1位
            throw new RuntimeException("CrossDataDecoder wrong head from ["+ ctx.channel().id() +"]");
        }
        //读取协议数据长度， 读+2  =3
        int dataLen = in.readShort();
        
        if(in.readableBytes()<dataLen+14){
        	in.resetReaderIndex();
        	return;
        }
        
        //读取CMD值 读+2  =5
        short cmd = in.readShort();
        //读取GMsg请求号 读+4  =9
        int gmsgId = in.readInt();
        //读取RoleId 
        long roleId = in.readLong();
       
        byte[] bytes = new byte[dataLen];
        in.readBytes(bytes);
        
        BasePacket basePacket = new BasePacket(cmd, gmsgId, roleId,bytes);
        
        out.add(basePacket);
	}

}
