/**
 * 
 */
package com.yaowan.httpserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.DefaultThreadFactory;

import com.yaowan.ServerConfig;
import com.yaowan.framework.httpserver.HttpServerInitializer;
import com.yaowan.framework.util.LogUtil;


/**
 * @author zane
 *
 */
public class NettyHttp {
	private static final EventLoopGroup bossGroup = new NioEventLoopGroup(2,
			new DefaultThreadFactory("bossGroup"));
	private static final EventLoopGroup workerGroup = new NioEventLoopGroup(
			Runtime.getRuntime().availableProcessors() * 3,
			new DefaultThreadFactory("workerGroup"));
	static final boolean SSL = System.getProperty("ssl") != null;
    
	public static void start() {
		// Configure SSL.
        SslContext sslCtx = null;
        if (SSL) {
        	try {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            
				sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } 
		
     // Configure the server.

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new HttpServerInitializer(sslCtx,HttpServerHandler.class));
            Channel ch = b.bind(ServerConfig.httpPort).sync().channel();

            System.err.println("Open your web browser and navigate to " +
                    (SSL? "https" : "http") + "://127.0.0.1:" + ServerConfig.httpPort + '/');

            ch.closeFuture().sync();
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
		LogUtil.info("Http start on " + ServerConfig.httpPort);
	}
}
