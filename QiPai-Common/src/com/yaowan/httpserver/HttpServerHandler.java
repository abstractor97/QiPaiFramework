package com.yaowan.httpserver;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.yaowan.constant.ChannelConst;
import com.yaowan.core.base.GlobalConfig;
import com.yaowan.core.base.Spring;
import com.yaowan.framework.core.handler.server.IServerExecutor;
import com.yaowan.framework.core.handler.server.ServerDispatcher;
import com.yaowan.framework.netty.Message;
import com.yaowan.framework.util.JSONObject;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.model.struct.Player;


public class HttpServerHandler extends ChannelInboundHandlerAdapter {
	private static final byte[] CONTENT = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd' };

	private static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");
    private static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");
    private static final AsciiString CONNECTION = new AsciiString("Connection");
    private static final AsciiString KEEP_ALIVE = new AsciiString("keep-alive");
    
    private static final byte[] EMPTY = {};
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); //Disk
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;

            if (HttpUtil.is100ContinueExpected(req)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }
            InetSocketAddress  socketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
            String validString = socketAddress.getHostName();
            if(!GlobalConfig.isTest){
            	if(GlobalConfig.HttpValidCheck.indexOf(validString) == -1){
            		LogUtil.info("HTTP Request IP 只能是： "+GlobalConfig.HttpValidCheck);
            		return;
            	}
            }
            HttpHeaders headers = req.headers();
    		
    		Map<String, String> params = new HashMap<String, String>();
    		
    		if (req.method()==HttpMethod.GET) { // 处理get请求  
    			// 屏蔽掉非websocket握手请求
    			// 只接受http GET和headers['Upgrade']为'websocket'的http请求
    			if ("websocket".equalsIgnoreCase(headers.get("Upgrade"))) {
    				
    				WebSocketServerHandshakerFactory wsShakerFactory = new WebSocketServerHandshakerFactory(
    						"ws://" + req.headers().get(HttpHeaders.Names.HOST), null, false);
    				WebSocketServerHandshaker wsShakerHandler = wsShakerFactory.newHandshaker(req);
    				if (null == wsShakerHandler) {
    					// 无法处理的websocket版本
    					WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
    				} else {
    					// 向客户端发送websocket握手,完成握手
    					// 客户端收到的状态是101 sitching protocol
    					wsShakerHandler.handshake(ctx.channel(), req);
    				}
    				return;
    			} else {
    				
    				QueryStringDecoder queryStringDecoder = new QueryStringDecoder(
    						req.uri(), CharsetUtil.UTF_8);
    				Map<String, List<String>> paramsTmp = queryStringDecoder.parameters();
    				for (Entry<String, List<String>> entry : paramsTmp.entrySet()) {
    					for (String v : entry.getValue()) {
    						params.put(entry.getKey(), v);
    						break;
    					}
    				}
    			}
    		}
    		
    		if (req.method()==HttpMethod.POST) { // 处理POST请求 
    			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(
    					factory, req, CharsetUtil.UTF_8);
    			
    			// 读取从客户端传过来的参数  
    			/*ChannelBuffer buffer = req.decoderResult();
    			if (buffer != null) {
    				params.put("post_context", new String(buffer.array()));
    			}*/
    			if( decoder.hasNext()) {
    				List<InterfaceHttpData> dataList = decoder.getBodyHttpDatas();
    				QueryStringDecoder queryStringDecoder = new QueryStringDecoder(
    						req.uri(), CharsetUtil.UTF_8);
    				Map<String, List<String>> paramsTmp = queryStringDecoder.parameters();
    				for (Entry<String, List<String>> entry : paramsTmp.entrySet()) {
    					for (String v : entry.getValue()) {
    						params.put(entry.getKey(), v);
    						break;
    					}
    				}
    				for (InterfaceHttpData interfaceHttpData : dataList) {
    					Attribute attribute = (Attribute) interfaceHttpData;
    					try {
							params.put(attribute.getName(), attribute.getValue());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    					
    				}
    			}
    			
    		}
    		
    		byte[] content = EMPTY;
    		try {
    			content = handleRequest(req, params);
    		} catch (Exception ex) {
    			LogUtil.error(ex);
    		}
            
            
            boolean keepAlive = HttpUtil.isKeepAlive(req);
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(content));
            response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
            response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());


            if (!keepAlive) {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                response.headers().set(CONNECTION, KEEP_ALIVE);
                ctx.write(response);
            }
        }else{
        	if (msg instanceof CloseWebSocketFrame) {
        		ctx.channel().close();
    		}else if (msg instanceof TextWebSocketFrame) {
                // Send the uppercase string back.
                String request = ((TextWebSocketFrame) msg).text();
                LogUtil.info("{} received {}"+ request);
                ctx.channel().writeAndFlush(new TextWebSocketFrame(request.toUpperCase(Locale.US)));
            } else if (msg instanceof BinaryWebSocketFrame) {
                // Send the uppercase string back.
                BinaryWebSocketFrame binReq = (BinaryWebSocketFrame) msg;
                ByteBuf buffer = binReq.content();
    			// 没有用，但是要移到读取下标
    			int len = buffer.readInt();
    			int protocol = buffer.readInt();
    			byte[] msgBody = new byte[len-4];
    			if(buffer.isDirect()){
    				buffer.getBytes(8, msgBody);
    			}else{
    				msgBody = buffer.readBytes(len-4).array();
    			}
    			
    			
    			//System.arraycopy(allData, 0, msgBody, 0, allData.length);
    			
    			Message gMessage = Message.build(protocol, msgBody);
    			
    			
    			Player player = (Player) ctx.channel().attr(ChannelConst.PLAYER).get();
    			if (player == null || player.getChannel() == null) {
    				return;
    			}
    			// 是否为需要登录的请求
    			if (!ServerDispatcher.isRequestValid(player, protocol)
    					&& player.getRole() == null) {
    				LogUtil.error("Protocol " + protocol + " request need login");
    				player.getChannel().disconnect();
    				return;
    			}


    			// 限制请求过多
    			// if (ServerDispatcher.isTooMuchRequest(player.socket, protocol)) {
    			// GError.send(player, GameError.SYSTEM_REQUEST_IS_TOO_FAST, false, "");
    			// return;
    			// }
    			// TODO 记录执行时间
    			// long startTime = System.currentTimeMillis();
    			// TODO 需要考虑是否手动进行线程分配
    			IServerExecutor executor = ServerDispatcher.getExecutor(protocol);
    			if (executor == null) {
    				LogUtil.error("Executor " + protocol + " not found");
    				return;
    			}
    			try {
    				executor.execute(player, gMessage.getMsgBody());
    			} catch (Exception e) {
    				LogUtil.error(e);
    			}
            } else {
    			// WebSocketFrame还有一些
    			LogUtil.debug("request:" + msg);
    		}
        }
    }
    
    /**
	 * 处理命令执行请求
	 * 
	 * @param params
	 * @return
	 */
	public byte[] handleRequest(HttpRequest request, Map<String, String> params) {
		Object target = null;
		try {
			String[] arr = null;
			if (params.containsKey("action")) {
				String action = params.get("action");
				if (action == null) {
					return EMPTY;
				}
				arr = action.split("\\.");
				if (arr.length != 2) {
					return EMPTY;
				}
			} else {
				// http://localhost:9801/user/admin  支持这种格式访问
				if (request.uri() == null || request.uri().length() < 1) {
					return EMPTY;
				}
				String uri = request.uri();
				int endIndex = uri.indexOf('?');
				if (endIndex > 1) {
					uri = uri.substring(1, endIndex);
				} else {
					uri = uri.substring(1);
				}
				
				arr = uri.split("/");
			}
			// class 和  action 都不为空
			if (null != arr && arr.length > 1 && 
					StringUtils.isNotBlank(arr[0]) && StringUtils.isNotBlank(arr[1])) {
				// need update...
			//	System.out.println(StringUtil.firstToUpperCase(arr[0]));
				target = Spring.getBean("HH" + StringUtil.firstToUpperCase(arr[0]));
				Method method = target.getClass().getMethod(arr[1], Map.class);
				Object ret = null;
				try {
					ret = method.invoke(target, params);
				}catch (InvocationTargetException e) {
//					e.getTargetException().printStackTrace();
					LogUtil.error(e);
				}
				if (ret == null) {
					return EMPTY;
				}
				if(params.containsKey("file")) {
					return (byte[])ret;
				} else {
					
					return JSONObject.encode(ret).getBytes();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return EMPTY;
	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
    @Override
    public void channelRegistered(ChannelHandlerContext ctx){
    	Player player = new Player(ctx.channel());
		// 记录是第几个玩家，用于给玩家分配一个逻辑处理线程
    	player.setClientType(1);
    	ctx.channel().attr(ChannelConst.PLAYER).set(player);
		LogUtil.info(ctx.channel().toString());
    }
    
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx){
    	Player player = ctx.channel().attr(ChannelConst.PLAYER).get();
		if(player != null) {
			IServerExecutor executor = ServerDispatcher.getExecutor(11001003);
			if(executor != null) {
				try {
					executor.execute(player, null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				LogUtil.error("Executor 11001003 not found");
			}
		}
    }
}