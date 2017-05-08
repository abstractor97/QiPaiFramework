package com.yaowan;

import com.yaowan.core.base.GlobalConfig;
import com.yaowan.framework.core.model.AbstractServer;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.protobuf.center.CError.CMsg_22003001;

/**
 * @author zane
 *
 */
public class CenterErrorMsg {
	/**
	 * 发送错误提示
	 * @param player
	 * @param errorCode
	 * @param protocol
	 */
	public static void send(AbstractServer server, int errorCode, int protocol) {
		CMsg_22003001.Builder builder = CMsg_22003001.newBuilder();
		builder.setErrorCode(errorCode);
		builder.setProtocol(protocol);
		server.write(builder.build());
		
		showLog(server.getServerId(), errorCode, protocol);
	}
	/**
	 * 发送错误提示
	 * @param player
	 * @param errorCode
	 * @param protocol
	 * @param t
	 */
	public static void send(AbstractServer server, int errorCode, int protocol, String t) {
		CMsg_22003001.Builder builder = CMsg_22003001.newBuilder();
		builder.setErrorCode(errorCode);
		builder.setProtocol(protocol);
		if(t != null && GlobalConfig.debug) {
			//StackTraceElement[] elements = t.getStackTrace();
			StringBuilder msg = new StringBuilder();
			msg.append(t);
			/*for(StackTraceElement element : elements) {
				msg.append("\n\tat " + element);
			}*/
			builder.setErrorMsg(msg.toString());
		}
		server.write(builder.build());
	}
	
	/**
	 * 错误信息日志
	 * @param rid
	 * @param errorCode
	 * @param protocol
	 */
	private static void showLog(int serverId, int errorCode, int protocol) {
		StackTraceElement[] element = new RuntimeException().getStackTrace();
		if (element.length >= 3) {
			LogUtil.error(new StringBuilder(40).append("serverId:").append(serverId).append(" protocol:")
					.append(protocol).append(" errorCode:").append(errorCode).toString()
					+ " " + element[2].toString());
		} else if (element.length >= 4) {
			LogUtil.error(new StringBuilder(40).append("serverId:").append(serverId).append(" protocol:")
					.append(protocol).append(" errorCode:").append(errorCode).toString()
					+ " " + element[3].toString());
		}
	}
}
