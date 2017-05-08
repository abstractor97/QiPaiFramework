package com.yaowan.game.qipai.test;

import com.google.protobuf.GeneratedMessageLite;
import com.yaowan.netty.IODecoder;
import com.yaowan.netty.IOEncoder;
import com.yaowan.netty.Message;
import com.yaowan.util.LogUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author huangyuyuan
 *
 */
public class NettyClient {

	private static final EventLoopGroup group = new NioEventLoopGroup(1);
	
	private static Bootstrap bootstrap;
	
	private static Channel channel;
	
	/**
	 * 处理回调逻辑的线程
	 */
	private static ExecutorService executor = Executors.newSingleThreadExecutor();
	
	/**
	 * 正在发送的协议队列
	 */
	public static ConcurrentLinkedQueue<GeneratedMessageLite> protobufSending = new ConcurrentLinkedQueue<>();
	/**
	 * 需重新发送的协议队列
	 */
	public static ConcurrentLinkedQueue<GeneratedMessageLite> protobufResend = new ConcurrentLinkedQueue<>();
	
	public static void connectToCenter(final String ip,final int port, final int i,final int playerGroup) {
		bootstrap = new Bootstrap();
		bootstrap.group(group);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.SO_RCVBUF, 1048576);
        bootstrap.option(ChannelOption.SO_SNDBUF, 1048576);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new IODecoder(), new IOEncoder(),
						new GameClientHandler(ip,port,i,playerGroup));
			}
		});
		doConnect(ip,port);
	}

	public static void doConnect(final String ip,final int port) {
		ChannelFuture channelFuture = bootstrap.connect(ip,port);
		
		channelFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if(future.isSuccess()) {
					LogUtil.info("Connect to Center server " + future.channel().remoteAddress());
				} else {
					LogUtil.error(future.cause());
					future.channel().eventLoop().schedule(new Runnable() {
						@Override
						public void run() {
							//LogUtil.info("Reconnecting to Center server...");
							doConnect(ip,port);
						}
					}, 0, TimeUnit.MINUTES);
				}
			}});
	}
	
	public static Channel getChannel() {
		return channel;
	}

	public static void setChannel(Channel channel) {
		NettyClient.channel = channel;
	}

	public static void write(GeneratedMessageLite msg) {
		if(channel != null && channel.isWritable() && msg != null) {
			Message message = Message.build(msg);
			channel.writeAndFlush(message);
		} else {
			if(!protobufResend.contains(msg)) {
				protobufResend.add(msg);
			}
		}
	}
	
	public static void write(Message message) {
		if(channel != null && channel.isWritable() && message != null) {
			channel.writeAndFlush(message);
		}
	}
	
	public static void doExecute(Runnable task) {
		executor.execute(task);
	}
	
	/**
	 * 断线重连发送
	 */
	public static void sendMessageWhenReconnected() {
		resendWhenReconnected();
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				resendWhenReconnected();
			}
		}, 10000);
	}
	/**
	 * 重发信息
	 */
	public static void resendWhenReconnected() {
		GeneratedMessageLite resendMsg = protobufResend.poll();
		while(resendMsg != null) {
			protobufSending.add(resendMsg);
			resendMsg = protobufResend.poll();
		}
		if(protobufSending.isEmpty()) {
			return;
		}
		GeneratedMessageLite msg = protobufSending.poll();
		while(msg != null) {
			write(msg);
			msg = protobufSending.poll();
		}
	}
}
