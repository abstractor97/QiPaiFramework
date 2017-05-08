package com.yaowan.framework.core.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import com.yaowan.framework.netty.Message;

/**
 * @author huangyuyuan
 * 
 * 协议类包装工具
 */
public class NetUtil {
	
	public static ByteBuf wrapBuffer(Message msg) {
		
		int protocol = msg.getProtocol();
		
		byte[] data = msg.getMsgBody();
		
		//消息长度=协议号4位+数据体长度
		int length = data.length + 4;
		//数据包=消息长度+协议号+数据体
		//数据包长度=4+消息长度

		ByteBuf buffer = Unpooled.buffer(length + 4);//.buffer(ByteOrder.BIG_ENDIAN, );
		buffer.writeInt(length);
		buffer.writeInt(protocol);
		buffer.writeBytes(data);
		return buffer;
	}
}