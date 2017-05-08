/**
 * 
 */
package com.yaowan.server.game.main;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageLite;
import com.yaowan.ServerConfig;
import com.yaowan.framework.netty.IODecoder;
import com.yaowan.framework.netty.IOEncoder;
import com.yaowan.framework.netty.Message;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.protobuf.center.CGame;
import com.yaowan.protobuf.center.CRegister.CMsg_21001001;

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
	
	public static void init(){
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
						new GameClientHandler());
			}
		});
	}
	public static void connectToCenter() {
		
		doConnect();
	}

	public static void doConnect() {
		ChannelFuture channelFuture = bootstrap.connect(ServerConfig.centerHost,
				ServerConfig.centerPort);
		
		channelFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if(future.isSuccess()) {
					LogUtil.info("Connect to Center server " + future.channel().remoteAddress());
				} else {
					future.channel().eventLoop().schedule(new Runnable() {
						@Override
						public void run() {
							//LogUtil.info("Reconnecting to Center server...");
							doConnect();							
						}
					}, 10, TimeUnit.SECONDS);
				}
			}});
	}
	
	public static void main(String[] args) {
		//ServerConfig.init("F:/qipai_server/trunk/ServerQiPai/config/");
		NettyClient.connectToCenter();
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

	public static void registeServer() {
		CMsg_21001001.Builder builder = CMsg_21001001.newBuilder();
		builder.setServerId(ServerConfig.serverId);
		write(builder.build());	
	}
	/**
	 * 请求中心服
	 * @param cmd 命令cmd
	 * @param data 包装传递的数据
	 */
	public static void request(int cmd,byte[] data){
		CGame.CMsg_21100007.Builder builder = CGame.CMsg_21100007.newBuilder();
		builder.setCmd(cmd);
		if(data != null){
			builder.setData(ByteString.copyFrom(data));
		}
		NettyClient.write(builder.build());
	}
}
