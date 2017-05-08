/**
 * 
 */
package com.yaowan.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author huangyuyuan
 *
 */
public class IODecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
                          List<Object> out) throws Exception {
		int size = in.readableBytes();
		if(size < 4) {
			return;
		}
		// 读半包
		int dataLen = in.readInt();
		if (dataLen > size - 4) {
			// 数据没有准备好，重置
			in.resetReaderIndex();
			return;
		}
		
		
		int protocol = in.readInt();
		byte[] msgBody = new byte[dataLen - 4];
		in.readBytes(msgBody);
		in.markReaderIndex();
		
		Message msg = Message.build(protocol, msgBody);
		out.add(msg);
	}
}
