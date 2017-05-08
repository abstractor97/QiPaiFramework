package com.yaowan.framework.httpserver;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.Locale;








import com.yaowan.framework.netty.Message;
import com.yaowan.framework.util.LogUtil;




/**
 * Echoes uppercase content of text frames.
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        // ping and pong frames already handled

        if (frame instanceof TextWebSocketFrame) {
            // Send the uppercase string back.
            String request = ((TextWebSocketFrame) frame).text();
            LogUtil.info("{} received {}"+ request);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(request.toUpperCase(Locale.US)));
        } else if (frame instanceof BinaryWebSocketFrame) {
            // Send the uppercase string back.
            BinaryWebSocketFrame binReq = (BinaryWebSocketFrame) frame;
            ByteBuf buffer = binReq.content();
			// 没有用，但是要移到读取下标
			buffer.readInt();
			int protocol = buffer.readInt();
			
			byte[] allData = buffer.array();
			int dateLength = allData.length - 8;
			byte[] msgBody = new byte[dateLength];
			System.arraycopy(allData, 8, msgBody, 0, dateLength);
			
			Message gMessage = Message.build(protocol, msgBody);
        }
    }
}