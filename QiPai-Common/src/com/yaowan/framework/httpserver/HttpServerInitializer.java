package com.yaowan.framework.httpserver;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;
    
    private Class<? extends ChannelInboundHandlerAdapter> httpServerHandlerClazz;
    private static final String WEBSOCKET_PATH = "/websocket";

    public HttpServerInitializer(SslContext sslCtx,Class<? extends ChannelInboundHandlerAdapter> httpServerHandlerClazz) {
        this.sslCtx = sslCtx;
        this.httpServerHandlerClazz = httpServerHandlerClazz;
    }

    @Override
    public void initChannel(SocketChannel ch) throws InstantiationException, IllegalAccessException {
        ChannelPipeline p = ch.pipeline();
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }
        p.addLast(new HttpServerCodec());
        p.addLast(new HttpObjectAggregator(1048576));
        p.addLast(httpServerHandlerClazz.newInstance());
        //p.addLast(new WebSocketServerCompressionHandler());
       // p.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
       
    }
}