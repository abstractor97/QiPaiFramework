/**
 * 
 */
package com.yaowan.game.qipai.test;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GenericFutureListener;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.protobuf.GeneratedMessageLite;
import com.yaowan.core.handler.ProtobufCenter;
import com.yaowan.netty.Message;
import com.yaowan.protobuf.game.GChat.GMsg_11008004;
import com.yaowan.protobuf.game.GDouDiZhu.GMsg_11012001;
import com.yaowan.protobuf.game.GExchange.GMsg_11017001;
import com.yaowan.protobuf.game.GGame.GMsg_11006001;
import com.yaowan.protobuf.game.GGame.GMsg_11006003;
import com.yaowan.protobuf.game.GGame.GMsg_11006004;
import com.yaowan.protobuf.game.GGame.GMsg_11006008;
import com.yaowan.protobuf.game.GItem.GMsg_11005001;
import com.yaowan.protobuf.game.GLogin.GMsg_11001001;
import com.yaowan.protobuf.game.GLogin.GMsg_11001002;
import com.yaowan.protobuf.game.GMahJong.GMsg_11011001;
import com.yaowan.protobuf.game.GMail.GMsg_11015004;
import com.yaowan.protobuf.game.GMenJi.GMsg_11013001;
import com.yaowan.protobuf.game.GMission.GMsg_11016001;
import com.yaowan.protobuf.game.GRankingList.GMsg_11018001;
import com.yaowan.protobuf.game.GRole.GMsg_11002009;
import com.yaowan.util.LogUtil;
import com.yaowan.util.MD5Util;
import com.yaowan.util.TimeUtil;

/**
 * @author huangyuyuan
 *
 */
public class GameClientHandler extends ChannelInboundHandlerAdapter {
	//游戏类型列表
	private static int[] GameTypes = new int[]{2,3};
	private AtomicInteger ai = new AtomicInteger(0);

	private int i;
	private String ip;
	private int port;
	private int group;

	AttributeKey<String> nameKey = AttributeKey.valueOf("name");
	AttributeKey<Integer> serialNumberKey = AttributeKey.valueOf("i");
	AttributeKey<Integer> statusKey = AttributeKey.valueOf("status");
	AttributeKey<Integer> gameTypeKey = AttributeKey.valueOf("gameType");

	AttributeKey<String> loginTimeKey = AttributeKey.valueOf("loginTime");
	public GameClientHandler(String ip, int port, int i,int group) {
		this.i = i;
		this.ip = ip;
		this.port = port;
		this.group = group;
	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {
		// 写协议过去
		login(ctx.channel());
		ctx.channel().attr(statusKey).set(0);
		// TODO 怎么处理重连后的未完成任务
		// NettyClient.sendMessageWhenReconnected();
		
		if(!Runner.OnlyLogin){
			ThreadPool.scheduled(new Runnable() {
	
				@Override
				public void run() {
					try {
						doSomething(ctx);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}, 5, TimeUnit.SECONDS);
		}
	}

	private void doSomething(final ChannelHandlerContext ctx)
			throws InterruptedException {
		if (ctx == null) {
			return;
		}
		if (ctx.channel() == null) {
			return;
		}
		
		if (! ctx.channel().isActive()) {
			return;
		}

		if (new Random().nextInt(10) > 5) {
			ThreadPool.execute(new Runnable() {

				@Override
				public void run() {
					GMsg_11018001.Builder builder = GMsg_11018001.newBuilder();
					LogUtil.error(ctx.channel().attr(nameKey).get() +"排行榜");
					write(builder.build(), ctx.channel());
				}
			});

			// return ;
		}

		if (new Random().nextInt(10) > 7) {
			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					GMsg_11005001.Builder builder = GMsg_11005001.newBuilder();// 请求道具信息
					LogUtil.error(ctx.channel().attr(nameKey).get() +"请求道具信息");
					write(builder.build(), ctx.channel());
				}
			});

			// return ;
		}

		if (new Random().nextInt(10) > 7) {
			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					GMsg_11006003.Builder builder = GMsg_11006003.newBuilder();
					LogUtil.error(ctx.channel().attr(nameKey).get() +"在线数据");
					write(builder.build(), ctx.channel());
				}
			});

			// return ;
		}

		

		if (new Random().nextInt(10) > 6) {
			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					GMsg_11015004.Builder builder = GMsg_11015004.newBuilder();// 查看邮件
					LogUtil.error(ctx.channel().attr(nameKey).get() +"查看邮件");
					write(builder.build(), ctx.channel());
				}
			});

			// return ;
		}
		if (new Random().nextInt(10) > 6) {
			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					// 请求大厅游戏在线数据
					GMsg_11016001.Builder builder = GMsg_11016001.newBuilder();// 任务
					LogUtil.error(ctx.channel().attr(nameKey).get() +"请求任务列表");
					write(builder.build(), ctx.channel());
				}
			});

			// return ;
		}
		if (new Random().nextInt(10) > 8) {
			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					GMsg_11002009.Builder builder = GMsg_11002009.newBuilder();// 任务
					LogUtil.error(ctx.channel().attr(nameKey).get() +"统计数据");
					write(builder.build(), ctx.channel());
				}
			});

			// return ;
		}
		if (new Random().nextInt(10) > 5) {
			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					GMsg_11001002.Builder builder = GMsg_11001002.newBuilder();// 任务
					LogUtil.error(ctx.channel().attr(nameKey).get() +"心跳包");
					write(builder.build(), ctx.channel());
				}
			});

			// return ;
		}
		
		if (new Random().nextInt(10) > 7) {
			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					//GMsg_11006003
					GMsg_11006003.Builder onlineBuilder = GMsg_11006003.newBuilder();
					write(onlineBuilder.build(),ctx.channel());
					
					GMsg_11006004.Builder builder = GMsg_11006004.newBuilder();
					LogUtil.error(ctx.channel().attr(nameKey).get() +"游戏类型在线数据");
					builder.setGameType(GameTypes[new Random().nextInt(GameTypes.length)]);
					write(builder.build(), ctx.channel());
				}
			});

			// return ;
		}

		if (new Random().nextInt(10) > 5) {
			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					GMsg_11006008.Builder builder = GMsg_11006008.newBuilder();
					LogUtil.error(ctx.channel().attr(nameKey).get() +"请求进入房间");
					write(builder.build(), ctx.channel());
				}
			});

			// return ;
		}

		if (new Random().nextInt(10) < 10) {
			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					GMsg_11006001.Builder builder2 = GMsg_11006001.newBuilder();
					int gameType = GameTypes[new Random().nextInt(GameTypes.length)];
					builder2.setGameType(gameType);
					builder2.setRoomType(1);
					ctx.channel().attr(gameTypeKey).set(gameType);
					ctx.channel().attr(statusKey).set(1);
					LogUtil.error(ctx.channel().attr(nameKey).get() +"进入房间");
					write(builder2.build(), ctx.channel());
				}
			});
		}
	}

	public void login(Channel channel) {
		String openId = "player_"+group+"_"+i;
		channel.attr(serialNumberKey).set(i);
		int now = TimeUtil.time();
		Map<String, Object> sign = new HashMap<String, Object>();
		sign.put("openId", openId);
		sign.put("time", 1);
		String md5Key = MD5Util.makeSign(sign, "^_^dfh3:start@2015-09-24!");
		GMsg_11001001.Builder builder = GMsg_11001001.newBuilder();
		builder.setOpenId(openId);
		builder.setTime(now);
		Map<String, Object> sign2 = new HashMap<String, Object>();
		sign2.put("openId", openId);
		sign2.put("time", now);
		String strSign = MD5Util.makeSign(sign2, md5Key);
		builder.setSign(strSign);
		channel.attr(nameKey).set(openId);
		
		channel.attr(loginTimeKey).set(String.valueOf(System.currentTimeMillis()));//记录登录时间
		LogUtil.error("******************************" + openId);
		write(builder.build(), channel);
		
		
	}

	public void write(GeneratedMessageLite msg, Channel channel) {
		if (channel == null || msg == null || !channel.isWritable()) {
			return;
		}
		Message message = Message.build(msg);
		if(channel.isActive() && channel.isWritable()){
			ChannelFuture future  = channel.writeAndFlush(message);
			future.addListener(new GenericFutureListener<ChannelFuture>()  {
				@Override
				public void operationComplete(ChannelFuture future)
						throws Exception {
					if (future.isSuccess()) {
						LogUtil.error("发送成功*************************");
					} else {
						LogUtil.error("发送失败》》》》》》》》》》》》》》》》》》》"+future.cause().getMessage());
					}
				}
			});
		}else{
			LogUtil.error("channel 失活或不可写");
		}
		
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		LogUtil.error(ctx.channel().attr(nameKey).get() + ">>>>>用户已经离线。。。");
		ThreadPool.scheduled(new Runnable() {

			@Override
			public void run() {
				NettyClient.connectToCenter(ip,port,i,group);
			}
		}, 5, TimeUnit.SECONDS);// 重连
	}
	/**
	 * 登录后进行的相关接口调用
	 * @param channel
	 */
	private void loginedRequest(Channel channel){
		//请求物品箱
		GMsg_11005001.Builder builder = GMsg_11005001.newBuilder();
		write(builder.build(), channel);
		//请求邮件列表
		GMsg_11015004.Builder builder2 = GMsg_11015004.newBuilder();
		write(builder2.build(), channel);
		//获取用户的兑换信息
		write(GMsg_11017001.newBuilder().build(), channel);
		//聊天接口
		write(GMsg_11008004.newBuilder().build(), channel);
		//获取游戏在线
		write(GMsg_11006003.newBuilder().build(), channel);
	}
	public void recieveMessage(Channel channel,int protocol){
		switch (protocol) {
		
		case 12001001:
			LogUtil.info(channel.attr(nameKey)+"登录成功-----------"+protocol);
			
			long currTime = System.currentTimeMillis();
			LogUtil.info(channel.attr(nameKey)+"进行登录时长: "+(currTime-Long.valueOf(channel.attr(loginTimeKey).toString())));
			loginedRequest(channel);
			break;
		case 12005001:
			LogUtil.info(channel.attr(nameKey)+"请求物品箱---------"+protocol);
		case 12015004:
			LogUtil.info(channel.attr(nameKey)+"请求邮件列表-------"+protocol);
		case 12017001:
			LogUtil.info(channel.attr(nameKey)+"获取用户的兑换信息--"+protocol);
		case 12008004:
			LogUtil.info(channel.attr(nameKey)+"聊天接口-----------"+protocol);
		
		case 12006001:
			LogUtil.info(channel.attr(nameKey)+"进入房间-----------"+protocol);
			break;
		case 12006002:
			LogUtil.info(channel.attr(nameKey)+"成功组建房间-----------"+protocol);
			break;
		case 12006003:
			LogUtil.info(channel.attr(nameKey)+"请求游戏大厅数据-----------"+protocol);
			break;
		case 12006004:
			LogUtil.info(channel.attr(nameKey)+"请求类型为"+channel.attr(gameTypeKey)+"的游戏大厅数据-----------"+protocol);
			break;
		case 12006007:
			LogUtil.info(channel.attr(nameKey)+"超时请求牌桌-----------"+protocol);
			break;
		case 12006008:
			LogUtil.info("进入房间广播-----------"+protocol);
			break;
		case 12006005:
			LogUtil.info("所在房间有人退出房间-----------"+protocol);
			break;
		case 12006009:
			LogUtil.info(channel.attr(nameKey)+"取消匹配-----------"+protocol);
			break;
		case 12012008:
			LogUtil.info(channel.attr(nameKey)+"收到斗地主---本局结束信息-----------"+protocol);
			break;
		case 12013002:
			LogUtil.info(channel.attr(nameKey)+"收到闷鸡---开局信息-----------"+protocol);
			break;
		case 12013005:
			LogUtil.info(channel.attr(nameKey)+"收到闷鸡---自己看牌信息-----------"+protocol);
			break;
		case 12013008:
			LogUtil.info(channel.attr(nameKey)+"收到闷鸡---本局结束信息-----------"+protocol);
			break;
		case 12011009:
			LogUtil.info(channel.attr(nameKey)+"收到麻将---本局结算信息-----------"+protocol);
			break;
		
		default:
			break;
		}
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (!(msg instanceof Message)) {
			return;
		}
		Message message = (Message) msg;
		int protocol = message.getProtocol();
		LogUtil.info("Message protocol: "+protocol);
		
		recieveMessage(ctx.channel(), protocol);
		
		
		Class<? extends GeneratedMessageLite> clazz = ProtobufCenter
				.getProtobufClass(protocol);
		try {
			Method method = clazz.getMethod("parseFrom", byte[].class);
			Object object = method.invoke(clazz, message.getMsgBody());
				if (12006002==protocol) {
						final Integer gameType = ctx.channel().attr(gameTypeKey).get();
						if(gameType != null){
							ThreadPool.execute(new Runnable() {
								@Override
								public void run() {
									switch (gameType) {
									case 1:// 1焖鸡 2斗地主 3麻将
										LogUtil.error(ctx.channel().attr(nameKey).get() +"+++++++++++++++++++焖鸡");
										write(GMsg_11013001.newBuilder().build(), ctx.channel());
										break;
									case 2:
										LogUtil.error(ctx.channel().attr(nameKey).get() +"+++++++++++++++++++斗地主");

										write(GMsg_11012001.newBuilder().build(), ctx.channel());
										break;
									case 3:
										LogUtil.error(ctx.channel().attr(nameKey).get() +"+++++++++++++++++++麻将");
										write(GMsg_11011001.newBuilder().build(), ctx.channel());
										break;
									}
								}
							});
						}
						
					return;
				}
//				LogUtil.error(ctx.channel().attr(a).get() + ">>>>>GMsg_"+ protocol + "===" + JSONObject.encode(object));
			if (12006005==protocol|| 12006007==protocol) {
				ThreadPool.simpleExecute(new Runnable() {
					@Override
					public void run() {
						// 结束游戏。与重连
						if (false && new Random().nextInt(10) <3) {  //不结束游戏，保持在线玩家数量
							ctx.channel().attr(statusKey).set(2);
						//	final int i = ctx.channel().attr(b).get();
							try {
								ctx.channel().closeFuture().awaitUninterruptibly();
							} catch (Exception e) {
								LogUtil.error(e);
							}
						} else {
							ctx.channel().attr(statusKey).set(0);
							try {
								doSomething(ctx);
							} catch (InterruptedException e) {
								LogUtil.error(e);
							}
						}
						
					}
				});
				
			}

			// ctx.fireChannelRead(msg);
			// LogUtil.error("GMsg_"+protocol+JSONObject.encode(clazz.getMethod("parseFrom",
			// byte[].class).invoke(clazz, message.getMsgBody())));
		} catch (Exception e) {
			LogUtil.error(e);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		LogUtil.error("exceptionCaught*****************" + cause);
		ctx.channel().closeFuture().awaitUninterruptibly();
	}
}
