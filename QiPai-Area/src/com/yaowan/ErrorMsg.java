/**
 * 
 */
package com.yaowan;

import com.yaowan.framework.core.handler.AbstractLink;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GError.GMsg_12003001;

/**
 * @author zane
 *
 */
public class ErrorMsg {
	/**
	 * 发送错误提示
	 * @param player
	 * @param errorCode
	 * @param protocol
	 */
	public static void send(AbstractLink link, int errorCode, int protocol) {
		GMsg_12003001.Builder builder = GMsg_12003001.newBuilder();
		builder.setErrorCode(errorCode);
		builder.setProtocol(protocol);
		link.write(builder.build());
		
		showLog(link.getId(), errorCode, protocol);
	}
	
	/**
	 * 发送错误提示
	 * 
	 * @param player
	 * @param errorCode
	 * @param protocol
	 * @param t
	 */
	public static void send(Player player, int errorCode, int protocol, String t) {
		GMsg_12003001.Builder builder = GMsg_12003001.newBuilder();
		builder.setErrorCode(errorCode);
		builder.setProtocol(protocol);
		if (t != null) {
			// StackTraceElement[] elements = t.getStackTrace();
			StringBuilder msg = new StringBuilder();
			msg.append(t);
			/*
			 * for(StackTraceElement element : elements) { msg.append("\n\tat "
			 * + element); }
			 */
			builder.setErrorMsg(msg.toString());
			LogUtil.error("errorCode" + errorCode + ":protocol:" + protocol
					+ ":msg:" + t + ":player:" + player);
		}
		if(player!=null){
			player.write(builder.build());
		}
		
	}
	/**
	 * 错误信息日志
	 * @param id
	 * @param errorCode
	 * @param protocol
	 */
	private static void showLog(long id, int errorCode, int protocol) {
		StackTraceElement[] element = new RuntimeException().getStackTrace();
		if (element.length >= 3) {
			LogUtil.error(new StringBuilder(40).append("id:").append(id).append(" protocol:")
					.append(protocol).append(" errorCode:").append(errorCode).toString()
					+ " " + element[2].toString());
		} else if(element.length >= 4) {
			LogUtil.error(new StringBuilder(40).append("id:").append(id).append(" protocol:")
					.append(protocol).append(" errorCode:").append(errorCode).toString()
					+ " " + element[3].toString());
		}
	}
}
