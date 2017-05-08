/**
 * 
 */
package com.yaowan.server.game.main;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.yaowan.ServerConfig;
import com.yaowan.framework.netty.IODecoder;
import com.yaowan.framework.netty.IOEncoder;
import com.yaowan.framework.util.LogUtil;

/**
 * @author huangyuyuan
 *
 */
public class NettyServer {

	// private static final EventLoopGroup bossGroup = new NioEventLoopGroup(2,
	// new DefaultThreadFactory("bossGroup"));
	// private static final EventLoopGroup workerGroup = new NioEventLoopGroup(
	// Runtime.getRuntime().availableProcessors() * 3,
	// new DefaultThreadFactory("workerGroup"));

	public static void start() {

		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					// .handler(new LoggingHandler(LogLevel.INFO))// http
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch)
								throws Exception {
							ch.pipeline().addLast(new IODecoder(),
									new GameServerHandler(), new IOEncoder());
							// //socket
							// p.addLast((ByteToMessageDecoder)SocketDecoder.newInstance());
							// p.addLast((ChannelInboundHandlerAdapter)SocketHandler.newInstance());

							// p.addLast("ping", new IdleStateHandler(10, 10,
							// 0,TimeUnit.SECONDS));
						}
					})

					.option(ChannelOption.SO_BACKLOG, 1024)
					.childOption(ChannelOption.TCP_NODELAY, true)
					.childOption(ChannelOption.SO_KEEPALIVE, true);

			// Bind and start to accept incoming connections.
			b.bind(ServerConfig.nettyPort).sync();
			LogUtil.info("Netty start on " + ServerConfig.nettyPort);

		} catch (Exception e) {
			LogUtil.error(ExceptionUtils.getStackTrace(e));
		}
	}

}
