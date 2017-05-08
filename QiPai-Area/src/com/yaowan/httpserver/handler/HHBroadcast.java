package com.yaowan.httpserver.handler;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yaowan.protobuf.game.GChat.GMsg_12008002;
import com.yaowan.server.game.function.RoleFunction;

/**
 * 格式
 * http格式:http://ip:port?action=HHClassName.methodName&param1=value1&param2=
 * value2&...
 * 该路径
 * http格式:http://ip:port?action=Broadcast.broadcast&message=value 
 */
@Service("HHBroadcast")
public class HHBroadcast {
	
	@Autowired
	RoleFunction roleFunction;
	
	/**
	 * 该类要删除
	 * http调用全局广播
	 * @param params
	 */
	public void broadcast(Map<String, String> params)
	{		
		GMsg_12008002.Builder builder = GMsg_12008002.newBuilder();
		builder.setRid(0);
		builder.setNick("系统");
		builder.setType(2);
		builder.setMessage(params.get("message"));
		roleFunction.sendMessageToAll(builder.build());	
	}
	
}
