/**
 * 
 */
package com.yaowan.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * @author huangyuyuan
 *
 */
public class IOEncoder extends ChannelOutboundHandlerAdapter {

	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
                      ChannelPromise promise) throws Exception {
		final Message data = (Message) msg;
		final ByteBuf encoded = ctx.alloc().buffer(4 + 4 + data.getMsgBody().length);
		encoded.writeInt(4 + data.getMsgBody().length);
		encoded.writeInt(data.getProtocol());
		encoded.writeBytes(data.getMsgBody());
		ctx.writeAndFlush(encoded);
	}
}
