/**
 * 
 */
package com.yaowan.framework.netty;

import com.google.protobuf.GeneratedMessageLite;
import com.yaowan.framework.core.handler.ProtobufCenter;

/**
 * @author huangyuyuan
 *
 */
public class Message {
	/**
	 * 协议号
	 */
	private int protocol;
	/**
	 * 协议体
	 */
	private byte[] msgBody;
	
	private Message() {
		
	}
	
	public static Message build(int protocol, byte[] msgBody) {
		Message message = new Message();
		message.setProtocol(protocol);
		message.setMsgBody(msgBody);
		return message;
	}
	
	public static Message build(GeneratedMessageLite msg) {
		int protocol = ProtobufCenter.getProtocol(msg.getClass());
		return build(protocol, msg.toByteArray());
	}

	public int getProtocol() {
		return protocol;
	}

	private void setProtocol(int protocol) {
		this.protocol = protocol;
	}

	public byte[] getMsgBody() {
		return msgBody;
	}

	private void setMsgBody(byte[] msgBody) {
		this.msgBody = msgBody;
	}
}
