/**
 * 
 */
package com.yaowan.simulator;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import com.google.protobuf.GeneratedMessageLite;
import com.yaowan.framework.netty.IODecoder;
import com.yaowan.framework.netty.IOEncoder;
import com.yaowan.framework.netty.Message;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.protobuf.game.GLogin.GMsg_11001001;
import com.yaowan.util.MD5Util;

/**
 * @author huangyuyuan
 *
 */
public class SimulatorNetty {
	
	private static SimulatorNetty instance = new SimulatorNetty();
	private SimulatorNetty(){}
	public static SimulatorNetty getInstance() {
		return instance;
	}
	
	private Channel channel;
	
	private HeartBeat heartBeat;
	
	private static final EventLoopGroup group = new NioEventLoopGroup();
	
	public void connect(String host, int port) {
		
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(group);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new IODecoder(), new IOEncoder(),
						new SimulatorHandler());
			}
		});
		
		bootstrap.connect(host, port);
//		System.out.println("Client start");
		
		while(channel == null || !channel.isWritable()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void login(String openId, String md5Key) {
		int now = TimeUtil.time();
		
		GMsg_11001001.Builder builder = GMsg_11001001.newBuilder();
		builder.setOpenId(openId);
		builder.setTime(now);
		Map<String, Object> sign = new HashMap<String, Object>();
		sign.put("openId", openId);
		sign.put("time", now);
		String strSign = MD5Util.makeSign(sign, md5Key);
		System.out.println(strSign);
		builder.setSign(strSign);
		write(builder.build());
	}
	
	public static void main(String[] args) {
		Map<String, Object> sign = new HashMap<String, Object>();
		sign.put("openId", "hyy12345");
		sign.put("time", 1);
		String strSign = MD5Util.makeSign(sign, "^_^dfh3:start@2015-09-24!");
		System.out.println(strSign);
	}
	
	public void connectCenter() {
//		CReqTellServerId.Builder builder = CReqTellServerId.newBuilder();
//		builder.setServerId(1);
//		write(builder.build());
	}
	
	public void create(String openId, String md5Key) {
//		int now = DateTime.time();
//		
//		GReqLogin.Builder builder = GReqLogin.newBuilder();
//		builder.setOpenId(openId);
//		builder.setTime(now);
//		builder.setPlatform(1);
//		Map<String, Object> sign = new HashMap<String, Object>();
//		sign.put("openId", openId);
//		sign.put("time", now);
//		String strSign = GameApp.makeSign(sign, md5Key);
//		builder.setSign(strSign);
//		write(builder.build());
//		
//		GReqInit.Builder initBuilder = GReqInit.newBuilder();
//		initBuilder.setNick(UUID.randomUUID().toString().substring(0, 25));
//		initBuilder.setLang(1);
//		initBuilder.setActor(1);
//		initBuilder.setInvestorId(0);
//		initBuilder.setPlatform(1);
//		initBuilder.setPushId("");
//		initBuilder.setToken("");
//		write(initBuilder.build());
	}
	
	public void write(GeneratedMessageLite msg) {
		if (channel == null || msg == null || !channel.isWritable()) {
			return;
		}
		Message message = Message.build(msg);
		channel.writeAndFlush(message);
	}
	
	public void startHeartBeat() {
		if(heartBeat != null) {
			heartBeat.stopHeartBeat();
		}
		heartBeat = new HeartBeat();
		Executors.newSingleThreadExecutor().execute(heartBeat);
	}

	private class HeartBeat implements Runnable {
		
		private boolean stopHeartBeat = false;
		
		public void run() {
			while (!stopHeartBeat) {
//				write(GReqHeartbeat.getDefaultInstance());
				try {
					Thread.sleep(300000);
				} catch (InterruptedException e) {
				}
			}
		}
		
		public void stopHeartBeat() {
			stopHeartBeat = true;
		}
	}
	
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	public Channel getChannel() {
		return this.channel;
	}
}
