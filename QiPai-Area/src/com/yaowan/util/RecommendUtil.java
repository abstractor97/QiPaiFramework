package com.yaowan.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * 推荐码处理类
 * @author YW0981
 *
 */
public class RecommendUtil {
	
	/**
	 * 创建推荐码
	 * @param rid
	 * @return
	 */
	public static String createCode(long rid) {
		String id = rid + "";
		//服务器id
		String serverId = id.substring(0, 5);
		//最后六位id
		String lastSixId = "";
		if(serverId.equals("10014")) {
			//取出用户的后六位id
			serverId = "101";
			lastSixId = id.substring(6, id.length());
		}else {
			serverId = id.substring(0, 3);
			lastSixId = id.substring(4, id.length());
		}
		return pingCode(serverId, lastSixId);
	}
	
	/**
	 * 拼接打乱验证码
	 * @param serverId
	 * @param lastSixId
	 * @return
	 */
	public static String pingCode(String serverId, String lastSixId) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(lastSixId.substring(0, 1));
		stringBuilder.append(serverId.substring(0, 1));
		stringBuilder.append(lastSixId.substring(1, 3));
		stringBuilder.append(serverId.substring(1, 2));
		stringBuilder.append(lastSixId.substring(3, lastSixId.length()));
		stringBuilder.append(serverId.substring(2, serverId.length()));
		return stringBuilder.toString();
	}
	
	/**
	 * 得到服务器id
	 * @param code
	 * @return
	 */
	public static String getServerId(String code) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(code.substring(1, 2));
		stringBuilder.append(code.substring(4, 5));
		stringBuilder.append(code.substring(code.length() - 1, code.length()));
		String serverId = stringBuilder.toString();
		if(serverId.equals("101")) {
			serverId = "10014";
		}
		return serverId;
	}
	
	

}
