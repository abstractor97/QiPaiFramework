/**
 * 
 */
package com.yaowan.server.game.cross;

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

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.GeneratedMessageLite;
import com.yaowan.cross.BasePacket;
import com.yaowan.cross.CrossGameDataDecoder;
import com.yaowan.cross.CrossGameDataEncoder;
import com.yaowan.framework.util.LogUtil;

/**
 * @author huangyuyuan
 *
 */
public class CrossGameClient {

	private final EventLoopGroup group = new NioEventLoopGroup(1);
	
	private Bootstrap bootstrap;
	
	private Channel channel;
	
	private String host;
	private int port;
	
	private List<Integer> gameTypes;
	public CrossGameClient(String host,int port,List<Integer> gameTypes){
		this.host = host;
		this.port = port;
		this.gameTypes = gameTypes;
	}
	
	public void connectToCrossGame() {
		bootstrap = new Bootstrap();
		bootstrap.group(group);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.SO_RCVBUF, 1048576);
        bootstrap.option(ChannelOption.SO_SNDBUF, 1048576);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
        
        final CrossGameClientHandler crossGameClientHandler = new CrossGameClientHandler(this);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new CrossGameDataDecoder(), new CrossGameDataEncoder(),
						crossGameClientHandler);
			}
		});
		doConnect();
	}

	public void doConnect() {
		ChannelFuture channelFuture = bootstrap.connect(host,
				port);
		
		channelFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if(future.isSuccess()) {
					LogUtil.info("Connect to cross server " + future.channel().remoteAddress());
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
	

	
	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	public List<Integer> getGameTypes() {
		return gameTypes;
	}

	public void write(short cmd,GeneratedMessageLite msg){
		
	}
	
//	public static void write(GeneratedMessageLite msg) {
//		if(channel != null && channel.isWritable() && msg != null) {
//			Message message = Message.build(msg);
//			channel.writeAndFlush(message);
//		} else {
//			if(!protobufResend.contains(msg)) {
//				protobufResend.add(msg);
//			}
//		}
//	}
	
//	public static void write(Message message) {
//		if(channel != null && channel.isWritable() && message != null) {
//			channel.writeAndFlush(message);
//		}
//	}
	

	public void registeServer() {
//		Spring.getBean(CrossFunction.class).executeRetransmission(crossServerId, crossGameClient);
	}

	public void write(BasePacket basePacket) {
		if(channel!=null && channel.isActive() && channel.isOpen()){
			channel.writeAndFlush(basePacket);
		}
		
	}

}
