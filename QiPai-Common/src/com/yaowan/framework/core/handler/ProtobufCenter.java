/**
 * 
 */
package com.yaowan.framework.core.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.protobuf.GeneratedMessageLite;
import com.yaowan.framework.util.CommonUtils;
import com.yaowan.framework.util.FileUtil;
import com.yaowan.framework.util.LogUtil;

/**
 * @author huangyuyuan
 * 
 * 协议中心，所有的传输协议都应该被注册到本类中
 * 
 * 分4段：
 * 第一段：左边第1位，(1：客户端和游戏服通讯，
 *                    2：游戏服和中心服通讯，
 *                    3：游戏服和代理服通讯)；
 * 第二段：左边第2位，1/2(请求/响应)；
 * 第三段：左边第3、4、5位(功能模块)；
 * 第四段：最后3位(具体的处理方法)；
 * 例：11001001
 */
public class ProtobufCenter {

	/**
	 * <protobufClass, protocol>
	 */
	private static Map<Class<? extends GeneratedMessageLite>, Integer> protocolMap = new HashMap<Class<? extends GeneratedMessageLite>, Integer>();
	/**
	 * <protocol, protobufClass>
	 */
	private static Map<Integer, Class<? extends GeneratedMessageLite>> protocolClzMap = new HashMap<Integer, Class<? extends GeneratedMessageLite>>();
	/**
	 * 注册协议
	 * 
	 * @param protocol
	 * @param clz
	 */
	private static void register(int protocol, Class<? extends GeneratedMessageLite> clz) {
		if(protocolMap.containsKey(clz) || protocolMap.containsValue(protocol)) {
			LogUtil.error("Duplicate protocol center register " + protocol);
			Class<? extends GeneratedMessageLite> clzed = protocolClzMap.get(protocol);
			throw new RuntimeException("Duplicate protocol center register:" + protocol + "-" + clz + "-" + clzed);
		}
		protocolMap.put(clz, protocol);
		protocolClzMap.put(protocol, clz);
	}
	/**
	 * 根据类型获取协议号
	 * 
	 * @param clz
	 * @return
	 */
	public static int getProtocol(Class<? extends GeneratedMessageLite> clz) {
		if(!protocolMap.containsKey(clz)) {
			LogUtil.error(clz.getSimpleName() + " not register");
			throw new RuntimeException(clz.getSimpleName() + " not register");
		}
		return protocolMap.get(clz);
	}
	/**
	 * 根据协议号获取类型
	 * 
	 * @param protocol
	 * @return
	 */
	public static Class<? extends GeneratedMessageLite> getProtobufClass(int protocol) {
		if(!protocolClzMap.containsKey(protocol)) {
			LogUtil.error(protocol + " not register");
			throw new RuntimeException(protocol + " not register");
		}
		return protocolClzMap.get(protocol);
	}
	/**
	 * 
	 * 
	 * @return
	 */
	public static Map<Integer, Class<? extends GeneratedMessageLite>> getProtocolClzMap() {
		return protocolClzMap;
	}
	/**
	 * 协议打印
	 * 
	 * @param protocol
	 * @return
	 */
	public static String toString(int protocol) {
		if(!protocolClzMap.containsKey(protocol)) {
			LogUtil.error(protocol + " not register");
			throw new RuntimeException(protocol + " not register");
		}
		return protocol + "-" + protocolClzMap.get(protocol).getSimpleName();
	}
	
	@SuppressWarnings("unchecked")
	public static void init() {
		String[] protobufPaths = new String[]{"com.yaowan.protobuf"};
		for(String protobufPath : protobufPaths) {
			Set<Class<?>> clazzSet = FileUtil.getClasses(protobufPath);
			
			for(Class<?> clazz : clazzSet) {
				if(GeneratedMessageLite.class.isAssignableFrom(clazz)) {
					if(!clazz.getSimpleName().contains("Msg_")) {
						continue;
					}
					String[] protocol = clazz.getSimpleName().split("Msg_");
					
					register(CommonUtils.parseInt(protocol[1]), (Class<? extends GeneratedMessageLite>) clazz);
				}
			}
		}
		System.out.println("protocol size " + protocolMap.size());
	}
}
