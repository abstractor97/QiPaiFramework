/**
 * 
 */
package com.yaowan.cross;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import com.yaowan.ServerConfig;
import com.yaowan.framework.netty.IODecoder;
import com.yaowan.framework.netty.IOEncoder;
import com.yaowan.framework.util.LogUtil;




/**
 * @author huangyuyuan
 *
 */
public class CrossGameNettyServer {
	 private static int io_threads = Runtime.getRuntime().availableProcessors();
	 private static int min_logic_threads = 2 * io_threads;
	 private static int max_logic_threads = 4 * io_threads;
	 
	private static final EventLoopGroup bossGroup = new NioEventLoopGroup(2,
			new DefaultThreadFactory("bossGroup"));
	private static final EventLoopGroup workerGroup = new NioEventLoopGroup(
			Runtime.getRuntime().availableProcessors() * 3,
			new DefaultThreadFactory("workerGroup"));

	private final static OrderedPacketProcessor processor = new OrderedPacketProcessor(min_logic_threads, max_logic_threads);
	public static void start() {
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.option(ChannelOption.SO_REUSEADDR, true);
//		bootstrap.option(ChannelOption.SO_BACKLOG, 128);
		bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
		bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
		//bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
		//TODO 线程切换时需要怎么释放？
//		bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		
		bootstrap.group(bossGroup, workerGroup);
		bootstrap.channel(NioServerSocketChannel.class);
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new CrossGameDataDecoder(), new CrossGameDataEncoder(),
						new CrossGameServerHandler());
			}
		});
		bootstrap.bind(ServerConfig.nettyPort);
		LogUtil.info("Netty start on " + ServerConfig.nettyPort);
	}
	/**
	 * 收到数据包
	 * @param packet
	 */
	public static void receivePacket(BasePacket packet) {
		processor.pushPacket(packet);
	}
}
